/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SbomInputComponent } from './sbom-input/sbom-input.component';

const routes: Routes = [
  { path: '', component: SbomInputComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
