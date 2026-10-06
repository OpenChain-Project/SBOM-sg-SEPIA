/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.transform;

import java.util.Map;

public interface TransformFunction {
    String getName();
    Object transform(Object sourceValue, Map<String, Object> args);

    /**
     * Context-aware overload for transforms that need document-scoped state
     * or loss reporting (e.g. {@code constant}, {@code idNormalize}).
     * Defaults to the pure overload so existing transforms are unaffected.
     */
    default Object transform(Object sourceValue, Map<String, Object> args, TransformContext context) {
        return transform(sourceValue, args);
    }
}
