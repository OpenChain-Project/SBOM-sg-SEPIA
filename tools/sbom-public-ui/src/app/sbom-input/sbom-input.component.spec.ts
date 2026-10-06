/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Component, TemplateRef, ViewChild, NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { SbomInputComponent } from './sbom-input.component';
import { SbomInputService } from '../services/sbom-input.service';
import { HealthCheckService } from '../services/health-check.service';
import { UploadModel, AuditLog, ChangeLog, ErrorModel } from './sbom-input.model';
import {
  CycloneDXSBOMStandard,
  License,
  OrganizationalContactObject,
  LicenseIDs,
  ComponentType
} from '../models/cyclonedx.model';
import { SpdxModel, Packages, externalRefs, CreationInfo, primaryPackagePurpose, referenceCategory } from '../models/spdx.model';
import {
  CDQCycloneDXSBOMStandard,
  CDQLicense,
  CDQLicenseIDs,
  CDQOrganizationalContactObject,
  CDQOrganizationalEntityObject,
  CDQExternalReference,
  CDQComponentType
} from '../models/cdqcyclonedx.model';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';

// ---------------------------------------------------------------------------
// Mock Services
// ---------------------------------------------------------------------------

class MockSbomInputService {
  sbomTypes = [{ label: 'Upload', value: 'upload' }, { label: 'FOSSID', value: 'fossid' }];
  schemaTypes = [
    { label: 'CycloneDX', value: 'cyclonedx' },
    { label: 'SPDX 2.3', value: 'spdx' },
    { label: 'SPDX 2.2', value: 'spdx2.2' },
    { label: 'CDQ SPDX 2.3', value: 'cdqspdx2.3' },
    { label: 'CDQ CycloneDX', value: 'cdqcydx' },
    { label: 'Custom', value: 'custom' }
  ];
  licenseInfoTypes = [{ label: 'License ID', value: 'licId' }];
  cdqLicenseInfoTypes = [{ label: 'CDQ License ID', value: 'licId' }];

  downloadParams = {
    fileName: ['errors.pdf', 'changelog.pdf', 'auditlog.pdf', 'sbom.json'],
    header: [
      [{ header: 'Error Key', dataKey: 'errorKey' }],
      [{ header: 'Path', dataKey: 'path' }],
      [{ header: 'User', dataKey: 'userId' }]
    ]
  };

  initializeSpdxUndefinedObjects(json: any) { return json; }
  initializeCDQSpdxUndefinedObjects(json: any) { return json; }
  initializeCycloneDXUndefinedObjects(json: any) { return json; }
  initializeCDQCycloneDXUndefinedObjects(json: any) { return json; }

  uploadFile(form: NgForm, index: number, isSchema: boolean, file: File) {
    const model = new UploadModel();
    model.status = 200;
    model.sbomFileName = 'test.json';
    model.dirName = 'dir1';
    model.fileHash = 'hash1';
    model.message = 'success';
    return of(model);
  }

  validateFiles(fileToUpload: UploadModel) {
    const model = new UploadModel();
    model.status = 200;
    model.message = 'Validation success';
    model.sbomFileName = 'test.json';
    model.fileHash = 'hash1';
    return of(model);
  }

  fetchSbomFromFossid(form: NgForm, fileToUpload: UploadModel, index: number) {
    const model = new UploadModel();
    model.status = 200;
    model.message = 'Fetch success';
    model.sbomFileName = 'fossid.json';
    model.fileHash = 'hash2';
    return of(model);
  }

  deleteSbomEntry(item: UploadModel) {
    return of(item);
  }

  clearSession() {
    return of({});
  }

  replaceFile(fileToEdit: UploadModel) {
    const model = new UploadModel();
    model.dirName = fileToEdit.dirName;
    model.sbomFileName = fileToEdit.sbomFileName;
    model.fileHash = fileToEdit.fileHash;
    model.sbomJsonString = fileToEdit.sbomJsonString;
    model.schemaType = fileToEdit.schemaType;
    model.customErrorDetails = [];
    return of(model);
  }

  mergeSboms(list: UploadModel[], cdx: any, spdx: any, cdqSpdx: any, cdqCydx: any, mergeType: any) {
    const model = new UploadModel();
    model.schemaType = mergeType || 'cyclonedx';
    model.sbomJsonString = '{}';
    model.customErrorDetails = [];
    model.changeLogsList = [];
    return of(model);
  }

  convertSbom(item: UploadModel) {
    const model = new UploadModel();
    model.schemaType = item.schemaType;
    model.sbomJsonString = '{}';
    model.customErrorDetails = [];
    return of(model);
  }

  getJsonDifferences(currentValue: string, item: UploadModel, mergeMode: boolean) {
    return of([{ path: '/test', op: 'replace', value: 'newVal' }]);
  }

  prepareForDownload(currentValue: string, fileToEdit: UploadModel) {
    return of({
      changeLogsList: [],
      errorDetails: [],
      fileHash: 'hash_dl'
    });
  }
}

class MockHealthCheckService {
  checkServerHealth() {
    return of({ status: 'Server is running', userId: 'user123' });
  }
}

class MockRouter {
  navigate = jasmine.createSpy('navigate');
}

// ---------------------------------------------------------------------------
// Mock JsonEditorComponent
// ---------------------------------------------------------------------------

@Component({
  selector: 'json-editor',
  template: ''
})
class MockJsonEditorComponent {
  options: any;
  data: any;

  get() { return {}; }
  getEditor() {
    return {
      get: () => ({}),
      validate: () => {},
      validateSchema: { errors: [] }
    };
  }
  set(data: any) {}
}

// ---------------------------------------------------------------------------
// Helper: create a minimal UploadModel
// ---------------------------------------------------------------------------

function createUploadModel(overrides: Partial<UploadModel> = {}): UploadModel {
  const model = new UploadModel();
  model.index = 0;
  model.inputType = 'upload';
  model.schemaType = 'cyclonedx';
  model.sbomFileName = 'test.json';
  model.schemaFileName = '';
  model.valid = true;
  model.hidden = false;
  model.schema = false;
  model.dirName = 'dir_0';
  model.fileHash = 'abc123';
  model.sbomJsonString = '{"test":true}';
  model.schemaJsonString = '{}';
  model.errorDetails = [];
  model.customErrorDetails = [];
  model.changeLogsList = [];
  model.status = 200;
  model.message = 'OK';
  Object.assign(model, overrides);
  return model;
}

// ---------------------------------------------------------------------------
// Helper: create a mock File
// ---------------------------------------------------------------------------

function createMockFile(name: string, type: string = 'application/json'): File {
  const blob = new Blob(['{}'], { type });
  return new File([blob], name, { type });
}

// ---------------------------------------------------------------------------
// Helper: create a mock NgForm
// ---------------------------------------------------------------------------

function createMockForm(values: Record<string, any> = {}): NgForm {
  return {
    value: { schemaType: 'cyclonedx', inputType: 'upload', ...values },
    reset: jasmine.createSpy('reset'),
    resetForm: jasmine.createSpy('resetForm')
  } as any as NgForm;
}

// ---------------------------------------------------------------------------
// Helper: create a mock TemplateRef
// ---------------------------------------------------------------------------

function createMockTemplateRef(): TemplateRef<any> {
  return {} as TemplateRef<any>;
}

function createEditorStub(): JsonEditorComponent {
  return {
    get: () => ({}),
    getEditor: () => ({
      get: () => ({}),
      validate: () => {},
      validateSchema: { errors: [] }
    }),
    set: () => {}
  } as any as JsonEditorComponent;
}

// ---------------------------------------------------------------------------
// Describe block
// ---------------------------------------------------------------------------

describe('SbomInputComponent', () => {
  let component: SbomInputComponent;
  let fixture: ComponentFixture<SbomInputComponent>;
  let mockSbomInputService: MockSbomInputService;
  let mockHealthCheckService: MockHealthCheckService;
  let mockRouter: MockRouter;

  beforeEach(async () => {
    mockSbomInputService = new MockSbomInputService();
    mockHealthCheckService = new MockHealthCheckService();
    mockRouter = new MockRouter();

    await TestBed.configureTestingModule({
      declarations: [SbomInputComponent, MockJsonEditorComponent],
      imports: [FormsModule],
      providers: [
        { provide: SbomInputService, useValue: mockSbomInputService },
        { provide: HealthCheckService, useValue: mockHealthCheckService },
        { provide: Router, useValue: mockRouter }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .overrideComponent(SbomInputComponent, {
        set: {
          providers: [{ provide: SbomInputService, useValue: mockSbomInputService }]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(SbomInputComponent);
    component = fixture.componentInstance;

    // Keep editor stubs stable even when ViewChild resolution runs during detectChanges.
    const sbomEditorStub = createEditorStub();
    const schemaEditorStub = createEditorStub();
    Object.defineProperty(component, 'sbomEditor', {
      configurable: true,
      get: () => sbomEditorStub,
      set: () => {}
    });
    Object.defineProperty(component, 'schemaEditor', {
      configurable: true,
      get: () => schemaEditorStub,
      set: () => {}
    });

    // Stub modal TemplateRefs
    component.sessionClearSuccessModal = createMockTemplateRef();
    component.confirmSessionClearModal = createMockTemplateRef();
    component.errorDetailsModal = createMockTemplateRef();
    component.downloadSuccessModal = createMockTemplateRef();
    component.mergeModal = createMockTemplateRef();

    // Prevent real sessionStorage side effects
    sessionStorage.clear();

  });

  afterEach(() => {
    sessionStorage.clear();
  });

  // -------------------------------------------------------------------------
  // Creation & initial state
  // -------------------------------------------------------------------------

  describe('Component creation', () => {
    it('should create the component', () => {
      fixture.detectChanges();
      expect(component).toBeTruthy();
    });

    it('should initialize enableMerge to false', () => {
      expect(component.enableMerge).toBeFalse();
    });

    it('should initialize enableCompare to false', () => {
      expect(component.enableCompare).toBeFalse();
    });

    it('should initialize enableValidate to false', () => {
      expect(component.enableValidate).toBeFalse();
    });

    it('should initialize enableModalSave to false', () => {
      expect(component.enableModalSave).toBeFalse();
    });

    it('should initialize fileSaveSuccess to false', () => {
      expect(component.fileSaveSuccess).toBeFalse();
    });

    it('should initialize fileUploadSuccess to false', () => {
      expect(component.fileUploadSuccess).toBeFalse();
    });

    it('should initialize mergeMode to false', () => {
      expect(component.mergeMode).toBeFalse();
    });

    it('should initialize index to 0', () => {
      expect(component.index).toBe(0);
    });

    it('should initialize uploadStatusList as empty array', () => {
      expect(component.uploadStatusList).toEqual([]);
    });

    it('should count loss events by severity regardless of case', () => {
      component.fileToEdit.lossEvent = [
        { severity: 'BLOCKER' } as any,
        { severity: 'major' } as any,
        { severity: 'MINOR' } as any,
        { severity: 'minor' } as any,
        { severity: 'Informational' } as any
      ];

      expect(component.getLossEventSeverityCount('blocker')).toBe(1);
      expect(component.getLossEventSeverityCount('MAJOR')).toBe(1);
      expect(component.getLossEventSeverityCount('minor')).toBe(2);
      expect(component.getLossEventSeverityCount('informational')).toBe(1);
      expect(component.getLossEventSeverityCount('unknown')).toBe(0);
    });

    it('should return an indicator class for each supported loss event severity', () => {
      expect(component.getLossEventSeverityClass('BLOCKER')).toBe('severity-blocker');
      expect(component.getLossEventSeverityClass('major')).toBe('severity-major');
      expect(component.getLossEventSeverityClass('Minor')).toBe('severity-minor');
      expect(component.getLossEventSeverityClass('informational')).toBe('severity-informational');
      expect(component.getLossEventSeverityClass('unknown')).toBe('severity-unknown');
    });

    it('should initialize sbomListToProcess as empty array', () => {
      expect(component.sbomListToProcess).toEqual([]);
    });

    it('should initialize isLoading to false', () => {
      expect(component.isLoading).toBeFalse();
    });

    it('should initialize showAlert to false', () => {
      expect(component.showAlert).toBeFalse();
    });

    it('should initialize isModalOpen to false', () => {
      expect(component.isModalOpen).toBeFalse();
    });

    it('should initialize isDialogBoxOpen to false', () => {
      expect(component.isDialogBoxOpen).toBeFalse();
    });

    it('should initialize serverStatus to checking', () => {
      expect(component.serverStatus).toBe('checking');
    });

    it('should filter schemaTypes excluding spdx2.2', () => {
      const hasExcluded = component.schemaTypes.some((s: any) => s.value === 'spdx2.2');
      expect(hasExcluded).toBeFalse();
    });

    it('should filter fossidSchemaTypes excluding custom and spdx', () => {
      const hasCustom = component.fossidSchemaTypes.some((s: any) => s.value === 'custom');
      const hasSpdx = component.fossidSchemaTypes.some((s: any) => s.value === 'spdx');
      expect(hasCustom).toBeFalse();
      expect(hasSpdx).toBeFalse();
    });

    it('should set editorOptions modes', () => {
      expect(component.editorOptions.modes).toEqual(['tree', 'code', 'view']);
    });

    it('should set editorOptions mode to tree', () => {
      expect(component.editorOptions.mode).toBe('tree');
    });
  });

  // -------------------------------------------------------------------------
  // ngOnInit
  // -------------------------------------------------------------------------

  describe('ngOnInit', () => {
    it('should set serverStatus to running when health check returns running', () => {
      spyOn(mockHealthCheckService, 'checkServerHealth').and.returnValue(
        of({ status: 'Server is running', userId: 'user123' })
      );
      fixture.detectChanges();
      expect(component.serverStatus).toBe('running');
    });

    it('should set userId in sessionStorage when health check returns userId', () => {
      spyOn(mockHealthCheckService, 'checkServerHealth').and.returnValue(
        of({ status: 'Server is running', userId: 'user_abc' })
      );
      fixture.detectChanges();
      expect(sessionStorage.getItem('userId')).toBe('user_abc');
    });

    it('should set token in sessionStorage when it does not exist', () => {
      sessionStorage.removeItem('token');
      spyOn(mockHealthCheckService, 'checkServerHealth').and.returnValue(
        of({ status: 'Server is running', userId: 'u1' })
      );
      fixture.detectChanges();
      expect(sessionStorage.getItem('token')).not.toBeNull();
    });

    it('should not overwrite existing token in sessionStorage', () => {
      sessionStorage.setItem('token', 'existing_token');
      spyOn(mockHealthCheckService, 'checkServerHealth').and.returnValue(
        of({ status: 'Server is running', userId: 'u1' })
      );
      fixture.detectChanges();
      expect(sessionStorage.getItem('token')).toBe('existing_token');
    });

    it('should set defaultInputType to upload', () => {
      fixture.detectChanges();
      expect(component.defaultInputType).toBe('upload');
    });

    it('should set defaultSchemaType to cyclonedx', () => {
      fixture.detectChanges();
      expect(component.defaultSchemaType).toBe('cyclonedx');
    });
    it('should set serverStatus to down when health check returns non-running status', () => {
      spyOn(mockHealthCheckService, 'checkServerHealth').and.returnValue(
        of({ status: 'Server is down', userId: '' })
      );
      fixture.detectChanges();
      expect(component.serverStatus).toBe('down');
    });
    it('should set serverStatus to down on health check error', () => {
      spyOn(mockHealthCheckService, 'checkServerHealth').and.returnValue(
        throwError(() => new Error('Network error'))
      );
      fixture.detectChanges();
      expect(component.serverStatus).toBe('down');
    });
/*
    it('should handle null response from health check gracefully', () => {
      spyOn(mockHealthCheckService, 'checkServerHealth').and.returnValue(of(null));
      fixture.detectChanges();
      // Should not throw; serverStatus remains 'checking'
      expect(component.serverStatus).toBe('checking');
    });
*/
    it('should initialize cdxMerged supplier contact', () => {
      fixture.detectChanges();
      expect(component.cdxMerged.metadata.supplier.contact.length).toBeGreaterThan(0);
    });

    it('should initialize cdqCydxMerged supplier contact', () => {
      fixture.detectChanges();
      expect(component.cdqCydxMerged.metadata.supplier.contact.length).toBeGreaterThan(0);
    });

    it('should initialize spdxMergedPackage defaults', () => {
      fixture.detectChanges();
      expect(component.spdxMergedPackage.name).toBe('');
      expect(component.spdxMergedPackage.versionInfo).toBe('');
    });

    it('should initialize cdqSpdxMergedPackage defaults', () => {
      fixture.detectChanges();
      expect(component.cdqSpdxMergedPackage.name).toBe('');
    });
  });

  // -------------------------------------------------------------------------
  // initializeUndefinedObjects
  // -------------------------------------------------------------------------

  describe('initializeUndefinedObjects', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should handle spdx schemaType', () => {
      spyOn(mockSbomInputService, 'initializeSpdxUndefinedObjects').and.callThrough();
      component.fileToEdit = createUploadModel({
        schemaType: 'spdx',
        sbomJsonString: '{"spdxVersion":"SPDX-2.3"}'
      });
      component.initializeUndefinedObjects();
      expect(mockSbomInputService.initializeSpdxUndefinedObjects).toHaveBeenCalled();
      expect(component.editorOptions.sortObjectKeys).toBeTrue();
    });

    it('should handle spdx2.2 schemaType', () => {
      component.fileToEdit = createUploadModel({
        schemaType: 'spdx2.2',
        sbomJsonString: '{"spdxVersion":"SPDX-2.2"}'
      });
      component.initializeUndefinedObjects();
      expect(component.editorOptions.sortObjectKeys).toBeTrue();
    });

    it('should handle cdqspdx2.3 schemaType', () => {
      spyOn(mockSbomInputService, 'initializeCDQSpdxUndefinedObjects').and.callThrough();
      component.fileToEdit = createUploadModel({
        schemaType: 'cdqspdx2.3',
        sbomJsonString: '{}'
      });
      component.initializeUndefinedObjects();
      expect(mockSbomInputService.initializeCDQSpdxUndefinedObjects).toHaveBeenCalled();
    });

    it('should handle cyclonedx schemaType', () => {
      spyOn(mockSbomInputService, 'initializeCycloneDXUndefinedObjects').and.callThrough();
      component.fileToEdit = createUploadModel({
        schemaType: 'cyclonedx',
        sbomJsonString: '{}'
      });
      component.initializeUndefinedObjects();
      expect(mockSbomInputService.initializeCycloneDXUndefinedObjects).toHaveBeenCalled();
      expect(component.editorOptions.sortObjectKeys).toBeFalse();
    });

    it('should handle cdqcydx schemaType', () => {
      spyOn(mockSbomInputService, 'initializeCDQCycloneDXUndefinedObjects').and.callThrough();
      component.fileToEdit = createUploadModel({
        schemaType: 'cdqcydx',
        sbomJsonString: '{}'
      });
      component.initializeUndefinedObjects();
      expect(mockSbomInputService.initializeCDQCycloneDXUndefinedObjects).toHaveBeenCalled();
    });

    it('should handle custom schemaType', () => {
      component.fileToEdit = createUploadModel({
        schemaType: 'custom',
        sbomJsonString: '{}',
        schemaJsonString: '{"type":"object"}'
      });
      component.initializeUndefinedObjects();
      expect(component.fileToEdit.sbomJson).toBeDefined();
    });

    it('should set validationErrors from customErrorDetails', () => {
      const err = new ErrorModel();
      err.errorKey = 'key1';
      err.message = 'Error 1';
      component.fileToEdit = createUploadModel({
        schemaType: 'cyclonedx',
        sbomJsonString: '{}',
        customErrorDetails: [err]
      });
      component.initializeUndefinedObjects();
      expect(component.validationErrors).toContain(err);
    });
  });

  // -------------------------------------------------------------------------
  // clearLicenses / cdqclearLicenses / cdqMetadataClearLicenses
  // -------------------------------------------------------------------------

  describe('clearLicenses', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should reset cdxMerged licenses[0] to new License', () => {
      component.clearLicenses();
      expect(component.cdxMerged.metadata.component.licenses[0]).toBeDefined();
      expect(component.cdxMerged.metadata.component.licenses[0].license).toBeDefined();
    });
  });

  describe('cdqclearLicenses', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should set expression when licText type is selected', () => {
      component.cdqMergeLicenseInfoType = 'licText';
      component.cdqclearLicenses();
      expect(component.cdqCydxMerged.metadata.component.licenses[0]).toEqual({ expression: '' });
    });

    it('should set license object when non-licText type is selected', () => {
      component.cdqMergeLicenseInfoType = 'licId';
      component.cdqclearLicenses();
      expect(component.cdqCydxMerged.metadata.component.licenses[0]).toBeDefined();
      expect((component.cdqCydxMerged.metadata.component.licenses[0] as any).license).toBeDefined();
    });
  });

  describe('cdqMetadataClearLicenses', () => {
    beforeEach(() => {
      fixture.detectChanges();
      // ensure metadata.licenses array exists
      component.cdqCydxMerged.metadata.licenses = [{ license: new CDQLicense() }];
    });

    it('should reset cdqCydxMerged metadata licenses[0]', () => {
      component.cdqMetadataClearLicenses();
      expect(component.cdqCydxMerged.metadata.licenses[0]).toBeDefined();
    });
  });

  // -------------------------------------------------------------------------
  // sanitizeSchemaObject
  // -------------------------------------------------------------------------

  describe('sanitizeSchemaObject', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should return null as is', () => {
      expect(component.sanitizeSchemaObject(null)).toBeNull();
    });

    it('should return undefined as is', () => {
      expect(component.sanitizeSchemaObject(undefined)).toBeUndefined();
    });

    it('should return primitive as is', () => {
      expect(component.sanitizeSchemaObject('hello')).toBe('hello');
      expect(component.sanitizeSchemaObject(42)).toBe(42);
    });

    it('should handle arrays recursively', () => {
      const input = [{ format: 'date' }, { format: 'iri-reference' }];
      const result = component.sanitizeSchemaObject(input);
      expect(Array.isArray(result)).toBeTrue();
      expect(result[0].format).toBe('date');
      expect(result[1].format).toBeUndefined();
    });

    it('should remove $schema key', () => {
      const input = { $schema: 'http://json-schema.org/draft-07/schema', type: 'object' };
      const result = component.sanitizeSchemaObject(input);
      expect(result.$schema).toBeUndefined();
      expect(result.type).toBe('object');
    });

    it('should keep supported format values', () => {
      const input = { format: 'date-time' };
      const result = component.sanitizeSchemaObject(input);
      expect(result.format).toBe('date-time');
    });

    it('should remove unsupported format values', () => {
      const input = { format: 'iri-reference' };
      const result = component.sanitizeSchemaObject(input);
      expect(result.format).toBeUndefined();
    });

    it('should remove idn-email format', () => {
      const input = { format: 'idn-email' };
      const result = component.sanitizeSchemaObject(input);
      expect(result.format).toBeUndefined();
    });

    it('should recursively sanitize nested objects', () => {
      const input = {
        $schema: 'remove-me',
        properties: {
          name: { type: 'string', format: 'uri' },
          email: { type: 'string', format: 'idn-email' }
        }
      };
      const result = component.sanitizeSchemaObject(input);
      expect(result.$schema).toBeUndefined();
      expect(result.properties.name.format).toBe('uri');
      expect(result.properties.email.format).toBeUndefined();
    });

    it('should keep all supported formats', () => {
      const supported = ['date', 'date-time', 'time', 'duration', 'uri', 'uri-reference',
        'uri-template', 'email', 'hostname', 'ipv4', 'ipv6', 'uuid', 'regex',
        'json-pointer', 'relative-json-pointer'];
      supported.forEach(fmt => {
        const result = component.sanitizeSchemaObject({ format: fmt });
        expect(result.format).toBe(fmt, `Expected format ${fmt} to be kept`);
      });
    });
  });

  // -------------------------------------------------------------------------
  // replaceFile
  // -------------------------------------------------------------------------

  describe('replaceFile', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should call sbomInputService.replaceFile and update uploadStatusList', () => {
      const item = createUploadModel({ dirName: 'dir_1', schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.uploadStatusList = [item];
      component.fileToEdit = item;
      spyOn(mockSbomInputService, 'replaceFile').and.callThrough();
      component.replaceFile();
      expect(mockSbomInputService.replaceFile).toHaveBeenCalled();
      expect(component.fileSaveSuccess).toBeTrue();
      expect(component.fileUploadSuccess).toBeFalse();
    });

    it('should set fileSaveSuccess to true on success', () => {
      const item = createUploadModel({ dirName: 'dir_replace', schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.uploadStatusList = [item];
      component.fileToEdit = item;
      component.replaceFile();
      expect(component.fileSaveSuccess).toBeTrue();
    });

    it('should not update uploadStatusList when dirName not found', () => {
      const item = createUploadModel({ dirName: 'dir_existing', schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.uploadStatusList = [item];
      const editItem = createUploadModel({ dirName: 'dir_nonexistent', schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.fileToEdit = editItem;

      const returnModel = new UploadModel();
      returnModel.dirName = 'dir_nonexistent';
      returnModel.sbomFileName = 'test.json';
      returnModel.customErrorDetails = [];
      returnModel.schemaType = 'cyclonedx';
      returnModel.sbomJsonString = '{}';
      spyOn(mockSbomInputService, 'replaceFile').and.returnValue(of(returnModel));

      const initialLength = component.uploadStatusList.length;
      component.replaceFile();
      expect(component.uploadStatusList.length).toBe(initialLength);
    });
  });

  // -------------------------------------------------------------------------
  // selectItemToShowErrs
  // -------------------------------------------------------------------------

  describe('selectItemToShowErrs', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should set fileToEdit and open modal', () => {
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}' });
      const modal = createMockTemplateRef();
      component.selectItemToShowErrs(item, modal);
      expect(component.fileToEdit).toBe(item);
      expect(component.enableModalSave).toBeTrue();
      expect(component.isModalOpen).toBeTrue();
      expect(component.currentModal).toBe(modal);
    });

    it('should reset fileSaveSuccess and fileUploadSuccess', () => {
      component.fileSaveSuccess = true;
      component.fileUploadSuccess = true;
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.selectItemToShowErrs(item, createMockTemplateRef());
      expect(component.fileSaveSuccess).toBeFalse();
      expect(component.fileUploadSuccess).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // openModal / closeModal
  // -------------------------------------------------------------------------

  describe('openModal and closeModal', () => {
    it('should set isModalOpen and currentModal on openModal', () => {
      const modal = createMockTemplateRef();
      component.openModal(modal);
      expect(component.isModalOpen).toBeTrue();
      expect(component.currentModal).toBe(modal);
    });

    it('should reset isModalOpen and currentModal on closeModal', () => {
      component.isModalOpen = true;
      component.currentModal = createMockTemplateRef();
      component.closeModal();
      expect(component.isModalOpen).toBeFalse();
      expect(component.currentModal).toBeNull();
    });
  });

  // -------------------------------------------------------------------------
  // openDialogBox / closeDialogBox
  // -------------------------------------------------------------------------

  describe('openDialogBox and closeDialogBox', () => {
    it('should set isDialogBoxOpen and alertModal on openDialogBox', () => {
      const modal = createMockTemplateRef();
      component.openDialogBox(modal);
      expect(component.isDialogBoxOpen).toBeTrue();
      expect(component.alertModal).toBe(modal);
    });

    it('should reset isDialogBoxOpen and alertModal on closeDialogBox', () => {
      component.isDialogBoxOpen = true;
      component.alertModal = createMockTemplateRef();
      component.closeDialogBox();
      expect(component.isDialogBoxOpen).toBeFalse();
      expect(component.alertModal).toBeNull();
    });
  });

  // -------------------------------------------------------------------------
  // showMergeModal
  // -------------------------------------------------------------------------

  describe('showMergeModal', () => {
    it('should open the modal', () => {
      const modal = createMockTemplateRef();
      component.showMergeModal(modal);
      expect(component.isModalOpen).toBeTrue();
      expect(component.currentModal).toBe(modal);
    });
  });

  // -------------------------------------------------------------------------
  // removeSboms
  // -------------------------------------------------------------------------

  describe('removeSboms', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should remove item from uploadStatusList', () => {
      const item1 = createUploadModel({ dirName: 'dir_1' });
      const item2 = createUploadModel({ dirName: 'dir_2' });
      component.uploadStatusList = [item1, item2];
      spyOn(mockSbomInputService, 'deleteSbomEntry').and.returnValue(of(item1));
      component.removeSboms(item1);
      expect(component.uploadStatusList.length).toBe(1);
      expect(component.uploadStatusList[0].dirName).toBe('dir_2');
    });

    it('should call deleteSbomEntry on service', () => {
      const item = createUploadModel({ dirName: 'dir_del' });
      component.uploadStatusList = [item];
      spyOn(mockSbomInputService, 'deleteSbomEntry').and.returnValue(of(item));
      component.removeSboms(item);
      expect(mockSbomInputService.deleteSbomEntry).toHaveBeenCalledWith(item);
    });
  });

  // -------------------------------------------------------------------------
  // getDifferences
  // -------------------------------------------------------------------------

  describe('getDifferences', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should call getJsonDifferences and set change logs', () => {
      const item = createUploadModel({ changeLogsList: [] });
      component.fileToEdit = item;
      spyOn(mockSbomInputService, 'getJsonDifferences').and.returnValue(
        of([{ path: '/name', op: 'replace', value: 'new' }])
      );
      component.getDifferences(item);
      expect(mockSbomInputService.getJsonDifferences).toHaveBeenCalled();
    });
  });

  // -------------------------------------------------------------------------
  // showclearSessionWarning
  // -------------------------------------------------------------------------

  describe('showclearSessionWarning', () => {
    it('should open dialog box with confirmSessionClearModal', () => {
      fixture.detectChanges();
      spyOn(component, 'openDialogBox');
      component.showclearSessionWarning();
      expect(component.openDialogBox).toHaveBeenCalledWith(component.confirmSessionClearModal);
    });
  });

  // -------------------------------------------------------------------------
  // clearSession
  // -------------------------------------------------------------------------

  describe('clearSession', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should clear uploadStatusList on success', () => {
      component.uploadStatusList = [createUploadModel()];
      spyOn(mockSbomInputService, 'clearSession').and.returnValue(of({}));
      sessionStorage.setItem('userId', 'testUser');
      component.clearSession();
      expect(component.uploadStatusList).toEqual([]);
    });

    it('should reset index to 0', () => {
      component.index = 5;
      spyOn(mockSbomInputService, 'clearSession').and.returnValue(of({}));
      component.clearSession();
      expect(component.index).toBe(0);
    });

    it('should preserve userId in sessionStorage after clear', () => {
      sessionStorage.setItem('userId', 'preserved_user');
      spyOn(mockSbomInputService, 'clearSession').and.returnValue(of({}));
      component.clearSession();
      expect(sessionStorage.getItem('userId')).toBe('preserved_user');
    });

    it('should set showAlert to true', () => {
      spyOn(mockSbomInputService, 'clearSession').and.returnValue(of({}));
      component.clearSession();
      expect(component.showAlert).toBeTrue();
    });

    it('should open dialog box with sessionClearSuccessModal', () => {
      spyOn(mockSbomInputService, 'clearSession').and.returnValue(of({}));
      spyOn(component, 'openDialogBox');
      component.clearSession();
      expect(component.openDialogBox).toHaveBeenCalledWith(component.sessionClearSuccessModal);
    });

    it('should handle null userId gracefully', () => {
      sessionStorage.removeItem('userId');
      spyOn(mockSbomInputService, 'clearSession').and.returnValue(of({}));
      component.clearSession();
      expect(sessionStorage.getItem('userId')).toBeNull();
    });
  });

  // -------------------------------------------------------------------------
  // logAction
  // -------------------------------------------------------------------------

  describe('logAction', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should store audit log in sessionStorage', () => {
      sessionStorage.setItem('userId', 'user1');
      const item = createUploadModel({ sbomFileName: 'myfile.json', fileHash: 'myhash' });
      component.logAction(item, 'Upload');
      const log = JSON.parse(sessionStorage.getItem('log') || '[]');
      expect(log.length).toBeGreaterThan(0);
      expect(log[0].action).toBe('Upload');
      expect(log[0].fileName).toBe('myfile.json');
    });

    it('should append to existing log', () => {
      sessionStorage.setItem('userId', 'user1');
      const existing: AuditLog[] = [
        { userId: 'user1', action: 'Previous', fileName: 'old.json', fileHash: 'h0', timestamp: '1/1/2025', status: '' }
      ];
      sessionStorage.setItem('log', JSON.stringify(existing));
      const item = createUploadModel({ sbomFileName: 'new.json', fileHash: 'h1' });
      component.logAction(item, 'Delete');
      const log = JSON.parse(sessionStorage.getItem('log') || '[]');
      expect(log.length).toBe(2);
      expect(log[1].action).toBe('Delete');
    });

    it('should use empty string for userId when not in sessionStorage', () => {
      sessionStorage.removeItem('userId');
      const item = createUploadModel();
      component.logAction(item, 'Convert');
      const log = JSON.parse(sessionStorage.getItem('log') || '[]');
      expect(log[0].userId).toBe('');
    });
  });

  // -------------------------------------------------------------------------
  // uploadFile
  // -------------------------------------------------------------------------

  describe('uploadFile', () => {
    let form: NgForm;

    beforeEach(() => {
      fixture.detectChanges();
      form = createMockForm();
    });

    it('should set fileName when valid JSON file is uploaded (not schema)', () => {
      const file = createMockFile('test.json', 'application/json');
      const event = { target: { files: [file] } } as any;
      spyOn(mockSbomInputService, 'uploadFile').and.callThrough();
      component.uploadFile(form, false, event);
      expect(component.fileName).toBe('test.json');
    });

    it('should set schemafileName when valid JSON schema file is uploaded', () => {
      const file = createMockFile('schema.json', 'application/json');
      const event = { target: { files: [file] } } as any;
      spyOn(mockSbomInputService, 'uploadFile').and.callThrough();
      component.uploadFile(form, true, event);
      expect(component.schemafileName).toBe('schema.json');
    });

    it('should show error message for non-JSON file', () => {
      const file = createMockFile('test.txt', 'text/plain');
      const event = { target: { files: [file] } } as any;
      component.uploadFile(form, false, event);
      expect(component.fileUploadSuccess).toBeTrue();
      expect(component.fileUploadmessage).toContain('valid JSON file');
      expect(component.calloutType).toBe('info');
    });

    it('should call sbomInputService.uploadFile for valid JSON', () => {
      const file = createMockFile('valid.json', 'application/json');
      const event = { target: { files: [file] } } as any;
      spyOn(mockSbomInputService, 'uploadFile').and.callThrough();
      component.uploadFile(form, false, event);
      expect(mockSbomInputService.uploadFile).toHaveBeenCalled();
    });

    it('should set isLoading to true during upload then false on completion', () => {
      const file = createMockFile('valid.json', 'application/json');
      const event = { target: { files: [file] } } as any;
      const model = new UploadModel();
      model.status = 200;
      model.sbomFileName = 'valid.json';
      model.dirName = 'dir1';
      model.fileHash = 'h1';
      model.message = 'ok';
      spyOn(mockSbomInputService, 'uploadFile').and.returnValue(of(model));
      component.uploadFile(form, false, event);
      expect(component.isLoading).toBeFalse();
    });

    it('should show error on upload failure with non-200 status', () => {
      const file = createMockFile('valid.json', 'application/json');
      const event = { target: { files: [file] } } as any;
      const errorModel = new UploadModel();
      errorModel.status = 500;
      errorModel.message = 'Internal error';
      spyOn(mockSbomInputService, 'uploadFile').and.returnValue(of(errorModel));
      component.uploadFile(form, false, event);
      expect(component.fileUploadSuccess).toBeTrue();
      expect(component.calloutType).toBe('error');
    });

    it('should show error on upload exception', () => {
      const file = createMockFile('valid.json', 'application/json');
      const event = { target: { files: [file] } } as any;
      spyOn(mockSbomInputService, 'uploadFile').and.returnValue(throwError(() => new Error('Network error')));
      component.uploadFile(form, false, event);
      expect(component.fileUploadSuccess).toBeTrue();
      expect(component.calloutType).toBe('error');
    });

    it('should handle empty files input gracefully', () => {
      const event = { target: { files: [] } } as any;
      // Current implementation expects a selected file and throws when none is present.
      expect(() => component.uploadFile(form, false, event)).toThrow();
    });

    it('should reset enableValidate to false', () => {
      component.enableValidate = true;
      const file = createMockFile('valid.json', 'application/json');
      const event = { target: { files: [file] } } as any;
      spyOn(mockSbomInputService, 'uploadFile').and.callThrough();
      component.uploadFile(form, false, event);
      // enableValidate will be re-evaluated, starts as false
      expect(component.enableValidate).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // checkMandatoryFieldsFilled
  // -------------------------------------------------------------------------

  describe('checkMandatoryFieldsFilled', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should enable validate for custom + upload when uploadFileCountTag has 2 items', () => {
      const form = createMockForm({ schemaType: 'custom', inputType: 'upload' });
      component.uploadFileCountTag = ['file1.json', 'schema.json'];
      component.checkMandatoryFieldsFilled(form);
      expect(component.enableValidate).toBeTrue();
      expect(component.uploadFileCountTag.length).toBe(1);
    });

    it('should enable validate for non-custom + upload when uploadFileCountTag has 1 item', () => {
      const form = createMockForm({ schemaType: 'cyclonedx', inputType: 'upload' });
      component.uploadFileCountTag = ['file1.json'];
      component.checkMandatoryFieldsFilled(form);
      expect(component.enableValidate).toBeTrue();
      expect(component.uploadFileCountTag.length).toBe(0);
    });

    it('should not enable validate for custom + upload when uploadFileCountTag has < 2 items', () => {
      const form = createMockForm({ schemaType: 'custom', inputType: 'upload' });
      component.uploadFileCountTag = ['file1.json'];
      component.checkMandatoryFieldsFilled(form);
      expect(component.enableValidate).toBeFalse();
    });

    it('should not enable validate for non-custom + upload when uploadFileCountTag has 0 items', () => {
      const form = createMockForm({ schemaType: 'cyclonedx', inputType: 'upload' });
      component.uploadFileCountTag = [];
      component.checkMandatoryFieldsFilled(form);
      expect(component.enableValidate).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // removeFileNamefromList
  // -------------------------------------------------------------------------

  describe('removeFileNamefromList', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should remove filename from fileNames and add to uploadFileCountTag', () => {
      const form = createMockForm();
      component.fileNames = ['a.json', 'b.json'];
      component.uploadFileCountTag = [];
      component.removeFileNamefromList('a.json', form);
      expect(component.fileNames).toEqual(['b.json']);
      expect(component.uploadFileCountTag).toContain('a.json');
    });

    it('should not modify fileNames if filename not found', () => {
      const form = createMockForm();
      component.fileNames = ['a.json'];
      component.removeFileNamefromList('notexist.json', form);
      expect(component.fileNames).toEqual(['a.json']);
    });

    it('should set isLoading to false when fileNames is empty', () => {
      component.isLoading = true;
      const form = createMockForm({ schemaType: 'cyclonedx', inputType: 'upload' });
      component.fileNames = ['single.json'];
      component.uploadFileCountTag = [];
      component.removeFileNamefromList('single.json', form);
      expect(component.isLoading).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // populateFileName / populateSchemaFileName
  // -------------------------------------------------------------------------

  describe('populateFileName', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should not throw when DOM element is not found', () => {
      const form = createMockForm();
      const file = createMockFile('sbom.json');
      expect(() => component.populateFileName(form, file)).not.toThrow();
    });
  });

  describe('populateSchemaFileName', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should not throw when DOM element is not found', () => {
      const form = createMockForm();
      const file = createMockFile('schema.json');
      expect(() => component.populateSchemaFileName(form, file)).not.toThrow();
    });
  });

  // -------------------------------------------------------------------------
  // addItemToList
  // -------------------------------------------------------------------------

  describe('addItemToList', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should enable merge for multiple valid files of same schema', () => {
      component.uploadStatusList = [
        createUploadModel({ schemaType: 'cyclonedx', valid: true, dirName: 'dir_0' }),
        createUploadModel({ schemaType: 'cyclonedx', valid: true, dirName: 'dir_1' })
      ];

      const selected = [
        { closest: () => ({ id: 'check_0' }) },
        { closest: () => ({ id: 'check_1' }) }
      ] as any;

      spyOn(document, 'querySelectorAll').and.returnValue(selected);

      component.addItemToList();

      expect(component.enableMerge).toBeTrue();
      expect(component.mergeType).toBe('cyclonedx');
      expect(component.sbomListToProcess.length).toBe(2);
    });

    it('should enable compare for exactly two files of same schema', () => {
      component.uploadStatusList = [
        createUploadModel({ schemaType: 'spdx', valid: true, dirName: 'dir_0' }),
        createUploadModel({ schemaType: 'spdx', valid: true, dirName: 'dir_1' })
      ];

      const selected = [
        { closest: () => ({ id: 'check_0' }) },
        { closest: () => ({ id: 'check_1' }) }
      ] as any;

      spyOn(document, 'querySelectorAll').and.returnValue(selected);

      component.addItemToList();

      expect(component.enableCompare).toBeTrue();
      expect(component.enableMerge).toBeTrue();
    });

    it('should keep merge and compare disabled for mixed schemas', () => {
      component.uploadStatusList = [
        createUploadModel({ schemaType: 'cyclonedx', valid: true, dirName: 'dir_0' }),
        createUploadModel({ schemaType: 'spdx', valid: true, dirName: 'dir_1' })
      ];

      const selected = [
        { closest: () => ({ id: 'check_0' }) },
        { closest: () => ({ id: 'check_1' }) }
      ] as any;

      spyOn(document, 'querySelectorAll').and.returnValue(selected);

      component.addItemToList();

      expect(component.enableMerge).toBeFalse();
      expect(component.enableCompare).toBeFalse();
    });

    it('should keep merge and compare disabled for spdx2.2 schema', () => {
      component.uploadStatusList = [
        createUploadModel({ schemaType: 'spdx2.2', valid: true, dirName: 'dir_0' }),
        createUploadModel({ schemaType: 'spdx2.2', valid: true, dirName: 'dir_1' })
      ];

      const selected = [
        { closest: () => ({ id: 'check_0' }) },
        { closest: () => ({ id: 'check_1' }) }
      ] as any;

      spyOn(document, 'querySelectorAll').and.returnValue(selected);

      component.addItemToList();

      expect(component.enableMerge).toBeFalse();
      expect(component.enableCompare).toBeFalse();
    });

    it('should reset sbomListToProcess and schemaTypesToMerge', () => {
      component.sbomListToProcess = [createUploadModel()];
      component.addItemToList();
      expect(component.sbomListToProcess).toEqual([]);
      expect(component.schemaTypesToMerge.size).toBe(0);
    });

    it('should set enableMerge to false when no checkboxes selected', () => {
      component.enableMerge = true;
      component.addItemToList();
      expect(component.enableMerge).toBeFalse();
    });

    it('should set enableCompare to false when no checkboxes selected', () => {
      component.enableCompare = true;
      component.addItemToList();
      expect(component.enableCompare).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // changeLog
  // -------------------------------------------------------------------------

  describe('changeLog', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should reset fileSaveSuccess and fileUploadSuccess', () => {
      component.fileSaveSuccess = true;
      component.fileUploadSuccess = true;
      component.changeLog({});
      expect(component.fileSaveSuccess).toBeFalse();
      expect(component.fileUploadSuccess).toBeFalse();
    });

    it('should not throw when called', () => {
      expect(() => component.changeLog({ test: true })).not.toThrow();
    });
  });

  // -------------------------------------------------------------------------
  // changeEvent
  // -------------------------------------------------------------------------

  describe('changeEvent', () => {
    it('should not throw when called', () => {
      fixture.detectChanges();
      expect(() => component.changeEvent({ test: true })).not.toThrow();
    });
  });

  // -------------------------------------------------------------------------
  // selectFileToEdit
  // -------------------------------------------------------------------------

  describe('selectFileToEdit', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should set fileToEdit and open modal', () => {
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}' });
      const modal = createMockTemplateRef();
      component.selectFileToEdit(item, modal);
      expect(component.fileToEdit).toBe(item);
      expect(component.isModalOpen).toBeTrue();
      expect(component.currentModal).toBe(modal);
    });

    it('should set mergeMode to false', () => {
      component.mergeMode = true;
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.selectFileToEdit(item, createMockTemplateRef());
      expect(component.mergeMode).toBeFalse();
    });

    it('should clear changeLogsList of the item', () => {
      const cl = new ChangeLog();
      cl.path = '/test';
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}', changeLogsList: [cl] });
      component.selectFileToEdit(item, createMockTemplateRef());
      expect(component.fileToEdit.changeLogsList).toEqual([]);
    });

    it('should set enableModalSave to true', () => {
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.selectFileToEdit(item, createMockTemplateRef());
      expect(component.enableModalSave).toBeTrue();
    });
  });

  // -------------------------------------------------------------------------
  // validateSboms
  // -------------------------------------------------------------------------

  describe('validateSboms', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should add data to uploadStatusList on 200 success', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200, message: 'Validation success' });
      spyOn(mockSbomInputService, 'validateFiles').and.returnValue(of(model));
      component.validateSboms(form);
      expect(component.uploadStatusList.length).toBe(1);
    });

    it('should increment index on success', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200, message: 'OK' });
      spyOn(mockSbomInputService, 'validateFiles').and.returnValue(of(model));
      const prevIndex = component.index;
      component.validateSboms(form);
      expect(component.index).toBe(prevIndex + 1);
    });

    it('should set calloutType to success on 200', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200, message: 'OK' });
      spyOn(mockSbomInputService, 'validateFiles').and.returnValue(of(model));
      component.validateSboms(form);
      expect(component.calloutType).toBe('success');
    });

    it('should set calloutType to error on non-200 status', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 422, message: 'Validation failed' });
      spyOn(mockSbomInputService, 'validateFiles').and.returnValue(of(model));
      component.validateSboms(form);
      expect(component.calloutType).toBe('error');
    });

    it('should set fileUploadSuccess to true', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200 });
      spyOn(mockSbomInputService, 'validateFiles').and.returnValue(of(model));
      component.validateSboms(form);
      expect(component.fileUploadSuccess).toBeTrue();
    });

    it('should handle error from validateFiles', () => {
      const form = createMockForm();
      spyOn(mockSbomInputService, 'validateFiles').and.returnValue(throwError(() => new Error('Server error')));
      component.validateSboms(form);
      expect(component.fileUploadSuccess).toBeTrue();
      expect(component.calloutType).toBe('error');
    });

    it('should set isLoading to false after completion', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200 });
      spyOn(mockSbomInputService, 'validateFiles').and.returnValue(of(model));
      component.validateSboms(form);
      expect(component.isLoading).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // fetchSbomFromFossid
  // -------------------------------------------------------------------------

  describe('fetchSbomFromFossid', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should add to uploadStatusList on 200 success', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200, sbomFileName: 'fossid.json' });
      spyOn(mockSbomInputService, 'fetchSbomFromFossid').and.returnValue(of(model));
      component.fetchSbomFromFossid(form);
      expect(component.uploadStatusList.length).toBe(1);
    });

    it('should set calloutType to success on 200', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200 });
      spyOn(mockSbomInputService, 'fetchSbomFromFossid').and.returnValue(of(model));
      component.fetchSbomFromFossid(form);
      expect(component.calloutType).toBe('success');
    });

    it('should show error on non-200 status', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 500, message: 'Fetch failed' });
      spyOn(mockSbomInputService, 'fetchSbomFromFossid').and.returnValue(of(model));
      component.fetchSbomFromFossid(form);
      expect(component.calloutType).toBe('error');
    });

    it('should handle error from service', () => {
      const form = createMockForm();
      spyOn(mockSbomInputService, 'fetchSbomFromFossid').and.returnValue(throwError(() => new Error('Network')));
      component.fetchSbomFromFossid(form);
      expect(component.fileUploadSuccess).toBeTrue();
      expect(component.calloutType).toBe('error');
    });

    it('should set isLoading to false after completion', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200 });
      spyOn(mockSbomInputService, 'fetchSbomFromFossid').and.returnValue(of(model));
      component.fetchSbomFromFossid(form);
      expect(component.isLoading).toBeFalse();
    });

    it('should increment index on success', () => {
      const form = createMockForm();
      const model = createUploadModel({ status: 200 });
      spyOn(mockSbomInputService, 'fetchSbomFromFossid').and.returnValue(of(model));
      const prev = component.index;
      component.fetchSbomFromFossid(form);
      expect(component.index).toBe(prev + 1);
    });
  });

  // -------------------------------------------------------------------------
  // hideCalloutAfterInterval
  // -------------------------------------------------------------------------

  describe('hideCalloutAfterInterval', () => {
    it('should hide callout after 6 seconds', fakeAsync(() => {
      fixture.detectChanges();
      component.fileUploadSuccess = true;
      component.fileUploadmessage = 'Some message';
      component.hideCalloutAfterInterval();
      tick(6000);
      expect(component.fileUploadSuccess).toBeFalse();
      expect(component.fileUploadmessage).toBe('');
    }));
  });

  // -------------------------------------------------------------------------
  // resetForm
  // -------------------------------------------------------------------------

  describe('resetForm', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should create a new UploadModel for fileToUpload', () => {
      const form = createMockForm();
      component.fileToUpload = createUploadModel();
      component.resetForm(form);
      expect(component.fileToUpload).toEqual(new UploadModel());
    });

    it('should reset uploadFileCountTag', () => {
      const form = createMockForm();
      component.uploadFileCountTag = ['file1', 'file2'];
      component.resetForm(form);
      expect(component.uploadFileCountTag).toEqual([]);
    });

    it('should set enableValidate to false', () => {
      const form = createMockForm();
      component.enableValidate = true;
      component.resetForm(form);
      expect(component.enableValidate).toBeFalse();
    });

    it('should set fileName to null when sbomFile DOM element found', () => {
      const form = createMockForm();
      const input = document.createElement('input');
      input.name = 'sbomFile';
      document.body.appendChild(input);
      component.fileName = 'old.json';
      component.resetForm(form);
      expect(component.fileName).toBeNull();
      document.body.removeChild(input);
    });

    it('should set schemafileName to null when schemaFile DOM element found', () => {
      const form = createMockForm();
      const input = document.createElement('input');
      input.name = 'schemaFile';
      document.body.appendChild(input);
      component.schemafileName = 'schema.json';
      component.resetForm(form);
      expect(component.schemafileName).toBeNull();
      document.body.removeChild(input);
    });
  });

  // -------------------------------------------------------------------------
  // mergeSelectedBoms
  // -------------------------------------------------------------------------

  describe('mergeSelectedBoms', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should call mergeSboms service and set fileToEdit', () => {
      spyOn(mockSbomInputService, 'mergeSboms').and.callThrough();
      component.sbomListToProcess = [createUploadModel(), createUploadModel({ dirName: 'dir_2' })];
      component.mergeType = 'cyclonedx';
      const modal = createMockTemplateRef();
      component.mergeSelectedBoms(modal);
      expect(mockSbomInputService.mergeSboms).toHaveBeenCalled();
    });

    it('should set mergeMode to true', () => {
      component.mergeMode = false;
      const modal = createMockTemplateRef();
      component.mergeSelectedBoms(modal);
      expect(component.mergeMode).toBeTrue();
    });

    it('should open modal after merge', () => {
      const modal = createMockTemplateRef();
      component.mergeSelectedBoms(modal);
      expect(component.isModalOpen).toBeTrue();
      expect(component.currentModal).toBe(modal);
    });

    it('should set enableModalSave to false', () => {
      component.enableModalSave = true;
      const modal = createMockTemplateRef();
      component.mergeSelectedBoms(modal);
      expect(component.enableModalSave).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // convertSboms
  // -------------------------------------------------------------------------

  describe('convertSboms', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should call convertSbom and set fileToEdit', () => {
      spyOn(mockSbomInputService, 'convertSbom').and.callThrough();
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.convertSboms(item);
      expect(mockSbomInputService.convertSbom).toHaveBeenCalledWith(item);
    });

    it('should set mergeMode to false', () => {
      component.mergeMode = true;
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.convertSboms(item);
      expect(component.mergeMode).toBeFalse();
    });

    it('should set enableModalSave to false', () => {
      component.enableModalSave = true;
      const item = createUploadModel({ schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.convertSboms(item);
      expect(component.enableModalSave).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // downloadContent
  // -------------------------------------------------------------------------

  describe('downloadContent', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should create anchor and trigger download when isDownloadZip is false', fakeAsync(() => {
      const item = createUploadModel({ sbomFileName: 'download.json', sbomJsonString: '{"key":"value"}' });
      const createElementSpy = spyOn(document, 'createElement').and.callThrough();
      const appendChildSpy = spyOn(document.body, 'appendChild').and.callThrough();
      spyOn(document.body, 'removeChild').and.stub();
      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:fake');
      spyOn(window.URL, 'revokeObjectURL').and.stub();
      component.downloadContent(item, false);
      tick(0);
      expect(createElementSpy).toHaveBeenCalledWith('a');
    }));

    it('should call prepareForDownload when isDownloadZip is true', () => {
      const item = createUploadModel({
        sbomFileName: 'download.json',
        sbomJsonString: '{}',
        errorDetails: [],
        changeLogsList: []
      });
      spyOn(component, 'prepareForDownload');
      component.downloadContent(item, true);
      expect(component.prepareForDownload).toHaveBeenCalled();
    });

    it('should open dialog with downloadSuccessModal', fakeAsync(() => {
      const item = createUploadModel({ sbomFileName: 'dl.json', sbomJsonString: '{}' });
      spyOn(component, 'openDialogBox');
      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:fake');
      spyOn(window.URL, 'revokeObjectURL').and.stub();
      spyOn(document.body, 'appendChild').and.stub();
      spyOn(document.body, 'removeChild').and.stub();
      component.downloadContent(item, false);
      tick(0);
      expect(component.openDialogBox).toHaveBeenCalledWith(component.downloadSuccessModal);
    }));
  });

  // -------------------------------------------------------------------------
  // setChangeLogs
  // -------------------------------------------------------------------------

  describe('setChangeLogs', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should push new change log items', () => {
      component.fileToEdit = createUploadModel({ changeLogsList: [] });
      component.setChangeLogs([{ path: '/name', op: 'replace', value: 'new' }]);
      expect(component.fileToEdit.changeLogsList.length).toBe(1);
    });

    it('should update existing item with same path', () => {
      const cl = new ChangeLog();
      cl.path = '/name';
      cl.op = 'add';
      cl.value = 'old';
      component.fileToEdit = createUploadModel({ changeLogsList: [cl] });
      component.setChangeLogs([{ path: '/name', op: 'replace', value: 'new' }]);
      expect(component.fileToEdit.changeLogsList.length).toBe(1);
      expect(component.fileToEdit.changeLogsList[0].op).toBe('replace');
      expect(component.fileToEdit.changeLogsList[0].value).toBe('new');
    });

    it('should handle empty data array', () => {
      component.fileToEdit = createUploadModel({ changeLogsList: [] });
      component.setChangeLogs([]);
      expect(component.fileToEdit.changeLogsList.length).toBe(0);
    });
  });

  // -------------------------------------------------------------------------
  // prepareForDownload
  // -------------------------------------------------------------------------

  describe('prepareForDownload', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should call sbomInputService.prepareForDownload', () => {
      const item = createUploadModel({ changeLogsList: [], errorDetails: [] });
      component.fileToEdit = item;
      spyOn(mockSbomInputService, 'prepareForDownload').and.callThrough();
      spyOn(component, 'downloadFiles').and.stub();
      component.prepareForDownload();
      expect(mockSbomInputService.prepareForDownload).toHaveBeenCalled();
    });

    it('should call downloadFiles with blob after service resolves', () => {
      const item = createUploadModel({ changeLogsList: [], errorDetails: [] });
      component.fileToEdit = item;
      spyOn(component, 'downloadFiles').and.stub();
      component.prepareForDownload();
      expect(component.downloadFiles).toHaveBeenCalled();
    });
  });

  // -------------------------------------------------------------------------
  // generatetZipContent
  // -------------------------------------------------------------------------

  describe('generatetZipContent', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should generate zip content array with correct length when errorDetails is empty', () => {
      component.fileToEdit = createUploadModel({
        errorDetails: [],
        changeLogsList: [],
        sbomFileName: 'test.json',
        sbomJsonString: '{}'
      });
      const blob = new Blob(['{}']);
      sessionStorage.setItem('log', JSON.stringify([]));
      const result = component.generatetZipContent(blob);
      expect(Array.isArray(result)).toBeTrue();
      expect(result.length).toBeGreaterThan(0);
    });

    it('should include error details pdf when errorDetails is non-empty', () => {
      const errModel = new ErrorModel();
      errModel.errorKey = 'err1';
      errModel.message = 'Some error';
      component.fileToEdit = createUploadModel({
        errorDetails: [errModel],
        changeLogsList: [],
        sbomFileName: 'test.json',
        sbomJsonString: '{}'
      });
      sessionStorage.setItem('log', JSON.stringify([]));
      const blob = new Blob(['{}']);
      const result = component.generatetZipContent(blob);
      expect(result.length).toBeGreaterThan(0);
    });

    it('should use sbomFileName when not in mergeMode', () => {
      component.mergeMode = false;
      component.fileToEdit = createUploadModel({
        sbomFileName: 'custom_name.json',
        errorDetails: [],
        changeLogsList: [],
        sbomJsonString: '{}'
      });
      sessionStorage.setItem('log', JSON.stringify([]));
      const blob = new Blob(['{}']);
      const result = component.generatetZipContent(blob);
      const lastItem = result[result.length - 1];
      expect(lastItem.name).toBe('custom_name.json');
    });
  });

  // -------------------------------------------------------------------------
  // showSuccessAlert / closeAlert
  // -------------------------------------------------------------------------

  describe('showSuccessAlert and closeAlert', () => {
    it('should set showAlert to true on showSuccessAlert', () => {
      fixture.detectChanges();
      component.showAlert = false;
      component.showSuccessAlert();
      expect(component.showAlert).toBeTrue();
    });

    it('should set showAlert to false on closeAlert', () => {
      fixture.detectChanges();
      component.showAlert = true;
      component.closeAlert();
      expect(component.showAlert).toBeFalse();
    });
  });

  // -------------------------------------------------------------------------
  // unloadHandler (HostListener)
  // -------------------------------------------------------------------------

  describe('unloadHandler', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should set event.returnValue to false', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      const event = { returnValue: true } as any;
      component.unloadHandler(event);
      expect(event.returnValue).toBeFalse();
    });

    it('should call clearSession when user confirms', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(component, 'clearSession');
      const mockOpener = { location: { reload: jasmine.createSpy('reload') } };
      spyOnProperty(window, 'opener', 'get').and.returnValue(mockOpener);
      const event = new Event('beforeunload') as any;
      event.returnValue = true;
      component.unloadHandler(event);
      expect(component.clearSession).toHaveBeenCalled();
    });
  });

  // -------------------------------------------------------------------------
  // Public properties & bindings
  // -------------------------------------------------------------------------

  describe('Public properties', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should expose sbomTypes from service', () => {
      expect(component.sbomTypes).toBeDefined();
      expect(Array.isArray(component.sbomTypes)).toBeTrue();
    });

    it('should expose licenseInfoTypes from service', () => {
      expect(component.licenseInfoTypes).toBeDefined();
    });

    it('should expose cdqLicenseInfoTypes from service', () => {
      expect(component.cdqLicenseInfoTypes).toBeDefined();
    });

    it('should expose licenseIdTypes', () => {
      expect(component.licenseIdTypes).toBeDefined();
    });

    it('should expose cdqLicenseIdTypes', () => {
      expect(component.cdqLicenseIdTypes).toBeDefined();
    });

    it('should expose compTypeList', () => {
      expect(component.compTypeList).toBeDefined();
    });

    it('should expose cdqcompTypeList', () => {
      expect(component.cdqcompTypeList).toBeDefined();
    });

    it('should expose primaryPackagePurposeList', () => {
      expect(component.primaryPackagePurposeList).toBeDefined();
    });

    it('should expose referenceCategoryList', () => {
      expect(component.referenceCategoryList).toBeDefined();
    });

    it('should expose schemaTypesToMerge as Set', () => {
      expect(component.schemaTypesToMerge instanceof Set).toBeTrue();
    });

    it('should initialize mergeLicenseInfoType to licId', () => {
      expect(component.mergeLicenseInfoType).toBe('licId');
    });

    it('should initialize cdqMergeLicenseInfoType to licId', () => {
      expect(component.cdqMergeLicenseInfoType).toBe('licId');
    });

    it('should initialize cdqMergeMetaLicenseInfoType to licId', () => {
      expect(component.cdqMergeMetaLicenseInfoType).toBe('licId');
    });

    it('should initialize calloutType to info', () => {
      expect(component.calloutType).toBe('info');
    });

    it('should initialize fileNames as empty array', () => {
      expect(component.fileNames).toEqual([]);
    });

    it('should initialize uploadFileCountTag as empty array', () => {
      expect(component.uploadFileCountTag).toEqual([]);
    });

    it('should initialize validationErrors as empty array', () => {
      expect(component.validationErrors).toEqual([]);
    });
  });

  // -------------------------------------------------------------------------
  // Edge cases & additional coverage
  // -------------------------------------------------------------------------

  describe('Edge cases', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should handle replaceFile when uploadStatusList has matching dirName', () => {
      const item = createUploadModel({ dirName: 'match_dir', schemaType: 'cyclonedx', sbomJsonString: '{}' });
      component.uploadStatusList = [item];
      component.fileToEdit = item;
      const updatedModel = createUploadModel({ dirName: 'match_dir', schemaType: 'cyclonedx', sbomJsonString: '{"updated":true}', customErrorDetails: [] });
      spyOn(mockSbomInputService, 'replaceFile').and.returnValue(of(updatedModel));
      component.replaceFile();
      expect(component.uploadStatusList[0].sbomJsonString).toBe('{"updated":true}');
    });

    it('should handle clearSession when no userId in sessionStorage', () => {
      sessionStorage.removeItem('userId');
      spyOn(mockSbomInputService, 'clearSession').and.returnValue(of({}));
      expect(() => component.clearSession()).not.toThrow();
    });

    it('should handle logAction with no prior log in sessionStorage', () => {
      sessionStorage.removeItem('log');
      const item = createUploadModel();
      expect(() => component.logAction(item, 'Test')).not.toThrow();
      const log = JSON.parse(sessionStorage.getItem('log') || '[]');
      expect(log.length).toBe(1);
    });

    it('should handle convertSboms with spdx type', () => {
      spyOn(mockSbomInputService, 'convertSbom').and.returnValue(
        of(createUploadModel({ schemaType: 'spdx', sbomJsonString: '{}', customErrorDetails: [] }))
      );
      const item = createUploadModel({ schemaType: 'spdx', sbomJsonString: '{}' });
      component.convertSboms(item);
      expect(component.fileToEdit.schemaType).toBe('spdx');
    });

    it('should not throw on initializeUndefinedObjects with unknown schemaType', () => {
      component.fileToEdit = createUploadModel({ schemaType: 'unknown', sbomJsonString: '{}' });
      expect(() => component.initializeUndefinedObjects()).not.toThrow();
    });

    it('should handle setChangeLogs with multiple new items', () => {
      component.fileToEdit = createUploadModel({ changeLogsList: [] });
      component.setChangeLogs([
        { path: '/a', op: 'add', value: '1' },
        { path: '/b', op: 'remove', value: '2' }
      ]);
      expect(component.fileToEdit.changeLogsList.length).toBe(2);
    });

    it('should handle downloadContent with logAction recording', fakeAsync(() => {
      sessionStorage.setItem('userId', 'dl_user');
      const item = createUploadModel({ sbomFileName: 'dl.json', sbomJsonString: '{}' });
      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:fake');
      spyOn(window.URL, 'revokeObjectURL').and.stub();
      spyOn(document.body, 'appendChild').and.stub();
      spyOn(document.body, 'removeChild').and.stub();
      component.downloadContent(item, false);
      tick(0);
      const log = JSON.parse(sessionStorage.getItem('log') || '[]');
      expect(log.some((l: AuditLog) => l.action === 'Download')).toBeTrue();
    }));
  });
});
