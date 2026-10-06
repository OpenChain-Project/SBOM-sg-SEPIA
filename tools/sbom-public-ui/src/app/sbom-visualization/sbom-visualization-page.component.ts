/*
 Parts of this file are created by genAI by using GitHub Copilot.
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

import { Component } from '@angular/core';

@Component({
  selector: 'app-sbom-visualization-page',
  templateUrl: './sbom-visualization.component.html',
  styleUrls: ['./sbom-visualization.component.css']
})
export class SbomVisualizationPageComponent {
  // Placeholders required because the existing view uses AngularJS-style
  // template variables (e.g., ng-repeat="s in ...", ng-repeat="m in ...").
  s = { key: '', label: '' };
  m = { id: '', source: '', section: '', target: '' };
  type = '';
  side = '';
  path = '';
  key = '';
  el: unknown = null;

  // Minimal view model to safely render the existing template without runtime errors.
  vm = {
    selectedType: 'spdx',
    searchQuery: '',
    filterSection: 'all',
    drawerOpen: false,
    sectionOptions: [
      { key: 'all', label: 'All' },
      { key: 'document', label: 'Document' },
      { key: 'package', label: 'Package' },
      { key: 'file', label: 'File' }
    ],
    isLoaded: false,
    loadError: '',
    sourceLabel: 'SPDX 2.3',
    targetLabel: 'CycloneDX 1.6',
    sourceRootPath: 'document',
    targetRootPath: 'bom',
    sourceBom: {},
    convertedBom: {},
    stats: {
      sourceFields: 0,
      targetFields: 0
    },
    activeMappings: [] as any[],
    filteredMappings: [] as any[],
    visibleMappings: [] as any[],
    activeMappingIds: [] as any[],
    highlightedPaths: {
      left: [] as any[],
      right: [] as any[]
    },
    targetExpandPaths: [] as any[],
    setType: (_type: string) => {},
    setFilter: (_key: string) => {},
    reset: () => {},
    onNodeEvent: (
      _type: string,
      _side: string,
      _path: string,
      _key: string,
      _el: unknown
    ) => {},
    selectMapping: (_id: number) => {},
    hoverMapping: (_id: number) => {},
    clearMappingHover: () => {},
    sectionClass: (_section: string) => ''
  };
}
