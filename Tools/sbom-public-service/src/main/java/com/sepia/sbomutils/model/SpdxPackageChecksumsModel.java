/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import java.util.HashMap;
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
public class SpdxPackageChecksumsModel {

	
	/**
	* Identifies the algorithm used to produce the subject Checksum. Currently, SHA-1 is the only supported algorithm. It is anticipated that other algorithms will be supported at a later time.
	*
	*/
	@JsonProperty("algorithm")
	@JsonPropertyDescription("Identifies the algorithm used to produce the subject Checksum. Currently, SHA-1 is the only supported algorithm. It is anticipated that other algorithms will be supported at a later time.")
	private Algorithm algorithm;
	/**
	* The checksumValue property provides a lower case hexidecimal encoded digest value produced using a specific algorithm.
	*
	*/
	@JsonProperty("checksumValue")
	@JsonPropertyDescription("The checksumValue property provides a lower case hexidecimal encoded digest value produced using a specific algorithm.")
	private String checksumValue;

	/**
	* Identifies the algorithm used to produce the subject Checksum. Currently, SHA-1 is the only supported algorithm. It is anticipated that other algorithms will be supported at a later time.
	*
	*/
	@JsonProperty("algorithm")
	public Algorithm getAlgorithm() {
	return algorithm;
	}

	/**
	* Identifies the algorithm used to produce the subject Checksum. Currently, SHA-1 is the only supported algorithm. It is anticipated that other algorithms will be supported at a later time.
	*
	*/
	@JsonProperty("algorithm")
	public void setAlgorithm(Algorithm algorithm) {
	this.algorithm = algorithm;
	}

	/**
	* The checksumValue property provides a lower case hexidecimal encoded digest value produced using a specific algorithm.
	*
	*/
	@JsonProperty("checksumValue")
	public String getChecksumValue() {
	return checksumValue;
	}

	/**
	* The checksumValue property provides a lower case hexidecimal encoded digest value produced using a specific algorithm.
	*
	*/
	@JsonProperty("checksumValue")
	public void setChecksumValue(String checksumValue) {
	this.checksumValue = checksumValue;
	}


	/**
	* Identifies the algorithm used to produce the subject Checksum. Currently, SHA-1 is the only supported algorithm. It is anticipated that other algorithms will be supported at a later time.
	*
	*/
	@Generated("jsonschema2pojo")
	public enum Algorithm {

	SHA1("SHA1"),
	BLAKE3("BLAKE3"),
	SHA_3_384("SHA3-384"),
	SHA256("SHA256"),
	SHA384("SHA384"),
	BLAKE_2_B_512("BLAKE2b-512"),
	BLAKE_2_B_256("BLAKE2b-256"),
	SHA_3_512("SHA3-512"),
	MD2("MD2"),
	ADLER32("ADLER32"),
	MD4("MD4"),
	SHA_3_256("SHA3-256"),
	BLAKE_2_B_384("BLAKE2b-384"),
	SHA512("SHA512"),
	MD6("MD6"),
	MD5("MD5"),
	SHA224("SHA224");
	private final String value;
	private final static Map<String, Algorithm> CONSTANTS = new HashMap<String, Algorithm>();



	Algorithm(String value) {
	this.value = value;
	}

	@Override
	public String toString() {
	return this.value;
	}

	@JsonValue
	public String value() {
	return this.value;
	}

	@JsonCreator
	public static Algorithm fromValue(String value) {
	Algorithm constant = CONSTANTS.get(value);
	if (constant == null) {
	throw new IllegalArgumentException(value);
	} else {
	return constant;
	}
	}

	}

	
	

}
