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

@Component
public class NormalizeToSingleLineTransform implements TransformFunction {
    public String getName() { return "normalizeToSingleLine"; }
    public Object transform(Object value, Map<String,Object> args) {
        return String.valueOf(value).replaceAll("\\s+", " ").trim();
    }
}
