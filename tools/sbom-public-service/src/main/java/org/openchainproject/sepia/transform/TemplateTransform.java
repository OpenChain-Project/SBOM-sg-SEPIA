/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.transform;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fills a template from the resolved source value.
 * <p>
 * Two shapes are supported:
 * <ul>
 *   <li><b>Scalar form</b> - a single {@code format} arg (or none) produces a
 *       string, e.g. {@code {format: "Organization: {value}"}}.</li>
 *   <li><b>Structured form</b> - any other arg set produces an object whose
 *       keys are the arg names and whose values are the substituted templates,
 *       e.g. {@code {referenceCategory: "PACKAGE-MANAGER", referenceType: "purl",
 *       referenceLocator: "{value}"}}.</li>
 * </ul>
 * Placeholders may be {@code {value}} (the whole resolved value) or a named /
 * dotted field ({@code {name}}, {@code {text.content}}) resolved from the
 * resolved value when it is a JSON object. Unresolved placeholders collapse to
 * an empty string so raw {@code {token}} markers never leak into the output.
 */
@Component
public class TemplateTransform implements TransformFunction {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z0-9_.\\-]+)\\}");

    public String getName() { return "template"; }

    public Object transform(Object value, Map<String,Object> args) {
        return transform(value, args, null);
    }

    @Override
    public Object transform(Object value, Map<String,Object> args, TransformContext context) {
        String field = context == null ? "" : context.getCurrentField();
        String index = context == null ? "" : context.getCurrentIndex();
        if (args == null || args.isEmpty()) {
            return substitute("{value}", value, field, index);
        }
        if (args.containsKey("format")) {
            return substitute(String.valueOf(args.get("format")), value, field, index);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            out.put(entry.getKey(), substitute(String.valueOf(entry.getValue()), value, field, index));
        }
        return out;
    }

    private String substitute(String template, Object value, String field, String index) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = resolveKey(matcher.group(1), value, field, index);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveKey(String key, Object value, String field, String index) {
        switch (key) {
            case "value":
                return scalarString(value);
            case "field":
                return field == null ? "" : field;
            case "index":
                return index == null ? "" : index;
            default:
                return resolveField(key, value);
        }
    }

    private String resolveField(String key, Object value) {
        if (!(value instanceof JsonNode node)) {
            return "";
        }
        // 1. Exact field match. The engine may pre-flatten correlated sibling fields under
        //    literal keys such as "text.content" or "dependsOn", so try a direct lookup first.
        JsonNode direct = node.get(key);
        if (isPresent(direct)) {
            return render(direct);
        }
        // 2. Dotted navigation ({a.b} -> node.get("a").get("b")).
        JsonNode current = node;
        for (String part : key.split("\\.")) {
            if (current == null) break;
            current = current.get(part);
        }
        if (isPresent(current)) {
            return render(current);
        }
        // 3. Fall back to a recursive search for the (last) field name so loosely written
        //    placeholders like {email} still resolve when the value is nested (e.g. contact[].email).
        String leaf = key.contains(".") ? key.substring(key.lastIndexOf('.') + 1) : key;
        JsonNode deep = searchField(node, leaf);
        return isPresent(deep) ? render(deep) : "";
    }

    private boolean isPresent(JsonNode n) {
        return n != null && !n.isNull() && !n.isMissingNode();
    }

    private String render(JsonNode n) {
        return n.isValueNode() ? n.asText() : n.toString();
    }

    private JsonNode searchField(JsonNode node, String field) {
        if (node == null) return null;
        if (node.isObject()) {
            JsonNode hit = node.get(field);
            if (isPresent(hit)) return hit;
            for (JsonNode child : node) {
                JsonNode found = searchField(child, field);
                if (found != null) return found;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = searchField(child, field);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String scalarString(Object value) {
        if (value == null) return "";
        if (value instanceof JsonNode node) {
            return node.isValueNode() ? node.asText() : node.toString();
        }
        return String.valueOf(value);
    }
}

