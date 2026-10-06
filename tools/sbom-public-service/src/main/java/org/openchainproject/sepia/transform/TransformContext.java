/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.transform;

import org.openchainproject.sepia.loss.LossEvent;
import org.openchainproject.sepia.loss.LossReporter;
import org.openchainproject.sepia.rule.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Per-conversion (per-document) mutable state made available to transform
 * functions whose behaviour is not a pure {@code value -> value} mapping
 * (e.g. {@code constant}, which fabricates a value, and {@code idNormalize},
 * which must avoid identifier collisions across the whole document).
 * <p>
 * A new instance is created once per {@code RuleEngine.execute(...)} call, so
 * state never leaks between conversions or across concurrent requests.
 */
public class TransformContext {
    private final LossReporter lossReporter;
    private final String sourceFormat;
    private final String targetFormat;
    private final Map<String, String> idMap = new HashMap<>();
    private final Set<String> assignedIds = new HashSet<>();
    private final Map<String, Map<String, Integer>> arraySlots = new HashMap<>();
    private Rule rule;
    private String sourcePath;
    private String currentField = "";
    private String currentIndex = "";

    public TransformContext(LossReporter lossReporter, String sourceFormat, String targetFormat) {
        this.lossReporter = lossReporter;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
    }

    /** Scope this context to the rule currently being evaluated, for loss reporting. */
    public void forRule(Rule rule, String sourcePath) {
        this.rule = rule;
        this.sourcePath = sourcePath;
    }

    public String getSourceFormat() { return sourceFormat; }
    public String getTargetFormat() { return targetFormat; }

    /** Document-scoped map of original identifier -> normalized identifier (idNormalize). */
    public Map<String, String> idMap() { return idMap; }

    /** Document-scoped set of identifiers already assigned, for collision avoidance (idNormalize). */
    public Set<String> assignedIds() { return assignedIds; }

    /** The field name of the source leaf currently being written, exposed as the {@code {field}} template variable. */
    public String getCurrentField() { return currentField; }
    public void setCurrentField(String currentField) { this.currentField = currentField == null ? "" : currentField; }

    /** The array index of the source leaf currently being written, exposed as the {@code {index}} template variable. */
    public String getCurrentIndex() { return currentIndex; }
    public void setCurrentIndex(String currentIndex) { this.currentIndex = currentIndex == null ? "" : currentIndex; }

    /**
     * Allocate (or look up) a stable position within a target array for a source entity.
     * <p>
     * The first time a given {@code (targetArray, entityKey)} pair is seen it is assigned
     * the next free index; subsequent calls return the same index. This lets fields written
     * by different rules for the same source entity (e.g. the root package's name, version and
     * purl, or one component's name and checksum) land in the same array element, while keeping
     * distinct source entities (the root component vs each {@code components[]} entry) in
     * separate elements. Indices are handed out contiguously (0, 1, 2, ...) per target array.
     */
    public int allocateSlot(String targetArray, String entityKey) {
        Map<String, Integer> slots = arraySlots.computeIfAbsent(targetArray, k -> new LinkedHashMap<>());
        return slots.computeIfAbsent(entityKey, k -> slots.size());
    }

    public void emitLoss(String kind, Object sourceValue, Object targetValue, String reason) {
        LossEvent e = new LossEvent();
        e.setKind(kind);
        e.setSeverity(rule == null ? null : rule.getSeverityIfLost());
        e.setSourceFormat(sourceFormat);
        e.setTargetFormat(targetFormat);
        e.setSourcePath(sourcePath);
        e.setTargetPath(rule == null ? null : rule.getTargetPath());
        e.setRuleId(rule == null ? null : rule.getRuleId());
        e.setSourceValue(sourceValue);
        e.setTargetValue(targetValue);
        e.setReason(reason);
        e.setToolName("sbom-xform");
        e.setToolVersion("1.0.0");
        lossReporter.report(e);
    }
}
