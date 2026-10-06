/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.registry;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openchainproject.sepia.util.Constants;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Component
public class SpdxLicenseRegistry {

    private final Set<String> licenseIdsInCydx16 = new HashSet<>();
    private final Set<String> licenseIdsInCydx14 = new HashSet<>();
    private final Set<String> allLicenseIds = new HashSet<>();

    @PostConstruct
    public void init() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        loadLicenseIdsFromSchema(mapper, "/CDQcyclonedx_1.6.json", licenseIdsInCydx16);
        loadLicenseIdsFromSchema(mapper, "/cyclonedx_1.4.schema.json", licenseIdsInCydx14);
        allLicenseIds.addAll(licenseIdsInCydx16);
        allLicenseIds.addAll(licenseIdsInCydx14);

        System.out.println("Loaded SPDX licenses (CDQ CycloneDX 1.6): " + licenseIdsInCydx16.size());
        System.out.println("Loaded SPDX licenses (CycloneDX 1.4): " + licenseIdsInCydx14.size());
    }

    private void loadLicenseIdsFromSchema(ObjectMapper mapper, String schemaFileName, Set<String> targetSet)
            throws Exception {
        try (InputStream is = getClass().getResourceAsStream(schemaFileName)) {

            if (is == null) {
                throw new IllegalStateException(schemaFileName + " not found");
            }

            JsonNode root = mapper.readTree(is);

            JsonNode enumNode = root.path("definitions")
                    .path("license")
                    .path("properties")
                    .path("id")
                    .path("enum");

            if (!enumNode.isArray()) {
                throw new IllegalStateException(
                        "Unable to locate definitions.license.properties.id.enum in " + schemaFileName);
            }

            for (JsonNode node : enumNode) {
                targetSet.add(node.asText());
            }
        }
    }

    public boolean isValidLicense(String id) {
        if (id == null) {
            return false;
        }
        String normalizedId = id.trim();
        return licenseIdsInCydx16.contains(normalizedId)
                || licenseIdsInCydx14.contains(normalizedId);
    }

    public boolean isValidLicense(String id,String schemaType) {
        if (id == null || schemaType == null) {
            return false;
        }
        switch (schemaType.toLowerCase()) {
	        case Constants.CDQ_CYDX1_6_LC:
	        	return licenseIdsInCydx16.contains(id.trim());
	        case Constants.CYCLONEDX_LC:
				return licenseIdsInCydx14.contains(id.trim());
		default:
				return false;	
          }
		}

    public Set<String> getLicenseIds() {

        return Set.copyOf(licenseIdsInCydx16);
    }

    public Set<String> getAllLicenseIds() {
        return new HashSet<>(allLicenseIds);
    }
}