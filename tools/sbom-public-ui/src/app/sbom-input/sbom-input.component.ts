/*
 Parts of this file are created by genAI by using GitHub Copilot.
 This notice needs to remain attached to any reproduction of or excerpt from this file.
*/
/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: MIT */

import { Component, HostListener, TemplateRef, ViewChild } from '@angular/core';
import { SbomInputService } from '../services/sbom-input.service';
import { AuditLog, UploadModel } from './sbom-input.model';
import { NgForm } from '@angular/forms';
import { JsonEditorOptions, JsonEditorComponent } from 'ang-jsoneditor';
import { Router } from '@angular/router';

import { spdx_2_3_schema } from '../schema/spdx_2.3.schema';
import { spdx_2_2_schema } from '../schema/spdx_2.2.schema';
import { cyclonedx_1_4_schema } from '../schema/cyclonedx_1.4.schema';
import { cdq_spdx_2_3_schema } from '../schema/cdq_spdx_2.3.schema';
import { automotive_spdx_2_3_schema } from '../schema/automotive_spdx_2.3.schema';
import { AttachmentText, ComponentType, CycloneDXSBOMStandard, License, LicenseIDs, OrganizationalContactObject } from '../models/cyclonedx.model';
import { CreationInfo, externalRefs, Packages, primaryPackagePurpose, referenceCategory, SpdxModel } from '../models/spdx.model';
import JSZip from 'jszip';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { HealthCheckService } from '../services/health-check.service';
import { cdq_cyclonedx_1_6_schema } from '../schema/cdq_cyclonedx_1.6.schema';
import { automotive_cyclonedx_1_6_schema } from '../schema/automotive_cyclonedx_1.6.schema';
import { CDQComponentType, CDQCycloneDXSBOMStandard, CDQExternalReference, CDQLicense, CDQLicenseIDs, CDQOrganizationalContactObject, CDQOrganizationalEntityObject } from '../models/cdqcyclonedx.model';
import { FormValidationUtil } from '../utils/validation.util';



@Component({
  selector: 'app-sbom-input',
  templateUrl: './sbom-input.component.html',
  styleUrl: './sbom-input.component.css',
  providers: [SbomInputService]
})

export class SbomInputComponent {

  public enableMerge: boolean = false;
  public enableCompare: boolean = false;
  public enableValidate: boolean = false;
  public enableModalSave: boolean = false;
  public fileSaveSuccess: boolean = false;
  public fileUploadSuccess: boolean = false;
  public mergeMode: boolean = false;

  public defaultInputType!: string;
  public defaultSchemaType!: string;
  public mergeLicenseId!: string;
  public mergeLicenseName!: string;
  public mergeLicenseText!: AttachmentText;
  public fileUploadmessage!: string;
  calloutType: 'error' | 'warning' | 'success' | 'info' = 'info';
  public index: number = 0;
  public jsonFileToUpload!: File;

  public dtOptions: any;
  public mergedJson: any;
  public schema: any;
  public mergeType: any;

  public sbomTypes = this.sbomInputService.sbomTypes;
  public schemaTypes = this.sbomInputService.schemaTypes.filter((sch: { value: string }) => (sch.value !== 'spdx2.2'));
  public fossidSchemaTypes = this.sbomInputService.schemaTypes.filter((sch: { value: string }) => (sch.value !== 'custom' && sch.value !== 'spdx'));
  public licenseInfoTypes = this.sbomInputService.licenseInfoTypes;



  public cdqLicenseInfoTypes = this.sbomInputService.cdqLicenseInfoTypes;

  public uploadStatusList: UploadModel[] = [];
  public sbomListToProcess: UploadModel[] = [];
  public fileToUpload: UploadModel = new UploadModel();
  public fileToEdit: UploadModel = new UploadModel();
  public mergedUploadModel: UploadModel = new UploadModel();

  public licenseIdTypes = LicenseIDs;
  public mergeLicenseInfoType = 'licId';

  public cdqLicenseIdTypes = CDQLicenseIDs;
  public cdqMergeLicenseInfoType = 'licId';

  public cdqMergeMetaLicenseInfoType = 'licId';

  public editorOptions: JsonEditorOptions = new JsonEditorOptions();
  public schemaViewerOptions: JsonEditorOptions = new JsonEditorOptions();

  public schemaTypesToMerge = new Set;
  public compTypeList = ComponentType;
  public cdqcompTypeList = CDQComponentType;
  public validationErrors: any = [];
  fileNames: string[] = [];
  uploadFileCountTag: string[] = [];
  isLoading: boolean = false;
  fileName: string | null = null;
  schemafileName: string | null = null;
  showAlert: boolean = false;
  currentModal!: TemplateRef<any> | null;
  alertModal!: TemplateRef<any> | null;

  public convertSourceSchema: string = '';
  public convertTargetSchema: string = '';
  private itemToConvert!: UploadModel | null;
  private convertTargetModal!: TemplateRef<any> | null;

  getLossEventSeverityCount(severity: string): number {
    return this.fileToEdit.lossEvent?.filter((event) =>
      event.severity?.toLowerCase() === severity.toLowerCase()
    ).length ?? 0;
  }

  getLossEventSeverityClass(severity: string): string {
    const normalizedSeverity = severity?.toLowerCase();
    const supportedSeverities = ['blocker', 'major', 'minor', 'informational'];

    return supportedSeverities.includes(normalizedSeverity)
      ? `severity-${normalizedSeverity}`
      : 'severity-unknown';
  }

  // Validation properties for merge forms
  public cdqSpdxInvalidFields: Set<string> = new Set();
  public spdxInvalidFields: Set<string> = new Set();
  public cycloneDXInvalidFields: Set<string> = new Set();
  public cdqCycloneDXInvalidFields: Set<string> = new Set();
  public mergeFormValidationMessage: string = '';
  isModalOpen = false;
  isDialogBoxOpen = false;
  /**
   * CycloneDx Objects
   */
  public cdxMerged: CycloneDXSBOMStandard = new CycloneDXSBOMStandard();
  public mergedMeteCompLicense: License = new License();

  /**
   * CDQ CycloneDx 1.6 Objects
   */
  public cdqCydxMerged: CDQCycloneDXSBOMStandard = new CDQCycloneDXSBOMStandard();


  /**
   * SPDX Objects
   */
  public spdxMerged: SpdxModel = new SpdxModel();
  public spdxMergedPackage: Packages = new Packages();
  public spdxMergedPackageExternalRef: externalRefs = new externalRefs();
  public spdxMergedCreationInfo: CreationInfo = new CreationInfo();
  public spdxMergedcreators!: [String, ...String[]];
  public spdxCreatorOrg!: String;
  public spdxCreatorPerson!: String;
  public primaryPackagePurposeList = primaryPackagePurpose;
  public referenceCategoryList = referenceCategory;

  /**
   * CDQ SPDX Objects
   */
  public cdqSpdxMerged: SpdxModel = new SpdxModel();
  public cdqSpdxMergedPackage: Packages = new Packages();
  public cdqSpdxMergedPackageExternalRef: externalRefs = new externalRefs();
  public cdqSpdxMergedCreationInfo: CreationInfo = new CreationInfo();
  public cdqSpdxMergedcreators!: [String, ...String[]];
  public cdqSpdxCreatorOrg!: String;
  public cdqSpdxCreatorPerson!: String;

  /**
   * ViewChilds
   */

  @ViewChild('sessionClearSuccessModal')
  sessionClearSuccessModal!: TemplateRef<any>;

  @ViewChild('confirmSessionClearModal')
  confirmSessionClearModal!: TemplateRef<any>;

  @ViewChild('convertConfirmModal')
  convertConfirmModal!: TemplateRef<any>;

  @ViewChild('errorDetailsModal')
  errorDetailsModal!: TemplateRef<any>;

  @ViewChild('downloadSuccessModal')
  downloadSuccessModal!: TemplateRef<any>;

  @ViewChild('mergeModal')
  mergeModal!: TemplateRef<any>;

  @ViewChild('editor')
  sbomEditor: JsonEditorComponent = new JsonEditorComponent;

  @ViewChild('schemaEditor')
  schemaEditor: JsonEditorComponent = new JsonEditorComponent;

  @HostListener('window: beforeunload', ['$event'])
  unloadHandler(event: Event) {
    let result = confirm("Data will be lost if you leave the page, are you sure?");
    if (result) {
      window.opener.location.reload();
      this.clearSession();
    }
    event.returnValue = false;
  }

  serverStatus: 'checking' | 'running' | 'down' = 'checking';


  constructor(public sbomInputService: SbomInputService,
    private router: Router, public healthCheckService: HealthCheckService) {
    this.editorOptions.schema = this.schema;
    this.editorOptions.modes = ['tree', 'code', 'view'];
    this.editorOptions.mode = 'tree';
    this.editorOptions.mainMenuBar = true;
    this.editorOptions.navigationBar = true;
    this.schemaViewerOptions.mode = 'view';
    this.editorOptions.onValidate = () => {
      return this.validationErrors;
    }
  }

  ngOnInit(): void {


    this.healthCheckService.checkServerHealth().subscribe(
      response => {

        if (response) {
          if (response.userId) {
            sessionStorage.setItem('userId', response.userId);
          }

          if (response.status === 'Server is running') {
            this.serverStatus = 'running';

            /**
            * defaults initialization
            */
            this.defaultInputType = 'upload';
            this.defaultSchemaType = 'cyclonedx';


            if (sessionStorage.getItem('token') === null) {
              sessionStorage.setItem('token', new Date().getTime().toString());
            }

            // CycloneDX 1.4 merged object defaults
            // Initialize merged objects with default values to prevent undefined errors in the UI before merge happens

            this.cdxMerged.metadata.supplier.contact.push(new OrganizationalContactObject());

            if (!this.cdxMerged.metadata.component.licenses) {
              this.cdxMerged.metadata.component.licenses = [];
            }

            if (!this.cdxMerged.metadata.component.licenses[0]) {
              this.cdxMerged.metadata.component.licenses[0] = {
                license: new License()
              };
            }


            // CDQ CycloneDX 1.6 merged object defaults
            // Initialize with one empty license object to show in the UI, user can add more if needed

            this.cdqCydxMerged.metadata.supplier.contact.push(new CDQOrganizationalContactObject());



            if(!this.cdqCydxMerged.metadata.component.licenses) {
              this.cdqCydxMerged.metadata.component.licenses = [];
            }
            if(!this.cdqCydxMerged.metadata.component.licenses[0]) {
              this.cdqCydxMerged.metadata.component.licenses[0] = {
                license: new CDQLicense()
              };
            }

            if (!this.cdqCydxMerged.metadata.component.externalReferences) {
              this.cdqCydxMerged.metadata.component.externalReferences = [];
              this.cdqCydxMerged.metadata.component.externalReferences.push(new CDQExternalReference());
              this.cdqCydxMerged.metadata.component.externalReferences[0].type = 'other';
            }
            if(!this.cdqCydxMerged.metadata.authors) {
              this.cdqCydxMerged.metadata.authors = [];
            }
            if(!this.cdqCydxMerged.metadata.authors[0]) {
              this.cdqCydxMerged.metadata.authors.push(new CDQOrganizationalContactObject());
            }
            if(!this.cdqCydxMerged.metadata.manufacturer) {
              this.cdqCydxMerged.metadata.manufacturer = new CDQOrganizationalEntityObject();
            }

            // if(!this.cdqCydxMerged.metadata.licenses) {
            //   this.cdqCydxMerged.metadata.licenses = [];
            // }

            // if (!this.cdqCydxMerged.metadata.licenses[0]) {
            //   this.cdqCydxMerged.metadata.licenses[0] = {
            //     license: new CDQLicense()
            //   };
            // }

            // SPDX merged object defaults

            this.spdxMergedPackage.name = '';
            this.spdxMergedPackage.versionInfo = '';
            this.spdxMergedPackage.primaryPackagePurpose = null;
            this.spdxMergedPackage.licenseDeclared = '';
            this.spdxMergedPackage.externalRefs = [];
            this.spdxMergedPackageExternalRef.comment = 'NOASSERTION';
            this.spdxMergedPackageExternalRef.referenceCategory = 'OTHER';
            this.spdxMergedPackageExternalRef.referenceLocator = 'NOASSERTION';
            this.spdxMergedPackageExternalRef.referenceType = 'NOASSERTION';
            this.spdxMergedPackage.externalRefs.push(this.spdxMergedPackageExternalRef);
            this.spdxMergedPackage.copyrightText = 'NOASSERTION';

            this.spdxMerged.packages = [];
            this.spdxMerged.packages.push(this.spdxMergedPackage);
            this.spdxCreatorOrg = '';
            this.spdxCreatorPerson = '';
            this.spdxMergedcreators = ['Tool: SBOM Validator'];
            this.spdxMergedcreators.push('Organization:' + this.spdxCreatorOrg);
            this.spdxMergedcreators.push('Person:' + this.spdxCreatorPerson);
            this.spdxMergedCreationInfo.creators = this.spdxMergedcreators;
            //this.spdxMergedCreationInfo.created = new Date().toISOString();
            //this.spdxMergedCreationInfo.comment = 'This SPDX file generated from Merge operation using SBOM Validator tool.'

            this.spdxMerged.creationInfo = this.spdxMergedCreationInfo;


            // CDQ SPDX 2.3 merged object defaults

            this.cdqSpdxMergedPackage.name = '';
            this.cdqSpdxMergedPackage.versionInfo = '';
            this.cdqSpdxMergedPackage.primaryPackagePurpose = null;
            this.cdqSpdxMergedPackage.licenseConcluded = '';

            this.cdqSpdxMergedPackage.externalRefs = [];
            this.cdqSpdxMergedPackageExternalRef.referenceCategory = null;
            this.cdqSpdxMergedPackage.externalRefs.push(this.cdqSpdxMergedPackageExternalRef);

            this.cdqSpdxMerged.packages = [];
            this.cdqSpdxMerged.packages.push(this.cdqSpdxMergedPackage);
            this.cdqSpdxCreatorOrg = '';
            this.cdqSpdxCreatorPerson = '';
            this.cdqSpdxMergedcreators = ['Tool: SBOM Validator'];
            this.cdqSpdxMergedcreators.push('Organization:' + this.cdqSpdxCreatorOrg);
            this.cdqSpdxMergedcreators.push('Person:' + this.cdqSpdxCreatorPerson);
            this.cdqSpdxMergedCreationInfo.creators = this.cdqSpdxMergedcreators;
            this.cdqSpdxMerged.creationInfo = this.cdqSpdxMergedCreationInfo;


            //
          } else {
            this.serverStatus = 'down';
          }
        }
      },
      error => {
        this.serverStatus = 'down';
      }
    );

  }

  /**
   *
   */
  initializeUndefinedObjects() {
    if (this.fileToEdit.schemaType === "spdx") {
      this.fileToEdit.sbomJson = this.sbomInputService.initializeSpdxUndefinedObjects(JSON.parse(this.fileToEdit.sbomJsonString));
      this.fileToEdit.schemaJsonString = JSON.stringify(spdx_2_3_schema);
      this.editorOptions.schema = spdx_2_3_schema;
      this.editorOptions.sortObjectKeys = true;
    } else if (this.fileToEdit.schemaType === "spdx2.2") {
      this.fileToEdit.sbomJson = JSON.parse(this.fileToEdit.sbomJsonString);
      this.fileToEdit.schemaJsonString = JSON.stringify(spdx_2_2_schema);
      this.editorOptions.schema = spdx_2_2_schema;
      this.editorOptions.sortObjectKeys = true;
    } else if (this.fileToEdit.schemaType === "cdqspdx2.3") {
      this.fileToEdit.sbomJson = this.sbomInputService.initializeCDQSpdxUndefinedObjects(JSON.parse(this.fileToEdit.sbomJsonString)) ;
      this.fileToEdit.schemaJsonString = JSON.stringify(cdq_spdx_2_3_schema);
      this.editorOptions.schema = cdq_spdx_2_3_schema;
      this.editorOptions.sortObjectKeys = true;
    }
    else if (this.fileToEdit.schemaType === "cyclonedx") {
      this.fileToEdit.sbomJson = this.sbomInputService.initializeCycloneDXUndefinedObjects(JSON.parse(this.fileToEdit.sbomJsonString));
      this.fileToEdit.schemaJsonString = JSON.stringify(cyclonedx_1_4_schema);
      this.editorOptions.schema = cyclonedx_1_4_schema;
      this.editorOptions.sortObjectKeys = false;
    } else if (this.fileToEdit.schemaType === "cdqcydx") {
      this.fileToEdit.sbomJson = this.sbomInputService.initializeCDQCycloneDXUndefinedObjects(JSON.parse(this.fileToEdit.sbomJsonString));
      let sanitizedSchema = this.sanitizeSchemaObject(JSON.parse(JSON.stringify(cdq_cyclonedx_1_6_schema)));
      this.fileToEdit.schemaJsonString = JSON.stringify(sanitizedSchema);
      this.editorOptions.schema = sanitizedSchema;
      this.editorOptions.sortObjectKeys = true;
    }
    else if (this.fileToEdit.schemaType === "automotivecydx1.6") {
      this.fileToEdit.sbomJson =this.sanitizeSchemaObject(JSON.parse(this.fileToEdit.sbomJsonString)) ;
      let sanitizedSchema = this.sanitizeSchemaObject(JSON.parse(JSON.stringify(automotive_cyclonedx_1_6_schema)));
      this.fileToEdit.schemaJsonString = JSON.stringify(sanitizedSchema);
      this.editorOptions.schema = sanitizedSchema;
      this.editorOptions.sortObjectKeys = true;
    }
    else if (this.fileToEdit.schemaType === "automotivespdx2.3") {
      this.fileToEdit.sbomJson =this.sanitizeSchemaObject(JSON.parse(this.fileToEdit.sbomJsonString)) ;
      let sanitizedSchema = this.sanitizeSchemaObject(JSON.parse(JSON.stringify(automotive_spdx_2_3_schema)));
      this.fileToEdit.schemaJsonString = JSON.stringify(sanitizedSchema);
      this.editorOptions.schema = sanitizedSchema;
      this.editorOptions.sortObjectKeys = true;
    }
     else if (this.fileToEdit.schemaType === "custom" ||  this.fileToEdit.schemaType === "automotivespdx2.3") {
      this.fileToEdit.sbomJson =this.sanitizeSchemaObject(JSON.parse(this.fileToEdit.sbomJsonString)) ;
      let sanitizedSchema = this.sanitizeSchemaObject(JSON.parse(this.fileToEdit.schemaJsonString));
      this.fileToEdit.schemaJsonString = JSON.stringify(sanitizedSchema);
      this.editorOptions.schema = sanitizedSchema;
    }
    this.validationErrors = this.fileToEdit.customErrorDetails;
  }

  clearLicenses() {
    this.cdxMerged.metadata.component.licenses[0] = {
      license: new License()
    };
  }

  cdqclearLicenses() {
    if (this.cdqMergeLicenseInfoType === 'licText') {
      this.cdqCydxMerged.metadata.component.licenses[0] = {
        expression: ''
      };
    } else {
      this.cdqCydxMerged.metadata.component.licenses[0] = {
        license: new CDQLicense()
      };
    }
  }

  cdqMetadataClearLicenses() {
    this.cdqCydxMerged.metadata.licenses[0] = {
      license: new CDQLicense()
    };
  }

  /**
   * Formats natively supported by AJV (used internally by jsoneditor).
   * Any format not in this list will be removed from the schema to prevent
   * "unknown format" validation errors.
   */
  private readonly supportedFormats: Set<string> = new Set([
    'date', 'date-time', 'time', 'duration',
    'uri', 'uri-reference', 'uri-template',
    'email',
    'hostname',
    'ipv4', 'ipv6',
    'uuid',
    'regex',
    'json-pointer', 'relative-json-pointer'
  ]);

  /**
   * Recursively walks a JSON Schema object and removes unsupported "format"
   * values so that AJV (inside jsoneditor) does not throw errors.
   * Also removes the top-level "$schema" key and external schema references
   * to prevent resolution errors.
   */
  sanitizeSchemaObject(schema: any): any {
    if (schema === null || schema === undefined || typeof schema !== 'object') {
      return schema;
    }

    if (Array.isArray(schema)) {
      return schema.map(item => this.sanitizeSchemaObject(item));
    }

    const sanitized: any = {};
    for (const key of Object.keys(schema)) {
      if (key === '$schema') {
        continue;
      }
      if (key === '$ref' && typeof schema[key] === 'string') {
        // Skip external references that can't be resolved locally
        // (e.g., "spdx.schema.json", "http://...", "https://...")
        const refValue = schema[key];
        if (refValue.startsWith('http://') || refValue.startsWith('https://') ||
            refValue === 'spdx.schema.json') {
          // Skip remote references - they cannot be resolved in the browser
          continue;
        }
        // Keep local references (starting with #)
        sanitized[key] = refValue;
      } else if (key === 'format' && typeof schema[key] === 'string') {
        if (this.supportedFormats.has(schema[key])) {
          sanitized[key] = schema[key];
        }
        // else: skip unsupported format (e.g. iri-reference, idn-email)
      } else {
        sanitized[key] = this.sanitizeSchemaObject(schema[key]);
      }
    }
    return sanitized;
  }

  /**
   * Save edited changes on the uploaded file
   */
  replaceFile() {
    this.fileToEdit.sbomJsonString = JSON.stringify(this.sbomEditor.get());

    this.sbomInputService.replaceFile(this.fileToEdit).subscribe((data: UploadModel) => {
      const indexToUpdate = this.uploadStatusList.findIndex((item) => item.dirName === data.dirName);
      if (indexToUpdate !== -1) {
        this.uploadStatusList.splice(indexToUpdate, 1, data);
        this.fileSaveSuccess = true;
        this.fileUploadSuccess = false;
        this.logAction(data, 'Edit');
      }
      this.fileToEdit = data
      this.initializeUndefinedObjects();
    });
  }

  /**
   * Shows validation errors on clicking the "INVALID" button on the status table
   * @param item
   */
  selectItemToShowErrs(item: UploadModel,modal: TemplateRef<any>) {
    this.fileSaveSuccess = false;
    this.fileUploadSuccess = false;
    this.enableModalSave = true;
    this.fileToEdit = item;
    //this.modalService.open(modal);
    this.openModal(modal);
    this.initializeUndefinedObjects();
  }

  openModal(modalName: TemplateRef<any>) {
    this.isModalOpen = true;
    this.currentModal = modalName;
  }

  openDialogBox(modalName: TemplateRef<any>) {
    this.isDialogBoxOpen = true;
    this.alertModal = modalName;
  }
  showMergeModal(modal: TemplateRef<any>) {
    this.openModal(modal);
  }

  /**
   *
   * @param item
   */
  removeSboms(item: UploadModel): void {
    this.sbomInputService.deleteSbomEntry(item).subscribe((data: any) => {
      this.uploadStatusList = this.uploadStatusList.filter(element => element.dirName !== item.dirName);
      this.logAction(data, 'Delete');
    });
  }

  getDifferences(item: UploadModel) {
    let currentValue = JSON.stringify(this.sbomEditor.getEditor().get());
    this.sbomInputService.getJsonDifferences(currentValue, item, this.mergeMode).subscribe((data: any) => {
      this.setChangeLogs(data);
    })
  }
  showclearSessionWarning(): void {
    //this.openModal(this.confirmSessionClearModal);
    this.openDialogBox(this.confirmSessionClearModal);
  }

  clearSession(): void {
    this.sbomInputService.clearSession().subscribe((data: any) => {
      this.uploadStatusList = [];
      const userId = sessionStorage.getItem('userId');
      sessionStorage.clear();
      if(userId !== null) {
        sessionStorage.setItem('userId', userId);
      }
      this.index = 0;
      this.openDialogBox(this.sessionClearSuccessModal);
      this.showAlert = true;
    });
  }

  logAction(data: UploadModel, action: string) {
    let auditLog: AuditLog = new AuditLog();
    auditLog.userId = sessionStorage.getItem('userId') || '';
    auditLog.action = action;
    auditLog.fileName = data.sbomFileName;
    auditLog.fileHash = data.fileHash;
    auditLog.timestamp = new Date().toLocaleString();

    let auditLogList: AuditLog[] = [];
    let currentLog = sessionStorage.getItem('log');
    if (currentLog !== null) {
      auditLogList = JSON.parse(currentLog);
    }
    auditLogList.push(auditLog);
    sessionStorage.setItem('log', JSON.stringify(auditLogList));
  }

  uploadFile(form: NgForm, isSchema: boolean,event: Event) {
    this.enableValidate = false;
    if (!isSchema) {
      const input = event.target as HTMLInputElement;
      if (input.files && input.files.length > 0) {
        this.jsonFileToUpload = input.files[0];
        this.fileName = this.jsonFileToUpload.name;
      }
    } else {
      const input = event.target as HTMLInputElement;
      if (input.files && input.files.length > 0) {
        this.jsonFileToUpload = input.files[0];
        this.schemafileName = this.jsonFileToUpload.name;
      }

    }
    if (this.jsonFileToUpload && this.jsonFileToUpload.type !== 'application/json') {
      this.fileUploadSuccess = true;
      this.fileUploadmessage = `Please upload a valid JSON file.`;
      this.calloutType = 'info';
      this.hideCalloutAfterInterval();
    } else {
      this.fileNames.push(this.jsonFileToUpload.name);
      this.fileUploadSuccess = false;
      if (isSchema) {
        //this.populateSchemaFileName(form,this.jsonFileToUpload);
      } else {
         //this.populateFileName(form,this.jsonFileToUpload);
      }
      this.isLoading = true;
      this.sbomInputService.uploadFile(form, this.index, isSchema,this.jsonFileToUpload).subscribe((data: any) => {
        this.isLoading = false;
        if (data.status !== 200) {
          this.fileUploadSuccess = true;
          this.fileUploadmessage = `File upload failed: ${data.message}`;
          this.calloutType = 'error';
          this.hideCalloutAfterInterval();
          this.fileNames = [];
        }else {
          this.fileToUpload = data;
          this.removeFileNamefromList(this.fileToUpload.sbomFileName, form);
          this.logAction(data, 'Upload');
        }
      },
      (error) => {
        this.fileUploadmessage = `File upload failed: ${error}`;
        this.fileUploadSuccess = true;
        this.calloutType = 'error';
        this.hideCalloutAfterInterval();
      }
      );
    }
  }

  checkMandatoryFieldsFilled(form: NgForm) {
    if (form.value['schemaType'] === 'custom' && form.value['inputType'] === 'upload') {
      if (this.uploadFileCountTag.length === 2) {
        this.enableValidate = true;
        this.uploadFileCountTag.pop();
        console.log("length:" + this.uploadFileCountTag.length);
      }
    } else if (form.value['schemaType'] !== 'custom' && form.value['inputType'] === 'upload') {
      if ((this.uploadFileCountTag.length === 1)) {
        this.enableValidate = true;
        this.uploadFileCountTag.pop();
      }
    }
  }

  removeFileNamefromList(fileName: string, form: NgForm) {
    const index = this.fileNames.indexOf(fileName);
    if (index > -1) {
      this.uploadFileCountTag.push(fileName);
      this.fileNames.splice(index, 1);
    }
    if (this.fileNames.length === 0) {
      this.isLoading = false;
      //this.enableValidate = true;
      this.checkMandatoryFieldsFilled(form);
    }
  }


  populateFileName(form: NgForm,jsonFileToUpload: File) {
    let incomingFileName: string = jsonFileToUpload.name;
    let inputFileDom = document.querySelector<HTMLInputElement>('input[name="sbomFile"]');
    if (inputFileDom) {
      inputFileDom.textContent = incomingFileName;
    }
  }

  populateSchemaFileName(form: NgForm,jsonFileToUpload: File) {
    let schemaFileName: string = jsonFileToUpload.name;
    let schemaFileDom = document.querySelector<HTMLInputElement>('input[name="schemaFile"]');
    if (schemaFileDom) {
      schemaFileDom.innerHTML = schemaFileName;
    }

  }

  addItemToList() {
    let selectedItems = document.querySelectorAll('input:checked');
    this.sbomListToProcess = [];
    this.schemaTypesToMerge = new Set;
    let isAllSelectedFilesValid: boolean = true;
    for (let i = 0; i < selectedItems.length; i++) {
      let checkbox = selectedItems[i].closest('input');
      if (checkbox !== null) {
        let checkboxId = checkbox.id;
        let splitId = checkboxId.split('_');
        if (splitId && splitId.length > 1) {
          let index: number = +splitId[1];
          this.sbomListToProcess.push(this.uploadStatusList[index]);
          this.schemaTypesToMerge.add(this.uploadStatusList[index].schemaType);
          if (isAllSelectedFilesValid) {
            isAllSelectedFilesValid = this.uploadStatusList[index].valid;
          }
        }
      }
    }

    this.enableMerge = isAllSelectedFilesValid && this.sbomListToProcess.length > 1 && this.schemaTypesToMerge.size === 1 && (Array.from(this.schemaTypesToMerge)[0] !== 'spdx2.2') && (Array.from(this.schemaTypesToMerge)[0] !== 'automotivecydx1.6' ) && (Array.from(this.schemaTypesToMerge)[0] !== 'automotivespdx2.3');
    this.enableCompare = this.schemaTypesToMerge.size === 1 && this.sbomListToProcess.length === 2 && (Array.from(this.schemaTypesToMerge)[0] !== 'spdx2.2');
    this.mergeType = Array.from(this.schemaTypesToMerge)[0];
    console.log("mergeType:" + this.mergeType);
    console.log(this.sbomListToProcess);
  }

  changeLog(event: any) {
    this.fileSaveSuccess = false;
    this.fileUploadSuccess = false;
    let editorJson = this.sbomEditor.getEditor();
    console.log(editorJson);

    editorJson.validate();
    const errors = editorJson.validateSchema.errors;
    if (errors && errors.length > 0) {
      console.log('Errors found', errors);
    } else {
    }
    console.log('event:', event);
    console.log('change:', this.editorOptions);
  }

  changeEvent(event: any) {
    console.log(event);
  }

  selectFileToEdit(item: UploadModel,modal: TemplateRef<any>) {
    this.mergeMode = false;
    this.fileSaveSuccess = false;
    this.fileUploadSuccess = false;
    this.enableModalSave = true;
    this.fileToEdit = item;
    this.fileToEdit.changeLogsList = [];
    this.openModal(modal);
    this.initializeUndefinedObjects();

  }


  validateSboms(form: NgForm): any {
    this.enableValidate = false;
    this.uploadFileCountTag = [];
    this.isLoading = true;
    this.sbomInputService.validateFiles(this.fileToUpload).subscribe((data: UploadModel) => {
      this.fileUploadmessage = data.message;
      this.fileUploadSuccess = true;
      this.isLoading = false;
      if (data.status == 200) {
        console.log("this.fileToUpload:" + this.fileToUpload);
        this.calloutType = 'success';
        this.uploadStatusList.push(data);
        this.index = this.index + 1;
        this.resetForm(form);
        this.logAction(data, 'Validate');
      } else {
        this.fileUploadmessage = `File validation failed: ${this.fileUploadmessage}`;
        this.calloutType = 'error';
      }
      this.hideCalloutAfterInterval();

    },
      (error) => {
        this.fileUploadmessage = `File validation failed: ${error}`;
        this.fileUploadSuccess = true;
        this.calloutType = 'error';
        this.hideCalloutAfterInterval();
      })
  }

  fetchSbomFromFossid(form: NgForm): any {
    this.isLoading = true;
    this.sbomInputService.fetchSbomFromFossid(form, this.fileToUpload, this.index).subscribe((data: UploadModel) => {
      this.isLoading = false;
      this.fileUploadmessage = data.message;
      this.fileUploadSuccess = true;
      if (data.status == 200) {
        this.uploadStatusList.push(data);
        this.index = this.index + 1;
        this.resetForm(form);
        this.logAction(data, 'FOSSID');
        this.calloutType = 'success';
        this.hideCalloutAfterInterval();
      } else {
        this.fileUploadmessage = `File upload failed: ${this.fileUploadmessage}`;
        this.calloutType = 'error';
        this.hideCalloutAfterInterval();
      }
    },
      (error) => {
        this.fileUploadmessage = `File upload failed: ${error}`;
        this.fileUploadSuccess = true;
        this.calloutType = 'error';
        this.hideCalloutAfterInterval();
      }
    )
  }

  hideCalloutAfterInterval() {
    setTimeout(() => {
      this.fileUploadSuccess = false;
      this.fileUploadmessage = '';
    }, 6000); // Hide after 4 seconds
  }

  resetForm(form: NgForm) {
    this.fileToUpload = new UploadModel();
    this.uploadFileCountTag = [];
    this.enableValidate = false;
    let inputFileDom = document.querySelector<HTMLInputElement>('input[name="sbomFile"]');
    if (inputFileDom) {
      inputFileDom.value = '';
      this.fileName = null;
    }
    let schemaFileDom = document.querySelector<HTMLInputElement>('input[name="schemaFile"]');
    if(schemaFileDom) {
      schemaFileDom.value = '';
      this.schemafileName = null;
    }
  }

  mergeSelectedBoms(modal: TemplateRef<any>) {
    this.mergeMode = true;
    this.fileSaveSuccess = false;
    this.fileUploadSuccess = false;
    this.enableModalSave = false;
    this.isLoading = true;
    console.log(this.cdxMerged);
    this.schemaTypesToMerge.values
    this.sbomInputService.mergeSboms(this.sbomListToProcess, this.cdxMerged, this.spdxMerged, this.cdqSpdxMerged,this.cdqCydxMerged,this.mergeType).subscribe((data: UploadModel) => {
      this.isLoading = false;
      this.fileToEdit = data;
      this.initializeUndefinedObjects();
      this.openModal(modal);
      this.logAction(data, 'Merge');
    },
    (error) => {
      this.isLoading = false;
    })
  }

  canConvert(item: UploadModel): boolean {
    return item.valid === true && (item.schemaType === 'cdqspdx2.3' || item.schemaType === 'cdqcydx');
  }

  getSchemaDisplayName(schemaType: string): string {
    switch (schemaType) {
      case 'spdx':
        return 'SPDX V2.3';
      case 'spdx2.2':
        return 'SPDX V2.2';
      case 'cyclonedx':
        return 'CycloneDX V1.4';
      case 'cdqcydx':
        return 'SEPIA CycloneDX V1.6';
      case 'cdqspdx2.3':
        return 'SEPIA SPDX V2.3';
      default:
        return schemaType;
    }
  }

  confirmConvert(item: UploadModel, modal: TemplateRef<any>) {
    this.itemToConvert = item;
    this.convertTargetModal = modal;
    const targetSchemaType = item.schemaType === 'cdqspdx2.3' ? 'cdqcydx' : 'cdqspdx2.3';
    this.convertSourceSchema = this.getSchemaDisplayName(item.schemaType);
    this.convertTargetSchema = this.getSchemaDisplayName(targetSchemaType);
    this.openDialogBox(this.convertConfirmModal);
  }

  proceedConvert() {
    this.closeDialogBox();
    if (this.itemToConvert && this.convertTargetModal) {
      this.convertSboms(this.itemToConvert, this.convertTargetModal);
    }
  }

  convertSboms(item: UploadModel,modal: TemplateRef<any>) {
    this.mergeMode = true;
    this.fileSaveSuccess = false;
    this.fileUploadSuccess = false;
    this.enableModalSave = false;
    this.isLoading = true;
    this.sbomInputService.convertSbom(item).subscribe((data: UploadModel) => {
      this.isLoading = false;
      this.fileToEdit = data;
      this.initializeUndefinedObjects();
      this.openModal(modal);
      this.logAction(data, 'Convert');
    })
  }



  closeModal() {
    this.isModalOpen = false;
    this.currentModal = null;
  }

  closeDialogBox(){
    this.isDialogBoxOpen = false;
    this.alertModal = null;
  }

  downloadContent(item: UploadModel, isDownloadZip: boolean) {
    const c = item.sbomJsonString;
    const blob = new Blob([c.replace(/ /g, ' ')], { type: 'text/json' });
    this.logAction(item, 'Download');
    if (!isDownloadZip) {
      var newAnchorEle = document.createElement("a");
      var url = URL.createObjectURL(blob);

      newAnchorEle.href = url;
      newAnchorEle.download = item.sbomFileName;

      document.body.appendChild(newAnchorEle);

      newAnchorEle.click();

      setTimeout(function () {
        document.body.removeChild(newAnchorEle);
        window.URL.revokeObjectURL(url);
      }, 0);
    } else {
      this.prepareForDownload();
    }
    this.openDialogBox(this.downloadSuccessModal);
  }


  setChangeLogs(data: any) {
    for (var i = 0; i < data.length; i++) {
      let item = this.fileToEdit.changeLogsList;
      var currentItem = item.filter(function (element) {
        return element.path === data[i].path;
      });

      if (currentItem.length) {
        currentItem[0].op = data[i].op;
        currentItem[0].value = data[i].value;
      } else {
        this.fileToEdit.changeLogsList.push(data[i]);
      }
    }
  }

  prepareForDownload() {
    let currentValue = JSON.stringify(this.sbomEditor.getEditor().get());
    this.sbomInputService.prepareForDownload(currentValue, this.fileToEdit).subscribe((data: any) => {
      this.setChangeLogs(data.changeLogsList);
      this.fileToEdit.errorDetails = data.errorDetails;
      this.fileToEdit.fileHash = data.fileHash;
      this.fileToEdit.lossEvent = data.lossEvent;
      const blob = new Blob([currentValue.replace(/ /g, ' ')], { type: 'text/json' });
      this.downloadFiles(blob);
    });

  }


  generatetZipContent(blob: Blob) {
    let content: any;
    let zipContent: any[] = [];
    var downloadParams = this.sbomInputService.downloadParams;
    var i = 0;

    if (this.fileToEdit.errorDetails === null || this.fileToEdit.errorDetails.length == 0) {
      i = 1;
    }

    while (i < 6) {
      var data: any;
      let fileName = downloadParams.fileName[i];
      content = null;
      if (i < 5) {
        switch (i) {
          case 0:
            data = JSON.parse(JSON.stringify(this.fileToEdit.errorDetails));
            break;
          case 1:
            data = JSON.parse(JSON.stringify(this.fileToEdit.changeLogsList));
            break;
          case 2:
            var auditLog = sessionStorage.getItem('log');
            if (auditLog !== null) {
              data = JSON.parse(auditLog);
            }
            break;
          case 3:
            data = JSON.parse(JSON.stringify(this.fileToEdit.lossEvent));
            break;
          case 4:
            data = JSON.parse(JSON.stringify(this.fileToEdit.conversionDeltas));
            break;
          case 5:
            if (!this.mergeMode) {
              fileName = this.fileToEdit.sbomFileName;
            }
            break;
        }
        if(data !== null && data !== undefined && data.length > 0) {
          const pdfFile = new jsPDF("l", "cm", "a3");
          const tableOptions: any = {
            columns: downloadParams.header[i],
            body: data
          };
          // Loss event and conversion log tables can contain long paths/values; make them fit the printable page.
          if (i === 3) {
            tableOptions.styles = { overflow: 'linebreak', cellWidth: 'wrap', fontSize: 8, valign: 'top' };
            tableOptions.headStyles = { overflow: 'linebreak' };
            tableOptions.tableWidth = 'auto';
            tableOptions.margin = { top: 1, right: 1, bottom: 1, left: 1 };
            tableOptions.columnStyles = {
              ruleId: { cellWidth: 4 },
              severity: { cellWidth: 4 },
              kind: { cellWidth: 4 },
              sourcePath: { cellWidth: 6 },
              sourceValue: { cellWidth: 6 },
              targetPath: { cellWidth: 6 },
              targetValue: { cellWidth: 6 },
              reason: { cellWidth: 4 }
            };
          }
          // Conversion log details can contain long paths/values; make the table fit the printable page.
          if (i === 4) {
            tableOptions.styles = { overflow: 'linebreak', cellWidth: 'wrap', fontSize: 8, valign: 'top' };
            tableOptions.headStyles = { overflow: 'linebreak' };
            tableOptions.tableWidth = 'auto';
            tableOptions.margin = { top: 1, right: 1, bottom: 1, left: 1 };
            tableOptions.columnStyles = {
              sourcePath: { cellWidth: 8 },
              targetPath: { cellWidth: 8 },
              value: { cellWidth: 8 },
              category: { cellWidth: 5 },
              reason: { cellWidth: 8 },
              ruleId: { cellWidth: 3 }
            };
          }
          autoTable(pdfFile, tableOptions);
          content = pdfFile.output('blob');
        }
      } else {
        if (!this.mergeMode) {
          fileName = this.fileToEdit.sbomFileName;
        }
        content = blob;
      }
     if(content !== null && content !== undefined) {
        zipContent.push({
          name: fileName,
          content: content
        })
     }
      i++;
    }

    return zipContent;
  }

  downloadFiles(blob: Blob) {

    let zipContent: any[] = this.generatetZipContent(blob);
    var zip = new JSZip();
    var count = 0;
    var totalFiles = zipContent.length;

    function addFileToZip(index: number) {
      var file = zipContent[index];
      var filename = file.name;
      var content = file.content;

      zip.file(filename, content, {
        binary: true
      });
      count++;
      if (count === totalFiles) {
        zip.generateAsync({
          type: "blob"
        }).then(function (content) {
          var link = document.createElement("a");
          link.href = URL.createObjectURL(content);
          link.download = "sbomValidatorData.zip";
          link.click();
          URL.revokeObjectURL(link.href);
        });
      }
    }
    for (var i = 0; i < totalFiles; i++) {
      addFileToZip(i);
    }
  }

  /**
   * Validates CDQ SPDX 2.3 merge form mandatory fields
   */
  validateCDQSpdxMergeForm(): boolean {
    const validation = FormValidationUtil.validateCDQSpdxMergeForm(this.cdqSpdxMerged);
    this.cdqSpdxInvalidFields.clear();

    if (!validation.isValid) {
      validation.emptyFields.forEach(field => {
        this.cdqSpdxInvalidFields.add(field);
      });
    } else {
      this.mergeFormValidationMessage = '';
    }

    return validation.isValid;
  }

  /**
   * Validates SPDX merge form mandatory fields
   */
  validateSPDXMergeForm(): boolean {
    const validation = FormValidationUtil.validateSPDXMergeForm(this.spdxMerged);
    this.spdxInvalidFields.clear();

    if (!validation.isValid) {
      validation.emptyFields.forEach(field => {
        this.spdxInvalidFields.add(field);
      });
    } else {
      this.mergeFormValidationMessage = '';
    }

    return validation.isValid;
  }

  /**
   * Validates CycloneDX merge form mandatory fields
   */
  validateCycloneDXMergeForm(): boolean {
    const validation = FormValidationUtil.validateCycloneDXMergeForm(this.cdxMerged, this.mergeLicenseInfoType);
    this.cycloneDXInvalidFields.clear();

    if (!validation.isValid) {
      validation.emptyFields.forEach(field => {
        this.cycloneDXInvalidFields.add(field);
      });
      this.mergeFormValidationMessage = `Please fill in all mandatory fields: ${validation.emptyFields.join(', ')}`;
    } else {
      this.mergeFormValidationMessage = '';
    }

    return validation.isValid;
  }

  /**
   * Validates CDQ CycloneDX merge form mandatory fields
   */
  validateCDQCycloneDXMergeForm(): boolean {
    const validation = FormValidationUtil.validateCDQCycloneDXMergeForm(this.cdqCydxMerged);
    this.cdqCycloneDXInvalidFields.clear();

    if (!validation.isValid) {
      validation.emptyFields.forEach(field => {
        this.cdqCycloneDXInvalidFields.add(field);
      });
      const errorMessage = validation.emptyFields.map(field => {
        if (field === 'authorsOrManufacturer') {
          return 'either Authors Name or Manufacturer Name';
        }
        return field;
      }).join(', ');
    } else {
      this.mergeFormValidationMessage = '';
    }

    return validation.isValid;
  }

  /**
   * Checks if Authors Name field should be disabled (when Manufacturer Name is filled)
   */
  isAuthorsNameDisabled(): boolean {
    const authorsName = this.cdqCydxMerged?.metadata?.authors?.[0]?.name;
    return !FormValidationUtil.isEmpty(authorsName);
  }

  /**
   * Checks if Manufacturer Name field should be disabled (when Authors Name is filled)
   */
  isManufacturerNameDisabled(): boolean {

    const manufacturerName = this.cdqCydxMerged?.metadata?.manufacturer?.name;
    return !FormValidationUtil.isEmpty(manufacturerName);
  }

  /**
   * Checks if a specific field is invalid (used in template)
   */
  isFieldInvalid(fieldName: string, mergeType: string): boolean {
    switch (mergeType) {
      case 'cdqspdx2.3':
        return this.cdqSpdxInvalidFields.has(fieldName);
      case 'spdx':
        return this.spdxInvalidFields.has(fieldName);
      case 'cyclonedx':
        return this.cycloneDXInvalidFields.has(fieldName);
      case 'cdqcydx':
        return this.cdqCycloneDXInvalidFields.has(fieldName);
      default:
        return false;
    }
  }

  /**
   * Validates the appropriate merge form based on merge type and performs merge if valid
   */
  validateAndMerge(modal: TemplateRef<any>): void {
    let isValid = false;

    switch (this.mergeType) {
      case 'cdqspdx2.3':
        isValid = this.validateCDQSpdxMergeForm();
        break;
      case 'spdx':
        isValid = this.validateSPDXMergeForm();
        break;
      case 'cyclonedx':
        isValid = this.validateCycloneDXMergeForm();
        break;
      case 'cdqcydx':
        isValid = this.validateCDQCycloneDXMergeForm();
        break;
      default:
        isValid = false;
    }

    if (isValid) {
      this.mergeSelectedBoms(modal);
    }
  }

  showSuccessAlert(): void {
    this.showAlert = true;
  }

  closeAlert(): void {
    this.showAlert = false;
  }

}
