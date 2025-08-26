// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_NULL)

public class BomFilesInputModel {
	
	@ApiModelProperty(required = false)
	private String inputType;
	
	@ApiModelProperty(required = true)
	private String schemaType;
	
	@ApiModelProperty(required = true)
	private String schemaVersion;
	
	private MultipartFile sbomFile;
	
	private MultipartFile schemaFile;
	
	@ApiModelProperty(required = true)
	private boolean schema;
	
	@ApiModelProperty(required = true)
	private int index;
	
	@ApiModelProperty(required = true)
	private String timestamp;
	
	@ApiModelProperty(hidden = true)
	private String sbomJsonString;
	
	@ApiModelProperty(hidden = true)
	private String schemaJsonString;
	
	@ApiModelProperty(required = false)
	private boolean valid;
	private String filePath;
	
	@ApiModelProperty(hidden = true)
	private boolean validatedAlready;
	
	@ApiModelProperty(required = true)
	private String sbomFileName;
	
	@ApiModelProperty(required = false)
	private String schemaFileName;
	
	@ApiModelProperty(required = false)
	private List<ErrorModel> errorDetails = new ArrayList<>();
	
	@ApiModelProperty(hidden = true)
	private String dirName;
	
	@ApiModelProperty(required = false)
	private List<ChangeLog> changeLogsList;
	
	@ApiModelProperty(required = false)
	private String fileHash;
	
	@ApiModelProperty(required = false)
	private List<ErrorModel> customErrorDetails;
	
	@ApiModelProperty(required = false)
	private Object sbomJson;
	
	@ApiModelProperty(required = false)
	private Object schemaJson;
	
	public void addErrorModel(List<ErrorModel> errorModel) {
        if (this.errorDetails == null) {
            this.errorDetails = new ArrayList<>();
        }
        this.errorDetails.addAll(errorModel);
    }
	
	@ApiModelProperty(required = false)
	private String fossidServer;
	
	@ApiModelProperty(required = false)
	private String apiKey;
	
	@ApiModelProperty(required = false)
	private String scanCode;
	
	@ApiModelProperty(required = false)
	private String ntid;
	
	@ApiModelProperty(required = false)
	private String message;
	
	@ApiModelProperty(required = false)
	private Integer status;
}