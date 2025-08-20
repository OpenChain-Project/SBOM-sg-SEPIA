/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpdxCreationInfoModel {
	private String created;
	private String comment;
	private String[] creators;
	private String licenseListVersion;
	
	public void setcreated(String created) {
        this.created = created;
    }
	
	public void setcomment(String comment) {
        this.comment = comment;
       
    }
	
	public void setcreators(String[] creators) {
        this.creators = creators;
    }
	
	public void setlicenseListVersion(String licenseListVersion) {
        this.licenseListVersion = licenseListVersion;
       
    }
}
