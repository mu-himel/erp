package com.agi.aesl.erpscm.statistics.service;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.statistics.repository.DemandStatisticsRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService{


    private final ClaimResolver claimResolver;

    private final DemandStatisticsRepository demandStatisticsRepository;

    private LocalDateTime parseDate(Optional<String> dateStr,String endTime){
        LocalDateTime date = null;
        if(dateStr.isPresent()){
            String time = (endTime!=null && endTime.trim().length()==8)? "T"+endTime:"T00:00:00";
            date = LocalDateTime.parse(dateStr.get()+time);
        }
        return date;
    }

    private Long getEmpWarehouseId(){
        Employee employee =  claimResolver.getEmployee().orElse(null);
        return (employee!=null)? employee.getWarehouseId() : null;
    }

    @Override
    public List<DemandStatisticsRepository.DemandStats> getDemandStatistics(Jwt token, Optional<Long> categoryId,
                                                                            Optional<Long> subCategoryId,
                                                                            Optional<String> fromDateStr,
                                                                            Optional<String> toDateStr) {
        claimResolver.setToken(token);
        Long warehouseId=null;
        if(claimResolver.getEmployee().isPresent()){
            warehouseId = getEmpWarehouseId();
        }
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        return demandStatisticsRepository.getDemandStatistics(categoryId.orElse(null),
                subCategoryId.orElse(null),warehouseId,fromDate,toDate);
    }
}
