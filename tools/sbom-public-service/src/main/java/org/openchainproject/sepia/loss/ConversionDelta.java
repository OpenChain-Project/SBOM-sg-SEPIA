/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.loss;

/**
 * A single value that differs between the source and the converted target SBOM. Reported separately
 * from {@link LossEvent}: a loss event describes a rule-driven drop, whereas a conversion delta is
 * the outcome of comparing every scalar leaf of the source document against the produced target and
 * classifying the difference as {@link ChangeCategory#LOST}, {@link ChangeCategory#ADDED} or
 * {@link ChangeCategory#MODIFIED}.
 */
public class ConversionDelta {
    private String deltaId;
    private ChangeCategory category;
    private String fieldName;
    private Object value;
    private String sourcePath;
    private String targetPath;
    private String reason;
    private String ruleId;
    private String sourceFormat;
    private String targetFormat;

    public String getDeltaId() { return deltaId; }
    public void setDeltaId(String v) { this.deltaId = v; }
    public ChangeCategory getCategory() { return category; }
    public void setCategory(ChangeCategory v) { this.category = v; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String v) { this.fieldName = v; }
    public Object getValue() { return value; }
    public void setValue(Object v) { this.value = v; }
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String v) { this.sourcePath = v; }
    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String v) { this.targetPath = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { this.reason = v; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String v) { this.ruleId = v; }
    public String getSourceFormat() { return sourceFormat; }
    public void setSourceFormat(String v) { this.sourceFormat = v; }
    public String getTargetFormat() { return targetFormat; }
    public void setTargetFormat(String v) { this.targetFormat = v; }
}
