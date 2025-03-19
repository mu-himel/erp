package com.agi.aesl.erpscm.statistics.service;

import com.agi.aesl.erpscm.statistics.repository.DemandStatisticsRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StatisticsServiceImpl implements StatisticsService{

    @Autowired
    private ClaimResolver claimResolver;
    @Autowired
    private DemandStatisticsRepository demandStatisticsRepository;

    private LocalDateTime parseDate(Optional<String> dateStr,String endTime){
        LocalDateTime date = null;
        if(dateStr.isPresent()){
            String time = (endTime!=null && endTime.trim().length()==8)? "T"+endTime:"T00:00:00";
            date = LocalDateTime.parse(dateStr.get()+time);
        }
        return date;
    }
    @Override
    public List<DemandStatisticsRepository.DemandStats> getDemandStatistics(Jwt token, Optional<Long> categoryId,
                                                                            Optional<Long> subCategoryId,
                                                                            Optional<String> fromDateStr,
                                                                            Optional<String> toDateStr) {
        claimResolver.setToken(token);
        Long warehouseId=null;
        if(claimResolver.getEmployee().isPresent()){
            warehouseId = claimResolver.getEmployee().get().getWarehouseId();
        }
        System.out.println("WAREHOUSE: "+warehouseId);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        return demandStatisticsRepository.getDemandStatistics(categoryId.orElse(null),
                subCategoryId.orElse(null),warehouseId,fromDate,toDate);
    }
}
