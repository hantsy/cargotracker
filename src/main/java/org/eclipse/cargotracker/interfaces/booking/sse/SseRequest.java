package org.eclipse.cargotracker.interfaces.booking.sse;

import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

public record SseRequest(Sse sse, SseEventSink sink) {}
