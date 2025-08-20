/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;


import org.cyclonedx.model.AttachmentText;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CycloneDXLicenseModel {
	
	 @JacksonXmlProperty(localName = "id")
	    @JsonProperty("id")
	    private String id;
	    private String name;
	    private String url;
	    
	    @JacksonXmlProperty(localName = "text")
	    @JsonProperty("text")
	    private AttachmentText attachmentText;
	   
	
}