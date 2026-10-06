/*
 Parts of this file are created by genAI by using GitHub Copilot.
 This notice needs to remain attached to any reproduction of or excerpt from this file.
// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
// SPDX-License-Identifier: MIT
*/
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { forkJoin } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SbomVisualizationService {
  constructor(private http: HttpClient) {}

  loadResources(basePath?: string): Observable<any> {
    const base = basePath || 'resources';

    return forkJoin([
      this.http.get(base + '/spdx-2.3.sample.json'),
      this.http.get(base + '/cyclonedx-1.6.sample.json'),
      this.http.get(base + '/spdx-to-cyclonedx.mapping.json'),
      this.http.get(base + '/cyclonedx-to-spdx.mapping.json')
    ]).pipe(
      map(r => {
        return {
          spdx: r[0],
          cyclonedx: r[1],
          spdxToCyclonedx: r[2],
          cyclonedxToSpdx: r[3]
        };
      })
    );
  }
}
