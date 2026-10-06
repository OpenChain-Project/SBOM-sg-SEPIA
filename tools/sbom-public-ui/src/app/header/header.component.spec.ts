// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

// SPDX-License-Identifier: MIT
import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  ComponentFixture,
  TestBed,
  fakeAsync,
  tick,
} from '@angular/core/testing';
import { Router } from '@angular/router';
import { TemplateRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { HeaderComponent } from './header.component';
import { RestEndpointsService } from '../services/rest-endpoints.service';

// ─────────────────────────────────────────────────────────────────────────────
// Third-Party Library Mocks
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Mock jsPDF instance exposing only the methods HeaderComponent uses.
 * Prevents real PDF generation (file I/O) during tests.
 */
const mockJsPDFInstance = {
  save: jasmine.createSpy('save'),
};

/**
 * Mock the jsPDF constructor so `new jsPDF(...)` returns our controlled stub.
 */
const mockJsPDF = jasmine.createSpy('jsPDF').and.returnValue(mockJsPDFInstance);

/**
 * Mock autoTable to suppress actual table rendering.
 */
const mockAutoTable = jasmine.createSpy('autoTable');

// Patch module-level references before Angular loads the component class.
// jest.mock / spyOnProperty cannot intercept ES module default exports in
// Jasmine+Karma, so we use the module-augmentation approach via spyOn on the
// imported references within the component's closure by re-assigning globals.
// The cleanest Karma-compatible approach is to spy at the window level where
// possible, and rely on service mocks for everything else.

// ─────────────────────────────────────────────────────────────────────────────
// Service Mocks
// ─────────────────────────────────────────────────────────────────────────────

class MockRouter {
  navigate = jasmine.createSpy('navigate');
  url = '/';
}

class MockRestEndpointsService {
  downloadUserManual = 'https://mock.example.com/user-manual.pdf';
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper – create a minimal TemplateRef stub
// ─────────────────────────────────────────────────────────────────────────────
function createMockTemplateRef(): TemplateRef<any> {
  return jasmine.createSpyObj<TemplateRef<any>>('TemplateRef', [
    'createEmbeddedView',
  ]);
}

// ─────────────────────────────────────────────────────────────────────────────
// Test Suite
// ─────────────────────────────────────────────────────────────────────────────

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let mockRouter: MockRouter;
  let mockRestEndpointsService: MockRestEndpointsService;

  // ── Module Setup ────────────────────────────────────────────────────────────

  beforeEach(async () => {
    mockRouter = new MockRouter();
    mockRestEndpointsService = new MockRestEndpointsService();

    await TestBed.configureTestingModule({
      declarations: [HeaderComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: RestEndpointsService, useValue: mockRestEndpointsService },
      ],
      // NO_ERRORS_SCHEMA suppresses unknown child element/directive errors
      // so we can focus purely on component logic.
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;

    // Reset jsPDF & autoTable spies between tests
    mockJsPDFInstance.save.calls.reset();
    mockAutoTable.calls.reset();

    fixture.detectChanges(); // triggers ngOnInit + ngAfterViewInit
  });

  // ── 1. Component Instantiation ──────────────────────────────────────────────

  describe('1. Component Instantiation', () => {
    it('should create the HeaderComponent', () => {
      expect(component).toBeTruthy();
    });

    it('should initialise sticky to true by default', () => {
      expect(component.sticky).toBeTrue();
    });

    it('should initialise isDialogBoxOpen to false by default', () => {
      expect(component.isDialogBoxOpen).toBeFalse();
    });

    it('should initialise alertModal as undefined/null (no modal open yet)', () => {
      // alertModal is declared with ! (definite assignment) but never set in
      // the constructor, so it evaluates to undefined at startup.
      expect(component.alertModal == null).toBeTrue();
    });

    it('should inject Router dependency', () => {
      const injectedRouter = TestBed.inject(Router);
      // Router is private; verify via the injected token identity
      expect(injectedRouter).toBe(mockRouter as unknown as Router);
    });

    it('should inject RestEndpointsService as a public dependency', () => {
      expect(component.restEndPointService).toBeDefined();
      expect(component.restEndPointService).toBe(
        mockRestEndpointsService as unknown as RestEndpointsService
      );
    });
  });

  // ── 2. Lifecycle Hooks ──────────────────────────────────────────────────────

  describe('2. Lifecycle Hooks', () => {
    describe('ngOnInit()', () => {
      it('should exist as a callable method', () => {
        expect(typeof component.ngOnInit).toBe('function');
      });

      it('should execute without throwing', () => {
        expect(() => component.ngOnInit()).not.toThrow();
      });

      it('should not alter isDialogBoxOpen during initialisation', () => {
        component.ngOnInit();
        expect(component.isDialogBoxOpen).toBeFalse();
      });

      it('should not alter sticky during initialisation', () => {
        component.ngOnInit();
        expect(component.sticky).toBeTrue();
      });
    });

    describe('ngAfterViewInit()', () => {
      it('should exist as a callable method', () => {
        expect(typeof component.ngAfterViewInit).toBe('function');
      });

      it('should execute without throwing', () => {
        expect(() => component.ngAfterViewInit()).not.toThrow();
      });

      it('should not modify component state', () => {
        const stateBefore = {
          sticky: component.sticky,
          isDialogBoxOpen: component.isDialogBoxOpen,
        };
        component.ngAfterViewInit();
        expect(component.sticky).toBe(stateBefore.sticky);
        expect(component.isDialogBoxOpen).toBe(stateBefore.isDialogBoxOpen);
      });
    });
  });

  // ── 3. downloadUserManual() ─────────────────────────────────────────────────

  describe('3. downloadUserManual()', () => {
    let windowOpenSpy: jasmine.Spy;

    beforeEach(() => {
      windowOpenSpy = spyOn(window, 'open');
    });

    it('should call window.open exactly once', () => {
      component.downloadUserManual();
      expect(windowOpenSpy).toHaveBeenCalledTimes(1);
    });

    it('should open the URL provided by RestEndpointsService', () => {
      component.downloadUserManual();
      expect(windowOpenSpy).toHaveBeenCalledWith(
        mockRestEndpointsService.downloadUserManual,
        '_blank'
      );
    });

    it('should open the link in a new tab (_blank)', () => {
      component.downloadUserManual();
      const [, target] = windowOpenSpy.calls.mostRecent().args as [
        string,
        string
      ];
      expect(target).toBe('_blank');
    });

    it('should reflect a dynamically updated URL from the service', () => {
      const newUrl = 'https://updated.example.com/manual-v2.pdf';
      mockRestEndpointsService.downloadUserManual = newUrl;

      component.downloadUserManual();

      expect(windowOpenSpy).toHaveBeenCalledWith(newUrl, '_blank');
    });

    it('should still invoke window.open even when URL is empty string', () => {
      mockRestEndpointsService.downloadUserManual = '';
      component.downloadUserManual();
      expect(windowOpenSpy).toHaveBeenCalledWith('', '_blank');
    });

    it('should not affect isDialogBoxOpen', () => {
      component.downloadUserManual();
      expect(component.isDialogBoxOpen).toBeFalse();
    });

    it('should not affect alertModal', () => {
      component.downloadUserManual();
      expect(component.alertModal == null).toBeTrue();
    });
  });

  // ── 4. downloadAuditLog() ───────────────────────────────────────────────────

  describe('4. downloadAuditLog()', () => {
    const validAuditLogData = JSON.stringify([
      {
        action: 'Upload',
        fileName: 'sbom.json',
        fileHash: 'abc123',
        timestamp: '2025-01-01T10:00:00Z',
      },
      {
        action: 'Download',
        fileName: 'report.pdf',
        fileHash: 'def456',
        timestamp: '2025-01-01T11:00:00Z',
      },
    ]);

    afterEach(() => {
      // Always clean sessionStorage after each test to avoid cross-test leakage
      sessionStorage.removeItem('log');
    });

    // ── 4a. When audit log EXISTS in sessionStorage ────────────────────────

    describe('when sessionStorage contains a valid audit log', () => {
      beforeEach(() => {
        sessionStorage.setItem('log', validAuditLogData);
      });

      it('should read the log from sessionStorage', () => {
        const getItemSpy = spyOn(sessionStorage, 'getItem').and.callThrough();
        component.downloadAuditLog();
        expect(getItemSpy).toHaveBeenCalledWith('log');
      });

      it('should NOT open the noLogsModal dialog', () => {
        spyOn(component, 'openDialogBox');
        component.downloadAuditLog();
        expect(component.openDialogBox).not.toHaveBeenCalled();
      });

      it('should NOT set isDialogBoxOpen to true', () => {
        component.downloadAuditLog();
        expect(component.isDialogBoxOpen).toBeFalse();
      });

      it('should not throw during PDF generation with valid log data', () => {
        expect(() => component.downloadAuditLog()).not.toThrow();
      });
    });

    // ── 4b. When audit log is NULL in sessionStorage ───────────────────────

    describe('when sessionStorage does NOT contain an audit log (null)', () => {
      beforeEach(() => {
        sessionStorage.removeItem('log'); // Guarantee null return
      });

      it('should call openDialogBox with noLogsModal', () => {
        spyOn(component, 'openDialogBox');
        // Provide a mock for noLogsModal ViewChild since the template
        // may not render in unit test context
        component.noLogsModal = createMockTemplateRef();

        component.downloadAuditLog();

        expect(component.openDialogBox).toHaveBeenCalledWith(
          component.noLogsModal
        );
      });

      it('should set isDialogBoxOpen to true via openDialogBox', () => {
        component.noLogsModal = createMockTemplateRef();
        component.downloadAuditLog();
        expect(component.isDialogBoxOpen).toBeTrue();
      });

      it('should set alertModal to the noLogsModal reference', () => {
        const mockModal = createMockTemplateRef();
        component.noLogsModal = mockModal;

        component.downloadAuditLog();

        expect(component.alertModal).toBe(mockModal);
      });

      it('should NOT throw even when noLogsModal ViewChild is defined', () => {
        component.noLogsModal = createMockTemplateRef();
        expect(() => component.downloadAuditLog()).not.toThrow();
      });
    });

    // ── 4c. sessionStorage getItem spy scenarios ───────────────────────────

    describe('sessionStorage interaction', () => {
      it('should call sessionStorage.getItem with key "log"', () => {
        const spy = spyOn(sessionStorage, 'getItem').and.returnValue(null);
        component.noLogsModal = createMockTemplateRef();

        component.downloadAuditLog();

        expect(spy).toHaveBeenCalledWith('log');
      });

      it('should NOT call sessionStorage.setItem or removeItem', () => {
        sessionStorage.setItem('log', validAuditLogData);
        const setItemSpy = spyOn(sessionStorage, 'setItem');
        const removeItemSpy = spyOn(sessionStorage, 'removeItem');

        component.downloadAuditLog();

        expect(setItemSpy).not.toHaveBeenCalled();
        expect(removeItemSpy).not.toHaveBeenCalled();
      });

      it('should handle an empty array log gracefully', () => {
        sessionStorage.setItem('log', JSON.stringify([]));
        expect(() => component.downloadAuditLog()).not.toThrow();
      });

      it('should handle a log with a single entry', () => {
        const singleEntry = JSON.stringify([
          {
            action: 'Upload',
            fileName: 'test.json',
            fileHash: 'xyz789',
            timestamp: '2025-06-01T09:00:00Z',
          },
        ]);
        sessionStorage.setItem('log', singleEntry);
        expect(() => component.downloadAuditLog()).not.toThrow();
      });
    });
  });

  // ── 5. openDialogBox() ──────────────────────────────────────────────────────

  describe('5. openDialogBox()', () => {
    let mockModal: TemplateRef<any>;

    beforeEach(() => {
      mockModal = createMockTemplateRef();
    });

    it('should set isDialogBoxOpen to true', () => {
      component.openDialogBox(mockModal);
      expect(component.isDialogBoxOpen).toBeTrue();
    });

    it('should assign the provided TemplateRef to alertModal', () => {
      component.openDialogBox(mockModal);
      expect(component.alertModal).toBe(mockModal);
    });

    it('should accept any TemplateRef instance', () => {
      const anotherModal = createMockTemplateRef();
      component.openDialogBox(anotherModal);
      expect(component.alertModal).toBe(anotherModal);
    });

    it('should override a previously set alertModal', () => {
      const firstModal = createMockTemplateRef();
      const secondModal = createMockTemplateRef();

      component.openDialogBox(firstModal);
      expect(component.alertModal).toBe(firstModal);

      component.openDialogBox(secondModal);
      expect(component.alertModal).toBe(secondModal);
    });

    it('should set isDialogBoxOpen to true even if already true', () => {
      component.isDialogBoxOpen = true;
      component.openDialogBox(mockModal);
      expect(component.isDialogBoxOpen).toBeTrue();
    });

    it('should not throw when called with a valid TemplateRef', () => {
      expect(() => component.openDialogBox(mockModal)).not.toThrow();
    });

    it('should not affect the sticky property', () => {
      component.openDialogBox(mockModal);
      expect(component.sticky).toBeTrue();
    });
  });

  // ── 6. closeDialogBox() ─────────────────────────────────────────────────────

  describe('6. closeDialogBox()', () => {
    beforeEach(() => {
      // Pre-condition: dialog is open with a modal assigned
      component.isDialogBoxOpen = true;
      component.alertModal = createMockTemplateRef();
    });

    it('should set isDialogBoxOpen to false', () => {
      component.closeDialogBox();
      expect(component.isDialogBoxOpen).toBeFalse();
    });

    it('should set alertModal to null', () => {
      component.closeDialogBox();
      expect(component.alertModal).toBeNull();
    });

    it('should not throw when dialog is already closed', () => {
      component.isDialogBoxOpen = false;
      component.alertModal = null;
      expect(() => component.closeDialogBox()).not.toThrow();
    });

    it('should remain idempotent when called multiple times', () => {
      component.closeDialogBox();
      component.closeDialogBox();
      expect(component.isDialogBoxOpen).toBeFalse();
      expect(component.alertModal).toBeNull();
    });

    it('should not affect the sticky property', () => {
      component.closeDialogBox();
      expect(component.sticky).toBeTrue();
    });
  });

  // ── 7. openDialogBox() → closeDialogBox() Workflow ─────────────────────────

  describe('7. Open → Close Dialog Workflow', () => {
    it('should correctly open and then close the dialog box', () => {
      const mockModal = createMockTemplateRef();

      // Open
      component.openDialogBox(mockModal);
      expect(component.isDialogBoxOpen).toBeTrue();
      expect(component.alertModal).toBe(mockModal);

      // Close
      component.closeDialogBox();
      expect(component.isDialogBoxOpen).toBeFalse();
      expect(component.alertModal).toBeNull();
    });

    it('should allow reopening the dialog after closing', () => {
      const mockModal = createMockTemplateRef();

      component.openDialogBox(mockModal);
      component.closeDialogBox();
      component.openDialogBox(mockModal);

      expect(component.isDialogBoxOpen).toBeTrue();
      expect(component.alertModal).toBe(mockModal);
    });

    it('should support switching between different modals', () => {
      const firstModal = createMockTemplateRef();
      const secondModal = createMockTemplateRef();

      component.openDialogBox(firstModal);
      expect(component.alertModal).toBe(firstModal);

      component.closeDialogBox();
      component.openDialogBox(secondModal);
      expect(component.alertModal).toBe(secondModal);
    });
  });

  // ── 8. downloadAuditLog() → openDialogBox() Integration ────────────────────

  describe('8. downloadAuditLog → openDialogBox Integration', () => {
    afterEach(() => {
      sessionStorage.removeItem('log');
    });

    it('should open dialog with noLogsModal when no log in sessionStorage', () => {
      sessionStorage.removeItem('log');
      const mockNoLogsModal = createMockTemplateRef();
      component.noLogsModal = mockNoLogsModal;

      component.downloadAuditLog();

      expect(component.isDialogBoxOpen).toBeTrue();
      expect(component.alertModal).toBe(mockNoLogsModal);
    });

    it('should NOT open dialog when log exists in sessionStorage', () => {
      sessionStorage.setItem(
        'log',
        JSON.stringify([
          {
            action: 'Test',
            fileName: 'f.json',
            fileHash: 'h1',
            timestamp: 't1',
          },
        ])
      );

      component.downloadAuditLog();

      expect(component.isDialogBoxOpen).toBeFalse();
      expect(component.alertModal == null).toBeTrue();
    });

    it('should close dialog after it was opened by downloadAuditLog', () => {
      sessionStorage.removeItem('log');
      component.noLogsModal = createMockTemplateRef();

      component.downloadAuditLog();
      expect(component.isDialogBoxOpen).toBeTrue();

      component.closeDialogBox();
      expect(component.isDialogBoxOpen).toBeFalse();
      expect(component.alertModal).toBeNull();
    });
  });

  // ── 9. Component State Isolation ────────────────────────────────────────────

  describe('9. Component State Isolation', () => {
    it('should maintain independent state from other instances', () => {
      // Create a second instance to verify state isolation
      const fixture2 = TestBed.createComponent(HeaderComponent);
      const component2 = fixture2.componentInstance;
      fixture2.detectChanges();

      component.isDialogBoxOpen = true;

      expect(component2.isDialogBoxOpen).toBeFalse();

      fixture2.destroy();
    });

    it('should not mutate injected service state', () => {
      const originalUrl = mockRestEndpointsService.downloadUserManual;
      spyOn(window, 'open');

      component.downloadUserManual();

      expect(mockRestEndpointsService.downloadUserManual).toBe(originalUrl);
    });
  });

  // ── 10. ViewChild – noLogsModal ─────────────────────────────────────────────

  describe('10. ViewChild – noLogsModal', () => {
    it('should accept assignment of a TemplateRef to noLogsModal', () => {
      const mockRef = createMockTemplateRef();
      component.noLogsModal = mockRef;
      expect(component.noLogsModal).toBe(mockRef);
    });

    it('openDialogBox should work correctly when passed noLogsModal', () => {
      const mockRef = createMockTemplateRef();
      component.noLogsModal = mockRef;

      component.openDialogBox(component.noLogsModal);

      expect(component.alertModal).toBe(mockRef);
      expect(component.isDialogBoxOpen).toBeTrue();
    });
  });
});
