/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.canonical;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class Canonicalizer {
    public CanonicalDocument canonicalize(JsonNode root) {
        CanonicalDocument out = new CanonicalDocument();
        flatten(root, "", out);
        return out;
    }

    private void flatten(JsonNode node, String prefix, CanonicalDocument out) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String path = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
                if (e.getValue().isValueNode() || e.getValue().isNull()) {
                    out.put(path, e.getValue());
                } else {
                    flatten(e.getValue(), path, out);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            out.put(prefix, arr);
        } else if (node.isValueNode()) {
            out.put(prefix, node);
        }
    }
}
