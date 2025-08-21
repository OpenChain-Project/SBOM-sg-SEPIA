/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SpdxExternalDocumentRefModel {
	
	@JsonProperty("checksum")
	@JsonPropertyDescription("A Checksum is value that allows the contents of a file to be authenticated. Even small changes to the content of the file will change its checksum. This class allows the results of a variety of checksum and cryptographic message digest algorithms to be represented.")
	private SpdxPackageChecksumsModel checksum;
	/**
	* externalDocumentId is a string containing letters, numbers, ., - and/or + which uniquely identifies an external document within this document.
	*
	*/
	@JsonProperty("externalDocumentId")
	@JsonPropertyDescription("externalDocumentId is a string containing letters, numbers, ., - and/or + which uniquely identifies an external document within this document.")
	private String externalDocumentId;
	/**
	* SPDX ID for SpdxDocument. A property containing an SPDX document.
	*
	*/
	@JsonProperty("spdxDocument")
	@JsonPropertyDescription("SPDX ID for SpdxDocument. A property containing an SPDX document.")
	private String spdxDocument;

}