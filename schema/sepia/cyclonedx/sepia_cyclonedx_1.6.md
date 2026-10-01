## CycloneDX_1.6

Required parameters declared by `bosch_sepia_cyclonedx_1.6.schema.json`.

| Root Element | Child Element |                    |           |           |
|:-------------|:--------------|:-------------------|:----------|:----------|
| bomFormat    |               |                    |           |           |
| specVersion  |               |                    |           |           |
| serialNumber |               |                    |           |           |
| metadata     | component     | type               |           |           |
|              |              | name                |           |           |
|              |              | version             |           |           |
|              |              | group               |           |           |
|              |              | purl                |           |           |
|              | supplier     | name                |           |           |
|              | tools        | components          |           |           |
|              | timestamp    |                    |           |           |
|              | authors OR manufacturer | name       |           |           |
| components   | name          |                    |           |           |
|              | version       |                    |           |           |
|              | purl          |                    |           |           |
|              | declarations | affirmation         | signatories |         |
|              |              |                    | signature  |           |
|              |              |                    | externalReference |    |
|              |              |                    | organization |         |
|              | dataGovernance | organization OR contact |       |       |
|              | inputType    | resource OR parameters OR environmentVars OR data | | |
|              | outputType   | resource OR environmentVars OR data |           |           |
|              | resourceReferenceChoice | ref OR externalReference |       |       |
|              | annotator    | organization OR individual OR component OR service | | |
|              | vulnerability | version OR range      |           |           |
