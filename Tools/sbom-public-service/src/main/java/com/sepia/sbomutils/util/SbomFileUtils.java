// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package com.sepia.sbomutils.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.sepia.sbomutils.model.BomFilesInputModel;
import com.sepia.sbomutils.model.ErrorModel;

@Component
public class SbomFileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(SbomFileUtils.class);
    
	
	/**
	 * 
	 * @param regex
	 * @param text
	 * @param group
	 * @return
	 */
	public static String regexExtractor(String regex, String text, int group) {
		String matchedText = "";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			matchedText = matcher.group(group);
		}
		return matchedText;
	}
	
	/**
	 * 
	 * @param existingFiles
	 * @param skipDirName
	 * @return
	 */
	public boolean deleteObsoleteDirs(File currentFile, String directoryToSkipDeleting) {
		try {
			if (currentFile.isDirectory()) {
				if (directoryToSkipDeleting == null
						|| !currentFile.getName().equalsIgnoreCase(directoryToSkipDeleting)) {
					FileUtils.deleteDirectory(currentFile);
				}
			} else {
				FileUtils.deleteDirectory(currentFile);
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("Exception occured while deleting files >> {}", currentFile.getName(), e);
			return false;
		}
	}
	

	/**
	 * generateUuid() Method used to generate a random UUID for SPDXID
	 * 
	 * @return
	 */
	public static String generateUuid() {
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();
		return uuidAsString;
	}

	/**
	 * 
	 * @param directory
	 * @param fileToSave
	 * @param isSchemaFile
	 * @return true if the file save operation is successful
	 */
	public File saveFileInDirectory(File directory, MultipartFile fileToSave, boolean isSchemaFile) {
		File uploadedFile = null;
		try {
			if (!directory.exists()) {
				directory.mkdirs();
			}
			String fileNameToBeSaved = fileToSave.getOriginalFilename();
			String keyword = isSchemaFile ? "~schema." : "~original.";

			if(!StringUtils.isEmpty(fileNameToBeSaved)) {
				String onlyName = Files.getNameWithoutExtension(fileNameToBeSaved);
				String revisedFileNameToBeSaved = onlyName + keyword
						+ fileNameToBeSaved.substring(fileNameToBeSaved.lastIndexOf(".") + 1);
				
				File currentFile = new File(directory, revisedFileNameToBeSaved);
				FileCopyUtils.copy(fileToSave.getBytes(), new FileOutputStream(currentFile));
				
				if (!isSchemaFile) {
					uploadedFile = new File(directory, fileNameToBeSaved);
					FileCopyUtils.copy(fileToSave.getBytes(), new FileOutputStream(uploadedFile));
				}
			}
		} catch (IOException e) {
			LOGGER.error("Exception occured while saving file >> {} in the given directory >> {}", directory.getName(),
					fileToSave.getName(), e);
		}

		return uploadedFile;
	}

	public void sanitizeDirectory(File directoryToSanitize, BomFilesInputModel sbomInputModel) {
		try {
			if (directoryToSanitize.isDirectory()) {
				if (!sbomInputModel.getSchemaType().equalsIgnoreCase(Constants.CUSTOM)) {
					FileUtils.deleteDirectory(directoryToSanitize);
				} else {
					if (!directoryToSanitize.getName().equalsIgnoreCase(
							sbomInputModel.getIndex() + Constants.UNDERSCORE + sbomInputModel.getSchemaType())) {
						FileUtils.deleteDirectory(directoryToSanitize);
					} else {
						File[] filesList = directoryToSanitize.listFiles();
						if (filesList.length > 0) {
							for (File curFile : filesList) {
								if (curFile.getName().contains("~schema")) {
									if (sbomInputModel.isSchema()) {
										java.nio.file.Files.delete(curFile.toPath());
									}
								} else {
									if (!sbomInputModel.isSchema()) {
										java.nio.file.Files.delete(curFile.toPath());
									}
								}
							}
						}
					}
				}
			} else {
				java.nio.file.Files.delete(directoryToSanitize.toPath());
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while sanitizing directory before upload >> ", e);
		}

	}

	public List<ErrorModel> getErrorListFromString(String errorString) {
		List<ErrorModel> errorList = new ArrayList<>();
		for (String error : errorString.split("\r\n")) {
			String[] keyAndValueSplit = error.split(":");
			if (keyAndValueSplit.length > 1) {
				ErrorModel errorModel = new ErrorModel();
				errorModel.setErrorKey(keyAndValueSplit[0]);
				errorModel.setMessage(keyAndValueSplit[1]);
				errorList.add(errorModel);
			}
		}
		return errorList;
	}

	public String generateFileHash(String stringToHash) throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("SHA-512");

		byte[] bytes = md.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public String generateFileHash(File fileToHash) throws IOException {

		return Files.hash(fileToHash, Hashing.sha512()).toString();
	}
}
