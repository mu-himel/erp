package com.agi.aesl.erpscm.statistics.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController extends BaseController {


    private final StatisticsService statisticsService;

    @GetMapping("/demand")
    public ResponseEntity<Object> getDemandStatistics(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("fromDate") Optional<String> fromDateStr,
            @RequestParam("toDate") Optional<String> toDateStr
            ){
        return new ResponseEntity<>(statisticsService
                    .getDemandStatistics(token, categoryId,subCategoryId,fromDateStr,toDateStr),HttpStatus.OK);
    }
}
