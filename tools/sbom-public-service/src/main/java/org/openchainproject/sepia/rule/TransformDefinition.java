/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.rule;

import java.util.LinkedHashMap;
import java.util.Map;

public class TransformDefinition {
    private String function = "identity";
    private Map<String, Object> args = new LinkedHashMap<>();
    public String getFunction() { return function; }
    public void setFunction(String v) { this.function = v; }
    public Map<String, Object> getArgs() { return args; }
    public void setArgs(Map<String, Object> v) { this.args = v; }
}
