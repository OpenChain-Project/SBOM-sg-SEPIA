/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.transform;

import org.springframework.stereotype.Component;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

@Component
public class FirstOnlyTransform implements TransformFunction {
    public String getName() { return "firstOnly"; }
    public Object transform(Object value, Map<String,Object> args) {
        if (value instanceof Collection<?> c) return c.stream().findFirst().orElse(null);
        if (value != null && value.getClass().isArray() && Array.getLength(value) > 0) return Array.get(value, 0);
        return value;
    }
}
