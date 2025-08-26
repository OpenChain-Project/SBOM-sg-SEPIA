// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SpdxPackageInfoModel {
	@JsonProperty("SPDXID")
	private String SPDXID;
	private List<SpdxAnnotationModel> annotations;
	private List<String> attributionTexts;
	private String builtDate;
	private List<SpdxPackageChecksumsModel> checksums;
	private String comment;
	private String copyrightText;
	private String description;
	private String name;
	private String versionInfo;
	private String originator;
	private String supplier;
	private String downloadLocation;
	private List<SpdxPackageExternalRefsModel> externalRefs;
	private Boolean filesAnalyzed;
	private String licenseConcluded;
	private String licenseDeclared;
	private List<String> hasFiles;
	private String homepage;
	private String licenseComments;
	private List<String> licenseInfoFromFiles;
	private String packageFileName;
	private SpdxPackageVerificationCodeModel packageVerificationCode;
	private PrimaryPackagePurpose primaryPackagePurpose;
	private String releaseDate;
	private String sourceInfo;
	private String summary;
	private String validUntilDate;


	public enum PrimaryPackagePurpose {

		OTHER("OTHER"),
		INSTALL("INSTALL"),
		ARCHIVE("ARCHIVE"),
		FIRMWARE("FIRMWARE"),
		APPLICATION("APPLICATION"),
		FRAMEWORK("FRAMEWORK"),
		LIBRARY("LIBRARY"),
		CONTAINER("CONTAINER"),
		SOURCE("SOURCE"),
		DEVICE("DEVICE"),
		OPERATING_SYSTEM("OPERATING_SYSTEM"),
		FILE("FILE");
		
		private final String value;
		private final static Map<String, PrimaryPackagePurpose> CONSTANTS = new HashMap<String, PrimaryPackagePurpose>();

		static {
		for (PrimaryPackagePurpose c: values()) {
		CONSTANTS.put(c.value, c);
		}
		}

		PrimaryPackagePurpose(String value) {
		this.value = value;
		}
	}

	
}
