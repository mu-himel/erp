package com.agi.aesl.erpscm.product_requirements.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.enums.DemandPriority;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.product_requirements.dto.request.ProductRequirementRequestDto;
import com.agi.aesl.erpscm.product_requirements.entity.ProductRequirement;
import com.agi.aesl.erpscm.product_requirements.enums.ProductRequirementStatus;
import com.agi.aesl.erpscm.product_requirements.repository.ProductRequirementRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;

import jakarta.transaction.Transactional;

@Service
public class ProductRequirementServiceImpl implements ProductRequirementService{

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private ProductRequirementRepository productRequirementRepository;

    @Override
    @Transactional
    public void createProductRequirement(Jwt token, ProductRequirementRequestDto productRequirementRequestDto) {
        claimResolver.setToken(token);
        Optional<Employee> empOp = claimResolver.getEmployee();
        if(empOp.isEmpty()){
            throw new RuntimeException("sorry! employee not found");
        }
        
        ProductRequirement productRequirement = productRequirementRequestDto.getEntity();
        productRequirement.setDemandDeadline(getPRDeadline(
            productRequirementRequestDto.getDemandDate(), 
            productRequirementRequestDto.getDemandPriority()));

        productRequirement.setRequestedBy(empOp.get());
        productRequirement.setWarehouse(new Warehouse(productRequirementRequestDto.getWarehouse().getId()));
        productRequirement.setStatus(ProductRequirementStatus.OPEN);
        productRequirementRepository.save(productRequirement);
    }

    private LocalDateTime getPRDeadline(LocalDateTime demandDate, DemandPriority demandPriority) {
        if (demandPriority.equals(DemandPriority.URGENT)) {
            return demandDate.plusDays(7L);
        } else if (demandPriority.equals(DemandPriority.MEDIUM)) {
            return demandDate.plusDays(14L);
        } else if (demandPriority.equals(DemandPriority.REGULAR)) {
            return demandDate.plusDays(20L);
        } else {
            return LocalDateTime.now();
        }
    }

    
}
