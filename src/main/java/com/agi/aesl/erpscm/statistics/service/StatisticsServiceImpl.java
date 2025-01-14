package com.agi.aesl.erpscm.statistics.service;

import com.agi.aesl.erpscm.statistics.repository.DemandStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService{

    @Autowired
    private DemandStatisticsRepository demandStatisticsRepository;

    @Override
    public List<?> getDemandStatistics() {
        return demandStatisticsRepository.getDemandStatistics(null,null);
    }
}
