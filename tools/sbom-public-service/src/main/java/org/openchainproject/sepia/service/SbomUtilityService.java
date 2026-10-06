// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openchainproject.sepia.model.BomFilesInputModel;
import org.openchainproject.sepia.model.ChangeLog;
import org.springframework.web.multipart.MultipartFile;

public interface SbomUtilityService {
	
	

	/**
	 * 
	 * @param inputFile
	 * @param sbomInputModel
	 * @return true if input is uploaded successfully and false otherwise
	 */
	BomFilesInputModel uploadInputFile(Optional<MultipartFile[]> inputFile, BomFilesInputModel sbomInputModel, boolean isFromApi);

	/**
	 * 
	 * @param sbomInputModel
	 * @return 
	 */
	BomFilesInputModel deleteSbomEntry(BomFilesInputModel sbomInputModel);

	/**
	 * 
	 * @param timestamp
	 * @return list of uploaded files and corresponding details
	 */
	List<BomFilesInputModel> getUploadedFiles(String timestamp);

	/**
	 * 
	 * @param sbomInputModel
	 * @return 
	 */
	BomFilesInputModel validateSboms(BomFilesInputModel sbomInputModel, boolean isMerge, boolean isReplace);

	/**
	 * 
	 * @param bomFilesInputModel
	 * @return
	 */
	BomFilesInputModel fetchErrorDetails(BomFilesInputModel bomFilesInputModel);

	/**
	 * 
	 * @param sbomInputModel
	 * @return
	 */
	BomFilesInputModel fetchJsonContent(BomFilesInputModel sbomInputModel);

	/**
	 * 
	 * @param bomInputList
	 * @param bomMetadata 
	 * @return
	 */
	BomFilesInputModel mergeSboms(List<BomFilesInputModel> bomInputList, String bomMetadata, String schemaType, boolean isFromApp);

	/**
	 * 
	 * @param sbomInputModel
	 */
	void clearSession(BomFilesInputModel sbomInputModel);

	/**
	 * 
	 * @param sbomInputModel
	 * @return saved bom files input model
	 */
	BomFilesInputModel replaceFile(BomFilesInputModel sbomInputModel);

	/**
	 * 
	 * @param mergedValue
	 * @param initialValue
	 * @return
	 */
	List<ChangeLog> getJsonDifferences(String currentValue,BomFilesInputModel sbomInputModel,Boolean isMerge);

	/**
	 * 
	 * @param currentValue
	 * @param bomFilesInputModel
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	BomFilesInputModel prepareForDownload(String currentValue, BomFilesInputModel bomFilesInputModel) throws NoSuchAlgorithmException;

	/**
	 * Writes the response JSON to a log file in the upload directory.
	 * File is named as: &lt;uploadedFileName&gt;_log.txt
	 * @param sbomInputModel the response model to write
	 */
	void writeResponseLog(BomFilesInputModel sbomInputModel);

	/**
	 * Validates and merges the input BOM files from API request. If both files are present, they are merged.
	 * @param inputFile
	 * @param manifestFile
	 * @param sbomInputModel
	 * @return
	 */
	List<BomFilesInputModel> validateAndMergeFromAPI(Optional<MultipartFile[]> inputFile,
			String manifestContent, BomFilesInputModel sbomInputModel);

	Set<String> manifestFileValidate(String schemaType, MultipartFile manifestFile) throws Exception;

	BomFilesInputModel validateAndConvertFromAPI(Optional<MultipartFile> inputFile,
			BomFilesInputModel sbomInputModel);
 
}
