package com.agi.aesl.erpscm.product_requirements.service;

import java.time.LocalDateTime;
import java.util.*;

import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.repository.DemandDetailRepository;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.product_requirements.dto.response.PrItemInfo;
import com.agi.aesl.erpscm.product_requirements.dto.response.PrWarehouseInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequiredArgsConstructor
public class ProductRequirementServiceImpl implements ProductRequirementService{


    private final ClaimResolver claimResolver;


    private final ProductRequirementRepository productRequirementRepository;


    private final DemandDetailRepository demandDetailRepository;


    @Override
    @Transactional
    public void createProductRequirement(Jwt token, ProductRequirementRequestDto productRequirementRequestDto) {
        claimResolver.setToken(token);
        Optional<Employee> empOp = claimResolver.getEmployee();
        if(empOp.isEmpty()){
            throw new AesException("sorry! employee not found");
        }
        Optional<DemandDetail> ddOp = demandDetailRepository.findById(productRequirementRequestDto.getDemandDetail().getId());
        if(ddOp.isPresent()){
            DemandDetail demandDetail = ddOp.get();
            demandDetail.setPrQty(productRequirementRequestDto.getDemandDetail().getPrQty());
        }
        ProductRequirement productRequirement = productRequirementRequestDto.getEntity();

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

    private LocalDateTime parseDate(Optional<String> dateStr,String endTime){
        LocalDateTime date = null;
        if(dateStr.isPresent()){
            String time = (endTime!=null && endTime.trim().length()==8)? "T"+endTime:"T00:00:00";
            date = LocalDateTime.parse(dateStr.get()+time);
        }
        return date;
    }
    @Override
    public Page<ProductRequirementRepository.ProductRequirementInfo> getAllProductRequirements(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<Long> categoryId,
                                                                                               Optional<Long> subCategoryId, Optional<String> startDate, Optional<String> endDate,
                                                                                               Optional<Integer> daysRemain) {

        claimResolver.setToken(token);

        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10));
        LocalDateTime fromDate = parseDate(startDate,null);
        LocalDateTime toDate = parseDate(endDate,"23:59:59");
        return productRequirementRepository.findAllProductRequirements(categoryId.orElse(null),
        subCategoryId.orElse(null),
            fromDate,
            toDate,
        daysRemain.orElse(null),
        pageable);
    }

    @Override
    public List<PrItemInfo> getAllProductRequirementView(Optional<Long> categoryId, Optional<Long> subCategoryId) {
        List<ProductRequirementRepository.ProductRequirementViewInfoV2> result = productRequirementRepository.getAllProductRequirementView(
            categoryId.orElseThrow(()-> new RuntimeException("Sorry! Category should not empty")),
            subCategoryId.orElseThrow(()->new RuntimeException("Sorry! Sub Category should not empty"))
        );

        Map<String, PrItemInfo> map = new HashMap<>();

        for(ProductRequirementRepository.ProductRequirementViewInfoV2 res: result){
            String key=res.getBrandName()+" - "+res.getItemName();
            if(map.containsKey(key)){
                PrItemInfo itemInfo = map.get(key);
                PrWarehouseInfo warehouseInfo= new PrWarehouseInfo(res.getWarehouseIds(),
                        res.getWarehouses(),res.getCurrentStock(),res.getSafetytStock(),res.getPrQty(),
                        res.getTransitQty(),res.getItemsQty());
                List<PrWarehouseInfo> warehouses = itemInfo.getWarehouses();
                List<PrWarehouseInfo> prWarehouses = new ArrayList<>();
                for(PrWarehouseInfo w: warehouses){
                    if(w.getWarehouseIds().equals(res.getWarehouseIds())){
                        w.setPrQty(w.getPrQty().add(res.getPrQty()));
                    }else{
                        prWarehouses.add(warehouseInfo);

                    }
                }
                prWarehouses.forEach(w->
                    itemInfo.setWarehouses(warehouseInfo)
                );
                itemInfo.setProductRequirementIds(res.getProductRequirementsIds());
                itemInfo.setPrQty(itemInfo.getPrQty().add(res.getPrQty()));
                itemInfo.setDaysRemain(res.getDaysRemain());
            }else{

                PrWarehouseInfo warehouseInfo=new PrWarehouseInfo(res.getWarehouseIds(),
                        res.getWarehouses(),res.getCurrentStock(),res.getSafetytStock(),res.getPrQty(),
                        res.getTransitQty(),res.getItemsQty());

                PrItemInfo itemInfo  = new PrItemInfo(res.getProductRequirementsIds(),res.getBrandName(),
                        res.getCategoryName(),res.getSubCategoryName(),
                        res.getItemName(),res.getPrQty());
                itemInfo.setWarehouses(warehouseInfo);
                itemInfo.setDaysRemain(res.getDaysRemain());
                itemInfo.setBrandId(res.getBrandId());
                itemInfo.setPriorityDate(res.getDemandDeadline());
                map.put(key,itemInfo);
            }
        }
        return map.values().stream().toList();
    }

    @Override
    public List<ProductRequirementRepository.WarehouseRequirement> getWarehouseRequirements(String attribute) {
        return productRequirementRepository.getWarehouseRequirements(attribute);
    }

    @Override
    @Transactional
    public void reOpen(String productRequirementsIds) {
        List<Long> ids = List.of(productRequirementsIds.split(",")).stream()
        .map(Long::parseLong)
        .toList();
        productRequirementRepository.updateStatusByIds(ids);
    }

    @Override
    public int updateStatusByCategoryAndSubCategory(ProductRequirementStatus toStatus,
            ProductRequirementStatus fromStatus, Long categoryId, Long subCategoryId) {
        return  productRequirementRepository
                    .updateStatusByCategoryAndSubCategory(toStatus,fromStatus,
                        categoryId, subCategoryId);

    }

    @Override
    public List<ProductRequirementRepository.PrDemandView> getDemandByProductRequirementIds(String prIds) {
        List<Long> ids = Arrays.stream(prIds.split(",")).map(Long::parseLong).toList();

        Optional<List<Long>> indentIds = Optional.of(ids);
        return productRequirementRepository.getDemandByProductRequirementIds(
                indentIds.orElseThrow(() -> new RuntimeException("ids is missing")));
    }
}
