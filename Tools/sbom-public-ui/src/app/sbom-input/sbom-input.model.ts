/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
export class UploadModel {
    inputType!: string;
    schemaType!: string;
    schemaVersion!: string;
    sbomFile!: File;
    schemaFile!: File;
    index!: number;
    schema!: boolean;
    timestamp!: any;
    hidden!: boolean;
    sbomFileName!: string;
    schemaFileName!: string;
    valid!: boolean;
    filePath!: string;
    errorDetails: ErrorModel[] = [];
    sbomJsonString!: string;
	schemaJsonString!: string;
    dirName!: string;
    sbomJson!: any;
    schemaJson!: any;
    changeLogsList: ChangeLog[] = [];
    fileHash!: string;
    customErrorDetails: ErrorModel[] = [];
    fossidServer!: String;
    apiKey!: String;
    scanCode!: String;
    ntid!:String;
    message!:string;
    status!: number;
}

export class ErrorModel {
    errorKey!: string;
    message!: string;
    path!: String[];
}

export class ChangeLog {
    fileName!: string;
	lineNumber!: string;
	changeDetails: string[] = [];
    op!: string;
	path!: string;
	value!: string;
  oldvalue!: string;
}

export class AuditLog {
    userId!: string;
    action!: string;
    fileName!: string;
    fileHash!: string;
    timestamp!: string;
    status!: string;
}
