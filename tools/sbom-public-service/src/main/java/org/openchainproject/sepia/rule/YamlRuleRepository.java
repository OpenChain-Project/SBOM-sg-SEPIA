/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.rule;

import org.openchainproject.sepia.exception.InvalidRuleException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
public class YamlRuleRepository implements RuleRepository {
    private final ObjectMapper mapper;
    private final String baseLocation;

    public YamlRuleRepository(@Value("${sbom.rules.base-location:classpath:/rules}") String baseLocation) {
        this.baseLocation = baseLocation;
        this.mapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    @Override
    public RuleSet load(String sourceFormat, String sourceVersion, String targetFormat, String targetVersion) {
        String normalizedSource = sourceFormat.toLowerCase(Locale.ROOT);
        String file = String.format("%s/%s/%s/%s-to-%s-%s.yaml", baseLocation, normalizedSource,sourceVersion,normalizedSource + "-" +sourceVersion, targetFormat, targetVersion);
        Resource resource = new org.springframework.core.io.DefaultResourceLoader().getResource(file);
        if (!resource.exists()) {
            throw new InvalidRuleException("No ruleset found for " + sourceFormat + " " + sourceVersion + " -> " + targetFormat + " " + targetVersion + ": " + file);
        }
        try {
            return mapper.readValue(resource.getInputStream(), RuleSet.class);
        } catch (IOException e) {
            throw new InvalidRuleException("Cannot load ruleset: " + file, e);
        }
    }
}
