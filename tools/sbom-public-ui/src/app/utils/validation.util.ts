/*
 Parts of this file are created by genAI by using GitHub Copilot.
 This notice needs to remain attached to any reproduction of or excerpt from this file.
// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
// SPDX-License-Identifier: MIT
*/

/**
 * Common validation utility class for form field validation
 * This utility provides reusable validation methods for mandatory fields
 */
export class FormValidationUtil {

  /**
   * Validates if a value is empty
   * @param value - Value to validate
   * @returns true if value is empty, false otherwise
   */
  static isEmpty(value: any): boolean {
    return value === null || value === undefined || value === '' || value === 'null';
  }

  /**
   * Validates multiple fields for emptiness
   * @param fields - Object containing field names and their values
   * @returns Array of field names that are empty
   */
  static validateMandatoryFields(fields: { [fieldName: string]: any }): string[] {
    const emptyFields: string[] = [];

    Object.keys(fields).forEach(fieldName => {
      if (this.isEmpty(fields[fieldName])) {
        emptyFields.push(fieldName);
      }
    });

    return emptyFields;
  }

  /**
   * Checks if a specific field is valid
   * @param value - Value to check
   * @returns true if field has a valid value, false otherwise
   */
  static isFieldValid(value: any): boolean {
    return !this.isEmpty(value);
  }

  /**
   * Validates CDQ SPDX 2.3 merge form mandatory fields
   * @param cdqSpdxMerged - CDQ SPDX merged object
   * @returns Object containing validation status and empty field names
   */
  static validateCDQSpdxMergeForm(cdqSpdxMerged: any): {
    isValid: boolean;
    emptyFields: string[];
  } {
    const mandatoryFields = {
      packageName: cdqSpdxMerged?.packages?.[0]?.name,
      packageVersion: cdqSpdxMerged?.packages?.[0]?.versionInfo,
      primaryPackagePurpose: cdqSpdxMerged?.packages?.[0]?.primaryPackagePurpose,
      externalRefCategory: cdqSpdxMerged?.packages?.[0]?.externalRefs?.[0]?.referenceCategory
    };

    const emptyFields = this.validateMandatoryFields(mandatoryFields);

    return {
      isValid: emptyFields.length === 0,
      emptyFields: emptyFields
    };
  }

  /**
   * Validates SPDX merge form mandatory fields
   * @param spdxMerged - SPDX merged object
   * @returns Object containing validation status and empty field names
   */
  static validateSPDXMergeForm(spdxMerged: any): {
    isValid: boolean;
    emptyFields: string[];
  } {
    const mandatoryFields = {
      packageName: spdxMerged?.packages?.[0]?.name,
      packageVersion: spdxMerged?.packages?.[0]?.versionInfo,
      primaryPackagePurpose: spdxMerged?.packages?.[0]?.primaryPackagePurpose,
      licenseDeclared: spdxMerged?.packages?.[0]?.licenseDeclared,
      creatorsOrganization: spdxMerged?.creationInfo?.creators?.[1],
      creatorsPerson: spdxMerged?.creationInfo?.creators?.[2]
    };

    const emptyFields = this.validateMandatoryFields(mandatoryFields);

    return {
      isValid: emptyFields.length === 0,
      emptyFields: emptyFields
    };
  }

  /**
   * Validates CycloneDX merge form mandatory fields
   * @param cdxMerged - CycloneDX merged object
   * @returns Object containing validation status and empty field names
   */
  static validateCycloneDXMergeForm(cdxMerged: any, mergeLicenseInfoType: string): {
    isValid: boolean;
    emptyFields: string[];
  } {
    const mandatoryFields: { [fieldName: string]: any } = {
      componentName: cdxMerged?.metadata?.component?.name,
      componentGroup: cdxMerged?.metadata?.component?.group,
      componentType: cdxMerged?.metadata?.component?.type,
      componentVersion: cdxMerged?.metadata?.component?.version,
      licenseInfoType: mergeLicenseInfoType,
      supplierName: cdxMerged?.metadata?.supplier?.name,
      supplierEmail: cdxMerged?.metadata?.supplier?.contact?.[0]?.email
    };

    if (mergeLicenseInfoType === 'licId') {
      mandatoryFields['licenseId'] = cdxMerged?.metadata?.component?.licenses?.[0]?.license?.id;
    } else if (mergeLicenseInfoType === 'licName') {
      mandatoryFields['licenseName'] = cdxMerged?.metadata?.component?.licenses?.[0]?.license?.name;
    } else if (mergeLicenseInfoType === 'licText') {
      mandatoryFields['licenseText'] = cdxMerged?.metadata?.component?.licenses?.[0]?.expression;
    }

    const emptyFields = this.validateMandatoryFields(mandatoryFields);

    return {
      isValid: emptyFields.length === 0,
      emptyFields: emptyFields
    };
  }

  /**
   * Validates CDQ CycloneDX merge form mandatory fields
   * @param cdqCydxMerged - CDQ CycloneDX merged object
   * @returns Object containing validation status and empty field names
   */
  static validateCDQCycloneDXMergeForm(cdqCydxMerged: any): {
    isValid: boolean;
    emptyFields: string[];
  } {
    const mandatoryFields = {
      componentName: cdqCydxMerged?.metadata?.component?.name,
      componentGroup: cdqCydxMerged?.metadata?.component?.group,
      componentType: cdqCydxMerged?.metadata?.component?.type,
      componentVersion: cdqCydxMerged?.metadata?.component?.version,
      componentPurl: cdqCydxMerged?.metadata?.component?.purl,
      supplierName: cdqCydxMerged?.metadata?.supplier?.name
    };

    const emptyFields = this.validateMandatoryFields(mandatoryFields);

    // Validate exclusive: exactly one of Authors Name OR Manufacturer Name must be filled
    // Invalid if both empty OR both filled
    const authorsName = cdqCydxMerged?.metadata?.authors?.[0]?.name;
    const manufacturerName = cdqCydxMerged?.metadata?.manufacturer?.name;

    const authorsEmpty = this.isEmpty(authorsName);
    const manufacturerEmpty = this.isEmpty(manufacturerName);

    // Must have exactly one filled: both empty is invalid, both filled is invalid
    if ((authorsEmpty && manufacturerEmpty) || (!authorsEmpty && !manufacturerEmpty)) {
      emptyFields.push('authorsOrManufacturer');
    }

    return {
      isValid: emptyFields.length === 0,
      emptyFields: emptyFields
    };
  }
}
