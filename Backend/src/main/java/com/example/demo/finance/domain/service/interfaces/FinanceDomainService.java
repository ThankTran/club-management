package com.example.demo.finance.domain.service.interfaces;

import java.time.LocalDateTime;

public interface FinanceDomainService {
    void validateTimeRange(LocalDateTime from, LocalDateTime to);

    void validateEventId(String eventId);
}
