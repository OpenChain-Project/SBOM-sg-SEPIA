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
import { CDQBOMMetadata, CDQCycloneDXSBOMStandard, CDQLicense, CDQMetadataComponent, CDQLicenses, CDQExternalReference, CDQOrganizationalEntityObject, CDQToolsChoice, CDQComponentObject, CDQOrganizationalContactObject } from '../models/cdqcyclonedx.model';


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
  public cdqCycloneDxModelToEdit: CDQCycloneDXSBOMStandard = new CDQCycloneDXSBOMStandard();

  public cdqSpdxModelToEdit: SpdxModel = new SpdxModel();

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
      item.sessionId = sessionStorage.getItem('token');
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
    item.sessionId = sessionStorage.getItem('token');

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



  mergeSboms(uploadModels: UploadModel[], cdxMerged: CycloneDXSBOMStandard, spdxMerged: SpdxModel,cdqSpdxMerged: SpdxModel,cdqCydxMerged: CDQCycloneDXSBOMStandard, mergeType: any): Observable<any> {

    const data: FormData = new FormData();
    let url: string = this.restEndPointService.mergeSpdx;
    data.append('postData', JSON.stringify(uploadModels));
    if(mergeType === 'cyclonedx') {
      url = this.restEndPointService.mergeCyclonedx;
      data.append('bomMetadata', JSON.stringify(cdxMerged));
    }
    else if (mergeType === 'cdqcydx') {
      url = this.restEndPointService.mergeCyclonedx;
      data.append('bomMetadata', JSON.stringify(cdqCydxMerged));
    } else if (mergeType === 'cdqspdx2.3') {
      data.append('bomMetadata', JSON.stringify(cdqSpdxMerged));
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
    uploadData.sessionId = sessionStorage.getItem('token');

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

  initializeCDQCycloneDXUndefinedObjects(inputJson: CDQCycloneDXSBOMStandard) {
     this.cdqCycloneDxModelToEdit = inputJson;
    if (!this.cdqCycloneDxModelToEdit.bomFormat) {
      this.cdqCycloneDxModelToEdit.bomFormat = "";
    }
    if (this.cdqCycloneDxModelToEdit?.specVersion !== "1.6") {
      this.cdqCycloneDxModelToEdit.specVersion = "1.6";
    }

    if (!this.cdqCycloneDxModelToEdit.serialNumber) {
      this.cdqCycloneDxModelToEdit.serialNumber = "";
    }
    if (!this.cdqCycloneDxModelToEdit.version) {
      this.cdqCycloneDxModelToEdit.version = 1;
    }

    if (!this.cdqCycloneDxModelToEdit.metadata) {
        let cdqmetadata: CDQBOMMetadata = new CDQBOMMetadata();
        cdqmetadata.timestamp = "";

        let cdqmetadataComponent = new CDQMetadataComponent();
        cdqmetadataComponent.name = "";
        cdqmetadataComponent.group = "";
        cdqmetadataComponent.type = null;
        cdqmetadataComponent.version = "";
        cdqmetadataComponent.purl = "";

        let cdqMetaComponentLicense = new CDQLicenses();
        let cdqLicense = new CDQLicense();
        cdqLicense.name = "";
        cdqMetaComponentLicense.license = cdqLicense;
        cdqmetadataComponent.licenses = [];
        cdqmetadataComponent.licenses.push(cdqMetaComponentLicense);

        let cdqMetaComponentExternalReference = new CDQExternalReference();
        cdqMetaComponentExternalReference.url = "";
        cdqmetadataComponent.externalReferences = []
        cdqmetadataComponent.externalReferences.push(cdqMetaComponentExternalReference);
        cdqmetadata.component = cdqmetadataComponent;

        let cdqmetadataSupplier = new CDQOrganizationalEntityObject();
        cdqmetadataSupplier.name = "";

        let contact = new OrganizationalContactObject();
        contact.email = "";
        cdqmetadataSupplier.contact = [];
        cdqmetadataSupplier.contact.push(contact);
        cdqmetadata.supplier = cdqmetadataSupplier;

        let cdqMetaLicenses = new CDQLicenses();
        let cdqMetaLicense = new CDQLicense();
        cdqMetaLicense.name = "";
        cdqMetaLicenses.license = cdqMetaLicense;
        cdqmetadata.licenses.push(cdqMetaLicenses);

        let cdqmetadataTool = new CDQToolsChoice();
        let cdqmetadataToolComponent = new CDQComponentObject();
        cdqmetadataToolComponent.name = "";
        cdqmetadataTool.components = [];
        cdqmetadataTool.components.push(cdqmetadataToolComponent);
        cdqmetadata.tools = cdqmetadataTool;

        let cdqMetadataAuthors = new CDQOrganizationalContactObject();
        cdqMetadataAuthors.name = "";
        cdqmetadata.authors = [];
        cdqmetadata.authors.push(cdqMetadataAuthors);

        // let cdqMetadataManufacturer = new CDQOrganizationalEntityObject();
        // cdqMetadataManufacturer.name = "";

        // cdqmetadata.manufacturer = cdqMetadataManufacturer;
        this.cdqCycloneDxModelToEdit.metadata = cdqmetadata;
    }else {
      let cdqmetadataVar: CDQBOMMetadata = new CDQBOMMetadata();
      cdqmetadataVar = this.cdqCycloneDxModelToEdit.metadata;
      if (!this.cdqCycloneDxModelToEdit.metadata.timestamp) {
        cdqmetadataVar.timestamp = "";
      }else{
        cdqmetadataVar.timestamp = this.cdqCycloneDxModelToEdit.metadata.timestamp;
      }
      cdqmetadataVar.component = this.initializeCDQCydxMetaDataComponents();
      if (!this.cdqCycloneDxModelToEdit.metadata.licenses) {
        cdqmetadataVar.licenses = [];
        cdqmetadataVar.licenses.push(this.initializeCDQCydxMetaDataLicense());
      } else {
        if (this.cdqCycloneDxModelToEdit.metadata.licenses.length > 0) {
          for (var i = 0; i < this.cdqCycloneDxModelToEdit.metadata.licenses.length; i++) {
            cdqmetadataVar.licenses[i] = this.initCDQMetadataLicenseUndefinedObj(this.cdqCycloneDxModelToEdit.metadata.licenses[i]);
            //cdqmetadataVar.licenses.push((this.cdqCycloneDxModelToEdit.metadata.licenses[i]))
          }
        }else {
          cdqmetadataVar.licenses.push(this.initializeCDQCydxMetaDataLicense());
        }
      }
       cdqmetadataVar.supplier = this.initializeCDQCydxMetaDataSupplier();
      if (!this.cdqCycloneDxModelToEdit.metadata.tools) {
        cdqmetadataVar.tools = this.initializeCDQCydxMetaDataTools();
      }else {
        if (!this.cdqCycloneDxModelToEdit.metadata.tools.components) {
          cdqmetadataVar.tools = this.initCDQCydxMetaDataToolsUndefinedObj(this.cdqCycloneDxModelToEdit.metadata.tools);
        } else {
          cdqmetadataVar.tools = this.initCDQCydxMetaDataToolsUndefinedObj(this.cdqCycloneDxModelToEdit.metadata.tools);
        }
      }
      if (!this.cdqCycloneDxModelToEdit.metadata.manufacturer) {
        if (!this.cdqCycloneDxModelToEdit.metadata.authors) {
            let cdqMetadataManufacturer = new CDQOrganizationalEntityObject();
            cdqMetadataManufacturer.name = "";
            cdqmetadataVar.manufacturer = cdqMetadataManufacturer;
        }
      }else {
        cdqmetadataVar.manufacturer = this.cdqCycloneDxModelToEdit.metadata.manufacturer;
        if (!this.cdqCycloneDxModelToEdit.metadata.manufacturer.name) {
          cdqmetadataVar.manufacturer.name = "";
        }
      }
      if (!this.cdqCycloneDxModelToEdit.metadata.authors) {
        if (!this.cdqCycloneDxModelToEdit.metadata.manufacturer) {
           let cdqMetadataAuthors = new CDQOrganizationalContactObject();
            cdqMetadataAuthors.name = "";
            cdqmetadataVar.authors = [];
            cdqmetadataVar.authors.push(cdqMetadataAuthors);
        }
      }else {
        cdqmetadataVar.authors = this.initCDQMetadataAuthorsUndefinedObj(this.cdqCycloneDxModelToEdit.metadata.authors);
      }
        this.cdqCycloneDxModelToEdit.metadata = cdqmetadataVar;
    }
    if (!this.cdqCycloneDxModelToEdit.components) {
      this.cdqCycloneDxModelToEdit.components = [];
      this.cdqCycloneDxModelToEdit.components.push(this.initializeCDQCydxComponents());
    }else {
      this.cdqCycloneDxModelToEdit.components = this.initializeCDQCydxComponentsUndefinedObj(this.cdqCycloneDxModelToEdit.components);
    }
    return this.cdqCycloneDxModelToEdit;
  }

  initializeCDQCydxComponentsUndefinedObj(components: any) {
    let componentsData: CDQComponentObject = new CDQComponentObject();
    if (components.length > 0) {
      for (var i = 0; i < components.length; i++) {
        componentsData = components[i];
          if (!componentsData.name) {
            componentsData.name = "";
          }
          if (!componentsData.version) {
            componentsData.version = "";
          }
          if (!componentsData.purl) {
            componentsData.purl = "";
          }
        components[i] = componentsData;
     }
    }else {
      let componentData: CDQComponentObject = new CDQComponentObject();
      componentData.name = "";
      componentData.version = "";
      componentData.purl = "";
      componentData.cpe = "";
      components.push(componentData);
    }

    return components;
  }

  initializeCDQCydxComponents() {
    let components: CDQComponentObject = new CDQComponentObject();
    components.name = "";
    components.version = "";
    components.purl = "";
    return components;
  }

  initCDQMetadataAuthorsUndefinedObj(authors: CDQOrganizationalContactObject[]) {
   if(authors.length > 0) {
    for (var i = 0; i < authors.length; i++) {
      if (!authors[i].name) {
        authors[i].name = "";
      }
    }
   }else{
    let cdqMetadataAuthors = new CDQOrganizationalContactObject();
    cdqMetadataAuthors.name = "";
    authors.push(cdqMetadataAuthors);
   }
    return authors;
  }

  initCDQCydxMetaDataToolsUndefinedObj(metaTools: CDQToolsChoice) {
    let metadataTool = new CDQToolsChoice();
    metadataTool = metaTools;
      if (!metaTools.components) {
      let metadataToolComponent = new CDQComponentObject();
      metadataToolComponent.name = "";
      metadataTool.components = [];
      metadataTool.components.push(metadataToolComponent);
      } else {
        if(metaTools.components.length > 0) {
          for (var i = 0; i < metaTools.components.length; i++) {
            if (!metaTools.components[i].name) {
              metaTools.components[i].name = "";
            }
          }
        metadataTool.components = metaTools.components;
        }else{
          let metadataToolComponent = new CDQComponentObject();
          metadataToolComponent.name = "";
          metadataTool.components = [];
          metadataTool.components.push(metadataToolComponent);
        }
      }
    return metadataTool;
  }

  initializeCDQCydxMetaDataTools() {
    let metadataTool = new CDQToolsChoice();
    let metadataToolComponent = new CDQComponentObject();
    metadataToolComponent.name = "";
    metadataTool.components = [];
    metadataTool.components.push(metadataToolComponent);
    return metadataTool;
  }

  initializeCDQCydxMetaDataSupplier() {
    let metadataSupplier = new CDQOrganizationalEntityObject();
    if (!this.cdqCycloneDxModelToEdit.metadata.supplier) {
      metadataSupplier.name = "";
      let organizationalContactObject = new CDQOrganizationalContactObject();
      organizationalContactObject.email = ""
      metadataSupplier.contact = [];
      metadataSupplier.contact.push(organizationalContactObject);
    }else {
      metadataSupplier = this.cdqCycloneDxModelToEdit.metadata.supplier;
      if (!this.cdqCycloneDxModelToEdit.metadata.supplier.name) {
        metadataSupplier.name = "";
      } else {
        metadataSupplier.name = this.cdqCycloneDxModelToEdit.metadata.supplier.name;
      }

      if (!this.cdqCycloneDxModelToEdit.metadata.supplier.contact) {
        let organizationalContactObject = new CDQOrganizationalContactObject();
        organizationalContactObject.email = "";
        metadataSupplier.contact = [];
        metadataSupplier.contact.push(organizationalContactObject);
      }else {
        var noOfcontact = this.cdqCycloneDxModelToEdit.metadata.supplier.contact.length;
        for (var i = 0; i < noOfcontact; i++) {
          metadataSupplier.contact[i] = this.initCDQCydxMetedataSupplierUndefinedObj(this.cdqCycloneDxModelToEdit.metadata.supplier.contact[i]);
        }
      }
    }
    return metadataSupplier;
  }

  initializeCDQCydxMetaDataComponents() {
    let metadataComponent = new CDQMetadataComponent();
    if(!this.cdqCycloneDxModelToEdit.metadata.component) {
      metadataComponent.name = "";
      metadataComponent.group = "";
      metadataComponent.type = null;
      metadataComponent.version = "";
      metadataComponent.purl = "";

      let cdqMetaComponentLicense = new CDQLicenses();
      let cdqLicense = new CDQLicense();
      cdqLicense.name = "";
      cdqMetaComponentLicense.license = cdqLicense;

      metadataComponent.licenses = [];
      metadataComponent.licenses.push(cdqMetaComponentLicense);

      let cdqMetaComponentExternalReference = new CDQExternalReference();
      cdqMetaComponentExternalReference.url = "";
      metadataComponent.externalReferences = []
      metadataComponent.externalReferences.push(cdqMetaComponentExternalReference);
    } else {
      metadataComponent = this.cdqCycloneDxModelToEdit.metadata.component;
        if (!metadataComponent.name) {
          metadataComponent.name = "";
        }
        if (!metadataComponent.version) {
          metadataComponent.version = "";
        }
        if (!metadataComponent.purl) {
          metadataComponent.purl = "";
        }
        if (!metadataComponent.group) {
          metadataComponent.group = "";
        }
        if (!metadataComponent.type) {
           metadataComponent.type = null;
        }
        if (!metadataComponent.licenses) {
          let cdqMetaComponentLicense = new CDQLicenses();
          let cdqLicense = new CDQLicense();
          cdqLicense.name = "";
          cdqMetaComponentLicense.license = cdqLicense;
          metadataComponent.licenses = [];
          metadataComponent.licenses.push(cdqMetaComponentLicense);
        }else {
          if (metadataComponent.licenses.length > 0) {
            for (var j = 0; j < metadataComponent.licenses.length; j++) {
              if(!metadataComponent.licenses[j].expression) {
                metadataComponent.licenses[j] = this.initCDQMetadataLicenseUndefinedObj(metadataComponent.licenses[j]);
              }
            }
          }else {
            let cdqMetaComponentLicense = new CDQLicenses();
            let cdqLicense = new CDQLicense();
            cdqLicense.name = "";
            cdqMetaComponentLicense.license = cdqLicense;
            metadataComponent.licenses = [];
            metadataComponent.licenses.push(cdqMetaComponentLicense);
          }
        }
        if (!metadataComponent.externalReferences) {
          let cdqMetaComponentExternalReference = new CDQExternalReference();
          cdqMetaComponentExternalReference.url = "";
          metadataComponent.externalReferences = []
          metadataComponent.externalReferences.push(cdqMetaComponentExternalReference);
        } else {
          if (metadataComponent.externalReferences.length > 0) {
            for (var j = 0; j < metadataComponent.externalReferences.length; j++) {
              if (!metadataComponent.externalReferences[j].url) {
                metadataComponent.externalReferences[j].url = "";
              }
            }
          }else {
            let cdqMetaComponentExternalReference = new CDQExternalReference();
            cdqMetaComponentExternalReference.url = "";
            metadataComponent.externalReferences = []
            metadataComponent.externalReferences.push(cdqMetaComponentExternalReference);
          }
        }
    }
    return metadataComponent;
  }

  initializeCDQCydxMetaDataLicense() {
    let cdqmetadataLicense = new CDQLicenses();
    let cdqLicense = new CDQLicense();
    cdqLicense.name = "";
    cdqmetadataLicense.license = cdqLicense;
    return cdqmetadataLicense;

  }

  initCDQMetadataLicenseUndefinedObj(metaLicense: any) {
    let metadataLicense = new CDQLicenses();
    let licenseData = new CDQLicense();
    metadataLicense = metaLicense;
    if (metadataLicense.license) {
        if(!metadataLicense.license.id) {
          if (!metadataLicense.license.name) {
            metadataLicense.license.name = "";
          }
        }
    } else {
      licenseData.name = "";
      metadataLicense.license = licenseData;
    }
    return metadataLicense;
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

    initCDQCydxMetedataSupplierUndefinedObj(contact: OrganizationalContactObject) {
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

  initializeCDQSpdxUndefinedObjects(inputJson: SpdxModel) {
    this.cdqSpdxModelToEdit = inputJson;
    if (!this.cdqSpdxModelToEdit.spdxVersion) {
      this.cdqSpdxModelToEdit.spdxVersion = "";
    }
    if (!this.cdqSpdxModelToEdit.documentNamespace) {
      this.cdqSpdxModelToEdit.documentNamespace = "";
    }
    if (!this.cdqSpdxModelToEdit.dataLicense) {
      this.cdqSpdxModelToEdit.dataLicense = "";
    }
    if (!this.cdqSpdxModelToEdit.creationInfo) {
        let creatInfo = new CreationInfo();
        creatInfo.created = "";
        creatInfo.creators = [""]
        this.cdqSpdxModelToEdit.creationInfo = creatInfo;
    } else {
        if (!this.cdqSpdxModelToEdit.creationInfo.created) {
          this.cdqSpdxModelToEdit.creationInfo.created = "";
        } if (!this.cdqSpdxModelToEdit.creationInfo.creators) {
          this.cdqSpdxModelToEdit.creationInfo.creators = [""];
        }
    }

    if(this.cdqSpdxModelToEdit.packages) {
      if(this.cdqSpdxModelToEdit.packages.length > 0) {
        for(var i=0; i<this.cdqSpdxModelToEdit.packages.length; i++) {
          if(!this.cdqSpdxModelToEdit.packages[i].name) {
            this.cdqSpdxModelToEdit.packages[i].name = "";
          }
          if (!this.cdqSpdxModelToEdit.packages[i].versionInfo) {
            this.cdqSpdxModelToEdit.packages[i].versionInfo = "";
          }
          if (!this.cdqSpdxModelToEdit.packages[i].primaryPackagePurpose) {
            this.cdqSpdxModelToEdit.packages[i].primaryPackagePurpose = null;
          }
          if (!this.cdqSpdxModelToEdit.packages[i].licenseConcluded) {
            this.cdqSpdxModelToEdit.packages[i].licenseConcluded = "";
          }
          if (!this.cdqSpdxModelToEdit.packages[i].externalRefs) {
            this.cdqSpdxModelToEdit.packages[i].externalRefs = this.initializeCDQSpdxPackageExternalRef();
          } else {
            if (this.cdqSpdxModelToEdit.packages[i].externalRefs.length === 0) {
              this.cdqSpdxModelToEdit.packages[i].externalRefs = this.initializeCDQSpdxPackageExternalRef();
            } else {
              for (var j = 0; j < this.cdqSpdxModelToEdit.packages[i].externalRefs.length; j++) {
                if (!this.cdqSpdxModelToEdit.packages[i].externalRefs[j].referenceCategory) {
                  this.cdqSpdxModelToEdit.packages[i].externalRefs[j].referenceCategory = null;
                }
              }
            }
          }

        }
      } else {
        this.cdqSpdxModelToEdit.packages = [];
        this.cdqSpdxModelToEdit.packages.push(this.initializeCDQSpdxPackage());
      }
    } else {
      this.cdqSpdxModelToEdit.packages = [];
      this.cdqSpdxModelToEdit.packages.push(this.initializeCDQSpdxPackage());
    }
    return this.cdqSpdxModelToEdit;
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

    initializeCDQSpdxPackage() {
    let packages: Packages = new Packages();
    packages.name = "";
    packages.versionInfo = "";
    packages.primaryPackagePurpose = null;
    packages.licenseConcluded = "";

    let externalRef: externalRefs[] = [];
    let extern = new externalRefs();
    extern.referenceCategory = null;
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

    initializeCDQSpdxPackageExternalRef() {
    let externalRef: externalRefs[] = [];
    let extern = new externalRefs();
    extern.referenceCategory = null;
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
    {
      "value": "spdx2.2",
      "label": "SPDX V2.2",
      "version": "2.2"
    },
    {
      "value": "cdqspdx2.3",
      "label": "SEPIA - SPDX V2.3",
      "version": "2.3"
    },
    {
      "value": "cdqcydx",
      "label": "SEPIA - CycloneDX V1.6",
      "version": "1.6"
    },
    {
      "value": "automotivecydx1.6",
      "label": "Automotive - CycloneDX V1.6",
      "version": "1.6"
    },
    {
      "value": "automotivespdx2.3",
      "label": "Automotive - SPDX V2.3",
      "version": "2.3"
    },
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

    cdqLicenseInfoTypes: any = [
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
      "label": "License Expression"
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

  lossEventListHeaders = [
    { title: "Rule Id", dataKey: "ruleId" },
    { title: "Severity", dataKey: "severity" },
    { title: "Kind", dataKey: "kind" },
    { title: "Source Path", dataKey: "sourcePath" },
    { title: "Source Value", dataKey: "sourceValue" },
    { title: "Target Path", dataKey: "targetPath" },
    { title: "Target Value", dataKey: "targetValue" },
    { title: "Reason", dataKey: "reason" }
  ];

  logDetails = [
    { title: "Source Path", dataKey: "sourcePath" },
    { title: "Target Path", dataKey: "targetPath" },
    { title: "Value", dataKey: "value" },
    { title: "Category", dataKey: "category" },
    { title: "Reason", dataKey: "reason" },
    { title: "Rule ID", dataKey: "ruleId" }
  ];

  downloadParams = {
    header: [this.errorListHeaders, this.changeLogHeaders, this.auditLogHeaders,this.lossEventListHeaders,this.logDetails, undefined],
    fileName: ['errorsList.pdf', 'changeLog.pdf', 'auditLog.pdf', 'lossEventList.pdf', 'conversionLogDetails.pdf', 'SbomContent.json']
  }
}
