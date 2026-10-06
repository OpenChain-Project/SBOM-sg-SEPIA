/*
 Parts of this file are created by genAI by using GitHub Copilot.
 This notice needs to remain attached to any reproduction of or excerpt from this file.
// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
// SPDX-License-Identifier: MIT
*/
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NgForm } from '@angular/forms';

import { SbomInputService } from './sbom-input.service';
import { RestEndpointsService } from './rest-endpoints.service';
import { UploadModel } from '../sbom-input/sbom-input.model';
import { CycloneDXSBOMStandard } from '../models/cyclonedx.model';
import { SpdxModel } from '../models/spdx.model';
import { CDQCycloneDXSBOMStandard } from '../models/cdqcyclonedx.model';

describe('SbomInputService', () => {
	let service: SbomInputService;
	let httpMock: HttpTestingController;
	let endpoints: RestEndpointsService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [HttpClientTestingModule],
			providers: [SbomInputService, RestEndpointsService]
		});

		service = TestBed.inject(SbomInputService);
		httpMock = TestBed.inject(HttpTestingController);
		endpoints = TestBed.inject(RestEndpointsService);
		sessionStorage.clear();
	});

	afterEach(() => {
		httpMock.verify();
		sessionStorage.clear();
	});

	function getPostData(reqBody: FormData): any {
		return JSON.parse(String(reqBody.get('postData')));
	}

	describe('HTTP API methods', () => {
		it('should post validateFiles payload', () => {
			const model = new UploadModel();
			model.sbomFileName = 'test.json';

			service.validateFiles(model).subscribe((res) => {
				expect(res).toEqual({ ok: true });
			});

			const req = httpMock.expectOne(endpoints.validateFiles);
			expect(req.request.method).toBe('POST');
			const body = req.request.body as FormData;
			expect(getPostData(body).sbomFileName).toBe('test.json');
			req.flush({ ok: true });
		});

		it('should post deleteSbomEntry payload', () => {
			const model = new UploadModel();
			model.fileHash = 'hash-1';

			service.deleteSbomEntry(model).subscribe();

			const req = httpMock.expectOne(endpoints.deleteSbomEntry);
			expect(req.request.method).toBe('POST');
			const body = req.request.body as FormData;
			expect(getPostData(body).fileHash).toBe('hash-1');
			req.flush({ ok: true });
		});

		it('should call clearSession when token exists', () => {
			sessionStorage.setItem('token', 'token-123');

			service.clearSession().subscribe((res) => {
				expect(res).toEqual({ ok: true });
			});

			const req = httpMock.expectOne(endpoints.clearSession);
			const body = req.request.body as FormData;
			expect(getPostData(body).sessionId).toBe('token-123');
			req.flush({ ok: true });
		});

		it('should not call clearSession endpoint when token does not exist', () => {
			service.clearSession().subscribe();
			httpMock.expectNone(endpoints.clearSession);
			expect(sessionStorage.getItem('token')).toBeNull();
		});

		it('should post convertSbom payload with app flag', () => {
			const model = new UploadModel();
			model.schemaType = 'spdx';

			service.convertSbom(model).subscribe();

			const req = httpMock.expectOne(endpoints.convertSbom);
			const body = req.request.body as FormData;
			expect(getPostData(body).schemaType).toBe('spdx');
			expect(body.get('isFromApp')).toBe('true');
			req.flush({ ok: true });
		});

		it('should post to fetchSbomFromFossid and enrich item/session data', () => {
			const form = { value: { schemaType: 'spdx2.2' } } as NgForm;
			const item = new UploadModel();

			service.fetchSbomFromFossid(form, item, 5).subscribe();

			const req = httpMock.expectOne(endpoints.fetchSbomFromFossid);
			const body = req.request.body as FormData;
			const postData = getPostData(body);
			expect(postData.index).toBe(5);
			expect(postData.schemaType).toBe('spdx2.2');
			expect(postData.dirName).toBe('5_spdx2.2');
			expect(postData.sessionId).toBeTruthy();
			expect(body.get('isFromApp')).toBe('true');
			req.flush({ ok: true });
		});

		it('should post uploadFile with file for non-schema upload', () => {
			const file = new File(['{}'], 'sbom.json', { type: 'application/json' });
			const form = {
				value: {
					inputType: 'upload',
					schemaType: 'cyclonedx'
				}
			} as NgForm;

			service.uploadFile(form, 2, false, file).subscribe();

			const req = httpMock.expectOne(endpoints.uploadFile);
			const body = req.request.body as FormData;
			const postData = getPostData(body);
			expect(postData.inputType).toBe('upload');
			expect(postData.schemaType).toBe('cyclonedx');
			expect(postData.schemaVersion).toBe('1.4');
			expect(postData.dirName).toBe('2_cyclonedx');
			expect(body.get('file')).toBe(file);
			req.flush({ ok: true });
		});

		it('should include schema file only for custom schema uploads', () => {
			const file = new File(['{}'], 'schema.json', { type: 'application/json' });
			const form = {
				value: {
					inputType: 'upload',
					schemaType: 'custom'
				}
			} as NgForm;

			service.uploadFile(form, 0, true, file).subscribe();

			const req = httpMock.expectOne(endpoints.uploadFile);
			const body = req.request.body as FormData;
			expect(body.get('file')).toBe(file);
			req.flush({ ok: true });
		});

		it('should not include schema file for non-custom schema uploads', () => {
			const file = new File(['{}'], 'schema.json', { type: 'application/json' });
			const form = {
				value: {
					inputType: 'upload',
					schemaType: 'spdx'
				}
			} as NgForm;

			service.uploadFile(form, 1, true, file).subscribe();

			const req = httpMock.expectOne(endpoints.uploadFile);
			const body = req.request.body as FormData;
			expect(body.get('file')).toBeNull();
			req.flush({ ok: true });
		});
	});

	describe('mergeSboms routing', () => {
		it('should call mergeCyclonedx for cyclonedx type', () => {
			service.mergeSboms([], new CycloneDXSBOMStandard(), new SpdxModel(), new SpdxModel(), new CDQCycloneDXSBOMStandard(), 'cyclonedx').subscribe();

			const req = httpMock.expectOne(endpoints.mergeCyclonedx);
			const body = req.request.body as FormData;
			expect(body.get('bomMetadata')).toContain('metadata');
			expect(body.get('isFromApp')).toBe('true');
			req.flush({ ok: true });
		});

		it('should call mergeCyclonedx for cdqcydx type', () => {
			service.mergeSboms([], new CycloneDXSBOMStandard(), new SpdxModel(), new SpdxModel(), new CDQCycloneDXSBOMStandard(), 'cdqcydx').subscribe();

			const req = httpMock.expectOne(endpoints.mergeCyclonedx);
			const body = req.request.body as FormData;
			expect(body.get('bomMetadata')).toContain('metadata');
			req.flush({ ok: true });
		});

		it('should call mergeSpdx for cdqspdx2.3 type', () => {
			service.mergeSboms([], new CycloneDXSBOMStandard(), new SpdxModel(), new SpdxModel(), new CDQCycloneDXSBOMStandard(), 'cdqspdx2.3').subscribe();

			const req = httpMock.expectOne(endpoints.mergeSpdx);
			const body = req.request.body as FormData;
			expect(body.get('bomMetadata')).toContain('packages');
			req.flush({ ok: true });
		});

		it('should call mergeSpdx for default type', () => {
			service.mergeSboms([], new CycloneDXSBOMStandard(), new SpdxModel(), new SpdxModel(), new CDQCycloneDXSBOMStandard(), 'spdx').subscribe();

			const req = httpMock.expectOne(endpoints.mergeSpdx);
			expect(req.request.method).toBe('POST');
			req.flush({ ok: true });
		});
	});

	describe('initialization helpers', () => {
		it('should initialize SPDX defaults when model is mostly empty', () => {
			const input = new SpdxModel();
			input.creationInfo = undefined as unknown as any;
			input.packages = [];

			const result = service.initializeSpdxUndefinedObjects(input);

			expect(result.spdxVersion).toBe('');
			expect(result.documentNamespace).toBe('');
			expect(result.dataLicense).toBe('');
			expect(result.creationInfo.created).toBe('');
			expect(result.packages.length).toBe(1);
			expect(result.packages[0].name).toBe('');
		});

		it('should initialize CDQ SPDX package defaults when external refs are missing', () => {
			const input = new SpdxModel();
			input.packages = [service.initializeCDQSpdxPackage()];
			input.packages[0].externalRefs = undefined as unknown as any;

			const result = service.initializeCDQSpdxUndefinedObjects(input);

			expect(result.packages[0].externalRefs.length).toBe(1);
			expect(result.packages[0].externalRefs[0].referenceCategory).toBeNull();
		});

		it('should initialize CycloneDX top-level defaults', () => {
			const input = new CycloneDXSBOMStandard();

			const result = service.initializeCycloneDXUndefinedObjects(input);

			expect(result.specVersion).toBe('1.0');
			expect(result.serialNumber).toBe('');
			expect(result.version).toBe(1);
			expect(result.components.length).toBeGreaterThan(0);
		});

		it('should initialize CDQ CycloneDX defaults with specVersion 1.6', () => {
			const input = new CDQCycloneDXSBOMStandard();

			const result = service.initializeCDQCycloneDXUndefinedObjects(input);

			expect(result.specVersion).toBe('1.6');
			expect(result.serialNumber).toBe('');
			expect(result.version).toBe(1);
			expect(result.components.length).toBeGreaterThan(0);
		});
	});

	describe('error handling', () => {
		it('should return undefined fallback when HTTP call fails', () => {
			const model = new UploadModel();
			let responseValue: any;

			service.validateFiles(model).subscribe((res) => {
				responseValue = res;
			});

			const req = httpMock.expectOne(endpoints.validateFiles);
			req.flush('boom', { status: 500, statusText: 'Server Error' });

			expect(responseValue).toBeUndefined();
		});
	});
});
