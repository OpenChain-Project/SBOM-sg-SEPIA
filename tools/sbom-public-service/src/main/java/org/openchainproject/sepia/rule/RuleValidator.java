/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.rule;

import org.openchainproject.sepia.exception.InvalidRuleException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class RuleValidator {
    public void validate(RuleSet ruleSet) {
        if (ruleSet == null || isBlank(ruleSet.getRuleSetId())) throw new InvalidRuleException("ruleSetId is mandatory");
        if (ruleSet.getAppliesTo() == null || ruleSet.getAppliesTo().getSource() == null || ruleSet.getAppliesTo().getTarget() == null) {
            throw new InvalidRuleException("appliesTo.source and appliesTo.target are mandatory");
        }
        if (ruleSet.getRules() == null || ruleSet.getRules().isEmpty()) throw new InvalidRuleException("At least one rule is required");
        Set<String> ids = new HashSet<>();
        for (Rule rule : ruleSet.getRules()) {
            if (isBlank(rule.getRuleId())) throw new InvalidRuleException("ruleId is mandatory");
            if (!ids.add(rule.getRuleId())) throw new InvalidRuleException("Duplicate ruleId: " + rule.getRuleId());
            String transform = rule.getTransform() == null ? "identity" : rule.getTransform().getFunction();
            boolean isConstant = "constant".equalsIgnoreCase(transform);
            if (isConstant) {
                // constant is a fabrication directive: it has no source value, so sourcePath
                // must be null, and it can never honour prohibitFabrication: true.
                if (!isBlank(rule.getSourcePath())) {
                    throw new InvalidRuleException(rule.getRuleId() + ": sourcePath must be null when transform is constant");
                }
                if (Boolean.TRUE.equals(rule.getProhibitFabrication())) {
                    throw new InvalidRuleException(rule.getRuleId() + ": constant transform cannot be combined with prohibitFabrication: true");
                }
            } else if (isBlank(rule.getSourcePath())) {
                throw new InvalidRuleException(rule.getRuleId() + ": sourcePath is mandatory");
            }
            if (!"none".equalsIgnoreCase(rule.getCardinality()) && isBlank(rule.getTargetPath())) {
                throw new InvalidRuleException(rule.getRuleId() + ": targetPath is mandatory unless cardinality=none");
            }
            if (ruleSet.getAllowedTransforms() != null && !ruleSet.getAllowedTransforms().isEmpty() && !ruleSet.getAllowedTransforms().contains(transform)) {
                throw new InvalidRuleException(rule.getRuleId() + ": unsupported transform " + transform);
            }
            if (isBlank(resolve(rule.getOnAbsent(), ruleSet.getDefaults().getOnAbsent()))) throw new InvalidRuleException(rule.getRuleId() + ": onAbsent unresolved");
            if (isBlank(resolve(rule.getOnUnmappable(), ruleSet.getDefaults().getOnUnmappable()))) throw new InvalidRuleException(rule.getRuleId() + ": onUnmappable unresolved");
        }
    }
    public String resolve(String value, String defaultValue) { return isBlank(value) ? defaultValue : value; }
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
