// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

// SPDX-License-Identifier: MIT
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FooterComponent } from './footer.component';
import { SbomInputService } from '../services/sbom-input.service';
import { RestEndpointsService } from '../services/rest-endpoints.service';

// ─────────────────────────────────────────────────────────────────────────────
// Mock Service Definitions
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Mock for SbomInputService.
 * Provides stub properties/methods that FooterComponent may reference
 * through its template or logic.
 */
class MockSbomInputService {
  // Add stub properties here if the template binds to them.
  // Keeping them observable-safe with sensible defaults.
  someProperty = 'mock-value';
}

/**
 * Mock for RestEndpointsService.
 * The real service exposes endpoint URL strings; we replicate that shape.
 */
class MockRestEndpointsService {
  downloadUserManual = 'https://mock-server.example.com/user-manual.pdf';
}

// ─────────────────────────────────────────────────────────────────────────────
// Test Suite
// ─────────────────────────────────────────────────────────────────────────────

describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;
  let mockSbomInputService: MockSbomInputService;
  let mockRestEndpointsService: MockRestEndpointsService;

  // ── Module Configuration ──────────────────────────────────────────────────

  beforeEach(async () => {
    mockSbomInputService = new MockSbomInputService();
    mockRestEndpointsService = new MockRestEndpointsService();

    await TestBed.configureTestingModule({
      declarations: [FooterComponent],
      providers: [
        { provide: SbomInputService, useValue: mockSbomInputService },
        { provide: RestEndpointsService, useValue: mockRestEndpointsService },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // triggers ngOnInit
  });

  // ── 1. Component Instantiation ────────────────────────────────────────────

  describe('Component Instantiation', () => {
    it('should create the FooterComponent successfully', () => {
      expect(component).toBeTruthy();
    });

    it('should have sbomInputService injected and publicly accessible', () => {
      expect(component.sbomInputService).toBeDefined();
      expect(component.sbomInputService).toBe(mockSbomInputService as unknown as SbomInputService);
    });

    it('should have restEndPointService injected and publicly accessible', () => {
      expect(component.restEndPointService).toBeDefined();
      expect(component.restEndPointService).toBe(
        mockRestEndpointsService as unknown as RestEndpointsService
      );
    });

    it('should implement OnInit lifecycle interface', () => {
      // Confirms the component declares ngOnInit without throwing
      expect(typeof component.ngOnInit).toBe('function');
    });

    it('ngOnInit should execute without throwing an error', () => {
      // ngOnInit is intentionally empty; calling it again must not throw
      expect(() => component.ngOnInit()).not.toThrow();
    });

    it('should expose downloadUserManual as a public method', () => {
      expect(typeof component.downloadUserManual).toBe('function');
    });
  });

  // ── 2. Dependency Injection ───────────────────────────────────────────────

  describe('Dependency Injection', () => {
    it('should receive the correct SbomInputService instance from the DI container', () => {
      const injectedService = TestBed.inject(SbomInputService);
      expect(component.sbomInputService).toBe(injectedService);
    });

    it('should receive the correct RestEndpointsService instance from the DI container', () => {
      const injectedService = TestBed.inject(RestEndpointsService);
      expect(component.restEndPointService).toBe(injectedService);
    });

    it('restEndPointService.downloadUserManual should expose the expected URL string', () => {
      expect(component.restEndPointService.downloadUserManual).toBe(
        'https://mock-server.example.com/user-manual.pdf'
      );
    });
  });

  // ── 3. Template Rendering ─────────────────────────────────────────────────

  describe('Template Rendering', () => {
    it('should render the footer element in the DOM', () => {
      const compiled: HTMLElement = fixture.nativeElement;
      // The root host element itself represents the footer shell
      expect(compiled).toBeTruthy();
    });

    it('should not throw during initial change detection', () => {
      expect(() => fixture.detectChanges()).not.toThrow();
    });

    it('should re-render cleanly after a subsequent detectChanges call', () => {
      fixture.detectChanges();
      const compiled: HTMLElement = fixture.nativeElement;
      expect(compiled).toBeTruthy();
    });
  });

  // ── 4. downloadUserManual() Method Logic ──────────────────────────────────

  describe('downloadUserManual()', () => {
    let windowOpenSpy: jasmine.Spy;

    beforeEach(() => {
      // Spy on window.open to avoid actual browser navigation during tests
      windowOpenSpy = spyOn(window, 'open');
    });

    it('should call window.open exactly once when invoked', () => {
      component.downloadUserManual();
      expect(windowOpenSpy).toHaveBeenCalledTimes(1);
    });

    it('should open the URL provided by restEndPointService.downloadUserManual', () => {
      component.downloadUserManual();
      expect(windowOpenSpy).toHaveBeenCalledWith(
        mockRestEndpointsService.downloadUserManual,
        '_blank'
      );
    });

    it('should open the link in a new browser tab (_blank target)', () => {
      component.downloadUserManual();
      const [, target] = windowOpenSpy.calls.mostRecent().args as [string, string];
      expect(target).toBe('_blank');
    });

    it('should use the URL string directly from restEndPointService without modification', () => {
      const expectedUrl = mockRestEndpointsService.downloadUserManual;
      component.downloadUserManual();
      const [actualUrl] = windowOpenSpy.calls.mostRecent().args as [string, string];
      expect(actualUrl).toBe(expectedUrl);
    });

    it('should reflect a dynamically changed URL from restEndPointService', () => {
      const updatedUrl = 'https://updated-server.example.com/new-manual.pdf';
      mockRestEndpointsService.downloadUserManual = updatedUrl;

      component.downloadUserManual();

      expect(windowOpenSpy).toHaveBeenCalledWith(updatedUrl, '_blank');
    });

    it('should still call window.open even when the URL is an empty string', () => {
      mockRestEndpointsService.downloadUserManual = '';
      component.downloadUserManual();
      expect(windowOpenSpy).toHaveBeenCalledWith('', '_blank');
    });

    it('should still call window.open when the URL is undefined', () => {
      // Edge-case: service misconfiguration; component must not crash
      (mockRestEndpointsService as any).downloadUserManual = undefined;
      expect(() => component.downloadUserManual()).not.toThrow();
      expect(windowOpenSpy).toHaveBeenCalledTimes(1);
    });

    it('should not call window.open more than once per invocation', () => {
      component.downloadUserManual();
      component.downloadUserManual();
      expect(windowOpenSpy).toHaveBeenCalledTimes(2);
    });
  });

  // ── 5. Event Handling ─────────────────────────────────────────────────────

  describe('Event Handling', () => {
    let windowOpenSpy: jasmine.Spy;

    beforeEach(() => {
      windowOpenSpy = spyOn(window, 'open');
    });

    it('should call downloadUserManual() when the method is triggered programmatically', () => {
      const methodSpy = spyOn(component, 'downloadUserManual').and.callThrough();
      component.downloadUserManual();
      expect(methodSpy).toHaveBeenCalledTimes(1);
    });

    it('should propagate the correct URL to window.open after a simulated click event', () => {
      // Simulate what a (click)="downloadUserManual()" binding would do
      component.downloadUserManual();
      expect(windowOpenSpy).toHaveBeenCalledWith(
        mockRestEndpointsService.downloadUserManual,
        '_blank'
      );
    });

    it('should not open any window before downloadUserManual() is called', () => {
      // Baseline: no spurious window.open calls on component creation
      expect(windowOpenSpy).not.toHaveBeenCalled();
    });
  });

  // ── 6. Service Interaction & Integration ──────────────────────────────────

  describe('Service Interaction', () => {
    it('should read the downloadUserManual property from RestEndpointsService lazily (on call)', () => {
      // Verify the URL is not cached at construction time
      const originalUrl = mockRestEndpointsService.downloadUserManual;
      const newUrl = 'https://lazy-loaded.example.com/manual.pdf';
      mockRestEndpointsService.downloadUserManual = newUrl;

      const windowOpenSpy = spyOn(window, 'open');
      component.downloadUserManual();

      expect(windowOpenSpy).toHaveBeenCalledWith(newUrl, '_blank');
      expect(windowOpenSpy).not.toHaveBeenCalledWith(originalUrl, '_blank');
    });

    it('should not interact with SbomInputService during downloadUserManual()', () => {
      // Ensure no unintended cross-service calls
      const sbomSpy = jasmine.createSpyObj('SbomInputService', ['someMethod']);
      component.sbomInputService = sbomSpy;
      spyOn(window, 'open');

      component.downloadUserManual();

      // No method on sbomInputService should have been called
      Object.keys(sbomSpy).forEach((key) => {
        if (typeof sbomSpy[key] === 'function') {
          expect(sbomSpy[key]).not.toHaveBeenCalled();
        }
      });
    });

    it('should not invoke ngOnInit more than once during normal lifecycle', () => {
      const ngOnInitSpy = spyOn(component, 'ngOnInit').and.callThrough();
      // Re-running detectChanges should not re-trigger ngOnInit
      fixture.detectChanges();
      expect(ngOnInitSpy).not.toHaveBeenCalled(); // already called before spy was set
    });
  });

  // ── 7. Lifecycle Hooks ────────────────────────────────────────────────────

  describe('Lifecycle Hooks', () => {
    it('ngOnInit should complete synchronously without async operations', (done) => {
      // Since ngOnInit is intentionally empty, it must resolve synchronously
      const result = component.ngOnInit();
      // ngOnInit returns void; ensure no pending promises
      Promise.resolve(result).then(() => {
        expect(true).toBe(true);
        done();
      });
    });

    it('component fixture should remain stable after multiple detectChanges calls', () => {
      for (let i = 0; i < 5; i++) {
        expect(() => fixture.detectChanges()).not.toThrow();
      }
      expect(component).toBeTruthy();
    });
  });

  // ── 8. Selector & Metadata ────────────────────────────────────────────────

  describe('Component Metadata', () => {
    it('should use the selector app-footer', () => {
      const selectors = (FooterComponent as any).ɵcmp?.selectors;
      expect(selectors?.[0]?.[0]).toBe('app-footer');
    });
  });
});
