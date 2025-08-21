/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { RestEndpointsService } from '../services/rest-endpoints.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  sticky = true;
  constructor(private router: Router,
    public restEndPointService:RestEndpointsService) {

  }
  alertModal!: TemplateRef<any> | null;
  isDialogBoxOpen = false;

  @ViewChild('noLogsModal')
  noLogsModal!: TemplateRef<any>;

  ngOnInit(): void {

  }

  ngAfterViewInit() {

  }

  downloadUserManual() {
    const url = this.restEndPointService.downloadUserManual; // Replace with your actual URL
    window.open(url, '_blank');
  }

  downloadAuditLog() {
    var auditLog = sessionStorage.getItem('log');
    if (auditLog !== null) {
      const auditLogPdf = new jsPDF("l", "cm", "a3");

      var head = [
        { title: "Action", dataKey: "action" },
        { title: "File Name", dataKey: "fileName" },
        { title: "File Hash", dataKey: "fileHash" },
        { title: "Timestamp", dataKey: "timestamp" }
      ];

      autoTable(auditLogPdf, {
        columns: head,
        body: JSON.parse(auditLog)
      })
      auditLogPdf.save('auditLog.pdf');
    } else {
      this.openDialogBox(this.noLogsModal);

    }
  }

  openDialogBox(modalName: TemplateRef<any>) {
    this.isDialogBoxOpen = true;
    this.alertModal = modalName;
  }

  closeDialogBox(){
    this.isDialogBoxOpen = false;
    this.alertModal = null;
  }


}
