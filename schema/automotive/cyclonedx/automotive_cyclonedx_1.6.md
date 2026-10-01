## CycloneDX_1.6

Required parameters declared by `automotive_cyclonedx_1.6.schema.json`.

| Root Element | Child Element |                    |           |           |
|:-------------|:--------------|:-------------------|:----------|:----------|
| bomFormat    |               |                    |           |           |
| specVersion  |               |                    |           |           |
| metadata     |               |                    |           |           |
|              | authors      |                    |           |           |
|              |              | bom-ref             |           |           |
|              |              | name                |           |           |
|              |              | email               |           |           |
|              |              | phone               |           |           |
|              | timestamp    |                    |           |           |
|              | lifecycles   |                    |           |           |
|              | component    | name               |           |           |
|              |              | type                |           |           |
|              | supplier     | bom-ref             |           |           |
|              |              | name                |           |           |
| serialNumber |               |                    |           |           |
| version      |               |                    |           |           |
| components   | name          |                    |           |           |
|              | version       |                    |           |           |
|              | supplier      | bom-ref             |           |           |
|              |              | name                |           |           |
|              | cpe           |                    |           |           |
|              | purl          |                    |           |           |
|              | evidence      | identity           |           |           |
|              |              |                    | methods   |           |
|              | externalReferences |                 |           |           |
|              | licenses      | id                 |           |           |
|              |              | acknowledgement    |           |           |
|              | hashes        | alg                |           |           |
|              |              | content            |           |           |
|              | copyright     |                    |           |           |
