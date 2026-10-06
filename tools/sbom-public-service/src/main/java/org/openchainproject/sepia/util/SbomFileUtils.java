// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openchainproject.sepia.model.BomFilesInputModel;
import org.openchainproject.sepia.model.ErrorModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

@Component
public class SbomFileUtils {
	
	@Value("${proxy.url}")
	private String proxyHost;

	@Value("${proxy.port}")
	private String proxyPort;

	/** Maximum number of HTTP redirects to follow when fetching an external schema. */
	private static final int MAX_REDIRECTS = 5;

	private static final Logger LOGGER = LoggerFactory.getLogger(SbomFileUtils.class);
    
	
	/**
	 * 
	 * @param regex
	 * @param text
	 * @param group
	 * @return
	 */
	public static String regexExtractor(String regex, String text, int group) {
		String matchedText = "";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find()) {
			matchedText = matcher.group(group);
		}
		return matchedText;
	}
	
	/**
	 * 
	 * @param existingFiles
	 * @param skipDirName
	 * @return
	 */
	public boolean deleteObsoleteDirs(File currentFile, String directoryToSkipDeleting) {
		try {
			if (currentFile.isDirectory()) {
				if (directoryToSkipDeleting == null
						|| !currentFile.getName().equalsIgnoreCase(directoryToSkipDeleting)) {
					FileUtils.deleteDirectory(currentFile);
				}
			} else {
				FileUtils.deleteDirectory(currentFile);
			}
			return true;
		} catch (Exception e) {
			LOGGER.error("Exception occured while deleting files >> {}", currentFile.getName(), e);
			return false;
		}
	}
	

	/**
	 * generateUuid() Method used to generate a random UUID for SPDXID
	 * 
	 * @return
	 */
	public static String generateUuid() {
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();
		return uuidAsString;
	}

	/**
	 * 
	 * @param directory
	 * @param fileToSave
	 * @param isSchemaFile
	 * @return true if the file save operation is successful
	 */
	public File saveFileInDirectory(File directory, MultipartFile fileToSave, boolean isSchemaFile) throws IOException {
		File uploadedFile = null;
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String fileNameToBeSaved = fileToSave.getOriginalFilename();
		String keyword = isSchemaFile ? "~schema." : "~original.";

		// Read file bytes once; if it is a schema file, integrate any external schema
		// references online before saving so the stored copy is self-contained
		byte[] fileBytes = fileToSave.getBytes();
		if (isSchemaFile) {
			String schemaContent = new String(fileBytes, StandardCharsets.UTF_8);
			String integratedContent = integrateExternalSchemasOnline(schemaContent, true);
			fileBytes = integratedContent.getBytes(StandardCharsets.UTF_8);
		}

		if (!StringUtils.isEmpty(fileNameToBeSaved)) {
			String onlyName = Files.getNameWithoutExtension(fileNameToBeSaved);
			String revisedFileNameToBeSaved = onlyName + keyword
					+ fileNameToBeSaved.substring(fileNameToBeSaved.lastIndexOf(".") + 1);

			File currentFile = new File(directory, revisedFileNameToBeSaved);
			FileCopyUtils.copy(fileBytes, new FileOutputStream(currentFile));

			if (!isSchemaFile) {
				uploadedFile = new File(directory, fileNameToBeSaved);
				FileCopyUtils.copy(fileBytes, new FileOutputStream(uploadedFile));
			}
		}

		return uploadedFile;
	}

	public void sanitizeDirectory(File directoryToSanitize, BomFilesInputModel sbomInputModel) {
		try {
			if (directoryToSanitize.isDirectory()) {
				if (!sbomInputModel.getSchemaType().equalsIgnoreCase(Constants.CUSTOM)) {
					FileUtils.deleteDirectory(directoryToSanitize);
				} else {
					if (!directoryToSanitize.getName().equalsIgnoreCase(
							sbomInputModel.getIndex() + Constants.UNDERSCORE + sbomInputModel.getSchemaType())) {
						FileUtils.deleteDirectory(directoryToSanitize);
					} else {
						File[] filesList = directoryToSanitize.listFiles();
						if (filesList.length > 0) {
							for (File curFile : filesList) {
								if (curFile.getName().contains("~schema")) {
									if (sbomInputModel.isSchema()) {
										java.nio.file.Files.delete(curFile.toPath());
									}
								} else {
									if (!sbomInputModel.isSchema()) {
										java.nio.file.Files.delete(curFile.toPath());
									}
								}
							}
						}
					}
				}
			} else {
				java.nio.file.Files.delete(directoryToSanitize.toPath());
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while sanitizing directory before upload >> ", e);
		}

	}

	public List<ErrorModel> getErrorListFromString(String errorString) {
		List<ErrorModel> errorList = new ArrayList<>();
		for (String error : errorString.split("\r\n")) {
			String[] keyAndValueSplit = error.split(":");
			if (keyAndValueSplit.length > 1) {
				ErrorModel errorModel = new ErrorModel();
				errorModel.setErrorKey(keyAndValueSplit[0]);
				errorModel.setMessage(keyAndValueSplit[1]);
				errorList.add(errorModel);
			}
		}
		return errorList;
	}

	public String generateFileHash(String stringToHash) throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("SHA-512");

		byte[] bytes = md.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public String generateFileHash(File fileToHash) throws IOException {

		return Files.hash(fileToHash, Hashing.sha512()).toString();
	}

	/**
	 * Integrates external schema references into the uploaded schema file.
	 * Fetches external schemas online and merges them into the root schema.
	 * 
	 * @param schemaJsonString The schema JSON string to process
	 * @param isSchemaFile Flag to indicate if the uploaded file is a schema file
	 * @return The integrated schema string with external references resolved, or the original string if not a schema file or no external refs found
	 * @throws IOException If there's an error reading or processing the schema
	 */
	public String integrateExternalSchemasOnline(String schemaJsonString, boolean isSchemaFile) throws IOException {
		if (!isSchemaFile || StringUtils.isEmpty(schemaJsonString)) {
			return schemaJsonString;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode schemaNode = mapper.readTree(schemaJsonString);
			
			if (!(schemaNode instanceof ObjectNode)) {
				return schemaJsonString;
			}

			ObjectNode schemaObjectNode = (ObjectNode) schemaNode;

			// Extract the base URL from the schema's $id so that unknown external
			// references can be resolved relative to it automatically.
			// e.g. $id = "http://cyclonedx.org/schema/bom-1.7.schema.json"
			// -> base = "http://cyclonedx.org/schema/"
			String baseUrl = resolveBaseUrl(schemaObjectNode);
			
			// Map to store external schemas: key = schema URL, value = schema content
			Map<String, ObjectNode> externalSchemas = new HashMap<>();
			
			// Find all external references in the schema
			List<ExternalSchemaRef> externalRefs = findExternalSchemaReferences(schemaObjectNode, baseUrl);
			
			if (externalRefs.isEmpty()) {
				LOGGER.debug("No external schema references found");
				return schemaJsonString;
			}

			// Fetch and process each external schema
			for (ExternalSchemaRef extRef : externalRefs) {
				try {
					ObjectNode externalSchema = fetchExternalSchema(extRef.getSchemaUrl(), extRef.getSchemaName());
					if (externalSchema != null) {
						externalSchemas.put(extRef.getSchemaUrl(), externalSchema);
						LOGGER.info("Successfully fetched external schema: {}", extRef.getSchemaUrl());
					}
				} catch (Exception e) {
					LOGGER.error("Failed to fetch external schema '{}': {}", extRef.getSchemaName(), e.getMessage());
					throw new IOException("Failed to fetch external schema '" + extRef.getSchemaName(), e);
				}
			}

			// Integrate fetched schemas into root schema
		integrateExternalSchemas(schemaObjectNode, externalRefs, externalSchemas);
			return mapper.writeValueAsString(schemaObjectNode);

		} catch (Exception e) {
			LOGGER.error("Error integrating external schemas online", e);
			throw new IOException("Failed to fetch external schema '"+ "': " + e.getMessage(), e);
			//return schemaJsonString; // Return original if integration fails
		}
	}

	/**
	 * Extracts the directory base URL from the schema's {@code $id} field so that
	 * relative external references can be resolved without any hardcoding.
	 * Returns an empty string if no {@code $id} is present.
	 */
	private String resolveBaseUrl(ObjectNode schemaNode) {
		JsonNode idNode = schemaNode.get("$id");
		if (idNode == null || !idNode.isTextual()) {
			return "";
		}
		String id = idNode.asText();
		int lastSlash = id.lastIndexOf('/');
		return lastSlash > 0 ? id.substring(0, lastSlash + 1) : "";
	}

	/**
	 * Finds all external schema references in the schema JSON.
	 *
	 * @param node    The JSON node to search
	 * @param baseUrl Base URL derived from the schema's {@code $id}, used to resolve
	 *                unknown relative references
	 * @return List of external schema references found
	 */
	private List<ExternalSchemaRef> findExternalSchemaReferences(JsonNode node, String baseUrl) {
		List<ExternalSchemaRef> externalRefs = new ArrayList<>();
		findExternalRefsRecursive(node, externalRefs, "", baseUrl);
		return externalRefs;
	}

	/**
	 * Recursively searches for external schema references.
	 */
	private void findExternalRefsRecursive(JsonNode node, List<ExternalSchemaRef> externalRefs,
			String path, String baseUrl) {
		if (node == null) {
			return;
		}

		if (node.isObject()) {
			ObjectNode objectNode = (ObjectNode) node;
			Iterator<Map.Entry<String, JsonNode>> fieldsIterator = objectNode.fields();
			
			while (fieldsIterator.hasNext()) {
				Map.Entry<String, JsonNode> field = fieldsIterator.next();
				String fieldName = field.getKey();
				JsonNode fieldValue = field.getValue();

				// Check if this is a $ref field pointing to an external schema
				if ("$ref".equals(fieldName) && fieldValue.isTextual()) {
					String refValue = fieldValue.asText();
					if (isExternalReference(refValue)) {
						ExternalSchemaRef extRef = parseExternalReference(refValue, baseUrl);
						if (extRef != null && !externalRefs.contains(extRef)) {
							externalRefs.add(extRef);
						}
					}
				}

				findExternalRefsRecursive(fieldValue, externalRefs, path + "/" + fieldName, baseUrl);
			}
		} else if (node.isArray()) {
			ArrayNode arrayNode = (ArrayNode) node;
			for (int i = 0; i < arrayNode.size(); i++) {
				findExternalRefsRecursive(arrayNode.get(i), externalRefs, path + "[" + i + "]", baseUrl);
			}
		}
	}

	/**
	 * Checks if a reference is an external schema reference.
	 * <p>
	 * A reference is considered external when it is a relative path pointing to a
	 * {@code .json} file — i.e. it does not start with {@code #} (internal anchor)
	 * and is not already an absolute HTTP/HTTPS URL.
	 * This generic check means <strong>any</strong> new relative JSON schema
	 * reference will be detected automatically without code changes.
	 */
	private boolean isExternalReference(String refValue) {
		if (StringUtils.isEmpty(refValue) || refValue.startsWith("#")) {
			return false;
		}
		// Already absolute URLs are not relative external schema files
		if (refValue.startsWith("http://") || refValue.startsWith("https://")) {
			return false;
		}
		// Any relative reference to a JSON file is treated as an external schema
		return refValue.contains("schema.json");
	}

	/**
	 * Parses an external reference into a schema name, fetch URL, and definition path.
	 * Example: "jsf-0.82.schema.json#/definitions/signature" ->
	 *   schemaName="jsf-0.82.schema.json", definitionPath="/definitions/signature"
	 *
	 * @param refValue the raw {@code $ref} value
	 * @param baseUrl  base URL from the schema's {@code $id}; used to build the fetch
	 *                 URL for schemas not in the known-URL table
	 */
	private ExternalSchemaRef parseExternalReference(String refValue, String baseUrl) {
		String[] parts = refValue.split("#", 2);
		String schemaName = parts[0];
		String definitionPath = parts.length > 1 ? parts[1] : "";

		String schemaUrl = getSchemaUrl(schemaName, baseUrl);
		if (schemaUrl != null) {
			return new ExternalSchemaRef(schemaName, schemaUrl, definitionPath);
		}
		return null;
	}

	/**
	 * Returns the primary HTTPS fetch URL for a given schema filename.
	 * <p>
	 * <b>Known schemas</b> are mapped to their canonical cyclonedx.org URLs.
	 * <b>Unknown schemas</b> (any future additions) are resolved automatically
	 * relative to the {@code baseUrl} extracted from the root schema's {@code $id},
	 * so no code change is needed when a new external reference appears.
	 * <p>
	 * Example with base {@code https://cyclonedx.org/schema/}:
	 * {@code new-defs.schema.json} → {@code https://cyclonedx.org/schema/new-defs.schema.json}
	 *
	 * @param schemaName short filename of the referenced schema
	 * @param baseUrl    directory portion of the root schema's {@code $id}
	 */
	private String getSchemaUrl(String schemaName, String baseUrl) {
		// Known primary URLs
		if ("spdx.schema.json".equals(schemaName)) {
			return "https://cyclonedx.org/schema/spdx.schema.json";
		} else if ("jsf-0.82.schema.json".equals(schemaName)) {
			return "https://cyclonedx.org/schema/jsf-0.82.schema.json";
		} else if ("cryptography-defs.schema.json".equals(schemaName)) {
			return "https://cyclonedx.org/schema/cryptography-defs.schema.json";
		}
		// Unknown schema: resolve relative to the root schema's $id base URL
		if (!StringUtils.isEmpty(baseUrl)) {
			String resolved = baseUrl + schemaName;
			LOGGER.info("Unknown external schema '{}'; resolved to: {}", schemaName, resolved);
			return resolved;
		}
		LOGGER.warn("Cannot resolve URL for unknown external schema '{}': no $id base URL available", schemaName);
		return null;
	}

	/**
	 * Returns a fallback HTTPS base URL (GitHub raw) for a given schema name,
	 * used when the primary cyclonedx.org host is unreachable.
	 */
	private String getFallbackSchemaUrl(String schemaName) {
		if ("spdx.schema.json".equals(schemaName)) {
			return "https://raw.githubusercontent.com/CycloneDX/cyclonedx-core-java/master/src/main/resources/spdx.schema.json";
		} else if ("jsf-0.82.schema.json".equals(schemaName)) {
			return "https://raw.githubusercontent.com/CycloneDX/cyclonedx-schema/master/schema/jsf-0.82.schema.json";
		} else if ("cryptography-defs.schema.json".equals(schemaName)) {
			return "https://raw.githubusercontent.com/CycloneDX/cyclonedx-schema/master/schema/ext/cryptography-defs.schema.json";
		}
		return null;
	}

	/**
	 * Fetches an external schema from the provided primary URL.
	 * If the host is unreachable (UnknownHostException), attempts to load the schema
	 * from the offline classpath resources at {@code externalSchemaDefinitions/<schemaName>}.
	 * If the schema is not found offline either, re-throws the original exception.
	 *
	 * @param schemaUrl  Primary URL to fetch
	 * @param schemaName Schema filename used to locate the offline resource
	 */
	private ObjectNode fetchExternalSchema(String schemaUrl, String schemaName) throws IOException {
		try {
			return fetchFromUrl(schemaUrl);
		} catch (IOException e) {
			LOGGER.warn("Network failure fetching schema '{}' ({}). Attempting offline fallback from classpath.",
					schemaName, e.getMessage());
			ObjectNode offlineSchema = fetchOfflineSchema(schemaName);
			if (offlineSchema != null) {
				LOGGER.info("Successfully loaded offline schema '{}' from classpath resources.", schemaName);
				return offlineSchema;
			}
			LOGGER.error("Schema '{}' not found in offline resources and online fetch failed. "
					+ "Check network connectivity or configure a proxy.", schemaName);
			throw e;
		}
	}

	/**
	 * Attempts to load a schema from the offline classpath resources directory
	 * {@code externalSchemaDefinitions/<schemaName>}.
	 *
	 * @param schemaName the schema filename to look up (e.g. {@code spdx.schema.json})
	 * @return the parsed {@link ObjectNode}, or {@code null} if the resource does not exist
	 *         or cannot be parsed
	 */
	private ObjectNode fetchOfflineSchema(String schemaName) {
		String resourcePath = Constants.EXTERNAL_SCHEMA_DEFINITIONS + schemaName;
		ClassPathResource resource = new ClassPathResource(resourcePath);
		if (!resource.exists()) {
			LOGGER.warn("Offline schema resource not found in classpath: {}", resourcePath);
			return null;
		}
		try (InputStream inputStream = resource.getInputStream()) {
			String schemaContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode schemaNode = mapper.readTree(schemaContent);
			if (schemaNode instanceof ObjectNode) {
				return (ObjectNode) schemaNode;
			}
			LOGGER.warn("Offline schema '{}' did not parse as a JSON object.", schemaName);
		} catch (IOException e) {
			LOGGER.warn("Failed to load offline schema '{}' from classpath: {}", schemaName, e.getMessage());
		}
		return null;
	}

	/**
	 * Performs the actual HTTP GET and parses the response body as an ObjectNode.
	 * Uses RestTemplate so Spring's connection/timeout infrastructure applies.
	 * <p>
	 * Redirects are followed manually because the JDK {@link java.net.HttpURLConnection}
	 * (used by {@link SimpleClientHttpRequestFactory}) refuses to follow cross-protocol
	 * redirects such as {@code http://} &rarr; {@code https://}. Without this, an
	 * {@code http} URL that 301-redirects to {@code https} returns the redirect HTML page
	 * (e.g. Cloudflare's "301 Moved Permanently") instead of the JSON schema, which then
	 * fails to parse.
	 * Wraps ResourceAccessException back to IOException so the UnknownHostException
	 * fallback logic in fetchExternalSchema continues to work unchanged.
	 */
	private ObjectNode fetchFromUrl(String schemaUrl) throws IOException {
		LOGGER.info("Fetching external schema from: {}", schemaUrl);
		RestTemplate restTemplate = new RestTemplate(buildRequestFactory());
		try {
			String schemaContent = getFollowingRedirects(restTemplate, schemaUrl);
			if (schemaContent == null) {
				LOGGER.warn("Received empty response from: {}", schemaUrl);
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			JsonNode schemaNode = mapper.readTree(schemaContent);
			if (schemaNode instanceof ObjectNode) {
				return (ObjectNode) schemaNode;
			}
			return null;
		} catch (ResourceAccessException e) {
			// Unwrap to IOException so UnknownHostException is still catchable upstream
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			throw new IOException("Failed to fetch schema from " + schemaUrl + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Issues GET requests and manually follows up to {@value #MAX_REDIRECTS} redirects,
	 * including cross-protocol ({@code http} &rarr; {@code https}) redirects that the
	 * underlying JDK connection will not follow on its own. Relative {@code Location}
	 * headers are resolved against the current URL.
	 *
	 * @param restTemplate the configured RestTemplate to use
	 * @param url          the initial URL to fetch
	 * @return the response body of the first non-redirect response, or {@code null}
	 *         if the redirect limit is exceeded or a redirect lacks a Location header
	 */
	private String getFollowingRedirects(RestTemplate restTemplate, String url) {
		String currentUrl = url;
		for (int redirectCount = 0; redirectCount < MAX_REDIRECTS; redirectCount++) {
			ResponseEntity<String> response = restTemplate.getForEntity(currentUrl, String.class);
			HttpStatus status = response.getStatusCode();
			if (!status.is3xxRedirection()) {
				return response.getBody();
			}

			URI location = response.getHeaders().getLocation();
			if (location == null) {
				LOGGER.warn("Redirect ({}) from {} had no Location header", status, currentUrl);
				return null;
			}
			String nextUrl = URI.create(currentUrl).resolve(location).toString();
			LOGGER.info("Following redirect ({}) from {} to {}", status.value(), currentUrl, nextUrl);
			currentUrl = nextUrl;
		}
		LOGGER.warn("Exceeded maximum of {} redirects while fetching {}", MAX_REDIRECTS, url);
		return null;
	}

	/**
	 * Builds a request factory with connection/read timeouts and, when running behind
	 * a corporate firewall, an HTTP proxy resolved from the standard JVM system properties:
	 * {@code https.proxyHost}/{@code https.proxyPort} (falling back to
	 * {@code http.proxyHost}/{@code http.proxyPort}).
	 * <p>
	 * If no proxy properties are set, a direct connection is used (existing behavior).
	 * To fix the {@code UnknownHostException: cyclonedx.org} error in a proxied network,
	 * start the service with, for example:
	 * {@code -Dhttps.proxyHost=proxy.example.com -Dhttps.proxyPort=8080}.
	 */
	private SimpleClientHttpRequestFactory buildRequestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(10000);
		factory.setReadTimeout(15000);

		if (!StringUtils.isEmpty(proxyHost) && !StringUtils.isEmpty(proxyPort)) {
			try {
				int port = Integer.parseInt(proxyPort.trim());
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.trim(), port));
				factory.setProxy(proxy);
				LOGGER.info("Using HTTP proxy {}:{} for external schema fetch", proxyHost, port);
			} catch (NumberFormatException e) {
				LOGGER.warn("Invalid proxy port '{}'; proceeding without a proxy", proxyPort);
			}
		}
		return factory;
	}

	/**
	 * Integrates external schemas into the root schema by:
	 * 1. Extracting all definitions from external schemas
	 * 2. Adding them to root schema's definitions
	 * 3. Rewriting external $refs to local references
	 */
	private void integrateExternalSchemas(ObjectNode rootSchema, List<ExternalSchemaRef> externalRefs,
			Map<String, ObjectNode> externalSchemas) {

		// Ensure a "definitions" ObjectNode exists in the root schema
		ObjectMapper mapper = new ObjectMapper();
		JsonNode existingDefs = rootSchema.get("definitions");
		ObjectNode definitions;
		if (existingDefs instanceof ObjectNode) {
			definitions = (ObjectNode) existingDefs;
		} else {
			definitions = mapper.createObjectNode();
			rootSchema.set("definitions", definitions);
		}
		
		// Build one schema-name lookup per URL so all definitions can be merged once per
		// fetched schema while still rewriting refs using the original schema filename.
		Map<String, String> schemaNameByUrl = new HashMap<>();
		for (ExternalSchemaRef extRef : externalRefs) {
			schemaNameByUrl.putIfAbsent(extRef.getSchemaUrl(), extRef.getSchemaName());
		}

		for (Map.Entry<String, ObjectNode> externalSchemaEntry : externalSchemas.entrySet()) {
			String schemaUrl = externalSchemaEntry.getKey();
			ObjectNode externalSchema = externalSchemaEntry.getValue();
			String schemaName = schemaNameByUrl.get(schemaUrl);

			if (externalSchema == null || StringUtils.isEmpty(schemaName)) {
				continue;
			}

			JsonNode externalDefinitionsNode = externalSchema.get("definitions");
			if (!(externalDefinitionsNode instanceof ObjectNode)) {
				// Some external schemas (e.g. spdx.schema.json) have no "definitions"
				// object; the schema root itself is the definition. Integrate the whole
				// schema as a single local definition so these references are not missed.
				integrateWholeSchemaAsDefinition(rootSchema, definitions, schemaName, externalSchema);
				continue;
			}

			ObjectNode externalDefinitions = (ObjectNode) externalDefinitionsNode;
			Map<String, String> localKeyByExternalDef = new HashMap<>();

			Iterator<Map.Entry<String, JsonNode>> definitionsIterator = externalDefinitions.fields();
			while (definitionsIterator.hasNext()) {
				Map.Entry<String, JsonNode> definitionEntry = definitionsIterator.next();
				String externalDefinitionName = definitionEntry.getKey();
				String localDefinitionKey = generateLocalDefinitionKey(schemaName, externalDefinitionName);

				definitions.set(localDefinitionKey, definitionEntry.getValue().deepCopy());
				localKeyByExternalDef.put(externalDefinitionName, localDefinitionKey);
				LOGGER.info("Added definition '{}' from external schema '{}'", localDefinitionKey, schemaName);
			}

			// Rewire internal refs inside imported definitions to their local namespaced
			// counterparts so copied definitions remain self-contained.
			for (Map.Entry<String, String> mapping : localKeyByExternalDef.entrySet()) {
				String externalDefinitionName = mapping.getKey();
				String localDefinitionKey = mapping.getValue();
				JsonNode importedDefinition = definitions.get(localDefinitionKey);
				for (Map.Entry<String, String> innerMapping : localKeyByExternalDef.entrySet()) {
					rewriteSchemaReferences(importedDefinition,
							"#/definitions/" + innerMapping.getKey(),
							"#/definitions/" + innerMapping.getValue());
				}

				// Rewrite references from root schema to the newly imported local definition.
				rewriteSchemaReferences(rootSchema,
						schemaName + "#/definitions/" + externalDefinitionName,
						"#/definitions/" + localDefinitionKey);
			}
		}
	}

	/**
	 * Gets a node at the specified JSON path.
	 * Example: "/definitions/signature" -> navigates to schema["definitions"]["signature"]
	 */
	private JsonNode getNodeAtPath(JsonNode node, String path) {
		if (StringUtils.isEmpty(path) || "/".equals(path)) {
			return node;
		}

		String[] pathParts = path.split("/");
		JsonNode currentNode = node;

		for (String part : pathParts) {
			if (StringUtils.isEmpty(part)) {
				continue;
			}
			if (currentNode.isObject()) {
				currentNode = currentNode.get(part);
				if (currentNode == null) {
					return null;
				}
			} else {
				return null;
			}
		}

		return currentNode;
	}

	/**
	 * Generates a deterministic local key to avoid collisions across external schema
	 * files by prefixing each imported definition with the schema filename stem.
	 */
	private String generateLocalDefinitionKey(String schemaName, String definitionName) {
		return normalizeSchemaName(schemaName) + "_" + definitionName;
	}

	/**
	 * Normalizes a schema filename into a safe definition-key stem.
	 * Example: {@code spdx.schema.json} -> {@code spdx}.
	 */
	private String normalizeSchemaName(String schemaName) {
		return schemaName
				.replace(".schema.json", "")
				.replace("-", "")
				.replace(".", "");
	}

	/**
	 * Integrates an external schema that has no {@code definitions} object by treating
	 * the entire schema document as a single local definition. This covers references
	 * such as {@code "$ref": "spdx.schema.json"} where the schema root itself
	 * (e.g. a {@code type: string} enum) is the definition.
	 * <p>
	 * Meta keywords {@code $schema} and {@code $id} are stripped from the embedded copy
	 * so the definition does not introduce a new resolution scope inside the root schema.
	 *
	 * @param rootSchema     the uploaded schema being integrated into
	 * @param definitions    the root schema's {@code definitions} node
	 * @param schemaName     the external schema filename (used for the ref and local key)
	 * @param externalSchema the fetched external schema content
	 */
	private void integrateWholeSchemaAsDefinition(ObjectNode rootSchema, ObjectNode definitions,
			String schemaName, ObjectNode externalSchema) {
		String localDefinitionKey = normalizeSchemaName(schemaName);

		ObjectNode definitionNode = (ObjectNode) externalSchema.deepCopy();
		definitionNode.remove("$schema");
		definitionNode.remove("$id");

		definitions.set(localDefinitionKey, definitionNode);
		LOGGER.info("Added whole external schema '{}' as definition '{}'", schemaName, localDefinitionKey);

		// Rewrite root references pointing to the whole external schema (no fragment).
		rewriteSchemaReferences(rootSchema, schemaName, "#/definitions/" + localDefinitionKey);
	}

	/**
	 * Rewrites all references from externalRef to newRef throughout the schema.
	 */
	private void rewriteSchemaReferences(JsonNode node, String externalRef, String newRef) {
		if (node == null) {
			return;
		}

		if (node.isObject()) {
			ObjectNode objectNode = (ObjectNode) node;
			Iterator<Map.Entry<String, JsonNode>> fieldsIterator = objectNode.fields();
			
			while (fieldsIterator.hasNext()) {
				Map.Entry<String, JsonNode> field = fieldsIterator.next();
				if ("$ref".equals(field.getKey()) && field.getValue().isTextual()
						&& externalRef.equals(field.getValue().asText())) {
					objectNode.put("$ref", newRef);
					LOGGER.debug("Rewrote reference from '{}' to '{}'", externalRef, newRef);
				} else {
					rewriteSchemaReferences(field.getValue(), externalRef, newRef);
				}
			}
		} else if (node.isArray()) {
			ArrayNode arrayNode = (ArrayNode) node;
			for (int i = 0; i < arrayNode.size(); i++) {
				rewriteSchemaReferences(arrayNode.get(i), externalRef, newRef);
			}
		}
	}

	/**
	 * Inner class to represent an external schema reference.
	 */
	private static class ExternalSchemaRef {
		private String schemaName;
		private String schemaUrl;
		private String definitionPath;

		public ExternalSchemaRef(String schemaName, String schemaUrl, String definitionPath) {
			this.schemaName = schemaName;
			this.schemaUrl = schemaUrl;
			this.definitionPath = definitionPath;
		}

		public String getSchemaName() {
			return schemaName;
		}

		public String getSchemaUrl() {
			return schemaUrl;
		}

		public String getDefinitionPath() {
			return definitionPath;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			ExternalSchemaRef other = (ExternalSchemaRef) obj;
			return schemaUrl.equals(other.schemaUrl) && definitionPath.equals(other.definitionPath);
		}

		@Override
		public int hashCode() {
			return (schemaUrl + definitionPath).hashCode();
		}
	}
}
