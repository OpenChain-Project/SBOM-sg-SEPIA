/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class RestEndpointsService {

  constructor() { }


  private endpoint = 'http://localhost:9051/';



  public uploadFile: string = this.endpoint + 'uploadInputFile';
  public validateFiles: string = this.endpoint + 'validateSboms';
  public deleteSbomEntry: string = this.endpoint + 'deleteSbomEntry';
  public clearSession: string = this.endpoint + 'clearSession';
  public fetchErrorDetails: string = this.endpoint + 'fetchErrorDetails';
  public fetchJsonContent: string = this.endpoint + 'fetchJsonContent';
  public mergeCyclonedx: string = this.endpoint + 'mergeCyclonedx';
  public mergeSpdx: string = this.endpoint + 'mergeSpdx';
  public replaceFile: string = this.endpoint + 'replaceFile';
  public getJsonDifferences: string = this.endpoint + 'getJsonDifferences';
  public prepareForDownload: string = this.endpoint + 'prepareForDownload';
  public convertSbom: string = this.endpoint + 'convertSbom';
  public fetchSbomFromFossid: string = this.endpoint + 'fetchSbomFromFossid';
  public healthCheckup: string = this.endpoint + 'health';
  public downloadUserManual: string = this.endpoint + 'downloadUserManual';
}

