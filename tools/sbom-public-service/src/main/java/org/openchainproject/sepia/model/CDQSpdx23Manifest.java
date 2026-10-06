/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CDQSpdx23Manifest {

    @Valid
    @NotNull(message = "creationInfo is mandatory")
    private CreationInfo creationInfo;

    @Valid
    @NotEmpty(message = "packages is mandatory")
    private List<PackageInfo> packages;

    // =========================================================
    // Getter Setter
    // =========================================================

    public CreationInfo getCreationInfo() {
        return creationInfo;
    }

    public void setCreationInfo(CreationInfo creationInfo) {
        this.creationInfo = creationInfo;
    }

    public List<PackageInfo> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageInfo> packages) {
        this.packages = packages;
    }

    // =========================================================
    // CreationInfo
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreationInfo {

        @NotEmpty(message = "creationInfo.creators is mandatory")
        private List<String> creators;

        // Getter Setter

        public List<String> getCreators() {
            return creators;
        }

        public void setCreators(List<String> creators) {
            this.creators = creators;
        }
    }

    // =========================================================
    // PackageInfo
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackageInfo {
    	
        public enum primaryPackagePurposeType {
        	@JsonProperty("APPLICATION")
            APPLICATION("APPLICATION"),
            @JsonProperty("ARCHIVE")
            ARCHIVE("ARCHIVE"),
            @JsonProperty("CONTAINER")
            CONTAINER("CONTAINER"),
            @JsonProperty("DEVICE")
            DEVICE("DEVICE"),
            @JsonProperty("FILE")
            FILE("FILE"),
            @JsonProperty("FIRMWARE")
            FIRMWARE("FIRMWARE"),
            @JsonProperty("FRAMEWORK")
            FRAMEWORK("FRAMEWORK"),
            @JsonProperty("INSTALL")
            INSTALL("INSTALL"),
            @JsonProperty("LIBRARY")
            LIBRARY("LIBRARY"),
            @JsonProperty("OPERATING_SYSTEM")
            OPERATING_SYSTEM("OPERATING_SYSTEM"),
            @JsonProperty("SOURCE")
            SOURCE("SOURCE"),
            @JsonProperty("OTHER")
            OTHER("OTHER");


            private final String name;

            public String getTypeName() {
                return this.name;
            }

            primaryPackagePurposeType(String name) {
                this.name = name;
            }

            public static primaryPackagePurposeType fromString(String text) {
                for (primaryPackagePurposeType t : primaryPackagePurposeType.values()) {
                    if (t.name.equals(text)) {
                        return t;
                    }
                }
                return null;
            }
        }

        @Valid
        @NotEmpty(message = "package.externalRefs is mandatory")
        private List<ExternalRef> externalRefs;

        @NotBlank(message = "package.name is mandatory")
        private String name;

        @NotBlank(message = "package.versionInfo is mandatory")
        private String versionInfo;

        @NotNull(message = "package.primaryPackagePurpose is mandatory")
        private primaryPackagePurposeType primaryPackagePurpose;

        
        private String licenseConcluded;

        // =====================================================
        // Getter Setter
        // =====================================================

        public List<ExternalRef> getExternalRefs() {
            return externalRefs;
        }

        public void setExternalRefs(List<ExternalRef> externalRefs) {
            this.externalRefs = externalRefs;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersionInfo() {
            return versionInfo;
        }

        public void setVersionInfo(String versionInfo) {
            this.versionInfo = versionInfo;
        }

        public primaryPackagePurposeType getPrimaryPackagePurpose() {
            return primaryPackagePurpose;
        }

        public void setPrimaryPackagePurpose(primaryPackagePurposeType primaryPackagePurpose) {
            this.primaryPackagePurpose = primaryPackagePurpose;
        }

        public String getLicenseConcluded() {
            return licenseConcluded;
        }

        public void setLicenseConcluded(String licenseConcluded) {
            this.licenseConcluded = licenseConcluded;
        }
    }

    // =========================================================
    // ExternalRef
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExternalRef {

        public enum externalRefCategory {
            
            @JsonProperty("OTHER")
            OTHER("OTHER"),

            @JsonProperty("PERSISTENT-ID")
            PERSISTENT_ID_HYPHEN("PERSISTENT-ID"),

            @JsonProperty("PERSISTENT_ID")
            PERSISTENT_ID_UNDERSCORE("PERSISTENT_ID"),

            @JsonProperty("SECURITY")
            SECURITY("SECURITY"),

            @JsonProperty("PACKAGE-MANAGER")
            PACKAGE_MANAGER_HYPHEN("PACKAGE-MANAGER"),

            @JsonProperty("PACKAGE_MANAGER")
            PACKAGE_MANAGER_UNDERSCORE("PACKAGE_MANAGER");

            private final String name;

            public String getTypeName() {
                return this.name;
            }

            externalRefCategory(String name) {
                this.name = name;
            }

            public static externalRefCategory fromString(String text) {
                for (externalRefCategory t : externalRefCategory.values()) {
                    if (t.name.equals(text)) {
                        return t;
                    }
                }
                return null;
            }
        }
    	
        @NotNull(message =
                "externalRef.referenceCategory is mandatory")
        private externalRefCategory referenceCategory;

        // Getter Setter

        public externalRefCategory getReferenceCategory() {
            return referenceCategory;
        }

        public void setReferenceCategory(externalRefCategory referenceCategory) {
            this.referenceCategory = referenceCategory;
        }
    }
}