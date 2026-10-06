# Automotive Schema Description

Comparison of **CycloneDX 1.6** and **SPDX 2.3** schema properties, descriptions, and mandatory/optional status.

| CycloneDX 1.6 | Description | Mandatory/Optional | SPDX 2.3 | Description | Mandatory/Optional |
|---|---|---|---|---|---|
| bomFormat | Specifies the format of the BOM | Mandatory |  |  |  |
| specVersion | The version of the CycloneDX specification | Mandatory | spdxVersion | Provide a reference number that can be used to understand how to parse and interpret the rest of the file. | Mandatory |
| serialNumber | Every BOM generated SHOULD have a unique serial number, even if the contents of the BOM have not changed over time | Mandatory | documentNamespace | The URI provides an unambiguous mechanism for other SPDX documents to reference SPDX elements within this SPDX document. | Mandatory |
| version | Whenever an existing BOM is modified, either manually or through automated processes, the version of the BOM SHOULD be incremented by 1 | Mandatory |  |  |  |
| metadata.authors.bom-ref | An identifier which can be used to reference the object elsewhere in the BOM | Mandatory |  |  |  |
| metadata.authors.name | The name of a contact | Mandatory | creationInfo.creators[]:Person | Identify who (or what, in the case of a tool) created the SPDX document | Mandatory |
| metadata.authors.email | The email address of the contact. | Mandatory |  |  |  |
| metadata.authors.phone | The phone number of the contact. | Mandatory |  |  |  |
| metadata.timestamp | The date and time (timestamp) when the BOM was created | Mandatory | creationInfo.Created | Identify when the SPDX document was originally created | Mandatory |
| metadata.lifecycles.phase | A pre-defined phase in the product lifecycle | Mandatory |  |  |  |
| metadata.component.name | The name of the component | Mandatory | packages.name | Identify name of this SpdxElement | Mandatory |
| metadata.component.type | Specifies the type of component | Mandatory |  |  |  |
| metadata.supplier.bom-ref |  |  |  |  |  |
| metadata.supplier.name | The name of the organization | Mandatory | creationInfo.creators[]:Organization |  | Mandatory |
| components[].name | The name of the component. | Mandatory | packages.name |  | Mandatory |
| components[].version | The component version | Mandatory | package.versionInfo | Provides an indication of the version of the package that is described by this SpdxDocument. | Mandatory |
| components[].supplier.bom-ref |  | Mandatory |  |  |  |
| components[].supplier.name | The organization that supplied the component | Mandatory | package.supplier | The name and, optionally, contact information of the person or organization who was the immediate supplier of this package to the recipient. | Mandatory |
| components[]. cpe | Asserts the identity of the component using CPE | Mandatory | package.externalRefs |  | Mandatory |
| components[].purl | Asserts the identity of the component using package-url (purl) | Mandatory | package.externalRefs.referenceLocator<br>package.externalRefs.referenceCategory<br>package.externalRefs.referenceType<br>package.externalRefs.comment | An External Reference allows a Package to reference an external source of additional information, metadata, enumerations, asset identifiers, or downloadable content believed to be relevant to the Package. | Mandatory |
| components[].evidence[].identity[].methods[].[{"technique":"","confidence":,"value":}] | The methods used to extract and/or analyze the evidence. | Mandatory |  |  |  |
|  |  | Mandatory |  |  |  |
| components [].externalReferences [].url | The URI (URL or URN) to the external reference | Mandatory | package.downloadLocation | The URI at which this package is available for download | Mandatory |
| components [].externalReferences [].type | Specifies the type of external reference | Mandatory |  |  |  |
| components[].licenses[].license.id | A valid SPDX license identifier. | Mandatory | package.licenseConcluded | License expression for licenseConcluded | Mandatory |
| components[].licenses[].acknowledgement[] |  | Mandatory | package.licenseDeclared or package.licenseConcluded | License expression for licenseDeclared or<br>License expression for licenseConcluded | Mandatory |
| components[].hashes[].alg | The algorithm that generated the hash value | Mandatory | package.checksums.algorithm | Identifies the algorithm used to produce the subject Checksum | Mandatory |
| components[].hashes[].content | The value of the hash. | Mandatory | package.checksums.checksumValue | The checksumValue property provides a lower case hexidecimal encoded digest value produced using a specific algorithm. | Mandatory |
| components[].copyright | A copyright notice informing users of the underlying claims to copyright ownership in a published work. | Mandatory | package.copyrightText | The text of copyright declarations recited in the package | Mandatory |
|  |  |  | package.packageFileName | The base name of the package file name | Mandatory |
|  |  |  | package.packageVerificationCode.packageVerificationCodeExcludedFiles | A file that was excluded when calculating the package verification code | Mandatory |
|  |  |  | package.packageVerificationCode.packageVerificationCodeValue | The actual package verification code as a hex encoded value. | Mandatory |
|  |  |  | externalDocumentRefs.checksum | A Checksum is value that allows the contents of a file to be authenticated. | Mandatory |
|  |  |  | externalDocumentRefs.externalDocumentId | externalDocumentId is a string containing letters, numbers, ., - and/or + which uniquely identifies an external document within this document. | Mandatory |
|  |  |  | externalDocumentRefs.spdxDocument | SPDX ID for SpdxDocument. | Mandatory |
|  |  |  | relationships | Relationships referenced in the SPDX document | Mandatory |
