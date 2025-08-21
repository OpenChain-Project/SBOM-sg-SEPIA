/*
  Copyright © 2025 Robert Bosch GmbH. All rights reserved.
*/
package com.sepia.sbomutils.util;

public class Constants {
	
	private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }

	public static final String JSON = "json";
	public static final String OSPDX = "OSPDX";
	public static final String OCYDX = "OCYDX";
	public static final String JSN = "JSN";
	public static final String ATTACHMENT_JSON = "attachment; filename=downloaded.json";
	public static final String ATTACHMENT_XML = "attachment; filename=downloaded.xml";
	
	public static final String SBOM_INPUT = "sbomInput";
	public static final String OPT_UPLOAD_SBOM = "OptUploadBOM";
	public static final String CUSTOM = "custom";
	public static final String OPT_COMPAOSS = "OptCompaoss";
	
	public static final String SPDX = "SPDX";
	public static final String CYDX = "CYDX";
	public static final String BTOCYDX = "BTOCYDX";
	public static final String CYCLONEDX_LC = "cyclonedx";
	public static final String SPDX_LC = "spdx";
	public static final String SPDX2_2_LC = "spdx2.2";
	
	public static final String CDX_14 = "cdx14";
	public static final String SPDX_23 = "spdx23";
	
	public static final String UTF_8 = "UTF-8";
	public static final String UNDERSCORE = "_";
	public static final String ZERO = "0";
	
	public static final String DOLLAR_SCHEMA = "$schema";
	public static final String TILDE_SCHEMA = "~schema";
	public static final String ERROR_LOG_TXT = "error_log.txt";
	public static final String SUCCESS_LOG_TXT = "success_log.txt";
	public static final String ADD = "add";
	public static final String REPLACE = "replace";
	
	public static final String  MERGED_FILE = "Merged File";
	public static final String SCHEMA = "schema";
	
	public static final String JSON_EXT = ".json";
	
	public static final String PACKAGES = "packages";
	public static final String ANNOTATIONS = "annotations";
	public static final String EXTERNALDOCUMENTREFS = "externalDocumentRefs";
	public static final String HAS_EXTRACTED_LICENSING_INFOS = "hasExtractedLicensingInfos";
	public static final String FILES = "files";
	public static final String SNIPPETS = "snippets";
	public static final String RELATIONSHIPS = "relationships";
	public static final String SPDXID = "SPDXID";
	public static final String SPDX_VERSION = "spdxVersion";
	public static final String DATALICENSE = "dataLicense";
	public static final String DOCUMENT_NAMESPACE = "documentNamespace";
	public static final String CREATION_INFO = "creationInfo";
	public static final String NAME = "name";
	public static final String VERSION_INFO = "versionInfo";
	public static final String SPDX_ELEMENT_ID = "spdxElementId";
	public static final String RELATIONSHIP_TYPE = "relationshipType";
	public static final String RELATED_SPDX_ELEMENT = "relatedSpdxElement";
	public static final String RANGES = "ranges";
	public static final String END_POINTER = "endPointer";
	public static final String REFERENCE = "reference";
	public static final String START_POINTER = "startPointer";
	public static final String FILE_NAME = "FileName";
	public static final String SNIPPET_FROM_FILE = "snippetFromFile";
	public static final String DESCRIBES = "DESCRIBES";
	public static final String CONTAINS = "CONTAINS";
	public static final String DUPLICATE_SPDXID = "Duplicate SPDXID";
	public static final String ORGANIZATION = "Organization: ";
	public static final String UNKNOWN = "[UNKNOWN]";
	public static final String RDFXML = "rdf.xml";
	public static final String SYMBOLS = "^\"|\"$";
}
