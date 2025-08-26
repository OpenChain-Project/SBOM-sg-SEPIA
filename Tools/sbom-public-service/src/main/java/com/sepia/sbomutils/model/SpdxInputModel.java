// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpdxInputModel {
	@JsonProperty("SPDXID")
	private String SPDXID;
	private String spdxVersion;
	private String dataLicense;
	private String name;
	private String documentNamespace;
	private String documentDescribes;
	private SpdxCreationInfoModel creationInfo;
	private List<SpdxAnnotationModel> annotations;
	private List<SpdxExternalDocumentRefModel> externalDocumentRefs;
	private List<SpdxHasExtractedLicensingInfoModel> hasExtractedLicensingInfos;
	private List<SpdxReviewedsModel> revieweds;
	private List<SpdxPackageInfoModel> packages;
	private List<SpdxFilesModel> files;
	private List<SpdxRelationshipModel> relationships;
	private List<SpdxSnippetModel> snippets;
	
	public String getspdxVersion() {
        return spdxVersion;
    }

    public void setspdxVersion(String spdxVersion) {
        this.spdxVersion = spdxVersion;
    }
    
    public void setSPDXID(String SPDXID) {
        this.SPDXID = SPDXID;
    }
    
    public void setdataLicense(String dataLicense) {
        this.dataLicense = dataLicense;
       
    }
    
    public void setdocumentNameSpace(String documentNameSpace) {
        this.documentNamespace = documentNameSpace;
       
    }
    
    public void setname(String name) {
        this.name = name;
       
    }
    
    
    public void setcreationInfo(SpdxCreationInfoModel creationInfo) {
        this.creationInfo = creationInfo;
    }
}

