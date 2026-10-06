/*
 Parts of this file are created by genAI by using GitHub Copilot.
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

declare var angular: any;
declare var d3: any;
import { Component, AfterViewInit, ElementRef, ViewEncapsulation } from '@angular/core';

/* eslint-disable no-var, prefer-const */
/* eslint-disable sonarjs/cognitive-complexity, sonarjs/no-nested-functions, sonarjs/prefer-optional-chain */

@Component({
  selector: 'app-sbom-visualization',
  templateUrl: './sbom-visualization.component.html',
  styleUrls: ['./sbom-visualization.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class SbomVisualizationComponent implements AfterViewInit {

  /* ╔════════════════════════════════════════════════════════════════════════════════════════════════════════════
     ║  STATE & VIEW MODEL
     ╚════════════════════════════════════════════════════════════════════════════════════════════════════════════ */

  isLoaded = false;
  loadError = "";
  selectedType = "spdx";
  searchQuery = "";
  filterSection = "all";
  drawerOpen = false;

  samples: any = {};
  rules: any = {};
  activeMappings: any[] = [];
  filteredMappings: any[] = [];
  visibleMappings: any[] = [];
  activeMappingIds: any[] = [];
  lockedMappingIds: any[] = [];
  hoverMappingIds: any[] = [];
  highlightedPaths: any = { left: [], right: [] };
  targetExpandPaths: any[] = [];

  sourceLabel = "";
  targetLabel = "";
  sourceRootPath = "document";
  targetRootPath = "bom";
  sourceBom: any = null;
  convertedBom: any = null;
  stats: any = { sourceFields: 0, targetFields: 0 };

  sectionOptions = [
    { key: "all",      label: "All"      },
    { key: "document", label: "Document" },
    { key: "package",  label: "Package"  },
    { key: "file",     label: "File"     }
  ];

  /* Stubs required for Angular template compilation.
     At runtime the AngularJS ng-controller and ng-repeat directives own these. */
  vm: any = {};
  s: any = {};
  m: any = {};
  type: any = '';
  side: any = '';
  path: any = '';
  key: any = '';
  el: any = null;

  /* Node registry for shared DOM-element lookup */
  private nodeRegistry: any = { left: {}, right: {} };

  constructor(private hostEl: ElementRef) {}

  ngAfterViewInit(): void {
    /* Guard: skip if AngularJS already compiled this element */
    if (angular.element(this.hostEl.nativeElement).injector()) return;
    const self = this;

    angular.module('sbomVisualizationModule', [])

      /* ── NodeRegistry ── */
      .service('NodeRegistry', function (this: any) {
        const reg: any = { left: {}, right: {} };
        this.set = (side: string, path: string, nodeEl: any) => { reg[side][path] = nodeEl; };
        this.getAll = (side: string) => reg[side] || {};
        this.clear = (side: string) => { reg[side] = {}; };
      })

      /* ── MainController ── */
      .controller('MainController', ['$http', '$q', 'NodeRegistry',
        function (this: any, $http: any, $q: any, NodeRegistry: any) {
          var vm = this;

          vm.isLoaded   = false;
          vm.loadError  = '';
          vm.selectedType  = 'spdx';
          vm.searchQuery   = '';
          vm.filterSection = 'all';
          vm.drawerOpen    = false;

          vm.samples = {};
          vm.rules   = {};
          vm.activeMappings   = [];
          vm.filteredMappings = [];
          vm.visibleMappings  = [];
          vm.activeMappingIds  = [];
          vm.lockedMappingIds  = [];
          vm.hoverMappingIds   = [];
          vm.highlightedPaths  = { left: [], right: [] };
          vm.targetExpandPaths = [];

          vm.sourceLabel    = '';
          vm.targetLabel    = '';
          vm.sourceRootPath = 'document';
          vm.targetRootPath = 'bom';
          vm.sourceBom    = null;
          vm.convertedBom = null;
          vm.stats = { sourceFields: 0, targetFields: 0 };

          vm.sectionOptions = [
            { key: 'all',      label: 'All'      },
            { key: 'document', label: 'Document' },
            { key: 'package',  label: 'Package'  },
            { key: 'file',     label: 'File'     }
          ];

          vm.setType           = setType;
          vm.setFilter         = setFilter;
          vm.reset             = reset;
          vm.selectMapping     = selectMapping;
          vm.hoverMapping      = hoverMapping;
          vm.clearMappingHover = clearMappingHover;
          vm.onNodeEvent       = onNodeEvent;
          vm.sectionClass      = (section: string) =>
            (section || 'other').toLowerCase().replace(/[^a-z0-9]/g, '-');

          init();

          function init() {
            $q.all([
              $http.get('resources/spdx-2.3.sample.json'),
              $http.get('resources/cyclonedx-1.6.sample.json'),
              $http.get('resources/spdx-to-cyclonedx.mapping.json'),
              $http.get('resources/cyclonedx-to-spdx.mapping.json')
            ]).then(function (r: any) {
              vm.samples.spdx          = r[0].data;
              vm.samples.cyclonedx     = r[1].data;
              vm.rules.spdxToCyclonedx = r[2].data;
              vm.rules.cyclonedxToSpdx = r[3].data;
              vm.isLoaded = true;
              applyTypeChange();
            }).catch(function (err: any) {
              vm.loadError = err && err.config
                ? 'Failed to load: ' + err.config.url + ' (status ' + err.status + ')'
                : 'Failed to load SBOM resources.';
              console.error('SBOM load error', err);
            });
          }

          function setType(type: string) {
            if (vm.selectedType === type) return;
            vm.selectedType = type;
            applyTypeChange();
          }

          function applyTypeChange() {
            if (!vm.isLoaded) return;
            NodeRegistry.clear('left');
            NodeRegistry.clear('right');
            if (vm.selectedType === 'spdx') {
              vm.sourceLabel    = 'SPDX 2.3';
              vm.targetLabel    = 'CycloneDX 1.6';
              vm.sourceRootPath = 'document';
              vm.targetRootPath = 'bom';
              vm.sourceBom      = angular.copy(vm.samples.spdx);
              vm.convertedBom   = angular.copy(vm.samples.cyclonedx);
              vm.activeMappings = vm.rules.spdxToCyclonedx;
            } else {
              vm.sourceLabel    = 'CycloneDX 1.6';
              vm.targetLabel    = 'SPDX 2.3';
              vm.sourceRootPath = 'bom';
              vm.targetRootPath = 'document';
              vm.sourceBom      = angular.copy(vm.samples.cyclonedx);
              vm.convertedBom   = angular.copy(vm.samples.spdx);
              vm.activeMappings = vm.rules.cyclonedxToSpdx;
            }
            vm.stats.sourceFields = self.countLeaves(vm.sourceBom);
            vm.stats.targetFields = self.countLeaves(vm.convertedBom);
            clearHighlights();
            applyFilter();
          }

          function setFilter(key: string) {
            vm.filterSection = key;
            applyFilter();
          }

          function applyFilter() {
            var q   = (vm.searchQuery || '').toLowerCase();
            var sec = vm.filterSection;
            vm.filteredMappings = vm.activeMappings.filter(function (m: any) {
              var sl = m.section.toLowerCase();
              var secOk = sec === 'all' ||
                (sec === 'document' && sl === 'document') ||
                (sec === 'package'  && sl.indexOf('package') >= 0) ||
                (sec === 'file'     && sl.indexOf('file')    >= 0);
              var qOk = !q ||
                m.source.toLowerCase().indexOf(q) >= 0 ||
                m.target.toLowerCase().indexOf(q) >= 0;
              return secOk && qOk;
            });
            vm.visibleMappings = vm.filteredMappings;
          }

          function reset() {
            vm.searchQuery   = '';
            vm.filterSection = 'all';
            clearHighlights();
            applyTypeChange();
          }

          function selectMapping(id: any) {
            if (vm.lockedMappingIds.length === 1 && vm.lockedMappingIds[0] === id) {
              clearHighlights(); return;
            }
            vm.lockedMappingIds = [id];
            vm.hoverMappingIds  = [];
            refreshHighlights();
          }

          function hoverMapping(id: any) {
            if (vm.lockedMappingIds.length) return;
            vm.hoverMappingIds = [id];
            refreshHighlights();
          }

          function clearMappingHover() {
            if (vm.lockedMappingIds.length) return;
            vm.hoverMappingIds = [];
            refreshHighlights();
          }

          function onNodeEvent(type: string, side: string, path: string, key: string, el: any) {
            if (type === 'hover' || type === 'leave') return;
            if (type === 'select') {
              if (!path) { clearHighlights(); return; }
              var cpath = self.canonicalPath(path);
              if (!cpath) { clearHighlights(); return; }
              var ids = vm.activeMappings.filter(function (m: any) {
                var expr = side === 'left' ? m.source : m.target;
                return self.canonicalPath(expr) === cpath;
              }).map(function (m: any) { return m.id; });
              if (!ids.length) { clearHighlights(); return; }
              if (self.arraysEq(vm.lockedMappingIds, ids)) { clearHighlights(); return; }
              vm.lockedMappingIds  = ids;
              vm.hoverMappingIds   = [];
              vm.targetExpandPaths = [];
              if (side === 'left') {
                var matched = vm.activeMappings.filter(function (m: any) {
                  return ids.indexOf(m.id) >= 0;
                });
                vm.targetExpandPaths = matched
                  .map(function (m: any) { return self.targetExprToTreePath(m.target); })
                  .filter(Boolean);
              }
              refreshHighlights();
            }
          }

          function refreshHighlights() {
            var activeIds = vm.lockedMappingIds.length
              ? vm.lockedMappingIds : vm.hoverMappingIds;
            vm.activeMappingIds = activeIds.slice();
            if (!activeIds.length) {
              vm.highlightedPaths = { left: [], right: [] };
              return;
            }
            var matched = vm.activeMappings.filter(function (m: any) {
              return activeIds.indexOf(m.id) >= 0;
            });
            vm.highlightedPaths = self.buildHighlightTokens(matched, vm.selectedType);
          }

          function clearHighlights() {
            vm.lockedMappingIds  = [];
            vm.hoverMappingIds   = [];
            vm.activeMappingIds  = [];
            vm.highlightedPaths  = { left: [], right: [] };
            vm.targetExpandPaths = [];
          }
        }
      ])

      /* ── Directives ── */
      .directive('markmapTree', function () { return self.markmapTreeDirective(); })
      .directive('ribbonLayer', function () { return self.ribbonLayerDirective(); });

    angular.bootstrap(this.hostEl.nativeElement, ['sbomVisualizationModule']);
  }

  /* ╔════════════════════════════════════════════════════════════════════════════════════════════════════════════
     ║  PUBLIC API
     ╚════════════════════════════════════════════════════════════════════════════════════════════════════════════ */

  setType(type: string): void {
    if (this.selectedType === type) return;
    this.selectedType = type;
    this.applyTypeChange();
  }

  setFilter(key: string): void {
    this.filterSection = key;
    this.applyFilter();
  }

  reset(): void {
    this.searchQuery = "";
    this.filterSection = "all";
    this.clearHighlights();
    this.applyTypeChange();
  }

  selectMapping(id: any): void {
    if (this.lockedMappingIds.length === 1 && this.lockedMappingIds[0] === id) {
      this.clearHighlights();
      return;
    }
    this.lockedMappingIds = [id];
    this.hoverMappingIds = [];
    this.refreshHighlights();
  }

  hoverMapping(id: any): void {
    if (this.lockedMappingIds.length) return;
    this.hoverMappingIds = [id];
    this.refreshHighlights();
  }

  clearMappingHover(): void {
    if (this.lockedMappingIds.length) return;
    this.hoverMappingIds = [];
    this.refreshHighlights();
  }

  onNodeEvent(type: string, side: string, path: string, key: string, el: any): void {
    if (type === "hover" || type === "leave") return;
    if (type === "select") {
      const ids = this.findMatchingMappingIds(side, path, key);
      if (!ids.length) {
        this.clearHighlights();
        return;
      }
      if (this.arraysEq(this.lockedMappingIds, ids)) {
        this.clearHighlights();
        return;
      }
      this.lockedMappingIds = ids;
      this.hoverMappingIds = [];
      this.targetExpandPaths = [];
      if (side === "left") {
        const matched = this.activeMappings.filter((m: any) => ids.indexOf(m.id) >= 0);
        this.targetExpandPaths = matched.map((m: any) => this.targetExprToTreePath(m.target)).filter(Boolean);
      }
      this.refreshHighlights();
    }
  }

  sectionClass(section: string): string {
    return (section || "other").toLowerCase().replace(/[^a-z0-9]/g, "-");
  }

  /* ╔════════════════════════════════════════════════════════════════════════════════════════════════════════════
     ║  PRIVATE METHODS
     ╚════════════════════════════════════════════════════════════════════════════════════════════════════════════ */



  private applyTypeChange(): void {
    if (!this.isLoaded) return;
    this.nodeRegistry.left = {};
    this.nodeRegistry.right = {};

    if (this.selectedType === "spdx") {
      this.sourceLabel = "SPDX 2.3";
      this.targetLabel = "CycloneDX 1.6";
      this.sourceRootPath = "document";
      this.targetRootPath = "bom";
      this.sourceBom = JSON.parse(JSON.stringify(this.samples.spdx));
      this.convertedBom = JSON.parse(JSON.stringify(this.samples.cyclonedx));
      this.activeMappings = this.rules.spdxToCyclonedx;
    } else {
      this.sourceLabel = "CycloneDX 1.6";
      this.targetLabel = "SPDX 2.3";
      this.sourceRootPath = "bom";
      this.targetRootPath = "document";
      this.sourceBom = JSON.parse(JSON.stringify(this.samples.cyclonedx));
      this.convertedBom = JSON.parse(JSON.stringify(this.samples.spdx));
      this.activeMappings = this.rules.cyclonedxToSpdx;
    }

    this.stats.sourceFields = this.countLeaves(this.sourceBom);
    this.stats.targetFields = this.countLeaves(this.convertedBom);
    this.clearHighlights();
    this.applyFilter();
  }

  private applyFilter(): void {
    const q = (this.searchQuery || "").toLowerCase();
    const sec = this.filterSection;

    this.filteredMappings = this.activeMappings.filter((m: any) => {
      const sl = m.section.toLowerCase();
      const secOk = sec === "all" ||
        (sec === "document" && sl === "document") ||
        (sec === "package" && sl.indexOf("package") >= 0) ||
        (sec === "file" && sl.indexOf("file") >= 0);
      const qOk = !q ||
        m.source.toLowerCase().indexOf(q) >= 0 ||
        m.target.toLowerCase().indexOf(q) >= 0;
      return secOk && qOk;
    });

    this.visibleMappings = this.filteredMappings;
  }

  private findMatchingMappingIds(side: string, path: string, key: string): any[] {
    if (!path) return [];
    const cpath = this.canonicalPath(path);
    if (!cpath) return [];
    /* STRICT: a mapping matches only when the clicked node path is EXACTLY the
     * mapping source (left tree) or target (right tree) canonical path. */
    return this.activeMappings.filter((m: any) => {
      const expr = side === "left" ? m.source : m.target;
      return this.canonicalPath(expr) === cpath;
    }).map((m: any) => m.id);
  }

  private refreshHighlights(): void {
    const activeIds = this.lockedMappingIds.length
      ? this.lockedMappingIds : this.hoverMappingIds;

    this.activeMappingIds = activeIds.slice();

    if (!activeIds.length) {
      this.highlightedPaths = { left: [], right: [] };
      return;
    }

    const matched = this.activeMappings.filter((m: any) => {
      return activeIds.indexOf(m.id) >= 0;
    });

    this.highlightedPaths = this.buildHighlightTokens(matched, this.selectedType);
  }

  private clearHighlights(): void {
    this.lockedMappingIds = [];
    this.hoverMappingIds = [];
    this.activeMappingIds = [];
    this.highlightedPaths = { left: [], right: [] };
    this.targetExpandPaths = [];
  }

  private getComponentKind(side: string, path: string): string {
    const p = this.normalizePath(path);
    if (p.indexOf(".components.") < 0) return "";
    const match = p.match(/\.components\.(\d+)/);
    if (!match) return "";
    const data = side === "left" ? this.sourceBom : this.convertedBom;
    return ((data && data.components && data.components[Number(match[1])] &&
      data.components[Number(match[1])].type) || "").toLowerCase();
  }

  private countLeaves(obj: any): number {
    if (!obj) return 0;
    let n = 0;
    const walk = (o: any) => {
      if (!o || typeof o !== "object") { n++; return; }
      Object.keys(o).forEach((k: string) => { walk(o[k]); });
    };
    walk(obj);
    return n;
  }

  private arraysEq(a: any[], b: any[]): boolean {
    if (a.length !== b.length) return false;
    const sa = a.slice().sort(), sb = b.slice().sort();
    return sa.every((v: any, i: number) => v === sb[i]);
  }

  /* ╔════════════════════════════════════════════════════════════════════════════════════════════════════════════
     ║  ANGULARJS DIRECTIVES
     ╚════════════════════════════════════════════════════════════════════════════════════════════════════════════ */

  private markmapTreeDirective(): any {
    const self = this;
    return {
      restrict: "E",
      scope: {
        data: "=",
        rootLabel: "=",
        rootPath: "=",
        side: "@",
        expandAll: "@",
        searchQuery: "=",
        activePaths: "=",
        expandPaths: "=",
        onNodeEvent: "&"
      },
      template: '<div class="mm-wrap"><svg class="mm-svg"></svg></div>',
      link: function (scope: any, element: any) {

        /* Branch colour palette (markmap-inspired) */
        const COLORS = ["#4361ee", "#7209b7", "#3a86ff", "#06d6a0",
          "#f77f00", "#e63946", "#118ab2", "#9b5de5"];

        const wrap = element[0].querySelector(".mm-wrap");
        const svgEl = element[0].querySelector(".mm-svg");
        const svg = d3.select(svgEl);
        const g = svg.append("g").attr("class", "mm-root");
        let root: any = null;
        let rawTree: any = null;
        let uid = 0;

        /* Shared tooltip (one per page) */
        const tip = d3.select("body").selectAll(".mm-tooltip")
          .data([0]).join("div").attr("class", "mm-tooltip");

        /* Zoom / pan */
        const zoom = d3.zoom().scaleExtent([0.12, 4])
          .on("zoom", function (evt: any) { g.attr("transform", evt.transform); });
        svg.call(zoom).on("dblclick.zoom", null);

        /* D3 tree layout - nodeSize gives even spacing */
        const layout = d3.tree().nodeSize([28, 210]);

        /* --- Build hierarchy from BOM JSON --- */
        function buildHierarchy(data: any, label: string, rootPath: string) {
          function mkNode(name: string, val: any, path: string, depth: number): any {
            const isObj = val !== null && val !== undefined && typeof val === "object";
            return {
              _uid: ++uid,
              name: String(name),
              path: path,
              value: isObj ? null : val,
              isLeaf: !isObj,
              _depth: depth,
              _collapsed: (scope.expandAll === "true") ? false :
                (scope.expandAll === "false") ? true : depth >= 2,
              children: isObj ? childNodes(val, path, depth + 1) : null
            };
          }
          function childNodes(obj: any, pPath: string, depth: number): any[] {
            const isArr = Array.isArray(obj);
            return Object.keys(obj).map((k: string) => {
              const displayKey = isArr ? "[" + k + "]" : k;
              const pathKey = isArr ? "[" + k + "]" : k;
              return mkNode(displayKey, obj[k], pPath + "." + pathKey, depth);
            });
          }
          return mkNode(label, data, rootPath || label, 0);
        }

        /* Rebuild D3 hierarchy from cached rawTree (preserves _collapsed state) */
        function rebuildAndUpdate() {
          if (!rawTree) return;
          root = d3.hierarchy(rawTree, function (d: any) {
            return d._collapsed ? null : (d.children || null);
          });
          update();
        }

        /* --- Render --- */
        function render() {
          if (!scope.data) return;
          uid = 0;
          rawTree = buildHierarchy(scope.data, scope.rootLabel,
            scope.rootPath || scope.rootLabel);
          rebuildAndUpdate();
          window.setTimeout(fitView, 60);
        }

        function update() {
          if (!root) return;
          layout(root);
          drawNodes();
          drawLinks();
          applyHighlights();
        }

        /* --- Links --- */
        function drawLinks() {
          const links = root.links();
          const sel = g.selectAll("path.mm-link")
            .data(links, function (d: any) { return d.target.data._uid; });

          sel.enter().append("path").attr("class", "mm-link")
            .style("opacity", 0)
            .merge(sel)
            .transition().duration(280)
            .attr("d", function (d: any) { return bezier(d.source, d.target); })
            .style("stroke", function (d: any) { return branchColor(d.target); })
            .style("opacity", 0.55);

          sel.exit().transition().duration(180).style("opacity", 0).remove();
        }

        /* --- Nodes --- */
        function drawNodes() {
          const nodeRegistry = getRegistry();
          const nodes = root.descendants();
          const sel = g.selectAll("g.mm-node")
            .data(nodes, function (d: any) { return d.data._uid; });

          /* Enter */
          const entered = sel.enter().append("g").attr("class", "mm-node")
            .style("opacity", 0)
            .attr("transform", function (d: any) {
              const p = d.parent || d;
              return "translate(" + p.y + "," + p.x + ")";
            });

          entered.append("rect").attr("class", "mm-node-bg").attr("rx", 6);
          entered.append("text").attr("class", "mm-node-text");
          entered.append("circle").attr("class", "mm-collapse-dot").attr("r", 3.5);

          /* Bind events on enter */
          entered
            .on("click", function (evt: any, d: any) {
              evt.stopPropagation();
              if (d.data.children && d.data.children.length > 0) {
                d.data._collapsed = !d.data._collapsed;
                rebuildAndUpdate();
              }
              scope.$applyAsync(function () {
                scope.onNodeEvent({ type: "select", side: scope.side,
                  path: d.data.path, key: d.data.name, el: evt.currentTarget });
              });
            })
            .on("mouseenter", function (evt: any, d: any) {
              showTip(d, evt);
              scope.$applyAsync(function () {
                scope.onNodeEvent({ type: "hover", side: scope.side,
                  path: d.data.path, key: d.data.name, el: evt.currentTarget });
              });
            })
            .on("mousemove", function (evt: any) {
              tip.style("left", (evt.clientX + 14) + "px")
                .style("top", (evt.clientY - 8) + "px");
            })
            .on("mouseleave", function () {
              tip.style("display", "none");
              scope.$applyAsync(function () {
                scope.onNodeEvent({ type: "leave", side: scope.side,
                  path: null, key: null, el: null });
              });
            });

          /* Update (enter + existing) */
          const all = entered.merge(sel);

          all.transition().duration(280)
            .attr("transform", function (d: any) {
              return "translate(" + d.y + "," + d.x + ")";
            })
            .style("opacity", 1);

          /* Size / style each node */
          all.each(function (this: Element, d: any) {
            styleNode(d3.select(this), d, nodeRegistry);
          });

          sel.exit().transition().duration(180).style("opacity", 0).remove();
        }

        function styleNode(gSel: any, d: any, nodeRegistry: any) {
          const color = branchColor(d);
          const isRoot = d.depth === 0;
          const label = buildLabel(d);

          /* Text */
          gSel.select("text.mm-node-text")
            .text(label)
            .style("fill", isRoot ? "#fff" : color)
            .style("font-size", isRoot ? "13px" : d.depth === 1 ? "12px" : "11px")
            .style("font-weight", d.depth <= 1 ? "600" : "400");

          /* Measure */
          const tEl = gSel.node() ? (gSel.node() as Element).querySelector("text") : null;
          let box = { width: 80, height: 14 };
          try { box = (tEl || gSel.select("text").node()).getBBox(); } catch (e) { }

          const pw = Math.max(box.width + 18, isRoot ? 110 : 60);
          const ph = Math.max(box.height + 10, 22);

          d.data._w = pw;

          /* Rect */
          gSel.select("rect.mm-node-bg")
            .attr("width", pw)
            .attr("height", ph)
            .attr("x", -6)
            .attr("y", -ph / 2)
            .style("fill", isRoot ? color : hexAlpha(color, 0.1))
            .style("stroke", color);

          /* Text position */
          gSel.select("text.mm-node-text").attr("x", 5).attr("y", 1);

          /* Collapse dot */
          const hasCh = d.data.children && d.data.children.length > 0;
          gSel.select(".mm-collapse-dot")
            .attr("cx", pw + 2).attr("cy", 0)
            .style("fill", color)
            .style("display", hasCh ? null : "none")
            .style("opacity", d.data._collapsed ? 0.9 : 0.35);

          /* Register for ribbon layer */
          if (nodeRegistry) {
            nodeRegistry.set(scope.side, d.data.path, gSel.node());
          }

          gSel.attr("data-path", d.data.path);
        }

        /* --- Highlights --- */
        function applyHighlights() {
          const activePaths = scope.activePaths || [];
          const q = (scope.searchQuery || "").toLowerCase();
          const hasActive = activePaths.length > 0;
          const hasSearch = q.length > 0;

          function isOnPath(cpath: string) {
            if (!cpath) return false;
            for (let i = 0; i < activePaths.length; i++) {
              if (activePaths[i].indexOf(cpath + ".") === 0) return true;
            }
            return false;
          }

          g.selectAll("g.mm-node").each(function (this: Element, d: any) {
            const gEl = d3.select(this);
            const cpath = self.canonicalPath(d.data.path || "");
            const pl = (d.data.path || "").toLowerCase();
            const nl = (d.data.name || "").toLowerCase();
            const vl = String(d.data.value !== null && d.data.value !== undefined ? d.data.value : "").toLowerCase();

            const isHit = hasActive && activePaths.indexOf(cpath) >= 0;
            const isPath = hasActive && !isHit && isOnPath(cpath);
            const isSrch = hasSearch && (pl.indexOf(q) >= 0 || nl.indexOf(q) >= 0 || vl.indexOf(q) >= 0);

            gEl.classed("node-highlighted", isHit)
              .classed("node-path", isPath)
              .classed("node-searched", isSrch && !isHit && !isPath)
              .classed("node-dim", hasActive && !isHit && !isPath && !isSrch);
          });

          g.selectAll("path.mm-link").each(function (this: Element, d: any) {
            const tcp = self.canonicalPath((d.target && d.target.data && d.target.data.path) || "");
            const onChain = hasActive && tcp &&
              (activePaths.indexOf(tcp) >= 0 || isOnPath(tcp));
            d3.select(this)
              .classed("link-path", onChain)
              .classed("link-dim", hasActive && !onChain);
          });

          if (!hasActive) {
            g.selectAll("g.mm-node").classed("node-dim", false);
            g.selectAll("path.mm-link").classed("link-dim", false).classed("link-path", false);
          }
        }

        function tokenMatches(tok: any, pathLower: string, valueLower: string, keyLower: string) {
          if (!tok) return false;
          if (typeof tok === "string") {
            return pathLower.indexOf(tok) >= 0 || tok.indexOf(pathLower) >= 0;
          }
          if (tok.pathToken && pathLower.indexOf(tok.pathToken) >= 0) return true;
          if (tok.propertyName && pathLower.indexOf(".properties.") >= 0 &&
            valueLower === tok.propertyName) return true;
          return false;
        }

        /* --- Auto-fit --- */
        function fitView() {
          try {
            const b = g.node().getBBox();
            const ww = wrap.clientWidth || 560;
            const wh = wrap.clientHeight || 400;
            if (!b.width || !b.height) return;
            const pad = 36;
            const scale = Math.min(
              (ww - pad * 2) / b.width,
              (wh - pad * 2) / b.height,
              1.3
            );
            const tx = -b.x * scale + (ww - b.width * scale) / 2;
            const ty = -b.y * scale + (wh - b.height * scale) / 2;
            svg.transition().duration(550).call(
              zoom.transform,
              d3.zoomIdentity.translate(tx, ty).scale(scale)
            );
          } catch (e) { }
        }

        /* --- Helpers --- */
        function getRegistry() {
          try { return element.injector().get('NodeRegistry'); } catch (e) { return null; }
        }
        function branchColor(d: any) {
          let cur = d;
          while (cur.depth > 1 && cur.parent) cur = cur.parent;
          if (!cur.parent) return COLORS[0];
          const idx = cur.parent.children ? cur.parent.children.indexOf(cur) : 0;
          return COLORS[idx % COLORS.length];
        }

        function hexAlpha(hex: string, a: number) {
          const r = parseInt(hex.slice(1, 3), 16);
          const g2 = parseInt(hex.slice(3, 5), 16);
          const b2 = parseInt(hex.slice(5, 7), 16);
          return "rgba(" + r + "," + g2 + "," + b2 + "," + a + ")";
        }

        function bezier(s: any, t: any) {
          const sw = (s.data && s.data._w) || 60;
          const sx = s.y - 6 + sw;
          const tx = t.y - 6;
          const mx = (sx + tx) / 2;
          return "M" + sx + "," + s.x +
            "C" + mx + "," + s.x + " " + mx + "," + t.x +
            " " + tx + "," + t.x;
        }

        function buildLabel(d: any) {
          if (d.depth === 0) return d.data.name;
          if (d.data.isLeaf && d.data.value !== null && d.data.value !== undefined) {
            return d.data.name + ": " + trunc(String(d.data.value), 30);
          }
          return d.data.name;
        }

        function showTip(d: any, evt: any) {
          let html = '<div class="tt-path">' + esc(d.data.path) + "</div>";
          if (d.data.value !== null && d.data.value !== undefined) {
            html += '<div class="tt-val">' + esc(String(d.data.value)) + "</div>";
          } else if (d.data.children) {
            html += "<div>" + d.data.children.length + " child fields</div>";
          }
          tip.style("display", "block").html(html)
            .style("left", (evt.clientX + 14) + "px")
            .style("top", (evt.clientY - 8) + "px");
        }

        function trunc(s: string, n: number) { return s.length > n ? s.slice(0, n) + "…" : s; }
        function esc(s: string) {
          return String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
        }

        /* --- Watchers --- */
        function resetNodeCollapse(node: any) {
          if (!node) return;
          node._collapsed = (scope.expandAll === "true") ? false :
            (scope.expandAll === "false") ? true :
            (node._depth || 0) >= 2;
          if (node.children) node.children.forEach(resetNodeCollapse);
        }
        function expandTreeToPath(treePaths: any) {
          if (!rawTree || !treePaths || !treePaths.length) return;
          resetNodeCollapse(rawTree);
          function expandAncestors(node: any) {
            if (!node) return;
            const nPath = self.canonicalPath(node.path || "");
            if (treePaths.some(function (tp: any) { const tpl = self.canonicalPath(tp || ""); return tpl === nPath || tpl.indexOf(nPath + ".") === 0; })) { node._collapsed = false; }
            if (node.children) node.children.forEach(expandAncestors);
          }
          expandAncestors(rawTree);
          rebuildAndUpdate();
          window.setTimeout(function () { scope.$root.$broadcast("sbom:treeExpanded"); }, 320);
          window.setTimeout(fitView, 80);
        }
        scope.$watch("data", function (v: any) { if (v) render(); }, false);
        scope.$watch("activePaths", function () { applyHighlights(); }, true);
        scope.$watch("searchQuery", function () { applyHighlights(); });
        scope.$watch("expandPaths", function (paths: any) {
          if (!rawTree) return;
          if (!paths || !paths.length) { resetNodeCollapse(rawTree); rebuildAndUpdate(); window.setTimeout(fitView, 60); }
          else { expandTreeToPath(paths); }
        }, true);
      }
    };
  }

  private ribbonLayerDirective(): any {
    const self = this;
    return {
      restrict: "E",
      scope: {
        activeMappings: "=",
        activeIds: "="
      },
      template: '<svg class="ribbon-svg" aria-hidden="true"></svg>',
      link: function (scope: any, element: any) {
        const svg = d3.select(element[0]).select("svg");
        let rafId: any = null;
        let nodeRegistry: any = null;
        try { nodeRegistry = element.injector().get('NodeRegistry'); } catch (e) { /* ok */ }

        const SECTION_COLORS: any = {
          "document": "#4361ee",
          "package-component": "#7209b7",
          "component-package": "#7209b7",
          "file-component": "#e63946",
          "file": "#e63946"
        };

        function schedule() {
          if (rafId) cancelAnimationFrame(rafId);
          rafId = requestAnimationFrame(draw);
        }

        function draw() {
          rafId = null;
          svg.selectAll("*").remove();

          const ids = scope.activeIds || [];
          if (!ids.length || !nodeRegistry) return;

          const svgRect = element[0].getBoundingClientRect();
          if (!svgRect.width) return;

          const mappings = (scope.activeMappings || []).filter((m: any) => {
            return ids.indexOf(m.id) >= 0;
          });

          const ribbons: any[] = [];

          mappings.forEach((m: any) => {
            const col = SECTION_COLORS[m.section] || "#06d6a0";
            const leftEls = matchNodes("left", m.source, m.section);
            const rightEls = matchNodes("right", m.target, m.section);

            leftEls.forEach((lEl: any) => {
              rightEls.forEach((rEl: any) => {
                const lr = lEl.getBoundingClientRect();
                const rr = rEl.getBoundingClientRect();
                if (!lr.width || !rr.width) return;

                const x1 = lr.right - svgRect.left;
                const y1 = (lr.top + lr.height / 2) - svgRect.top;
                const x2 = rr.left - svgRect.left;
                const y2 = (rr.top + rr.height / 2) - svgRect.top;

                ribbons.push({ id: m.id, x1: x1, y1: y1, x2: x2, y2: y2, color: col });
              });
            });
          });

          if (!ribbons.length) return;

          /* Glow layer */
          ribbons.forEach((rb: any) => {
            svg.append("path")
              .attr("class", "rb-path rb-path-glow")
              .attr("d", curvePath(rb))
              .attr("stroke", rb.color)
              .attr("stroke-width", 8)
              .attr("opacity", 0.22);
          });

          /* Main animated ribbon */
          ribbons.forEach((rb: any) => {
            svg.append("path")
              .attr("class", "rb-path rb-path-animated")
              .attr("d", curvePath(rb))
              .attr("stroke", rb.color)
              .attr("stroke-width", 2.5)
              .attr("opacity", 0.88);
          });
        }

        function curvePath(rb: any) {
          const cpX = (rb.x2 - rb.x1) * 0.42;
          return "M" + rb.x1 + "," + rb.y1 +
            "C" + (rb.x1 + cpX) + "," + rb.y1 +
            " " + (rb.x2 - cpX) + "," + rb.y2 +
            " " + rb.x2 + "," + rb.y2;
        }

        function matchNodes(side: string, expr: string, section: string) {
          const reg = nodeRegistry ? nodeRegistry.getAll(side) : {};
          const target = self.canonicalPath(expr);
          const results: any[] = [];
          if (!target) return results;

          Object.keys(reg).forEach((path: string) => {
            const el = reg[path];
            if (!el) return;
            if (self.canonicalPath(path) === target) results.push(el);
          });

          return results;
        }

        /* Watchers */
        scope.$watchGroup(["activeIds", "activeMappings"], function () { schedule(); }, true);

        const onResize = function () { schedule(); };
        window.addEventListener("resize", onResize);

        const dtl = scope.$root.$on("sbom:treeExpanded", function () { schedule(); });
        scope.$on("$destroy", function () { dtl(); window.removeEventListener("resize", onResize); if (rafId) cancelAnimationFrame(rafId); });
      }
    };
  }

  /* ╔════════════════════════════════════════════════════════════════════════════════════════════════════════════
     ║  CONVERSION FUNCTIONS
     ╚════════════════════════════════════════════════════════════════════════════════════════════════════════════ */

  private convertSpdxToCycloneDx(spdx: any): any {
    const bom: any = {
      bomFormat: "CycloneDX",
      specVersion: "1.6",
      version: 1,
      serialNumber: "urn:uuid:generated-from-spdx",
      metadata: {
        timestamp: this.getValue(spdx, "creationInfo.created"),
        authors: [],
        properties: []
      },
      components: []
    };
    bom.metadata.properties[0] = {};
    bom.metadata.properties[0]["spdx:spdxid"] = spdx.SPDXID;
    bom.metadata.properties[0]["spdx:document:spdx-version"] = spdx.spdxVersion;
    bom.metadata.properties[0]["spdx:document:spdx:comment"] = spdx.comment;
    bom.metadata.properties[0]["spdx:document:name"] = spdx.name;
    bom.metadata.properties[0]["spdx:document:document-namespace"] = spdx.documentNamespace;
    bom.metadata.properties[0]["spdx:creation-info:license-list-version"] = this.getValue(spdx, "creationInfo.licenseListVersion");
    bom.metadata.properties[0]["spdx:creation-info:comment(creationInfo)"] = this.getValue(spdx, "creationInfo.creationInfocomment");
    bom.metadata.properties[0]["annotations.comment"] = this.getValue(spdx, "annotations.0.annotationsComment");
    bom.metadata.properties[0]["annotations.annotator"] = this.getValue(spdx, "annotations.0.annotator");
    bom.metadata.properties[0]["annotations.annotationDate"] = this.getValue(spdx, "annotations.0.annotationDate");
    bom.metadata.properties[0]["annotations.annotationType"] = this.getValue(spdx, "annotations.0.annotationType");
    bom.metadata.properties[0]["spdx:document:describes"] = this.getValue(spdx, "documentDescribes.0");

    if (Array.isArray(spdx.documentDescribes)) {
      this.setProp(bom.metadata.properties[0], "spdx:document:describes", spdx.documentDescribes.join(", "));
    }

    const creators = this.getValue(spdx, "creationInfo.creators") || [];
    if (creators.length) {
      const parsed = this.parseParty(creators[0]);
      bom.metadata.authors.push({ authorsName: parsed.name || creators[0], email: parsed.email || undefined });
    }

    (Array.isArray(spdx.packages) ? spdx.packages : []).forEach((pkg: any) => {
      bom.components.push(this.convertSpdxPackage(pkg));
    });
    (Array.isArray(spdx.files) ? spdx.files : []).forEach((file: any) => {
      bom.components.push(this.convertSpdxFile(file));
    });

    const pkgs = Array.isArray(spdx.packages) ? spdx.packages : [];
    if (pkgs.length) {
      bom.metadata.component = {
        type: "application",
        name: spdx.name || pkgs[0].name,
        version: pkgs[0].versionInfo || "",
        group: "converted"
      };
    }

    return this.pruneEmpty(bom);
  }

  private convertCycloneDxToSpdx(cdx: any): any {
    const doc: any = {
      spdxVersion: "SPDX-2.3",
      SPDXID: "SPDXRef-DOCUMENT",
      name: "Converted from CycloneDX",
      documentNamespace: cdx.serialNumber || "https://example.com/spdx/converted",
      creationInfo: {
        created: this.getValue(cdx, "metadata.timestamp") || new Date().toISOString(),
        creators: [],
        licenseListVersion: "3.26",
        comment: "Converted from CycloneDX"
      },
      comment: "Converted BOM",
      documentDescribes: [],
      packages: [],
      files: []
    };

    const props = this.toPropertyMap(this.getValue(cdx, "metadata.properties"));
    doc.SPDXID = props["spdx:spdxid"] || doc.SPDXID;
    doc.name = props["spdx:document:name"] || this.getValue(cdx, "metadata.component.name") || doc.name;
    doc.comment = props["spdx:comment"] || doc.comment;
    doc.documentNamespace = props["spdx:document:document-namespace"] || doc.documentNamespace;
    doc.creationInfo.licenseListVersion = props["spdx:creation-info:license-list-version"] || doc.creationInfo.licenseListVersion;
    doc.creationInfo.comment = props["spdx:creation-info:comment"] || doc.creationInfo.comment;

    (this.getValue(cdx, "metadata.authors") || []).forEach((a: any) => {
      if (a && a.name) {
        doc.creationInfo.creators.push("Person: " + a.name + (a.email ? " (" + a.email + ")" : ""));
      }
    });

    (Array.isArray(cdx.components) ? cdx.components : []).forEach((c: any) => {
      if ((c.type || "").toLowerCase() === "file") {
        doc.files.push(this.convertCycloneFile(c));
      } else {
        const pkg = this.convertCyclonePackage(c);
        doc.packages.push(pkg);
        if (pkg.SPDXID) doc.documentDescribes.push(pkg.SPDXID);
      }
    });

    return this.pruneEmpty(doc);
  }

  private convertSpdxPackage(pkg: any): any {
    const c: any = {
      type: pkg.primaryPackagePurpose || "library",
      name: pkg.packagesName,
      version: pkg.packagesVersionInfo,
      description: pkg.description,
      copyright: pkg.copyrightText,
      properties: [],
      evidence: { licenses: [], copyright: [] },
      hashes: [],
      externalReferences: []
    };
    this.setProp(c.properties, "spdx:spdxid", pkg.SPDXID);
    this.setProp(c.properties, "spdx:files-analyzed", this.toStringOrNull(pkg.filesAnalyzed));
    this.setProp(c.properties, "spdx:license-comments", pkg.licenseComments);
    this.setProp(c.properties, "spdx:license-concluded", pkg.licenseConcluded);
    this.setProp(c.properties, "spdx:package:file-name", pkg.packageFileName);
    this.setProp(c.properties, "spdx:package:source-info", pkg.sourceInfo);
    this.setProp(c.properties, "spdx:package:summary", pkg.summary);
    this.setProp(c.properties, "spdx:comment", pkg.comment);
    this.setProp(c.properties, "spdx:package:built-date", pkg.builtDate);
    this.setProp(c.properties, "spdx:package:release-date", pkg.releaseDate);
    this.setProp(c.properties, "spdx:package:valid-until-date", pkg.validUntilDate);

    if (pkg.licenseDeclared === "NOASSERTION") this.setProp(c.properties, "spdx:license-declared", "NOASSERTION");
    else if (pkg.licenseDeclared === "NONE") c.licenses = [];
    else if (pkg.licenseDeclared) c.licenses = [{ expression: pkg.licenseDeclared }];

    (pkg.licenseInfoFromFiles || []).forEach((id: any) => {
      c.evidence.licenses.push({ license: { id: id } });
    });

    if (pkg.supplier && pkg.supplier !== "NOASSERTION") {
      const sup = this.parseParty(pkg.supplier);
      c.supplier = { name: sup.name || pkg.supplier, contact: sup.email ? [{ email: sup.email }] : [] };
      if (sup.kind === "Organization") this.setProp(c.properties, "spdx:package:supplier:organization", sup.name);
    } else if (pkg.supplier === "NOASSERTION") {
      this.setProp(c.properties, "spdx:package:supplier", "NOASSERTION");
    }

    if (pkg.originator === "NOASSERTION") this.setProp(c.properties, "spdx:package:originator", "NOASSERTION");

    (pkg.attributionTexts || []).forEach((t: any) => { c.evidence.copyright.push({ text: t }); });

    (pkg.checksums || []).forEach((ch: any) => {
      if (ch.algorithm && ch.checksumValue) {
        c.hashes.push({ alg: this.normalizeCycloneHash(ch.algorithm), content: ch.checksumValue });
      }
    });

    (pkg.externalRefs || []).forEach((ref: any) => {
      if (ref.referenceType === "purl") c.purl = ref.referenceLocator;
      else if (ref.referenceType === "cpe23Type") c.cpe = ref.referenceLocator;
      else c.externalReferences.push({ type: (ref.referenceType || "other").toLowerCase(), url: ref.referenceLocator });
    });

    if (pkg.downloadLocation) {
      c.externalReferences.push({ type: "distribution", url: pkg.downloadLocation });
      this.setProp(c.properties, "spdx:download-location", pkg.downloadLocation);
    }
    if (pkg.homepage && pkg.homepage !== "NOASSERTION") {
      c.externalReferences.push({ type: "website", url: pkg.homepage });
      this.setProp(c.properties, "spdx:homepage", pkg.homepage);
    }

    return this.pruneEmpty(c);
  }

  private convertSpdxFile(file: any): any {
    const c: any = {
      type: "file",
      name: file.fileName,
      copyright: file.copyrightText,
      properties: [],
      evidence: { copyright: [] },
      hashes: []
    };

    this.setProp(c.properties, "spdx:spdxid", file.SPDXID);
    this.setProp(c.properties, "spdx:comment", file.comment);
    this.setProp(c.properties, "spdx:file:type", this.arrayToCsv(file.fileTypes));
    this.setProp(c.properties, "spdx:license-comments", file.licenseComments);
    this.setProp(c.properties, "spdx:license-concluded", file.licenseConcluded);
    this.setProp(c.properties, "spdx:file:contributor", this.arrayToCsv(file.fileContributors));
    this.setProp(c.properties, "spdx:file:notice-text", file.noticeText);

    (file.attributionTexts || []).forEach((t: any) => { c.evidence.copyright.push({ text: t }); });
    (file.checksums || []).forEach((ch: any) => {
      c.hashes.push({ alg: this.normalizeCycloneHash(ch.algorithm), content: ch.checksumValue });
    });

    return this.pruneEmpty(c);
  }

  private convertCyclonePackage(comp: any): any {
    const props = this.toPropertyMap(comp.properties || []);
    const pkg: any = {
      name: comp.name,
      SPDXID: props["spdx:spdxid"] || "SPDXRef-Package-" + this.safeId(comp.name),
      versionInfo: comp.version,
      description: comp.description,
      copyrightText: comp.copyright,
      filesAnalyzed: props["spdx:files-analyzed"] === "true",
      licenseComments: props["spdx:license-comments"],
      licenseConcluded: props["spdx:license-concluded"],
      packageFileName: props["spdx:package:file-name"],
      sourceInfo: props["spdx:package:source-info"],
      summary: props["spdx:package:summary"],
      comment: props["spdx:comment"],
      builtDate: props["spdx:package:built-date"],
      releaseDate: props["spdx:package:release-date"],
      validUntilDate: props["spdx:package:valid-until-date"],
      checksums: [],
      externalRefs: [],
      attributionTexts: []
    };

    if (props["spdx:license-declared"]) {
      pkg.licenseDeclared = props["spdx:license-declared"];
    } else if (Array.isArray(comp.licenses) && comp.licenses.length === 1) {
      pkg.licenseDeclared = comp.licenses[0].expression || this.getValue(comp, "licenses.0.license.id");
    }

    if (comp.supplier && comp.supplier.name) {
      const em = this.getValue(comp, "supplier.contact.0.email");
      pkg.supplier = "Organization: " + comp.supplier.name + (em ? " (" + em + ")" : "");
    } else if (props["spdx:package:supplier"]) {
      pkg.supplier = props["spdx:package:supplier"];
    }
    if (props["spdx:package:originator"]) pkg.originator = props["spdx:package:originator"];

    (comp.hashes || []).forEach((h: any) => {
      pkg.checksums.push({ algorithm: this.normalizeSpdxHash(h.alg), checksumValue: h.content });
    });

    if (comp.purl) pkg.externalRefs.push({ referenceCategory: "PACKAGE-MANAGER", referenceType: "purl", referenceLocator: comp.purl });
    if (comp.cpe) pkg.externalRefs.push({ referenceCategory: "SECURITY", referenceType: "cpe23Type", referenceLocator: comp.cpe });

    (comp.externalReferences || []).forEach((r: any) => {
      pkg.externalRefs.push({ referenceCategory: "OTHER", referenceType: (r.type || "other").toUpperCase(), referenceLocator: r.url });
      if (r.type === "distribution" && !pkg.downloadLocation) pkg.downloadLocation = r.url;
      if (r.type === "website" && !pkg.homepage) pkg.homepage = r.url;
    });

    if (props["spdx:download-location"]) pkg.downloadLocation = props["spdx:download-location"];
    if (props["spdx:homepage"]) pkg.homepage = props["spdx:homepage"];

    (this.getValue(comp, "evidence.copyright") || []).forEach((e: any) => { if (e.text) pkg.attributionTexts.push(e.text); });

    const licIds: any[] = [];
    (this.getValue(comp, "evidence.licenses") || []).forEach((e: any) => {
      const id = this.getValue(e, "license.id");
      if (id) licIds.push(id);
    });
    if (licIds.length) pkg.licenseInfoFromFiles = licIds;

    return this.pruneEmpty(pkg);
  }

  private convertCycloneFile(comp: any): any {
    const props = this.toPropertyMap(comp.properties || []);
    const file: any = {
      fileName: comp.name,
      SPDXID: props["spdx:spdxid"] || "SPDXRef-File-" + this.safeId(comp.name),
      comment: props["spdx:comment"],
      licenseComments: props["spdx:license-comments"],
      noticeText: props["spdx:file:notice-text"],
      copyrightText: comp.copyright,
      licenseConcluded: props["spdx:license-concluded"],
      checksums: [],
      fileTypes: this.splitCsv(props["spdx:file:type"]),
      fileContributors: this.splitCsv(props["spdx:file:contributor"]),
      attributionTexts: []
    };

    (comp.hashes || []).forEach((h: any) => {
      file.checksums.push({ algorithm: this.normalizeSpdxHash(h.alg), checksumValue: h.content });
    });
    (this.getValue(comp, "evidence.copyright") || []).forEach((e: any) => {
      if (e.text) file.attributionTexts.push(e.text);
    });

    return this.pruneEmpty(file);
  }

  /* ╔════════════════════════════════════════════════════════════════════════════════════════════════════════════
     ║  HIGHLIGHT TOKENS
     ╚════════════════════════════════════════════════════════════════════════════════════════════════════════════ */

  private buildHighlightTokens(mappings: any[], selectedType: string): any {
    const left: any[] = [], right: any[] = [];
    mappings.forEach((m: any) => {
      const l = this.canonicalPath(m.source);
      const r = this.canonicalPath(m.target);
      if (l) left.push(l);
      if (r) right.push(r);
    });
    return { left: this.uniqueStrings(left), right: this.uniqueStrings(right) };
  }

  private uniqueStrings(arr: any[]): any[] {
    const seen: any = {}, res: any[] = [];
    arr.forEach((s: any) => { if (!seen[s]) { seen[s] = true; res.push(s); } });
    return res;
  }

  private dedupeTokens(tokens: any[]): any[] {
    const seen: any = {};
    return tokens.filter((t: any) => {
      if (!t || (!t.pathToken && !t.propertyName)) return false;
      const k = (t.pathToken || "") + "|" + (t.propertyName || "");
      if (seen[k]) return false;
      seen[k] = true;
      return true;
    });
  }

  private toHighlightToken(expr: string, section: string, selectedType: string, side: string): any {
    return {
      pathToken: this.toPathToken(this.normalizeExpression(expr), section, selectedType, side),
      propertyName: this.extractPropertyName(expr)
    };
  }

  private toPathToken(nexpr: string, section: string, selectedType: string, side: string): string {
    if (!nexpr) return "";
    if (selectedType === "spdx") {
      return side === "left" ? this.toSpdxPathToken(nexpr, section) : this.toCyclonePathToken(nexpr, section);
    }
    return side === "left" ? this.toCyclonePathToken(nexpr, section) : this.toSpdxPathToken(nexpr, section);
  }

  private toSpdxPathToken(nexpr: string, section: string): string {
    if (nexpr.indexOf("document.") === 0) return nexpr;
    if (nexpr.indexOf("creationinfo.") === 0) return "document." + nexpr;
    if (nexpr.indexOf("externaldocumentrefs") === 0 ||
      nexpr.indexOf("annotations") === 0 ||
      nexpr.indexOf("documentdescribes") === 0) return "document." + nexpr;
    if (this.isFileSection(section)) return "document.files." + nexpr;
    if (this.isPackageSection(section)) return "document.packages." + nexpr;
    return "document." + nexpr;
  }

  private toCyclonePathToken(nexpr: string, section: string): string {
    if (nexpr.indexOf("bom.") === 0) return nexpr;
    if (nexpr.indexOf("metadata.") === 0 || nexpr.indexOf("serialnumber") === 0)
      return "bom." + nexpr;
    if (this.isFileSection(section) || this.isPackageSection(section))
      return "bom.components." + nexpr;
    return "bom." + nexpr;
  }

  /* ╔════════════════════════════════════════════════════════════════════════════════════════════════════════════
     ║  MAPPING MATCH LOGIC
     ╚════════════════════════════════════════════════════════════════════════════════════════════════════════════ */

  private mappingMatchesNode(expr: string, section: string, normalizedPath: string, key: string, value: any, componentKind: string): boolean {
    const nexpr = this.normalizeExpression(expr);
    if (!nexpr) return false;
    if (this.sectionMatchesNode(section, normalizedPath, componentKind) === false) return false;

    const tok = this.toPathQueryToken(nexpr);
    if (tok && normalizedPath.indexOf(tok) >= 0) return true;

    const prop = this.extractPropertyName(expr);
    if (prop && key === "name" && normalizedPath.indexOf(".properties.") >= 0 && typeof value === "string") {
      return value.toLowerCase() === prop;
    }

    const tail = nexpr.split(".").pop();
    if (tail && normalizedPath.endsWith("." + tail)) return true;

    return false;
  }

  private sectionMatchesNode(section: string, normalizedPath: string, componentKind: string): boolean {
    if (!section) return true;
    if (this.isDocumentSection(section)) {
      return normalizedPath.indexOf("document.") === 0 ||
        normalizedPath.indexOf("bom.metadata.") === 0 ||
        normalizedPath === "bom.serialnumber";
    }
    if (this.isPackageSection(section)) {
      if (normalizedPath.indexOf("document.packages.") >= 0) return true;
      if (normalizedPath.indexOf("bom.components.") >= 0) return componentKind !== "file";
      return false;
    }
    if (this.isFileSection(section)) {
      if (normalizedPath.indexOf("document.files.") >= 0) return true;
      if (normalizedPath.indexOf("bom.components.") >= 0) return componentKind === "file";
      return false;
    }
    return true;
  }

  private isDocumentSection(s: string): boolean { return s.toLowerCase() === "document"; }
  private isPackageSection(s: string): boolean { const l = s.toLowerCase(); return l.indexOf("package") >= 0 && l.indexOf("file") < 0; }
  private isFileSection(s: string): boolean { return s.toLowerCase().indexOf("file") >= 0; }

  /* ╔════════════════════════════════════════════════════════════════════════════════════════════════════════════
     ║  UTILITY HELPERS
     ╚════════════════════════════════════════════════════════════════════════════════════════════════════════════ */

  private toPathQueryToken(nexpr: string): string {
    return nexpr.replace(/\[\]/g, "").replace(/\.[0-9]+\./g, ".");
  }

  private normalizeExpression(expr: string): string {
    if (!expr || typeof expr !== "string") return "";
    const c = expr.toLowerCase()
      .replace(/\"/g, "")
      .replace(/\s+with.*$/g, "")
      .replace(/\s*==.*$/g, "")
      .replace(/\s*=.*$/g, "")
      .replace(/\s+plus.*$/g, "")
      .replace(/\sand\s/g, " ")
      .replace(/\s*\+\s*/g, " ")
      .replace(/\s*\/\s*/g, " ")
      .replace(/\[["']([^"']+)["']\]/g, ".$1")
      .replace(/\[\]/g, "")
      .replace(/[^a-z0-9:._\-\s]/g, "")
      .trim();
    if (!c || c === "none") return "";
    return c.indexOf(" ") >= 0 ? c.split(" ")[0] : c;
  }

  private extractPropertyName(expr: string): string {
    if (!expr || typeof expr !== "string") return "";
    const bracket = expr.match(/\[name:\s*["']([^"']+)["']\]/i);
    if (bracket) return bracket[1].toLowerCase();
    const m = expr.toLowerCase().match(/(?:spdx|packages|annotations)[.:][a-z0-9:._-]+/);
    return m ? m[0] : "";
  }

  private targetExprToTreePath(t: string): string {
    return this.canonicalPath(t);
  }

  private canonicalPath(s: string): string {
    if (!s || typeof s !== "string") return "";
    return s.toLowerCase()
      .replace(/\s*=\s*\S+.*$/g, "")
      .replace(/\.?\[name:[^\]]*\]/g, ".name")
      .replace(/["']/g, "")
      .replace(/\[\]/g, "")
      .replace(/\[(\d+)\]/g, ".$1")
      .replace(/[^a-z0-9:._\-]/g, "")
      .replace(/\.+/g, ".")
      .replace(/^\.+|\.+$/g, "")
      .trim();
  }

  private normalizePath(path: string): string {
    return String(path || "").toLowerCase()
      .replace(/\"/g, "")
      .replace(/\[["']([^"']+)["']\]/g, ".$1")
      .replace(/[^a-z0-9:._\-]/g, "");
  }

  private getValue(obj: any, path: string): any {
    if (!obj || !path) return undefined;
    return path.split(".").reduce((acc: any, k: string) => {
      if (acc === undefined || acc === null) return undefined;
      return k.match(/^\d+$/) ? acc[Number(k)] : acc[k];
    }, obj);
  }

  private toPropertyMap(properties: any[]): any {
    const map: any = {};
    (properties || []).forEach((p: any) => { if (p && p.name) map[p.name] = p.value; });
    return map;
  }

  private setProp(arr: any[], name: string, value: any): void {
    if (value === undefined || value === null || value === "") return;
    const ex = arr.filter((p: any) => p.propertyname === name)[0];
    if (ex) { ex.value = value; return; }
    arr.push({ propertyname: name });
  }

  private parseParty(text: string): any {
    const r = { kind: "", name: "", email: "" };
    if (!text || typeof text !== "string") return r;
    const m = text.match(/^(Person|Organization):\s*([^(]+?)(?:\s*\(([^)]+)\))?$/);
    if (!m) { r.name = text; return r; }
    r.kind = m[1];
    r.name = (m[2] || "").trim();
    r.email = (m[3] || "").trim();
    return r;
  }

  private normalizeCycloneHash(alg: string): string {
    return { SHA1: "SHA-1", SHA256: "SHA-256", SHA384: "SHA-384", SHA512: "SHA-512" }[alg] || alg;
  }

  private normalizeSpdxHash(alg: string): string {
    return { "SHA-1": "SHA1", "SHA-256": "SHA256", "SHA-384": "SHA384", "SHA-512": "SHA512" }[alg] || alg;
  }

  private splitCsv(val: string): any {
    if (!val) return undefined;
    const parts = val.split(",").map((s: string) => s.trim()).filter(Boolean);
    return parts.length ? parts : undefined;
  }

  private arrayToCsv(val: any[]): any {
    return Array.isArray(val) && val.length ? val.join(", ") : undefined;
  }

  private safeId(text: string): string {
    return (text || "item").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
  }

  private toStringOrNull(val: any): any {
    return val === undefined || val === null ? undefined : String(val);
  }

  private pruneEmpty(node: any): any {
    if (Array.isArray(node)) {
      const arr = node.map((v: any) => this.pruneEmpty(v)).filter((v: any) => {
        if (v === undefined || v === null) return false;
        if (Array.isArray(v)) return v.length > 0;
        if (typeof v === "object") return Object.keys(v).length > 0;
        return true;
      });
      return arr;
    }
    if (node && typeof node === "object") {
      const obj: any = {};
      Object.keys(node).forEach((k: string) => {
        const v = this.pruneEmpty(node[k]);
        if (v === undefined || v === null) return;
        if (Array.isArray(v) && !v.length) return;
        if (typeof v === "object" && !Array.isArray(v) && !Object.keys(v).length) return;
        obj[k] = v;
      });
      return obj;
    }
    return node;
  }
}
