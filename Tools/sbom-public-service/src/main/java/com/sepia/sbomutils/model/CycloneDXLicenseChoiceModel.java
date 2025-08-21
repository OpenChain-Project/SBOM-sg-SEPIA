/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CycloneDXLicenseChoiceModel {
	
	private CycloneDXLicenseModel license;
    private String expression;
	
}