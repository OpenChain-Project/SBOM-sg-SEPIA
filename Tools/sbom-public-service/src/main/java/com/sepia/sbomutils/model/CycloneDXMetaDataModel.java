/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CycloneDXMetaDataModel {
	
	private Date timestamp = new Date();
	private List<CycloneDXToolModel> tools;
	private CycloneDXOrganizationalEntityModel supplier;
	private CycloneDXComponentModel component;
	private List<CycloneDXPropertyModel> properties;
	public CycloneDXOrganizationalEntityModel getSupplier() {
        return supplier;
    }
}