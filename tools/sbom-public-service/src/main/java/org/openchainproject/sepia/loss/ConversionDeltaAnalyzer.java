/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.loss;

import com.fasterxml.jackson.databind.JsonNode;
import org.openchainproject.sepia.rule.Rule;
import org.openchainproject.sepia.rule.RuleSet;
import org.openchainproject.sepia.rule.TransformDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Computes the set of values that differ between the source and the converted target SBOM.
 * <p>
 * This complements {@link LossReporter}: loss events describe rule-driven drops, while this analyzer
 * diffs the two documents scalar-by-scalar and classifies each difference:
 * <ul>
 *   <li>{@link ChangeCategory#LOST} - a source value with no representation in the target;</li>
 *   <li>{@link ChangeCategory#MODIFIED} - a source value that a mapping rule transformed, so the
 *       original value does not appear verbatim in the target;</li>
 *   <li>{@link ChangeCategory#ADDED} - a target value with no corresponding source value that was
 *       not produced by a mapping rule from a source field.</li>
 * </ul>
 * The rule set is used to attribute a reason and category to each delta; it does not affect which
 * values are detected.
 */
@Component
public class ConversionDeltaAnalyzer {

    // Below this length a scalar is only matched exactly against the other document, to avoid a short
    // token (e.g. "1", "id") spuriously matching an unrelated substring and hiding a genuine delta.
    private static final int MIN_SUBSTRING_LENGTH = 3;

    private static final String BY_RULE = " by rule '";

    /**
     * Diff {@code source} against {@code target} and return every value that differs between the two,
     * classified as LOST, MODIFIED or ADDED. {@code rules} is used only to attribute a reason and
     * category to each delta; it does not affect which values are detected.
     */
    public List<ConversionDelta> analyze(RuleSet rules, JsonNode source, JsonNode target,
                                         String sourceFormat, String targetFormat) {
        AnalysisContext ctx = new AnalysisContext(sourceFormat, targetFormat);
        collectValues(target, ctx.targetExact, ctx.targetSubstrings);
        collectValues(source, ctx.sourceExact, ctx.sourceSubstrings);
        indexRules(rules, ctx);
        if (source != null && source.isObject()) {
            collectSourceDeltas(source, "", "", ctx);
        }
        if (target != null && target.isObject()) {
            collectAddedDeltas(target, "", "", "", ctx);
        }
        return ctx.deltas;
    }

    /** Recursively gather every scalar value of {@code node} into the exact and substring lookups. */
    private void collectValues(JsonNode node, Set<String> exact, Set<String> substrings) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(e -> collectValues(e.getValue(), exact, substrings));
        } else if (node.isArray()) {
            node.forEach(child -> collectValues(child, exact, substrings));
        } else if (node.isValueNode() && !node.isNull()) {
            String text = node.asText();
            if (text != null && !text.isEmpty()) {
                String lower = text.toLowerCase();
                exact.add(lower);
                if (text.length() >= MIN_SUBSTRING_LENGTH) {
                    substrings.add(lower);
                }
            }
        }
    }

    /**
     * Split each rule's source paths into mapped (has target) and loss (no target) lookups by field
     * path, and record the normalized target paths that a mapping rule produces.
     */
    private void indexRules(RuleSet rules, AnalysisContext ctx) {
        if (rules == null || rules.getRules() == null) {
            return;
        }
        for (Rule rule : rules.getRules()) {
            boolean mapped = !rule.getTargetPaths().isEmpty();
            indexSourcePaths(rule, mapped ? ctx.mappedRuleByPath : ctx.lossRuleByPath);
            if (mapped) {
                indexTargetPaths(rule, ctx);
                indexPropertyRules(rule, ctx);
            }
        }
    }

    private void indexSourcePaths(Rule rule, Map<String, Rule> lookup) {
        for (String sourcePath : rule.getSourcePaths()) {
            String fieldPath = normalizeRulePath(sourcePath);
            if (!fieldPath.isEmpty()) {
                lookup.putIfAbsent(fieldPath, rule);
            }
        }
    }

    private void indexTargetPaths(Rule rule, AnalysisContext ctx) {
        for (String targetPath : rule.getTargetPaths()) {
            String fieldPath = normalizeRulePath(targetPath);
            if (!fieldPath.isEmpty()) {
                ctx.targetRulesByPath.computeIfAbsent(fieldPath, k -> new ArrayList<>()).add(rule);
            }
        }
    }

    /**
     * Index the rules that emit CycloneDX properties so an ADDED property leaf can be attributed by
     * the namespace of its {@code name} (e.g. {@code spdx:file:referenceLocator} or
     * {@code spdx:relationship:VARIANT_OF}) rather than by the shared {@code properties[]} container.
     */
    private void indexPropertyRules(Rule rule, AnalysisContext ctx) {
        if (!writesToProperties(rule)) {
            return;
        }
        TransformDefinition transform = rule.getTransform();
        String function = transform != null ? transform.getFunction() : null;
        if ("template".equals(function)) {
            Object nameArg = transform.getArgs().get("name");
            if (nameArg instanceof String name && !name.isEmpty()) {
                indexTemplatePropertyName(rule, name, ctx);
            }
        } else if ("enumMap".equals(function)) {
            indexEnumMapPropertyRule(rule, ctx);
        }
    }

    // Relationship post-processors emit into properties[] via enumMap with no name template; the engine
    // names them by the spdx-relationship namespace, so they are keyed under that namespace here.
    private void indexEnumMapPropertyRule(Rule rule, AnalysisContext ctx) {
        for (String sourcePath : rule.getSourcePaths()) {
            if ("relationships".equals(rootSegment(normalizeRulePath(sourcePath)))) {
                ctx.rulesByPropertyNamespace.computeIfAbsent("spdx:relationship", k -> new ArrayList<>()).add(rule);
                return;
            }
        }
    }

    private void indexTemplatePropertyName(Rule rule, String nameTemplate, AnalysisContext ctx) {
        String namespace = propertyNamespace(nameTemplate);
        if (namespace != null) {
            ctx.rulesByPropertyNamespace.computeIfAbsent(namespace, k -> new ArrayList<>()).add(rule);
        }
        if (nameTemplate.contains("{field}")) {
            for (String sourcePath : rule.getSourcePaths()) {
                String field = lastSegment(normalizeRulePath(sourcePath));
                if (field != null && !field.isEmpty()) {
                    ctx.ruleByPropertyName.putIfAbsent(nameTemplate.replace("{field}", field), rule);
                }
            }
        } else if (!nameTemplate.contains("{")) {
            ctx.ruleByPropertyName.putIfAbsent(nameTemplate, rule);
        }
    }

    private boolean writesToProperties(Rule rule) {
        for (String targetPath : rule.getTargetPaths()) {
            String path = normalizeRulePath(targetPath);
            if (path.equals("properties") || path.endsWith(".properties")) {
                return true;
            }
        }
        return false;
    }

    /** The property namespace of a name, e.g. {@code spdx:file:{field}} -> {@code spdx:file}. */
    private String propertyNamespace(String name) {
        if (name == null) {
            return null;
        }
        String[] parts = name.split(":");
        return parts.length >= 2 && "spdx".equals(parts[0]) ? "spdx:" + parts[1] : null;
    }

    private String rootSegment(String fieldPath) {
        if (fieldPath == null || fieldPath.isEmpty()) {
            return fieldPath;
        }
        int dot = fieldPath.indexOf('.');
        return dot < 0 ? fieldPath : fieldPath.substring(0, dot);
    }

    /** Strip array/predicate markers from a rule path, leaving a dotted field path. */
    private String normalizeRulePath(String rulePath) {
        if (rulePath == null || rulePath.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (String token : rulePath.split("\\.")) {
            int bracket = token.indexOf('[');
            String name = bracket < 0 ? token : token.substring(0, bracket);
            if (name.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append('.');
            }
            out.append(name);
        }
        return out.toString();
    }

    /**
     * Walk the source document, recording LOST and MODIFIED deltas. {@code displayPath} keeps array
     * indices for human-readable output (e.g. {@code components[0].name}); {@code fieldPath} drops
     * indices for rule correlation (e.g. {@code components.name}).
     */
    private void collectSourceDeltas(JsonNode node, String displayPath, String fieldPath, AnalysisContext ctx) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(e -> {
                String name = e.getKey();
                collectSourceDeltas(e.getValue(), appendField(displayPath, name), appendField(fieldPath, name), ctx);
            });
        } else if (node.isArray()) {
            int i = 0;
            for (JsonNode child : node) {
                collectSourceDeltas(child, displayPath + "[" + i + "]", fieldPath, ctx);
                i++;
            }
        } else {
            recordSourceDelta(node, displayPath, fieldPath, ctx);
        }
    }

    /**
     * Walk the target document, recording ADDED deltas for values that are absent from the source and
     * are not produced by a mapping rule (those are already accounted for on the source side).
     */
    private void collectAddedDeltas(JsonNode node, String displayPath, String fieldPath, String nameHint,
                                    AnalysisContext ctx) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            String childHint = objectNameHint(node, nameHint);
            node.fields().forEachRemaining(e -> {
                String name = e.getKey();
                collectAddedDeltas(e.getValue(), appendField(displayPath, name), appendField(fieldPath, name),
                        childHint, ctx);
            });
        } else if (node.isArray()) {
            int i = 0;
            for (JsonNode child : node) {
                collectAddedDeltas(child, displayPath + "[" + i + "]", fieldPath, nameHint, ctx);
                i++;
            }
        } else {
            recordAddedDelta(node, displayPath, fieldPath, nameHint, ctx);
        }
    }

    /** The enclosing object's textual {@code name} field, used to attribute a property leaf. */
    private String objectNameHint(JsonNode node, String inherited) {
        JsonNode name = node.get("name");
        return name != null && name.isTextual() ? name.asText() : inherited;
    }

    private String appendField(String path, String name) {
        return path.isEmpty() ? name : path + "." + name;
    }

    /** Record a source leaf as LOST or MODIFIED when its value has no representation in the target. */
    private void recordSourceDelta(JsonNode node, String displayPath, String fieldPath, AnalysisContext ctx) {
        if (!node.isValueNode() || node.isNull()) {
            return;
        }
        String text = node.asText();
        if (text == null || text.isEmpty() || isPresent(node, text, ctx.targetExact, ctx.targetSubstrings)) {
            return;
        }
        ConversionDelta delta = newDelta(node, text, ctx);
        delta.setFieldName(lastSegment(fieldPath));
        delta.setSourcePath(displayPath);
        classifySourceDelta(delta, fieldPath, ctx);
        ctx.deltas.add(delta);
    }

    /** Record a target leaf as ADDED when its value has no representation in the source. */
    private void recordAddedDelta(JsonNode node, String displayPath, String fieldPath, String propertyName,
                                  AnalysisContext ctx) {
        if (!node.isValueNode() || node.isNull()) {
            return;
        }
        String text = node.asText();
        if (text == null || text.isEmpty() || isPresent(node, text, ctx.sourceExact, ctx.sourceSubstrings)) {
            return;
        }
        // A value whose exact target path is a mapping rule's target is a transformation of a source
        // value, already reported as MODIFIED on the source side; do not double-count it as an addition.
        if (ctx.targetRulesByPath.containsKey(fieldPath)) {
            return;
        }
        ConversionDelta delta = newDelta(node, text, ctx);
        delta.setCategory(ChangeCategory.ADDED);
        delta.setFieldName(lastSegment(fieldPath));
        delta.setTargetPath(displayPath);
        attributeAddedRule(delta, fieldPath, propertyName, ctx);
        ctx.deltas.add(delta);
    }

    /**
     * Attribute the emitting rule to an ADDED value. A property leaf is attributed by the namespace of
     * its {@code name} ({@link #attributeByPropertyName}); otherwise the value's leaf sits inside a
     * container a rule writes into (e.g. a name/value pair inside {@code components[].properties[]}),
     * so its rule is found by the longest target-path prefix. When several rules share that target
     * path the exact producer is ambiguous; the first-declared rule is used and the alternatives noted.
     */
    private void attributeAddedRule(ConversionDelta delta, String fieldPath, String propertyName, AnalysisContext ctx) {
        if (attributeByPropertyName(delta, propertyName, ctx)) {
            return;
        }
        String base = addedBaseReason(ctx);
        List<Rule> producers = findTargetRules(fieldPath, ctx);
        if (producers.isEmpty()) {
            delta.setReason(base + ".");
            return;
        }
        delta.setRuleId(producers.get(0).getRuleId());
        if (producers.size() == 1) {
            delta.setReason(base + BY_RULE + producers.get(0).getRuleId() + "'.");
            return;
        }
        delta.setReason(base + BY_RULE + producers.get(0).getRuleId()
                + "' (this target path is also written by: " + joinRuleIdsFrom(producers, 1) + ").");
    }

    /**
     * Attribute an ADDED property leaf by the namespace of its {@code name}. An exact full name (e.g.
     * {@code spdx:file:referenceLocator}) resolves to a single rule; otherwise the namespace narrows
     * the candidates, and for enumMap rules the specific relationship type disambiguates further.
     * Returns {@code true} when the delta was attributed.
     */
    private boolean attributeByPropertyName(ConversionDelta delta, String propertyName, AnalysisContext ctx) {
        if (propertyName == null || !propertyName.contains(":")) {
            return false;
        }
        String base = addedBaseReason(ctx);
        Rule exact = ctx.ruleByPropertyName.get(propertyName);
        if (exact != null) {
            delta.setRuleId(exact.getRuleId());
            delta.setReason(base + BY_RULE + exact.getRuleId() + "' (property '" + propertyName + "').");
            return true;
        }
        String namespace = propertyNamespace(propertyName);
        if (namespace == null) {
            return false;
        }
        List<Rule> candidates = ctx.rulesByPropertyNamespace.getOrDefault(namespace, List.of());
        if (candidates.isEmpty()) {
            return false;
        }
        String token = propertyName.length() > namespace.length() + 1
                ? propertyName.substring(namespace.length() + 1) : "";
        Rule byToken = findRuleByEnumToken(candidates, token);
        Rule chosen = byToken != null ? byToken : candidates.get(0);
        delta.setRuleId(chosen.getRuleId());
        if (byToken != null || candidates.size() == 1) {
            delta.setReason(base + BY_RULE + chosen.getRuleId() + "' (property '" + propertyName + "').");
        } else {
            delta.setReason(base + BY_RULE + chosen.getRuleId() + "' (property namespace '" + namespace
                    + ":' is also written by: " + joinRuleIdsFrom(candidates, 1) + ").");
        }
        return true;
    }

    /** The rule whose enumMap table contains {@code token} (the relationship type), or null. */
    private Rule findRuleByEnumToken(List<Rule> rules, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        for (Rule rule : rules) {
            TransformDefinition transform = rule.getTransform();
            if (transform == null || !"enumMap".equals(transform.getFunction())) {
                continue;
            }
            Object table = transform.getArgs().get("table");
            if (table instanceof Map<?, ?> map && map.containsKey(token)) {
                return rule;
            }
        }
        return null;
    }

    private String addedBaseReason(AnalysisContext ctx) {
        return "Value present in the converted SBOM has no corresponding source value; it was added during the "
                + ctx.sourceFormat + " to " + ctx.targetFormat + " conversion";
    }

    private String joinRuleIdsFrom(List<Rule> rules, int start) {
        StringBuilder out = new StringBuilder();
        for (int i = start; i < rules.size(); i++) {
            if (!out.isEmpty()) {
                out.append(", ");
            }
            out.append(rules.get(i).getRuleId());
        }
        return out.toString();
    }

    /** Longest target-path prefix lookup: return the rules that write into the enclosing container. */
    private List<Rule> findTargetRules(String fieldPath, AnalysisContext ctx) {
        String path = fieldPath;
        while (path != null && !path.isEmpty()) {
            List<Rule> rules = ctx.targetRulesByPath.get(path);
            if (rules != null) {
                return rules;
            }
            int dot = path.lastIndexOf('.');
            if (dot < 0) {
                break;
            }
            path = path.substring(0, dot);
        }
        return List.of();
    }

    /**
     * A value is considered carried over when it appears in the other document. Strings additionally
     * match when the whole value is embedded in a value of the other document (a value carried inside
     * a template or a joined field) or when it ends with a token of the other document (a prefix such
     * as "Organization: " was stripped and the remainder is represented). A token that appears only
     * somewhere inside a longer free-text value - e.g. a component name mentioned in a relationship
     * comment - is NOT treated as carried over, so genuine free-text differences are reported.
     */
    private boolean isPresent(JsonNode node, String text, Set<String> exact, Set<String> substrings) {
        String lower = text.toLowerCase();
        if (exact.contains(lower)) {
            return true;
        }
        if (!node.isTextual() || text.length() < MIN_SUBSTRING_LENGTH) {
            return false;
        }
        for (String candidate : substrings) {
            if (candidate.contains(lower) || lower.endsWith(candidate)) {
                return true;
            }
        }
        return false;
    }

    private ConversionDelta newDelta(JsonNode node, String text, AnalysisContext ctx) {
        ConversionDelta delta = new ConversionDelta();
        delta.setDeltaId("DELTA-%04d".formatted(ctx.sequence.incrementAndGet()));
        delta.setValue(javaValue(node, text));
        delta.setSourceFormat(ctx.sourceFormat);
        delta.setTargetFormat(ctx.targetFormat);
        return delta;
    }

    /**
     * Attribute a category and reason to a source delta by correlating its field path with the rules:
     * a loss rule (or no rule) means the value is LOST, a mapping rule means it was MODIFIED.
     */
    private void classifySourceDelta(ConversionDelta delta, String fieldPath, AnalysisContext ctx) {
        Rule lossRule = ctx.lossRuleByPath.get(fieldPath);
        Rule mappedRule = ctx.mappedRuleByPath.get(fieldPath);
        if (mappedRule != null && lossRule == null) {
            delta.setCategory(ChangeCategory.MODIFIED);
            delta.setRuleId(mappedRule.getRuleId());
            delta.setTargetPath(mappedRule.getTargetPath());
            String function = mappedRule.getTransform() != null ? mappedRule.getTransform().getFunction() : null;
            String transformNote = function != null ? " (transform '" + function + "')" : "";
            delta.setReason("Field is mapped by rule '" + mappedRule.getRuleId() + "'" + transformNote
                    + ", but the original value was transformed and does not appear verbatim in the converted SBOM.");
        } else if (lossRule != null) {
            delta.setCategory(ChangeCategory.LOST);
            delta.setRuleId(lossRule.getRuleId());
            delta.setReason(lossRule.getLossReason() != null ? lossRule.getLossReason()
                    : "Source field has no representation in the target format and is intentionally dropped.");
        } else {
            delta.setCategory(ChangeCategory.LOST);
            delta.setReason("No mapping rule addresses this source field for the " + ctx.sourceFormat + " to "
                    + ctx.targetFormat + " conversion; the value is not represented in the converted SBOM.");
        }
    }

    private String lastSegment(String fieldPath) {
        if (fieldPath == null || fieldPath.isEmpty()) {
            return fieldPath;
        }
        int dot = fieldPath.lastIndexOf('.');
        return dot < 0 ? fieldPath : fieldPath.substring(dot + 1);
    }

    private Object javaValue(JsonNode node, String text) {
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return text;
    }

    /** Per-conversion working state, kept in one holder so the recursive walkers stay low-arity. */
    private static final class AnalysisContext {
        private final String sourceFormat;
        private final String targetFormat;
        private final Set<String> sourceExact = new HashSet<>();
        private final Set<String> sourceSubstrings = new HashSet<>();
        private final Set<String> targetExact = new HashSet<>();
        private final Set<String> targetSubstrings = new HashSet<>();
        private final Map<String, Rule> mappedRuleByPath = new LinkedHashMap<>();
        private final Map<String, Rule> lossRuleByPath = new LinkedHashMap<>();
        private final Map<String, List<Rule>> targetRulesByPath = new LinkedHashMap<>();
        private final Map<String, Rule> ruleByPropertyName = new LinkedHashMap<>();
        private final Map<String, List<Rule>> rulesByPropertyNamespace = new LinkedHashMap<>();
        private final List<ConversionDelta> deltas = new ArrayList<>();
        private final AtomicLong sequence = new AtomicLong();

        private AnalysisContext(String sourceFormat, String targetFormat) {
            this.sourceFormat = sourceFormat;
            this.targetFormat = targetFormat;
        }
    }
}
