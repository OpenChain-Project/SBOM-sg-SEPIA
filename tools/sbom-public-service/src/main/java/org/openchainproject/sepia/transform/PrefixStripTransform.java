/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.transform;

import org.openchainproject.sepia.exception.ConversionException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recovers a bare value from a string that carries a structured prefix,
 * e.g. SPDX {@code creationInfo.creators[]} entries such as
 * {@code "Organization: Acme Corp"} -> {@code "Acme Corp"}.
 * <p>
 * Accepts either {@code args.prefix} (single string) or {@code args.prefixes}
 * (list of candidate strings); the longest matching prefix wins. When no
 * prefix matches, the default behaviour ({@code onNoMatch: unmappable}) is
 * to throw, so the caller's existing {@code onUnmappable} policy handling
 * applies; set {@code onNoMatch: passthrough} to return the value unchanged
 * instead.
 * <p>
 * When {@code args.prefixMap} is supplied (an ordered map of prefix -> dotted
 * target sub-path), the matched prefix additionally selects a CycloneDX
 * annotator alternative: the stripped value is wrapped in the nested object
 * named by the mapped sub-path, e.g. {@code "Person: Jane Doe"} with
 * {@code {"Person: ": individual.name}} -> {@code {"individual": {"name":
 * "Jane Doe"}}}. The longest matching prefix wins.
 */
@Component
public class PrefixStripTransform implements TransformFunction {

    public String getName() { return "prefixStrip"; }

    @Override
    public Object transform(Object value, Map<String, Object> args) {
        if (value == null) return null;
        String text = String.valueOf(value);
        Map<String, String> prefixMap = resolvePrefixMap(args);
        if (prefixMap != null) {
            for (Map.Entry<String, String> e : prefixMap.entrySet()) {
                if (text.startsWith(e.getKey())) {
                    String stripped = text.substring(e.getKey().length()).trim();
                    return nest(e.getValue(), stripped);
                }
            }
            String onNoMatch = String.valueOf(args.getOrDefault("onNoMatch", "unmappable"));
            if ("passthrough".equals(onNoMatch)) return text;
            throw new ConversionException("prefixStrip: value \"" + text + "\" does not start with any mapped prefix " + prefixMap.keySet());
        }
        List<String> prefixes = resolvePrefixes(args);
        for (String prefix : prefixes) {
            if (text.startsWith(prefix)) {
                return text.substring(prefix.length()).trim();
            }
        }
        String onNoMatch = String.valueOf(args.getOrDefault("onNoMatch", "unmappable"));
        if ("passthrough".equals(onNoMatch)) return text;
        throw new ConversionException("prefixStrip: value \"" + text + "\" does not start with any of " + prefixes);
    }

    /**
     * Read the optional {@code prefixMap} argument (prefix -> dotted target sub-path), returning its
     * entries ordered longest-prefix-first so the most specific prefix wins. Returns {@code null}
     * when no {@code prefixMap} is configured, leaving the plain strip behaviour in force.
     */
    private Map<String, String> resolvePrefixMap(Map<String, Object> args) {
        Object raw = args.get("prefixMap");
        if (!(raw instanceof Map<?, ?> map) || map.isEmpty()) {
            return null;
        }
        List<Map.Entry<?, ?>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparingInt((Map.Entry<?, ?> e) -> String.valueOf(e.getKey()).length()).reversed());
        Map<String, String> ordered = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : entries) {
            ordered.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
        }
        return ordered;
    }

    /** Wrap {@code value} in the nested object named by a dotted path (e.g. "individual.name"). */
    private Object nest(String dottedPath, String value) {
        String[] parts = dottedPath.split("\\.");
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> cursor = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Map<String, Object> next = new LinkedHashMap<>();
            cursor.put(parts[i], next);
            cursor = next;
        }
        cursor.put(parts[parts.length - 1], value);
        return root;
    }

    private List<String> resolvePrefixes(Map<String, Object> args) {
        List<String> prefixes = new ArrayList<>();
        Object multi = args.get("prefixes");
        if (multi instanceof List<?> list) {
            for (Object o : list) prefixes.add(String.valueOf(o));
        }
        Object single = args.get("prefix");
        if (single != null) prefixes.add(String.valueOf(single));
        if (prefixes.isEmpty()) {
            throw new ConversionException("prefixStrip: 'prefix' or 'prefixes' argument is required");
        }
        prefixes.sort(Comparator.comparingInt(String::length).reversed());
        return prefixes;
    }
}
