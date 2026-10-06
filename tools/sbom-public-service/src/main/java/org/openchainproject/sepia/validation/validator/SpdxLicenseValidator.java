/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.validation.validator;

import org.openchainproject.sepia.registry.SpdxLicenseRegistry;

import org.openchainproject.sepia.validation.annotation.ValidSpdxLicense;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class SpdxLicenseValidator
        implements ConstraintValidator<ValidSpdxLicense, String> {

    @Autowired
    private SpdxLicenseRegistry registry;
    
    public SpdxLicenseValidator(
            SpdxLicenseRegistry registry) {

        this.registry = registry;
    }

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context) {

        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        System.out.println("Inside SpdxLicenseValidator");
        return registry.isValidLicense(value);
    }
}