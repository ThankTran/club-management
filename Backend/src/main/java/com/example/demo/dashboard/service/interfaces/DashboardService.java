package com.example.demo.dashboard.service.interfaces;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

public interface DashboardService {
    Map<String, Object> getOverview();
    Map<String, Object> getOverview(LocalDate fromDate, LocalDate toDate);

    List<Map<String, Object>> getStats();

    List<Map<String, Object>> getNotifications();
}
