/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.transform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class EnumMapTransform implements TransformFunction {
    public String getName() { return "enumMap"; }

    public Object transform(Object value, Map<String,Object> args) {
        Object tableObj = args.get("table");
        Map<?,?> table = tableObj instanceof Map<?,?> t ? t : null;

        // Object-valued mapping (e.g. a CycloneDX hash {alg, content} -> an SPDX checksum
        // {algorithm, checksumValue}): when a keyMap is supplied and the value is a whole
        // object (delivered as a Jackson node), rename its keys and map the named enum field
        // in place. Gated on keyMap so rules that only declare `field` keep the scalar
        // behaviour below unchanged.
        if (value instanceof ObjectNode obj && args.get("keyMap") instanceof Map<?,?> keyMap) {
            return reshape(obj, keyMap, table, String.valueOf(args.get("field")));
        }

        if (table == null) return value;
        Object mapped = table.get(scalarKey(value));
        return mapped == null ? value : mapped;
    }

    private Object reshape(ObjectNode obj, Map<?,?> keyMap, Map<?,?> table, String field) {
        ObjectNode out = obj.objectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = obj.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode v = entry.getValue();
            if (table != null && key.equals(field) && v.isValueNode()) {
                Object mappedField = table.get(v.asText());
                if (mappedField != null) v = out.textNode(String.valueOf(mappedField));
            }
            Object renamedKey = keyMap.get(key);
            out.set(renamedKey != null ? String.valueOf(renamedKey) : key, v);
        }
        return out;
    }

    private String scalarKey(Object value) {
        if (value instanceof JsonNode node) {
            return node.isValueNode() ? node.asText() : node.toString();
        }
        return String.valueOf(value);
    }
}
