// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;

import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpdxPackageExternalRefsModel {
	private String referenceLocator;
	private String referenceType;
	private ReferenceCategory referenceCategory;
	private String comment;
	
	@Generated("jsonschema2pojo")
	public enum ReferenceCategory {

		OTHER("OTHER"),
        PERSISTENT_ID("PERSISTENT_ID"),
        SECURITY("SECURITY"),
        PACKAGE_MANAGER("PACKAGE_MANAGER");
		
		private final String name;

        public String getReferenceCategory() {
            return this.name;
        }

        ReferenceCategory(String name) {
            this.name = name;
        }
	}
}