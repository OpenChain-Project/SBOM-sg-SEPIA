/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.validation.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;
import org.openchainproject.sepia.validation.validator.SpdxLicenseValidator;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SpdxLicenseValidator.class)
@Documented
public @interface ValidSpdxLicense {

    String message() default "Invalid SPDX License ID";

//    Class<?>[] groups() default {};
//
//    Class<? extends Payload>[] payload() default {};
}