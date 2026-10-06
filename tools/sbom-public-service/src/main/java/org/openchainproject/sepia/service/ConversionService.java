/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.service;

import org.openchainproject.sepia.exception.ConversionException;
import org.openchainproject.sepia.exception.InvalidRuleException;
import org.openchainproject.sepia.loss.LossReporter;
import org.openchainproject.sepia.loss.ConversionDeltaAnalyzer;
import org.openchainproject.sepia.model.BomFilesInputModel;
import org.openchainproject.sepia.model.ChangeLog;
import org.openchainproject.sepia.rule.RuleRepository;
import org.openchainproject.sepia.rule.RuleSet;
import org.openchainproject.sepia.rule.RuleValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ConversionService {
    private final ObjectMapper mapper;
    private final RuleRepository repository;
    private final RuleValidator validator;
    private final RuleEngine engine;
    private final LossReporter lossReporter;
    private final ConversionDeltaAnalyzer conversionDeltaAnalyzer;

    public ConversionService(ObjectMapper mapper, RuleRepository repository, RuleValidator validator, RuleEngine engine, LossReporter lossReporter, ConversionDeltaAnalyzer conversionDeltaAnalyzer) {
        this.mapper=mapper; this.repository=repository; this.validator=validator; this.engine=engine; this.lossReporter=lossReporter; this.conversionDeltaAnalyzer=conversionDeltaAnalyzer;
    }
    
    /**
     * Converts the source SBOM content from the specified source format and version to the target format and version.
     * @param source
     * @param sourceFormat
     * @param sourceVersion
     * @param targetFormat
     * @param targetVersion
     * @return
     */
    public BomFilesInputModel convert(JsonNode source, String sourceFormat, String sourceVersion, String targetFormat, String targetVersion) {
        String sf = sourceFormat + "-" + sourceVersion;
        String tf = targetFormat + "-" + targetVersion;
        if (source == null || source.isNull()) {
            throw new ConversionException("Source SBOM content is missing for conversion " + sf + " -> " + tf);
        }
        RuleSet rules;
        try {
            lossReporter.clear();
            rules = repository.load(sourceFormat, sourceVersion, targetFormat, targetVersion);
            validator.validate(rules);
        } catch (InvalidRuleException e) {
            throw new ConversionException("Cannot convert " + sf + " to " + tf + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ConversionException("Unexpected error while preparing the ruleset for " + sf + " -> " + tf + ": " + e.getMessage(), e);
        }

        JsonNode target = mapper.createObjectNode();
        try {
            engine.execute(rules, source, target);
        } catch (ConversionException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ConversionException("Conversion from " + sf + " to " + tf + " failed: " + e.getMessage(), e);
        }

        try {
            BomFilesInputModel result = new BomFilesInputModel();
            result.setSbomJsonString(target.toString());
            result.setSbomJson(target);
            result.setLossEvent(lossReporter.snapshot());
            result.setConversionDeltas(conversionDeltaAnalyzer.analyze(rules, source, target, sf, tf));
            result.setSbomFileName("convertedSbomContent.json");
            result.setMessage("Conversion completed successfully.");
            List<ChangeLog> changeLogsList = new ArrayList<>();
            result.setChangeLogsList(changeLogsList);
            return result;
        } catch (RuntimeException e) {
            throw new ConversionException("Failed to assemble the conversion result for " + sf + " -> " + tf + ": " + e.getMessage(), e);
        }
    }
}
