/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public class Rule {
    private String ruleId;
    private String description;

    @JsonProperty("sourcePath")
    @JsonDeserialize(using = StringOrListDeserializer.class)
    private List<String> sourcePath;

    @JsonProperty("targetPath")
    @JsonDeserialize(using = StringOrListDeserializer.class)
    private List<String> targetPath;

    private String cardinality;
    private TransformDefinition transform;
    private String onAbsent;
    private String onUnmappable;
    private String severityIfLost;
    private String authority;
    private String lossReason;
    private Boolean prohibitFabrication;

    private String xbucket;
    private Integer xfieldsCovered;
    private String xsplitGroup;

    public String getRuleId() { return ruleId; }
    public void setRuleId(String v) { this.ruleId = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }

    /** All configured source paths (never null; empty when unset). */
    @JsonIgnore
    public List<String> getSourcePaths() { return sourcePath == null ? List.of() : sourcePath; }
    /** First source path, or null when unset - kept for single-path callers and loss reporting. */
    @JsonIgnore
    public String getSourcePath() { return (sourcePath == null || sourcePath.isEmpty()) ? null : sourcePath.get(0); }
    public void setSourcePath(List<String> v) { this.sourcePath = v; }

    /** All configured target paths (never null; empty when unset, e.g. loss rules with targetPath: null). */
    @JsonIgnore
    public List<String> getTargetPaths() { return targetPath == null ? List.of() : targetPath; }
    /** First target path, or null when unset - kept for single-path callers and loss reporting. */
    @JsonIgnore
    public String getTargetPath() { return (targetPath == null || targetPath.isEmpty()) ? null : targetPath.get(0); }
    public void setTargetPath(List<String> v) { this.targetPath = v; }

    public String getCardinality() { return cardinality; }
    public void setCardinality(String v) { this.cardinality = v; }
    public TransformDefinition getTransform() { return transform; }
    public void setTransform(TransformDefinition v) { this.transform = v; }
    public String getOnAbsent() { return onAbsent; }
    public void setOnAbsent(String v) { this.onAbsent = v; }
    public String getOnUnmappable() { return onUnmappable; }
    public void setOnUnmappable(String v) { this.onUnmappable = v; }
    public String getSeverityIfLost() { return severityIfLost; }
    public void setSeverityIfLost(String v) { this.severityIfLost = v; }
    public String getAuthority() { return authority; }
    public void setAuthority(String v) { this.authority = v; }
    public String getLossReason() { return lossReason; }
    public void setLossReason(String v) { this.lossReason = v; }
    public Boolean getProhibitFabrication() { return prohibitFabrication; }
    public void setProhibitFabrication(Boolean v) { this.prohibitFabrication = v; }
    public String getXbucket() { return xbucket; }
    public void setXbucket(String v) { this.xbucket = v; }
    public Integer getXfieldsCovered() { return xfieldsCovered; }
    public void setXfieldsCovered(Integer v) { this.xfieldsCovered = v; }
    public String getXsplitGroup() { return xsplitGroup; }
    public void setXsplitGroup(String v) { this.xsplitGroup = v; }
}
