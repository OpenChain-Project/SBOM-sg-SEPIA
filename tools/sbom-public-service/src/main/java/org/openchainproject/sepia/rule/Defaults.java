/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.rule;

public class Defaults {
    private String onAbsent = "drop";
    private String onUnmappable = "annotate";
    private String defaultSeverityIfLost = "MINOR";
    private String authority = "spec";
    public String getOnAbsent() { return onAbsent; }
    public void setOnAbsent(String v) { this.onAbsent = v; }
    public String getOnUnmappable() { return onUnmappable; }
    public void setOnUnmappable(String v) { this.onUnmappable = v; }
    public String getDefaultSeverityIfLost() { return defaultSeverityIfLost; }
    public void setDefaultSeverityIfLost(String v) { this.defaultSeverityIfLost = v; }
    public String getAuthority() { return authority; }
    public void setAuthority(String v) { this.authority = v; }
}
