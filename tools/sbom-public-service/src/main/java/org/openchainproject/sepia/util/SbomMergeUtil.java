/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */
// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.util;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.Stack;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.Version;
import org.cyclonedx.generators.BomGeneratorFactory;
import org.cyclonedx.model.Annotation;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.BomReference;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Composition;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.ExternalReference.Type;
import org.cyclonedx.model.Hash;
import org.cyclonedx.model.Hash.Algorithm;
import org.cyclonedx.model.License;
import org.cyclonedx.model.LicenseChoice;
import org.cyclonedx.model.Metadata;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.Property;
import org.cyclonedx.model.Service;
import org.cyclonedx.model.Signature;
import org.cyclonedx.model.Tool;
import org.cyclonedx.model.attestation.Declarations;
import org.cyclonedx.model.attestation.Assessor;
import org.cyclonedx.model.attestation.Claim;
import org.cyclonedx.model.attestation.Targets;
import org.cyclonedx.model.definition.Definition;
import org.cyclonedx.model.definition.Standard;
import org.cyclonedx.model.formulation.Formula;
import org.cyclonedx.model.metadata.ToolInformation;
import org.cyclonedx.model.Pedigree;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.cyclonedx.model.vulnerability.Vulnerability.Affect;
import org.cyclonedx.parsers.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.openchainproject.sepia.model.BomFilesInputModel;
import org.openchainproject.sepia.model.ChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SbomMergeUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SbomMergeUtil.class);
	
	private SbomMergeUtil() {
        throw new AssertionError("Cannot instantiate SbomMergeUtil");
    }
	/**
	 * Hierarchical merge
	 * 
	 * @param version
	 * 
	 * @throws Exception
	 */

	public static BomFilesInputModel hierarchicalMerge(String rootPath, List<BomFilesInputModel> bomInputList, 
			String bomMetadata, Version version, boolean isFromApp) throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		Bom mergedBom = mapper.readValue(bomMetadata, Bom.class);
		List<ChangeLog> changeLogsList = new ArrayList<>();
		Set<String> bomrefIdSet = new java.util.HashSet<>();
		Map<String, String> bomrefIdMap = new HashMap<>();
		
		if (mergedBom != null) {

			String serialNumber = "urn:uuid:" + UUID.randomUUID();

			Metadata metadata = mergedBom.getMetadata();
			Component component = metadata.getComponent();
			List<Tool> toolsList = new ArrayList<>();
			List<Hash> hashesList = new ArrayList<>();

			String metaCompBomRef= getMetaCompBomRef(component);
			component.setBomRef(metaCompBomRef);
			changeLogsList = addChangeLog(changeLogsList, getMetaCompBomRef(component), Constants.MERGED_FILE, "$.metadata.component.bom-ref", Constants.ADD, Constants.CYCLONEDX_LC);
			component.setPurl("pkg:" + component.getType() + "/" + component.getGroup() + "/" + component.getName()
					+ "@" + component.getVersion());

			List<License> licenseList = new ArrayList<>();
			License license = new License();

			license.setId("CC-BY-4.0");
			licenseList.add(license);

			LicenseChoice licenseChoice = new LicenseChoice();
			licenseChoice.setLicenses(licenseList);
			
			Tool tool = new Tool();
			tool.setName("SBOM Validator");
			tool.setVendor("SEPIA");
			tool.setVersion("1.0");

			Hash hash = new Hash(Algorithm.SHA_512,
					"a9847ecbf5a9a2e08518a7a05f5f8089dc1b9d245f933d43ff3adcc7f102a6d047cb48465a3c4f6cd0da00409d083155a88499829dd2fd108aed4e75c919a207");
			hashesList.add(hash);
			
			List<ExternalReference> extRefList = new ArrayList<>();
			ExternalReference externalReference = new ExternalReference();
			externalReference.setComment(".");
			externalReference.setType(Type.DOCUMENTATION);
			externalReference.setUrl(".");
			externalReference.setHashes(hashesList);
			
			extRefList.add(externalReference);

			tool.setHashes(hashesList);
			tool.setExternalReferences(extRefList);
			toolsList.add(tool);

			metadata.setComponent(component);
			metadata.setTools(toolsList);
			metadata.setTimestamp(new Date());
			metadata.setLicenseChoice(licenseChoice);
			
			mergedBom.setMetadata(metadata);
			mergedBom.setSerialNumber(serialNumber);

		}		
		mergedBom.setComponents(new ArrayList<Component>());
		mergedBom.setServices(new ArrayList<Service>());
		mergedBom.setExternalReferences(new ArrayList<ExternalReference>());
		mergedBom.setDependencies(new ArrayList<Dependency>());
		mergedBom.setCompositions(new ArrayList<Composition>());
		mergedBom.setVulnerabilities(new ArrayList<Vulnerability>());
		// Top-level BOM signature: clear any signature from bomMetadata since the merged
		// content differs from what was originally signed. Component-level and
		// service-level signatures are preserved as-is from the input BOMs.
		mergedBom.setSignature(null);

		List<Dependency> bomSubjectDependencies = new ArrayList<>();

		for (BomFilesInputModel bomInput : bomInputList) {
			JsonParser jsonParser = new JsonParser();
			File inputFile;
			if (isFromApp) {
				    inputFile = new File(rootPath + File.separator + bomInput.getIndex() + "_cyclonedx" + File.separator + bomInput.getSbomFileName());
			}else {
				MultipartFile mf = bomInput.getSbomFile();
				if (mf != null) {
					inputFile = File.createTempFile("sbom-", ".json");
					mf.transferTo(inputFile);
				} else {
					throw new IllegalStateException("SBOM file is missing in model");
				}

			}
			
			Bom bom = jsonParser.parse(Files.readAllBytes(inputFile.toPath()));
			
			// Clean up temp file if created
			if (!isFromApp && inputFile != null && inputFile.exists()) {
				inputFile.delete();
			}

			Component metaComp = bom.getMetadata().getComponent();

			if (metaComp == null) {
				throw new Exception(bom.getSerialNumber() == null
						? "Required metadata (top level) component is missing from BOM."
						: "Required metadata (top level) component is missing from BOM " + bom.getSerialNumber() + ".");
			}

			if (metaComp.getComponents() == null) {
				metaComp.setComponents(new ArrayList<Component>());
			}
			if (bom.getComponents() != null) {
				metaComp.getComponents().addAll(bom.getComponents());
			}

			// add a namespace to existing BOM refs with dedup
			String compBasePath = "$.components[" + mergedBom.getComponents().size() + "]";
			deduplicateCompBomRefs(metaComp, changeLogsList, bomInput.getSbomFileName(), bomrefIdSet, bomrefIdMap, compBasePath, Constants.CYCLONEDX_LC);

			// make sure we have a BOM ref set and add top level dependency reference
			if (metaComp.getBomRef() == null) {
				String bomRefString = getMetaCompBomRef(metaComp);
				if(bomrefIdSet.contains(bomRefString)) {
					String newBomRefString = bomRefString + ":" + UUID.randomUUID();
					bomrefIdMap.put(bomRefString, newBomRefString);
					bomRefString = newBomRefString;
				}
				bomrefIdSet.add(bomRefString);
				metaComp.setBomRef(bomRefString);
				changeLogsList = addChangeLog(changeLogsList, bomRefString, bomInput.getSbomFileName(), compBasePath + ".bom-ref", Constants.ADD, Constants.CYCLONEDX_LC);
			} 
//			else {
//				String existingRef = metaComp.getBomRef();
//				if(bomrefIdSet.contains(existingRef)) {
//					String newRef = existingRef + ":" + UUID.randomUUID();
//					bomrefIdMap.put(existingRef, newRef);
//					metaComp.setBomRef(newRef);
//					changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newRef, existingRef), bomInput.getSbomFileName(), compBasePath + ".bom-ref", Constants.REPLACE, Constants.CYCLONEDX_LC);
//				}
//				bomrefIdSet.add(metaComp.getBomRef());
//			}
			bomSubjectDependencies.add(new Dependency(metaComp.getBomRef()));

			mergedBom.getComponents().add(metaComp);

			// services (with dedup and nested service traversal)
			if (bom.getServices() != null) {
				int serviceStartIdx = mergedBom.getServices().size();
				deduplicateServiceBomRefs(bom.getServices(), bomrefIdSet, bomrefIdMap, changeLogsList, bomInput.getSbomFileName(), serviceStartIdx, "$.services", Constants.CYCLONEDX_LC);
				mergedBom.getServices().addAll(bom.getServices());
			}

			// external references
			if (bom.getExternalReferences() != null) {
				mergedBom.getExternalReferences().addAll(bom.getExternalReferences());
			}

			// dependencies (remap refs using bomrefIdMap)
			if (bom.getDependencies() != null) {
				int depStartIdx = mergedBom.getDependencies().size();
				remapDependencyRefs(bom.getDependencies(), bomrefIdMap, changeLogsList, bomInput.getSbomFileName(), "$.dependencies", depStartIdx, false, Constants.CYCLONEDX_LC);
				mergedBom.getDependencies().addAll(bom.getDependencies());
			}

			// compositions (remap refs using bomrefIdMap)
			if (bom.getCompositions() != null) {
				int compStartIdx = mergedBom.getCompositions().size();
				remapCompositionRefs(bom.getCompositions(), bomrefIdMap, changeLogsList, bomInput.getSbomFileName(), compStartIdx, Constants.CYCLONEDX_LC);
				mergedBom.getCompositions().addAll(bom.getCompositions());
			}

			// vulnerabilities (dedup bom-ref + remap affects refs)
			if (bom.getVulnerabilities() != null) {
				int vulnStartIdx = mergedBom.getVulnerabilities().size();
				deduplicateVulnRefs(bom.getVulnerabilities(), bomrefIdSet, bomrefIdMap, changeLogsList, bomInput.getSbomFileName(), vulnStartIdx, Constants.CYCLONEDX_LC);
				mergedBom.getVulnerabilities().addAll(bom.getVulnerabilities());
			}

		}

		if (mergedBom.getMetadata().getComponent() != null) {
			Dependency dependency = new Dependency(mergedBom.getMetadata().getComponent().getBomRef());
			dependency.setDependencies(bomSubjectDependencies);
			mergedBom.getDependencies().add(dependency);

		}

		// cleanup empty top level elements
		if (mergedBom.getComponents().isEmpty())
			mergedBom.setComponents(null);
		if (mergedBom.getServices().isEmpty())
			mergedBom.setServices(null);
		if (mergedBom.getExternalReferences().isEmpty())
			mergedBom.setExternalReferences(null);
		if (mergedBom.getDependencies().isEmpty() && mergedBom.getCompositions().isEmpty())
			mergedBom.setCompositions(null);
		if (mergedBom.getVulnerabilities().isEmpty())
			mergedBom.setVulnerabilities(null);
		
		String mergedBomJsonString = BomGeneratorFactory.createJson(version, mergedBom).toJsonString();
		
		BomFilesInputModel bomFilesInputModel = new BomFilesInputModel();
		bomFilesInputModel.setSbomJsonString(mergedBomJsonString);
		bomFilesInputModel.setChangeLogsList(changeLogsList);
		
		if(!isFromApp) {
			bomFilesInputModel.setSbomJson(BomGeneratorFactory.createJson(version, mergedBom).toJsonNode());
		}
		
		return bomFilesInputModel;
	}
	
	/**
	 * CycloneDX 1.6 hierarchical merge.
	 * Applies all CycloneDX 1.4 merge logic (bom-ref dedup, dependency/composition remap,
	 * vulnerability handling, signature clearing) plus new CycloneDX 1.6 objects:
	 * annotations, formulation, declarations, definitions, and properties.
	 */
	public static BomFilesInputModel cdqhierarchicalMerge(String rootPath, List<BomFilesInputModel> bomInputList,
			String bomMetadata, Version version, boolean isFromApp) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		Bom mergedBom = mapper.readValue(bomMetadata, Bom.class);
		List<ChangeLog> changeLogsList = new ArrayList<>();
		Set<String> bomrefIdSet = new HashSet<>();
		Map<String, String> bomrefIdMap = new HashMap<>();

		if (mergedBom != null) {

			String serialNumber = "urn:uuid:" + UUID.randomUUID();

			Metadata metadata = mergedBom.getMetadata();
			Component component = metadata.getComponent();

			// CycloneDX 1.6 uses toolChoice (ToolInformation) instead of tools list
			ToolInformation tools = new ToolInformation();
			List<Component> toolsComponentsList = new ArrayList<>();
			Component toolscomponent = new Component();
			toolscomponent.setName("SBOM Validator");
			toolsComponentsList.add(toolscomponent);
			tools.setComponents(toolsComponentsList);
			metadata.setToolChoice(tools);

			// Enforce mutual exclusion: if authors[0].name is present, remove manufacturer;
			// otherwise keep manufacturer as-is.
			List<OrganizationalContact> metadataAuthors = metadata.getAuthors();
			if (metadataAuthors != null && !metadataAuthors.isEmpty()
					&& StringUtils.isNotBlank(metadataAuthors.get(0).getName())) {
				metadata.setManufacturer(null);
			}else {
				metadata.setAuthors(null);
			}

			String metaCompBomRef = getMetaCompBomRef(component);
			component.setBomRef(metaCompBomRef);
			changeLogsList = addChangeLog(changeLogsList, getMetaCompBomRef(component), Constants.MERGED_FILE,
					"$.metadata.component.bom-ref", Constants.ADD, Constants.CDQ_CYDX1_6_LC);

			metadata.setComponent(component);
			metadata.setTimestamp(new Date());
			mergedBom.setMetadata(metadata);
			mergedBom.setSerialNumber(serialNumber);
		}

		// initialize all top-level lists (1.4 + 1.6 objects)
		mergedBom.setComponents(new ArrayList<Component>());
		mergedBom.setServices(new ArrayList<Service>());
		mergedBom.setExternalReferences(new ArrayList<ExternalReference>());
		mergedBom.setDependencies(new ArrayList<Dependency>());
		mergedBom.setCompositions(new ArrayList<Composition>());
		mergedBom.setVulnerabilities(new ArrayList<Vulnerability>());
		mergedBom.setAnnotations(new ArrayList<Annotation>());
		mergedBom.setFormulation(new ArrayList<Formula>());
		mergedBom.setProperties(new ArrayList<Property>());
		// declarations: merge into first non-null or keep null
		Declarations mergedDeclarations = null;
		// definitions: merge standards lists
		Definition mergedDefinitions = null;

		// Top-level BOM signature: clear since the merged content differs from what was originally signed.
		// Component-level and service-level signatures are preserved as-is from the input BOMs.
		mergedBom.setSignature(null);

		List<Dependency> bomSubjectDependencies = new ArrayList<>();

		for (BomFilesInputModel bomInput : bomInputList) {
			JsonParser jsonParser = new JsonParser();
			File inputFile = null;
			if (isFromApp) {
			     inputFile = new File(rootPath + File.separator + bomInput.getIndex() + "_cdqcydx"
						+ File.separator + bomInput.getSbomFileName());
			}else {
				MultipartFile mf = bomInput.getSbomFile();
				if (mf != null) {
					inputFile  = File.createTempFile("sbom-", ".json");
					mf.transferTo(inputFile);
				} else {
					throw new IllegalStateException("SBOM file is missing in model");
				}

			}

			Bom bom = jsonParser.parse(Files.readAllBytes(inputFile.toPath()));
			
			// Clean up temp file if created
			if (!isFromApp && inputFile != null && inputFile.exists()) {
				inputFile.delete();
			}

			Component metaComp = bom.getMetadata().getComponent();

			if (metaComp == null) {
				throw new Exception(bom.getSerialNumber() == null
						? "Required metadata (top level) component is missing from BOM."
						: "Required metadata (top level) component is missing from BOM " + bom.getSerialNumber() + ".");
			}

			if (metaComp.getComponents() == null) {
				metaComp.setComponents(new ArrayList<Component>());
			}
			if (bom.getComponents() != null) {
				metaComp.getComponents().addAll(bom.getComponents());
			}

			// add a namespace to existing BOM refs with dedup
			String compBasePath = "$.components[" + mergedBom.getComponents().size() + "]";
			deduplicateCompBomRefs(metaComp, changeLogsList, bomInput.getSbomFileName(), bomrefIdSet, bomrefIdMap, compBasePath, Constants.CDQ_CYDX1_6_LC);

			// make sure we have a BOM ref set and add top level dependency reference
			if (metaComp.getBomRef() == null) {
				String bomRefString = getMetaCompBomRef(metaComp);
				if (bomrefIdSet.contains(bomRefString)) {
					String newBomRefString = bomRefString + ":" + UUID.randomUUID();
					bomrefIdMap.put(bomRefString, newBomRefString);
					bomRefString = newBomRefString;
				}
				bomrefIdSet.add(bomRefString);
				metaComp.setBomRef(bomRefString);
				changeLogsList = addChangeLog(changeLogsList, bomRefString, bomInput.getSbomFileName(),
						compBasePath + ".bom-ref", Constants.ADD, Constants.CDQ_CYDX1_6_LC);
			} 
//			else {
//				String existingRef = metaComp.getBomRef();
//				if (bomrefIdSet.contains(existingRef)) {
//					String newRef = existingRef + ":" + UUID.randomUUID();
//					bomrefIdMap.put(existingRef, newRef);
//					metaComp.setBomRef(newRef);
//					changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newRef, existingRef),
//							bomInput.getSbomFileName(), compBasePath + ".bom-ref", Constants.REPLACE, Constants.CDQ_CYDX1_6_LC);
//				}
//				bomrefIdSet.add(metaComp.getBomRef());
//			}
			bomSubjectDependencies.add(new Dependency(metaComp.getBomRef()));

			mergedBom.getComponents().add(metaComp);

			// services (with dedup and nested service traversal)
			if (bom.getServices() != null) {
				int serviceStartIdx = mergedBom.getServices().size();
				deduplicateServiceBomRefs(bom.getServices(), bomrefIdSet, bomrefIdMap, changeLogsList,
						bomInput.getSbomFileName(), serviceStartIdx, "$.services", Constants.CDQ_CYDX1_6_LC);
				mergedBom.getServices().addAll(bom.getServices());
			}

			// external references
			if (bom.getExternalReferences() != null) {
				mergedBom.getExternalReferences().addAll(bom.getExternalReferences());
			}

			// dependencies (remap refs using bomrefIdMap)
			if (bom.getDependencies() != null) {
				int depStartIdx = mergedBom.getDependencies().size();
				remapDependencyRefs(bom.getDependencies(), bomrefIdMap, changeLogsList,
						bomInput.getSbomFileName(), "$.dependencies", depStartIdx, false, Constants.CDQ_CYDX1_6_LC);
				mergedBom.getDependencies().addAll(bom.getDependencies());
			}

			// compositions (remap refs using bomrefIdMap)
			if (bom.getCompositions() != null) {
				int compStartIdx = mergedBom.getCompositions().size();
				remapCompositionRefs(bom.getCompositions(), bomrefIdMap, changeLogsList,
						bomInput.getSbomFileName(), compStartIdx, Constants.CDQ_CYDX1_6_LC);
				mergedBom.getCompositions().addAll(bom.getCompositions());
			}

			// vulnerabilities (dedup bom-ref + remap affects refs)
			if (bom.getVulnerabilities() != null) {
				int vulnStartIdx = mergedBom.getVulnerabilities().size();
				deduplicateVulnRefs(bom.getVulnerabilities(), bomrefIdSet, bomrefIdMap, changeLogsList,
						bomInput.getSbomFileName(), vulnStartIdx, Constants.CDQ_CYDX1_6_LC);
				mergedBom.getVulnerabilities().addAll(bom.getVulnerabilities());
			}

			// ---- CycloneDX 1.6 new objects ----

			// annotations (bom-ref dedup + subjects BomReference remap)
			if (bom.getAnnotations() != null) {
				int annotStartIdx = mergedBom.getAnnotations().size();
				deduplicateAnnotationBomRefs(bom.getAnnotations(), bomrefIdSet, bomrefIdMap, changeLogsList,
						bomInput.getSbomFileName(), annotStartIdx);
				mergedBom.getAnnotations().addAll(bom.getAnnotations());
			}

			// formulation (Formula bom-ref dedup + inner components/services dedup)
			if (bom.getFormulation() != null) {
				int formulaStartIdx = mergedBom.getFormulation().size();
				deduplicateFormulaBomRefs(bom.getFormulation(), bomrefIdSet, bomrefIdMap, changeLogsList,
						bomInput.getSbomFileName(), formulaStartIdx);
				mergedBom.getFormulation().addAll(bom.getFormulation());
			}

			// declarations (assessor, claim bom-ref dedup + targets component/service dedup)
			if (bom.getDeclarations() != null) {
				mergedDeclarations = mergeDeclarations(mergedDeclarations, bom.getDeclarations(),
						bomrefIdSet, bomrefIdMap, changeLogsList, bomInput.getSbomFileName());
			}

			// definitions (standards bom-ref dedup)
			if (bom.getDefinitions() != null) {
				mergedDefinitions = mergeDefinitions(mergedDefinitions, bom.getDefinitions(),
						bomrefIdSet, bomrefIdMap, changeLogsList, bomInput.getSbomFileName());
			}

			// properties (simple pass-through, no bom-ref)
			if (bom.getProperties() != null) {
				mergedBom.getProperties().addAll(bom.getProperties());
			}
		}

		if (mergedBom.getMetadata().getComponent() != null) {
			Dependency dependency = new Dependency(mergedBom.getMetadata().getComponent().getBomRef());
			dependency.setDependencies(bomSubjectDependencies);
			mergedBom.getDependencies().add(dependency);
		}

		// set merged declarations and definitions
		mergedBom.setDeclarations(mergedDeclarations);
		mergedBom.setDefinitions(mergedDefinitions);

		// cleanup empty top level elements
		if (mergedBom.getComponents().isEmpty())
			mergedBom.setComponents(null);
		if (mergedBom.getServices().isEmpty())
			mergedBom.setServices(null);
		if (mergedBom.getExternalReferences().isEmpty())
			mergedBom.setExternalReferences(null);
		if (mergedBom.getDependencies().isEmpty() && mergedBom.getCompositions().isEmpty())
			mergedBom.setCompositions(null);
		if (mergedBom.getVulnerabilities().isEmpty())
			mergedBom.setVulnerabilities(null);
		if (mergedBom.getAnnotations().isEmpty())
			mergedBom.setAnnotations(null);
		if (mergedBom.getFormulation().isEmpty())
			mergedBom.setFormulation(null);
		if (mergedBom.getProperties().isEmpty())
			mergedBom.setProperties(null);

		String mergedBomJsonString = BomGeneratorFactory.createJson(version, mergedBom).toJsonString();

		BomFilesInputModel bomFilesInputModel = new BomFilesInputModel();
		bomFilesInputModel.setSbomJsonString(mergedBomJsonString);
		bomFilesInputModel.setChangeLogsList(changeLogsList);

		if (!isFromApp) {
			bomFilesInputModel.setSbomJson(BomGeneratorFactory.createJson(version, mergedBom).toJsonNode());
		}
		
		return bomFilesInputModel;
	}
	
	private static String getCompBomRef(Component component, String bomRef) {
		log.info("First: {}", bomRef);
		log.info(getMetaCompBomRef(component));
		log.info("Third: {}", getCompBomRef(getMetaCompBomRef(component), bomRef));
		return bomRef == null || bomRef.isEmpty() ? null : getCompBomRef(getMetaCompBomRef(component), bomRef);
	}

	private static String getCompBomRef(String metaCompBomRef, String bomRef) {
		log.info("Fifth: {}", bomRef);
		log.info("Sixth: {}", metaCompBomRef);
		return bomRef == null || bomRef.isEmpty() ? null : bomRef;
	}

	private static String getMetaCompBomRef(Component component) {
		log.info("Fourth: {} >> {}", component.getName(), component.getBomRef());
		String bomRef;
		if(component.getBomRef() != null) {
			bomRef = component.getBomRef();
		} else {
			String nameVersion = component.getName() + "@" + component.getVersion() + "@" + SbomFileUtils.generateUuid();
			if (component.getGroup() == null) {
		        bomRef = nameVersion;
		    } else {
		        bomRef = component.getGroup() + "." + nameVersion;
		    }
		}
		
		return bomRef;
	}
	
	private static List<ChangeLog> deduplicateCompBomRefs(Component topComponent, List<ChangeLog> changeLogsList,
			String fileName, Set<String> bomrefIdSet, Map<String, String> bomrefIdMap, String basePath, String schemaType) {
		String metaCompBomRef = getMetaCompBomRef(topComponent);
		Stack<AbstractMap.SimpleEntry<Component, String>> components = new Stack<>();
		components.push(new AbstractMap.SimpleEntry<>(topComponent, basePath));

		while (!components.isEmpty()) {
			AbstractMap.SimpleEntry<Component, String> entry = components.pop();
			Component currentComponent = entry.getKey();
			String currentPath = entry.getValue();

			if (currentComponent.getComponents() != null) {
				for (int j = 0; j < currentComponent.getComponents().size(); j++) {
					components.push(new AbstractMap.SimpleEntry<>(
							currentComponent.getComponents().get(j),
							currentPath + ".components[" + j + "]"));
				}
			}

			// also traverse pedigree ancestors, descendants, variants
			Pedigree pedigree = currentComponent.getPedigree();
			if (pedigree != null) {
				if (pedigree.getAncestors() != null && pedigree.getAncestors().getComponents() != null) {
					List<Component> ancestors = pedigree.getAncestors().getComponents();
					for (int j = 0; j < ancestors.size(); j++) {
						components.push(new AbstractMap.SimpleEntry<>(
								ancestors.get(j),
								currentPath + ".pedigree.ancestors[" + j + "]"));
					}
				}
				if (pedigree.getDescendants() != null && pedigree.getDescendants().getComponents() != null) {
					List<Component> descendants = pedigree.getDescendants().getComponents();
					for (int j = 0; j < descendants.size(); j++) {
						components.push(new AbstractMap.SimpleEntry<>(
								descendants.get(j),
								currentPath + ".pedigree.descendants[" + j + "]"));
					}
				}
				if (pedigree.getVariants() != null && pedigree.getVariants().getComponents() != null) {
					List<Component> variants = pedigree.getVariants().getComponents();
					for (int j = 0; j < variants.size(); j++) {
						components.push(new AbstractMap.SimpleEntry<>(
								variants.get(j),
								currentPath + ".pedigree.variants[" + j + "]"));
					}
				}
			}

			String tempBomRef = getMetaCompBomRef(currentComponent);
			String newBomRef = null;

			if (!tempBomRef.equalsIgnoreCase(metaCompBomRef)) {
				if (!bomrefIdSet.contains(tempBomRef)) {
					bomrefIdSet.add(tempBomRef);
					currentComponent.setBomRef(tempBomRef);
					changeLogsList = addChangeLog(changeLogsList,
							tempBomRef,
							fileName, currentPath + ".bom-ref", Constants.ADD, schemaType);
				} else {
					newBomRef = tempBomRef + "@" + SbomFileUtils.generateUuid();
					bomrefIdMap.put(tempBomRef, newBomRef);
					bomrefIdSet.add(newBomRef);
					currentComponent.setBomRef(newBomRef);
					changeLogsList = addChangeLog(changeLogsList,
							(newBomRef == null ? tempBomRef : getNewValueForChangeLog(newBomRef, tempBomRef)),
							fileName, currentPath + ".bom-ref", Constants.REPLACE, schemaType);
				}
			}
		}

		return changeLogsList;
	}

	private static void deduplicateServiceBomRefs(List<Service> services,
			Set<String> bomrefIdSet, Map<String, String> bomrefIdMap,
			List<ChangeLog> changeLogsList, String fileName, int startIdx, String pathPrefix, String schemaType) {
		Stack<AbstractMap.SimpleEntry<Service, String>> pendingServices = new Stack<>();
		for (int i = 0; i < services.size(); i++) {
			pendingServices.push(new AbstractMap.SimpleEntry<>(services.get(i), pathPrefix + "[" + (startIdx + i) + "]"));
		}

		while (!pendingServices.isEmpty()) {
			AbstractMap.SimpleEntry<Service, String> entry = pendingServices.pop();
			Service service = entry.getKey();
			String servicePath = entry.getValue();

			// recurse into nested sub-services
			if (service.getServices() != null) {
				for (int j = 0; j < service.getServices().size(); j++) {
					pendingServices.push(new AbstractMap.SimpleEntry<>(
							service.getServices().get(j),
							servicePath + ".services[" + j + "]"));
				}
			}

			String serviceBomRef = service.getBomRef();
			if (serviceBomRef != null) {
				if (!bomrefIdSet.contains(serviceBomRef)) {
					bomrefIdSet.add(serviceBomRef);
				} else {
					String newServiceBomRef = serviceBomRef + "@" + UUID.randomUUID();
					bomrefIdMap.put(serviceBomRef, newServiceBomRef);
					bomrefIdSet.add(newServiceBomRef);
					service.setBomRef(newServiceBomRef);
					changeLogsList = addChangeLog(changeLogsList,
							getNewValueForChangeLog(newServiceBomRef, serviceBomRef),
							fileName, servicePath + ".bom-ref", Constants.REPLACE, schemaType);
				}
			} else {
				String newServiceBomRef = "service@" + UUID.randomUUID();
				bomrefIdSet.add(newServiceBomRef);
				service.setBomRef(newServiceBomRef);
				changeLogsList = addChangeLog(changeLogsList, newServiceBomRef, fileName, servicePath + ".bom-ref", Constants.ADD, schemaType);
			}
		}
	}

	

	private static List<ChangeLog> getDepBomRefs(String bomRefNamespace, List<Dependency> dependencies,
			List<ChangeLog> changeLogsList, String fileName, String basePath, int indexOffset, boolean isDependsOn) {
		for (int i = 0; i < dependencies.size(); i++) {
			Dependency dependency = dependencies.get(i);
			String depPath = basePath + "[" + (indexOffset + i) + "]";

			if (!isDependsOn && dependency.getDependencies() != null) {
				changeLogsList = getDepBomRefs(bomRefNamespace, dependency.getDependencies(), changeLogsList,
						fileName, depPath + ".dependsOn", 0, true);
			}
			String bomRefString = getCompBomRef(bomRefNamespace, dependency.getRef());
			String refPath = isDependsOn ? depPath : depPath + ".ref";
			changeLogsList = addChangeLog(changeLogsList, bomRefString, fileName, refPath, Constants.ADD, Constants.CYCLONEDX_LC);
		}

		return changeLogsList;
	}

	private static List<ChangeLog> deduplicateVulnRefs(List<Vulnerability> vulnerabilities,
			Set<String> bomrefIdSet, Map<String, String> bomrefIdMap,
			List<ChangeLog> changeLogsList, String fileName, int startIdx, String schemaType) {
		for (int v = 0; v < vulnerabilities.size(); v++) {
			Vulnerability vulnerability = vulnerabilities.get(v);
			String vulnPath = "$.vulnerabilities[" + (startIdx + v) + "]";
			String vulnRef = vulnerability.getBomRef();
			if (vulnRef != null && !vulnRef.isEmpty()) {
				if (!bomrefIdSet.contains(vulnRef)) {
					bomrefIdSet.add(vulnRef);
				} else {
					String newVulnRef = vulnRef + "@" + SbomFileUtils.generateUuid();
					bomrefIdMap.put(vulnRef, newVulnRef);
					bomrefIdSet.add(newVulnRef);
					vulnerability.setBomRef(newVulnRef);
					changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newVulnRef, vulnRef), fileName, vulnPath + ".bom-ref", Constants.REPLACE, schemaType);
				}
			}

			// remap affects refs using bomrefIdMap
			if (vulnerability.getAffects() != null) {
				for (int a = 0; a < vulnerability.getAffects().size(); a++) {
					Affect affect = vulnerability.getAffects().get(a);
					if (affect.getRef() != null && bomrefIdMap.containsKey(affect.getRef())) {
						String ref = affect.getRef();
						String newAffectRef = bomrefIdMap.get(ref);
						affect.setRef(newAffectRef);
						changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newAffectRef, ref), fileName, vulnPath + ".affects[" + a + "].ref", Constants.REPLACE, schemaType);
					}
				}
			}
		}
		return changeLogsList;
	}

	private static void remapDependencyRefs(List<Dependency> dependencies,
			Map<String, String> bomrefIdMap, List<ChangeLog> changeLogsList, String fileName,
			String basePath, int indexOffset, boolean isDependsOn, String schemaType) {
		for (int i = 0; i < dependencies.size(); i++) {
			Dependency dependency = dependencies.get(i);
			String depPath = basePath + "[" + (indexOffset + i) + "]";

			// recurse into sub-dependencies (serialized as dependsOn string array)
			if (!isDependsOn && dependency.getDependencies() != null) {
				remapDependencyRefs(dependency.getDependencies(), bomrefIdMap, changeLogsList, fileName,
						depPath + ".dependsOn", 0, true, schemaType);
			}

			String ref = dependency.getRef();
			if (ref != null && bomrefIdMap.containsKey(ref)) {
				String newRef = bomrefIdMap.get(ref);
				Dependency newDep = new Dependency(newRef);
				newDep.setDependencies(dependency.getDependencies());
				dependencies.set(i, newDep);
				String refPath = isDependsOn ? depPath : depPath + ".ref";
				changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newRef, ref), fileName, refPath, Constants.REPLACE, schemaType);
			}
		}
	}

	private static void remapCompositionRefs(List<Composition> compositions,
			Map<String, String> bomrefIdMap, List<ChangeLog> changeLogsList, String fileName, int startIdx, String schemaType) {
		for (int c = 0; c < compositions.size(); c++) {
			Composition composition = compositions.get(c);
			String compPath = "$.compositions[" + (startIdx + c) + "]";

			if (composition.getAssemblies() != null) {
				for (int i = 0; i < composition.getAssemblies().size(); i++) {
					String ref = composition.getAssemblies().get(i).getRef();
					if (ref != null && bomrefIdMap.containsKey(ref)) {
						String newRef = bomrefIdMap.get(ref);
						composition.getAssemblies().set(i, new BomReference(newRef));
						changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newRef, ref), fileName, compPath + ".assemblies[" + i + "]", Constants.REPLACE, schemaType);
					}
				}
			}

			if (composition.getDependencies() != null) {
				for (int i = 0; i < composition.getDependencies().size(); i++) {
					String ref = composition.getDependencies().get(i).getRef();
					if (ref != null && bomrefIdMap.containsKey(ref)) {
						String newRef = bomrefIdMap.get(ref);
						composition.getDependencies().set(i, new BomReference(newRef));
						changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newRef, ref), fileName, compPath + ".dependencies[" + i + "]", Constants.REPLACE, schemaType);
					}
				}
			}
		}
	}
	
	// ---- CycloneDX 1.6 helper methods ----

	/**
	 * Dedup annotation bom-refs and remap annotation subjects (BomReference list)
	 * using the bomrefIdMap.
	 */
	private static void deduplicateAnnotationBomRefs(List<Annotation> annotations,
			Set<String> bomrefIdSet, Map<String, String> bomrefIdMap,
			List<ChangeLog> changeLogsList, String fileName, int startIdx) {
		for (int i = 0; i < annotations.size(); i++) {
			Annotation annotation = annotations.get(i);
			String annotPath = "$.annotations[" + (startIdx + i) + "]";

			// dedup annotation bom-ref
			String bomRef = annotation.getBomRef();
			if (bomRef != null) {
				if (!bomrefIdSet.contains(bomRef)) {
					bomrefIdSet.add(bomRef);
				} else {
					String newBomRef = bomRef + "@" + UUID.randomUUID();
					bomrefIdMap.put(bomRef, newBomRef);
					bomrefIdSet.add(newBomRef);
					annotation.setBomRef(newBomRef);
					changeLogsList = addChangeLog(changeLogsList,
							getNewValueForChangeLog(newBomRef, bomRef),
							fileName, annotPath + ".bom-ref", Constants.REPLACE, Constants.CDQ_CYDX1_6_LC);
				}
			}

			// remap subjects (list of BomReference) using bomrefIdMap
			if (annotation.getSubjects() != null) {
				for (int j = 0; j < annotation.getSubjects().size(); j++) {
					String ref = annotation.getSubjects().get(j).getRef();
					if (ref != null && bomrefIdMap.containsKey(ref)) {
						String newRef = bomrefIdMap.get(ref);
						annotation.getSubjects().set(j, new BomReference(newRef));
						changeLogsList = addChangeLog(changeLogsList,
								getNewValueForChangeLog(newRef, ref),
								fileName, annotPath + ".subjects[" + j + "]", Constants.REPLACE, Constants.CDQ_CYDX1_6_LC);
					}
				}
			}
		}
	}

	/**
	 * Dedup formula bom-refs and traverse inner components/services for bom-ref dedup.
	 */
	private static void deduplicateFormulaBomRefs(List<Formula> formulas,
			Set<String> bomrefIdSet, Map<String, String> bomrefIdMap,
			List<ChangeLog> changeLogsList, String fileName, int startIdx) {
		for (int i = 0; i < formulas.size(); i++) {
			Formula formula = formulas.get(i);
			String formulaPath = "$.formulation[" + (startIdx + i) + "]";

			// dedup formula bom-ref
			String bomRef = formula.getBomRef();
			if (bomRef != null) {
				if (!bomrefIdSet.contains(bomRef)) {
					bomrefIdSet.add(bomRef);
				} else {
					String newBomRef = bomRef + "@" + UUID.randomUUID();
					bomrefIdMap.put(bomRef, newBomRef);
					bomrefIdSet.add(newBomRef);
					formula.setBomRef(newBomRef);
					changeLogsList = addChangeLog(changeLogsList,
							getNewValueForChangeLog(newBomRef, bomRef),
							fileName, formulaPath + ".bom-ref", Constants.REPLACE, Constants.CDQ_CYDX1_6_LC);
				}
			}

			// dedup inner components
			if (formula.getComponents() != null) {
				for (int j = 0; j < formula.getComponents().size(); j++) {
					deduplicateCompBomRefs(formula.getComponents().get(j), changeLogsList, fileName,
							bomrefIdSet, bomrefIdMap, formulaPath + ".components[" + j + "]", Constants.CDQ_CYDX1_6_LC);
				}
			}

			// dedup inner services
			if (formula.getServices() != null) {
				deduplicateServiceBomRefs(formula.getServices(), bomrefIdSet, bomrefIdMap, changeLogsList,
						fileName, 0, formulaPath + ".services", Constants.CDQ_CYDX1_6_LC);
			}
		}
	}

	/**
	 * Merge declarations from multiple BOMs. Assessor and claim bom-refs are deduped.
	 * Target components/services are merged with existing dedup sets.
	 */
	private static Declarations mergeDeclarations(Declarations merged, Declarations incoming,
			Set<String> bomrefIdSet, Map<String, String> bomrefIdMap,
			List<ChangeLog> changeLogsList, String fileName) {
		if (merged == null) {
			merged = new Declarations();
		}

		// assessors (bom-ref dedup)
		if (incoming.getAssessors() != null) {
			if (merged.getAssessors() == null) {
				merged.setAssessors(new ArrayList<>());
			}
			for (int i = 0; i < incoming.getAssessors().size(); i++) {
				Assessor assessor = incoming.getAssessors().get(i);
				int mergedIdx = merged.getAssessors().size();
				String assessorPath = "$.declarations.assessors[" + mergedIdx + "]";
				String bomRef = assessor.getBomRef();
				if (bomRef != null) {
					if (!bomrefIdSet.contains(bomRef)) {
						bomrefIdSet.add(bomRef);
					} else {
						String newBomRef = bomRef + "@" + UUID.randomUUID();
						bomrefIdMap.put(bomRef, newBomRef);
						bomrefIdSet.add(newBomRef);
						assessor.setBomRef(newBomRef);
						changeLogsList = addChangeLog(changeLogsList,
								getNewValueForChangeLog(newBomRef, bomRef),
								fileName, assessorPath + ".bom-ref", Constants.REPLACE, Constants.CDQ_CYDX1_6_LC);
					}
				}
				merged.getAssessors().add(assessor);
			}
		}

		// claims (bom-ref dedup + target ref remap)
		if (incoming.getClaims() != null) {
			if (merged.getClaims() == null) {
				merged.setClaims(new ArrayList<>());
			}
			for (int i = 0; i < incoming.getClaims().size(); i++) {
				Claim claim = incoming.getClaims().get(i);
				int mergedIdx = merged.getClaims().size();
				String claimPath = "$.declarations.claims[" + mergedIdx + "]";
				String bomRef = claim.getBomRef();
				if (bomRef != null) {
					if (!bomrefIdSet.contains(bomRef)) {
						bomrefIdSet.add(bomRef);
					} else {
						String newBomRef = bomRef + "@" + UUID.randomUUID();
						bomrefIdMap.put(bomRef, newBomRef);
						bomrefIdSet.add(newBomRef);
						claim.setBomRef(newBomRef);
						changeLogsList = addChangeLog(changeLogsList,
								getNewValueForChangeLog(newBomRef, bomRef),
								fileName, claimPath + ".bom-ref", Constants.REPLACE, Constants.CDQ_CYDX1_6_LC);
					}
				}
				// remap target ref if it was remapped
				if (claim.getTarget() != null && bomrefIdMap.containsKey(claim.getTarget())) {
					String oldTarget = claim.getTarget();
					String newTarget = bomrefIdMap.get(oldTarget);
					claim.setTarget(newTarget);
					changeLogsList = addChangeLog(changeLogsList,
							getNewValueForChangeLog(newTarget, oldTarget),
							fileName, claimPath + ".target", Constants.REPLACE, Constants.CDQ_CYDX1_6_LC);
				}
				merged.getClaims().add(claim);
			}
		}

		// attestations (pass-through, no bom-ref)
		if (incoming.getAttestations() != null) {
			if (merged.getAttestations() == null) {
				merged.setAttestations(new ArrayList<>());
			}
			merged.getAttestations().addAll(incoming.getAttestations());
		}

		// evidence (pass-through)
		if (incoming.getEvidence() != null) {
			if (merged.getEvidence() == null) {
				merged.setEvidence(new ArrayList<>());
			}
			merged.getEvidence().addAll(incoming.getEvidence());
		}

		// targets: merge components/services (dedup via existing sets)
		if (incoming.getTargets() != null) {
			Targets incomingTargets = incoming.getTargets();
			Targets mergedTargets = merged.getTargets();
			if (mergedTargets == null) {
				mergedTargets = new Targets();
				merged.setTargets(mergedTargets);
			}
			if (incomingTargets.getComponents() != null) {
				if (mergedTargets.getComponents() == null) {
					mergedTargets.setComponents(new ArrayList<>());
				}
				for (Component comp : incomingTargets.getComponents()) {
					int idx = mergedTargets.getComponents().size();
					deduplicateCompBomRefs(comp, changeLogsList, fileName, bomrefIdSet, bomrefIdMap,
							"$.declarations.targets.components[" + idx + "]", Constants.CDQ_CYDX1_6_LC);
					mergedTargets.getComponents().add(comp);
				}
			}
			if (incomingTargets.getServices() != null) {
				if (mergedTargets.getServices() == null) {
					mergedTargets.setServices(new ArrayList<>());
				}
				int svcStartIdx = mergedTargets.getServices().size();
				deduplicateServiceBomRefs(incomingTargets.getServices(), bomrefIdSet, bomrefIdMap,
						changeLogsList, fileName, svcStartIdx, "$.declarations.targets.services", Constants.CDQ_CYDX1_6_LC);
				mergedTargets.getServices().addAll(incomingTargets.getServices());
			}
		}

		// affirmation: take latest (last one wins)
		if (incoming.getAffirmation() != null) {
			merged.setAffirmation(incoming.getAffirmation());
		}

		// signature: clear since we're merging from multiple sources
		merged.setSignature(null);

		return merged;
	}

	/**
	 * Merge definitions (standards) from multiple BOMs with bom-ref dedup.
	 */
	private static Definition mergeDefinitions(Definition merged, Definition incoming,
			Set<String> bomrefIdSet, Map<String, String> bomrefIdMap,
			List<ChangeLog> changeLogsList, String fileName) {
		if (merged == null) {
			merged = new Definition();
		}
		if (incoming.getStandards() != null) {
			if (merged.getStandards() == null) {
				merged.setStandards(new ArrayList<>());
			}
			for (int i = 0; i < incoming.getStandards().size(); i++) {
				Standard standard = incoming.getStandards().get(i);
				int mergedIdx = merged.getStandards().size();
				String stdPath = "$.definitions.standards[" + mergedIdx + "]";
				String bomRef = standard.getBomRef();
				if (bomRef != null) {
					if (!bomrefIdSet.contains(bomRef)) {
						bomrefIdSet.add(bomRef);
					} else {
						String newBomRef = bomRef + "@" + UUID.randomUUID();
						bomrefIdMap.put(bomRef, newBomRef);
						bomrefIdSet.add(newBomRef);
						standard.setBomRef(newBomRef);
						changeLogsList = addChangeLog(changeLogsList,
								getNewValueForChangeLog(newBomRef, bomRef),
								fileName, stdPath + ".bom-ref", Constants.REPLACE, Constants.CDQ_CYDX1_6_LC);
					}
				}
				merged.getStandards().add(standard);
			}
		}
		return merged;
	}

	/**
	 * Workaround for cyclonedx-core-java library bug where TaskType.LINT enum
	 * is uppercase but the CycloneDX 1.6 schema defines "lint" (lowercase).
	 * Pre-processes JSON bytes to normalize taskTypes values before parsing.
	 */
	
	/**
	 * mergeSPDXBoms Method perform the merge operations of the Input SPDX BOM List
	 * This Method checking the SPDXID of each package and replacing with new SPDXID
	 * if there is any duplicates SPDXID in Packages, files, snippet. Info recorded
	 * in the changeLog This Method checking the SPDXID of each package and
	 * replacing with new SPDXID if there is any duplicates SPDXID in Packages,
	 * files, snippet. Info recorded in the changeLog
	 */
	public static BomFilesInputModel mergeSPDXBoms(List<ObjectNode> bomNodes, String bomMetadata, boolean isFromApp,String schemaType) throws Exception {
		LOGGER.info("Inside the Service Implementation Method - mergeSPDXBoms()");
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode mergedSpdxBomNode = mapper.createObjectNode();
		BomFilesInputModel bomFilesInputModel = new BomFilesInputModel();
		ArrayNode mergedpackagesNode = mergedSpdxBomNode.putArray(Constants.PACKAGES);
		ArrayNode mergedAnnotations = mergedSpdxBomNode.putArray(Constants.ANNOTATIONS);
		ArrayNode mergedExternalDocumentRefs = mergedSpdxBomNode.putArray(Constants.EXTERNALDOCUMENTREFS);
		ArrayNode mergedHasExtractedLicensingInfos = mergedSpdxBomNode.putArray(Constants.HAS_EXTRACTED_LICENSING_INFOS);
		ArrayNode mergedFiles = mergedSpdxBomNode.putArray(Constants.FILES);
		ArrayNode mergedSnippets = mergedSpdxBomNode.putArray(Constants.SNIPPETS);
		ArrayNode mergedRelationships = mergedSpdxBomNode.putArray(Constants.RELATIONSHIPS);

		Set<String> packageSpdxIdSet = new java.util.HashSet<>();
		Set<String> filesSpdxIdSet = new java.util.HashSet<>();
		Set<String> snippetSpdxIdSet = new java.util.HashSet<>();
		Map<String, String> spdxIdMap = new HashMap<>();
		ArrayList<String> inputSpdxIdList = new ArrayList<>();
		List<ChangeLog> changeLogsList = new ArrayList<>();

		try {
			String mergedFileSpdxID;
			String metaPackageSpdxID = null;
			mergedFileSpdxID = "SPDX-Merged_Result-" + SbomFileUtils.generateUuid() + "#SPDXRef-DOCUMENT";
			mergedSpdxBomNode.put(Constants.SPDXID, mergedFileSpdxID);
			mergedSpdxBomNode.put(Constants.SPDX_VERSION, "SPDX-2.3");
			
			if(!schemaType.equalsIgnoreCase(Constants.CDQ_SPDX2_3_LC)) {
				mergedSpdxBomNode.put(Constants.DATALICENSE, "CCBY4.0");
			} 

			mergedSpdxBomNode.put(Constants.DOCUMENT_NAMESPACE ,
					"http://spdx.org/spdxdocs/" + (mergedFileSpdxID.substring(0, mergedFileSpdxID.indexOf("#"))));
			ObjectNode bomMetadataNode = (ObjectNode) mapper.readTree(bomMetadata);
			
			ObjectNode bomMetadataCreationInfoNode = mapper.createObjectNode();
			bomMetadataCreationInfoNode = (ObjectNode) bomMetadataNode.get(Constants.CREATION_INFO);
			
			if(bomMetadataCreationInfoNode != null && !bomMetadataCreationInfoNode.isNull()) {
				bomMetadataCreationInfoNode.put(Constants.CREATED, Instant.now().toString());
				bomMetadataCreationInfoNode.put(Constants.COMMENT, "This SPDX file generated from Merge operation using SBOM Validator tool.");
				
			}
			
			mergedSpdxBomNode.put(Constants.CREATION_INFO, bomMetadataCreationInfoNode);
			


			ArrayNode bomMetaPackageNode = (ArrayNode) bomMetadataNode.get(Constants.PACKAGES);

			if (bomMetaPackageNode != null && !bomMetaPackageNode.isNull()) {
				for (JsonNode metapackageNode : bomMetaPackageNode) {
					metaPackageSpdxID = "SPDXRef-Package "
							+ (metapackageNode.get(Constants.NAME).toString()).replaceAll(Constants.SYMBOLS, "") + "-"
							+ (metapackageNode.get(Constants.VERSION_INFO).toString()).replaceAll(Constants.SYMBOLS, "");
					((ObjectNode) metapackageNode).put(Constants.SPDXID, metaPackageSpdxID);
					mergedpackagesNode.add(metapackageNode);
					mergedSpdxBomNode.put(Constants.NAME, (metapackageNode.get(Constants.NAME).toString()).replaceAll(Constants.SYMBOLS, "")
							+ "-" + (metapackageNode.get(Constants.VERSION_INFO).toString()).replaceAll(Constants.SYMBOLS, ""));
				}
			}

			// relationships between new merged result document and meta packages from user
			// input.
			ObjectNode newRelationship = mapper.createObjectNode();
			((ObjectNode) newRelationship).put(Constants.SPDX_ELEMENT_ID, mergedFileSpdxID);
			((ObjectNode) newRelationship).put(Constants.RELATIONSHIP_TYPE, Constants.DESCRIBES);
			((ObjectNode) newRelationship).put(Constants.RELATED_SPDX_ELEMENT, metaPackageSpdxID);
			mergedRelationships.add(newRelationship);
			changeLogsList = addChangeLog(changeLogsList, newRelationship.toString(), Constants.MERGED_FILE,Constants.RELATIONSHIPS , Constants.ADD, Constants.SPDX);

			for (ObjectNode bomNode : bomNodes) {

				ArrayNode bomSpdxPackagesNode = (ArrayNode) bomNode.get(Constants.PACKAGES);
				if (bomSpdxPackagesNode != null && !bomSpdxPackagesNode.isNull()) {
					for (int pi = 0; pi < bomSpdxPackagesNode.size(); pi++) {
						LOGGER.info("Inside the bomNodes" + pi);
						JsonNode packageNode = bomSpdxPackagesNode.get(pi);
						String spdxID = packageNode.get(Constants.SPDXID) != null ? packageNode.get(Constants.SPDXID).asText() : null;
						String newSpdxId;
						if (spdxID != null) {
							if (!packageSpdxIdSet.contains(spdxID)) {
								packageSpdxIdSet.add(spdxID);
								inputSpdxIdList.add(spdxID);
							} else {
								// The package is located at $.packages[pi]; build the SPDXID path directly
								// instead of re-serializing and scanning the whole bomNode (same result).
								String packagePath = "$." + Constants.PACKAGES + "[" + pi + "]." + Constants.SPDXID;
								newSpdxId = generateNewSpdxId(packageNode, packageSpdxIdSet, bomNode, Constants.REPLACE,
										changeLogsList, spdxID, packagePath);
								spdxIdMap.put(spdxID, newSpdxId);
								inputSpdxIdList.add(newSpdxId);
							}
						} else {
							String packagePath = "$." + Constants.PACKAGES + "[" + pi + "]." + Constants.SPDXID;
							newSpdxId = generateNewSpdxId(packageNode, packageSpdxIdSet, bomNode, Constants.ADD, changeLogsList,
									spdxID, packagePath);
							inputSpdxIdList.add(newSpdxId);
						}
						mergedpackagesNode.add(packageNode);
					}
				}

				// annotations
				mergeDiffSpdxObectsNode(Constants.ANNOTATIONS, mergedAnnotations, bomNode);

				// externalDocumentRefs
				mergeDiffSpdxObectsNode(Constants.EXTERNALDOCUMENTREFS, mergedExternalDocumentRefs, bomNode);

				// hasExtractedLicensingInfos
				mergeDiffSpdxObectsNode(Constants.HAS_EXTRACTED_LICENSING_INFOS, mergedHasExtractedLicensingInfos, bomNode);

				// files
				ArrayNode bomFiles = (ArrayNode) bomNode.get(Constants.FILES);
				if (bomFiles != null && !bomFiles.isNull()) {
					for (int fi = 0; fi < bomFiles.size(); fi++) {
						LOGGER.info("Inside the files" + fi);
						JsonNode filesNode = bomFiles.get(fi);
						String filesSpdxID = filesNode.get(Constants.SPDXID) != null ? filesNode.get(Constants.SPDXID).asText() : null;
						String newFilesSpdxId;
						if (filesSpdxID != null) {
							if (!filesSpdxIdSet.contains(filesSpdxID)) {
								filesSpdxIdSet.add(filesSpdxID);
							} else {
								// The file is located at $.files[fi]; build the SPDXID path directly
								// instead of re-serializing and scanning the whole bomNode (same result).
								String filePath = "$." + Constants.FILES + "[" + fi + "]." + Constants.SPDXID;
								newFilesSpdxId = generateNewSpdxId(filesNode, filesSpdxIdSet, bomNode, Constants.REPLACE,
										changeLogsList, filesSpdxID, filePath);
								spdxIdMap.put(filesSpdxID, newFilesSpdxId);
							}
						} else {
							String filePath = "$." + Constants.FILES + "[" + fi + "]." + Constants.SPDXID;
							newFilesSpdxId = generateNewSpdxId(filesNode, filesSpdxIdSet, bomNode, Constants.ADD,
									changeLogsList, filesSpdxID, filePath);
						} 
						mergedFiles.add(filesNode);
					}
				}

				// snippets
				ArrayNode bomSnippets = (ArrayNode) bomNode.get(Constants.SNIPPETS);
				if (bomSnippets != null && !bomSnippets.isNull()) {
					for (int si = 0; si < bomSnippets.size(); si++) {
						LOGGER.info("Inside the snippets" + si);
						JsonNode snippetsNode = bomSnippets.get(si);
						ArrayNode snippetsRanges = (ArrayNode) snippetsNode.get(Constants.RANGES);
						if (snippetsRanges != null && !snippetsRanges.isNull()) {
							for (int ri = 0; ri < snippetsRanges.size(); ri++) {
								JsonNode ranges = snippetsRanges.get(ri);
								ObjectNode rangesEndPointer = (ObjectNode) ranges.get(Constants.END_POINTER);
								if (rangesEndPointer != null && !rangesEndPointer.isNull()) {
									String endPointerReference = (rangesEndPointer.get(Constants.REFERENCE) != null
											? rangesEndPointer.get(Constants.REFERENCE).asText()
											: null);
									if (spdxIdMap.containsKey(endPointerReference)) {
										((ObjectNode) rangesEndPointer).put(Constants.REFERENCE,
												spdxIdMap.get(endPointerReference));
										// The reference is located at $.snippets[si].ranges[ri].endPointer.reference;
										// build the path directly instead of re-serializing and scanning the whole bomNode.
										String path = "$." + Constants.SNIPPETS + "[" + si + "]." + Constants.RANGES + "[" + ri + "]."
												+ Constants.END_POINTER + "." + Constants.REFERENCE;
										changeLogsList = addChangeLog(changeLogsList,
												spdxIdMap.get(endPointerReference), endPointerReference,
												bomNode.get(Constants.FILE_NAME).toString(), path,
												Constants.REPLACE); // "snippets->ranges->endPointer->reference"
									}
								}

								ObjectNode rangesStartPointer = (ObjectNode) ranges.get(Constants.START_POINTER);
								if (rangesStartPointer != null && !rangesStartPointer.isNull()) {
									String startPointerReference = (rangesStartPointer.get(Constants.REFERENCE) != null
											? rangesStartPointer.get(Constants.REFERENCE).asText()
											: null);
									if (spdxIdMap.containsKey(startPointerReference)) {
										((ObjectNode) rangesStartPointer).put(Constants.REFERENCE,
												spdxIdMap.get(startPointerReference));
										String path = "$." + Constants.SNIPPETS + "[" + si + "]." + Constants.RANGES + "[" + ri + "]."
												+ Constants.START_POINTER + "." + Constants.REFERENCE;
										changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(spdxIdMap.get(startPointerReference), startPointerReference), bomNode.get(Constants.FILE_NAME).toString(), path, Constants.REPLACE, Constants.SPDX); // "snippets->ranges->startPointer->reference"
									}
								}
							}
						}

						String snippetFromFile = (snippetsNode.get(Constants.SNIPPET_FROM_FILE) != null
								? snippetsNode.get(Constants.SNIPPET_FROM_FILE).asText()
								: null);
						if (spdxIdMap.containsKey(snippetFromFile)) {
							((ObjectNode) snippetsNode).put(Constants.SNIPPET_FROM_FILE, spdxIdMap.get(snippetFromFile));
							String path = "$." + Constants.SNIPPETS + "[" + si + "]." + Constants.SNIPPET_FROM_FILE;

							changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(spdxIdMap.get(snippetFromFile),
									snippetFromFile), bomNode.get(Constants.FILE_NAME).toString(), path, Constants.REPLACE, Constants.SPDX); // "snippets->snippetFromFile"
						}

						String snippetSpdxId = snippetsNode.get(Constants.SPDXID) != null ? snippetsNode.get(Constants.SPDXID).asText()
								: null;
						String newSnippetSpdxId;
						if (snippetSpdxId != null) {
							if (!snippetSpdxIdSet.contains(snippetSpdxId)) {
								snippetSpdxIdSet.add(snippetSpdxId);
							} else {
								String snippetPath = "$." + Constants.SNIPPETS + "[" + si + "]." + Constants.SPDXID;
								newSnippetSpdxId = generateNewSpdxId(snippetsNode, snippetSpdxIdSet, bomNode, Constants.REPLACE,
										changeLogsList, snippetSpdxId, snippetPath);
								spdxIdMap.put(snippetSpdxId, newSnippetSpdxId);
							}
						} else {
							String snippetPath = "$." + Constants.SNIPPETS + "[" + si + "]." + Constants.SPDXID;
							newSnippetSpdxId = generateNewSpdxId(snippetsNode, snippetSpdxIdSet, bomNode, Constants.ADD,
									changeLogsList, snippetSpdxId, snippetPath);
						}
						mergedSnippets.add(snippetsNode);
					}
				}

				// relationships

				String metaPackageSpdxIDInput = metaPackageSpdxID;
				// relationships between new merged file and each packages from Input files
				for (int i = 0; i < inputSpdxIdList.size(); i++) {
					LOGGER.info("Inside the // relationships-" + i);
					ObjectNode newRelationships = mapper.createObjectNode();
					((ObjectNode) newRelationships).put(Constants.SPDX_ELEMENT_ID, metaPackageSpdxIDInput);
					((ObjectNode) newRelationships).put(Constants.RELATIONSHIP_TYPE, Constants.CONTAINS);
					((ObjectNode) newRelationships).put(Constants.RELATED_SPDX_ELEMENT, inputSpdxIdList.get(i));
					mergedRelationships.add(newRelationships);
					changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newRelationships.toString(), null), Constants.MERGED_FILE,
							Constants.RELATIONSHIPS , Constants.ADD, Constants.SPDX);
				}
				inputSpdxIdList.removeAll(inputSpdxIdList);

				// relationships from Input Files
				ArrayNode bomRelationships = (ArrayNode) bomNode.get(Constants.RELATIONSHIPS);
				if (bomRelationships != null && !bomRelationships.isNull()) {
					// Resolve the file name once per bomNode instead of per relationship.
					String relationshipFileName = bomNode.get(Constants.FILE_NAME).toString();
					for (int r = 0; r < bomRelationships.size(); r++) {
						LOGGER.info("Inside the // relationships-" + r);
						JsonNode relationshipsNode = bomRelationships.get(r);
						String spdxElementId = (relationshipsNode.get(Constants.SPDX_ELEMENT_ID) != null
								? relationshipsNode.get(Constants.SPDX_ELEMENT_ID).asText()
								: null);
						String relatedSpdxElement = (relationshipsNode.get(Constants.RELATED_SPDX_ELEMENT) != null
								? relationshipsNode.get(Constants.RELATED_SPDX_ELEMENT).asText()
								: null);
						if (spdxIdMap.containsKey(spdxElementId)) {
							((ObjectNode) relationshipsNode).put(Constants.SPDX_ELEMENT_ID, spdxIdMap.get(spdxElementId));
							// The relationship is located at $.relationships[r]; build the path directly
							// instead of re-serializing and scanning the whole bomNode (same result).
							String path = "$." + Constants.RELATIONSHIPS + "[" + r + "]." + Constants.SPDX_ELEMENT_ID;
							changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(spdxIdMap.get(spdxElementId), spdxElementId),
									relationshipFileName, path, Constants.REPLACE, Constants.SPDX); // relationships->spdxElementId
						}
						if (spdxIdMap.containsKey(relatedSpdxElement)) {
							((ObjectNode) relationshipsNode).put(Constants.RELATED_SPDX_ELEMENT,
									spdxIdMap.get(relatedSpdxElement));
							String path = "$." + Constants.RELATIONSHIPS + "[" + r + "]." + Constants.RELATED_SPDX_ELEMENT;
							changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(spdxIdMap.get(relatedSpdxElement),
									relatedSpdxElement), relationshipFileName, path,
									Constants.REPLACE, Constants.SPDX); // "relationships->relatedSpdxElement"
						}
						mergedRelationships.add(relationshipsNode);
					}
				}
			}
			LOGGER.info("merge completed");
			String mergedBomJsonString = mapper.writeValueAsString(mergedSpdxBomNode);
			bomFilesInputModel.setSbomJsonString(mergedBomJsonString);
			if(!isFromApp) {
				bomFilesInputModel.setSbomJson(mergedSpdxBomNode);
			}
			bomFilesInputModel.setChangeLogsList(changeLogsList);
		} catch (Exception e) {
			LOGGER.error("An exception occured Inside mergeSPDXBoms()", e);
		}
		// mergedBomNode
		return bomFilesInputModel;
	}

	/**
	 * mergeDiffSpdxObectsNode method perform the merging of specified properties
	 * like annotations,externalDocumentRefs,hasExtractedLicensingInfos
	 * 
	 * @param property
	 * @param mergedBomPropertyNode
	 * @param tempBomNode
	 */
	private static void mergeDiffSpdxObectsNode(String property, ArrayNode mergedBomPropertyNode, ObjectNode tempBomNode) {
		LOGGER.info("Inside the mergeDiffSpdxObectsNode() method");
		ArrayNode bomPropertyNode = (ArrayNode) tempBomNode.get(property);
		if (bomPropertyNode != null && !bomPropertyNode.isNull()) {
			for (JsonNode propertyNode : bomPropertyNode) {
				mergedBomPropertyNode.add(propertyNode);
			}
		}
	}

	/**
	 * generateNewSpdxId generate SPDXID and add the values to SPDX Objects.
	 * Changelog will create based on the values.
	 * 
	 * @param propertyNode
	 * @param propertySpdxIdSet
	 * @param tempBomNode
	 * @param action
	 * @param changeLogsList
	 * @param SpdxID
	 */

	private static String generateNewSpdxId(JsonNode propertyNode, Set<String> propertySpdxIdSet, ObjectNode tempBomNode,
			String action, List<ChangeLog> changeLogsList, String spdxID) {
		ObjectMapper mapper = new ObjectMapper();
		String newSpdxId = "SPDXRef-Package" + SbomFileUtils.generateUuid();
		((ObjectNode) propertyNode).put(Constants.SPDXID, newSpdxId);
		propertySpdxIdSet.add(newSpdxId);
		String path = null;
		try {
			path = JsonPathFinder.getPath(new JSONObject(mapper.writeValueAsString(tempBomNode)),Constants.SPDXID , newSpdxId,
					false);
		} catch (JsonProcessingException | JSONException e) {
			LOGGER.error("An exception occured Inside generateNewSpdxId() >> {}", e);
			e.printStackTrace();
		}
		changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newSpdxId, spdxID), tempBomNode.get(Constants.FILE_NAME).toString(), path,
				action, Constants.SPDX);
		return newSpdxId;
	}

	/**
	 * Optimized variant of generateNewSpdxId for callers that already know the JSON
	 * path of the property node (e.g. the files array index). This avoids serializing
	 * and scanning the entire bomNode via JsonPathFinder.getPath, which is prohibitively
	 * expensive for very large SBOMs (e.g. hundreds of thousands of files). The generated
	 * SPDXID is unique, so the path discovered by getPath is always the caller-supplied
	 * one; behaviour is therefore identical to the path-discovering overload.
	 */
	private static String generateNewSpdxId(JsonNode propertyNode, Set<String> propertySpdxIdSet, ObjectNode tempBomNode,
			String action, List<ChangeLog> changeLogsList, String spdxID, String path) {
		String newSpdxId = "SPDXRef-Package" + SbomFileUtils.generateUuid();
		((ObjectNode) propertyNode).put(Constants.SPDXID, newSpdxId);
		propertySpdxIdSet.add(newSpdxId);
		changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(newSpdxId, spdxID), tempBomNode.get(Constants.FILE_NAME).toString(), path,
				action, Constants.SPDX);
		return newSpdxId;
	}

	private static List<ChangeLog> addChangeLog(List<ChangeLog> changeLogsList, String newValue, String fileName, 
			String path, String operation, String schemaType) {

		if ((schemaType.equalsIgnoreCase(Constants.CYCLONEDX_LC) && !StringUtils.isEmpty(newValue))
				|| (schemaType.equalsIgnoreCase(Constants.CDQ_CYDX1_6_LC) && !StringUtils.isEmpty(newValue))
				|| schemaType.equalsIgnoreCase(Constants.SPDX)
				|| schemaType.equalsIgnoreCase(Constants.CDQ_SPDX2_3_LC)) {
			ChangeLog changeLog = new ChangeLog();
			changeLog.setOp(operation);
			changeLog.setValue(newValue);
			changeLog.setFileName(fileName);
			changeLog.setPath(path);
			changeLogsList.add(changeLog);
		}

		return changeLogsList;
	}
	
	private static String getNewValueForChangeLog(String newValue, String oldValue) {
		return oldValue != null ? "New: " + newValue + " Old: " + oldValue : newValue;
	}
}
