/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: MIT */
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SbomInputComponent } from './sbom-input/sbom-input.component';
import { SbomVisualizationComponent } from './sbom-visualization/sbom-visualization.component';

const routes: Routes = [
  { path: '', component: SbomInputComponent },
  { path: 'sbom-visualization', component: SbomVisualizationComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
