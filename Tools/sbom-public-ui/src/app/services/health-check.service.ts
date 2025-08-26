// health-check.service.ts
/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: MIT */
import { Injectable } from '@angular/core';
import { HttpClient,HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RestEndpointsService } from './rest-endpoints.service';

@Injectable({
  providedIn: 'root'
})
export class HealthCheckService {
  private healthCheckUrl = '/api/health-check'; // Replace with your actual health check API endpoint

  constructor(private http: HttpClient,private restEndPointService: RestEndpointsService) {}

  checkServerHealth(): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, PATCH, PUT, DELETE, OPTIONS',
            'Access-Control-Allow-Headers': 'Origin, Content-Type, X-Auth-Token'
    });

    return this.http.get(this.restEndPointService.healthCheckup,{ headers }).pipe(
      catchError(() => {
        return of({ status: 'Server is down' });
      })
    );
  }
}
