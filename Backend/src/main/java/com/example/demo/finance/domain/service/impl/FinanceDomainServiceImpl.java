package com.example.demo.finance.domain.service.impl;

import com.example.demo.finance.domain.service.interfaces.FinanceDomainService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class FinanceDomainServiceImpl implements FinanceDomainService {
    @Override
    public void validateTimeRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Time range must not be empty");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End time must be on or after start time");
        }
    }

    @Override
    public void validateEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
    }
}
