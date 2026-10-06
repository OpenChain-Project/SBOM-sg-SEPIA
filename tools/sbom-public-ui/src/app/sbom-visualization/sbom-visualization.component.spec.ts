/*
 Parts of this file are created by genAI by using GitHub Copilot.
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

describe("sbomVisualization component", function () {
  beforeEach(module("sbomVisualizationModule"));

  it("should compile component", inject(function ($compile, $rootScope) {
    var scope = $rootScope.$new();
    var element = $compile("<sbom-visualization></sbom-visualization>")(scope);
    scope.$digest();
    expect(element.length).toBe(1);
  }));
});
