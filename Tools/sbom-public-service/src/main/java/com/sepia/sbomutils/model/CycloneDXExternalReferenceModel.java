/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import org.cyclonedx.model.ExternalReference.Type;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CycloneDXExternalReferenceModel {
	
	private String url;
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private Type type;
    private String comment;
	
}