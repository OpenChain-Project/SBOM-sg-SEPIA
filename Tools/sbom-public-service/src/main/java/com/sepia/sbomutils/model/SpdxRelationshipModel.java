// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpdxRelationshipModel {
	private String spdxElementId;
	private String comment;
	private String relatedSpdxElement;
	private RelationshipType relationshipType;
	
	@Generated("jsonschema2pojo")
	public enum RelationshipType {

		VARIANT_OF("VARIANT_OF"),
		COPY_OF("COPY_OF"),
		PATCH_FOR("PATCH_FOR"),
		TEST_DEPENDENCY_OF("TEST_DEPENDENCY_OF"),
		CONTAINED_BY("CONTAINED_BY"),
		DATA_FILE_OF("DATA_FILE_OF"),
		OPTIONAL_COMPONENT_OF("OPTIONAL_COMPONENT_OF"),
		ANCESTOR_OF("ANCESTOR_OF"),
		GENERATES("GENERATES"),
		CONTAINS("CONTAINS"),
		OPTIONAL_DEPENDENCY_OF("OPTIONAL_DEPENDENCY_OF"),
		FILE_ADDED("FILE_ADDED"),
		REQUIREMENT_DESCRIPTION_FOR("REQUIREMENT_DESCRIPTION_FOR"),
		DEV_DEPENDENCY_OF("DEV_DEPENDENCY_OF"),
		DEPENDENCY_OF("DEPENDENCY_OF"),
		BUILD_DEPENDENCY_OF("BUILD_DEPENDENCY_OF"),
		DESCRIBES("DESCRIBES"),
		PREREQUISITE_FOR("PREREQUISITE_FOR"),
		HAS_PREREQUISITE("HAS_PREREQUISITE"),
		PROVIDED_DEPENDENCY_OF("PROVIDED_DEPENDENCY_OF"),
		DYNAMIC_LINK("DYNAMIC_LINK"),
		DESCRIBED_BY("DESCRIBED_BY"),
		METAFILE_OF("METAFILE_OF"),
		DEPENDENCY_MANIFEST_OF("DEPENDENCY_MANIFEST_OF"),
		PATCH_APPLIED("PATCH_APPLIED"),
		RUNTIME_DEPENDENCY_OF("RUNTIME_DEPENDENCY_OF"),
		TEST_OF("TEST_OF"),
		TEST_TOOL_OF("TEST_TOOL_OF"),
		DEPENDS_ON("DEPENDS_ON"),
		SPECIFICATION_FOR("SPECIFICATION_FOR"),
		FILE_MODIFIED("FILE_MODIFIED"),
		DISTRIBUTION_ARTIFACT("DISTRIBUTION_ARTIFACT"),
		AMENDS("AMENDS"),
		DOCUMENTATION_OF("DOCUMENTATION_OF"),
		GENERATED_FROM("GENERATED_FROM"),
		STATIC_LINK("STATIC_LINK"),
		OTHER("OTHER"),
		BUILD_TOOL_OF("BUILD_TOOL_OF"),
		TEST_CASE_OF("TEST_CASE_OF"),
		PACKAGE_OF("PACKAGE_OF"),
		DESCENDANT_OF("DESCENDANT_OF"),
		FILE_DELETED("FILE_DELETED"),
		EXPANDED_FROM_ARCHIVE("EXPANDED_FROM_ARCHIVE"),
		DEV_TOOL_OF("DEV_TOOL_OF"),
		EXAMPLE_OF("EXAMPLE_OF");
		private final String value;
		private final static Map<String, RelationshipType> CONSTANTS = new HashMap<String, RelationshipType>();
		
		static {
			for (RelationshipType c: values()) {
			CONSTANTS.put(c.value, c);
			}
			}

			RelationshipType(String value) {
			this.value = value;
			}
	}

}