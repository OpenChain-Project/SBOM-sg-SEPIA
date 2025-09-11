// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BomStatusModel {
	private static final long serialVersionUID = 1L;
	
	private Integer pkBomDetailsId;
	private String bomFilecontent;
	private String bomFileversion;
	private String bomFormat;
	private String bomFilename;
	private String validStatus;
	private String creationDate;
	private String displayName;
	private String schemaFilecontent;
}
