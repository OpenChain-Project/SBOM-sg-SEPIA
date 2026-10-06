/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.cyclonedx.Version;
import org.cyclonedx.model.VersionFilter;
import org.cyclonedx.model.Component.Type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CDQCycloneDx16Manifest {

    @Valid
    @NotNull
    private Metadata metadata;

    // Getter Setter
    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    // =========================================================
    // Metadata
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
    	
    	@Valid
        @NotNull
        private List<Authors> authors;

        @Valid
        @NotNull
        private Manufacturer manufacturer;

        @Valid
        @NotNull
        private Component component;

        @Valid
        @NotNull
        private Supplier supplier;

        // Getter Setter
        
        // Getter Setter

        public List<Authors> getAuthors() {
            return authors;
        }

        public void setAuthors(List<Authors> authors) {
            this.authors = authors;
        }
        
        public Manufacturer getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(Manufacturer manufacturer) {
            this.manufacturer = manufacturer;
        }

        public Component getComponent() {
            return component;
        }

        public void setComponent(Component component) {
            this.component = component;
        }

        public Supplier getSupplier() {
            return supplier;
        }

        public void setSupplier(Supplier supplier) {
            this.supplier = supplier;
        }
    }

    // =========================================================
    // Manufacturer
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Manufacturer {

        @NotBlank(message = "manufacturer.name is mandatory")
        private String name;

        // Getter Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    
 // =========================================================
    // Authors
    // =========================================================
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Authors {

        @NotBlank(message = "Authors.name is mandatory")
        private String name;

        // Getter Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // =========================================================
    // Component
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Component {
    	
        public enum Type {
            @JsonProperty("application")
            APPLICATION("application"),
            @JsonProperty("framework")
            FRAMEWORK("framework"),
            @JsonProperty("library")
            LIBRARY("library"),
            @JsonProperty("container")
            CONTAINER("container"),
            @JsonProperty("platform")
            PLATFORM("platform"),
            @JsonProperty("operating-system")
            OPERATING_SYSTEM("operating-system"),
            @JsonProperty("device")
            DEVICE("device"),
            @JsonProperty("device-driver")
            DEVICE_DRIVER("device-driver"),
            @JsonProperty("firmware")
            FIRMWARE("firmware"),
            @JsonProperty("file")
            FILE("file"),
            @JsonProperty("machine-learning-model")
            MACHINE_LEARNING_MODEL("machine-learning-model"),
            @JsonProperty("data")
            DATA("data"),
            @VersionFilter(Version.VERSION_16)
            @JsonProperty("cryptographic-asset")
            CRYPTOGRAPHIC_ASSET("cryptographic-asset");

            private final String name;

            public String getTypeName() {
                return this.name;
            }

            Type(String name) {
                this.name = name;
            }
            
            @JsonCreator
            public static Type fromValue(String value) {

                for (Type type : Type.values()) {
                    if (type.name.equalsIgnoreCase(value)) {
                        return type;
                    }
                }

                throw new IllegalArgumentException(
                    "Invalid component type: " + value
                );
            }
        }

        //@Valid
        //@NotEmpty(message = "component.licenses is mandatory")
        private List<LicenseWrapper> licenses;

//        @Valid
//        @NotEmpty(message = "component.externalReferences is mandatory")
        private List<ExternalReference> externalReferences;

        @NotBlank(message = "component.name is mandatory")
        private String name;

        @NotBlank(message = "component.group is mandatory")
        private String group;

        @NotNull(message = "component.type is mandatory")
        private Type type;
        //private String type;

        @NotBlank(message = "component.version is mandatory")
        private String version;

        @NotBlank(message = "component.purl is mandatory")
        private String purl;

        // Getter Setter

        public List<LicenseWrapper> getLicenses() {
            return licenses;
        }

        public void setLicenses(List<LicenseWrapper> licenses) {
            this.licenses = licenses;
        }

        public List<ExternalReference> getExternalReferences() {
            return externalReferences;
        }

        public void setExternalReferences(
                List<ExternalReference> externalReferences) {
            this.externalReferences = externalReferences;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPurl() {
            return purl;
        }

        public void setPurl(String purl) {
            this.purl = purl;
        }
    }

    // =========================================================
    // LicenseWrapper
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LicenseWrapper {

//        @Valid
//        @NotNull(message = "license object is mandatory")
        private License license;

        // Getter Setter
        public License getLicense() {
            return license;
        }

        public void setLicense(License license) {
            this.license = license;
        }
    }

    // =========================================================
    // License
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class License {

        @NotBlank(message = "license.id is mandatory")
        private String id;

        // Getter Setter
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    // =========================================================
    // ExternalReference
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExternalReference {
    	
        public enum externalRefType {
            @JsonProperty("vcs")
            VCS("vcs"),
            @JsonProperty("issue-tracker")
            ISSUE_TRACKER("issue-tracker"),
            @JsonProperty("website")
            WEBSITE("website"),
            @JsonProperty("advisories")
            ADVISORIES("advisories"),
            @JsonProperty("bom")
            BOM("bom"),
            @JsonProperty("mailing-list")
            MAILING_LIST("mailing-list"),
            @JsonProperty("social")
            SOCIAL("social"),
            @JsonProperty("chat")
            CHAT("chat"),
            @JsonProperty("documentation")
            DOCUMENTATION("documentation"),
            @JsonProperty("support")
            SUPPORT("support"),
            @JsonProperty("source-distribution")
            SOURCE_DISTRIBUTION("source-distribution"),
            @JsonProperty("distribution")
            DISTRIBUTION("distribution"),
            @JsonProperty("distribution-intake")
            DISTRIBUTION_INTAKE("distribution-intake"),
            @JsonProperty("license")
            LICENSE("license"),
            @JsonProperty("build-meta")
            BUILD_META("build-meta"),
            @JsonProperty("build-system")
            BUILD_SYSTEM("build-system"),
            @JsonProperty("release-notes")
            RELEASE_NOTES("release-notes"),
            @VersionFilter(Version.VERSION_15)
            @JsonProperty("security-contact")
            SECURITY_CONTACT("security-contact"),
            @JsonProperty("model_card")
            MODEL_CARD("model_card"),
            @JsonProperty("attestation")
            ATTESTATION("attestation"),
            @JsonProperty("threat-model")
            THREAT_MODEL("threat-model"),
            @JsonProperty("adversary-model")
            ADVERSARY_MODEL("adversary-model"),
            @JsonProperty("risk-assessment")
            RISK_ASSESSMENT("risk-assessment"),
            @JsonProperty("vulnerability-assertion")
            VULNERABILITY_ASSERTION("vulnerability-assertion"),
            @JsonProperty("exploitability-statement")
            EXPLOITABILITY_STATEMENT("exploitability-statement"),
            @JsonProperty("pentest-report")
            PENTEST_REPORT("pentest-report"),
            @JsonProperty("static-analysis-report")
            STATIC_ANALYSIS_REPORT("static-analysis-report"),
            @JsonProperty("dynamic-analysis-report")
            DYNAMIC_ANALYSIS_REPORT("dynamic-analysis-report"),
            @JsonProperty("runtime-analysis-report")
            RUNTIME_ANALYSIS_REPORT("runtime-analysis-report"),
            @JsonProperty("component-analysis-report")
            COMPONENT_ANALYSIS_REPORT("component-analysis-report"),
            @JsonProperty("maturity-report")
            MATURITY_REPORT("maturity-report"),
            @JsonProperty("certification-report")
            CERTIFICATION_REPORT("certification-report"),
            @JsonProperty("codified-infrastructure")
            CODIFIED_INFRASTRUCTURE("codified-infrastructure"),
            @JsonProperty("quality-metrics")
            QUALITY_METRICS("quality-metrics"),
            @JsonProperty("log")
            LOG("log"),
            @JsonProperty("configuration")
            CONFIGURATION("configuration"),
            @JsonProperty("evidence")
            EVIDENCE("evidence"),
            @JsonProperty("formulation")
            FORMULATION("formulation"),
            @JsonProperty("rfc-9116")
            RFC_9116("rfc-9116"),
            @VersionFilter(Version.VERSION_16)
            @JsonProperty("electronic-signature")
            ELECTRONIC_SIGNATURE("electronic-signature"),
            @VersionFilter(Version.VERSION_16)
            @JsonProperty("digital-signature")
            DIGITAL_SIGNATURE("digital-signature"),
            @JsonProperty("other")
            OTHER("other");

            private final String name;

            public String getTypeName() {
                return this.name;
            }

            externalRefType(String name) {
                this.name = name;
            }

            public static externalRefType fromString(String text) {
                for (externalRefType t : externalRefType.values()) {
                    if (t.name.equals(text)) {
                        return t;
                    }
                }
                return null;
            }
        }
        
//    	@NotNull(message = "externalReference.type is mandatory")
        private externalRefType type;

//        @NotBlank(message = "externalReference.url is mandatory")
        private String url;

        // Getter Setter
        public externalRefType getType() {
            return type;
        }

        public void setType(externalRefType type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    // =========================================================
    // Supplier
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Supplier {

        @NotBlank(message = "supplier.name is mandatory")
        private String name;

        // Getter Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}