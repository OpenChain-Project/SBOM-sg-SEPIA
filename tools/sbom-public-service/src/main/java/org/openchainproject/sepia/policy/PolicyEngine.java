/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.policy;

import org.openchainproject.sepia.exception.ConversionException;
import org.openchainproject.sepia.loss.LossEvent;
import org.openchainproject.sepia.loss.LossReporter;
import org.openchainproject.sepia.rule.Rule;
import org.springframework.stereotype.Component;

@Component
public class PolicyEngine {
    public void handleAbsent(Rule rule, String sourceFormat, String targetFormat, String sourcePath, LossReporter reporter) {
        switch (rule.getOnAbsent()) {
            case "drop" -> { }
            case "annotate", "emitNoAssertion" -> reporter.report(event(rule, sourceFormat, targetFormat, sourcePath, "DROPPED", "Source value is absent"));
            case "fail" -> throw new ConversionException("Required source field absent: " + rule.getRuleId());
            default -> throw new ConversionException("Unsupported onAbsent policy: " + rule.getOnAbsent());
        }
    }
    public void handleUnmappable(Rule rule, String sourceFormat, String targetFormat, String sourcePath, LossReporter reporter, String reason) {
        switch (rule.getOnUnmappable()) {
            case "drop", "annotate", "property" -> reporter.report(event(rule, sourceFormat, targetFormat, sourcePath, "DROPPED", reason));
            case "fail" -> throw new ConversionException(rule.getRuleId() + ": " + reason);
            default -> throw new ConversionException("Unsupported onUnmappable policy: " + rule.getOnUnmappable());
        }
    }
    private LossEvent event(Rule r,String sf,String tf,String sp,String kind,String reason){
        LossEvent e=new LossEvent(); e.setSeverity(r.getSeverityIfLost()); e.setKind(kind); e.setSourceFormat(sf); e.setTargetFormat(tf); e.setSourcePath(sp); e.setTargetPath(r.getTargetPath()); e.setRuleId(r.getRuleId()); e.setReason(r.getLossReason()!=null?r.getLossReason():reason); e.setToolName("sbom-xform"); e.setToolVersion("1.0.0"); return e;
    }
}
