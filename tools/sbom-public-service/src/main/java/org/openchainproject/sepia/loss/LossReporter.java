/*
 Parts of this file are created by genAI by using GitHub Copilot. 
 This notice needs to remain attached to any reproduction of or excerpt from this file.
 */

// SPDX-FileCopyrightText: Copyright (C) 2026 Contributors to SEPIA
//
// SPDX-License-Identifier: MIT

package org.openchainproject.sepia.loss;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LossReporter {
    private final AtomicLong sequence = new AtomicLong();
    private final List<LossEvent> events = new ArrayList<>();
    public void report(LossEvent event) { if (event.getEventId() == null) event.setEventId("LOSS-%04d".formatted(sequence.incrementAndGet())); synchronized (events){events.add(event);} }
    public List<LossEvent> snapshot() { synchronized (events){ return new ArrayList<>(events); } }
    public void clear() { synchronized (events){ events.clear(); } }
}
