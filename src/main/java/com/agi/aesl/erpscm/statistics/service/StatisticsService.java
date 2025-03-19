package com.agi.aesl.erpscm.statistics.service;

import com.agi.aesl.erpscm.statistics.repository.DemandStatisticsRepository;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface StatisticsService {
    List<DemandStatisticsRepository.DemandStats> getDemandStatistics(Jwt token, Optional<Long> categoryId,
                                                                    Optional<Long> subCategoryId,
                                                                    Optional<String> fromDateStr,
                                                                    Optional<String> toDateStr);
    
}
