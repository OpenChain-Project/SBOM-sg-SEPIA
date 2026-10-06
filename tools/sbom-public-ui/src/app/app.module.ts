/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: MIT */
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { FooterComponent } from './footer/footer.component';
import { HeaderComponent } from './header/header.component';
import { SbomInputComponent } from './sbom-input/sbom-input.component';
import { SbomVisualizationPageComponent } from './sbom-visualization/sbom-visualization-page.component';
import { SbomVisualizationComponent } from './sbom-visualization/sbom-visualization.component';
//import { DataTablesModule } from 'angular-datatables';
import { HttpClientModule } from '@angular/common/http';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { NgJsonEditorModule } from 'ang-jsoneditor'

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    SbomInputComponent,
    SbomVisualizationPageComponent,
    SbomVisualizationComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    NgxJsonViewerModule,
    TabsModule.forRoot(),
    NgJsonEditorModule

  ],
  providers: [],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
  bootstrap: [AppComponent]
})
export class AppModule { }
