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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Joins a collection of resolved source values into a single string.
 * <p>
 * Supported args:
 * <ul>
 *   <li>{@code separator} - inserted between rendered items (default {@code " "}).</li>
 *   <li>{@code prefix} - prepended to the joined result (default empty).</li>
 *   <li>{@code itemFormat} - a template applied to every item that is a structured
 *       (object) value, e.g. {@code "{name}={value}"}. Bare scalar items (e.g. tags)
 *       are emitted verbatim. When absent, every item is rendered with
 *       {@code String.valueOf}.</li>
 * </ul>
 * The engine correlates sibling source fields (e.g. a property's {@code name} and
 * {@code value}) into one object per entity before calling this transform, so the
 * {@code itemFormat} placeholders resolve against a single entity at a time.
 */
@Component
public class JoinTransform implements TransformFunction {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z0-9_.\\-]+)\\}");

    public String getName() { return "join"; }

    public Object transform(Object value, Map<String,Object> args) {
        Map<String, Object> safeArgs = args == null ? Map.of() : args;
        String separator = String.valueOf(safeArgs.getOrDefault("separator", " "));
        String prefix = safeArgs.containsKey("prefix") ? String.valueOf(safeArgs.get("prefix")) : "";
        Object itemFormatArg = safeArgs.get("itemFormat");
        String itemFormat = itemFormatArg == null ? null : String.valueOf(itemFormatArg);

        if (value instanceof Collection<?> collection) {
            List<String> parts = new ArrayList<>();
            for (Object item : collection) {
                String rendered = renderItem(item, itemFormat);
                if (!rendered.isEmpty()) parts.add(rendered);
            }
            return prefix + String.join(separator, parts);
        }
        return String.valueOf(value);
    }

    private String renderItem(Object item, String itemFormat) {
        if (item == null) return "";
        if (itemFormat != null && item instanceof JsonNode node && node.isObject()) {
            return substitute(itemFormat, node);
        }
        if (item instanceof JsonNode node) {
            return nodeText(node);
        }
        return String.valueOf(item);
    }

    private String substitute(String template, JsonNode node) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            JsonNode field = node.get(matcher.group(1));
            String replacement = field == null ? "" : nodeText(field);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String nodeText(JsonNode node) {
        if (node.isNull()) return "";
        return node.isValueNode() ? node.asText() : node.toString();
    }
}
