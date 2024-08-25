package com.agi.aesl.erpscm.product_requirements.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.agi.aesl.erpscm.demand.service.DemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
//        productRequirement.setDemandDeadline(getPRDeadline(
//            productRequirementRequestDto.getDemandDate(),
//            productRequirementRequestDto.getDemandPriority()));
        productRequirement.setDemandDeadline(
                productRequirementRequestDto.getDemandDate().atTime(LocalDateTime.now().toLocalTime()));
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

    @Override
    public Page<?> getAllProductRequirements(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<Long> categoryId,
            Optional<Long> subCategoryId, Optional<LocalDateTime> startDate, Optional<LocalDateTime> endDate) {

        claimResolver.setToken(token);
        String uri="";

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Page<?> result = null;
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        result = productRequirementRepository.findAllProductRequirements(categoryId.orElse(null),
        subCategoryId.orElse(null),
        startDate.orElse(null),
        endDate.orElse(null),
        pageable);
        return result;
    }

    @Override
    public List<?> getAllProductRequirementView(Optional<Long> categoryId, Optional<Long> subCategoryId) {
        List<?> result = productRequirementRepository.getAllProductRequirementView(
            categoryId.orElseThrow(()-> new RuntimeException("Sorry! Category should not empty")),
            subCategoryId.orElseThrow(()->new RuntimeException("Sorry! Sub Category should not empty"))
        );
        return result;
    }

    @Override
    public List<?> getWarehouseRequirements(String attribute) {
        return productRequirementRepository.getWarehouseRequirements(attribute);
    }

    @Override
    @Transactional
    public void reOpen(String productRequirementsIds) {
        List<Long> ids = List.of(productRequirementsIds.split(",")).stream()
        .map(id->Long.parseLong(id))
        .collect(Collectors.toList());
        productRequirementRepository.updateStatusByIds(ids);
    }

    @Override
    public int updateStatusByCategoryAndSubCategory(ProductRequirementStatus toStatus,
            ProductRequirementStatus fromStatus, Long categoryId, Long subCategoryId) {
        int result = productRequirementRepository
                    .updateStatusByCategoryAndSubCategory(toStatus,fromStatus,
                        categoryId, subCategoryId);
        return result;
    }

    @Override
    public List<?> getDemandByProductRequirementIds(String prIds) {
        List<Long> ids = Arrays.stream(prIds.split(",")).map(s -> Long.parseLong(s)).collect(Collectors.toList());

        Optional<List<Long>> indentIds = Optional.of(ids);
        return productRequirementRepository.getDemandByProductRequirementIds(
                indentIds.orElseThrow(() -> new RuntimeException("ids is missing")));
    }
}
