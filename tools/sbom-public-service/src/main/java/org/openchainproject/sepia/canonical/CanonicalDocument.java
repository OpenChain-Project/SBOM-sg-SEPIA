/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.canonical;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

public class CanonicalDocument {
    private final Map<String, JsonNode> values = new HashMap<>();
    public JsonNode get(String path) { return values.get(path); }
    public void put(String path, JsonNode value) { values.put(path, value); }
    public Map<String, JsonNode> values() { return values; }

    public ObjectNode toObjectNode(com.fasterxml.jackson.databind.ObjectMapper mapper) {
        ObjectNode root = mapper.createObjectNode();
        for (Map.Entry<String, JsonNode> e : values.entrySet()) {
            String[] parts = e.getKey().split("\\.");
            ObjectNode current = root;
            for (int i = 0; i < parts.length - 1; i++) {
                current = current.putObject(parts[i]);
            }
            current.set(parts[parts.length - 1], e.getValue());
        }
        return root;
    }
}
