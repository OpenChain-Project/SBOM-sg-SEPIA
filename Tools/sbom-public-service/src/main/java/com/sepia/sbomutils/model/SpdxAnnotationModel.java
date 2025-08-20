/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpdxAnnotationModel {
	
	public enum AnnotationType {

		OTHER("OTHER"),
		REVIEW("REVIEW");
		
		private final String name;

        public String getAnnotationTypeName() {
            return this.name;
        }

        AnnotationType(String name) {
            this.name = name;
        }
	}
	@JsonProperty("annotationDate")
	@JsonPropertyDescription("Identify when the comment was made. This is to be specified according to the combined date and time in the UTC format, as specified in the ISO 8601 standard.")
	private String annotationDate;
	/**
	* Type of the annotation.
	*
	*/
	@JsonProperty("annotationType")
	@JsonPropertyDescription("Type of the annotation.")
	private AnnotationType annotationType;
	/**
	* This field identifies the person, organization, or tool that has commented on a file, package, snippet, or the entire document.
	*
	*/
	@JsonProperty("annotator")
	@JsonPropertyDescription("This field identifies the person, organization, or tool that has commented on a file, package, snippet, or the entire document.")
	private String annotator;
	@JsonProperty("comment")
	private String comment;

	
}