/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.rule;

public interface RuleRepository {
    RuleSet load(String sourceFormat, String sourceVersion, String targetFormat, String targetVersion);
}
