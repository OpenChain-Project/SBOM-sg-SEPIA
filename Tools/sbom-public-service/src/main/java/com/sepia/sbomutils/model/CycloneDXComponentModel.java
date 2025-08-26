// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CycloneDXComponentModel {
	
	public enum Type {
        @JsonProperty("application")
        APPLICATION("application"),
        @JsonProperty("framework")
        FRAMEWORK("framework"),
        @JsonProperty("library")
        LIBRARY("library"),
        @JsonProperty("container")
        CONTAINER("container"),
        @JsonProperty("operating-system")
        OPERATING_SYSTEM("operating-system"),
        @JsonProperty("device")
        DEVICE("device"),
        @JsonProperty("firmware")
        FIRMWARE("firmware"),
        @JsonProperty("file")
        FILE("file");

        private final String name;

        public String getTypeName() {
            return this.name;
        }

        Type(String name) {
            this.name = name;
        }
    }
	
	public enum Scope {
        @JsonProperty("required")
        REQUIRED("required"),
        @JsonProperty("optional")
        OPTIONAL("optional"),
        @JsonProperty("excluded")
        EXCLUDED("excluded");

        private final String name;

        public String getScopeName() {
            return this.name;
        }

        Scope(String name) {
            this.name = name;
        }
    }
	
	private Type type;
	@JacksonXmlProperty(isAttribute = true, localName = "bom-ref")
    @JsonProperty("bom-ref")
	@SerializedName("bom-ref")
    private String bomRef;
    @JacksonXmlProperty(isAttribute = true, localName = "mime-type")
    @JsonProperty("mime-type")
    private String mimeType;
    private String group;
    private String name;
    private String version;
    private String description;
    private List<CycloneDXHashModel> hashes;
    private List<CycloneDXLicenseChoiceModel> licenses;
    private String copyright;
    private String cpe;
    private String purl;
    private List<CycloneDXExternalReferenceModel> externalReferences;
    private CycloneDXOrganizationalEntityModel supplier;
    private List<CycloneDXPropertyModel> properties;
}