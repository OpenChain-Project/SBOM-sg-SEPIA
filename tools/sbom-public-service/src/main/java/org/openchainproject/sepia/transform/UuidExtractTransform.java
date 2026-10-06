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

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recovers an RFC 4122 UUID embedded anywhere in a source string (typically
 * an SPDX {@code documentNamespace} such as
 * {@code http://spdx.org/spdxdocs/example-444504E0-4F89-41D3-9A0C-0305E82C3301})
 * and re-emits it through {@code args.format} (default {@code "urn:uuid:{value}"}),
 * producing a CycloneDX {@code serialNumber} that satisfies the
 * {@code ^urn:uuid:[0-9a-fA-F-]{36}$} constraint.
 * <p>
 * When the source carries no UUID the behaviour is governed by
 * {@code args.onNoMatch}: {@code fabricate} (default) mints a fresh
 * {@link UUID} and records a {@code FABRICATED} loss; {@code unmappable}
 * throws so the caller's {@code onUnmappable} policy applies. When a UUID is
 * extracted but the source was not already in {@code urn:uuid:} form the
 * reshaping is recorded as a {@code MODIFIED} loss.
 */
@Component
public class UuidExtractTransform implements TransformFunction {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    public String getName() { return "uuidExtract"; }

    @Override
    public Object transform(Object value, Map<String, Object> args) {
        String uuid = findUuid(value);
        if (uuid != null) return applyFormat(args, uuid);
        if (fabricate(args)) return applyFormat(args, UUID.randomUUID().toString());
        throw new ConversionException("uuidExtract: no UUID found in \"" + value + "\"");
    }

    @Override
    public Object transform(Object value, Map<String, Object> args, TransformContext context) {
        String source = value == null ? null : String.valueOf(value);
        String uuid = findUuid(value);
        if (uuid != null) {
            String result = applyFormat(args, uuid);
            if (!result.equals(source)) {
                context.emitLoss("MODIFIED", source, result,
                        "UUID extracted from source and reshaped into a urn:uuid serial number");
            }
            return result;
        }
        if (fabricate(args)) {
            String result = applyFormat(args, UUID.randomUUID().toString());
            context.emitLoss("FABRICATED", source, result,
                    "source carries no UUID; a fresh urn:uuid serial number was fabricated");
            return result;
        }
        throw new ConversionException("uuidExtract: no UUID found in \"" + source + "\"");
    }

    private String findUuid(Object value) {
        if (value == null) return null;
        Matcher m = UUID_PATTERN.matcher(String.valueOf(value));
        return m.find() ? m.group().toLowerCase() : null;
    }

    private String applyFormat(Map<String, Object> args, String uuid) {
        return String.valueOf(args.getOrDefault("format", "urn:uuid:{value}")).replace("{value}", uuid);
    }

    private boolean fabricate(Map<String, Object> args) {
        return !"unmappable".equalsIgnoreCase(String.valueOf(args.getOrDefault("onNoMatch", "fabricate")));
    }
}
