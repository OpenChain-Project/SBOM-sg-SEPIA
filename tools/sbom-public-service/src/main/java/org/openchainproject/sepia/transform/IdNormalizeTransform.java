/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.transform;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Sanitizes a free-form source identifier (e.g. CycloneDX {@code bom-ref})
 * into a value that matches the stricter SPDX idstring grammar
 * ({@code SPDXRef-[A-Za-z0-9.-]+}), then applies {@code args.format}
 * (default {@code "{value}"}) to the sanitized core.
 * <p>
 * Collision avoidance and consistent re-use of the same normalized id for a
 * repeated source value require document-scoped state, so this transform is
 * not pure: the 3-argument overload uses the {@link TransformContext}'s
 * id map / assigned-id set, which the engine creates fresh per conversion.
 * The 2-argument overload (used when no context is available) performs
 * sanitisation only, without collision tracking.
 */
@Component
public class IdNormalizeTransform implements TransformFunction {

    private static final Pattern ILLEGAL = Pattern.compile("[^A-Za-z0-9.\\-]+");
    private static final Pattern REPEATED_HYPHEN = Pattern.compile("-{2,}");

    public String getName() { return "idNormalize"; }

    @Override
    public Object transform(Object value, Map<String, Object> args) {
        return sanitize(value, args);
    }

    @Override
    public Object transform(Object value, Map<String, Object> args, TransformContext context) {
        if (value == null) return null;
        String source = String.valueOf(value);

        String existing = context.idMap().get(source);
        if (existing != null) return existing; // already assigned in this document; no second loss event

        String base = sanitize(value, args);
        String candidate = base;
        int suffix = 1;
        while (context.assignedIds().contains(candidate)) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        context.assignedIds().add(candidate);
        context.idMap().put(source, candidate);

        if (!candidate.equals(source)) {
            context.emitLoss("RENAMED", source, candidate, "identifier sanitized to the SPDX idstring grammar");
        }
        return candidate;
    }

    private String sanitize(Object value, Map<String, Object> args) {
        if (value == null) return null;
        String text = String.valueOf(value);
        String core = ILLEGAL.matcher(text).replaceAll("-");
        core = REPEATED_HYPHEN.matcher(core).replaceAll("-");
        core = stripEdges(core);
        if (core.isEmpty()) core = "unnamed";
        String format = String.valueOf(args.getOrDefault("format", "{value}"));
        return format.replace("{value}", core);
    }

    private String stripEdges(String s) {
        int start = 0;
        int end = s.length();
        while (start < end && isEdgeChar(s.charAt(start))) start++;
        while (end > start && isEdgeChar(s.charAt(end - 1))) end--;
        return s.substring(start, end);
    }

    private boolean isEdgeChar(char c) { return c == '-' || c == '.'; }
}
