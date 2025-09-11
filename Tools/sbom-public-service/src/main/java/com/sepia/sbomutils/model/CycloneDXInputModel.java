// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;


import java.util.List;

import org.cyclonedx.model.JsonOnly;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CycloneDXInputModel {
	
    @JacksonXmlProperty(isAttribute = true)
    private int version = 1;

    @JacksonXmlProperty(isAttribute = true)
    private String serialNumber;

    @JsonOnly
    private String specVersion;

    @JsonOnly
    private String bomFormat;
    
	private CycloneDXMetaDataModel metadata;
	
	private List<CycloneDXComponentModel> components;
	
	public void setVersion(int version) {
        this.version = version;
    }
	
	public void setspecVersion(String specVersion) {
        this.specVersion = specVersion;
    }
	
	public void setBomFormat(String bomFormat) {
        this.bomFormat = bomFormat;
    }
	
}