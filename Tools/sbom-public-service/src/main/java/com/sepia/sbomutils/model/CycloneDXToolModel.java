// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CycloneDXToolModel {
	private String vendor;
    private String name;
    private String version;
    private List<CycloneDXHashModel> hashes;
    private List<CycloneDXExternalReferenceModel> externalReferences;
}