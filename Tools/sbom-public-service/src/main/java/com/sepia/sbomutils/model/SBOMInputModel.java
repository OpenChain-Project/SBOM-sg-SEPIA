/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SBOMInputModel {

	@SerializedName("apiKey")
	@Expose
	private String apiKey;

	@SerializedName("scanCode")
	@Expose
	private String scanCode;

	@SerializedName("reqType")
	@Expose
	private String reqType;

	@SerializedName("selVersion")
	@Expose
	private Object selVersion;

	@SerializedName("servers")
	@Expose
	private String servers;

	@SerializedName("workon")
	@Expose
	private String workon;

	@SerializedName("bomfile")
	@Expose
	private String bomfile;

	@SerializedName("schemafile")
	@Expose
	private String schemafile;
	
	@SerializedName("cmpList")
	@Expose
	private Integer[] cmpList;
	
}
