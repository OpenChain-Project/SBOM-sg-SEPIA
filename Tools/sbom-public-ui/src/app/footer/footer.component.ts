/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: MIT */
import { Component, OnInit } from '@angular/core';
import { SbomInputService } from '../services/sbom-input.service';
import { RestEndpointsService } from '../services/rest-endpoints.service';
@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {


  constructor(public sbomInputService: SbomInputService,public restEndPointService:RestEndpointsService) { }

  ngOnInit(): void {
  }

  downloadUserManual() {
    const url = this.restEndPointService.downloadUserManual; // Replace with your actual URL
    window.open(url, '_blank');
  }

}
