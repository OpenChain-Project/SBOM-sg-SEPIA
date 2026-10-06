/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PathResolver {

    public List<ResolvedValue> resolve(JsonNode source, String path) {
        List<ResolvedValue> current = List.of(new ResolvedValue(source, ""));
        for (String token : path.split("\\.")) {
            List<ResolvedValue> next = new ArrayList<>();
            Segment seg = parseSegment(token);
            for (ResolvedValue rv : current) {
                expandSegment(source, seg, rv, next);
            }
            current = next;
        }
        return current;
    }

    /** Expand one path segment for a single resolved value, appending the results to {@code next}. */
    private void expandSegment(JsonNode root, Segment seg, ResolvedValue rv, List<ResolvedValue> next) {
        JsonNode node = childNode(rv.getValue(), seg.name);
        if (node == null || node.isMissingNode()) {
            return;
        }
        if (!seg.arrayToken) {
            next.add(new ResolvedValue(node, rv.getIndexPath()));
            return;
        }
        if (!node.isArray()) {
            return;
        }
        int i = 0;
        for (JsonNode child : node) {
            // A filter predicate (`[?...]`) selects which array elements survive; the original
            // element index is preserved in the index path so downstream entity correlation stays
            // stable regardless of how many elements matched.
            if (seg.predicate == null || matchesPredicate(child, seg.predicate, root)) {
                next.add(new ResolvedValue(child, appendIndex(rv.getIndexPath(), i)));
            }
            i++;
        }
    }

    private JsonNode childNode(JsonNode value, String name) {
        if (name.isEmpty()) {
            return value;
        }
        return value.isObject() ? value.get(name) : null;
    }

    private String appendIndex(String indexPath, int i) {
        return indexPath.isEmpty() ? Integer.toString(i) : indexPath + "." + i;
    }

    /**
     * Parse one dot-separated path segment into its field name and (optional) array/predicate
     * marker. Supported forms: {@code name}, {@code name[]} (iterate every element) and
     * {@code name[?predicate]} (iterate only matching elements).
     */
    private Segment parseSegment(String token) {
        int bracket = token.indexOf('[');
        if (bracket < 0) {
            return new Segment(token, false, null);
        }
        String name = token.substring(0, bracket);
        String suffix = token.substring(bracket);
        if (suffix.startsWith("[?") && suffix.endsWith("]")) {
            return new Segment(name, true, suffix.substring(2, suffix.length() - 1).trim());
        }
        // Plain "[]" (or any other bracket form) is treated as a full array iteration.
        return new Segment(name, true, null);
    }

    /**
     * Evaluate a filter predicate against a single array element. Supported operators, matching the
     * dialect used by the mapping rules:
     * <ul>
     *   <li>{@code startsWith 'prefix'} - the (scalar) element string begins with the prefix;</li>
     *   <li>{@code field=='value'} / {@code field=="value"} - the element's field equals a literal;</li>
     *   <li>{@code field==otherPath} - the element's field equals a scalar resolved from the
     *       document root (e.g. {@code SPDXID==documentDescribes[0]});</li>
     *   <li>{@code field in ['a','b']} / {@code field not in ['a','b']} - set membership.</li>
     * </ul>
     */
    private boolean matchesPredicate(JsonNode element, String predicate, JsonNode root) {
        if (predicate.startsWith("startsWith ")) {
            String prefix = unquote(predicate.substring("startsWith ".length()).trim());
            return element.isValueNode() && element.asText().startsWith(prefix);
        }
        int notIn = predicate.indexOf(" not in ");
        if (notIn >= 0) {
            String field = predicate.substring(0, notIn).trim();
            List<String> values = resolveSetValues(predicate.substring(notIn + " not in ".length()), root);
            return !values.contains(fieldText(element, field));
        }
        int in = predicate.indexOf(" in ");
        if (in >= 0) {
            String field = predicate.substring(0, in).trim();
            List<String> values = resolveSetValues(predicate.substring(in + " in ".length()), root);
            return values.contains(fieldText(element, field));
        }
        int eq = predicate.indexOf("==");
        if (eq >= 0) {
            String field = predicate.substring(0, eq).trim();
            String rhs = predicate.substring(eq + 2).trim();
            String expected = isQuoted(rhs) ? unquote(rhs) : resolveScalar(root, rhs);
            return expected != null && expected.equals(fieldText(element, field));
        }
        return false;
    }

    /** Read a comparable string from an element: the element itself if scalar, else its named field. */
    private String fieldText(JsonNode element, String field) {
        if (element.isValueNode()) {
            return element.asText();
        }
        JsonNode v = element.get(field);
        return v != null && v.isValueNode() ? v.asText() : null;
    }

    /** Resolve a simple scalar path (dot navigation plus {@code [n]} indexing) against a root node. */
    private String resolveScalar(JsonNode root, String path) {
        JsonNode current = root;
        for (String token : path.split("\\.")) {
            current = navigateScalarToken(current, token);
            if (current == null) {
                return null;
            }
        }
        return current.isValueNode() ? current.asText() : null;
    }

    /** Navigate one {@code name} or {@code name[n]} token of a scalar path; {@code null} if absent. */
    private JsonNode navigateScalarToken(JsonNode current, String token) {
        if (current == null || !current.isObject()) {
            return null;
        }
        String name = token;
        Integer index = null;
        int bracket = token.indexOf('[');
        if (bracket >= 0 && token.endsWith("]")) {
            name = token.substring(0, bracket);
            index = parseIndex(token.substring(bracket + 1, token.length() - 1));
            if (index == null) {
                return null;
            }
        }
        JsonNode node = current.get(name);
        if (index == null) {
            return node;
        }
        if (node == null || !node.isArray() || index >= node.size()) {
            return null;
        }
        return node.get(index);
    }

    private Integer parseIndex(String raw) {
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<String> parseList(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        List<String> values = new ArrayList<>();
        if (trimmed.isBlank()) return values;
        for (String part : trimmed.split(",")) {
            values.add(unquote(part.trim()));
        }
        return values;
    }

    /**
     * Resolve the right-hand side of an {@code in} / {@code not in} predicate into a set of
     * comparable strings. A bracketed literal such as {@code ['a','b']} is parsed directly; any
     * other value is treated as a path expression resolved against the document root (e.g.
     * {@code documentDescribes}), collecting the scalar values of the referenced array or node.
     */
    private List<String> resolveSetValues(String raw, JsonNode root) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return parseList(trimmed);
        }
        List<String> values = new ArrayList<>();
        for (ResolvedValue rv : resolve(root, trimmed)) {
            JsonNode node = rv.getValue();
            if (node == null) {
                continue;
            }
            if (node.isArray()) {
                for (JsonNode el : node) {
                    if (el.isValueNode()) {
                        values.add(el.asText());
                    }
                }
            } else if (node.isValueNode()) {
                values.add(node.asText());
            }
        }
        return values;
    }

    private boolean isQuoted(String s) {
        return s.length() >= 2 && ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\"")));
    }

    private String unquote(String s) {
        return isQuoted(s) ? s.substring(1, s.length() - 1) : s;
    }

    private static final class Segment {
        private final String name;
        private final boolean arrayToken;
        private final String predicate;

        private Segment(String name, boolean arrayToken, String predicate) {
            this.name = name;
            this.arrayToken = arrayToken;
            this.predicate = predicate;
        }
    }

    public void set(JsonNode target, String path, JsonNode value, List<Integer> slots) {
        String[] tokens = path.split("\\.");
        JsonNode current = target;
        int arrayDepth = 0;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            boolean array = token.endsWith("[]");
            String name = array ? token.substring(0, token.length() - 2) : token;
            boolean last = i == tokens.length - 1;
            if (array) {
                ObjectNode obj = (ObjectNode) current;
                ArrayNode arr = obj.withArray(name);
                if (last) {
                    // Array-terminal target (e.g. creationInfo.creators[], relationships[],
                    // packages[].externalRefs[]): the transformed value IS a complete element,
                    // so append it. Appending also lets independent rules accumulate entries
                    // in the same array instead of colliding on a shared source ordinal.
                    arr.add(value);
                    return;
                }
                // Non-terminal array (e.g. packages[].name, or the nested authors[] of
                // components[].authors[].name): each level gets its own correlation slot so
                // distinct source entities land in distinct elements while sibling fields of the
                // same entity are preserved. A missing slot for this level falls back to reusing
                // the last element (scalar/root-singleton and constant-fabrication callers).
                Integer index = slots != null && arrayDepth < slots.size() ? slots.get(arrayDepth) : null;
                arrayDepth++;
                int pos = index == null ? Math.max(0, arr.size() - 1) : index;
                while (arr.size() <= pos) arr.addObject();
                current = arr.get(pos);
            } else {
                if (last) {
                    ((ObjectNode) current).set(name, value);
                } else {
                    // Reuse an existing child object instead of replacing it with a fresh one;
                    // putObject() would discard fields already written by earlier rules
                    // (e.g. creationInfo.created wiped when creationInfo.creators is written).
                    JsonNode child = ((ObjectNode) current).get(name);
                    if (child != null && child.isObject()) {
                        current = child;
                    } else {
                        current = ((ObjectNode) current).putObject(name);
                    }
                }
            }
        }
    }

    public static class ResolvedValue {
        private final JsonNode value;
        private final String indexPath;

        public ResolvedValue(JsonNode value, String indexPath) {
            this.value = value;
            this.indexPath = indexPath;
        }

        public JsonNode getValue() { return value; }
        public String getIndexPath() { return indexPath; }
    }
}
