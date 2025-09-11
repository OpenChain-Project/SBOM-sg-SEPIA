// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpdxCrossRefModel {
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonPropertyOrder({
	"isLive",
	"isValid",
	"isWayBackLink",
	"match",
	"order",
	"timestamp",
	"url"
	})
	
	@JsonProperty("isLive")
	@JsonPropertyDescription("Indicate a URL is still a live accessible location on the public internet")
	private String isLive;
	/**
	* True if the URL is a valid well formed URL
	*
	*/
	@JsonProperty("isValid")
	@JsonPropertyDescription("True if the URL is a valid well formed URL")
	private String isValid;
	/**
	* True if the License SeeAlso URL points to a Wayback archive
	*
	*/
	@JsonProperty("isWayBackLink")
	@JsonPropertyDescription("True if the License SeeAlso URL points to a Wayback archive")
	private String isWayBackLink;
	/**
	* Status of a License List SeeAlso URL reference if it refers to a website that matches the license text.
	*
	*/
	@JsonProperty("match")
	@JsonPropertyDescription("Status of a License List SeeAlso URL reference if it refers to a website that matches the license text.")
	private String match;
	/**
	* The ordinal order of this element within a list
	*
	*/
	@JsonProperty("order")
	@JsonPropertyDescription("The ordinal order of this element within a list")
	private Integer order;
	/**
	* Timestamp
	*
	*/
	@JsonProperty("timestamp")
	@JsonPropertyDescription("Timestamp")
	private String timestamp;
	/**
	* URL Reference
	*
	*/
	@JsonProperty("url")
	@JsonPropertyDescription("URL Reference")
	private String url;

	/**
	* Indicate a URL is still a live accessible location on the public internet
	*
	*/

	
}