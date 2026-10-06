/* SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA

SPDX-License-Identifier: MIT */
export class CDQCycloneDXSBOMStandard {
  bomFormat!: string;
  specVersion!: string;
  serialNumber!: string;
  version!: number;
  metadata: CDQBOMMetadata = new CDQBOMMetadata();
  components: CDQComponentObject[] = [];
  services?: CDQServiceObject[];
  externalReferences?: CDQExternalReference[];
  dependencies?: CDQDependency[];
  compositions?: CDQCompositions[];
  vulnerabilities?: CDQVulnerability[];
  annotations?: CDQAnnotation[];
  formulation?: CDQFormula[];
  declarations?: CDQDeclarations;
  definitions?: CDQDefinitions;
  properties?: CDQLightweightNameValuePair[];
  signature?: CDQSignature;
}

// ==================== Signature ====================

export class CDQSignature {
  signers!: CDQSignatureData[];
  chain!: CDQSignatureData[];
  algorithm!: CDQSignatureAlgorithm;
  keyId!: string;
  publicKey!: CDQPublicKey;
  certificatePath!: string[];
  excludes!: string[];
  value!: string;
}

export class CDQSignatureData {
  algorithm!: CDQSignatureAlgorithm;
  keyId!: string;
  publicKey!: CDQPublicKey;
  certificatePath!: string[];
  excludes!: string[];
  value!: string;
}

export class CDQPublicKey {
  kty!: CDQKty;
  crv!: CDQCrv;
  x!: string;
  y?: string;
  n?: string;
  e?: string;
}

export type CDQCrv = "P-256" | "P-384" | "P-521" | "Ed25519" | "Ed448";
export type CDQKty = "EC" | "OKP" | "RSA";
export type CDQSignatureAlgorithm =
  | "RS256"
  | "RS384"
  | "RS512"
  | "PS256"
  | "PS384"
  | "PS512"
  | "ES256"
  | "ES384"
  | "ES512"
  | "Ed25519"
  | "Ed448"
  | "HS256"
  | "HS384"
  | "HS512";

// ==================== Metadata ====================

export class CDQBOMMetadata {
  timestamp!: string;
  lifecycles?: CDQLifecycle[];
  tools?: CDQToolsChoice;
  authors: CDQOrganizationalContactObject[] = [];
  manufacturer: CDQOrganizationalEntityObject = new CDQOrganizationalEntityObject();
  component: CDQMetadataComponent = new CDQMetadataComponent();
  manufacture?: CDQManufacture;
  supplier: CDQOrganizationalEntityObject = new CDQOrganizationalEntityObject();
  licenses: CDQLicenses[] = [];
  properties?: CDQLightweightNameValuePair[];
}

export class CDQToolsChoice {
  components?: CDQComponentObject[];
  services?: CDQServiceObject[];
}

export class CDQLifecycle {
  phase?: CDQLifecyclePhase;
  name?: string;
  description?: string;
}

export type CDQLifecyclePhase =
  | "design"
  | "pre-build"
  | "build"
  | "post-build"
  | "operations"
  | "discovery"
  | "decommission";

// ==================== Component ====================

export class CDQComponentObject {
  type!: CDQComponentType;
  "mime-type"?: string;
  "bom-ref"?: string;
  supplier?: CDQOrganizationalEntityObject;
  manufacturer?: CDQOrganizationalEntityObject;
  authors?: CDQOrganizationalContactObject[];
  author?: string;
  publisher?: string;
  group?: string;
  name!: string;
  version!: string;
  description?: string;
  scope?: CDQComponentScope;
  hashes!: CDQHashObjects[];
  licenses!: CDQLicenses[];
  copyright!: string;
  cpe!: string;
  purl!: string;
  omniborId?: string[];
  swhid?: string[];
  swid?: CDQSWIDTag;
  modified?: boolean;
  pedigree?: CDQComponentPedigree;
  externalReferences?: CDQExternalReference[];
  components?: CDQComponentObject[];
  evidence?: CDQComponentEvidence;
  releaseNotes?: CDQReleaseNotes;
  modelCard?: CDQModelCard;
  data?: CDQComponentData[];
  cryptoProperties?: CDQCryptoProperties;
  properties?: CDQLightweightNameValuePair[];
  tags?: string[];
  signature?: CDQSignature;
}

export class CDQMetadataComponent {
  type!:
    | null
    | "application"
    | "framework"
    | "library"
    | "container"
    | "platform"
    | "operating-system"
    | "device"
    | "device-driver"
    | "firmware"
    | "file"
    | "machine-learning-model"
    | "data"
    | "cryptographic-asset";
  "mime-type"?: string;
  "bom-ref"?: string;
  supplier?: CDQOrganizationalEntityObject;
  manufacturer?: CDQOrganizationalEntityObject;
  authors?: CDQOrganizationalContactObject[];
  author?: string;
  publisher?: string;
  group?: string;
  name!: string;
  version!: string;
  description?: string;
  scope?: CDQComponentScope;
  hashes!: CDQHashObjects[];
  licenses!: CDQLicenses[];
  copyright!: string;
  cpe!: string;
  purl!: string;
  omniborId?: string[];
  swhid?: string[];
  swid?: CDQSWIDTag;
  modified?: boolean;
  pedigree?: CDQComponentPedigree;
  externalReferences!: CDQExternalReference[];
  components?: CDQComponentObject[];
  evidence?: CDQComponentEvidence;
  releaseNotes?: CDQReleaseNotes;
  modelCard?: CDQModelCard;
  data?: CDQComponentData[];
  cryptoProperties?: CDQCryptoProperties;
  properties?: CDQLightweightNameValuePair[];
  tags?: string[];
  signature?: CDQSignature;
}

// ==================== Service ====================

export class CDQServiceObject {
  "bom-ref"?: string;
  provider?: CDQOrganizationalEntityObject;
  group?: string;
  name?: string;
  version?: string;
  description?: string;
  endpoints?: string[];
  authenticated?: boolean;
  "x-trust-boundary"?: boolean;
  trustZone?: string;
  data?: CDQServiceData[];
  licenses?: CDQLicenses[];
  externalReferences?: CDQExternalReference[];
  services?: CDQServiceObject[];
  releaseNotes?: CDQReleaseNotes;
  properties?: CDQLightweightNameValuePair[];
  tags?: string[];
  signature?: CDQSignature;
}

// ==================== Compositions ====================

export class CDQCompositions {
  "bom-ref"?: string;
  aggregate?: CDQAggregate;
  assemblies?: string[];
  dependencies?: string[];
  vulnerabilities?: string[];
  signature?: CDQSignature;
}

export type CDQAggregate =
  | "complete"
  | "incomplete"
  | "incomplete_first_party_only"
  | "incomplete_first_party_proprietary_only"
  | "incomplete_first_party_opensource_only"
  | "incomplete_third_party_only"
  | "incomplete_third_party_proprietary_only"
  | "incomplete_third_party_opensource_only"
  | "unknown"
  | "not_specified";

// ==================== Vulnerability ====================

export class CDQVulnerabilityReference {
  id!: string;
  source!: CDQVulnerabilitySource;
}

export class CDQRating {
  source?: CDQVulnerabilitySource;
  score?: number;
  severity?: CDQSeverity;
  method?: CDQScoreMethod;
  vector?: string;
  justification?: string;
}

export type CDQSeverity = "critical" | "high" | "medium" | "low" | "info" | "none" | "unknown";
export type CDQScoreMethod = "CVSSv2" | "CVSSv3" | "CVSSv31" | "CVSSv4" | "OWASP" | "SSVC" | "other";

export class CDQVulnerability {
  "bom-ref"?: string;
  id?: string;
  source?: CDQVulnerabilitySource;
  references?: CDQVulnerabilityReference[];
  ratings?: CDQRating[];
  cwes?: number[];
  description?: string;
  detail?: string;
  recommendation?: string;
  workaround?: string;
  proofOfConcept?: CDQProofOfConcept;
  advisories?: CDQAdvisory[];
  created?: string;
  published?: string;
  updated?: string;
  rejected?: string;
  credits?: CDQCredits;
  tools?: CDQTool[] | CDQToolsChoice;
  analysis?: CDQImpactAnalysis;
  affects?: CDQAffects[];
  properties?: CDQLightweightNameValuePair[];
}

export class CDQProofOfConcept {
  reproductionSteps?: string;
  environment?: string;
  supportingMaterial?: CDQAttachmentText[];
}

export class CDQAffects {
  ref!: string;
  versions?: CDQVersions[];
}

export class CDQVersions {
  version?: string;
  range?: string;
  status?: CDQAffectedStatus;
}

export type CDQAffectedStatus = "affected" | "unaffected" | "unknown";

export class CDQAdvisory {
  title?: string;
  url?: string;
}

export class CDQImpactAnalysis {
  state?: CDQImpactAnalysisState;
  justification?: CDQImpactAnalysisJustification;
  response?: CDQResponse;
  detail?: string;
  firstIssued?: string;
  lastUpdated?: string;
}

export type CDQImpactAnalysisState =
  | "resolved"
  | "resolved_with_pedigree"
  | "exploitable"
  | "in_triage"
  | "false_positive"
  | "not_affected";

export type CDQImpactAnalysisJustification =
  | "code_not_present"
  | "code_not_reachable"
  | "requires_configuration"
  | "requires_dependency"
  | "requires_environment"
  | "protected_by_compiler"
  | "protected_at_runtime"
  | "protected_at_perimeter"
  | "protected_by_mitigating_control";

export type CDQResponse = ("can_not_fix" | "will_not_fix" | "update" | "rollback" | "workaround_available")[];

export class CDQCredits {
  organizations?: CDQOrganizationalEntityObject[];
  individuals?: CDQOrganizationalContactObject[];
}

export class CDQVulnerabilitySource {
  name?: string;
  url?: string;
}

// ==================== Dependency ====================

export class CDQDependency {
  ref?: string;
  dependsOn?: string[];
  provides?: string[];
}

// ==================== Service Data ====================

export class CDQServiceData {
  flow?: CDQDirectionalFlow;
  classification?: string;
  name?: string;
  description?: string;
  governance?: CDQDataGovernance;
  source?: string[];
  destination?: string[];
}

export type CDQDirectionalFlow = "inbound" | "outbound" | "bi-directional" | "unknown";

// ==================== Organizational ====================

export class CDQOrganizationalEntityObject {
  "bom-ref"?: string;
  name!: string;
  address?: CDQPostalAddress;
  url?: string[];
  contact: CDQOrganizationalContactObject[] = [];
}

export class CDQOrganizationalContactObject {
  "bom-ref"?: string;
  name!: string;
  email!: string;
  phone!: string;
}

export class CDQPostalAddress {
  "bom-ref"?: string;
  country?: string;
  region?: string;
  locality?: string;
  postOfficeBoxNumber?: string;
  postalCode?: string;
  streetAddress?: string;
}

// ==================== Component Scope & Type ====================

export type CDQComponentScope = "required" | "optional" | "excluded";

export enum CDQComponentType {
  //null = "null",
  application = "application",
  framework = "framework",
  library = "library",
  container = "container",
  platform = "platform",
  "operating-system" = "operating-system",
  device = "device",
  "device-driver" = "device-driver",
  firmware = "firmware",
  file = "file",
  "machine-learning-model" = "machine-learning-model",
  data = "data",
  "cryptographic-asset" = "cryptographic-asset"
}


// ==================== Release Notes ====================

export class CDQReleaseNotes {
  type?: string;
  title?: string;
  featuredImage?: string;
  socialImage?: string;
  description?: string;
  timestamp?: string;
  aliases?: string[];
  tags?: string[];
  resolves?: CDQIssue[];
  notes?: CDQNote[];
  properties?: CDQLightweightNameValuePair[];
}

export class CDQNote {
  locale?: string;
  text?: CDQAttachmentText;
}

export class CDQAttachmentText {
  contentType?: string;
  encoding?: "base64";
  content?: string;
}

// ==================== Evidence ====================

export class CDQComponentEvidence {
  identity?: CDQComponentIdentityEvidence[] | CDQComponentIdentityEvidence;
  occurrences?: CDQOccurrence[];
  callstack?: CDQCallstack;
  licenses?: CDQLicenses[];
  copyright?: CDQCopyright[];
}

export class CDQComponentIdentityEvidence {
  field?: CDQIdentityField;
  confidence?: number;
  concludedValue?: string;
  methods?: CDQIdentityMethod[];
  tools?: string[];
}

export type CDQIdentityField = "group" | "name" | "version" | "purl" | "cpe" | "omniborId" | "swhid" | "swid" | "hash";

export class CDQIdentityMethod {
  technique?: CDQIdentityTechnique;
  confidence?: number;
  value?: string;
}

export type CDQIdentityTechnique =
  | "source-code-analysis"
  | "binary-analysis"
  | "manifest-analysis"
  | "ast-fingerprint"
  | "hash-comparison"
  | "instrumentation"
  | "dynamic-analysis"
  | "filename"
  | "attestation"
  | "other";

export class CDQOccurrence {
  "bom-ref"?: string;
  location?: string;
  line?: number;
  offset?: number;
  symbol?: string;
  additionalContext?: string;
}

export class CDQCallstack {
  frames?: CDQFrame[];
}

export class CDQFrame {
  package?: string;
  module?: string;
  function?: string;
  parameters?: string[];
  line?: number;
  column?: number;
  fullFilename?: string;
}

export class CDQCopyright {
  text?: string;
}

// ==================== SWID Tag ====================

export class CDQSWIDTag {
  tagId?: string;
  name?: string;
  version?: string;
  tagVersion?: number;
  patch?: boolean;
  text?: CDQAttachmentText;
  url?: string;
}

// ==================== Pedigree ====================

export class CDQComponentPedigree {
  ancestors?: CDQComponentObject[];
  descendants?: CDQComponentObject[];
  variants?: CDQComponentObject[];
  commits?: CDQCommit[];
  patches?: CDQPatch[];
  notes?: string;
}

export class CDQPatch {
  type?: CDQPatchType;
  diff?: CDQDiff;
  resolves?: CDQIssue[];
}

export class CDQIssue {
  type?: CDQIssueType;
  id?: string;
  name?: string;
  description?: string;
  source?: CDQIssueSource;
  references?: string[];
}

export type CDQIssueType = "defect" | "enhancement" | "security";

export class CDQIssueSource {
  name?: string;
  url?: string;
}

export class CDQDiff {
  text?: CDQAttachmentText;
  url?: string;
}

export type CDQPatchType = "unofficial" | "monkey" | "backport" | "cherry-pick";

export class CDQCommit {
  uid?: string;
  url?: string;
  author?: CDQIdentifiableAction;
  committer?: CDQIdentifiableAction;
  message?: string;
}

export class CDQIdentifiableAction {
  timestamp?: string;
  name?: string;
  email?: string;
}

// ==================== Lightweight Name-Value Pair ====================

export class CDQLightweightNameValuePair {
  name?: string;
  value?: string;
  [k: string]: unknown;
}

// ==================== Licenses ====================

export class CDQLicenses {
  license?: CDQLicense;
  expression?: string;
}

export class CDQLicense {
  id!: CDQLicenseIDs;
  name!: string;
  text?: CDQAttachmentText;
  url?: string;
  acknowledgement?: CDQLicenseAcknowledgement;
  licensing?: CDQLicensing;
  "bom-ref"?: string;
  properties?: CDQLightweightNameValuePair[];
}

export type CDQLicenseAcknowledgement = "declared" | "concluded";

export class CDQLicensing {
  altIds?: string[];
  licensor?: CDQLicenseParty;
  licensee?: CDQLicenseParty;
  purchaser?: CDQLicenseParty;
  purchaseOrder?: string;
  licenseTypes?: CDQLicenseType[];
  lastRenewal?: string;
  expiration?: string;
}

export class CDQLicenseParty {
  organization?: CDQOrganizationalEntityObject;
  individual?: CDQOrganizationalContactObject;
}

export type CDQLicenseType =
  | "academic"
  | "appliance"
  | "client-access"
  | "concurrent-user"
  | "core-points"
  | "custom-metric"
  | "device"
  | "evaluation"
  | "named-user"
  | "node-locked"
  | "oem"
  | "perpetual"
  | "processor-points"
  | "subscription"
  | "user"
  | "other";

// ==================== Supplier / Manufacture ====================

export class CDQSupplier {
  "bom-ref"?: string;
  name!: string;
  address?: CDQPostalAddress;
  url: string[] = [];
  contact: CDQOrganizationalContactObject[] = [];
}

export class CDQManufacture {
  "bom-ref"?: string;
  name?: string;
  address?: CDQPostalAddress;
  url?: string[];
  contact: CDQOrganizationalContactObject[] = [];
}

// ==================== Tool ====================

export class CDQTool {
  vendor!: string;
  name!: string;
  version!: string;
  hashes: CDQHashObjects[] = [];
  externalReferences: CDQExternalReference[] = [];
}

// ==================== External Reference ====================

export class CDQExternalReference {
  url?: string;
  comment?: string;
  type?: CDQExternalReferenceType;
  hashes?: CDQHashObjects[];
}

export type CDQExternalReferenceType =
  | "vcs"
  | "issue-tracker"
  | "website"
  | "advisories"
  | "bom"
  | "mailing-list"
  | "social"
  | "chat"
  | "documentation"
  | "support"
  | "source-distribution"
  | "distribution"
  | "distribution-intake"
  | "license"
  | "build-meta"
  | "build-system"
  | "release-notes"
  | "security-contact"
  | "model-card"
  | "log"
  | "configuration"
  | "evidence"
  | "formulation"
  | "attestation"
  | "threat-model"
  | "adversary-model"
  | "risk-assessment"
  | "vulnerability-assertion"
  | "exploitability-statement"
  | "pentest-report"
  | "static-analysis-report"
  | "dynamic-analysis-report"
  | "runtime-analysis-report"
  | "component-analysis-report"
  | "maturity-report"
  | "certification-report"
  | "codified-infrastructure"
  | "quality-metrics"
  | "poam"
  | "electronic-signature"
  | "digital-signature"
  | "rfc-9116"
  | "other";

// ==================== Hash ====================

export class CDQHashObjects {
  alg!: CDQHashAlgorithm;
  content!: string;
}

export type CDQHashAlgorithm =
  | null
  | "MD5"
  | "SHA-1"
  | "SHA-256"
  | "SHA-384"
  | "SHA-512"
  | "SHA3-256"
  | "SHA3-384"
  | "SHA3-512"
  | "BLAKE2b-256"
  | "BLAKE2b-384"
  | "BLAKE2b-512"
  | "BLAKE3";

// ==================== Annotations ====================

export class CDQAnnotation {
  "bom-ref"?: string;
  subjects?: string[];
  annotator?: CDQAnnotator;
  timestamp?: string;
  text?: string;
  signature?: CDQSignature;
}

export class CDQAnnotator {
  organization?: CDQOrganizationalEntityObject;
  individual?: CDQOrganizationalContactObject;
  component?: CDQComponentObject;
  service?: CDQServiceObject;
}

// ==================== Formulation ====================

export class CDQFormula {
  "bom-ref"?: string;
  components?: CDQComponentObject[];
  services?: CDQServiceObject[];
  workflows?: CDQWorkflow[];
  properties?: CDQLightweightNameValuePair[];
}

export class CDQWorkflow {
  "bom-ref"?: string;
  uid?: string;
  name?: string;
  description?: string;
  resourceReferences?: CDQResourceReferenceChoice[];
  tasks?: CDQTask[];
  taskDependencies?: CDQDependency[];
  taskTypes?: CDQTaskType[];
  trigger?: CDQTrigger;
  steps?: CDQStep[];
  inputs?: CDQInputType[];
  outputs?: CDQOutputType[];
  timeStart?: string;
  timeEnd?: string;
  workspaces?: CDQWorkspace[];
  runtimeTopology?: CDQDependency[];
  properties?: CDQLightweightNameValuePair[];
}

export class CDQTask {
  "bom-ref"?: string;
  uid?: string;
  name?: string;
  description?: string;
  resourceReferences?: CDQResourceReferenceChoice[];
  taskTypes?: CDQTaskType[];
  trigger?: CDQTrigger;
  steps?: CDQStep[];
  inputs?: CDQInputType[];
  outputs?: CDQOutputType[];
  timeStart?: string;
  timeEnd?: string;
  workspaces?: CDQWorkspace[];
  runtimeTopology?: CDQDependency[];
  properties?: CDQLightweightNameValuePair[];
}

export class CDQStep {
  name?: string;
  description?: string;
  commands?: CDQCommand[];
  properties?: CDQLightweightNameValuePair[];
}

export class CDQCommand {
  executed?: string;
  properties?: CDQLightweightNameValuePair[];
}

export class CDQWorkspace {
  "bom-ref"?: string;
  uid?: string;
  name?: string;
  aliases?: string[];
  description?: string;
  resourceReferences?: CDQResourceReferenceChoice[];
  accessMode?: CDQAccessMode;
  mountPath?: string;
  managedDataType?: string;
  volumeRequest?: string;
  volume?: CDQVolume;
  properties?: CDQLightweightNameValuePair[];
}

export type CDQAccessMode = "read-only" | "read-write" | "read-write-once" | "write-once" | "write-only";

export class CDQVolume {
  uid?: string;
  name?: string;
  mode?: CDQVolumeMode;
  path?: string;
  sizeAllocated?: string;
  persistent?: boolean;
  remote?: boolean;
  properties?: CDQLightweightNameValuePair[];
}

export type CDQVolumeMode = "filesystem" | "block";

export class CDQTrigger {
  "bom-ref"?: string;
  uid?: string;
  name?: string;
  description?: string;
  resourceReferences?: CDQResourceReferenceChoice[];
  type?: CDQTriggerType;
  event?: CDQEvent;
  conditions?: CDQCondition[];
  timeActivated?: string;
  inputs?: CDQInputType[];
  outputs?: CDQOutputType[];
  properties?: CDQLightweightNameValuePair[];
}

export type CDQTriggerType = "manual" | "api" | "webhook" | "scheduled";

export class CDQEvent {
  uid?: string;
  description?: string;
  timeReceived?: string;
  data?: CDQAttachmentText;
  source?: CDQResourceReferenceChoice;
  target?: CDQResourceReferenceChoice;
  properties?: CDQLightweightNameValuePair[];
}

export class CDQCondition {
  description?: string;
  expression?: string;
  properties?: CDQLightweightNameValuePair[];
}

export class CDQInputType {
  source?: string;
  target?: string;
  resource?: CDQResourceReferenceChoice;
  parameters?: CDQParameter[];
  environmentVars?: CDQLightweightNameValuePair[];
  data?: CDQAttachmentText;
  properties?: CDQLightweightNameValuePair[];
}

export class CDQOutputType {
  type?: CDQOutputTypeEnum;
  source?: string;
  target?: string;
  resource?: CDQResourceReferenceChoice;
  data?: CDQAttachmentText;
  environmentVars?: CDQLightweightNameValuePair[];
  properties?: CDQLightweightNameValuePair[];
}

export type CDQOutputTypeEnum = "artifact" | "attestation" | "log" | "evidence" | "metrics" | "other";

export class CDQResourceReferenceChoice {
  ref?: string;
  externalReference?: CDQExternalReference;
}

export class CDQParameter {
  name?: string;
  value?: string;
  dataType?: string;
}

export type CDQTaskType =
  | "copy"
  | "clone"
  | "lint"
  | "scan"
  | "merge"
  | "build"
  | "test"
  | "deliver"
  | "deploy"
  | "release"
  | "clean"
  | "other";

// ==================== Declarations ====================

export class CDQDeclarations {
  assessors?: CDQAssessor[];
  attestations?: CDQAttestation[];
  claims?: CDQClaim[];
  evidence?: CDQDeclarationEvidence[];
  targets?: CDQDeclarationTargets;
  affirmation?: CDQAffirmation;
  signature?: CDQSignature;
}

export class CDQAssessor {
  "bom-ref"?: string;
  thirdParty?: boolean;
  organization?: CDQOrganizationalEntityObject;
}

export class CDQAttestation {
  summary?: string;
  assessor?: string;
  map?: CDQAttestationMap[];
  signature?: CDQSignature;
}

export class CDQAttestationMap {
  requirement?: string;
  claims?: string[];
  counterClaims?: string[];
  conformance?: CDQConformance;
  confidence?: CDQConfidence;
}

export class CDQConformance {
  score?: number;
  rationale?: string;
  mitigationStrategies?: string[];
}

export class CDQConfidence {
  score?: number;
  rationale?: string;
}

export class CDQClaim {
  "bom-ref"?: string;
  target?: string;
  predicate?: string;
  mitigationStrategies?: string[];
  reasoning?: string;
  evidence?: string[];
  counterEvidence?: string[];
  externalReferences?: CDQExternalReference[];
  signature?: CDQSignature;
}

export class CDQDeclarationEvidence {
  "bom-ref"?: string;
  propertyName?: string;
  description?: string;
  data?: CDQDeclarationEvidenceData[];
  created?: string;
  expires?: string;
  author?: CDQOrganizationalContactObject;
  reviewer?: CDQOrganizationalContactObject;
  signature?: CDQSignature;
}

export class CDQDeclarationEvidenceData {
  name?: string;
  contents?: CDQDeclarationEvidenceDataContents;
  classification?: string;
  sensitiveData?: string[];
  governance?: CDQDataGovernance;
}

export class CDQDeclarationEvidenceDataContents {
  attachment?: CDQAttachmentText;
  url?: string;
}

export class CDQDeclarationTargets {
  organizations?: CDQOrganizationalEntityObject[];
  components?: CDQComponentObject[];
  services?: CDQServiceObject[];
}

export class CDQAffirmation {
  statement?: string;
  signatories?: CDQSignatory[];
  signature?: CDQSignature;
}

export class CDQSignatory {
  name?: string;
  role?: string;
  signature?: CDQSignature;
  organization?: CDQOrganizationalEntityObject;
  externalReference?: CDQExternalReference;
}

// ==================== Definitions ====================

export class CDQDefinitions {
  standards?: CDQStandard[];
}

export class CDQStandard {
  "bom-ref"?: string;
  name?: string;
  version?: string;
  description?: string;
  owner?: string;
  requirements?: CDQRequirement[];
  levels?: CDQLevel[];
  externalReferences?: CDQExternalReference[];
  signature?: CDQSignature;
}

export class CDQRequirement {
  "bom-ref"?: string;
  identifier?: string;
  title?: string;
  text?: string;
  descriptions?: string[];
  openCre?: string[];
  parent?: string;
  properties?: CDQLightweightNameValuePair[];
  externalReferences?: CDQExternalReference[];
}

export class CDQLevel {
  "bom-ref"?: string;
  identifier?: string;
  title?: string;
  description?: string;
  requirements?: string[];
}

// ==================== Model Card ====================

export class CDQModelCard {
  "bom-ref"?: string;
  modelParameters?: CDQModelParameters;
  quantitativeAnalysis?: CDQQuantitativeAnalysis;
  considerations?: CDQConsiderations;
  properties?: CDQLightweightNameValuePair[];
}

export class CDQModelParameters {
  approach?: CDQApproach;
  task?: string;
  architectureFamily?: string;
  modelArchitecture?: string;
  datasets?: CDQComponentData[];
  inputs?: CDQInputOutputMLParameters[];
  outputs?: CDQInputOutputMLParameters[];
}

export class CDQApproach {
  type?: CDQLearningType;
}

export type CDQLearningType =
  | "supervised"
  | "unsupervised"
  | "reinforcement-learning"
  | "semi-supervised"
  | "self-supervised";

export class CDQInputOutputMLParameters {
  format?: string;
}

export class CDQQuantitativeAnalysis {
  performanceMetrics?: CDQPerformanceMetric[];
  graphics?: CDQGraphicsCollection;
}

export class CDQPerformanceMetric {
  type?: string;
  value?: string;
  slice?: string;
  confidenceInterval?: CDQConfidenceInterval;
}

export class CDQConfidenceInterval {
  lowerBound?: string;
  upperBound?: string;
}

export class CDQGraphicsCollection {
  description?: string;
  collection?: CDQGraphic[];
}

export class CDQGraphic {
  name?: string;
  image?: CDQAttachmentText;
}

export class CDQConsiderations {
  users?: string[];
  useCases?: string[];
  technicalLimitations?: string[];
  performanceTradeoffs?: string[];
  ethicalConsiderations?: CDQRisk[];
  environmentalConsiderations?: CDQEnvironmentalConsiderations;
  fairnessAssessments?: CDQFairnessAssessment[];
}

export class CDQRisk {
  name?: string;
  mitigationStrategy?: string;
}

export class CDQFairnessAssessment {
  groupAtRisk?: string;
  benefits?: string;
  harms?: string;
  mitigationStrategy?: string;
}

export class CDQEnvironmentalConsiderations {
  energyConsumptions?: CDQEnergyConsumption[];
  properties?: CDQLightweightNameValuePair[];
}

export class CDQEnergyConsumption {
  activity?: CDQEnergyActivity;
  energyProviders?: CDQEnergyProvider[];
  activityEnergyCost?: CDQEnergyMeasure;
  co2CostEquivalent?: CDQCO2Measure;
  co2CostOffset?: CDQCO2Measure;
  properties?: CDQLightweightNameValuePair[];
}

export type CDQEnergyActivity =
  | "design"
  | "data-collection"
  | "data-preparation"
  | "training"
  | "fine-tuning"
  | "validation"
  | "deployment"
  | "inference"
  | "other";

export class CDQEnergyProvider {
  "bom-ref"?: string;
  description?: string;
  organization?: CDQOrganizationalEntityObject;
  energySource?: CDQEnergySource;
  energyProvided?: CDQEnergyMeasure;
  externalReferences?: CDQExternalReference[];
}

export type CDQEnergySource =
  | "coal"
  | "oil"
  | "natural-gas"
  | "nuclear"
  | "wind"
  | "solar"
  | "geothermal"
  | "hydropower"
  | "biofuel"
  | "unknown"
  | "other";

export class CDQEnergyMeasure {
  value?: number;
  unit?: "kWh";
}

export class CDQCO2Measure {
  value?: number;
  unit?: "tCO2eq";
}

// ==================== Component Data ====================

export class CDQComponentData {
  "bom-ref"?: string;
  type?: CDQComponentDataType;
  name?: string;
  contents?: CDQComponentDataContents;
  classification?: string;
  sensitiveData?: string[];
  graphics?: CDQGraphicsCollection;
  description?: string;
  governance?: CDQDataGovernance;
}

export type CDQComponentDataType = "source-code" | "configuration" | "dataset" | "definition" | "other";

export class CDQComponentDataContents {
  attachment?: CDQAttachmentText;
  url?: string;
  properties?: CDQLightweightNameValuePair[];
}

// ==================== Data Governance ====================

export class CDQDataGovernance {
  custodians?: CDQDataGovernanceResponsibleParty[];
  stewards?: CDQDataGovernanceResponsibleParty[];
  owners?: CDQDataGovernanceResponsibleParty[];
}

export class CDQDataGovernanceResponsibleParty {
  organization?: CDQOrganizationalEntityObject;
  contact?: CDQOrganizationalContactObject;
}

// ==================== Crypto Properties ====================

export class CDQCryptoProperties {
  assetType?: CDQCryptoAssetType;
  algorithmProperties?: CDQAlgorithmProperties;
  certificateProperties?: CDQCertificateProperties;
  relatedCryptoMaterialProperties?: CDQRelatedCryptoMaterialProperties;
  protocolProperties?: CDQProtocolProperties;
  oid?: string;
}

export type CDQCryptoAssetType = "algorithm" | "certificate" | "protocol" | "related-crypto-material";

export class CDQAlgorithmProperties {
  primitive?: CDQCryptoPrimitive;
  parameterSetIdentifier?: string;
  curve?: string;
  executionEnvironment?: CDQExecutionEnvironment;
  implementationPlatform?: CDQImplementationPlatform;
  certificationLevel?: CDQCertificationLevel[];
  mode?: CDQCryptoMode;
  padding?: CDQCryptoPadding;
  cryptoFunctions?: CDQCryptoFunction[];
  classicalSecurityLevel?: number;
  nistQuantumSecurityLevel?: number;
}

export type CDQCryptoPrimitive =
  | "drbg"
  | "mac"
  | "block-cipher"
  | "stream-cipher"
  | "signature"
  | "hash"
  | "pke"
  | "xof"
  | "kdf"
  | "key-agree"
  | "kem"
  | "ae"
  | "combiner"
  | "other"
  | "unknown";

export type CDQExecutionEnvironment =
  | "software-plain-ram"
  | "software-encrypted-ram"
  | "software-tee"
  | "hardware"
  | "other"
  | "unknown";

export type CDQImplementationPlatform =
  | "generic"
  | "x86_32"
  | "x86_64"
  | "armv7-a"
  | "armv7-m"
  | "armv8-a"
  | "armv8-m"
  | "armv9-a"
  | "armv9-m"
  | "s390x"
  | "ppc64"
  | "ppc64le"
  | "other"
  | "unknown";

export type CDQCertificationLevel =
  | "none"
  | "fips140-1"
  | "fips140-2"
  | "fips140-3"
  | "cc-eal1"
  | "cc-eal1+"
  | "cc-eal2"
  | "cc-eal2+"
  | "cc-eal3"
  | "cc-eal3+"
  | "cc-eal4"
  | "cc-eal4+"
  | "cc-eal5"
  | "cc-eal5+"
  | "cc-eal6"
  | "cc-eal6+"
  | "cc-eal7"
  | "cc-eal7+"
  | "other"
  | "unknown";

export type CDQCryptoMode =
  | "cbc"
  | "ecb"
  | "ccm"
  | "gcm"
  | "cfb"
  | "ofb"
  | "ctr"
  | "other"
  | "unknown";

export type CDQCryptoPadding =
  | "pkcs5"
  | "pkcs7"
  | "pkcs1v15"
  | "oaep"
  | "raw"
  | "other"
  | "unknown";

export type CDQCryptoFunction =
  | "generate"
  | "keygen"
  | "encrypt"
  | "decrypt"
  | "digest"
  | "tag"
  | "keyderive"
  | "sign"
  | "verify"
  | "encapsulate"
  | "decapsulate"
  | "other"
  | "unknown";

export class CDQCertificateProperties {
  subjectName?: string;
  issuerName?: string;
  notValidBefore?: string;
  notValidAfter?: string;
  signatureAlgorithmRef?: string;
  subjectPublicKeyRef?: string;
  certificateFormat?: string;
  certificateExtension?: string;
}

export class CDQRelatedCryptoMaterialProperties {
  type?: CDQRelatedCryptoMaterialType;
  id?: string;
  state?: CDQCryptoMaterialState;
  algorithmRef?: string;
  creationDate?: string;
  activationDate?: string;
  updateDate?: string;
  expirationDate?: string;
  value?: string;
  size?: number;
  format?: string;
  securedBy?: CDQSecuredBy;
}

export type CDQRelatedCryptoMaterialType =
  | "private-key"
  | "public-key"
  | "secret-key"
  | "key"
  | "ciphertext"
  | "signature"
  | "digest"
  | "initialization-vector"
  | "nonce"
  | "seed"
  | "salt"
  | "shared-secret"
  | "tag"
  | "additional-data"
  | "password"
  | "credential"
  | "token"
  | "other"
  | "unknown";

export type CDQCryptoMaterialState =
  | "pre-activation"
  | "active"
  | "suspended"
  | "deactivated"
  | "compromised"
  | "destroyed";

export class CDQSecuredBy {
  mechanism?: string;
  algorithmRef?: string;
}

export class CDQProtocolProperties {
  type?: CDQProtocolType;
  version?: string;
  cipherSuites?: CDQCipherSuite[];
  ikev2TransformTypes?: CDQIKEv2TransformTypes;
  cryptoRefArray?: string[];
}

export type CDQProtocolType =
  | "tls"
  | "ssh"
  | "ipsec"
  | "ike"
  | "sstp"
  | "wpa"
  | "other"
  | "unknown";

export class CDQCipherSuite {
  name?: string;
  algorithms?: string[];
  identifiers?: string[];
}

export class CDQIKEv2TransformTypes {
  encr?: string[];
  prf?: string[];
  integ?: string[];
  ke?: string[];
  esn?: boolean;
  auth?: string[];
}

// ==================== License IDs ====================

export enum CDQLicenseIDs {
  "0BSD" = "0BSD",
  "AAL" = "AAL",
  "Abstyles" = "Abstyles",
  "AdaCore-doc" = "AdaCore-doc",
  "Adobe-2006" = "Adobe-2006",
  "Adobe-Glyph" = "Adobe-Glyph",
  "ADSL" = "ADSL",
  "AFL-1.1" = "AFL-1.1",
  "AFL-1.2" = "AFL-1.2",
  "AFL-2.0" = "AFL-2.0",
  "AFL-2.1" = "AFL-2.1",
  "AFL-3.0" = "AFL-3.0",
  "Afmparse" = "Afmparse",
  "AGPL-1.0" = "AGPL-1.0",
  "AGPL-1.0-only" = "AGPL-1.0-only",
  "AGPL-1.0-or-later" = "AGPL-1.0-or-later",
  "AGPL-3.0" = "AGPL-3.0",
  "AGPL-3.0-only" = "AGPL-3.0-only",
  "AGPL-3.0-or-later" = "AGPL-3.0-or-later",
  "Aladdin" = "Aladdin",
  "AMDPLPA" = "AMDPLPA",
  "AML" = "AML",
  "AMPAS" = "AMPAS",
  "ANTLR-PD" = "ANTLR-PD",
  "ANTLR-PD-fallback" = "ANTLR-PD-fallback",
  "Apache-1.0" = "Apache-1.0",
  "Apache-1.1" = "Apache-1.1",
  "Apache-2.0" = "Apache-2.0",
  "APAFML" = "APAFML",
  "APL-1.0" = "APL-1.0",
  "App-s2p" = "App-s2p",
  "APSL-1.0" = "APSL-1.0",
  "APSL-1.1" = "APSL-1.1",
  "APSL-1.2" = "APSL-1.2",
  "APSL-2.0" = "APSL-2.0",
  "Arphic-1999" = "Arphic-1999",
  "Artistic-1.0" = "Artistic-1.0",
  "Artistic-1.0-cl8" = "Artistic-1.0-cl8",
  "Artistic-1.0-Perl" = "Artistic-1.0-Perl",
  "Artistic-2.0" = "Artistic-2.0",
  "ASWF-Digital-Assets-1.0" = "ASWF-Digital-Assets-1.0",
  "ASWF-Digital-Assets-1.1" = "ASWF-Digital-Assets-1.1",
  "Baekmuk" = "Baekmuk",
  "Bahyph" = "Bahyph",
  "Barr" = "Barr",
  "Beerware" = "Beerware",
  "Bitstream-Charter" = "Bitstream-Charter",
  "Bitstream-Vera" = "Bitstream-Vera",
  "BitTorrent-1.0" = "BitTorrent-1.0",
  "BitTorrent-1.1" = "BitTorrent-1.1",
  "blessing" = "blessing",
  "BlueOak-1.0.0" = "BlueOak-1.0.0",
  "Boehm-GC" = "Boehm-GC",
  "Borceux" = "Borceux",
  "Brian-Gladman-3-Clause" = "Brian-Gladman-3-Clause",
  "BSD-1-Clause" = "BSD-1-Clause",
  "BSD-2-Clause" = "BSD-2-Clause",
  "BSD-2-Clause-FreeBSD" = "BSD-2-Clause-FreeBSD",
  "BSD-2-Clause-NetBSD" = "BSD-2-Clause-NetBSD",
  "BSD-2-Clause-Patent" = "BSD-2-Clause-Patent",
  "BSD-2-Clause-Views" = "BSD-2-Clause-Views",
  "BSD-3-Clause" = "BSD-3-Clause",
  "BSD-3-Clause-Attribution" = "BSD-3-Clause-Attribution",
  "BSD-3-Clause-Clear" = "BSD-3-Clause-Clear",
  "BSD-3-Clause-LBNL" = "BSD-3-Clause-LBNL",
  "BSD-3-Clause-Modification" = "BSD-3-Clause-Modification",
  "BSD-3-Clause-No-Military-License" = "BSD-3-Clause-No-Military-License",
  "BSD-3-Clause-No-Nuclear-License" = "BSD-3-Clause-No-Nuclear-License",
  "BSD-3-Clause-No-Nuclear-License-2014" = "BSD-3-Clause-No-Nuclear-License-2014",
  "BSD-3-Clause-No-Nuclear-Warranty" = "BSD-3-Clause-No-Nuclear-Warranty",
  "BSD-3-Clause-Open-MPI" = "BSD-3-Clause-Open-MPI",
  "BSD-4-Clause" = "BSD-4-Clause",
  "BSD-4-Clause-Shortened" = "BSD-4-Clause-Shortened",
  "BSD-4-Clause-UC" = "BSD-4-Clause-UC",
  "BSD-4.3RENO" = "BSD-4.3RENO",
  "BSD-4.3TAHOE" = "BSD-4.3TAHOE",
  "BSD-Advertising-Acknowledgement" = "BSD-Advertising-Acknowledgement",
  "BSD-Attribution-HPND-disclaimer" = "BSD-Attribution-HPND-disclaimer",
  "BSD-Protection" = "BSD-Protection",
  "BSD-Source-Code" = "BSD-Source-Code",
  "BSL-1.0" = "BSL-1.0",
  "BUSL-1.1" = "BUSL-1.1",
  "bzip2-1.0.5" = "bzip2-1.0.5",
  "bzip2-1.0.6" = "bzip2-1.0.6",
  "C-UDA-1.0" = "C-UDA-1.0",
  "CAL-1.0" = "CAL-1.0",
  "CAL-1.0-Combined-Work-Exception" = "CAL-1.0-Combined-Work-Exception",
  "Caldera" = "Caldera",
  "CATOSL-1.1" = "CATOSL-1.1",
  "CC-BY-1.0" = "CC-BY-1.0",
  "CC-BY-2.0" = "CC-BY-2.0",
  "CC-BY-2.5" = "CC-BY-2.5",
  "CC-BY-2.5-AU" = "CC-BY-2.5-AU",
  "CC-BY-3.0" = "CC-BY-3.0",
  "CC-BY-3.0-AT" = "CC-BY-3.0-AT",
  "CC-BY-3.0-DE" = "CC-BY-3.0-DE",
  "CC-BY-3.0-IGO" = "CC-BY-3.0-IGO",
  "CC-BY-3.0-NL" = "CC-BY-3.0-NL",
  "CC-BY-3.0-US" = "CC-BY-3.0-US",
  "CC-BY-4.0" = "CC-BY-4.0",
  "CC-BY-NC-1.0" = "CC-BY-NC-1.0",
  "CC-BY-NC-2.0" = "CC-BY-NC-2.0",
  "CC-BY-NC-2.5" = "CC-BY-NC-2.5",
  "CC-BY-NC-3.0" = "CC-BY-NC-3.0",
  "CC-BY-NC-3.0-DE" = "CC-BY-NC-3.0-DE",
  "CC-BY-NC-4.0" = "CC-BY-NC-4.0",
  "CC-BY-NC-ND-1.0" = "CC-BY-NC-ND-1.0",
  "CC-BY-NC-ND-2.0" = "CC-BY-NC-ND-2.0",
  "CC-BY-NC-ND-2.5" = "CC-BY-NC-ND-2.5",
  "CC-BY-NC-ND-3.0" = "CC-BY-NC-ND-3.0",
  "CC-BY-NC-ND-3.0-DE" = "CC-BY-NC-ND-3.0-DE",
  "CC-BY-NC-ND-3.0-IGO" = "CC-BY-NC-ND-3.0-IGO",
  "CC-BY-NC-ND-4.0" = "CC-BY-NC-ND-4.0",
  "CC-BY-NC-SA-1.0" = "CC-BY-NC-SA-1.0",
  "CC-BY-NC-SA-2.0" = "CC-BY-NC-SA-2.0",
  "CC-BY-NC-SA-2.0-DE" = "CC-BY-NC-SA-2.0-DE",
  "CC-BY-NC-SA-2.0-FR" = "CC-BY-NC-SA-2.0-FR",
  "CC-BY-NC-SA-2.0-UK" = "CC-BY-NC-SA-2.0-UK",
  "CC-BY-NC-SA-2.5" = "CC-BY-NC-SA-2.5",
  "CC-BY-NC-SA-3.0" = "CC-BY-NC-SA-3.0",
  "CC-BY-NC-SA-3.0-DE" = "CC-BY-NC-SA-3.0-DE",
  "CC-BY-NC-SA-3.0-IGO" = "CC-BY-NC-SA-3.0-IGO",
  "CC-BY-NC-SA-4.0" = "CC-BY-NC-SA-4.0",
  "CC-BY-ND-1.0" = "CC-BY-ND-1.0",
  "CC-BY-ND-2.0" = "CC-BY-ND-2.0",
  "CC-BY-ND-2.5" = "CC-BY-ND-2.5",
  "CC-BY-ND-3.0" = "CC-BY-ND-3.0",
  "CC-BY-ND-3.0-DE" = "CC-BY-ND-3.0-DE",
  "CC-BY-ND-4.0" = "CC-BY-ND-4.0",
  "CC-BY-SA-1.0" = "CC-BY-SA-1.0",
  "CC-BY-SA-2.0" = "CC-BY-SA-2.0",
  "CC-BY-SA-2.0-UK" = "CC-BY-SA-2.0-UK",
  "CC-BY-SA-2.1-JP" = "CC-BY-SA-2.1-JP",
  "CC-BY-SA-2.5" = "CC-BY-SA-2.5",
  "CC-BY-SA-3.0" = "CC-BY-SA-3.0",
  "CC-BY-SA-3.0-AT" = "CC-BY-SA-3.0-AT",
  "CC-BY-SA-3.0-DE" = "CC-BY-SA-3.0-DE",
  "CC-BY-SA-3.0-IGO" = "CC-BY-SA-3.0-IGO",
  "CC-BY-SA-4.0" = "CC-BY-SA-4.0",
  "CC-PDDC" = "CC-PDDC",
  "CC0-1.0" = "CC0-1.0",
  "CDDL-1.0" = "CDDL-1.0",
  "CDDL-1.1" = "CDDL-1.1",
  "CDL-1.0" = "CDL-1.0",
  "CDLA-Permissive-1.0" = "CDLA-Permissive-1.0",
  "CDLA-Permissive-2.0" = "CDLA-Permissive-2.0",
  "CDLA-Sharing-1.0" = "CDLA-Sharing-1.0",
  "CECILL-1.0" = "CECILL-1.0",
  "CECILL-1.1" = "CECILL-1.1",
  "CECILL-2.0" = "CECILL-2.0",
  "CECILL-2.1" = "CECILL-2.1",
  "CECILL-B" = "CECILL-B",
  "CECILL-C" = "CECILL-C",
  "CERN-OHL-1.1" = "CERN-OHL-1.1",
  "CERN-OHL-1.2" = "CERN-OHL-1.2",
  "CERN-OHL-P-2.0" = "CERN-OHL-P-2.0",
  "CERN-OHL-S-2.0" = "CERN-OHL-S-2.0",
  "CERN-OHL-W-2.0" = "CERN-OHL-W-2.0",
  "CFITSIO" = "CFITSIO",
  "checkmk" = "checkmk",
  "ClArtistic" = "ClArtistic",
  "Clips" = "Clips",
  "CMU-Mach" = "CMU-Mach",
  "CNRI-Jython" = "CNRI-Jython",
  "CNRI-Python" = "CNRI-Python",
  "CNRI-Python-GPL-Compatible" = "CNRI-Python-GPL-Compatible",
  "COIL-1.0" = "COIL-1.0",
  "Community-Spec-1.0" = "Community-Spec-1.0",
  "Condor-1.1" = "Condor-1.1",
  "copyleft-next-0.3.0" = "copyleft-next-0.3.0",
  "copyleft-next-0.3.1" = "copyleft-next-0.3.1",
  "Cornell-Lossless-JPEG" = "Cornell-Lossless-JPEG",
  "CPAL-1.0" = "CPAL-1.0",
  "CPL-1.0" = "CPL-1.0",
  "CPOL-1.02" = "CPOL-1.02",
  "Crossword" = "Crossword",
  "CrystalStacker" = "CrystalStacker",
  "CUA-OPL-1.0" = "CUA-OPL-1.0",
  "Cube" = "Cube",
  "curl" = "curl",
  "D-FSL-1.0" = "D-FSL-1.0",
  "diffmark" = "diffmark",
  "DL-DE-BY-2.0" = "DL-DE-BY-2.0",
  "DOC" = "DOC",
  "Dotseqn" = "Dotseqn",
  "DRL-1.0" = "DRL-1.0",
  "DSDP" = "DSDP",
  "dtoa" = "dtoa",
  "dvipdfm" = "dvipdfm",
  "ECL-1.0" = "ECL-1.0",
  "ECL-2.0" = "ECL-2.0",
  "eCos-2.0" = "eCos-2.0",
  "EFL-1.0" = "EFL-1.0",
  "EFL-2.0" = "EFL-2.0",
  "eGenix" = "eGenix",
  "Elastic-2.0" = "Elastic-2.0",
  "Entessa" = "Entessa",
  "EPICS" = "EPICS",
  "EPL-1.0" = "EPL-1.0",
  "EPL-2.0" = "EPL-2.0",
  "ErlPL-1.1" = "ErlPL-1.1",
  "etalab-2.0" = "etalab-2.0",
  "EUDatagrid" = "EUDatagrid",
  "EUPL-1.0" = "EUPL-1.0",
  "EUPL-1.1" = "EUPL-1.1",
  "EUPL-1.2" = "EUPL-1.2",
  "Eurosym" = "Eurosym",
  "Fair" = "Fair",
  "FDK-AAC" = "FDK-AAC",
  "Frameworx-1.0" = "Frameworx-1.0",
  "FreeBSD-DOC" = "FreeBSD-DOC",
  "FreeImage" = "FreeImage",
  "FSFAP" = "FSFAP",
  "FSFUL" = "FSFUL",
  "FSFULLR" = "FSFULLR",
  "FSFULLRWD" = "FSFULLRWD",
  "FTL" = "FTL",
  "GD" = "GD",
  "GFDL-1.1" = "GFDL-1.1",
  "GFDL-1.1-invariants-only" = "GFDL-1.1-invariants-only",
  "GFDL-1.1-invariants-or-later" = "GFDL-1.1-invariants-or-later",
  "GFDL-1.1-no-invariants-only" = "GFDL-1.1-no-invariants-only",
  "GFDL-1.1-no-invariants-or-later" = "GFDL-1.1-no-invariants-or-later",
  "GFDL-1.1-only" = "GFDL-1.1-only",
  "GFDL-1.1-or-later" = "GFDL-1.1-or-later",
  "GFDL-1.2" = "GFDL-1.2",
  "GFDL-1.2-invariants-only" = "GFDL-1.2-invariants-only",
  "GFDL-1.2-invariants-or-later" = "GFDL-1.2-invariants-or-later",
  "GFDL-1.2-no-invariants-only" = "GFDL-1.2-no-invariants-only",
  "GFDL-1.2-no-invariants-or-later" = "GFDL-1.2-no-invariants-or-later",
  "GFDL-1.2-only" = "GFDL-1.2-only",
  "GFDL-1.2-or-later" = "GFDL-1.2-or-later",
  "GFDL-1.3" = "GFDL-1.3",
  "GFDL-1.3-invariants-only" = "GFDL-1.3-invariants-only",
  "GFDL-1.3-invariants-or-later" = "GFDL-1.3-invariants-or-later",
  "GFDL-1.3-no-invariants-only" = "GFDL-1.3-no-invariants-only",
  "GFDL-1.3-no-invariants-or-later" = "GFDL-1.3-no-invariants-or-later",
  "GFDL-1.3-only" = "GFDL-1.3-only",
  "GFDL-1.3-or-later" = "GFDL-1.3-or-later",
  "Giftware" = "Giftware",
  "GL2PS" = "GL2PS",
  "Glide" = "Glide",
  "Glulxe" = "Glulxe",
  "GLWTPL" = "GLWTPL",
  "gnuplot" = "gnuplot",
  "GPL-1.0" = "GPL-1.0",
  "GPL-1.0+" = "GPL-1.0+",
  "GPL-1.0-only" = "GPL-1.0-only",
  "GPL-1.0-or-later" = "GPL-1.0-or-later",
  "GPL-2.0" = "GPL-2.0",
  "GPL-2.0+" = "GPL-2.0+",
  "GPL-2.0-only" = "GPL-2.0-only",
  "GPL-2.0-or-later" = "GPL-2.0-or-later",
  "GPL-2.0-with-autoconf-exception" = "GPL-2.0-with-autoconf-exception",
  "GPL-2.0-with-bison-exception" = "GPL-2.0-with-bison-exception",
  "GPL-2.0-with-classpath-exception" = "GPL-2.0-with-classpath-exception",
  "GPL-2.0-with-font-exception" = "GPL-2.0-with-font-exception",
  "GPL-2.0-with-GCC-exception" = "GPL-2.0-with-GCC-exception",
  "GPL-3.0" = "GPL-3.0",
  "GPL-3.0+" = "GPL-3.0+",
  "GPL-3.0-only" = "GPL-3.0-only",
  "GPL-3.0-or-later" = "GPL-3.0-or-later",
  "GPL-3.0-with-autoconf-exception" = "GPL-3.0-with-autoconf-exception",
  "GPL-3.0-with-GCC-exception" = "GPL-3.0-with-GCC-exception",
  "Graphics-Gems" = "Graphics-Gems",
  "gSOAP-1.3b" = "gSOAP-1.3b",
  "HaskellReport" = "HaskellReport",
  "Hippocratic-2.1" = "Hippocratic-2.1",
  "HP-1986" = "HP-1986",
  "HPND" = "HPND",
  "HPND-export-US" = "HPND-export-US",
  "HPND-Markus-Kuhn" = "HPND-Markus-Kuhn",
  "HPND-sell-variant" = "HPND-sell-variant",
  "HPND-sell-variant-MIT-disclaimer" = "HPND-sell-variant-MIT-disclaimer",
  "HTMLTIDY" = "HTMLTIDY",
  "IBM-pibs" = "IBM-pibs",
  "ICU" = "ICU",
  "IEC-Code-Components-EULA" = "IEC-Code-Components-EULA",
  "IJG" = "IJG",
  "IJG-short" = "IJG-short",
  "ImageMagick" = "ImageMagick",
  "iMatix" = "iMatix",
  "Imlib2" = "Imlib2",
  "Info-ZIP" = "Info-ZIP",
  "Inner-Net-2.0" = "Inner-Net-2.0",
  "Intel" = "Intel",
  "Intel-ACPI" = "Intel-ACPI",
  "Interbase-1.0" = "Interbase-1.0",
  "IPA" = "IPA",
  "IPL-1.0" = "IPL-1.0",
  "ISC" = "ISC",
  "Jam" = "Jam",
  "JasPer-2.0" = "JasPer-2.0",
  "JPL-image" = "JPL-image",
  "JPNIC" = "JPNIC",
  "JSON" = "JSON",
  "Kazlib" = "Kazlib",
  "Knuth-CTAN" = "Knuth-CTAN",
  "LAL-1.2" = "LAL-1.2",
  "LAL-1.3" = "LAL-1.3",
  "Latex2e" = "Latex2e",
  "Latex2e-translated-notice" = "Latex2e-translated-notice",
  "Leptonica" = "Leptonica",
  "LGPL-2.0" = "LGPL-2.0",
  "LGPL-2.0+" = "LGPL-2.0+",
  "LGPL-2.0-only" = "LGPL-2.0-only",
  "LGPL-2.0-or-later" = "LGPL-2.0-or-later",
  "LGPL-2.1" = "LGPL-2.1",
  "LGPL-2.1+" = "LGPL-2.1+",
  "LGPL-2.1-only" = "LGPL-2.1-only",
  "LGPL-2.1-or-later" = "LGPL-2.1-or-later",
  "LGPL-3.0" = "LGPL-3.0",
  "LGPL-3.0+" = "LGPL-3.0+",
  "LGPL-3.0-only" = "LGPL-3.0-only",
  "LGPL-3.0-or-later" = "LGPL-3.0-or-later",
  "LGPLLR" = "LGPLLR",
  "Libpng" = "Libpng",
  "libpng-2.0" = "libpng-2.0",
  "libselinux-1.0" = "libselinux-1.0",
  "libtiff" = "libtiff",
  "libutil-David-Nugent" = "libutil-David-Nugent",
  "LiLiQ-P-1.1" = "LiLiQ-P-1.1",
  "LiLiQ-R-1.1" = "LiLiQ-R-1.1",
  "LiLiQ-Rplus-1.1" = "LiLiQ-Rplus-1.1",
  "Linux-man-pages-1-para" = "Linux-man-pages-1-para",
  "Linux-man-pages-copyleft" = "Linux-man-pages-copyleft",
  "Linux-man-pages-copyleft-2-para" = "Linux-man-pages-copyleft-2-para",
  "Linux-man-pages-copyleft-var" = "Linux-man-pages-copyleft-var",
  "Linux-OpenIB" = "Linux-OpenIB",
  "LOOP" = "LOOP",
  "LPL-1.0" = "LPL-1.0",
  "LPL-1.02" = "LPL-1.02",
  "LPPL-1.0" = "LPPL-1.0",
  "LPPL-1.1" = "LPPL-1.1",
  "LPPL-1.2" = "LPPL-1.2",
  "LPPL-1.3a" = "LPPL-1.3a",
  "LPPL-1.3c" = "LPPL-1.3c",
  "LZMA-SDK-9.11-to-9.20" = "LZMA-SDK-9.11-to-9.20",
  "LZMA-SDK-9.22" = "LZMA-SDK-9.22",
  "MakeIndex" = "MakeIndex",
  "Martin-Birgmeier" = "Martin-Birgmeier",
  "metamail" = "metamail",
  "Minpack" = "Minpack",
  "MirOS" = "MirOS",
  "MIT" = "MIT",
  "MIT-0" = "MIT-0",
  "MIT-advertising" = "MIT-advertising",
  "MIT-CMU" = "MIT-CMU",
  "MIT-enna" = "MIT-enna",
  "MIT-feh" = "MIT-feh",
  "MIT-Festival" = "MIT-Festival",
  "MIT-Modern-Variant" = "MIT-Modern-Variant",
  "MIT-open-group" = "MIT-open-group",
  "MIT-Wu" = "MIT-Wu",
  "MITNFA" = "MITNFA",
  "Motosoto" = "Motosoto",
  "mpi-permissive" = "mpi-permissive",
  "mpich2" = "mpich2",
  "MPL-1.0" = "MPL-1.0",
  "MPL-1.1" = "MPL-1.1",
  "MPL-2.0" = "MPL-2.0",
  "MPL-2.0-no-copyleft-exception" = "MPL-2.0-no-copyleft-exception",
  "mplus" = "mplus",
  "MS-LPL" = "MS-LPL",
  "MS-PL" = "MS-PL",
  "MS-RL" = "MS-RL",
  "MTLL" = "MTLL",
  "MulanPSL-1.0" = "MulanPSL-1.0",
  "MulanPSL-2.0" = "MulanPSL-2.0",
  "Multics" = "Multics",
  "Mup" = "Mup",
  "NAIST-2003" = "NAIST-2003",
  "NASA-1.3" = "NASA-1.3",
  "Naumen" = "Naumen",
  "NBPL-1.0" = "NBPL-1.0",
  "NCGL-UK-2.0" = "NCGL-UK-2.0",
  "NCSA" = "NCSA",
  "Net-SNMP" = "Net-SNMP",
  "NetCDF" = "NetCDF",
  "Newsletr" = "Newsletr",
  "NGPL" = "NGPL",
  "NICTA-1.0" = "NICTA-1.0",
  "NIST-PD" = "NIST-PD",
  "NIST-PD-fallback" = "NIST-PD-fallback",
  "NIST-Software" = "NIST-Software",
  "NLOD-1.0" = "NLOD-1.0",
  "NLOD-2.0" = "NLOD-2.0",
  "NLPL" = "NLPL",
  "Nokia" = "Nokia",
  "NOSL" = "NOSL",
  "Noweb" = "Noweb",
  "NPL-1.0" = "NPL-1.0",
  "NPL-1.1" = "NPL-1.1",
  "NPOSL-3.0" = "NPOSL-3.0",
  "NRL" = "NRL",
  "NTP" = "NTP",
  "NTP-0" = "NTP-0",
  "Nunit" = "Nunit",
  "O-UDA-1.0" = "O-UDA-1.0",
  "OCCT-PL" = "OCCT-PL",
  "OCLC-2.0" = "OCLC-2.0",
  "ODbL-1.0" = "ODbL-1.0",
  "ODC-By-1.0" = "ODC-By-1.0",
  "OFFIS" = "OFFIS",
  "OFL-1.0" = "OFL-1.0",
  "OFL-1.0-no-RFN" = "OFL-1.0-no-RFN",
  "OFL-1.0-RFN" = "OFL-1.0-RFN",
  "OFL-1.1" = "OFL-1.1",
  "OFL-1.1-no-RFN" = "OFL-1.1-no-RFN",
  "OFL-1.1-RFN" = "OFL-1.1-RFN",
  "OGC-1.0" = "OGC-1.0",
  "OGDL-Taiwan-1.0" = "OGDL-Taiwan-1.0",
  "OGL-Canada-2.0" = "OGL-Canada-2.0",
  "OGL-UK-1.0" = "OGL-UK-1.0",
  "OGL-UK-2.0" = "OGL-UK-2.0",
  "OGL-UK-3.0" = "OGL-UK-3.0",
  "OGTSL" = "OGTSL",
  "OLDAP-1.1" = "OLDAP-1.1",
  "OLDAP-1.2" = "OLDAP-1.2",
  "OLDAP-1.3" = "OLDAP-1.3",
  "OLDAP-1.4" = "OLDAP-1.4",
  "OLDAP-2.0" = "OLDAP-2.0",
  "OLDAP-2.0.1" = "OLDAP-2.0.1",
  "OLDAP-2.1" = "OLDAP-2.1",
  "OLDAP-2.2" = "OLDAP-2.2",
  "OLDAP-2.2.1" = "OLDAP-2.2.1",
  "OLDAP-2.2.2" = "OLDAP-2.2.2",
  "OLDAP-2.3" = "OLDAP-2.3",
  "OLDAP-2.4" = "OLDAP-2.4",
  "OLDAP-2.5" = "OLDAP-2.5",
  "OLDAP-2.6" = "OLDAP-2.6",
  "OLDAP-2.7" = "OLDAP-2.7",
  "OLDAP-2.8" = "OLDAP-2.8",
  "OLFL-1.3" = "OLFL-1.3",
  "OML" = "OML",
  "OpenPBS-2.3" = "OpenPBS-2.3",
  "OpenSSL" = "OpenSSL",
  "OPL-1.0" = "OPL-1.0",
  "OPL-UK-3.0" = "OPL-UK-3.0",
  "OPUBL-1.0" = "OPUBL-1.0",
  "OSET-PL-2.1" = "OSET-PL-2.1",
  "OSL-1.0" = "OSL-1.0",
  "OSL-1.1" = "OSL-1.1",
  "OSL-2.0" = "OSL-2.0",
  "OSL-2.1" = "OSL-2.1",
  "OSL-3.0" = "OSL-3.0",
  "Parity-6.0.0" = "Parity-6.0.0",
  "Parity-7.0.0" = "Parity-7.0.0",
  "PDDL-1.0" = "PDDL-1.0",
  "PHP-3.0" = "PHP-3.0",
  "PHP-3.01" = "PHP-3.01",
  "Plexus" = "Plexus",
  "PolyForm-Noncommercial-1.0.0" = "PolyForm-Noncommercial-1.0.0",
  "PolyForm-Small-Business-1.0.0" = "PolyForm-Small-Business-1.0.0",
  "PostgreSQL" = "PostgreSQL",
  "PSF-2.0" = "PSF-2.0",
  "psfrag" = "psfrag",
  "psutils" = "psutils",
  "Python-2.0" = "Python-2.0",
  "Python-2.0.1" = "Python-2.0.1",
  "Qhull" = "Qhull",
  "QPL-1.0" = "QPL-1.0",
  "QPL-1.0-INRIA-2004" = "QPL-1.0-INRIA-2004",
  "Rdisc" = "Rdisc",
  "RHeCos-1.1" = "RHeCos-1.1",
  "RPL-1.1" = "RPL-1.1",
  "RPL-1.5" = "RPL-1.5",
  "RPSL-1.0" = "RPSL-1.0",
  "RSA-MD" = "RSA-MD",
  "RSCPL" = "RSCPL",
  "Ruby" = "Ruby",
  "SAX-PD" = "SAX-PD",
  "Saxpath" = "Saxpath",
  "SCEA" = "SCEA",
  "SchemeReport" = "SchemeReport",
  "Sendmail" = "Sendmail",
  "Sendmail-8.23" = "Sendmail-8.23",
  "SGI-B-1.0" = "SGI-B-1.0",
  "SGI-B-1.1" = "SGI-B-1.1",
  "SGI-B-2.0" = "SGI-B-2.0",
  "SGP4" = "SGP4",
  "SHL-0.5" = "SHL-0.5",
  "SHL-0.51" = "SHL-0.51",
  "SimPL-2.0" = "SimPL-2.0",
  "SISSL" = "SISSL",
  "SISSL-1.2" = "SISSL-1.2",
  "Sleepycat" = "Sleepycat",
  "SMLNJ" = "SMLNJ",
  "SMPPL" = "SMPPL",
  "SNIA" = "SNIA",
  "snprintf" = "snprintf",
  "Spencer-86" = "Spencer-86",
  "Spencer-94" = "Spencer-94",
  "Spencer-99" = "Spencer-99",
  "SPL-1.0" = "SPL-1.0",
  "SSH-OpenSSH" = "SSH-OpenSSH",
  "SSH-short" = "SSH-short",
  "SSPL-1.0" = "SSPL-1.0",
  "StandardML-NJ" = "StandardML-NJ",
  "SugarCRM-1.1.3" = "SugarCRM-1.1.3",
  "SunPro" = "SunPro",
  "SWL" = "SWL",
  "Symlinks" = "Symlinks",
  "TAPR-OHL-1.0" = "TAPR-OHL-1.0",
  "TCL" = "TCL",
  "TCP-wrappers" = "TCP-wrappers",
  "TermReadKey" = "TermReadKey",
  "TMate" = "TMate",
  "TORQUE-1.1" = "TORQUE-1.1",
  "TOSL" = "TOSL",
  "TPDL" = "TPDL",
  "TPL-1.0" = "TPL-1.0",
  "TTWL" = "TTWL",
  "TU-Berlin-1.0" = "TU-Berlin-1.0",
  "TU-Berlin-2.0" = "TU-Berlin-2.0",
  "UCAR" = "UCAR",
  "UCL-1.0" = "UCL-1.0",
  "Unicode-DFS-2015" = "Unicode-DFS-2015",
  "Unicode-DFS-2016" = "Unicode-DFS-2016",
  "Unicode-TOU" = "Unicode-TOU",
  "UnixCrypt" = "UnixCrypt",
  "Unlicense" = "Unlicense",
  "UPL-1.0" = "UPL-1.0",
  "Vim" = "Vim",
  "VOSTROM" = "VOSTROM",
  "VSL-1.0" = "VSL-1.0",
  "W3C" = "W3C",
  "W3C-19980720" = "W3C-19980720",
  "W3C-20150513" = "W3C-20150513",
  "w3m" = "w3m",
  "Watcom-1.0" = "Watcom-1.0",
  "Widget-Workshop" = "Widget-Workshop",
  "Wsuipa" = "Wsuipa",
  "WTFPL" = "WTFPL",
  "wxWindows" = "wxWindows",
  "X11" = "X11",
  "X11-distribute-modifications-variant" = "X11-distribute-modifications-variant",
  "Xdebug-1.03" = "Xdebug-1.03",
  "Xerox" = "Xerox",
  "Xfig" = "Xfig",
  "XFree86-1.1" = "XFree86-1.1",
  "xinetd" = "xinetd",
  "xlock" = "xlock",
  "Xnet" = "Xnet",
  "xpp" = "xpp",
  "XSkat" = "XSkat",
  "YPL-1.0" = "YPL-1.0",
  "YPL-1.1" = "YPL-1.1",
  "Zed" = "Zed",
  "Zend-2.0" = "Zend-2.0",
  "Zimbra-1.3" = "Zimbra-1.3",
  "Zimbra-1.4" = "Zimbra-1.4",
  "Zlib" = "Zlib",
  "zlib-acknowledgement" = "zlib-acknowledgement",
  "ZPL-1.1" = "ZPL-1.1",
  "ZPL-2.0" = "ZPL-2.0",
  "ZPL-2.1" = "ZPL-2.1",
  "389-exception" = "389-exception",
  "Asterisk-exception" = "Asterisk-exception",
  "Autoconf-exception-2.0" = "Autoconf-exception-2.0",
  "Autoconf-exception-3.0" = "Autoconf-exception-3.0",
  "Autoconf-exception-generic" = "Autoconf-exception-generic",
  "Autoconf-exception-macro" = "Autoconf-exception-macro",
  "Bison-exception-2.2" = "Bison-exception-2.2",
  "Bootloader-exception" = "Bootloader-exception",
  "Classpath-exception-2.0" = "Classpath-exception-2.0",
  "CLISP-exception-2.0" = "CLISP-exception-2.0",
  "cryptsetup-OpenSSL-exception" = "cryptsetup-OpenSSL-exception",
  "DigiRule-FOSS-exception" = "DigiRule-FOSS-exception",
  "eCos-exception-2.0" = "eCos-exception-2.0",
  "Fawkes-Runtime-exception" = "Fawkes-Runtime-exception",
  "FLTK-exception" = "FLTK-exception",
  "Font-exception-2.0" = "Font-exception-2.0",
  "freertos-exception-2.0" = "freertos-exception-2.0",
  "GCC-exception-2.0" = "GCC-exception-2.0",
  "GCC-exception-3.1" = "GCC-exception-3.1",
  "GNAT-exception" = "GNAT-exception",
  "gnu-javamail-exception" = "gnu-javamail-exception",
  "GPL-3.0-interface-exception" = "GPL-3.0-interface-exception",
  "GPL-3.0-linking-exception" = "GPL-3.0-linking-exception",
  "GPL-3.0-linking-source-exception" = "GPL-3.0-linking-source-exception",
  "GPL-CC-1.0" = "GPL-CC-1.0",
  "GStreamer-exception-2005" = "GStreamer-exception-2005",
  "GStreamer-exception-2008" = "GStreamer-exception-2008",
  "i2p-gpl-java-exception" = "i2p-gpl-java-exception",
  "KiCad-libraries-exception" = "KiCad-libraries-exception",
  "LGPL-3.0-linking-exception" = "LGPL-3.0-linking-exception",
  "libpri-OpenH323-exception" = "libpri-OpenH323-exception",
  "Libtool-exception" = "Libtool-exception",
  "Linux-syscall-note" = "Linux-syscall-note",
  "LLGPL" = "LLGPL",
  "LLVM-exception" = "LLVM-exception",
  "LZMA-exception" = "LZMA-exception",
  "mif-exception" = "mif-exception",
  "Nokia-Qt-exception-1.1" = "Nokia-Qt-exception-1.1",
  "OCaml-LGPL-linking-exception" = "OCaml-LGPL-linking-exception",
  "OCCT-exception-1.0" = "OCCT-exception-1.0",
  "OpenJDK-assembly-exception-1.0" = "OpenJDK-assembly-exception-1.0",
  "openvpn-openssl-exception" = "openvpn-openssl-exception",
  "PS-or-PDF-font-exception-20170817" = "PS-or-PDF-font-exception-20170817",
  "QPL-1.0-INRIA-2004-exception" = "QPL-1.0-INRIA-2004-exception",
  "Qt-GPL-exception-1.0" = "Qt-GPL-exception-1.0",
  "Qt-LGPL-exception-1.1" = "Qt-LGPL-exception-1.1",
  "Qwt-exception-1.0" = "Qwt-exception-1.0",
  "SHL-2.0" = "SHL-2.0",
  "SHL-2.1" = "SHL-2.1",
  "SWI-exception" = "SWI-exception",
  "Swift-exception" = "Swift-exception",
  "u-boot-exception-2.0" = "u-boot-exception-2.0",
  "Universal-FOSS-exception-1.0" = "Universal-FOSS-exception-1.0",
  "vsftpd-openssl-exception" = "vsftpd-openssl-exception",
  "WxWindows-exception-3.1" = "WxWindows-exception-3.1",
  "x11vnc-openssl-exception" = "x11vnc-openssl-exception"
}
