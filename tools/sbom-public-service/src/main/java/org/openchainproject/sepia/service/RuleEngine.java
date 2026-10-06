/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.service;

import org.openchainproject.sepia.loss.LossReporter;
import org.openchainproject.sepia.policy.PolicyEngine;
import org.openchainproject.sepia.resolver.PathResolver;
import org.openchainproject.sepia.rule.Rule;
import org.openchainproject.sepia.rule.RuleSet;
import org.openchainproject.sepia.rule.TransformDefinition;
import org.openchainproject.sepia.transform.TransformContext;
import org.openchainproject.sepia.transform.TransformRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RuleEngine {
    private static final String CONSTANT_FUNCTION = "constant";
    private static final String TEMPLATE_FUNCTION = "template";
    private static final String JOIN_FUNCTION = "join";
    private static final String ENUM_MAP_FUNCTION = "enumMap";
    private static final String ROOT_COMPONENT = "metadata.component";
    private static final String COMPONENTS_FIELD = "components";
    private static final String BOM_REF_FIELD = "bom-ref";
    private static final String ANNOTATIONS_FIELD = "annotations";
    private static final String PROPERTIES_FIELD = "properties";
    private static final String CONTAINS_PROPERTY = "spdx:relationship:contains";
    private static final String RELATIONSHIP_PROPERTY_PREFIX = "spdx:relationship:";
    private static final String VALUE_KEY = "value";
    private static final String DEPENDENCIES_FIELD = "dependencies";
    private static final String DEPENDS_ON_FIELD = "dependsOn";
    private static final String REF_FIELD = "ref";
    private static final String SPDX_REF_PREFIX = "SPDXRef-";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z0-9_.\\-]+)\\}");

    // Dependency-like SPDX relationship types feed the CycloneDX dependencies[] graph
    // (MAN-RELATIONSHIP-*), so the structural post-processor must not also record them as
    // spdx:relationship:<TYPE> properties.
    private static final Set<String> DEPENDENCY_RELATIONSHIP_TYPES = Set.of(
            "DEPENDS_ON", "DYNAMIC_LINK", "STATIC_LINK",
            "DEPENDENCY_OF", "TEST_DEPENDENCY_OF", "BUILD_DEPENDENCY_OF", "DEV_DEPENDENCY_OF",
            "OPTIONAL_DEPENDENCY_OF", "PROVIDED_DEPENDENCY_OF", "RUNTIME_DEPENDENCY_OF");

    private final PathResolver pathResolver;
    private final TransformRegistry transforms;
    private final PolicyEngine policy;
    private final LossReporter lossReporter;
    private final ObjectMapper mapper;

    public RuleEngine(PathResolver pathResolver, TransformRegistry transforms, PolicyEngine policy, LossReporter lossReporter, ObjectMapper mapper) {
        this.pathResolver = pathResolver; this.transforms = transforms; this.policy = policy; this.lossReporter = lossReporter; this.mapper = mapper;
    }

    public void execute(RuleSet ruleSet, JsonNode source, JsonNode target) {
        String sf = ruleSet.getAppliesTo().getSource().getFormat()+"-"+ruleSet.getAppliesTo().getSource().getVersion();
        String tf = ruleSet.getAppliesTo().getTarget().getFormat()+"-"+ruleSet.getAppliesTo().getTarget().getVersion();

        // Source normalization: CycloneDX allows components to be nested arbitrarily deep
        // (assemblies). The mapping rules only address the top-level components[] collection,
        // so nested components would otherwise never become their own packages. Hoist every
        // nested component up into the top-level list (deduplicated by bom-ref) so the existing
        // component rules give each one a full sibling package, and record the parent->child
        // nesting as a synthetic x-nestedContainment[] array that a rule maps to CONTAINS.
        JsonNode work = normalizeSource(ruleSet, source);

        TransformContext context = new TransformContext(lossReporter, sf, tf);
        context.assignedIds().add("SPDXRef-DOCUMENT"); // reserved, fabricated by MAN-DOC-SPDXID-001

        // Two-phase evaluation: ordinary mapping rules populate the target document first,
        // then fabrication rules (transform: constant) fill in still-empty mandatory fields.
        // A single pass in file order would let a constant's onlyWhenAbsent check race against
        // mapping rules that have not run yet (e.g. packages[] not created before
        // packages[].downloadLocation defaulting is applied).
        List<Rule> mappingRules = new ArrayList<>();
        List<Rule> constantRules = new ArrayList<>();
        for (Rule r : ruleSet.getRules()) {
            if (isConstant(r)) constantRules.add(r); else mappingRules.add(r);
        }

        for (Rule r : mappingRules) {
            applyMappingRule(r, work, target, context, sf, tf);
        }

        for (Rule r : constantRules) {
            context.forRule(r, r.getSourcePath());
            applyConstantRule(r, target, context);
        }

        // Structural reparenting: a CycloneDX annotation carries subject bom-refs that decide
        // where its rule-built SPDX annotation object belongs. Relocate each finished
        // document-level annotation onto every referenced package (resolved via the idNormalize
        // symbol table); annotations whose subjects match no package stay on the document.
        reparentAnnotations(work, target, context);

        // Structural normalization: the relationship rules emit one flat {ref, dependsOn} edge per
        // SPDX DEPENDS_ON/DEPENDENCY_OF pair. Fold those edges into valid CycloneDX 1.6 shape - one
        // entry per ref with a de-duplicated dependsOn array. No-op unless the target carries a
        // dependencies[] array (the CycloneDX target), so the reverse conversion is untouched.
        normalizeDependencies(target);

        // Structural relationships (OPT-SPDX-REL-STRUCTURAL-001): CONTAINS/CONTAINED_BY degrade to a
        // namespaced property on the parent component; ANCESTOR_OF/DESCENDANT_OF/VARIANT_OF populate
        // that component's pedigree. DESCRIBES/DESCRIBED_BY are already covered by the MAN-ROOT-* rules
        // (metadata.component). Runs only when the source carries relationships[] and the target carries
        // components[] - the SPDX->CycloneDX direction - so the reverse conversion is untouched.
        applyStructuralRelationships(work, target);
    }

    /**
     * Fold the flat dependency edges produced by the relationship rules into valid CycloneDX 1.6
     * shape: one entry per {@code ref} whose {@code dependsOn} is a de-duplicated array of bom-refs.
     * Each relationship edge arrives as a {@code {ref, dependsOn}} object (dependsOn carried as a
     * scalar or bracketed token by the template), so this step groups edges by ref, unwraps each
     * dependsOn token, and rewrites the SPDX element ids onto the component bom-refs the rules
     * produced (SPDXRef- prefix stripped, matching OPT-SPDX-PKG-SPDXID-001 / OPT-SPDX-FILE-BOMREF-001).
     * Runs only when the target already carries a {@code dependencies[]} array, so the reverse
     * CycloneDX->SPDX conversion - whose dependency graph lives in {@code relationships[]} - is
     * left unchanged.
     */
    private void normalizeDependencies(JsonNode target) {
        JsonNode depsNode = target.get(DEPENDENCIES_FIELD);
        if (!(depsNode instanceof ArrayNode deps) || deps.isEmpty()) {
            return;
        }
        Map<String, List<String>> edges = new LinkedHashMap<>();
        for (JsonNode edge : deps) {
            String ref = rewriteBomRef(edge.path(REF_FIELD).asText(""));
            if (ref.isEmpty()) continue; // an edge with no owning ref cannot be represented in CycloneDX
            List<String> bucket = edges.computeIfAbsent(ref, k -> new ArrayList<>());
            for (String dep : dependsOnTokens(edge.get(DEPENDS_ON_FIELD))) {
                String rewritten = rewriteBomRef(dep);
                if (!rewritten.isEmpty() && !bucket.contains(rewritten)) bucket.add(rewritten);
            }
        }
        ArrayNode rebuilt = mapper.createArrayNode();
        for (Map.Entry<String, List<String>> e : edges.entrySet()) {
            ObjectNode node = mapper.createObjectNode();
            node.put(REF_FIELD, e.getKey());
            ArrayNode dependsOn = node.putArray(DEPENDS_ON_FIELD);
            for (String dep : e.getValue()) dependsOn.add(dep);
            rebuilt.add(node);
        }
        ((ObjectNode) target).set(DEPENDENCIES_FIELD, rebuilt);
    }

    /**
     * Unwrap a {@code dependsOn} value into its individual bom-ref tokens. Accepts a JSON array, a
     * bracketed string token ({@code "[a]"} / {@code "[]"} left by a {@code "[{relatedSpdxElement}]"}
     * template) or a bare scalar; empty tokens are discarded.
     */
    private List<String> dependsOnTokens(JsonNode dependsOn) {
        List<String> tokens = new ArrayList<>();
        if (dependsOn == null || dependsOn.isNull() || dependsOn.isMissingNode()) {
            return tokens;
        }
        if (dependsOn.isArray()) {
            for (JsonNode el : dependsOn) {
                if (el.isValueNode()) addToken(tokens, el.asText());
            }
            return tokens;
        }
        String text = dependsOn.asText("").trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1);
        }
        for (String part : text.split(",")) {
            addToken(tokens, part.trim());
        }
        return tokens;
    }

    private void addToken(List<String> tokens, String token) {
        if (token != null && !token.isEmpty()) tokens.add(token);
    }

    /** Rewrite an SPDX element id onto the component bom-ref convention (leading SPDXRef- stripped). */
    private String rewriteBomRef(String id) {
        if (id == null || id.isEmpty()) return "";
        return id.startsWith(SPDX_REF_PREFIX) ? id.substring(SPDX_REF_PREFIX.length()) : id;
    }

    /**
     * Apply the SPDX structural relationships to a CycloneDX target (OPT-SPDX-REL-STRUCTURAL-001).
     * CONTAINS/CONTAINED_BY are recorded flat as a {@code spdx:relationship:contains} property on the
     * parent component (no tree restructuring, so no duplicate bom-refs); ANCESTOR_OF/DESCENDANT_OF/
     * VARIANT_OF/DESCRIBES/DESCRIBED_BY - and every other non-dependency, non-containment type
     * (BUILD_TOOL_OF, GENERATES, TEST_OF, AMENDS, ...; OPT-SPDX-REL-OTHER-001) - are recorded flat as
     * a {@code spdx:relationship:<TYPE>} property on the spdxElementId component, valued with the
     * relatedSpdxElement bom-ref, so they are preserved rather than lost.
     * DESCRIBES/DESCRIBED_BY are recorded here in addition to metadata.component, which the
     * MAN-ROOT-* rules still populate; dependency-like types feed dependencies[] instead. Endpoint
     * ids are rewritten SPDXRef- -> bom-ref. Guarded on an SPDX source (relationships[]) and a
     * CycloneDX target (components[]) so the reverse conversion is a no-op.
     */
    private void applyStructuralRelationships(JsonNode source, JsonNode target) {
        JsonNode relationships = source.path("relationships");
        JsonNode componentsNode = target.get(COMPONENTS_FIELD);
        if (!relationships.isArray() || !(componentsNode instanceof ArrayNode components)) {
            return;
        }
        Map<String, ObjectNode> byRef = new LinkedHashMap<>();
        for (JsonNode c : components) {
            if (c.isObject()) {
                String ref = textField(c, BOM_REF_FIELD);
                if (ref != null) byRef.put(ref, (ObjectNode) c);
            }
        }
        for (JsonNode rel : relationships) {
            String type = rel.path("relationshipType").asText("");
            String element = rewriteBomRef(rel.path("spdxElementId").asText(""));
            String related = rewriteBomRef(rel.path("relatedSpdxElement").asText(""));
            if ("CONTAINS".equals(type)) {
                addContainmentProperty(byRef.get(element), related);
            } else if ("CONTAINED_BY".equals(type)) {
                addContainmentProperty(byRef.get(related), element);
            } else if (!type.isEmpty() && !DEPENDENCY_RELATIONSHIP_TYPES.contains(type)) {
                // ANCESTOR_OF/DESCENDANT_OF/VARIANT_OF/DESCRIBES/DESCRIBED_BY (OPT-SPDX-REL-STRUCTURAL-001)
                // and every other non-dependency, non-containment type - BUILD_TOOL_OF, GENERATES,
                // TEST_OF, AMENDS, ... (OPT-SPDX-REL-OTHER-001) - are recorded flat as a
                // spdx:relationship:<TYPE> property on the spdxElementId component so they are
                // preserved rather than lost. DESCRIBES/DESCRIBED_BY are also on metadata.component
                // via MAN-ROOT-*; dependency-like types instead feed dependencies[].
                addRelationshipProperty(byRef.get(element), type, related);
            }
        }
    }

    /** Record an SPDX CONTAINS edge as a namespaced property on the parent component (flat, non-destructive). */
    private void addContainmentProperty(ObjectNode parent, String childRef) {
        if (parent == null || childRef == null || childRef.isEmpty()) {
            return;
        }
        ObjectNode prop = parent.withArray(PROPERTIES_FIELD).addObject();
        prop.put("name", CONTAINS_PROPERTY);
        prop.put(VALUE_KEY, childRef);
    }

    /** Record an SPDX structural relationship as a flat {@code spdx:relationship:<TYPE>} property on the subject component. */
    private void addRelationshipProperty(ObjectNode subject, String type, String objectRef) {
        if (subject == null || objectRef == null || objectRef.isEmpty()) {
            return;
        }
        ObjectNode prop = subject.withArray(PROPERTIES_FIELD).addObject();
        prop.put("name", RELATIONSHIP_PROPERTY_PREFIX + type);
        prop.put(VALUE_KEY, objectRef);
    }

    /**
     * Reparent finished document-level annotations onto the packages named by their CycloneDX
     * {@code subjects} bom-refs. The annotation objects themselves are produced by the ordinary
     * annotation rules (annotator / annotationDate / comment / annotationType); this step only
     * relocates them. Document-level {@code annotations[]} stays index-aligned with the source
     * annotations, so index {@code i} of the target array corresponds to source annotation
     * {@code i}. An annotation is removed from the document once it has been placed on at least one
     * package; if none of its subjects resolve to a package it is left in place.
     */
    private void reparentAnnotations(JsonNode source, JsonNode target, TransformContext context) {
        JsonNode srcAnnotations = source.path(ANNOTATIONS_FIELD);
        JsonNode outAnnotationsNode = target.get(ANNOTATIONS_FIELD);
        JsonNode packagesNode = target.get("packages");
        if (!srcAnnotations.isArray() || !(outAnnotationsNode instanceof ArrayNode) || !(packagesNode instanceof ArrayNode)) {
            return;
        }
        ArrayNode outAnnotations = (ArrayNode) outAnnotationsNode;
        ArrayNode packages = (ArrayNode) packagesNode;

        // The built document-level annotations are not necessarily in source order (a many-source
        // firstOnly rule allocates slots in match order), so correlate each source annotation to its
        // built object by content signature (text + timestamp) rather than by position.
        Set<Integer> consumed = new HashSet<>();
        List<Integer> placedIndices = new ArrayList<>();
        for (JsonNode srcAnnotation : srcAnnotations) {
            JsonNode subjects = srcAnnotation.path("subjects");
            if (!subjects.isArray() || subjects.isEmpty()) continue;
            int outIdx = matchOutputAnnotation(srcAnnotation, outAnnotations, consumed);
            if (outIdx < 0) continue;
            if (placeOnSubjects(subjects, outAnnotations.get(outIdx), packages, context)) {
                consumed.add(outIdx);
                placedIndices.add(outIdx);
            }
        }

        // Remove reparented annotations from the document level, highest index first so the
        // remaining indices stay valid.
        placedIndices.sort(Collections.reverseOrder());
        for (int idx : placedIndices) {
            outAnnotations.remove(idx);
        }
        if (outAnnotations.isEmpty() && target.isObject()) {
            ((ObjectNode) target).remove(ANNOTATIONS_FIELD);
        }
    }

    /**
     * Find the built document-level annotation that corresponds to {@code srcAnnotation}, matched by
     * a content signature (comment/text plus annotationDate/timestamp) so the correlation is
     * independent of emission order. Returns the first unconsumed matching index, or {@code -1}.
     */
    private int matchOutputAnnotation(JsonNode srcAnnotation, ArrayNode outAnnotations, Set<Integer> consumed) {
        String signature = srcAnnotation.path("text").asText("") + '\u0000' + srcAnnotation.path("timestamp").asText("");
        for (int k = 0; k < outAnnotations.size(); k++) {
            if (consumed.contains(k)) continue;
            JsonNode out = outAnnotations.get(k);
            String outSignature = out.path("comment").asText("") + '\u0000' + out.path("annotationDate").asText("");
            if (signature.equals(outSignature)) {
                return k;
            }
        }
        return -1;
    }

    /**
     * Append a copy of {@code annotation} onto every package named by {@code subjects} whose bom-ref
     * resolves to a package SPDXID. Returns {@code true} if it was placed on at least one package.
     */
    private boolean placeOnSubjects(JsonNode subjects, JsonNode annotation, ArrayNode packages, TransformContext context) {
        if (!subjects.isArray() || subjects.isEmpty()) {
            return false;
        }
        boolean placed = false;
        for (JsonNode subject : subjects) {
            String spdxId = subject.isValueNode() ? context.idMap().get(subject.asText()) : null;
            ObjectNode pkg = spdxId == null ? null : findPackageBySpdxId(packages, spdxId);
            if (pkg != null) {
                pkg.withArray(ANNOTATIONS_FIELD).add(annotation.deepCopy());
                placed = true;
            }
        }
        return placed;
    }

    private ObjectNode findPackageBySpdxId(ArrayNode packages, String spdxId) {
        for (JsonNode p : packages) {
            if (p.isObject() && spdxId.equals(p.path("SPDXID").asText(null))) {
                return (ObjectNode) p;
            }
        }
        return null;
    }

    /**
     * Flatten nested CycloneDX components into the top-level {@code components[]} list and expose
     * the containment hierarchy as a synthetic {@code x-nestedContainment[]} array of
     * {@code {parent, child}} bom-ref pairs. The input document is not mutated (a deep copy is
     * returned) so callers keep their original source. For an SPDX source the DESCRIBES fallback
     * (see {@link #normalizeSpdxSource(JsonNode)}) is applied instead; any other source is returned
     * unchanged.
     */
    private JsonNode normalizeSource(RuleSet ruleSet, JsonNode source) {
        String sourceFormat = ruleSet.getAppliesTo().getSource().getFormat();
        if (!source.isObject()) {
            return source;
        }
        if ("spdx".equalsIgnoreCase(sourceFormat)) {
            return normalizeSpdxSource(source);
        }
        if (!"cyclonedx".equalsIgnoreCase(sourceFormat)) {
            return source;
        }
        ObjectNode root = (ObjectNode) source.deepCopy();
        ArrayNode components = root.withArray(COMPONENTS_FIELD);
        ArrayNode containment = mapper.createArrayNode();

        Set<String> seen = new HashSet<>();
        List<ObjectNode> topLevel = new ArrayList<>();
        for (JsonNode c : components) {
            if (c.isObject()) {
                topLevel.add((ObjectNode) c);
                String ref = textField(c, BOM_REF_FIELD);
                if (ref != null) seen.add(ref);
            }
        }

        // The root component (metadata.component) can also assemble children.
        JsonNode metaComponent = root.path("metadata").path("component");
        if (metaComponent.isObject()) {
            hoistNestedComponents((ObjectNode) metaComponent, components, seen, containment);
        }
        for (ObjectNode c : topLevel) {
            hoistNestedComponents(c, components, seen, containment);
        }

        if (!containment.isEmpty()) {
            root.set("x-nestedContainment", containment);
        }
        return root;
    }

    /**
     * Ensure an SPDX source exposes {@code documentDescribes}. When it is absent or empty, derive it
     * from the document's DESCRIBES / DESCRIBED_BY relationships (a DESCRIBES names the described
     * element in {@code relatedSpdxElement}; a DESCRIBED_BY names it in {@code spdxElementId}) so the
     * root-package rules, which read {@code packages[?SPDXID in documentDescribes]}, can still
     * populate {@code metadata.component}. A source that already declares a non-empty
     * {@code documentDescribes} is returned unchanged, and the original node is never mutated.
     */
    private JsonNode normalizeSpdxSource(JsonNode source) {
        JsonNode existing = source.get("documentDescribes");
        if (existing != null && existing.isArray() && !existing.isEmpty()) {
            return source;
        }
        JsonNode relationships = source.get("relationships");
        if (!(relationships instanceof ArrayNode)) {
            return source;
        }
        Set<String> described = new LinkedHashSet<>();
        for (JsonNode rel : relationships) {
            String type = rel.path("relationshipType").asText("");
            if ("DESCRIBES".equals(type)) {
                addDescribed(described, rel.path("relatedSpdxElement"));
            } else if ("DESCRIBED_BY".equals(type)) {
                addDescribed(described, rel.path("spdxElementId"));
            }
        }
        if (described.isEmpty()) {
            return source;
        }
        ObjectNode root = (ObjectNode) source.deepCopy();
        ArrayNode derived = root.putArray("documentDescribes");
        for (String id : described) {
            derived.add(id);
        }
        return root;
    }

    private void addDescribed(Set<String> target, JsonNode idNode) {
        if (idNode != null && idNode.isValueNode()) {
            String id = idNode.asText();
            if (!id.isEmpty()) {
                target.add(id);
            }
        }
    }

    /**
     * Recursively lift {@code parent.components[]} into the top-level {@code components} array.
     * Each nested child yields a {@code parent -> child} containment edge; a child whose bom-ref
     * has not been seen before is hoisted as a new sibling package (duplicates are collapsed so a
     * component assembled in several places still produces a single package). The parent's
     * {@code components} field is removed once its children have been processed.
     */
    private void hoistNestedComponents(ObjectNode parent, ArrayNode components, Set<String> seen, ArrayNode containment) {
        JsonNode children = parent.get(COMPONENTS_FIELD);
        parent.remove(COMPONENTS_FIELD);
        if (children == null || !children.isArray()) {
            return;
        }
        String parentRef = textField(parent, BOM_REF_FIELD);
        for (JsonNode child : children) {
            if (!child.isObject()) continue;
            ObjectNode childNode = (ObjectNode) child;
            String childRef = textField(childNode, BOM_REF_FIELD);
            if (parentRef != null && childRef != null) {
                ObjectNode edge = mapper.createObjectNode();
                edge.put("parent", parentRef);
                edge.put("child", childRef);
                containment.add(edge);
            }
            boolean isNew = childRef == null || seen.add(childRef);
            // Recurse first so grandchildren are hoisted regardless of whether this child is a
            // duplicate that is itself collapsed away.
            hoistNestedComponents(childNode, components, seen, containment);
            if (isNew) {
                components.add(childNode);
            }
        }
    }

    private String textField(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && v.isValueNode() && !v.isNull() ? v.asText() : null;
    }

    private void applyMappingRule(Rule r, JsonNode source, JsonNode target, TransformContext context, String sf, String tf) {
        context.forRule(r, r.getSourcePath());
        List<Leaf> leaves = new ArrayList<>();
        for (String sourcePath : r.getSourcePaths()) {
            for (PathResolver.ResolvedValue rv : pathResolver.resolve(source, sourcePath)) {
                leaves.add(new Leaf(sourcePath, rv.getValue(), rv.getIndexPath()));
            }
        }
        if (leaves.isEmpty()) {
            policy.handleAbsent(r, sf, tf, r.getSourcePath(), lossReporter);
            return;
        }
        List<String> targetPaths = r.getTargetPaths();
        if (targetPaths.isEmpty()) {
            // Loss rule (targetPath: null): the source data is present but has no
            // representation in the target format, so it is dropped and reported.
            policy.handleUnmappable(r, sf, tf, r.getSourcePath(), lossReporter, "Source field has no target mapping");
            return;
        }
        if (usesNamedFields(r)) {
            // Sibling-field templates ({name} ({email}), Tool: {group}-{name}-{version},
            // dependency {ref}/{dependsOn}, ...) need the whole source entity, not isolated
            // leaves, so correlate leaves back into their originating entity first.
            emitCorrelated(r, leaves, target, context);
        } else if (isDispatchEnumMap(r)) {
            // Structured target dispatch: the enumMap table values are target paths and a
            // separate dispatch field (e.g. license.acknowledgement) chooses which target the
            // pass-through value (license id/expression) is written to.
            emitDispatched(r, leaves, target, context);
        } else if (isJoin(r)) {
            // many-to-one join: correlate sibling fields (e.g. a property's name/value) into one
            // entity object each, then fold every entity into a single joined string per target
            // slot. A plain per-leaf write would instead let each leaf overwrite the scalar target.
            emitJoined(r, leaves, target, context);
        } else {
            for (Leaf lf : leaves) {
                write(r, jsonToJava(lf.value), lf.sourcePath, lf.indexPath, target, context);
            }
        }
    }

    /**
     * Transform a value and write it to every target path, correlating each target array position
     * to the source entity it came from. Failures are reported as unmappable losses.
     */
    private void write(Rule r, Object input, String sourcePath, String indexPath, JsonNode target, TransformContext context) {
        try {
            // Expose the source leaf's field name and index to the transform so per-leaf templates
            // can reference the {field} / {index} engine variables (e.g. degraded-property rules
            // that emit {name: "spdx:package:{field}", value: "{value}"}).
            context.setCurrentField(leafFieldName(sourcePath));
            context.setCurrentIndex(lastIndexSegment(indexPath));
            Object transformed = transforms.transform(r.getTransform(), input, context);
            JsonNode targetValue = mapper.valueToTree(transformed);
            for (String targetPath : r.getTargetPaths()) {
                pathResolver.set(target, targetPath, targetValue, slotsFor(targetPath, sourcePath, indexPath, context));
            }
        } catch (RuntimeException ex) {
            policy.handleUnmappable(r, context.getSourceFormat(), context.getTargetFormat(),
                    r.getSourcePath(), lossReporter, ex.getMessage());
        }
    }

    /** The leaf field name of a source path (last dot segment with any trailing array marker removed). */
    private String leafFieldName(String sourcePath) {
        if (sourcePath == null) return "";
        int dot = sourcePath.lastIndexOf('.');
        String seg = dot < 0 ? sourcePath : sourcePath.substring(dot + 1);
        int bracket = seg.indexOf('[');
        if (bracket >= 0) seg = seg.substring(0, bracket);
        return seg;
    }

    /** The last index segment of an index path (e.g. "1" for "2.1"), or "" when there is none. */
    private String lastIndexSegment(String indexPath) {
        if (indexPath == null || indexPath.isEmpty()) return "";
        int dot = indexPath.lastIndexOf('.');
        return dot < 0 ? indexPath : indexPath.substring(dot + 1);
    }

    /**
     * Rebuild source entities from the flat list of resolved leaves and emit one output per
     * entity, so a template that references several sibling fields (possibly at different
     * nesting depths) resolves them together instead of producing one degenerate output per leaf.
     */
    private void emitCorrelated(Rule r, List<Leaf> leaves, JsonNode target, TransformContext context) {
        // A rule may pull from more than one source collection (e.g. components[] and
        // metadata.component); process each collection independently so their entities do not mix.
        Map<String, List<Leaf>> byCollection = new LinkedHashMap<>();
        for (Leaf lf : leaves) {
            byCollection.computeIfAbsent(collectionRoot(lf.sourcePath), k -> new ArrayList<>()).add(lf);
        }
        for (List<Leaf> group : byCollection.values()) {
            emitCorrelatedGroup(r, group, target, context);
        }
    }

    private boolean isJoin(Rule r) {
        return r.getTransform() != null && JOIN_FUNCTION.equals(r.getTransform().getFunction());
    }

    /**
     * True for an {@code enumMap} rule that performs structured target dispatch: a dispatch
     * {@code field} names a sibling source value (e.g. {@code license.acknowledgement}), the
     * {@code table} maps that field's values onto the rule's own target paths, and there is more
     * than one target path to choose between. Distinguishes the license declared/concluded rule
     * from ordinary value-mapping enumMaps (type, hash algorithm) whose table values are literals.
     */
    private boolean isDispatchEnumMap(Rule r) {
        TransformDefinition t = r.getTransform();
        if (t == null || !ENUM_MAP_FUNCTION.equals(t.getFunction()) || t.getArgs() == null) return false;
        Map<String, Object> args = t.getArgs();
        if (!(args.get("field") instanceof String) || !(args.get("table") instanceof Map<?, ?> table)) {
            return false;
        }
        List<String> targets = r.getTargetPaths();
        if (targets.size() < 2) return false;
        for (Object v : table.values()) {
            if (targets.contains(String.valueOf(v))) return true;
        }
        return false;
    }

    /**
     * Structured target dispatch for {@code enumMap}. Each source entity (e.g. one license) is
     * rebuilt from its correlated leaves: the dispatch {@code field} (e.g. acknowledgement) selects
     * a target path from the {@code table} (falling back to {@code default}), and the entity's
     * pass-through value (the first non-dispatch source leaf, e.g. the SPDX license id or
     * expression) is written verbatim to just that one target. When an entity has no pass-through
     * value (e.g. a named license carrying only an acknowledgement) the rule emits nothing, so the
     * dispatch key never leaks into the output as a literal.
     */
    private void emitDispatched(Rule r, List<Leaf> leaves, JsonNode target, TransformContext context) {
        // Correlate leaves by the individual source entity they belong to so the dispatch field is
        // read from the same entity as the value it steers.
        Map<String, List<Leaf>> byEntity = new LinkedHashMap<>();
        for (Leaf lf : leaves) {
            byEntity.computeIfAbsent(collectionRoot(lf.sourcePath) + '#' + lf.indexPath, k -> new ArrayList<>()).add(lf);
        }
        for (List<Leaf> group : byEntity.values()) {
            emitDispatchedEntity(r, group, target, context);
        }
    }

    private void emitDispatchedEntity(Rule r, List<Leaf> group, JsonNode target, TransformContext context) {
        Map<String, Object> args = r.getTransform().getArgs();
        String field = String.valueOf(args.get("field"));
        Map<?, ?> table = (Map<?, ?>) args.get("table");
        String defaultTarget = args.get("default") != null ? String.valueOf(args.get("default")) : null;

        Leaf valueLeaf = null;
        Leaf dispatchLeaf = null;
        for (Leaf lf : group) {
            boolean isDispatch = lastSegment(lf.sourcePath).equals(field);
            if (isDispatch && dispatchLeaf == null) {
                dispatchLeaf = lf;
            } else if (!isDispatch && valueLeaf == null) {
                valueLeaf = lf;
            }
        }
        // No pass-through value (e.g. a named license carrying only an acknowledgement): the rule
        // emits nothing, so the dispatch key never leaks into the output as a literal.
        if (valueLeaf == null) return;

        String dispatchKey = dispatchLeaf != null && dispatchLeaf.value.isValueNode() ? dispatchLeaf.value.asText() : null;
        Object mappedTarget = dispatchKey != null ? table.get(dispatchKey) : null;
        String targetPath = mappedTarget != null ? String.valueOf(mappedTarget) : defaultTarget;
        if (targetPath == null) return;

        try {
            JsonNode targetValue = mapper.valueToTree(jsonToJava(valueLeaf.value));
            pathResolver.set(target, targetPath, targetValue,
                    slotsFor(targetPath, valueLeaf.sourcePath, valueLeaf.indexPath, context));
        } catch (RuntimeException ex) {
            policy.handleUnmappable(r, context.getSourceFormat(), context.getTargetFormat(),
                    r.getSourcePath(), lossReporter, ex.getMessage());
        }
    }

    /** The last path segment with any trailing array marker removed (e.g. "acknowledgement", "content"). */
    private String lastSegment(String path) {
        int dot = path.lastIndexOf('.');
        String seg = dot < 0 ? path : path.substring(dot + 1);
        return seg.endsWith("[]") ? seg.substring(0, seg.length() - 2) : seg;
    }


    /**
     * Fold every resolved leaf into a single joined value per target slot. Leaves are grouped by
     * the target entity they belong to (a scalar target yields one global group), sibling fields of
     * the same source entity (e.g. a property's name/value) are correlated into one object, and the
     * {@code join} transform renders the ordered items with its {@code itemFormat}/{@code separator}/
     * {@code prefix} args. Declared source-path order and document order are preserved.
     */
    private void emitJoined(Rule r, List<Leaf> leaves, JsonNode target, TransformContext context) {
        String targetPath = r.getTargetPaths().get(0);
        boolean perEntity = nonTerminalArrayField(targetPath) != null;
        Map<String, List<Leaf>> groups = new LinkedHashMap<>();
        for (Leaf lf : leaves) {
            String key = perEntity ? entityKey(lf.sourcePath, lf.indexPath) : "";
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(lf);
        }
        for (List<Leaf> group : groups.values()) {
            List<Object> items = buildJoinItems(group);
            if (items.isEmpty()) continue;
            Leaf rep = group.get(0);
            write(r, items, rep.sourcePath, rep.indexPath, target, context);
        }
    }

    /**
     * Correlate a group of leaves into an ordered list of join items. A leaf whose source path ends
     * in a named field (e.g. {@code properties[].name}) is merged with its siblings sharing the same
     * parent entity into one object; a leaf whose source path ends in a bare array element
     * (e.g. {@code components[].tags[]}) becomes a standalone scalar item. First-encounter order is
     * kept so items follow declared source-path and document order.
     */
    private List<Object> buildJoinItems(List<Leaf> group) {
        List<Object> items = new ArrayList<>();
        Map<String, ObjectNode> objectsByEntity = new LinkedHashMap<>();
        for (Leaf lf : group) {
            String field = joinFieldName(lf.sourcePath);
            if (field == null) {
                items.add(jsonToJava(lf.value));
                continue;
            }
            String entity = joinEntityPath(lf.sourcePath) + '#' + lf.indexPath;
            ObjectNode obj = objectsByEntity.computeIfAbsent(entity, k -> {
                ObjectNode created = mapper.createObjectNode();
                items.add(created);
                return created;
            });
            if (!obj.has(field)) obj.set(field, lf.value);
        }
        return items;
    }

    /** The named leaf field of a join source path, or null when the path ends in a bare array element. */
    private String joinFieldName(String sourcePath) {
        String last = sourcePath.substring(sourcePath.lastIndexOf('.') + 1);
        return last.endsWith("[]") ? null : last;
    }

    /** The parent-entity path of a join source path (path minus its trailing named field). */
    private String joinEntityPath(String sourcePath) {
        int dot = sourcePath.lastIndexOf('.');
        if (dot < 0) return sourcePath;
        return sourcePath.substring(dot + 1).endsWith("[]") ? sourcePath : sourcePath.substring(0, dot);
    }

    private void emitCorrelatedGroup(Rule r, List<Leaf> group, JsonNode target, TransformContext context) {
        List<String> orderedPaths = new ArrayList<>();
        for (String sp : r.getSourcePaths()) {
            if (!orderedPaths.contains(sp) && containsPath(group, sp)) orderedPaths.add(sp);
        }
        if (orderedPaths.isEmpty()) return;

        String anchor = longestCommonPrefix(orderedPaths);
        String primary = primarySourcePath(group, orderedPaths);
        for (Leaf pl : leavesOf(group, primary)) {
            ObjectNode fields = mapper.createObjectNode();
            for (String sp : orderedPaths) {
                Leaf best = bestMatch(group, sp, pl.indexPath);
                if (best != null) putAliases(fields, sp, anchor, best.value);
            }
            write(r, fields, primary, pl.indexPath, target, context);
        }
    }

    /**
     * The collection that drives iteration: the source path whose resolved leaves are the deepest
     * (e.g. dependencies[].dependsOn[] over dependencies[].ref) so each edge/element becomes its
     * own output; ties resolve to declaration order.
     */
    private String primarySourcePath(List<Leaf> group, List<String> orderedPaths) {
        String primary = orderedPaths.get(0);
        int primaryDepth = maxDepth(group, primary);
        for (String sp : orderedPaths) {
            int d = maxDepth(group, sp);
            if (d > primaryDepth) {
                primary = sp;
                primaryDepth = d;
            }
        }
        return primary;
    }

    /**
     * True for template rules referencing named sibling fields. The engine variables
     * {@code {value}}, {@code {field}} (the source leaf's field name) and {@code {index}} are not
     * sibling fields, so a template using only those stays on the per-leaf path (where {@code {field}}
     * is resolved per leaf) instead of being correlated into a single entity object.
     */
    private boolean usesNamedFields(Rule r) {
        TransformDefinition t = r.getTransform();
        if (t == null || !TEMPLATE_FUNCTION.equals(t.getFunction()) || t.getArgs() == null) return false;
        for (Object v : t.getArgs().values()) {
            Matcher m = PLACEHOLDER.matcher(String.valueOf(v));
            while (m.find()) {
                String key = m.group(1);
                if (!VALUE_KEY.equals(key) && !"field".equals(key) && !"index".equals(key)) return true;
            }
        }
        return false;
    }

    /** Stable target array position for this source entity, or null when the target has no correlating array. */
    private List<Integer> slotsFor(String targetPath, String sourcePath, String indexPath, TransformContext context) {
        List<String> arrayFields = nonTerminalArrayFields(targetPath);
        if (arrayFields.isEmpty()) {
            return Collections.emptyList();
        }
        String collection = collectionRoot(sourcePath);
        String[] idx = indexPath.isEmpty() ? new String[0] : indexPath.split("\\.");
        List<Integer> slots = new ArrayList<>(arrayFields.size());
        for (int k = 0; k < arrayFields.size(); k++) {
            if (k == 0) {
                // Outermost array: identical keying to the historical single-slot behaviour, so
                // every existing single-array rule allocates exactly the same positions as before.
                slots.add(context.allocateSlot(arrayFields.get(0), entityKey(sourcePath, indexPath)));
            } else {
                // Deeper array (e.g. authors[] under components[].authors[].name): a counter scoped
                // to the parent entity so positions restart within each parent, keyed by this
                // level's own source index. When the source has no matching depth (a scalar leaf,
                // e.g. two rules each writing one hash field) the shared key merges their writes
                // into one element instead of scattering them across padded slots.
                String counter = arrayFields.get(k) + '@' + collection + '#' + joinIndex(idx, k);
                String levelKey = k < idx.length ? idx[k] : "-";
                slots.add(context.allocateSlot(counter, levelKey));
            }
        }
        return slots;
    }

    /** Join the first {@code count} index-path components (parent scope for a nested target array). */
    private String joinIndex(String[] idx, int count) {
        int n = Math.min(count, idx.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append('.');
            sb.append(idx[i]);
        }
        return sb.toString();
    }

    /**
     * Every non-terminal array of a target path, each returned as its full dotted path prefix and
     * ordered outermost first (e.g. {@code ["components", "components.authors"]} for
     * {@code components[].authors[].name}). Using the full prefix - rather than just the leaf field
     * name - keeps distinct arrays that share a leaf name (top-level {@code components[]} vs
     * {@code metadata.tools.components[]}) in separate slot counters. A terminal array
     * (e.g. {@code creationInfo.creators[]}) is not included, so its entries append.
     */
    private List<String> nonTerminalArrayFields(String targetPath) {
        String[] tokens = targetPath.split("\\.");
        List<String> fields = new ArrayList<>();
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            if (!prefix.isEmpty()) prefix.append('.');
            if (tokens[i].endsWith("[]")) {
                prefix.append(tokens[i], 0, tokens[i].length() - 2);
                fields.add(prefix.toString());
            } else {
                prefix.append(tokens[i]);
            }
        }
        return fields;
    }

    /**
     * The first non-terminal array of a target path, or null when there is none. Retained for the
     * join rules, which only need to know whether the target correlates per entity at all.
     */
    private String nonTerminalArrayField(String targetPath) {
        List<String> fields = nonTerminalArrayFields(targetPath);
        return fields.isEmpty() ? null : fields.get(0);
    }

    /**
     * A key that identifies the source entity a value belongs to, stable across every rule.
     * Values from the same array element (e.g. all of component 0's fields, however deeply nested)
     * share a key; the singleton root component is kept distinct from the {@code components[]} entries.
     */
    private String entityKey(String sourcePath, String indexPath) {
        String collection = collectionRoot(sourcePath);
        if (ROOT_COMPONENT.equals(collection)) {
            // The document's root component is a single package: every field, including those under
            // nested arrays (metadata.component.externalReferences[], .hashes[], ...), belongs to it.
            return collection + "#";
        }
        int dot = indexPath.indexOf('.');
        String firstIndex;
        if (indexPath.isEmpty()) {
            firstIndex = "";
        } else if (dot < 0) {
            firstIndex = indexPath;
        } else {
            firstIndex = indexPath.substring(0, dot);
        }
        return collection + "#" + firstIndex;
    }

    private String collectionRoot(String sourcePath) {
        // Treat the root component as one collection regardless of nested arrays inside it.
        if (sourcePath.startsWith(ROOT_COMPONENT)) return ROOT_COMPONENT;
        // The collection ends at the first array marker, whether a plain "[]" or a filter predicate
        // "[?...]", so sibling leaves pulled from the same (possibly filtered) array collection - e.g.
        // a relationship's spdxElementId and relatedSpdxElement under
        // relationships[?relationshipType=='DEPENDS_ON'] - correlate into one entity instead of
        // splitting into separate, half-populated outputs.
        int open = sourcePath.indexOf('[');
        if (open >= 0) {
            int close = sourcePath.indexOf(']', open);
            if (close >= 0) return sourcePath.substring(0, close + 1);
        }
        return sourcePath;
    }

    private boolean containsPath(List<Leaf> group, String sourcePath) {
        for (Leaf lf : group) if (lf.sourcePath.equals(sourcePath)) return true;
        return false;
    }

    private List<Leaf> leavesOf(List<Leaf> group, String sourcePath) {
        List<Leaf> out = new ArrayList<>();
        for (Leaf lf : group) if (lf.sourcePath.equals(sourcePath)) out.add(lf);
        return out;
    }

    private int maxDepth(List<Leaf> group, String sourcePath) {
        int max = -1;
        for (Leaf lf : group) {
            if (lf.sourcePath.equals(sourcePath)) max = Math.max(max, depth(lf.indexPath));
        }
        return max;
    }

    private int depth(String indexPath) {
        if (indexPath.isEmpty()) return 0;
        int d = 1;
        for (int i = 0; i < indexPath.length(); i++) if (indexPath.charAt(i) == '.') d++;
        return d;
    }

    /** The leaf of {@code sourcePath} whose index path best correlates with {@code target} (equal, else closest ancestor). */
    private Leaf bestMatch(List<Leaf> group, String sourcePath, String target) {
        Leaf best = null;
        for (Leaf lf : group) {
            boolean candidate = lf.sourcePath.equals(sourcePath) && isPrefixOrEqual(lf.indexPath, target);
            if (candidate && (best == null || lf.indexPath.length() > best.indexPath.length())) {
                best = lf;
            }
        }
        return best;
    }

    private boolean isPrefixOrEqual(String prefix, String path) {
        return prefix.isEmpty() || prefix.equals(path) || path.startsWith(prefix + ".");
    }

    /** Longest common segment prefix of the paths, capped so at least one suffix segment always remains. */
    private String longestCommonPrefix(List<String> paths) {
        String[] first = paths.get(0).split("\\.");
        int common = first.length;
        int shortest = first.length;
        for (String p : paths) {
            String[] tok = p.split("\\.");
            shortest = Math.min(shortest, tok.length);
            int i = 0;
            while (i < common && i < tok.length && tok[i].equals(first[i])) i++;
            common = i;
        }
        common = Math.min(common, shortest - 1);
        if (common < 0) common = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < common; i++) {
            if (!sb.isEmpty()) sb.append('.');
            sb.append(first[i]);
        }
        return sb.toString();
    }

    /**
     * Register a resolved sibling value in the entity object under both its full relative path
     * (dots kept, array markers stripped, e.g. "text.content") and its last segment (e.g. "content"),
     * so templates can reference either form. First writer wins, preserving the intended binding
     * when two paths share a last segment (e.g. supplier.name vs supplier.contact[].name).
     */
    private void putAliases(ObjectNode fields, String sourcePath, String anchor, JsonNode value) {
        String[] spTok = sourcePath.split("\\.");
        String[] anTok = anchor.isEmpty() ? new String[0] : anchor.split("\\.");
        StringBuilder rel = new StringBuilder();
        for (int i = anTok.length; i < spTok.length; i++) {
            String seg = spTok[i];
            if (seg.endsWith("[]")) seg = seg.substring(0, seg.length() - 2);
            if (seg.isEmpty()) continue;
            if (!rel.isEmpty()) rel.append('.');
            rel.append(seg);
        }
        String full = rel.toString();
        if (full.isEmpty()) return;
        String last = full.contains(".") ? full.substring(full.lastIndexOf('.') + 1) : full;
        if (!fields.has(full)) fields.set(full, value);
        if (!fields.has(last)) fields.set(last, value);
    }

    private boolean isConstant(Rule r) {
        return r.getTransform() != null && CONSTANT_FUNCTION.equals(r.getTransform().getFunction());
    }

    private void applyConstantRule(Rule r, JsonNode target, TransformContext context) {
        Map<String, Object> args = r.getTransform().getArgs();
        boolean onlyWhenAbsent = Boolean.TRUE.equals(args.get("onlyWhenAbsent"));
        String targetPath = r.getTargetPath();
        int arrayMarker = targetPath.indexOf("[].");

        if (arrayMarker < 0) {
            if (onlyWhenAbsent && hasValue(target, targetPath)) return;
            Object constantValue = transforms.transform(r.getTransform(), null, context);
            pathResolver.set(target, targetPath, mapper.valueToTree(constantValue), null);
            return;
        }

        String arrayField = targetPath.substring(0, arrayMarker);
        String remainder = targetPath.substring(arrayMarker + 3);
        JsonNode arrayNode = target.get(arrayField);
        if (arrayNode == null || !arrayNode.isArray()) return;

        for (JsonNode element : arrayNode) {
            boolean present = element.hasNonNull(remainder);
            if (onlyWhenAbsent && present) continue;
            Object constantValue = transforms.transform(r.getTransform(), null, context);
            ((ObjectNode) element).set(remainder, mapper.valueToTree(constantValue));
        }
    }

    private boolean hasValue(JsonNode root, String dotPath) {
        JsonNode current = root;
        for (String token : dotPath.split("\\.")) {
            if (current == null || !current.isObject()) return false;
            current = current.get(token);
            if (current == null) return false;
        }
        return current != null && !current.isNull();
    }

    private Object jsonToJava(JsonNode n) {
        if (n == null || n.isNull()) return null;
        if (n.isTextual()) return n.textValue();
        if (n.isNumber()) return n.numberValue();
        if (n.isBoolean()) return n.booleanValue();
        return n;
    }

    /** A single resolved source value together with the path and array indices it came from. */
    private static final class Leaf {
        final String sourcePath;
        final JsonNode value;
        final String indexPath;

        Leaf(String sourcePath, JsonNode value, String indexPath) {
            this.sourcePath = sourcePath;
            this.value = value;
            this.indexPath = indexPath;
        }
    }
}
