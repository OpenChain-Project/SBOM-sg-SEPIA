# SBOM Public Service API

This document describes the `/health`, `/uploadAndValidate`, `/customValidate`, and `/validateAndConvert` endpoints implemented by `SbomUtilityController`.

Set `BASE_URL` to the service origin, including any configured context path. The examples use `http://localhost:9051`; the deployed host and port depend on the environment. The swagger UI can be accessed using 'http://localhost:9051/swagger-ui.html#/'

## General Request Conventions

The three SBOM operations accept `multipart/form-data`. Each request contains a form field named `postData` whose value is a JSON-encoded `BomFilesInputModel`, plus one or more file parts. `postData` is a form field, not a standalone `application/json` request body.

The model accepts unknown JSON properties, but the following fields are relevant to these endpoints:

| `postData` property | Purpose |
| --- | --- |
| `schemaType` | Required format/type selector. Exact accepted values depend on the endpoint. Case-insensitive. |
| `sessionId` | Optional caller session identifier. The upload service creates one when absent. |
| `schema` | Boolean model flag used by custom-schema upload processing. Defaults to `false` when omitted. |

The configured Spring multipart maximum file size and request size are both 100 MB. A reverse proxy or deployment may impose a lower limit.

## `GET /health`

Simple liveness response. The optional `iv-user` request header is reflected in `userId`.

### Request

```http
GET /health
iv-user: example-user
```

### Response

Returns HTTP `200 OK` with a JSON object:

```json
{
  "status": "Server is running",
  "userId": "example-user"
}
```

If `iv-user` is not supplied, `userId` is null (and may be serialized as `null`).

## `POST /uploadAndValidate`

Uploads one SBOM JSON file and validates it against the built-in schema selected by `schemaType`.

### Form fields

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `file` | File | Yes | One `.json` SBOM file. Content must be a JSON object and match the selected format/version. |
| `postData` | Text | Yes | JSON-encoded model containing at least `schemaType`. |

Accepted `schemaType` values and content checks:

| `schemaType` | Expected uploaded document |
| --- | --- |
| `cyclonedx` | CycloneDX with `bomFormat: "CycloneDX"` and `specVersion: "1.4"` |
| `cdqcydx` | CycloneDX with `bomFormat: "CycloneDX"` and `specVersion: "1.6"` |
| `spdx` | SPDX JSON with a non-empty `spdxVersion`; schema validation then applies the service's SPDX 2.3 schema |
| `cdqspdx2.3` | SPDX JSON with `spdxVersion: "SPDX-2.3"` |

The filename must end in `.json` (case-insensitive). The controller checks the uploaded document's top-level format/version before handing it to the validator. This endpoint does not accept the automotive or custom schema types, even though other service code supports additional types.

### Example

```bash
curl -X POST "$BASE_URL/uploadAndValidate" \
  -F 'file=@./sbom.json;type=application/json' \
  -F 'postData={"schemaType":"cdqcydx"}'
```

### Response

Returns a JSON object based on `BomFilesInputModel`. It can include `schemaType`, `schemaVersion`, `sessionId`, `index`, `sbomFileName`, `valid`, `errorDetails`, `customErrorDetails`, `fileHash`, and `message`, depending on the result. Null properties are omitted. The controller removes `schema`, `filePath`, `validatedAlready`, and `status`; the service also clears `sbomJsonString` for this API upload path.

Example validation result:

```json
{
  "schemaType": "cdqcydx",
  "schemaVersion": "1.6",
  "index": 0,
  "sessionId": "1720000000000",
  "sbomFileName": "sbom.json",
  "valid": false,
  "errorDetails": [
    {
      "errorKey": "$.metadata",
      "message": "Example schema validation error"
    }
  ],
  "message": "File uploaded successfully and validation is completed with 1 errors"
}
```

The example error text and session ID are illustrative; actual validation details depend on the uploaded SBOM.

## `POST /customValidate`

Uploads an SBOM JSON file and a custom JSON Schema file, then validates the SBOM using that schema. This endpoint requires `schemaType: "custom"`.

### Form fields

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `file` | File | Yes | SBOM file. Its original filename must end in `.json`. |
| `schemaFile` | File | Yes | Custom JSON Schema document. Its contents must be readable as JSON and use a supported `$schema` draft. |
| `postData` | Text | Yes | JSON-encoded model with `schemaType` set to `custom`. |

The validator recognizes JSON Schema Draft 7, Draft 2019-09, and Draft 2020-12 identifiers (HTTP and HTTPS variants where implemented). The controller does not enforce a `.json` suffix on the schema filename, but the schema contents must be JSON.

### Example

```bash
curl -X POST "$BASE_URL/customValidate" \
  -F 'file=@./sbom.json;type=application/json' \
  -F 'schemaFile=@./sbom.schema.json;type=application/json' \
  -F 'postData={"schemaType":"custom"}'
```

### Response

Returns a JSON object based on `BomFilesInputModel`, including validation results such as `valid`, `errorDetails`, `customErrorDetails`, and `message` when present. The controller removes `schema`, `filePath`, `validatedAlready`, `status`, and `schemaJsonString` from the response. The custom schema itself is therefore not returned.

Example:

```json
{
  "schemaType": "custom",
  "index": 0,
  "sessionId": "1720000000000",
  "sbomFileName": "sbom.json",
  "valid": true,
  "errorDetails": [],
  "message": "File uploaded successfully and validation is completed without any errors"
}
```

The response properties are conditional; the service omits null values.

## `POST /validateAndConvert`

Validates a SEPIA CycloneDX 1.6 or SPDX 2.3 SBOM, converts it to the other format, then validates the converted result.

### Form fields

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `file` | File | Yes | One `.json` SBOM file. |
| `postData` | Text | Yes | JSON-encoded model containing `schemaType`. |

Accepted input types:

| `schemaType` | Required uploaded content | Converted output |
| --- | --- | --- |
| `cdqcydx` | CycloneDX 1.6 (`bomFormat: "CycloneDX"`, `specVersion: "1.6"`) | SPDX 2.3 (`schemaType: "cdqspdx2.3"`) |
| `cdqspdx2.3` | SPDX 2.3 (`spdxVersion: "SPDX-2.3"`) | CycloneDX 1.6 (`schemaType: "cdqcydx"`) |

### Example

```bash
curl -X POST "$BASE_URL/validateAndConvert" \
  -F 'file=@./sbom.json;type=application/json' \
  -F 'postData={"schemaType":"cdqcydx"}'
```

### Response

Returns a JSON object based on the converted `BomFilesInputModel`. On success, the converted document is returned as `sbomJson` (a JSON object), with conversion information such as `lossEvent`, `conversionDeltas`, `schemaType`, `sbomFileName`, `valid`, and `message` when present. The controller removes `schema`, `filePath`, `validatedAlready`, `status`, `schemaJsonString`, and `sbomJsonString`. In particular, the converted SBOM is not returned as a string property.

Example success response (illustrative):

```json
{
  "schemaType": "cdqspdx2.3",
  "sbomFileName": "convertedSbomContent.json",
  "sbomJson": {
    "spdxVersion": "SPDX-2.3"
  },
  "valid": true,
  "errorDetails": [],
  "lossEvent": [],
  "conversionDeltas": [],
  "message": "Convert operation is successful and converted SBOM available in sbomJson property of the response. Refer lossEvent,conversionDeltas,errorDetails properties for more details"
}
```

The `sbomJson` body above is abbreviated. If input validation or conversion fails, the response message indicates failure; validation errors from the source document are not consistently propagated into the returned converted model by the current service implementation.


## Error and Status Behavior

`/health` explicitly returns HTTP `200 OK`. The three upload/conversion handlers return JSON objects rather than setting an HTTP status code for application-level validation failures, so clients should inspect `message` and, when present, `valid` and `errorDetails` instead of relying only on the HTTP status. Errors detected before processing generally have a `message` and may echo supplied `schemaType`, `schemaVersion`, `sessionId`, `index`, or `sbomFileName`. Framework-level failures such as malformed multipart requests or request-size limits may still produce HTTP error statuses.
