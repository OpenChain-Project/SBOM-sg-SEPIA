/*
 Parts of this file are created by genAI by using GitHub Copilot.
 This notice needs to remain attached to any reproduction of or excerpt from this file.
*/
/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: MIT */

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { RestEndpointsService } from './rest-endpoints.service';
import { NgForm } from '@angular/forms';
import { catchError, Observable, of } from 'rxjs';
import { UploadModel } from '../sbom-input/sbom-input.model';
import {
  BOMMetadata, MetadataComponent, Licenses, License, Supplier,
  OrganizationalContactObject, Tool, HashObjects, ComponentObject,
  ExternalReference, CycloneDXSBOMStandard,
  AttachmentText
} from '../models/cyclonedx.model';
import { CreationInfo, externalRefs, Packages, SpdxModel } from '../models/spdx.model';


@Injectable({
  providedIn: 'root'
})
export class SbomInputService {


  constructor(private httpClient: HttpClient,
    private restEndPointService: RestEndpointsService) { }

  httpOptions: Object = {

    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PATCH, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Origin, Content-Type, X-Auth-Token'
    })
  }

  public spdxModelToEdit: SpdxModel = new SpdxModel();
  public cycloneDxModelToEdit: CycloneDXSBOMStandard = new CycloneDXSBOMStandard();

  deleteSbomEntry(item: UploadModel) {
    const data: FormData = new FormData();
    data.append('postData', JSON.stringify(item));
    return this.httpClient.post(this.restEndPointService.deleteSbomEntry, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }

  clearSession() {
    const data: FormData = new FormData();
    let item: UploadModel = new UploadModel();
    if (sessionStorage.getItem('token') !== null) {
      item.timestamp = sessionStorage.getItem('token');
      data.append('postData', JSON.stringify(item));
      return this.httpClient.post(this.restEndPointService.clearSession, data)
        .pipe(catchError(this.handleError<any>('null'))
        );
    } else {
      return new Observable();
    }
  }

  validateFiles(fileToValidate: UploadModel): Observable<any> {

    const data: FormData = new FormData();
    data.append('postData', JSON.stringify(fileToValidate));
    return this.httpClient.post(this.restEndPointService.validateFiles, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }

  fetchSbomFromFossid(form: NgForm,item: UploadModel,index: number) {
    const data: FormData = new FormData();
    let url: string = this.restEndPointService.fetchSbomFromFossid;
    item.index = index;
    //item.schemaType = "cyclonedx";
    item.schemaType = form.value['schemaType'];
    item.dirName = item.index + "_" + item.schemaType;

    if (sessionStorage.getItem('token') === null) {
      sessionStorage.setItem('token', new Date().getTime().toString());
    }
    item.timestamp = sessionStorage.getItem('token');

    data.append('postData', JSON.stringify(item));
    data.append('isFromApp', JSON.stringify(true));
    return this.httpClient.post(url, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }

  getJsonDifferences(currentValue: string,item: UploadModel,mergeMode: boolean): Observable<any> {
    let uploadData: UploadModel = new UploadModel();
    if(!mergeMode) {
       uploadData.filePath = item.filePath;
       uploadData.sbomFileName = item.sbomFileName;
    } else {
      uploadData.sbomJsonString = item.sbomJsonString;
    }

    const data: FormData = new FormData();
    data.append('currentValue', currentValue);
    data.append('postData', JSON.stringify(uploadData));
    data.append('isMerge', JSON.stringify(mergeMode));
    return this.httpClient.post(this.restEndPointService.getJsonDifferences, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }

  prepareForDownload(currentValue: string, uploadModel: UploadModel): Observable<any> {

    const data: FormData = new FormData();
    data.append('currentValue', currentValue);
    data.append('postData', JSON.stringify(uploadModel));
    return this.httpClient.post(this.restEndPointService.prepareForDownload, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }



  mergeSboms(uploadModels: UploadModel[], cdxMerged: CycloneDXSBOMStandard, spdxMerged: SpdxModel, mergeType: any): Observable<any> {

    const data: FormData = new FormData();
    let url: string = this.restEndPointService.mergeSpdx;
    data.append('postData', JSON.stringify(uploadModels));
    if(mergeType === 'cyclonedx') {
      url = this.restEndPointService.mergeCyclonedx;
      data.append('bomMetadata', JSON.stringify(cdxMerged));
    } else {
      data.append('bomMetadata', JSON.stringify(spdxMerged));
    }
    data.append('isFromApp', JSON.stringify(true));
    return this.httpClient.post(url, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }

  convertSbom(item: UploadModel) {
    const data: FormData = new FormData();
    let url: string = this.restEndPointService.convertSbom;
    data.append('postData', JSON.stringify(item));
    data.append('isFromApp', JSON.stringify(true));
    return this.httpClient.post(url, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }


  uploadFile(form: NgForm, index: number, isSchema: boolean,jsonFileToUpload: File): Observable<any> {
    const data: FormData = new FormData();
    let uploadData: UploadModel = new UploadModel();

    uploadData.inputType = form.value['inputType']
    uploadData.schemaType = form.value['schemaType'];
    uploadData.index = index;
    uploadData.schema = isSchema;
    uploadData.schemaVersion = this.schemaTypes.find((schType: { value: string; }) => schType.value === uploadData.schemaType).version;
    uploadData.dirName = uploadData.index + "_" + uploadData.schemaType;

    if (sessionStorage.getItem('token') === null) {
      sessionStorage.setItem('token', new Date().getTime().toString());
    }
    uploadData.timestamp = sessionStorage.getItem('token');

    data.append('postData', JSON.stringify(uploadData));

    if (isSchema) {
      if (uploadData.schemaType === 'custom') {
        data.append('file', jsonFileToUpload);
      }
    } else {
      data.append('file', jsonFileToUpload);
    }

    return this.httpClient.post(this.restEndPointService.uploadFile, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }

  replaceFile(replaceData: UploadModel) {
    const data: FormData = new FormData();

    data.append('postData', JSON.stringify(replaceData));
    return this.httpClient.post(this.restEndPointService.replaceFile, data)
      .pipe(catchError(this.handleError<any>('null'))
      );
  }

  private handleError<String>(operation = 'operation', result?: String) {
    return (error: any): Observable<String> => {
      this.log(`${operation} failed: ${error.message}`);

      return of(result as String);
    };
  }

  private log(message: string) {
    console.log(message);
  }


  initializeCycloneDXUndefinedObjects(inputJson: CycloneDXSBOMStandard) {
    this.cycloneDxModelToEdit = inputJson;
    if (!this.cycloneDxModelToEdit.bomFormat) {
      this.cycloneDxModelToEdit.bomFormat = "";
    }
    if (this.cycloneDxModelToEdit?.specVersion !== "1.4") {
      this.cycloneDxModelToEdit.specVersion = "1.0";
    }
    if (!this.cycloneDxModelToEdit.serialNumber) {
      this.cycloneDxModelToEdit.serialNumber = "";
    }
    if (!this.cycloneDxModelToEdit.version) {
      this.cycloneDxModelToEdit.version = 1;
    }

    if (!this.cycloneDxModelToEdit.metadata) {
      let metadata: BOMMetadata = new BOMMetadata();

      metadata.timestamp = "";

      let metadataComponent = new MetadataComponent();
      metadataComponent.name = "";
      metadataComponent.version = "";
      metadataComponent.purl = "";
      metadata.component = metadataComponent;

      let metadataLicense = new Licenses();
      let license = new License();
      license.name = "";
      metadataLicense.license = license;
      metadata.licenses.push(metadataLicense);

      let metadataSupplier = new Supplier();
      let contact = new OrganizationalContactObject();
      metadataSupplier.name = "";
      contact.email = "";
      metadataSupplier.contact.push(contact);
      metadata.supplier = metadataSupplier;

      let metadataTool = new Tool();
      metadataTool.name = "";
      metadataTool.vendor = "";
      metadataTool.version = "";
      let hash = new HashObjects();
      hash.alg = null;
      hash.content = "";
      metadataTool.hashes.push(hash);

      let externalReferences = new ExternalReference();
      externalReferences.url = "";
      externalReferences.comment = "";
      externalReferences.type = null;
      let exterHashes = new HashObjects();
      exterHashes.alg = null;
      exterHashes.content = "";
      externalReferences.hashes = [];
      externalReferences.hashes.push(exterHashes);
      metadataTool.externalReferences.push(externalReferences);
      metadata.tools.push(metadataTool);

      this.cycloneDxModelToEdit.metadata = metadata;


    } else {
      let metadataVar: BOMMetadata = new BOMMetadata();
      if (!this.cycloneDxModelToEdit.metadata.timestamp) {
        metadataVar.timestamp = "";
      } else {
        metadataVar.timestamp = this.cycloneDxModelToEdit.metadata.timestamp;
      }


      metadataVar.component = this.initializeCydxMetaDataComponents();
      if (!this.cycloneDxModelToEdit.metadata.licenses) {
        metadataVar.licenses.push(this.initializeCydxMetaDataLicense());
      } else {
        if (this.cycloneDxModelToEdit.metadata.licenses.length > 0) {
          for (var i = 0; i < this.cycloneDxModelToEdit.metadata.licenses.length; i++) {
            metadataVar.licenses.push(this.initMetadataLicenseUndefinedObj(this.cycloneDxModelToEdit.metadata.licenses[i]))
          }
        } else {
          metadataVar.licenses.push(this.initializeCydxMetaDataLicense());
        }
      }

      metadataVar.supplier = this.initializeCydxMetaDataSupplier();
      if (!this.cycloneDxModelToEdit.metadata.tools) {
        metadataVar.tools.push(this.initializeCydxMetaDataTools());
      } else {
        if (this.cycloneDxModelToEdit.metadata.tools.length > 0) {
          for (var i = 0; i < this.cycloneDxModelToEdit.metadata.tools.length; i++) {
            metadataVar.tools.push(this.initCydxMetaDataToolsUndefinedObj(this.cycloneDxModelToEdit.metadata.tools[i]));
          }
        } else {
          metadataVar.tools.push(this.initializeCydxMetaDataTools());
        }
      }
      if (this.cycloneDxModelToEdit.metadata.authors) {
        metadataVar.authors = this.cycloneDxModelToEdit.metadata.authors;
      }
      if (this.cycloneDxModelToEdit.metadata.manufacture) {
        metadataVar.manufacture = this.cycloneDxModelToEdit.metadata.manufacture;
      }
      if (this.cycloneDxModelToEdit.metadata.properties) {
        metadataVar.properties = this.cycloneDxModelToEdit.metadata.properties;
      }
      this.cycloneDxModelToEdit.metadata = metadataVar;

    }

    if (!this.cycloneDxModelToEdit.components) {
      this.cycloneDxModelToEdit.components = [];
      this.cycloneDxModelToEdit.components.push(this.initializeCydxComponents());
    } else {
      var noOfComponents = this.cycloneDxModelToEdit.components.length;
      if (noOfComponents === 0) {
        this.cycloneDxModelToEdit.components.push(this.initializeCydxComponents());
      } else {
        for (var i = 0; i < noOfComponents; i++) {
          this.cycloneDxModelToEdit.components[i] = this.initCydxUndefinedComponentsObj(this.cycloneDxModelToEdit.components[i]);
        }
      }

    }

    return this.cycloneDxModelToEdit;
  }

  initMetadataLicenseUndefinedObj(metaLicense: any) {
    let metadataLicense = new Licenses();
    let licenseData = new License();
    let text = new AttachmentText();
    metadataLicense = metaLicense;
    if (metadataLicense.license) {
      if (!metadataLicense.license.id) {
        if (!metadataLicense.license.name) {
        }
      }
      if (!metadataLicense.license.text) {
        text.content = "";
        text.contentType = "";
        text.encoding = "base64";
        metadataLicense.license.text = text;
      }
    } else {
      text.content = "";
      text.contentType = "";
      text.encoding = "base64";
      licenseData.text = text;
      metadataLicense.license = licenseData;
    }
    return metadataLicense;
  }


  initializeCydxComponents() {
    let components: ComponentObject = new ComponentObject();
    components.name = "";
    components.version = "";
    components.purl = "";
    components.copyright = "";

    let licenses: Licenses = new Licenses();
    let license: License = new License();
    licenses.license = license;
    components.licenses.push(licenses);
    return components;
  }

  initCydxUndefinedComponentsObj(components: any) {
    let componentsData = new ComponentObject();
    componentsData = components;
    if (!components.name) {
      componentsData.name = "";
    }
    if (!components.version) {
      componentsData.version = "";
    }
    if (!components.type) {
    }
    if (!components.purl) {
      componentsData.purl = "";
    }
    if (!components.copyright) {
      componentsData.copyright = "";
    }
    if (!components.licenses) {
      componentsData.licenses = [];
      let licenses = new Licenses();
      let license = new License();
      licenses.license = license;
      componentsData.licenses.push(licenses);
    } else {
      if (components.licenses.length > 0) {
        for (var j = 0; j < components.licenses.length; j++) {
          componentsData.licenses[j] = this.initComponentsLicenseUndefinedObj(components.licenses[j]);
        }
      } else {
        let licenses = new Licenses();
        let license = new License();
        licenses.license = license;
        componentsData.licenses.push(licenses);
      }
    }
    return componentsData;
  }

  initializeCydxMetaDataTools() {
    let metadataTool = new Tool();
    metadataTool.name = "";
    metadataTool.vendor = "";
    metadataTool.version = "";

    let hash = new HashObjects();
    hash.alg = null;
    hash.content = "";
    metadataTool.hashes.push(hash);
    let externalReferences = new ExternalReference();
    externalReferences.url = "";
    externalReferences.comment = "";
    externalReferences.type = null;

    let externalHash = new HashObjects();
    externalHash.alg = null;
    externalHash.content = "";
    externalReferences.hashes = [];
    externalReferences.hashes.push(externalHash);
    metadataTool.externalReferences.push(externalReferences);
    return metadataTool;
  }

  initCydxMetaDataToolsUndefinedObj(metaTools: Tool) {
    let metadataTool = new Tool();
    metadataTool = metaTools;
    if (!metaTools.name) {
      metadataTool.name = "";
    }
    if (!metaTools.version) {
      metadataTool.version = "";
    }
    if (!metaTools.vendor) {
      metadataTool.vendor = "";
    }
    if (!metaTools.hashes) {
      metadataTool.hashes = [];
      metadataTool.hashes.push(this.initializeCydxHashes());
    } else {
      for (var i = 0; i < metaTools.hashes.length; i++) {
        metadataTool.hashes[i] = this.initCydxHashesUndefinedObj(metaTools.hashes[i]);
      }
    }
    if (!metaTools.externalReferences) {
      metadataTool.externalReferences = [];
      metadataTool.externalReferences.push(this.initializeCydxExternalReference());
    } else {
      for (var i = 0; i < metaTools.externalReferences.length; i++) {
        metadataTool.externalReferences[i] = this.initCydxExternalReferenceUndefinedObj(metaTools.externalReferences[i]);
      }
    }
    return metadataTool;
  }

  initializeCydxHashes() {
    let hash = new HashObjects();
    hash.alg = null;
    hash.content = "";
    return hash;
  }

  initCydxHashesUndefinedObj(hashes: any) {
    let hash = new HashObjects();
    hash = hashes;
    if (!hash.alg) {
      hash.alg = null;
    }
    if (!hash.content) {
      hash.content = "";
    }
    return hash;
  }

  initComponentsLicenseUndefinedObj(componentLicense: any) {
    let cmpntlicense = new Licenses();
    let licenseData = new License();
    cmpntlicense = componentLicense;
    if (cmpntlicense.license) {
      if (!cmpntlicense.license.id) {
        if (!cmpntlicense.license.name) {
        }
      }
    } else {
      cmpntlicense.license = licenseData;
    }
    return cmpntlicense;
  }

  initializeCydxExternalReference() {
    let externalReferences = new ExternalReference();
    externalReferences.url = "";
    externalReferences.comment = "";
    externalReferences.type = null;

    let externalHash = new HashObjects();
    externalHash.alg = null;
    externalHash.content = "";
    externalReferences.hashes = [];
    externalReferences.hashes.push(externalHash);
    return externalReferences;
  }

  initCydxExternalReferenceUndefinedObj(externalRef: any) {
    let externalReferences = new ExternalReference();
    externalReferences = externalRef;
    if (!externalReferences.url) {
      externalReferences.url = "";
    }
    if (!externalReferences.type) {
      externalReferences.type = null;
    }
    if (!externalReferences.comment) {
      externalReferences.comment = "";
    }
    if (!externalReferences.hashes) {
      externalReferences.hashes = [];
      externalReferences.hashes.push(this.initializeCydxHashes());
    } else {
      for (var j = 0; j < externalReferences.hashes.length; j++) {
        externalReferences.hashes[j] = this.initCydxHashesUndefinedObj(externalReferences.hashes[j]);
      }
    }
    return externalReferences;
  }

  initCydxMetedataSupplierUndefinedObj(contact: any) {
    let contactObj = new OrganizationalContactObject();
    contactObj = contact;
    if (!contactObj.email) {
      contactObj.email = "";
    }
    return contactObj;
  }

  initializeCydxMetaDataSupplier() {
    let metadataSupplier = new Supplier();
    if (!this.cycloneDxModelToEdit.metadata.supplier) {
      metadataSupplier.name = "";
      let organizationalContactObject = new OrganizationalContactObject();
      organizationalContactObject.email = ""
      metadataSupplier.contact.push(organizationalContactObject);
    } else {
      if (!this.cycloneDxModelToEdit.metadata.supplier.name) {
        metadataSupplier.name = "";
      } else {
        metadataSupplier.name = this.cycloneDxModelToEdit.metadata.supplier.name;
      }
      if (this.cycloneDxModelToEdit.metadata.supplier.url) {
        metadataSupplier.url = this.cycloneDxModelToEdit.metadata.supplier.url;
      }
      if (!this.cycloneDxModelToEdit.metadata.supplier.contact) {
        let organizationalContactObject = new OrganizationalContactObject();
        organizationalContactObject.email = ""
        metadataSupplier.contact.push(organizationalContactObject);
      } else {
        for (var i = 0; i < this.cycloneDxModelToEdit.metadata.supplier.contact.length; i++) {
          metadataSupplier.contact.push(this.initCydxMetedataSupplierUndefinedObj(this.cycloneDxModelToEdit.metadata.supplier.contact[i]));
        }
      }
    }
    return metadataSupplier;
  }

  initializeCydxMetaDataLicense() {
    let metadataLicense = new Licenses();
    let license = new License();
    let text = new AttachmentText();
    text.content = "";
    text.contentType = "";
    text.encoding = "base64";
    license.text = text;
    metadataLicense.license = license;
    return metadataLicense;
  }

  initializeCydxMetaDataComponents() {
    let metadataComponent = new MetadataComponent();
    if (!this.cycloneDxModelToEdit.metadata.component) {
      metadataComponent.name = "";
      metadataComponent.version = "";
      metadataComponent.purl = "";
    } else {
      metadataComponent = this.cycloneDxModelToEdit.metadata.component;
      if (!this.cycloneDxModelToEdit.metadata.component.name) {
        metadataComponent.name = "";
      }
      if (!this.cycloneDxModelToEdit.metadata.component.type) {

      }
      if (!this.cycloneDxModelToEdit.metadata.component.version) {
        metadataComponent.version = "";
      }
      if (!this.cycloneDxModelToEdit.metadata.component.purl) {
        metadataComponent.purl = "";
      }
    }
    return metadataComponent;
  }

  initializeSpdxUndefinedObjects(inputJson: SpdxModel) {
    this.spdxModelToEdit = inputJson;

    if (!this.spdxModelToEdit.spdxVersion) {
      this.spdxModelToEdit.spdxVersion = "";
    }
    if (!this.spdxModelToEdit.documentNamespace) {
      this.spdxModelToEdit.documentNamespace = "";
    }
    if (!this.spdxModelToEdit.dataLicense) {
      this.spdxModelToEdit.dataLicense = "";
    }

    if (!this.spdxModelToEdit.creationInfo) {
      let creatInfo = new CreationInfo();
      creatInfo.created = "";
      creatInfo.creators = [""]
      this.spdxModelToEdit.creationInfo = creatInfo;
    } else {
      if (!this.spdxModelToEdit.creationInfo.created) {
        this.spdxModelToEdit.creationInfo.created = "";
      }
      if (!this.spdxModelToEdit.creationInfo.creators) {
        this.spdxModelToEdit.creationInfo.creators = [""];
      }
    }

    if (this.spdxModelToEdit.packages) {
      if (this.spdxModelToEdit.packages.length > 0) {
        for (var i = 0; i < this.spdxModelToEdit.packages.length; i++) {
          if (!this.spdxModelToEdit.packages[i].name) {
            this.spdxModelToEdit.packages[i].name = "";
          }
          if (!this.spdxModelToEdit.packages[i].versionInfo) {
            this.spdxModelToEdit.packages[i].versionInfo = "";
          }
          if (!this.spdxModelToEdit.packages[i].primaryPackagePurpose) {
            this.spdxModelToEdit.packages[i].primaryPackagePurpose = null;
          }
          if (!this.spdxModelToEdit.packages[i].licenseDeclared) {
            this.spdxModelToEdit.packages[i].licenseDeclared = "";
          }
          if (!this.spdxModelToEdit.packages[i].copyrightText) {
            this.spdxModelToEdit.packages[i].copyrightText = "";
          }
          if (!this.spdxModelToEdit.packages[i].externalRefs) {
            this.spdxModelToEdit.packages[i].externalRefs = this.initializeSpdxPackageExternalRef();
          } else {
            if (this.spdxModelToEdit.packages[i].externalRefs.length === 0) {
              this.spdxModelToEdit.packages[i].externalRefs = this.initializeSpdxPackageExternalRef();
            } else {
              for (var j = 0; j < this.spdxModelToEdit.packages[i].externalRefs.length; j++) {
                if (!this.spdxModelToEdit.packages[i].externalRefs[j].referenceCategory) {
                  this.spdxModelToEdit.packages[i].externalRefs[j].referenceCategory = null;
                }
                if (!this.spdxModelToEdit.packages[i].externalRefs[j].referenceLocator) {
                  this.spdxModelToEdit.packages[i].externalRefs[j].referenceLocator = "";
                }
                if (!this.spdxModelToEdit.packages[i].externalRefs[j].referenceType) {
                  this.spdxModelToEdit.packages[i].externalRefs[j].referenceType = "";
                }
                if (!this.spdxModelToEdit.packages[i].externalRefs[j].comment) {
                  this.spdxModelToEdit.packages[i].externalRefs[j].comment = "";
                }
              }
            }
          }
        }
      } else {
        this.spdxModelToEdit.packages = [];
        this.spdxModelToEdit.packages.push(this.initializeSpdxPackage());
      }
    } else {
      this.spdxModelToEdit.packages = [];
      this.spdxModelToEdit.packages.push(this.initializeSpdxPackage());
    }

    return this.spdxModelToEdit;
  }

  initializeSpdxPackage() {
    let packages: Packages = new Packages();
    packages.name = "";
    packages.versionInfo = "";
    packages.primaryPackagePurpose = null;
    packages.licenseDeclared = "";
    packages.copyrightText = "";

    let externalRef: externalRefs[] = [];
    let extern = new externalRefs();
    extern.comment = "";
    extern.referenceCategory = null;
    extern.referenceLocator = "";
    extern.referenceType = "";
    externalRef.push(extern);
    packages.externalRefs = externalRef;
    return packages;
  }

  initializeSpdxPackageExternalRef() {
    let externalRef: externalRefs[] = [];
    let extern = new externalRefs();
    extern.comment = "";
    extern.referenceCategory = null;
    extern.referenceLocator = "";
    extern.referenceType = "";
    externalRef.push(extern);
    return externalRef;
  }

  sbomTypes: any = [
    {
      "value": "upload",
      "label": "Upload Supplier SBOM"
    }
  ]

  fossidSchemaTypes: any = [
    {
      "value": "cyclonedx",
      "label": "CycloneDX V1.4",
      "version": "1.4"
    },
    {
      "value": "spdx2.2",
      "label": "SPDX V2.2",
      "version": "2.2"
    }
  ]

  schemaTypes: any = [
    {
      "value": "cyclonedx",
      "label": "CycloneDX V1.4",
      "version": "1.4"
    },
    {
      "value": "spdx",
      "label": "SPDX V2.3",
      "version": "2.3"
    },
    {
      "value": "custom",
      "label": "Custom Schema",
      "version": "1.0"
    },
    ,
    {
      "value": "spdx2.2",
      "label": "SPDX V2.2",
      "version": "2.2"
    }
  ]

  licenseInfoTypes: any = [
    {
      "value": "licId",
      "label": "License ID"
    },
    {
      "value": "licName",
      "label": "License Name"
    },
    {
      "value": "licText",
      "label": "License Text"
    }
  ]


  auditLogHeaders = [
    { title: "Action", dataKey: "action" },
    { title: "File Name", dataKey: "fileName" },
    { title: "File Hash", dataKey: "fileHash" },
    { title: "Timestamp", dataKey: "timestamp" }
  ];

  changeLogHeaders = [
    { title: "File Name", dataKey: "fileName" },
    { title: "Path", dataKey: "path" },
    { title: "Operation", dataKey: "op" },
    { title: "Value", dataKey: "value" }
  ];

  errorListHeaders = [
    { title: "Error Path", dataKey: "errorKey" },
    { title: "Error Message", dataKey: "message" }
  ];

  downloadParams = {
    header: [this.errorListHeaders, this.changeLogHeaders, this.auditLogHeaders, undefined],
    fileName: ['errorsList.pdf', 'changeLog.pdf', 'auditLog.pdf', 'mergedSbomContent.json']
  }
}
