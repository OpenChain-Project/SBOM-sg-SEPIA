/* SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA

SPDX-License-Identifier: MIT */
export class SpdxModel {
  SPDXID!: string;
  annotations: Annotations[] = [];
  comment?: string;
  creationInfo: CreationInfo = new CreationInfo();
  dataLicense!: string;
  externalDocumentRefs: ExternalDocumentRefs[] = [];
  hasExtractedLicensingInfos: HasExtractedLicensingInfos[] = [];
  name!: string;
  revieweds: Revieweds[] = [];
  spdxVersion!: string;
  documentNamespace!: string;
  documentDescribes?: string[];
  packages: Packages[] = [];
  files: Files[] = [];
  snippets: Snippets[] = [];
  relationships: Relationships[] = [];
}


export class Annotations {
  annotationDate!: string;
  annotationType!: "OTHER" | "REVIEW";
  annotator!: string;
  comment!: string;
}

export class CreationInfo {
  comment?: string;
  created!: string;
  creators!: [String, ...String[]];
  licenseListVersion?: string;
}


export class ExternalDocumentRefs {
  checksum!: {
    algorithm:
      | "SHA1"
      | "BLAKE3"
      | "SHA3-384"
      | "SHA256"
      | "SHA384"
      | "BLAKE2b-512"
      | "BLAKE2b-256"
      | "SHA3-512"
      | "MD2"
      | "ADLER32"
      | "MD4"
      | "SHA3-256"
      | "BLAKE2b-384"
      | "SHA512"
      | "MD6"
      | "MD5"
      | "SHA224";
    checksumValue: string;
  }
  externalDocumentId!: string;
  spdxDocument!: string;
}

export class HasExtractedLicensingInfos {
  comment?: string;
  crossRefs:crossRefs[] = [];
  extractedText!: string;
  licenseId!: string;
  name?: string;
  seeAlsos?: string[];
}

export class crossRefs {
  isLive?: boolean;
  isValid?: boolean;
  isWayBackLink?: boolean;
  match?: string;
  order?: number;
  timestamp?: string;
  url!: string;
}

export class Revieweds {
  comment?: string;
  reviewDate!: string;
  reviewer?: string;
}


export class Packages {
  SPDXID!: string;
  annotations?: {
    annotationDate: string;
    annotationType: "OTHER" | "REVIEW";
    annotator: string;
    comment: string;
  }[];
  attributionTexts?: string[];
  builtDate?: string;
  checksums?: {
    algorithm:
      | "SHA1"
      | "BLAKE3"
      | "SHA3-384"
      | "SHA256"
      | "SHA384"
      | "BLAKE2b-512"
      | "BLAKE2b-256"
      | "SHA3-512"
      | "MD2"
      | "ADLER32"
      | "MD4"
      | "SHA3-256"
      | "BLAKE2b-384"
      | "SHA512"
      | "MD6"
      | "MD5"
      | "SHA224";
    checksumValue: string;
  }[];
  comment?: string;
  copyrightText?: string;
  description?: string;
  downloadLocation!: string;
  externalRefs: externalRefs[] = [];
  // externalRefs?: {
  //   comment?: string;
  //   referenceCategory:
  //     | "OTHER"
  //     | "PERSISTENT-ID"
  //     | "PERSISTENT_ID"
  //     | "SECURITY"
  //     | "PACKAGE-MANAGER"
  //     | "PACKAGE_MANAGER";
  //   referenceLocator: string;
  //   referenceType: string;
  // }[];
  filesAnalyzed?: boolean;
  /**
   * @deprecated
   * DEPRECATED: use relationships instead of this field. Indicates that a particular file belongs to a package.
   */
  hasFiles?: string[];
  homepage?: string;
  licenseComments?: string;
  licenseConcluded?: string;
  licenseDeclared?: string;
  licenseInfoFromFiles?: string[];
  name!: string;
  originator?: string;
  packageFileName?: string;
  packageVerificationCode?: {
    packageVerificationCodeExcludedFiles?: string[];
    packageVerificationCodeValue: string;
  };
  primaryPackagePurpose?:
    | null
    | "OTHER"
    | "INSTALL"
    | "ARCHIVE"
    | "FIRMWARE"
    | "APPLICATION"
    | "FRAMEWORK"
    | "LIBRARY"
    | "CONTAINER"
    | "SOURCE"
    | "DEVICE"
    | "OPERATING_SYSTEM"
    | "FILE";
  releaseDate?: string;
  sourceInfo?: string;
  summary?: string;
  supplier?: string;
  validUntilDate?: string;
  versionInfo?: string;
}

export enum primaryPackagePurpose {
  OTHER = "OTHER",
  INSTALL = "INSTALL",
  ARCHIVE = "ARCHIVE",
  FIRMWARE = "FIRMWARE",
  APPLICATION = "APPLICATION",
  FRAMEWORK = "FRAMEWORK",
  LIBRARY = "LIBRARY",
  CONTAINER = "CONTAINER",
  SOURCE = "SOURCE",
  DEVICE = "DEVICE",
  OPERATING_SYSTEM = "OPERATING_SYSTEM",
  FILE = "FILE"
};
export class Files {
  SPDXID!: string;
  annotations?: {
    annotationDate: string;
    annotationType: "OTHER" | "REVIEW";
    annotator: string;
    comment: string;
  }[];
  artifactOfs?: {
    [k: string]: unknown;
  }[];
  attributionTexts?: string[];
  checksums?: [
    {
      /**
       * Identifies the algorithm used to produce the subject Checksum. Currently, SHA-1 is the only supported algorithm. It is anticipated that other algorithms will be supported at a later time.
       */
      algorithm:
        | "SHA1"
        | "BLAKE3"
        | "SHA3-384"
        | "SHA256"
        | "SHA384"
        | "BLAKE2b-512"
        | "BLAKE2b-256"
        | "SHA3-512"
        | "MD2"
        | "ADLER32"
        | "MD4"
        | "SHA3-256"
        | "BLAKE2b-384"
        | "SHA512"
        | "MD6"
        | "MD5"
        | "SHA224";
      checksumValue: string;
    },
    ...{
      algorithm:
        | "SHA1"
        | "BLAKE3"
        | "SHA3-384"
        | "SHA256"
        | "SHA384"
        | "BLAKE2b-512"
        | "BLAKE2b-256"
        | "SHA3-512"
        | "MD2"
        | "ADLER32"
        | "MD4"
        | "SHA3-256"
        | "BLAKE2b-384"
        | "SHA512"
        | "MD6"
        | "MD5"
        | "SHA224";
      checksumValue: string;
    }[]
  ];
  comment?: string;
  copyrightText?: string;
  fileContributors?: string[];
  /**
   * @deprecated
   * This field is deprecated since SPDX 2.0 in favor of using Section 7 which provides more granularity about relationships.
   */
  fileDependencies?: string[];
  fileName!: string;
  fileTypes?: (
    | "OTHER"
    | "DOCUMENTATION"
    | "IMAGE"
    | "VIDEO"
    | "ARCHIVE"
    | "SPDX"
    | "APPLICATION"
    | "SOURCE"
    | "BINARY"
    | "TEXT"
    | "AUDIO"
  )[];
  licenseComments?: string;
  licenseConcluded?: string;
  licenseInfoInFiles?: string[];
  noticeText?: string;
}

export class Snippets {
  SPDXID!: string;
  annotations?: {
    annotationDate: string;
    annotationType: "OTHER" | "REVIEW";
    annotator: string;
    comment: string;
  }[];
  attributionTexts?: string[];
  comment?: string;
  copyrightText?: string;
  licenseComments?: string;
  licenseConcluded?: string;
  licenseInfoInSnippets?: string[];
  name!: string;
  ranges!: [
    {
      endPointer: {
        reference: string;
        offset?: number;
        lineNumber?: number;
      };
      startPointer: {
        reference: string;
        offset?: number;
        lineNumber?: number;
      };
    },
    ...{
      endPointer: {
        reference: string;
        offset?: number;
        lineNumber?: number;
      };
      startPointer: {
        reference: string;
        offset?: number;
        lineNumber?: number;
      };
    }[]
  ];
  snippetFromFile!: string;
}

export class Relationships {
  spdxElementId!: string;
  comment?: string;
  relatedSpdxElement!: string;
  relationshipType!:
    | "VARIANT_OF"
    | "COPY_OF"
    | "PATCH_FOR"
    | "TEST_DEPENDENCY_OF"
    | "CONTAINED_BY"
    | "DATA_FILE_OF"
    | "OPTIONAL_COMPONENT_OF"
    | "ANCESTOR_OF"
    | "GENERATES"
    | "CONTAINS"
    | "OPTIONAL_DEPENDENCY_OF"
    | "FILE_ADDED"
    | "REQUIREMENT_DESCRIPTION_FOR"
    | "DEV_DEPENDENCY_OF"
    | "DEPENDENCY_OF"
    | "BUILD_DEPENDENCY_OF"
    | "DESCRIBES"
    | "PREREQUISITE_FOR"
    | "HAS_PREREQUISITE"
    | "PROVIDED_DEPENDENCY_OF"
    | "DYNAMIC_LINK"
    | "DESCRIBED_BY"
    | "METAFILE_OF"
    | "DEPENDENCY_MANIFEST_OF"
    | "PATCH_APPLIED"
    | "RUNTIME_DEPENDENCY_OF"
    | "TEST_OF"
    | "TEST_TOOL_OF"
    | "DEPENDS_ON"
    | "SPECIFICATION_FOR"
    | "FILE_MODIFIED"
    | "DISTRIBUTION_ARTIFACT"
    | "AMENDS"
    | "DOCUMENTATION_OF"
    | "GENERATED_FROM"
    | "STATIC_LINK"
    | "OTHER"
    | "BUILD_TOOL_OF"
    | "TEST_CASE_OF"
    | "PACKAGE_OF"
    | "DESCENDANT_OF"
    | "FILE_DELETED"
    | "EXPANDED_FROM_ARCHIVE"
    | "DEV_TOOL_OF"
    | "EXAMPLE_OF";
}


export class externalRefs {
  comment?: string;
  referenceCategory!:
    | null
    | "OTHER"
    | "PERSISTENT-ID"
    | "PERSISTENT_ID"
    | "SECURITY"
    | "PACKAGE-MANAGER"
    | "PACKAGE_MANAGER";
  referenceLocator?: string;
  referenceType?: string;
}
