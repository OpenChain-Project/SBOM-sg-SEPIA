/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.sepia.sbomutils.model.BomFilesInputModel;
import com.sepia.sbomutils.model.ChangeLog;

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

 
}
