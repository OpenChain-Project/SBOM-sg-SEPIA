/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: MIT */
/*
 * Comprehensive unit tests for AppComponent
 */

import {
  ComponentFixture,
  TestBed,
} from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA,
} from '@angular/core';

import { AppComponent } from './app.component';

// ─────────────────────────────────────────────────────────────────────────────
// Test Suite
// ─────────────────────────────────────────────────────────────────────────────

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  // ── Module Setup ────────────────────────────────────────────────────────────

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AppComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 1. Component Instantiation
  // ══════════════════════════════════════════════════════════════════════════

  describe('1. Component Instantiation', () => {
    it('should create the AppComponent successfully', () => {
      expect(component).toBeTruthy();
    });

    it('should be an instance of AppComponent', () => {
      expect(component instanceof AppComponent).toBeTrue();
    });

    it('should initialise without throwing any errors', () => {
      expect(() => {
        const localFixture = TestBed.createComponent(AppComponent);
        localFixture.detectChanges();
      }).not.toThrow();
    });

    it('should produce a defined component instance', () => {
      expect(component).toBeDefined();
    });

    it('should produce a defined fixture', () => {
      expect(fixture).toBeDefined();
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 2. Title Property
  // ══════════════════════════════════════════════════════════════════════════

  describe('2. Title Property', () => {
    it('should have a title property defined', () => {
      expect(component.title).toBeDefined();
    });

    it('should initialise title with the value "S E P I A"', () => {
      expect(component.title).toBe('S E P I A');
    });

    it('should have title as a string type', () => {
      expect(typeof component.title).toBe('string');
    });

    it('should have a non-empty title', () => {
      expect(component.title.length).toBeGreaterThan(0);
    });

    it('should have title that starts with "S"', () => {
      expect(component.title.startsWith('S')).toBeTrue();
    });

    it('should have title that ends with "A"', () => {
      expect(component.title.endsWith('A')).toBeTrue();
    });

    it('should allow title to be updated at runtime', () => {
      component.title = 'NEW TITLE';
      expect(component.title).toBe('NEW TITLE');
    });

    it('should reflect a dynamically changed title value', () => {
      const newTitle = 'UPDATED APP';
      component.title = newTitle;
      fixture.detectChanges();
      expect(component.title).toBe(newTitle);
    });

    it('should have title that contains all six spaced characters', () => {
      const chars = component.title.split(' ');
      expect(chars).toContain('S');
      expect(chars).toContain('E');
      expect(chars).toContain('P');
      expect(chars).toContain('I');
      expect(chars).toContain('A');
    });

    it('should have title with exactly 5 words separated by spaces', () => {
      const words = component.title.trim().split(' ').filter(w => w.length > 0);
      expect(words.length).toBe(5);
    });

    it('should not have title as null', () => {
      expect(component.title).not.toBeNull();
    });

    it('should not have title as undefined', () => {
      expect(component.title).not.toBeUndefined();
    });

    it('should preserve title case (all uppercase letters)', () => {
      const upperCaseTitle = component.title.replace(/\s/g, '').toUpperCase();
      const noSpaceTitle = component.title.replace(/\s/g, '');
      expect(noSpaceTitle).toBe(upperCaseTitle);
    });

    it('should return to original title after multiple reassignments', () => {
      const original = component.title;
      component.title = 'TEMP TITLE';
      component.title = original;
      expect(component.title).toBe('S E P I A');
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 3. Component Selector & Metadata
  // ══════════════════════════════════════════════════════════════════════════

  describe('3. Component Selector and Metadata', () => {
    it('should use the selector app-root', () => {
      const selectors = (AppComponent as any).ɵcmp?.selectors;
      expect(selectors?.[0]?.[0]).toBe('app-root');
    });

    it('should have a nativeElement defined', () => {
      expect(fixture.nativeElement).toBeDefined();
    });

    it('should expose debugElement on the fixture', () => {
      expect(fixture.debugElement).toBeDefined();
    });

    it('should have componentInstance match the component reference', () => {
      expect(fixture.componentInstance).toBe(component);
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 4. Template Rendering
  // ══════════════════════════════════════════════════════════════════════════

  describe('4. Template Rendering', () => {
    it('should render the root host element in the DOM', () => {
      const compiled: HTMLElement = fixture.nativeElement;
      expect(compiled).toBeTruthy();
    });

    it('should not throw during initial change detection', () => {
      expect(() => fixture.detectChanges()).not.toThrow();
    });

    it('should remain stable after multiple detectChanges calls', () => {
      for (let i = 0; i < 5; i++) {
        expect(() => fixture.detectChanges()).not.toThrow();
      }
      expect(component).toBeTruthy();
    });

    it('should re-render cleanly after title is changed', () => {
      component.title = 'CHANGED';
      expect(() => fixture.detectChanges()).not.toThrow();
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 5. Change Detection
  // ══════════════════════════════════════════════════════════════════════════

  describe('5. Change Detection', () => {
    it('should reflect component state changes after detectChanges', () => {
      component.title = 'SEPIA TOOL';
      fixture.detectChanges();
      expect(component.title).toBe('SEPIA TOOL');
    });

    it('fixture should be stable after ngOnInit equivalent', () => {
      fixture.detectChanges();
      expect(fixture.isStable()).toBeTrue();
    });

    it('component should retain state between multiple detectChanges', () => {
      fixture.detectChanges();
      fixture.detectChanges();
      expect(component.title).toBe('S E P I A');
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 6. Component Isolation
  // ══════════════════════════════════════════════════════════════════════════

  describe('6. Component Instance Isolation', () => {
    it('should maintain independent state across multiple instances', () => {
      const fixture2 = TestBed.createComponent(AppComponent);
      const component2 = fixture2.componentInstance;
      fixture2.detectChanges();

      component.title = 'INSTANCE ONE';
      expect(component2.title).toBe('S E P I A');

      fixture2.destroy();
    });

    it('mutating one instance title should not affect another instance', () => {
      const fixture2 = TestBed.createComponent(AppComponent);
      const component2 = fixture2.componentInstance;
      fixture2.detectChanges();

      component2.title = 'INSTANCE TWO';
      expect(component.title).toBe('S E P I A');

      fixture2.destroy();
    });

    it('each instance should begin with the same default title', () => {
      const fixture2 = TestBed.createComponent(AppComponent);
      const component2 = fixture2.componentInstance;
      fixture2.detectChanges();

      expect(component.title).toBe(component2.title);

      fixture2.destroy();
    });
  });

  // ══════════════════════════════════════════════════════════════════════════
  // 7. Lifecycle Safety
  // ══════════════════════════════════════════════════════════════════════════

  describe('7. Lifecycle Safety', () => {
    it('should not throw when the fixture is destroyed', () => {
      expect(() => fixture.destroy()).not.toThrow();
    });

    it('should complete without errors through the full lifecycle', () => {
      const localFixture = TestBed.createComponent(AppComponent);
      const localComponent = localFixture.componentInstance;
      expect(() => {
        localFixture.detectChanges();
        localComponent.title = 'LIFECYCLE TEST';
        localFixture.detectChanges();
        localFixture.destroy();
      }).not.toThrow();
    });
  });
});
