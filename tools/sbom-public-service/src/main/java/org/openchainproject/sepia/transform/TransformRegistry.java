/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.transform;

import org.openchainproject.sepia.exception.ConversionException;
import org.openchainproject.sepia.rule.TransformDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TransformRegistry {
    private final Map<String, TransformFunction> functions;
    public TransformRegistry(List<TransformFunction> list) {
        this.functions = list.stream().collect(Collectors.toMap(TransformFunction::getName, Function.identity()));
    }
    public Object transform(TransformDefinition definition, Object value) {
        String name = definition == null || definition.getFunction() == null ? "identity" : definition.getFunction();
        TransformFunction f = functions.get(name);
        if (f == null) throw new ConversionException("Unknown transform: " + name);
        return f.transform(value, definition == null ? Map.of() : definition.getArgs());
    }

    public Object transform(TransformDefinition definition, Object value, TransformContext context) {
        String name = definition == null || definition.getFunction() == null ? "identity" : definition.getFunction();
        TransformFunction f = functions.get(name);
        if (f == null) throw new ConversionException("Unknown transform: " + name);
        return f.transform(value, definition == null ? Map.of() : definition.getArgs(), context);
    }
}
