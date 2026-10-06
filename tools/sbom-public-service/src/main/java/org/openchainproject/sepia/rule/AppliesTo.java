/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.rule;

public class AppliesTo {
    private SpecVersion source;
    private SpecVersion target;
    public SpecVersion getSource() { return source; }
    public void setSource(SpecVersion v) { this.source = v; }
    public SpecVersion getTarget() { return target; }
    public void setTarget(SpecVersion v) { this.target = v; }
}
