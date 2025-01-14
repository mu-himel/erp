package com.agi.aesl.erpscm.statistics.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.statistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/statistics")
public class StatisticsController extends BaseController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/demand")
    public ResponseEntity<?> getDemandStatistics(){
        return new ResponseEntity<>(statisticsService.getDemandStatistics(),HttpStatus.OK);
    }
}
