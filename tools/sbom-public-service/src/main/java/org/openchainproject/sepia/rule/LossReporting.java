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

public class LossReporting {
    private boolean enabled = true;
    private boolean recordSourceValue = true;
    private boolean recordTargetValue = true;
    private boolean includeRuleId = true;
    private boolean includeReason = true;
    private boolean includeToolName = true;
    private boolean includeToolVersion = true;
    private List<String> eventKinds = new ArrayList<>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public boolean isRecordSourceValue() { return recordSourceValue; }
    public void setRecordSourceValue(boolean v) { this.recordSourceValue = v; }
    public boolean isRecordTargetValue() { return recordTargetValue; }
    public void setRecordTargetValue(boolean v) { this.recordTargetValue = v; }
    public boolean isIncludeRuleId() { return includeRuleId; }
    public void setIncludeRuleId(boolean v) { this.includeRuleId = v; }
    public boolean isIncludeReason() { return includeReason; }
    public void setIncludeReason(boolean v) { this.includeReason = v; }
    public boolean isIncludeToolName() { return includeToolName; }
    public void setIncludeToolName(boolean v) { this.includeToolName = v; }
    public boolean isIncludeToolVersion() { return includeToolVersion; }
    public void setIncludeToolVersion(boolean v) { this.includeToolVersion = v; }
    public List<String> getEventKinds() { return eventKinds; }
    public void setEventKinds(List<String> v) { this.eventKinds = v; }
}
