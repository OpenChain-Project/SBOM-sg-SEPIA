# SEPIA Schema description
 
 This table refers the mandatory parameters from CycloneDX 1.6 and SPDX 2.3 as per the SEPIA Schema.


<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th>CycloneDX 1.6</th>
      <th>Description</th>
      <th>Mandatory/Optional</th>
      <th>SPDX 2.3</th>
      <th>Description</th>
      <th>Mandatory/Optional</th>
      <th></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>metadata.component.name</td>
      <td>The name of the component. This will often be a shortened, single name of the component.</td>
      <td>Mandatory</td>
      <td>Package:name</td>
      <td>Identify name of this SpdxElement.</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.component.group</td>
      <td>The grouping name or identifier.</td>
      <td>Mandatory</td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.component.type</td>
      <td>Specifies the type of component.</td>
      <td>Mandatory</td>
      <td>packages.PrimaryPackagePurpose</td>
      <td>This field provides information about the primary purpose of the identified package.</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.component.version</td>
      <td>The component version.</td>
      <td>Mandatory</td>
      <td>packages.versionInfo</td>
      <td>Identify the version of the package.</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.component.licenses.license.id                             or metadata.component.licenses.license.name                             or metadata.component.licenses.license.Expression</td>
      <td>A valid SPDX license identifier                   or The name of the license.                    or A valid SPDX license expression.</td>
      <td>Optional</td>
      <td>packages.LicenseConcluded</td>
      <td>List the licenses that have been declared by the authors of the package.</td>
      <td>Optional</td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.supplier.name</td>
      <td>The organization that supplied the component that the BOM describes. The supplier may often be the manufacturer, but may also be a distributor or repackager.</td>
      <td>Mandatory</td>
      <td>CreationInfo:Creator: Organization:</td>
      <td>Name of the organization that generated and provides this SBOM.</td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.supplier.contact.email</td>
      <td>The email address of the contact.</td>
      <td>Optional</td>
      <td>CreationInfo:Creator: Person:</td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.component.purl</td>
      <td>Asserts the identity of the component using package-url (purl).</td>
      <td>Mandatory</td>
      <td>packages.externalRefs.referenceCategory</td>
      <td>Category for the external reference</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.component.externalReferences.url</td>
      <td></td>
      <td>Optional</td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.licenses.license.id</td>
      <td>Specifies the details and attributes related to a software license.</td>
      <td>Optional</td>
      <td>dataLicense</td>
      <td>License expression for dataLicense.</td>
      <td>Optional</td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.licenses.license.name</td>
      <td></td>
      <td>Optional</td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.authors</td>
      <td>The person(s) who created the BOM. Authors are common in BOMs created through manual processes.</td>
      <td>Mandatory -> If manufacturer is not present</td>
      <td>CreationInfo:creators:Person</td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.tools.components.name</td>
      <td></td>
      <td>Mandatory</td>
      <td>CreationInfo:creators:Tool</td>
      <td>Identify who (or what, in the case of a tool) created the SPDX document.</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.manufacturer</td>
      <td>BOMs created through automated means may have @.manufacturer instead.</td>
      <td>Mandatory -> If Authors is not present</td>
      <td>CreationInfo:creators:Organization</td>
      <td>Identify who (or what, in the case of a tool) created the SPDX document.</td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.timestamp</td>
      <td>The date and time (timestamp) when the BOM was created.</td>
      <td>Mandatory</td>
      <td>CreationInfo.created</td>
      <td>Identify when the SPDX document was originally created.</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>Serialnumber</td>
      <td></td>
      <td>Mandatory</td>
      <td>documentNamespace</td>
      <td>The URI provides an unambiguous mechanism for other SPDX documents to reference SPDX elements within this SPDX document.</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>components.name</td>
      <td>The name of the component.</td>
      <td>Mandatory</td>
      <td>packages.name</td>
      <td>Identify the full name of the package as given by the Package Originator</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>components.version</td>
      <td>The component version.</td>
      <td>Mandatory</td>
      <td>packages.versionInfo</td>
      <td>Identify the version of the package.</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>component.cpe</td>
      <td>Specifies a well-formed CPE name that conforms to the CPE 2.2 or 2.3 specification.</td>
      <td>Mandatory</td>
      <td>packages.externalRefs.referenceCategory</td>
      <td>ExternalRef: SECURITY cpe23Type cpe:2.3:a:pivotal_software:spring_framework:4.1.0:*:*:*:*:*:*:*</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>components.purl</td>
      <td>Component Package URL (purl)</td>
      <td>Mandatory</td>
      <td>packages.externalRefs.referenceCategory</td>
      <td>Category for the external reference</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.components.supplier.bom-ref</td>
      <td>An optional identifier which can be used to reference the object elsewhere in the BOM.</td>
      <td>Optional</td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.components.supplier.name</td>
      <td>The name of the organization</td>
      <td>Optional</td>
      <td>Package.Supplier</td>
      <td>The name and, optionally, contact information of the person or organization who was the immediate supplier of this package to the recipient.</td>
      <td>Optional</td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.components.supplier.address</td>
      <td>The physical address (location) of the organization</td>
      <td>Optional</td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.components.supplier.url</td>
      <td>The URL of the organization.</td>
      <td>Optional</td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.components.supplier.contact</td>
      <td>A contact at the organization</td>
      <td>Optional</td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.authors (person in SPDX)</td>
      <td>The person(s) who created the BOM.Authors are common in BOMs created through manual processes. BOMs created through automated means may have @.manufacturer instead.</td>
      <td>Mandatory - If manufacturer is not present</td>
      <td>CreationInfo:creators:Person (if orgranization is absent)</td>
      <td>Identify who (or what, in the case of a tool) created the SPDX document.</td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.manufacturer</td>
      <td>The organization that created the BOM.Manufacturer is common in BOMs created through automated processes. BOMs created through manual means may have @.authors instead.</td>
      <td>Mandatory - If Authors is not present</td>
      <td>CreationInfo:creators:Organization</td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
      <td></td>
    </tr>
    <tr>
      <td>metadata.timestamp</td>
      <td>The date and time (timestamp) when the BOM was created</td>
      <td>Mandatory</td>
      <td>CreationInfo.created</td>
      <td>Identify when the SPDX document was originally created.</td>
      <td>Mandatory</td>
      <td></td>
    </tr>
  </tbody>
</table>