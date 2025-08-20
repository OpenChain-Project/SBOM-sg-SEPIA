/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import java.util.HashMap;
import java.util.List;
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
public class SpdxSnippetModel{
	private String SPDXID;
	private List<SpdxAnnotationModel> annotations;
	private List<String> attributionTexts;
	private String comment;
	private String copyrightText;
	private String licenseComments;
	private String licenseConcluded;
	private List<String> licenseInfoInSnippets;
	private String name;
	private List<SpdxSnippetsRangesModel> ranges;
	private String snippetFromFile;

}