// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;

import java.util.List;

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
public class SpdxHasExtractedLicensingInfoModel {
	@JsonProperty("comment")
	private String comment;
	/**
	* Cross Reference Detail for a license SeeAlso URL
	*
	*/
	@JsonProperty("crossRefs")
	@JsonPropertyDescription("Cross Reference Detail for a license SeeAlso URL")
	private List<SpdxCrossRefModel> crossRefs;
	/**
	* Provide a copy of the actual text of the license reference extracted from the package, file or snippet that is associated with the License Identifier to aid in future analysis.
	*
	*/
	@JsonProperty("extractedText")
	@JsonPropertyDescription("Provide a copy of the actual text of the license reference extracted from the package, file or snippet that is associated with the License Identifier to aid in future analysis.")
	private String extractedText;
	/**
	* A human readable short form license identifier for a license. The license ID is either on the standard license list or the form "LicenseRef-[idString]" where [idString] is a unique string containing letters, numbers, "." or "-". When used within a license expression, the license ID can optionally include a reference to an external document in the form "DocumentRef-[docrefIdString]:LicenseRef-[idString]" where docRefIdString is an ID for an external document reference.
	*
	*/
	@JsonProperty("licenseId")
	@JsonPropertyDescription("A human readable short form license identifier for a license. The license ID is either on the standard license list or the form \"LicenseRef-[idString]\" where [idString] is a unique string containing letters, numbers, \".\" or \"-\". When used within a license expression, the license ID can optionally include a reference to an external document in the form \"DocumentRef-[docrefIdString]:LicenseRef-[idString]\" where docRefIdString is an ID for an external document reference.")
	private String licenseId;
	/**
	* Identify name of this SpdxElement.
	*
	*/
	@JsonProperty("name")
	@JsonPropertyDescription("Identify name of this SpdxElement.")
	private String name;
	@JsonProperty("seeAlsos")
	private List<String> seeAlsos;
}