/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.rule;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes a YAML value that may be either a single scalar path or a
 * sequence of paths into a {@code List<String>}. An explicit {@code null}
 * scalar (used by fabrication rules with {@code sourcePath: null}) becomes
 * an empty list, which downstream blank-checks treat as "no path set".
 */
public class StringOrListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        List<String> values = new ArrayList<>();
        if (node == null || node.isNull() || node.isMissingNode()) {
            return values;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (!item.isNull()) {
                    values.add(item.asText());
                }
            }
        } else {
            values.add(node.asText());
        }
        return values;
    }

    @Override
    public List<String> getNullValue(DeserializationContext ctxt) {
        return new ArrayList<>();
    }
}
