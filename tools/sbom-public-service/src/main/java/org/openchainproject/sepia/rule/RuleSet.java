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

public class RuleSet {
    private String ruleSetId;
    private String version;
    private String description;
    private AppliesTo appliesTo;
    private Defaults defaults = new Defaults();
    private List<String> allowedTransforms = new ArrayList<>();
    private List<Rule> rules = new ArrayList<>();
    private Conformance conformance = new Conformance();
    private LossReporting lossReporting = new LossReporting();

    public String getRuleSetId() { return ruleSetId; }
    public void setRuleSetId(String v) { this.ruleSetId = v; }
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public AppliesTo getAppliesTo() { return appliesTo; }
    public void setAppliesTo(AppliesTo v) { this.appliesTo = v; }
    public Defaults getDefaults() { return defaults; }
    public void setDefaults(Defaults v) { this.defaults = v; }
    public List<String> getAllowedTransforms() { return allowedTransforms; }
    public void setAllowedTransforms(List<String> v) { this.allowedTransforms = v; }
    public List<Rule> getRules() { return rules; }
    public void setRules(List<Rule> v) { this.rules = v; }
    public Conformance getConformance() { return conformance; }
    public void setConformance(Conformance v) { this.conformance = v; }
    public LossReporting getLossReporting() { return lossReporting; }
    public void setLossReporting(LossReporting v) { this.lossReporting = v; }
}
