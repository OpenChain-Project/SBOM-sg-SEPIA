/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */
// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package com.sepia.sbomutils.util;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.BomGeneratorFactory;
import org.cyclonedx.CycloneDxSchema.Version;
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
import org.cyclonedx.model.Service;
import org.cyclonedx.model.Tool;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.cyclonedx.model.vulnerability.Vulnerability.Affect;
import org.cyclonedx.parsers.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sepia.sbomutils.model.BomFilesInputModel;
import com.sepia.sbomutils.model.ChangeLog;

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

		Set<String> bomRefSet = new HashSet<>();
		ObjectMapper mapper = new ObjectMapper();

		Bom mergedBom = mapper.readValue(bomMetadata, Bom.class);
		List<ChangeLog> changeLogsList = new ArrayList<>();

		if (mergedBom != null) {

			String serialNumber = "urn:uuid:" + UUID.randomUUID();

			Metadata metadata = mergedBom.getMetadata();
			Component component = metadata.getComponent();
			List<Tool> toolsList = new ArrayList<>();
			List<Hash> hashesList = new ArrayList<>();

			String metaCompBomRef= getMetaCompBomRef(component);
			component.setBomRef(metaCompBomRef);
			changeLogsList = addChangeLog(changeLogsList, getMetaCompBomRef(component), Constants.MERGED_FILE, null, Constants.ADD, Constants.CYCLONEDX_LC);
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

		List<Dependency> bomSubjectDependencies = new ArrayList<>();

		for (BomFilesInputModel bomInput : bomInputList) {
			JsonParser jsonParser = new JsonParser();

			File inputFile = new File(rootPath + File.separator + bomInput.getIndex() + "_cyclonedx" + File.separator + bomInput.getSbomFileName());
			Bom bom = jsonParser.parse(Files.readAllBytes(inputFile.toPath()));

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

			// add a namespace to existing BOM refs
			getCompBomRef(metaComp, changeLogsList, bomInput.getSbomFileName());

			// make sure we have a BOM ref set and add top level dependency reference
			if (metaComp.getBomRef() == null) {
				String bomRefString = getMetaCompBomRef(metaComp);
				if(bomRefSet.contains(bomRefString)) {
					bomRefString = bomRefString + ":" + UUID.randomUUID();
				}
				bomRefSet.add(bomRefString);
				metaComp.setBomRef(bomRefString);
				changeLogsList = addChangeLog(changeLogsList, bomRefString, bomInput.getSbomFileName(), null, Constants.ADD, Constants.CYCLONEDX_LC);
			}
			bomSubjectDependencies.add(new Dependency(metaComp.getBomRef()));

			mergedBom.getComponents().add(metaComp);

			// services
			if (bom.getServices() != null) {
				for (Service service : bom.getServices()) {
					service.setBomRef(getCompBomRef(bom.getMetadata().getComponent(), service.getBomRef()));
					changeLogsList = addChangeLog(changeLogsList, service.getBomRef(), bomInput.getSbomFileName(), null, Constants.ADD, Constants.CYCLONEDX_LC);
					mergedBom.getServices().add(service);
				}
			}

			// external references
			if (bom.getExternalReferences() != null) {
				mergedBom.getExternalReferences().addAll(bom.getExternalReferences());
			}

			// dependencies
			if (bom.getDependencies() != null) {
				changeLogsList = getDepBomRefs(getMetaCompBomRef(metaComp), bom.getDependencies(), changeLogsList,
						bomInput.getSbomFileName());
				mergedBom.getDependencies().addAll(bom.getDependencies());
			}

			// compositions
			if (bom.getCompositions() != null) {
				changeLogsList = getCompositions(getMetaCompBomRef(bom.getMetadata().getComponent()),
						bom.getCompositions(), changeLogsList, bomInput.getSbomFileName());
				mergedBom.getCompositions().addAll(bom.getCompositions());
			}

			// vulnerabilities
			if (bom.getVulnerabilities() != null) {
				changeLogsList = getVulnRefs(getMetaCompBomRef(mergedBom.getMetadata().getComponent()),
						bom.getVulnerabilities(), changeLogsList, bomInput.getSbomFileName());
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
		
		for(ChangeLog changeLog : bomFilesInputModel.getChangeLogsList()) {
			String path = JsonPathFinder.getPath(new JSONObject(bomFilesInputModel.getSbomJsonString()), "bom-ref", changeLog.getValue(), false);
			changeLog.setPath(path);
		}
		
		if(!isFromApp) {
			bomFilesInputModel.setSbomJson(mergedBom);
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

	private static List<ChangeLog> getCompBomRef(Component topComponent, List<ChangeLog> changeLogsList,
			String fileName) {
		return getCompBomRef(getMetaCompBomRef(topComponent), topComponent, changeLogsList, fileName);
	}

	private static List<ChangeLog> getCompBomRef(String metaCompBomRef, Component topComponent,
			List<ChangeLog> changeLogsList, String fileName) {
		// cairo@1.6.1
		Stack<Component> components = new Stack<>();
		components.push(topComponent);

		while (!components.isEmpty()) { // cairo
			Component currentComponent = components.pop();

			if (currentComponent.getComponents() != null) {
				for (Component subComponent : currentComponent.getComponents()) {
					components.push(subComponent);
				}
			}
			String tempBomRef = getMetaCompBomRef(currentComponent);

			if (!tempBomRef.equalsIgnoreCase(metaCompBomRef)) {
				currentComponent.setBomRef(getCompBomRef(metaCompBomRef, tempBomRef));
				changeLogsList = addChangeLog(changeLogsList, tempBomRef, fileName, null, Constants.ADD, Constants.CYCLONEDX_LC);
			}
		}

		return changeLogsList;
	}

	private static List<ChangeLog> getVulnRefs(String bomRefNamespace, List<Vulnerability> vulnerabilities,
			List<ChangeLog> changeLogsList, String fileName) {
		Stack<Vulnerability> pendingVulnerabilities = new Stack<>();
		pendingVulnerabilities.addAll(vulnerabilities);

		while (!pendingVulnerabilities.isEmpty()) {
			Vulnerability vulnerability = pendingVulnerabilities.pop();

			vulnerability.setBomRef(getCompBomRef(bomRefNamespace, vulnerability.getBomRef()));
			changeLogsList = addChangeLog(changeLogsList, vulnerability.getBomRef(), fileName, null, Constants.ADD, Constants.CYCLONEDX_LC);

			if (vulnerability.getAffects() != null) {
				for (Affect affect : vulnerability.getAffects()) {
					affect.setRef(bomRefNamespace);
				}
			}
		}
		return changeLogsList;
	}

	private static List<ChangeLog> getDepBomRefs(String bomRefNamespace, List<Dependency> dependencies,
			List<ChangeLog> changeLogsList, String fileName) {
		Stack<Dependency> pendingDependencies = new Stack<>();
		pendingDependencies.addAll(dependencies);

		while (!pendingDependencies.isEmpty()) {
			Dependency dependency = pendingDependencies.pop();

			if (dependency.getDependencies() != null) {
				for (Dependency subDependency : dependency.getDependencies()) {
					pendingDependencies.push(subDependency);
				}
			}
			String bomRefString = getCompBomRef(bomRefNamespace, dependency.getRef());
			changeLogsList = addChangeLog(changeLogsList, bomRefString, fileName, null, Constants.ADD, Constants.CYCLONEDX_LC);
		}

		return changeLogsList;
	}

	private static List<ChangeLog> getCompositions(String bomRefNamespace, List<Composition> compositions,
			List<ChangeLog> changeLogsList, String fileName) {
		for (Composition composition : compositions) {
			if (composition.getAssemblies() != null) {
				for (int i = 0; i < composition.getAssemblies().size(); i++) {
					String bomRefString = getCompBomRef(bomRefNamespace,
							composition.getAssemblies().get(i).toString());
					BomReference bomRef = new BomReference(
							getCompBomRef(bomRefNamespace, composition.getAssemblies().get(i).toString()));
					composition.getAssemblies().set(i, bomRef);
					changeLogsList = addChangeLog(changeLogsList, bomRefString, fileName, null, Constants.ADD, Constants.CYCLONEDX_LC);
				}
			}

			if (composition.getDependencies() != null) {
				for (int i = 0; i < composition.getDependencies().size(); i++) {
					String bomRefString = getCompBomRef(bomRefNamespace,
							composition.getDependencies().get(i).toString());
					BomReference bomRef = new BomReference(bomRefString);
					composition.getDependencies().set(i, bomRef);
					changeLogsList = addChangeLog(changeLogsList, bomRefString, fileName, null, Constants.ADD, Constants.CYCLONEDX_LC);
				}
			}
		}
		return changeLogsList;
	}
	
	/**
	 * mergeSPDXBoms Method perform the merge operations of the Input SPDX BOM List
	 * This Method checking the SPDXID of each package and replacing with new SPDXID
	 * if there is any duplicates SPDXID in Packages, files, snippet. Info recorded
	 * in the changeLog This Method checking the SPDXID of each package and
	 * replacing with new SPDXID if there is any duplicates SPDXID in Packages,
	 * files, snippet. Info recorded in the changeLog
	 */
	public static BomFilesInputModel mergeSPDXBoms(List<ObjectNode> bomNodes, String bomMetadata, boolean isFromApp) throws Exception {
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
			mergedSpdxBomNode.put(Constants.SPDX_VERSION, "2.3");
			mergedSpdxBomNode.put(Constants.DATALICENSE, "CCBY4.0");

			mergedSpdxBomNode.put(Constants.DOCUMENT_NAMESPACE ,
					"http://spdx.org/spdxdocs/" + (mergedFileSpdxID.substring(0, mergedFileSpdxID.indexOf("#"))));
			ObjectNode bomMetadataNode = (ObjectNode) mapper.readTree(bomMetadata);
			mergedSpdxBomNode.put(Constants.CREATION_INFO, bomMetadataNode.get(Constants.CREATION_INFO));

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
					for (JsonNode packageNode : bomSpdxPackagesNode) {
						String spdxID = packageNode.get(Constants.SPDXID) != null ? packageNode.get(Constants.SPDXID).asText() : null;
						String newSpdxId;
						if (spdxID != null) {
							if (!packageSpdxIdSet.contains(spdxID)) {
								packageSpdxIdSet.add(spdxID);
								inputSpdxIdList.add(spdxID);
							} else {
								newSpdxId = generateNewSpdxId(packageNode, packageSpdxIdSet, bomNode, Constants.REPLACE,
										changeLogsList, spdxID);
								spdxIdMap.put(spdxID, newSpdxId);
								inputSpdxIdList.add(newSpdxId);
							}
						} else {
							newSpdxId = generateNewSpdxId(packageNode, packageSpdxIdSet, bomNode, Constants.ADD, changeLogsList,
									spdxID);
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
					for (JsonNode filesNode : bomFiles) {
						String filesSpdxID = filesNode.get(Constants.SPDXID) != null ? filesNode.get(Constants.SPDXID).asText() : null;
						String newFilesSpdxId;
						if (filesSpdxID != null) {
							if (!filesSpdxIdSet.contains(filesSpdxID)) {
								filesSpdxIdSet.add(filesSpdxID);
							} else {
								newFilesSpdxId = generateNewSpdxId(filesNode, filesSpdxIdSet, bomNode, Constants.REPLACE,
										changeLogsList, filesSpdxID);
								spdxIdMap.put(filesSpdxID, newFilesSpdxId);
							}
						} else {
							newFilesSpdxId = generateNewSpdxId(filesNode, filesSpdxIdSet, bomNode, Constants.ADD,
									changeLogsList, filesSpdxID);
						} 
						mergedFiles.add(filesNode);
					}
				}

				// snippets
				ArrayNode bomSnippets = (ArrayNode) bomNode.get(Constants.SNIPPETS);
				if (bomSnippets != null && !bomSnippets.isNull()) {
					for (JsonNode snippetsNode : bomSnippets) {
						ArrayNode snippetsRanges = (ArrayNode) snippetsNode.get(Constants.RANGES);
						if (snippetsRanges != null && !snippetsRanges.isNull()) {
							for (JsonNode ranges : snippetsRanges) {
								ObjectNode rangesEndPointer = (ObjectNode) ranges.get(Constants.END_POINTER);
								if (rangesEndPointer != null && !rangesEndPointer.isNull()) {
									String endPointerReference = (rangesEndPointer.get(Constants.REFERENCE) != null
											? rangesEndPointer.get(Constants.REFERENCE).asText()
											: null);
									if (spdxIdMap.containsKey(endPointerReference)) {
										((ObjectNode) rangesEndPointer).put(Constants.REFERENCE,
												spdxIdMap.get(endPointerReference));
										String path = JsonPathFinder.getPath(
												new JSONObject(mapper.writeValueAsString(bomNode)), Constants.REFERENCE,
												rangesEndPointer.toString(), true);
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
										String path = JsonPathFinder.getPath(
												new JSONObject(mapper.writeValueAsString(bomNode)), Constants.REFERENCE,
												rangesStartPointer.toString(), true);
										  
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
							String path = JsonPathFinder.getPath(new JSONObject(mapper.writeValueAsString(bomNode)),
									Constants.SNIPPET_FROM_FILE, spdxIdMap.get(snippetFromFile), false);
							
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
								newSnippetSpdxId = generateNewSpdxId(snippetsNode, snippetSpdxIdSet, bomNode, Constants.REPLACE,
										changeLogsList, snippetSpdxId);
								spdxIdMap.put(snippetSpdxId, newSnippetSpdxId);
							}
						} else {
							newSnippetSpdxId = generateNewSpdxId(snippetsNode, snippetSpdxIdSet, bomNode, Constants.ADD,
									changeLogsList, snippetSpdxId);
						}
						mergedSnippets.add(snippetsNode);
					}
				}

				// relationships

				String metaPackageSpdxIDInput = metaPackageSpdxID;
				// relationships between new merged file and each packages from Input files
				for (int i = 0; i < inputSpdxIdList.size(); i++) {
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
					for (JsonNode relationshipsNode : bomRelationships) {
						String spdxElementId = (relationshipsNode.get(Constants.SPDX_ELEMENT_ID) != null
								? relationshipsNode.get(Constants.SPDX_ELEMENT_ID).asText()
								: null);
						String relatedSpdxElement = (relationshipsNode.get(Constants.RELATED_SPDX_ELEMENT) != null
								? relationshipsNode.get(Constants.RELATED_SPDX_ELEMENT).asText()
								: null);
						if (spdxIdMap.containsKey(spdxElementId)) {
							((ObjectNode) relationshipsNode).put(Constants.SPDX_ELEMENT_ID, spdxIdMap.get(spdxElementId));
							String path = JsonPathFinder.getPath(new JSONObject(mapper.writeValueAsString(bomNode)),
									Constants.SPDX_ELEMENT_ID, relationshipsNode.toString(), true);
							changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(spdxIdMap.get(spdxElementId), spdxElementId),
									bomNode.get(Constants.FILE_NAME).toString(), path, Constants.REPLACE, Constants.SPDX); // relationships->spdxElementId
						}
						if (spdxIdMap.containsKey(relatedSpdxElement)) {
							((ObjectNode) relationshipsNode).put(Constants.RELATED_SPDX_ELEMENT,
									spdxIdMap.get(relatedSpdxElement));
							String path = JsonPathFinder.getPath(new JSONObject(mapper.writeValueAsString(bomNode)),
									Constants.RELATED_SPDX_ELEMENT, relationshipsNode.toString(), true);
							changeLogsList = addChangeLog(changeLogsList, getNewValueForChangeLog(spdxIdMap.get(relatedSpdxElement),
									relatedSpdxElement), bomNode.get(Constants.FILE_NAME).toString(), path,
									Constants.REPLACE, Constants.SPDX); // "relationships->relatedSpdxElement"
						}
						mergedRelationships.add(relationshipsNode);
					}
				}
			}

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

	private static List<ChangeLog> addChangeLog(List<ChangeLog> changeLogsList, String newValue, String fileName, 
			String path, String operation, String schemaType) {

		if ((schemaType.equalsIgnoreCase(Constants.CYCLONEDX_LC) && !StringUtils.isEmpty(newValue))
				|| schemaType.equalsIgnoreCase(Constants.SPDX)) {
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
