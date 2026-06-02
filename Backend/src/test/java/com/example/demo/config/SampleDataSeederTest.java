package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SampleDataSeederTest {
    @Test
    void capToNotAfterClampsFutureTimestampsToUpperBound() {
        LocalDateTime upperBound = LocalDateTime.of(2026, 6, 2, 12, 0);
        LocalDateTime futureCandidate = LocalDateTime.of(2026, 6, 3, 9, 30);

        assertEquals(upperBound, SampleDataSeeder.capToNotAfter(futureCandidate, upperBound));
    }

    @Test
    void capToNotAfterKeepsPastTimestampsUnchanged() {
        LocalDateTime upperBound = LocalDateTime.of(2026, 6, 2, 12, 0);
        LocalDateTime pastCandidate = LocalDateTime.of(2026, 6, 1, 18, 45);

        assertEquals(pastCandidate, SampleDataSeeder.capToNotAfter(pastCandidate, upperBound));
    }

    @Test
    void seedDocumentTimestampNeverExceedsUpperBound() {
        LocalDateTime upperBound = LocalDateTime.of(2026, 4, 1, 10, 0);

        assertEquals(upperBound, SampleDataSeeder.seedDocumentTimestamp(0, upperBound));
    }
}
