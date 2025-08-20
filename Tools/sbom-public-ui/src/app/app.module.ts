/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { FooterComponent } from './footer/footer.component';
import { HeaderComponent } from './header/header.component';
import { SbomInputComponent } from './sbom-input/sbom-input.component';
import { DataTablesModule } from 'angular-datatables';
import { HttpClientModule } from '@angular/common/http';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { NgJsonEditorModule } from 'ang-jsoneditor'

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    SbomInputComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    DataTablesModule,
    FormsModule,
    HttpClientModule,
    NgxJsonViewerModule,
    TabsModule.forRoot(),
    NgJsonEditorModule

  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
