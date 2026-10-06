/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.rule;

import java.util.ArrayList;
import java.util.List;

public class Conformance {
    private List<String> failOnSeverity = new ArrayList<>();
    private List<String> warnOnSeverity = new ArrayList<>();
    private List<String> reportOnSeverity = new ArrayList<>();
    public List<String> getFailOnSeverity() { return failOnSeverity; }
    public void setFailOnSeverity(List<String> v) { this.failOnSeverity = v; }
    public List<String> getWarnOnSeverity() { return warnOnSeverity; }
    public void setWarnOnSeverity(List<String> v) { this.warnOnSeverity = v; }
    public List<String> getReportOnSeverity() { return reportOnSeverity; }
    public void setReportOnSeverity(List<String> v) { this.reportOnSeverity = v; }
}
