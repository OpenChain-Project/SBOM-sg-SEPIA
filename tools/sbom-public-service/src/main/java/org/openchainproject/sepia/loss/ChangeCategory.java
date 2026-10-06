/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT
package org.openchainproject.sepia.loss;

/** Classifies how a value differs between the source and the converted target SBOM. */
public enum ChangeCategory {
    /** Source value with no representation in the target; dropped during conversion. */
    LOST,
    /** Value present in the target but with no corresponding source value; added during conversion. */
    ADDED,
    /** Source value transformed before landing in the target; the original value does not appear verbatim. */
    MODIFIED
}
