// SPDX-FileCopyrightText: Copyright (C) 2025 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package com.sepia.sbomutils.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sepia.sbomutils.model.BomFilesInputModel;
import com.sepia.sbomutils.model.ChangeLog;
import com.sepia.sbomutils.service.SbomUtilityService;
import com.sepia.sbomutils.util.Constants;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 
 * 
 *
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SbomUtilityController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SbomUtilityController.class);
    

	
	@Autowired
	private SbomUtilityService sbomUtilityService;
	
	

    
	
	@ApiOperation(value = "Health check for SBOM Validator API")
	@GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth(@RequestHeader HttpHeaders headers) {
        // Perform any necessary health checks here
        // For simplicity, we'll just return a 200 OK status
		Map<String, String> response = new HashMap<>();
		
		List<String> ivUserList = headers.get("iv-user");
		String userNtId = null;
		if (ivUserList != null && !ivUserList.isEmpty()) {
			userNtId = ivUserList.get(0);
		}
		
        response.put("status", "Server is running");
        response.put("userId", userNtId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

	@ApiIgnore
	@PostMapping(path = "/uploadInputFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public BomFilesInputModel uploadInputFile(@RequestParam("file") Optional<MultipartFile[]> inputFile,
			@RequestParam("postData") String postData) {
		BomFilesInputModel sbomInputModel = null;
		try {
			ObjectMapper mapper = new ObjectMapper();

			sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			sbomInputModel = sbomUtilityService.uploadInputFile(inputFile, sbomInputModel, false);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the given information inside uploadInputFile()", e);
		}
		return sbomInputModel;
	}

	@ApiIgnore
	@PostMapping(path = "/deleteSbomEntry")
	public BomFilesInputModel deleteSbomEntry(@RequestParam("postData") String postData) {

		BomFilesInputModel sbomInputModel = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);

			sbomInputModel = sbomUtilityService.deleteSbomEntry(sbomInputModel);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the given information inside deleteSbomEntry()", e);
		}
		return sbomInputModel;
	}

	@ApiIgnore
	@PostMapping(path = "/clearSession")
	public void clearSession(@RequestParam("postData") String postData) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			
			BomFilesInputModel sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			sbomUtilityService.clearSession(sbomInputModel);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the given information inside clearSession()", e);
		}
	}

	@ApiIgnore
	@PostMapping(path = "/validateSboms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public BomFilesInputModel validateSboms(@RequestParam("postData") String postData) {
		BomFilesInputModel sbomInputModel = null;
		try {
			ObjectMapper mapper = new ObjectMapper();

			sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			sbomInputModel = sbomUtilityService.validateSboms(sbomInputModel, false, false);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the given information inside validateSboms()", e);
	        if (sbomInputModel == null) {
	            throw new NullPointerException("sbomInputModel is null due to an error during deserialization or validation.");
	        }
			sbomInputModel.setMessage("Error uploading file: " + e.getMessage());
			sbomInputModel.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return sbomInputModel;
	}

	@ApiIgnore
	@PostMapping(path = "/fetchErrorDetails")
	public BomFilesInputModel fetchErrorDetails(@RequestParam("postData") String postData) {
		BomFilesInputModel sbomInputModel = new BomFilesInputModel();
		try {

			ObjectMapper mapper = new ObjectMapper();

			sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			sbomInputModel = sbomUtilityService.fetchErrorDetails(sbomInputModel);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the given information inside fetchErrorDetails()", e);
		}

		return sbomInputModel;
	}

	@ApiIgnore
	@PostMapping("/fetchJsonContent")
	public BomFilesInputModel fetchJsonContent(@RequestParam("postData") String postData) {
		BomFilesInputModel sbomInputModel = new BomFilesInputModel();
		try {

			ObjectMapper mapper = new ObjectMapper();

			sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			sbomInputModel = sbomUtilityService.fetchJsonContent(sbomInputModel);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the given information inside fetchJsonContent()", e);
		}

		return sbomInputModel;
	}

	@ApiOperation(value = "Merge 2 or more valid CycloneDx V1.4 SBOMs with this API")
	@PostMapping("/mergeCyclonedx")
	public BomFilesInputModel mergeCyclonedxSboms(@RequestParam("postData") String postData,
			@RequestParam("bomMetadata") String bomMetadata, @RequestParam(value = "isFromApp", required = false) boolean isFromApp) {
		List<BomFilesInputModel> bomInputList = new ArrayList<>();
		BomFilesInputModel mergedBomInput = new BomFilesInputModel();

		try {
			ObjectMapper mapper = new ObjectMapper();

			bomInputList = mapper.readValue(postData, new TypeReference<List<BomFilesInputModel>>() {
			});
			
			mergedBomInput = sbomUtilityService.mergeSboms(bomInputList, bomMetadata, Constants.CYCLONEDX_LC, isFromApp);
			if(!isFromApp) {
				mergedBomInput.setSbomJsonString(null);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while merging the selected SBOMs inside mergeCyclonedxSboms()", e);
		}
		return mergedBomInput;
	}

	@ApiOperation(value = "Merge 2 or more valid SPDX V2.3 SBOMs with this API")
	@PostMapping("/mergeSpdx")
	public BomFilesInputModel mergeSpdxSboms(@RequestParam("postData") String postData,
			@RequestParam("bomMetadata") String bomMetadata, @RequestParam(value = "isFromApp", required = false) boolean isFromApp) {
		List<BomFilesInputModel> bomInputList = new ArrayList<>();
		BomFilesInputModel mergedBomInput = new BomFilesInputModel();

		try {
			ObjectMapper mapper = new ObjectMapper();

			bomInputList = mapper.readValue(postData, new TypeReference<List<BomFilesInputModel>>() {
			});

			mergedBomInput = sbomUtilityService.mergeSboms(bomInputList, bomMetadata, Constants.SPDX, isFromApp);
			if(!isFromApp) {
				mergedBomInput.setSbomJsonString(null);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while merging the selected SBOMs inside mergeSpdxSboms() ", e);
		}
		return mergedBomInput;
	}
	
	

	@ApiIgnore
	@PostMapping(path = "/replaceFile")
	public BomFilesInputModel replaceFile(@RequestParam("postData") String postData) {
		BomFilesInputModel sbomInputModel = new BomFilesInputModel();
		try {

			ObjectMapper mapper = new ObjectMapper();

			sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			sbomInputModel = sbomUtilityService.replaceFile(sbomInputModel);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the replacing file with updated content", e);
		}

		return sbomInputModel;
	}

	@ApiIgnore
	@PostMapping(path = "/getJsonDifferences")
	public List<ChangeLog> getJsonDifferences(@RequestParam("currentValue") String currentValue,
			@RequestParam("postData") String postData,@RequestParam("isMerge") Boolean isMerge) {

		List<ChangeLog> changeLogsList = new ArrayList<>();
		BomFilesInputModel sbomInputModel = new BomFilesInputModel();
		try {
			ObjectMapper mapper = new ObjectMapper();
			sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			changeLogsList = sbomUtilityService.getJsonDifferences(currentValue,sbomInputModel,isMerge);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the replacing file with updated content inside getJsonDifferences()", e);
		}

		return changeLogsList;
	}

	@ApiIgnore
	@PostMapping(path = "/prepareForDownload")
	public BomFilesInputModel prepareForDownload(@RequestParam("currentValue") String currentValue,
			@RequestParam("postData") String postData) {
		ObjectMapper mapper = new ObjectMapper();

		BomFilesInputModel bomFilesInputModel = null;
		try {
			
			bomFilesInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			bomFilesInputModel = sbomUtilityService.prepareForDownload(currentValue, bomFilesInputModel);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the preparing content for download", e);
		}
		
		return bomFilesInputModel;
	}
	
	@ApiOperation(value = "Upload an input SBOM of type CycloneDx V1.4 or SPDX V2.3 and get it validated")
	@PostMapping(path = "/uploadAndValidate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public BomFilesInputModel uploadAndValidate(@RequestParam("file") Optional<MultipartFile[]> inputFile,
			@RequestParam("postData") String postData) {
		BomFilesInputModel sbomInputModel = null;
		try {
			ObjectMapper mapper = new ObjectMapper();

			sbomInputModel = mapper.readValue(postData, BomFilesInputModel.class);
			sbomInputModel = sbomUtilityService.uploadInputFile(inputFile, sbomInputModel, true);
		} catch (Exception e) {
			LOGGER.error("Exception occurred while the given information inside uploadAndValidate()", e);
		}
		return sbomInputModel;
	}
	
	@ApiIgnore
	@GetMapping(value = "/downloadUserManual", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void downloaderUnattended(HttpServletRequest request, HttpServletResponse response) {
 
		try {
			response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=User_Manual_Beta.pdf");
			IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("User_Manual_Beta.pdf"),
					response.getOutputStream());
			response.getOutputStream().flush();
		} catch (IOException e) {
			LOGGER.error("Error occured during file download - downloaderUnattended() :", e);
		}
 
	}

}
