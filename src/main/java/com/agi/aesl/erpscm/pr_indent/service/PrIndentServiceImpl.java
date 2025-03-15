package com.agi.aesl.erpscm.pr_indent.service;


import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.service.CategoryService;
import com.agi.aesl.erpscm.pr_indent.dto.reqeust.PrIndentRequestDto;
import com.agi.aesl.erpscm.pr_indent.dto.reqeust.UpdatePrIndentDetailRequestDto;
import com.agi.aesl.erpscm.pr_indent.entity.PrIndent;
import com.agi.aesl.erpscm.pr_indent.entity.PrIndentPartialDelivery;
import com.agi.aesl.erpscm.pr_indent.entity.PrIndentWarehouseDetail;
import com.agi.aesl.erpscm.pr_indent.enums.PrIndentStatus;
import com.agi.aesl.erpscm.pr_indent.repository.PrIndentPartialDeliveryRepository;
import com.agi.aesl.erpscm.pr_indent.repository.PrIndentRepository;
import com.agi.aesl.erpscm.pr_indent.repository.PrIndentWarehouseDetailRepository;
import com.agi.aesl.erpscm.product_requirements.enums.ProductRequirementStatus;
import com.agi.aesl.erpscm.product_requirements.service.ProductRequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PrIndentServiceImpl implements PrIndentService {


    private final ProductRequirementService productRequirementService;


    private final PrIndentRepository prIndentRepository;


    private final CategoryService categoryService;


    private final PrIndentWarehouseDetailRepository prIndentWarehouseDetailRepository;


    private final PrIndentPartialDeliveryRepository prIndentPartialDeliveryRepository;

    @Override
    public void createPrIndent(Jwt token, PrIndentRequestDto prIndentRequestDto) {
        PrIndent prIndent = prIndentRequestDto.getEntity();
        prIndent.setStatus(PrIndentStatus.OPEN);

        categoryService.validateCategorySubCategoryRelation(
                prIndent.getCategory(),
                prIndent.getSubCategory()
        );

        //Save PrIndent
        prIndentRepository.save(prIndent);

        //Update Product Requirement Status
        productRequirementService.updateStatusByCategoryAndSubCategory(ProductRequirementStatus.CLOSE,
                ProductRequirementStatus.OPEN,
                prIndent.getCategory().getId(),
                prIndent.getSubCategory().getId());

    }

    private LocalDateTime parseDate(Optional<String> dateStr, String endTime){
        LocalDateTime date = null;
        if(dateStr.isPresent()){
            String time = (endTime!=null && endTime.trim().length()==8)? "T"+endTime:"T00:00:00";
            date = LocalDateTime.parse(dateStr.get()+time);
        }
        return date;
    }

    @Override
    public Page<?> getAllPrIndents(Optional<Integer> page,
                                   Optional<Integer> size,
                                   Optional<Long> categoryId,
                                   Optional<Long> subCategoryId,
                                   Optional<String> fromDateStr,
                                   Optional<String> toDateStr
                                   ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(10), sort);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        return prIndentRepository.getAllPrIndents(
                categoryId.orElse(null),
                subCategoryId.orElse(null),
                fromDate,
                toDate,
                pageable);
    }

    @Override
    public List<?> getPrIndentById(Optional<Long> prIndentId) {
        List<PrIndentRepository.PrIndentViewInfo> result = prIndentRepository.getPrIndentById(
                prIndentId.orElseThrow(() -> new RuntimeException("PrIndent id should not empty"))
        );

        return getProcessedResult(result);
    }

    @Override
    public List<?> getPrIndentByIds(Optional<List<Long>> prIndentIds) {
        List<PrIndentRepository.PrIndentViewInfo> result = prIndentRepository.getPrIndentByIds(
                prIndentIds.orElseThrow(() -> new RuntimeException("PrIndent id should not empty"))
        );
        return getProcessedResults(result);
    }

    private List<?> getProcessedResult(List<PrIndentRepository.PrIndentViewInfo> result) {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> warehousKeyMap = new HashMap<>();
        for (PrIndentRepository.PrIndentViewInfo prIndentViewInfo : result) {

            Map<String, Object> item = new HashMap<>();
            String warehouseKey = prIndentViewInfo.getBrandName()+"_"+prIndentViewInfo.getPrAttribute() + "_" + prIndentViewInfo.getWarehouseId();

            Optional<Map<String, Object>> anyItemOp = items.stream().filter(itm -> {
                if (itm.get("brandName") != null) {

                return itm.get("brandName").equals(prIndentViewInfo.getBrandName()) &&
                        itm.get("itemName").equals(prIndentViewInfo.getPrAttribute());
                }else{
                    return itm.get("itemName").equals(prIndentViewInfo.getPrAttribute());
                }
            }).findAny();

            if (anyItemOp.isEmpty()) {
                item.put("id", prIndentViewInfo.getId());
                item.put("productRequirementsIds", prIndentViewInfo.getProductRequirementsIds());
                item.put("prDetailId", prIndentViewInfo.getPrDetailId());

                item.put("itemName", prIndentViewInfo.getPrAttribute());
                item.put("categoryName", prIndentViewInfo.getCategoryName());
                item.put("subCategoryName", prIndentViewInfo.getSubCategoryName());
                item.put("categoryId", prIndentViewInfo.getCategoryId());
                item.put("subCategoryId", prIndentViewInfo.getSubCategoryId());
                item.put("daysRemain", prIndentViewInfo.getDaysRemain());
                item.put("priority", prIndentViewInfo.getPriority());
                item.put("priorityDate", prIndentViewInfo.getPriorityDate());
                item.put("brandId", prIndentViewInfo.getBrandId());
                item.put("brandName", prIndentViewInfo.getBrandName());
                Map<String, Object> warehouseInfo = new HashMap<>();
                warehouseInfo.put("id", prIndentViewInfo.getPiwId());
                warehouseInfo.put("warehouseName", prIndentViewInfo.getWarehouseName());
                warehouseInfo.put("key", prIndentViewInfo.getPrDetailId() + "_" + prIndentViewInfo.getPiwId());
                warehouseInfo.put("warehouseId", prIndentViewInfo.getWarehouseId());
                warehouseInfo.put("orderQty", prIndentViewInfo.getOrderQty());
                warehouseInfo.put("prQty", prIndentViewInfo.getPrQty());


                List<Map<String, Object>> pdList = new ArrayList<>();

                Map<String, Object> pd = new HashMap<>();
                pd.put("id", prIndentViewInfo.getPdId());
                pd.put("pdDate", prIndentViewInfo.getPdDate());
                pd.put("qty", prIndentViewInfo.getPdQty());

                pdList.add(pd);
                if (prIndentViewInfo.getPdDate() != null && prIndentViewInfo.getPdQty() != null) {
                    warehouseInfo.put("partialDeliveries", pdList);
                } else {
                    warehouseInfo.put("partialDeliveries", new ArrayList<>());
                }

                warehousKeyMap.put(warehouseKey, warehouseInfo);
                item.put("warehouses", warehousKeyMap);
                item.put("prQty", prIndentViewInfo.getPrQty());
                items.add(item);
            } else {
                Map<String, Object> existItem = anyItemOp.get();
                Long prDetailId = (Long) existItem.get("prDetailId");
                BigDecimal existingPrQty = (BigDecimal) existItem.get("prQty");

                if (prDetailId.equals(prIndentViewInfo.getPrDetailId())) {
                    existItem.put("prQty", existingPrQty.add(prIndentViewInfo.getPrQty()));

                }
                if (existItem.containsKey("warehouses")) {
                    Map<String, Object> existWarehouseProp = (Map<String, Object>) existItem.get("warehouses");

                    if (existWarehouseProp.containsKey(warehouseKey)) {
                        warehousKeyMap = (Map<String, Object>) existWarehouseProp.get(warehouseKey);
                        String key = (String) warehousKeyMap.get("key");
                        String currentkey = prIndentViewInfo.getPrDetailId() + "_" + prIndentViewInfo.getPiwId();
                        if (!key.contains(currentkey)) {
                            BigDecimal orderQty = (BigDecimal) warehousKeyMap.get("orderQty");
                            orderQty = orderQty.add(prIndentViewInfo.getOrderQty());
                            warehousKeyMap.replace("orderQty", orderQty);
                            BigDecimal wPrQty = (BigDecimal) warehousKeyMap.get("prQty");
                            BigDecimal existingVal = (item.get("prQty") != null) ? (BigDecimal) item.get("prQty") : new BigDecimal(0L);
                            existItem.put("prQty", existingVal.add(wPrQty));
                            wPrQty = wPrQty.add(prIndentViewInfo.getPrQty());
                            warehousKeyMap.replace("prQty", wPrQty);

                            warehousKeyMap.replace("key", currentkey);
                        }

                        List<Map<String, Object>> pdList = (List<Map<String, Object>>) warehousKeyMap.get("partialDeliveries");
                        Map<String, Object> pd = new HashMap<>();
                        pd.put("id", prIndentViewInfo.getPdId());
                        pd.put("pdDate", prIndentViewInfo.getPdDate());
                        pd.put("qty", prIndentViewInfo.getPdQty());
                        pdList.add(pd);
                    } else {
                        Map<String, Object> warehouseInfo = new HashMap<>();
                        warehouseInfo.put("id", prIndentViewInfo.getPiwId());
                        warehouseInfo.put("warehouseName", prIndentViewInfo.getWarehouseName());
                        warehouseInfo.put("key", prIndentViewInfo.getPrDetailId() + "_" + prIndentViewInfo.getPiwId());
                        warehouseInfo.put("warehouseId", prIndentViewInfo.getWarehouseId());
                        warehouseInfo.put("orderQty", prIndentViewInfo.getOrderQty());
                        warehouseInfo.put("prQty", prIndentViewInfo.getPrQty());

                        List<Map<String, Object>> pdList = new ArrayList<>();
                        Map<String, Object> pd = new HashMap<>();
                        pd.put("id", prIndentViewInfo.getPdId());
                        pd.put("pdDate", prIndentViewInfo.getPdDate());
                        pd.put("qty", prIndentViewInfo.getPdQty());
                        pdList.add(pd);
                        if (prIndentViewInfo.getPdDate() != null && prIndentViewInfo.getPdQty() != null) {
                            warehouseInfo.put("partialDeliveries", pdList);
                        } else {
                            warehouseInfo.put("partialDeliveries", new ArrayList<>());
                        }
                        existWarehouseProp.put(warehouseKey, warehouseInfo);
                    }
                }
            }


        }

        return items.stream().map(itm -> {
            Map<String, Object> warehouses = (Map<String, Object>) itm.get("warehouses");
            itm.replace("warehouses", warehouses.values());
            return itm;
        }).toList();

    }

    private List<?> getProcessedResults(List<PrIndentRepository.PrIndentViewInfo> result) {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> warehousKeyMap = new HashMap<>();
        for (PrIndentRepository.PrIndentViewInfo prIndentViewInfo : result) {

            Map<String, Object> item = new HashMap<>();
            String warehouseKey = prIndentViewInfo.getBrandName() + "_" + prIndentViewInfo.getPrAttribute() + "_" + prIndentViewInfo.getWarehouseId();
            Optional<Map<String, Object>> anyItemOp = items.stream().filter(itm -> {
                String itemName = (String) itm.get("itemName");
                String brandName = (String) itm.get("brandName");
                String prAttribute = prIndentViewInfo.getPrAttribute();
                if(brandName!=null){
                    return brandName.equals(prIndentViewInfo.getBrandName()) && itemName.equals(prAttribute);
                }
                return itemName.equals(prAttribute);
            }).findAny();

            if (anyItemOp.isEmpty()) {
                item.put("id", prIndentViewInfo.getId());
                item.put("productRequirementsIds", prIndentViewInfo.getProductRequirementsIds());
                item.put("prDetailId", prIndentViewInfo.getPrDetailId());

                item.put("itemName", prIndentViewInfo.getPrAttribute());
                item.put("categoryName", prIndentViewInfo.getCategoryName());
                item.put("subCategoryName", prIndentViewInfo.getSubCategoryName());
                item.put("categoryId", prIndentViewInfo.getCategoryId());
                item.put("subCategoryId", prIndentViewInfo.getSubCategoryId());
                item.put("daysRemain", prIndentViewInfo.getDaysRemain());
                item.put("priority", prIndentViewInfo.getPriority());
                item.put("priorityDate", prIndentViewInfo.getPriorityDate());
                item.put("brandId", prIndentViewInfo.getBrandId());
                item.put("brandName", prIndentViewInfo.getBrandName());
                Map<String, Object> warehouseInfo = new HashMap<>();
                warehouseInfo.put("id", prIndentViewInfo.getPiwId());
                warehouseInfo.put("warehouseName", prIndentViewInfo.getWarehouseName());
                warehouseInfo.put("key", prIndentViewInfo.getBrandName() + " " + prIndentViewInfo.getPrAttribute());
                warehouseInfo.put("warehouseId", prIndentViewInfo.getWarehouseId());
                warehouseInfo.put("orderQty", prIndentViewInfo.getOrderQty());
                warehouseInfo.put("prQty", prIndentViewInfo.getPrQty());


                List<Map<String, Object>> pdList = new ArrayList<>();

                Map<String, Object> pd = new HashMap<>();
                pd.put("id", prIndentViewInfo.getPdId());
                pd.put("pdDate", prIndentViewInfo.getPdDate());
                pd.put("qty", prIndentViewInfo.getPdQty());
                if (prIndentViewInfo.getPdId() != null) {
                    pdList.add(pd);
                }
                if (prIndentViewInfo.getPdDate() != null && prIndentViewInfo.getPdQty() != null) {
                    warehouseInfo.put("partialDeliveries", pdList);
                } else {
                    warehouseInfo.put("partialDeliveries", new ArrayList<>());
                }

                warehousKeyMap.put(warehouseKey, warehouseInfo);
                item.put("warehouses", warehousKeyMap);
                item.put("prQty", prIndentViewInfo.getPrQty());
                item.put("orderQty", prIndentViewInfo.getOrderQty());

                items.add(item);
            } else {
                Map<String, Object> existItem = anyItemOp.get();
                BigDecimal existingPrQty = (BigDecimal) existItem.get("prQty");
                BigDecimal existingOrderQty = (BigDecimal) existItem.get("orderQty");
                String ePrIds = (String) existItem.get("productRequirementsIds");
                String brandName = (String) existItem.get("brandName");
                String prAttribute = (String) existItem.get("itemName");
                if(brandName!=null){
                    if (brandName.equals(prIndentViewInfo.getBrandName()) && prAttribute.equals(prIndentViewInfo.getPrAttribute())) {
                        existItem.put("prQty", existingPrQty.add(prIndentViewInfo.getPrQty()));
                        existItem.put("orderQty", existingOrderQty.add( prIndentViewInfo.getOrderQty()));

                        List<String> prIds = new ArrayList<>();
                        prIds.add(ePrIds);
                        prIds.add(prIndentViewInfo.getProductRequirementsIds());
                        existItem.put("productRequirementsIds", String.join(",", prIds));
                    }
                }else {
                    if (prAttribute.equals(prIndentViewInfo.getPrAttribute())) {
                        existItem.put("prQty", existingPrQty.add(prIndentViewInfo.getPrQty()));
                        existItem.put("orderQty", existingOrderQty.add(prIndentViewInfo.getOrderQty()));

                        List<String> prIds = new ArrayList<>();
                        prIds.add(ePrIds);
                        prIds.add(prIndentViewInfo.getProductRequirementsIds());
                        existItem.put("productRequirementsIds", String.join(",", prIds));
                    }
                }
                if (existItem.containsKey("warehouses")) {
                    Map<String, Object> existWarehouseProp = (Map<String, Object>) existItem.get("warehouses");

                    if (existWarehouseProp.containsKey(warehouseKey)) {
                        warehousKeyMap = (Map<String, Object>) existWarehouseProp.get(warehouseKey);
                        String key = (String) warehousKeyMap.get("key");
                        String currentkey = prIndentViewInfo.getBrandName() + " " + prIndentViewInfo.getPrAttribute();
                        if (!key.contains(currentkey)) {
                            BigDecimal orderQty = (BigDecimal) warehousKeyMap.get("orderQty");
                            orderQty = orderQty.add(prIndentViewInfo.getOrderQty());
                            warehousKeyMap.replace("orderQty", orderQty);
                            BigDecimal wPrQty = (BigDecimal) warehousKeyMap.get("prQty");
                            BigDecimal existingVal = (item.get("prQty") != null) ? (BigDecimal) item.get("prQty") : new BigDecimal(0L);
                            existItem.put("prQty", existingVal.add(wPrQty));
                            wPrQty = wPrQty.add(prIndentViewInfo.getPrQty());
                            warehousKeyMap.replace("prQty", wPrQty);

                            warehousKeyMap.replace("key", currentkey);
                        }

                        List<Map<String, Object>> pdList = (List<Map<String, Object>>) warehousKeyMap.get("partialDeliveries");
                        Map<String, Object> pd = new HashMap<>();
                        pd.put("id", prIndentViewInfo.getPdId());
                        pd.put("pdDate", prIndentViewInfo.getPdDate());
                        pd.put("qty", prIndentViewInfo.getPdQty());
                        pdList.add(pd);
                    } else {
                        Map<String, Object> warehouseInfo = new HashMap<>();
                        warehouseInfo.put("id", prIndentViewInfo.getPiwId());
                        warehouseInfo.put("warehouseName", prIndentViewInfo.getWarehouseName());
                        warehouseInfo.put("key", prIndentViewInfo.getBrandName() + " " + prIndentViewInfo.getPrAttribute());
                        warehouseInfo.put("warehouseId", prIndentViewInfo.getWarehouseId());
                        warehouseInfo.put("orderQty", prIndentViewInfo.getOrderQty());
                        warehouseInfo.put("prQty", prIndentViewInfo.getPrQty());

                        List<Map<String, Object>> pdList = new ArrayList<>();
                        Map<String, Object> pd = new HashMap<>();
                        pd.put("id", prIndentViewInfo.getPdId());
                        pd.put("pdDate", prIndentViewInfo.getPdDate());
                        pd.put("qty", prIndentViewInfo.getPdQty());
                        pdList.add(pd);
                        if (prIndentViewInfo.getPdDate() != null && prIndentViewInfo.getPdQty() != null) {
                            warehouseInfo.put("partialDeliveries", pdList);
                        } else {
                            warehouseInfo.put("partialDeliveries", new ArrayList<>());
                        }
                        existWarehouseProp.put(warehouseKey, warehouseInfo);
                    }
                }
            }


        }

        return items.stream().map(itm -> {
            Map<String, Object> warehouses = (Map<String, Object>) itm.get("warehouses");
            itm.replace("warehouses", warehouses.values());
            return itm;
        }).toList();

    }


    @Override
    @Transactional
    public void updateOrderDetailsOrderQty(UpdatePrIndentDetailRequestDto updatePrIndentDetailRequestDto) {

        if (updatePrIndentDetailRequestDto.getPrIndentDetails() != null && updatePrIndentDetailRequestDto
                .getPrIndentDetails().isEmpty()) {
            throw new AesException("Pr Indent details should not empty");
        }

        if(updatePrIndentDetailRequestDto.getPrIndentDetails()!=null) {
            updatePrIndentDetailRequestDto.getPrIndentDetails().forEach(prd -> {
                Optional<PrIndentWarehouseDetail> piwdOp = prIndentWarehouseDetailRepository.findById(prd.getId());
                if (piwdOp.isPresent()) {
                    PrIndentWarehouseDetail piwd = piwdOp.get();
                    piwd.setOrderQty(prd.getOrderQty());

                    prd.getPartialDeliveries().forEach(pd -> {
                        PrIndentPartialDelivery pipd = new PrIndentPartialDelivery();
                        if (pd.getId() != null) {
                            pipd.setId(pd.getId());
                        }
                        pipd.setPdDate(pd.getPdDate());
                        pipd.setQty(pd.getQty());
                        pipd.setPrIndentWarehouseDetail(piwd);
                        prIndentPartialDeliveryRepository.save(pipd);
                    });
                }
            });
        }

    }

}
