/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */
// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package com.sepia.sbomutils.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.CycloneDxSchema.Version;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import com.sepia.sbomutils.model.BomFilesInputModel;
import com.sepia.sbomutils.model.ChangeLog;
import com.sepia.sbomutils.model.ErrorModel;
import com.sepia.sbomutils.service.SbomUtilityService;
import com.sepia.sbomutils.util.Constants;
import com.sepia.sbomutils.util.JsonPathFinder;
import com.sepia.sbomutils.util.SbomFileUtils;
import com.sepia.sbomutils.util.SbomMergeUtil;

@Service
public class SbomUtilityServiceImpl implements SbomUtilityService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SbomUtilityServiceImpl.class);

	@Value("${sbom.upload.path}")
	private String sbomUploadPath;
	
	@Value("${sbom.input.path}")
	private String sbomPath;
	
	@Autowired
	private SbomFileUtils sbomFileUtils;
	
	
	/**
	 * 
	 * @param outputFileVersion
	 * @return CycloneDX Version
	 * @throws IOException
	 */
	public Version getCycloneDxOutputFileVersion(String outputFileVersion) {
		LOGGER.info("Inside the Service Implementation Method - getCycloneDxOutputFileVersion()");
		Version ver = null;
		try {
			LOGGER.info("Selected CycloneDX Output File Version >> {}", outputFileVersion);
			switch (outputFileVersion) {
			case "v1_4":
				ver = Version.VERSION_14;
				break;
			case "v1_3":
				ver = Version.VERSION_13;
				break;
			case "v1_2":
				ver = Version.VERSION_12;
				break;
			case "v1_1":
				ver = Version.VERSION_11;
				break;
			case "v1_0":
				ver = Version.VERSION_10;
				break;
			default:
				ver = Version.VERSION_14;
			}
		} catch (Exception e) {
			LOGGER.error("An exception occured while getting CycloneDX File Version", e);
		}
		return ver;
	}

	/**
	 * 
	 * @param schemaParameter
	 * @return Custom Schema Version
	 */
	public VersionFlag getCustomSchemaVersion(String schemaParameter) {
		LOGGER.info("Inside the Service Implementation Method - getCustomSchemaVersion()");
		VersionFlag version = null;
		try {
			LOGGER.info("Selected Custom Schema Version >> {}", schemaParameter);
			switch (schemaParameter) {
			case "https://json-schema.org/draft/2019-09/schema#":
				version = SpecVersion.VersionFlag.V201909;
				break;

			case "http://json-schema.org/draft/2019-09/schema#":
				version = SpecVersion.VersionFlag.V201909;
				break;

			case "https://json-schema.org/draft-07/schema#":
				version = SpecVersion.VersionFlag.V7;
				break;

			case "http://json-schema.org/draft-07/schema#":
				version = SpecVersion.VersionFlag.V7;
				break;
			default: version = null;	
			}
		} catch (Exception e) {
			LOGGER.error("An exception occured while getting Custom Schema Version", e);
		}
		return version;

	}

	
	/**
	 * @param inputFileOptional
	 * @param sbomInputModel
	 * @param isFromApi
	 * @return sbomInputModel
	 */
	@Override
	public BomFilesInputModel uploadInputFile(Optional<MultipartFile[]> inputFileOptional,
			BomFilesInputModel sbomInputModel, boolean isFromApi) {
		try {

			if(isFromApi && sbomInputModel.getTimestamp() == null) {
				String timestamp = Long.toString(System.currentTimeMillis());
				sbomInputModel.setTimestamp(timestamp);
			}
			if (inputFileOptional != null && inputFileOptional.isPresent()) {
				String rootPath = sbomUploadPath + sbomInputModel.getTimestamp();
				MultipartFile[] inputFiles = inputFileOptional.get();

				File uploadDestination = getFileFromModel(sbomInputModel);

				File rootDirectory = new File(rootPath);
				if (!rootDirectory.exists()) {
					rootDirectory.mkdirs();
				}
				File[] rootPathFiles = rootDirectory.listFiles();

				if (rootPathFiles.length > 0) {
					for (File curDir : rootPathFiles) {
						if (curDir.getName().startsWith(sbomInputModel.getIndex() + Constants.UNDERSCORE)) {
							sbomFileUtils.sanitizeDirectory(curDir, sbomInputModel);
						}
					}
				}
				File uploadedFile = sbomFileUtils.saveFileInDirectory(uploadDestination, inputFiles[0],
						sbomInputModel.isSchema());
				sbomInputModel.setSbomFileName(inputFiles[0].getOriginalFilename());
				if(uploadedFile != null) {
					sbomInputModel.setFileHash(sbomFileUtils.generateFileHash(uploadedFile));	
				}
				if (isFromApi) {
					sbomInputModel = validateSboms(sbomInputModel, false, false);
					sbomInputModel.setSbomJsonString(null);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception occurred in uploadInputFile() while the given file", e);
		}
		return sbomInputModel;
	}

	/**
	 * @param sbomInputModel
	 * @return sbomInputModel
	 */
	@Override
	public BomFilesInputModel deleteSbomEntry(BomFilesInputModel sbomInputModel) {
		try {
			String sourceDirectoryName = getFilePathFromModel(sbomInputModel);
			File indexFolder = new File(sourceDirectoryName);
			FileUtils.deleteDirectory(indexFolder);
			sbomInputModel.setFileHash(sbomFileUtils.generateFileHash(sbomInputModel.getSbomJsonString()));
		} catch (Exception e) {
			LOGGER.error("Exception occurred while trying to rename the selected directory", e);
		}
		return sbomInputModel;
	}

	/**
	 * @param timestamp
	 * @return bomFilesInputList
	 */
	@Override
	public List<BomFilesInputModel> getUploadedFiles(String timestamp) {
		try {
			String sourceDirectoryName = sbomUploadPath + timestamp;
			File sourceDirectory = new File(sourceDirectoryName);
			File[] sourceDirectorySubFolders = sourceDirectory.listFiles();
			List<BomFilesInputModel> bomFilesInputList = new ArrayList<>();

			if (sourceDirectorySubFolders.length > 0) {
				for (File currentIndexDirectory : sourceDirectorySubFolders) {
					if (!currentIndexDirectory.getName().contains("_deleted")) {
						setValuesForBomFileInput(currentIndexDirectory);
					}

				}
			}
			return bomFilesInputList;
		} catch (Exception e) {
			LOGGER.error("Exception occured while retreiving the uploaded files", e);
		}
		return new ArrayList<>();
	}

	/**
	 * @param indexDirectory
	 * @return bomFilesInputModel
	 */
	private BomFilesInputModel setValuesForBomFileInput(File indexDirectory) {

		BomFilesInputModel bomFilesInputModel = new BomFilesInputModel();
		bomFilesInputModel.setIndex(Integer.parseInt(indexDirectory.getName()));

		String[] subDirNames = indexDirectory.list();
		if (subDirNames.length > 0) {
			for (String curSubDirName : subDirNames) {
				bomFilesInputModel.setSchemaType(curSubDirName);
				if (curSubDirName.equalsIgnoreCase(Constants.CUSTOM)) {
					bomFilesInputModel.setSchemaType("custom");
				}
			}
		}
		bomFilesInputModel.setSchemaType(indexDirectory.listFiles()[0].getName());

		return bomFilesInputModel;
	}

	/**
	 * @param sbomInputModel
	 * @return sbomInputModel
	 */
	private BomFilesInputModel preProcessSbomInputForValidation(BomFilesInputModel sbomInputModel) {

		try {
			File curDir = getFileFromModel(sbomInputModel);

			sbomInputModel.setFilePath(curDir.getPath());

			File[] curDirFiles = curDir.listFiles();
			if (curDirFiles.length > 0) {
				sbomInputModel = getFileDetails(sbomInputModel, curDirFiles);
			}

			return sbomInputModel;
		} catch (Exception e) {
			 LOGGER.error("Exception occured while preProcessing the Sbom file", e);
		}
		return null;
	}

	/**
	 * @param bomFilesInputModel
	 * @param curDirFiles
	 * @return bomFilesInputModel
	 */
	private BomFilesInputModel getFileDetails(BomFilesInputModel bomFilesInputModel, File[] curDirFiles) {

		String schemaJsonString = null;
		String inputJsonString = null;
		String schemaFileName = null;
		String inputFileName = null;
		try {
			bomFilesInputModel.setValidatedAlready(false);
			for (File thisFile : curDirFiles) {
				String thisFileName = thisFile.getName();
				if (thisFileName.contains("~schema")) {
					schemaJsonString = FileUtils.readFileToString(thisFile, StandardCharsets.UTF_8);
					schemaFileName = thisFileName;
				} else if (thisFileName.equalsIgnoreCase(Constants.SUCCESS_LOG_TXT)
						|| thisFileName.equalsIgnoreCase(Constants.ERROR_LOG_TXT)) {
					bomFilesInputModel.setValidatedAlready(true);
					if (thisFileName.equalsIgnoreCase(Constants.SUCCESS_LOG_TXT)) {
						bomFilesInputModel.setValid(true);
					} else {
						String errs = new String(Files.readAllBytes(thisFile.toPath()), StandardCharsets.UTF_8);
						bomFilesInputModel.setErrorDetails(sbomFileUtils.getErrorListFromString(errs));
						bomFilesInputModel.setValid(false);
					}
				} else if (thisFileName.contains(Constants.JSON_EXT) && !thisFileName.contains("~original.json")) {
					inputJsonString = FileUtils.readFileToString(thisFile, StandardCharsets.UTF_8);
					inputFileName = thisFileName;
				}
			}
			bomFilesInputModel.setSchemaJsonString(schemaJsonString);
			bomFilesInputModel.setSbomJsonString(inputJsonString);
			bomFilesInputModel.setSbomFileName(inputFileName);
			bomFilesInputModel.setSchemaFileName(schemaFileName);
		} catch (Exception e) {
			LOGGER.error("Exception occured while fetching the file details", e);
		}

		return bomFilesInputModel;
	}

	/**
	 * @param bomFilesInputModel
	 * @param isMerge
	 * @param isReplace
	 * @return bomFilesInputModel
	 */
	@Override
	public BomFilesInputModel validateSboms(BomFilesInputModel bomFilesInputModel, boolean isMerge, boolean isReplace) {

		try {
			VersionFlag versionFlag = null;
			String schemaFileName = null;
			ObjectMapper mapper = new ObjectMapper();

			if (!isMerge) {
				bomFilesInputModel = preProcessSbomInputForValidation(bomFilesInputModel);
			}

			JsonSchemaFactory schemaFactory = null;
			InputStream schemaStream = null;
			InputStream inputJsonStream = null;
			JsonNode json = null;
			JsonSchema schema = null;

			if (!bomFilesInputModel.isValidatedAlready() || isReplace) {
				String schemaType = bomFilesInputModel.getSchemaType();
				if (!StringUtils.isEmpty(schemaType)) {
					if (schemaType.equalsIgnoreCase(Constants.CYCLONEDX_LC)) {
						versionFlag = SpecVersion.VersionFlag.V7;
						schemaFileName = "/cyclonedx_1.4.schema.json";
					} else if (schemaType.equalsIgnoreCase(Constants.SPDX_LC)) {
						versionFlag = SpecVersion.VersionFlag.V201909;
						schemaFileName = "/spdx_2.3.schema.json";
					} else if (schemaType.equalsIgnoreCase(Constants.SPDX2_2_LC)) {
						versionFlag = SpecVersion.VersionFlag.V7;
						schemaFileName = "/spdx_2.2.schema.json";
					} 
					else if (schemaType.equalsIgnoreCase(Constants.CUSTOM)) {
						String schemaJsonString = bomFilesInputModel.getSchemaJsonString();
						schemaStream = new ByteArrayInputStream(schemaJsonString.getBytes(StandardCharsets.UTF_8));
						JsonNode schemaJsonNode = mapper.readTree(schemaJsonString);
						String schemaParameter = schemaJsonNode.get(Constants.DOLLAR_SCHEMA) != null
								? schemaJsonNode.get(Constants.DOLLAR_SCHEMA).toString()
								: null;
						versionFlag = schemaParameter != null
								? getCustomSchemaVersion(schemaParameter.replaceAll("^\"|\"$", ""))
								: null;
					}
				}

				if (!schemaType.equalsIgnoreCase(Constants.CUSTOM)) {
					schemaStream = getClass().getResourceAsStream(schemaFileName);
				}

				if (versionFlag != null) {
					schemaFactory = JsonSchemaFactory.getInstance(versionFlag);
					inputJsonStream = new ByteArrayInputStream(
							bomFilesInputModel.getSbomJsonString().getBytes(StandardCharsets.UTF_8));
					json = mapper.readTree(inputJsonStream);
					schema = schemaFactory.getSchema(schemaStream);

					Set<ValidationMessage> validationResult = schema.validate(json);
					bomFilesInputModel.setValid(validationResult.isEmpty());
					if (isReplace) {
						bomFilesInputModel.setErrorDetails(null);
						bomFilesInputModel.setCustomErrorDetails(null);
						clearLogFiles(bomFilesInputModel.getFilePath());
					}
					if (schemaType.equalsIgnoreCase(Constants.CYCLONEDX_LC)) {
						List<String> bomRefDupePaths = JsonPathFinder.findRepetitionPaths(json, "bom-ref");
						bomFilesInputModel.setValid(
								bomFilesInputModel.isValid() && (bomRefDupePaths == null || bomRefDupePaths.isEmpty()));
						List<ErrorModel> errList = new ArrayList<>();
						List<ErrorModel> customErrs = new ArrayList<>();
						for (String path : bomRefDupePaths) {
							errList.add(new ErrorModel(path, "Duplicate 'bom-ref' value found", null));
							if (!StringUtils.isEmpty(path)) {
								String[] pathArr = path.replace("$.", "").replaceAll("[\\[]", ".")
										.replaceAll("[\\]]", "").split("\\.");
								customErrs.add(new ErrorModel(null, "Duplicate 'bom-ref' value found", pathArr));
							}
						}
						bomFilesInputModel.setErrorDetails(errList);
						bomFilesInputModel.setCustomErrorDetails(customErrs);
					}
					List<ErrorModel> duplicateSpdxIdsList = null;
					if (schemaType.equalsIgnoreCase(Constants.SPDX_LC)) {
						duplicateSpdxIdsList = checkDuplicateSpdxId(bomFilesInputModel.getSbomJsonString());
						bomFilesInputModel.setValid(bomFilesInputModel.isValid() && duplicateSpdxIdsList.isEmpty());
					}

					if (!bomFilesInputModel.isValid()) {
						List<ErrorModel> errList = bomFilesInputModel.getErrorDetails();
						if (errList == null) {
							errList = new ArrayList<>();
						}
						errList.addAll(validationResult.stream().map(v -> v.getMessage().split(":"))
								.map(error -> new ErrorModel(error[0], error[1], null)).collect(Collectors.toList()));
						bomFilesInputModel.setErrorDetails(errList);

						if (duplicateSpdxIdsList != null && !duplicateSpdxIdsList.isEmpty()) {
							bomFilesInputModel.addErrorModel(duplicateSpdxIdsList);
							bomFilesInputModel.setCustomErrorDetails(duplicateSpdxIdsList);
						}

					}

					if (!isMerge) {
						if (!bomFilesInputModel.isValid()) {
							try (PrintStream out = new PrintStream(new FileOutputStream(
									bomFilesInputModel.getFilePath() + File.separator + Constants.ERROR_LOG_TXT))) {
								List<ErrorModel> errors = bomFilesInputModel.getErrorDetails();
								for (ErrorModel err : errors) {
									out.println(err.getErrorKey() + " : " + err.getMessage());
								}
							}
						} else {
							File successFile = new File(
									bomFilesInputModel.getFilePath() + File.separator + Constants.SUCCESS_LOG_TXT);
							successFile.createNewFile();

						}
					}
				} else {
					bomFilesInputModel.setValid(false);

					ErrorModel errorModel = new ErrorModel("SCHEMA", "The provided schema is invalid", null);
					List<ErrorModel> errorModelList = new ArrayList<>();
					errorModelList.add(errorModel);

					bomFilesInputModel.setErrorDetails(errorModelList);

					try (PrintStream out = new PrintStream(new FileOutputStream(
							bomFilesInputModel.getFilePath() + File.separator + Constants.ERROR_LOG_TXT))) {
						out.println(errorModel.getErrorKey() + " : " + errorModel.getMessage());
					}
				}
			}

			bomFilesInputModel.setFileHash(sbomFileUtils.generateFileHash(bomFilesInputModel.getSbomJsonString()));
			bomFilesInputModel.setMessage("File validated successfully");
			bomFilesInputModel.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.error("Exception occured while validation the Sbom file", e);
			bomFilesInputModel.setMessage("Error:" + e.getMessage());
			bomFilesInputModel.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return bomFilesInputModel;
	}

	/**
	 * checkDuplicateSpdxId method perform the Checking of duplicate SPDXID in the
	 * input SPDX BOM file.
	 * 
	 * @param inputJsonString
	 */
	private List<ErrorModel> checkDuplicateSpdxId(String inputJsonString) {
		LOGGER.info("Inside the Service Implementation Method - checkDuplicateSpdxId()");
		List<ErrorModel> duplicateIdSet = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();
		Set<String> packageSpdxIdSet = new java.util.HashSet<>();
		Set<String> snippetSpdxIdSet = new java.util.HashSet<>();
		Set<String> filesSpdxIdSet = new java.util.HashSet<>();
		try {
			ObjectNode bomNode = (ObjectNode) mapper.readTree(inputJsonString);
			// Packages
			ArrayNode bomSpdxPackagesNode = (ArrayNode) bomNode.get(Constants.PACKAGES);
			if (bomSpdxPackagesNode != null && !bomSpdxPackagesNode.isNull()) {
				for (JsonNode packageNode : bomSpdxPackagesNode) {
					String spdxID = packageNode.get(Constants.SPDXID) != null ? packageNode.get(Constants.SPDXID).asText() : "";
					if (!StringUtils.isEmpty(spdxID)) {
						if (!packageSpdxIdSet.contains(spdxID)) {
							packageSpdxIdSet.add(spdxID);
						} else {
							ErrorModel errorModel = new ErrorModel();
							String errorKey = "";
							List<String> objectPath = getObjectPath(bomNode, packageNode, Constants.PACKAGES);
							if (objectPath != null) {
								for (int i = 0; i < objectPath.size(); i++) {
									errorKey = errorKey + objectPath.get(i) + "->";
								}
							}
							String path = JsonPathFinder.getPath(new JSONObject(mapper.writeValueAsString(bomNode)),
									Constants.SPDXID, packageNode.toString(), true);
							errorModel.setErrorKey(path);
							errorModel.setMessage(Constants.DUPLICATE_SPDXID);
							errorModel.setPath(objectPath.toArray(new String[0]));
							duplicateIdSet.add(errorModel);

						}
					}
				}
			}

			// snippets
			ArrayNode bomSnippets = (ArrayNode) bomNode.get("snippets");
			if (bomSnippets != null && !bomSnippets.isNull()) {
				for (JsonNode snippetsNode : bomSnippets) {
					String snippetSpdxId = snippetsNode.get(Constants.SPDXID) != null ? snippetsNode.get(Constants.SPDXID).asText()
							: "";
					if (!StringUtils.isEmpty(snippetSpdxId)) {
						if (!snippetSpdxIdSet.contains(snippetSpdxId)) {
							snippetSpdxIdSet.add(snippetSpdxId);
						} else {
							ErrorModel errorModel = new ErrorModel();
							String errorKey = "";
							List<String> objectPath = getObjectPath(bomNode, snippetsNode, "snippets");
							if (objectPath != null) {
								for (int i = 0; i < objectPath.size(); i++) {
									errorKey = errorKey + objectPath.get(i) + ".";
								}
							}
							String path = JsonPathFinder.getPath(new JSONObject(mapper.writeValueAsString(bomNode)),
									Constants.SPDXID, snippetsNode.toString(), true);
							errorModel.setErrorKey(path);
							errorModel.setMessage(Constants.DUPLICATE_SPDXID);
							errorModel.setPath(objectPath.toArray(new String[0]));
							duplicateIdSet.add(errorModel);
						}
					}
				}
			}

			// Files
			ArrayNode bomFiles = (ArrayNode) bomNode.get("files");
			if (bomFiles != null && !bomFiles.isNull()) {
				for (JsonNode filesNode : bomFiles) {
					String filesSpdxID = filesNode.get(Constants.SPDXID) != null ? filesNode.get(Constants.SPDXID).asText() : "";
					if (filesSpdxID != null) {
						if (!filesSpdxIdSet.contains(filesSpdxID)) {
							filesSpdxIdSet.add(filesSpdxID);
						} else {
							ErrorModel errorModel = new ErrorModel();
							String errorKey = "";
							List<String> objectPath = getObjectPath(bomNode, filesNode, "files");
							if (objectPath != null) {
								for (int i = 0; i < objectPath.size(); i++) {
									errorKey = errorKey + objectPath.get(i) + ".";
								}
							}
							String path = JsonPathFinder.getPath(new JSONObject(mapper.writeValueAsString(bomNode)),
									Constants.SPDXID, filesNode.toString(), true);
							errorModel.setErrorKey(path);
							errorModel.setMessage(Constants.DUPLICATE_SPDXID);
							errorModel.setPath(objectPath.toArray(new String[0]));
							duplicateIdSet.add(errorModel);
						}
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception occured while checking Duplicate SPDXID details", e);
		}
		return duplicateIdSet;
	}

	/**
	 * getObjectPath() method perform the rootpath identification
	 * 
	 * @param bomNode
	 * @param packageNode
	 * @param path
	 * @return
	 */
	public List<String> getObjectPath(JsonNode bomNode, JsonNode packageNode, String path) {
		LOGGER.info("Inside the Service Implementation Method - getObjectPath()");
		List<String> objectPath = new ArrayList<String>();
		
		try {
			Iterator<Map.Entry<String, JsonNode>> fieldsIterator = bomNode.fields();
			while (fieldsIterator.hasNext()) {
				Map.Entry<String, JsonNode> field = fieldsIterator.next();
				if (field.getKey().equals(path)) {
					objectPath.add(path);
					JsonNode value = field.getValue();
					if (value.isArray()) {
						JsonNode arrayElementNode = (ArrayNode) value;
						for (int i = 0; i < arrayElementNode.size(); i++) {
							if (arrayElementNode.get(i).equals(packageNode)) {
								objectPath.add("" + i + "");
								objectPath.add(Constants.SPDXID);
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while checking Checking the ObjectPath", e);
		}
		return objectPath;
	}

	private void clearLogFiles(String filePath) throws IOException {

		File successFile = new File(filePath + File.separator + Constants.SUCCESS_LOG_TXT);
		File errorLogFile = new File(filePath + File.separator + Constants.ERROR_LOG_TXT);

		if (successFile.exists()) {
			java.nio.file.Files.delete(successFile.toPath());
		}

		if (errorLogFile.exists()) {
			java.nio.file.Files.delete(errorLogFile.toPath());
		}
	}

	/*
	 * @param bomFilesInputModel
	 * @return bomFilesInputModel
	 */
	@Override
	public BomFilesInputModel fetchErrorDetails(BomFilesInputModel bomFilesInputModel) {
		try {
			File errorFileFolder = new File(bomFilesInputModel.getFilePath());
			if (errorFileFolder.exists()) {
				File[] childFiles = errorFileFolder.listFiles();
				if (childFiles.length > 0) {
					for (File curFile : childFiles) {
						if (curFile.getName().equalsIgnoreCase(Constants.ERROR_LOG_TXT)) {
							String errs = new String(Files.readAllBytes(curFile.toPath()), StandardCharsets.UTF_8);
							bomFilesInputModel.setErrorDetails(sbomFileUtils.getErrorListFromString(errs));
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while fetching error details", e);
		}
		return bomFilesInputModel;
	}

	/*
	 * @param bomFilesInputModel
	 * @return bomFilesInputModel
	 */
	@Override
	public BomFilesInputModel fetchJsonContent(BomFilesInputModel bomFilesInputModel) {
		try {
			File indexFileFolder = new File(bomFilesInputModel.getFilePath());
			if (indexFileFolder.exists()) {
				File[] childFiles = indexFileFolder.listFiles();
				if (childFiles.length > 0) {
					for (File curFile : childFiles) {
						if (StringUtils.isEmpty(bomFilesInputModel.getSbomJsonString())
								&& !curFile.getName().equalsIgnoreCase(Constants.ERROR_LOG_TXT)
								&& !curFile.getName().equalsIgnoreCase(Constants.SUCCESS_LOG_TXT)) {
							String jsonString = new String(Files.readAllBytes(curFile.toPath()),
									StandardCharsets.UTF_8);
							bomFilesInputModel.setSbomJsonString(jsonString);
						} else if ((bomFilesInputModel.getErrorDetails() == null
								|| bomFilesInputModel.getErrorDetails().isEmpty())
								&& curFile.getName().equalsIgnoreCase(Constants.ERROR_LOG_TXT)) {
							String errorString = new String(Files.readAllBytes(curFile.toPath()),
									StandardCharsets.UTF_8);

							bomFilesInputModel.setErrorDetails(sbomFileUtils.getErrorListFromString(errorString));
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while fetching error details", e);
		}
		return bomFilesInputModel;
	}

	/*
	 * @param bomInputList
	 * @param bomMetadata
	 * @param schemaType
	 * @param isFromApp
	 * @return bomFileInput
	 */
	@Override
	public BomFilesInputModel mergeSboms(List<BomFilesInputModel> bomInputList, String bomMetadata, String schemaType, boolean isFromApp) {

		BomFilesInputModel bomFileInput = new BomFilesInputModel();
		try {
			List<ObjectNode> bomNodes = new ArrayList<>();
			ObjectMapper mapper = new ObjectMapper();

			if (bomInputList != null && !bomInputList.isEmpty()) {

				String timestamp = bomInputList.get(0).getTimestamp();

				for (BomFilesInputModel curBom : bomInputList) {
					File inputFile = new File(getFilePathFromModel(curBom) + File.separator + curBom.getSbomFileName());
					if (schemaType.equalsIgnoreCase(Constants.SPDX_LC)
							|| schemaType.equalsIgnoreCase(Constants.CUSTOM)) {
						ObjectNode bomNode = (ObjectNode) mapper
								.readTree(FileUtils.readFileToString(inputFile, StandardCharsets.UTF_8));
						bomNode.put("FileName", curBom.getSbomFileName());
						bomNodes.add(bomNode);
					}
				}

				if (schemaType.equalsIgnoreCase(Constants.SPDX_LC)) {
					bomFileInput = SbomMergeUtil.mergeSPDXBoms(bomNodes, bomMetadata, isFromApp);
				} else if (schemaType.equalsIgnoreCase(Constants.CYCLONEDX_LC)) {
					Version cycloneDxVersion = getCycloneDxOutputFileVersion("v1_4");
					String rootPath = sbomUploadPath + timestamp;
					bomFileInput = SbomMergeUtil.hierarchicalMerge(rootPath, bomInputList, bomMetadata,
							cycloneDxVersion, isFromApp);
				}

				bomFileInput.setSchemaType(bomInputList.get(0).getSchemaType());
				bomFileInput.setTimestamp(timestamp);
				bomFileInput = validateSboms(bomFileInput, true, false);
				bomFileInput.setFileHash(sbomFileUtils.generateFileHash(bomFileInput.getSbomJsonString()));

				bomFileInput.setSbomFileName("mergedSbomContent.json");

			} else {
				throw new Exception("There are no SBOMS to merge!!");
			}

		} catch (Exception e) {
			LOGGER.error("Exception occurred while merging sbom files", e);
		}
		return bomFileInput;
	}
	
	
	/*
	 * @param sbomInputModel
	 * @return sbomInputModel
	 */
	@Override
	public void clearSession(BomFilesInputModel sbomInputModel) {
		try {
			File sessionDir = new File(sbomUploadPath + sbomInputModel.getTimestamp());
			FileUtils.deleteDirectory(sessionDir);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while trying to rename the selected directory", e);
		}
	}

	/*
	 * @param sbomInputModel
	 * @return sbomInputModel
	 */
	@Override
	public BomFilesInputModel replaceFile(BomFilesInputModel sbomInputModel) {
		try {
			PrintWriter prw = new PrintWriter(
					sbomInputModel.getFilePath() + File.separator + sbomInputModel.getSbomFileName());
			prw.println(sbomInputModel.getSbomJsonString());
			prw.close();

			sbomInputModel = validateSboms(sbomInputModel, false, true);
			sbomInputModel.setFileHash(sbomFileUtils.generateFileHash(sbomInputModel.getSbomJsonString()));
		} catch (Exception e) {
			LOGGER.error("Exception occurred while replacing the file content", e);
		}
		return sbomInputModel;
	}

	/*
	 * @param currentContent
	 * @param bomFilesInputModel
	 * @param isMerge
	 * @return changeLogsList
	 */
	@Override
	public List<ChangeLog> getJsonDifferences(String currentContent, BomFilesInputModel bomFilesInputModel,
			Boolean isMerge) {

		List<ChangeLog> changeLogsList = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();
		String initialContent;
		try {
			if (!isMerge) {
				File indexFileFolder = new File(bomFilesInputModel.getFilePath());
				if (indexFileFolder.exists()) {
					String filePath = bomFilesInputModel.getSbomFileName();
					filePath = filePath.substring(0, filePath.indexOf(".json")) + "~original.json";
					File currentFile = new File(indexFileFolder, filePath);
					if (currentFile.exists()) {
						initialContent = new String(Files.readAllBytes(currentFile.toPath()), StandardCharsets.UTF_8);
					} else {
						throw new Exception("The original file not exists!!");
					}
				} else {
					throw new Exception("The file folder not exists!!");
				}
			} else {
				initialContent = bomFilesInputModel.getSbomJsonString();
			}
			JsonNode initialJson = mapper.readTree(initialContent);
			JsonNode currentJson = mapper.readTree(currentContent);
			JsonNode diffs = JsonDiff.asJson(initialJson, currentJson);

			for (JsonNode diff : diffs) {
				ChangeLog changeLog = new ChangeLog();

				String op = diff.get("op") != null ? diff.get("op").toString() : null;
				String path = diff.get("path") != null ? diff.get("path").toString() : null;
				String value = diff.get("value") != null ? diff.get("value").toString() : null;

				if(op != null) {
					if(op.startsWith("\"") && op.endsWith("\"")) {
						changeLog.setOp(op.substring(1, op.length() - 1));
					} else {
						changeLog.setOp(op);
					}
				} else {
					changeLog.setOp(null);
				}
				
				if(path != null) {
					if(path.startsWith("\"") && path.endsWith("\"")) {
						changeLog.setPath(path.substring(1, path.length() - 1));
					} else {
						changeLog.setPath(path);
					}
				} else {
					changeLog.setPath(null);
				}
				
				String pathToProcess = removeEnclosingQuotes(path);

				changeLog.setOp(removeEnclosingQuotes(op));
				changeLog.setPath(changePathFormat(pathToProcess));
				changeLog.setValue(value != null ? (value) : null);
				changeLog.setFileName(isMerge != true ? bomFilesInputModel.getSbomFileName(): "Merged File");

				changeLogsList.add(changeLog);
			}
			LOGGER.info("Difference: {}", diffs);
		} catch (Exception e) {
			LOGGER.error("Exception occured while finding the differences between given strings", e);
		}

		return changeLogsList;
	}

	private String removeEnclosingQuotes(String valToProcess) {

		if(valToProcess != null) {
			if(valToProcess.startsWith("\"") && valToProcess.endsWith("\"")) {
				return valToProcess.substring(1, valToProcess.length() - 1);
			} else {
				return valToProcess;
			}
		} else {
			return null;
		}
	}

	private String changePathFormat(String pathToProcess) {

		StringBuilder processedPath = new StringBuilder("$");

		String[] pathSplit = pathToProcess.split("/");
		for (String pathBit : pathSplit) {
			if (!StringUtils.isEmpty(pathBit)) {
				if (StringUtils.isNumeric(pathBit)) {
					pathBit = "[" + pathBit + "]";
				} else {
					pathBit = "." + pathBit;
				}
				processedPath.append(pathBit);
			}
		}
		return processedPath.toString();
	}

	/*
	 * @param currentValue
	 * @param bomFilesInputModel
	 */
	@Override
	public BomFilesInputModel prepareForDownload(String currentValue, BomFilesInputModel bomFilesInputModel)
			throws NoSuchAlgorithmException {

		List<ChangeLog> changeLogsList = getJsonDifferences(currentValue, bomFilesInputModel, true);
		//Set current json content to validate during download preparation
		String tempInitialString = bomFilesInputModel.getSbomJsonString();
		bomFilesInputModel.setSbomJsonString(currentValue);
		bomFilesInputModel = validateSboms(bomFilesInputModel, true, false);
		
		//Setting back the initial content as it's required for further change logs
		bomFilesInputModel.setSbomJsonString(tempInitialString);
		bomFilesInputModel.setChangeLogsList(changeLogsList);
		bomFilesInputModel.setFileHash(sbomFileUtils.generateFileHash(currentValue));
		return bomFilesInputModel;
	}
	
	private File getFileFromModel(BomFilesInputModel sbomInputModel) {
		return new File(getFilePathFromModel(sbomInputModel));
	}
	
	private String getFilePathFromModel(BomFilesInputModel sbomInputModel) {
		return sbomUploadPath + sbomInputModel.getTimestamp() + File.separator + sbomInputModel.getIndex()
		+ "_" + sbomInputModel.getSchemaType();
	}
	
	
	
}