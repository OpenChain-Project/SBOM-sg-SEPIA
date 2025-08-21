/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpdxFilesModel {
	private String SPDXID;
	private List<SpdxAnnotationModel> annotations;
	private List<String> attributionTexts;
	private List<SpdxPackageChecksumsModel> checksums;
	private String comment;
	private String copyrightText;
	private List<String> fileContributors;
	private List<String> fileDependencies;
	private String fileName;
	private List<FileType> fileTypes;
	private String licenseComments;
	private String licenseConcluded;
	private List<String> licenseInfoInFiles;
	private String noticeText;

	
	public enum FileType {
		OTHER("OTHER"),
		DOCUMENTATION("DOCUMENTATION"),
		IMAGE("IMAGE"),
		VIDEO("VIDEO"),
		ARCHIVE("ARCHIVE"),
		SPDX("SPDX"),
		APPLICATION("APPLICATION"),
		SOURCE("SOURCE"),
		BINARY("BINARY"),
		TEXT("TEXT"),
		AUDIO("AUDIO");
		private final String value;
		private final static Map<String, FileType> CONSTANTS = new HashMap<String, FileType>();

		static {
		for (FileType c: values()) {
		CONSTANTS.put(c.value, c);
		}
		}

		FileType(String value) {
		this.value = value;
		}
	}
}