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
public class ComponentStatusModel {

	private static final long serialVersionUID = 1L;

	private Integer pkAllowlistMasterId;
	
	private Integer componentId;
	
	private Integer woId;

	private String componentName;

	private String version;

	private String division;
	
	private String requestorName;

	private String requestedDate;

	private String eSignature;

	private String license;

	private String status;
	
	private String os;

}
