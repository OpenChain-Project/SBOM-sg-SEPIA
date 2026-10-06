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

/**
 * Fabrication transform: emits a fixed value at the target path where the
 * target specification mandates a field that has no source counterpart
 * (e.g. SPDX {@code spdxVersion}, {@code dataLicense}).
 * <p>
 * {@code sourcePath} must be null for rules using this transform - the
 * value returned is always {@code args.value}, taken verbatim (type
 * preserved, never stringified). The engine (see {@code RuleEngine}) is
 * responsible for the {@code onlyWhenAbsent} write-condition and for
 * broadcasting the value across array elements when the target path
 * contains {@code []}; those concerns require visibility into the target
 * document that a pure transform does not have.
 * <p>
 * Every invocation records a {@code FABRICATED} loss event, since a
 * constant is by definition a value with no source.
 */
@Component
public class ConstantTransform implements TransformFunction {

    public String getName() { return "constant"; }

    @Override
    public Object transform(Object value, Map<String, Object> args) {
        return args.get("value");
    }

    @Override
    public Object transform(Object value, Map<String, Object> args, TransformContext context) {
        Object constantValue = args.get("value");
        context.emitLoss("FABRICATED", null, constantValue,
                "target field is required and has no source counterpart; value was fabricated");
        return constantValue;
    }
}
