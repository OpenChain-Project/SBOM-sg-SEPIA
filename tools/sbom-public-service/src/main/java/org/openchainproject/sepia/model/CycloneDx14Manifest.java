/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.cyclonedx.Version;
import org.cyclonedx.model.VersionFilter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CycloneDx14Manifest {

    @Valid
    @NotNull(message = "metadata is mandatory")
    private Metadata metadata;

    // =========================================================
    // Getter Setter
    // =========================================================

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
        @NotNull(message = "component is mandatory")
        private Component component;

        @Valid
        @NotNull(message = "supplier is mandatory")
        private Supplier supplier;

        // Getter Setter

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
            @JsonProperty("operating-system")
            OPERATING_SYSTEM("operating-system"),
            @JsonProperty("device")
            DEVICE("device"),
            @JsonProperty("firmware")
            FIRMWARE("firmware"),
            @JsonProperty("file")
            FILE("file");

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
        
        @Valid
        @NotEmpty(message = "component.licenses is mandatory")
        private List<LicenseWrapper> licenses;

        @NotBlank(message = "component.name is mandatory")
        private String name;

        @NotBlank(message = "component.group is mandatory")
        private String group;

        @NotNull(message = "component.type is mandatory")
        private Type type;

        @NotBlank(message = "component.version is mandatory")
        private String version;

        // =====================================================
        // Getter Setter
        // =====================================================

        public List<LicenseWrapper> getLicenses() {
            return licenses;
        }

        public void setLicenses(List<LicenseWrapper> licenses) {
            this.licenses = licenses;
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
    }

    // =========================================================
    // LicenseWrapper
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LicenseWrapper {

        @Valid
        @NotNull(message = "license object is mandatory")
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
    // Supplier
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Supplier {

        @Valid
        @NotEmpty(message = "supplier.contact is mandatory")
        private List<Contact> contact;

        @NotBlank(message = "supplier.name is mandatory")
        private String name;

        // Getter Setter

        public List<Contact> getContact() {
            return contact;
        }

        public void setContact(List<Contact> contact) {
            this.contact = contact;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // =========================================================
    // Contact
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {

        @Email(message = "supplier.contact.email must be valid")
        @NotBlank(message = "supplier.contact.email is mandatory")
        private String email;

        // Getter Setter

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}