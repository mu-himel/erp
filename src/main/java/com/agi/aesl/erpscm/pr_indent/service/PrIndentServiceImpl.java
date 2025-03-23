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

    private static final String ID="id";
    private static final String KEY="key";
    private static final String BRAND_NAME_KEY="brandName";
    private static final String ITEM_NAME_KEY="itemName";
    private static final String PRODUCT_REQUIREMENT_IDS_KEY="productRequirementsIds";
    private static final String PR_DETAIL_ID_KEY="prDetailId";
    private static final String WAREHOUSE_NAME_KEY="warehouseName";
    private static final String WAREHOUSE_ID_KEY="warehouseId";
    private static final String PR_QTY_KEY="prQty";
    private static final String ORDER_QTY_KEY="orderQty";
    private static final String PARTIAL_DELIVERIES_KEY="partialDeliveries";
    private static final String PD_DATE_KEY="pdDate";
    private static final String WAREHOUSES_KEY="warehouses";

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
    public Page<PrIndentRepository.PrIndentInfo> getAllPrIndents(Optional<Integer> page,
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
    public List<Map<String,Object>> getPrIndentById(Optional<Long> prIndentId) {
        List<PrIndentRepository.PrIndentViewInfo> result = prIndentRepository.getPrIndentById(
                prIndentId.orElseThrow(() -> new RuntimeException("PrIndent id should not empty"))
        );

        return getProcessedResult(result);
    }

    @Override
    public List<Map<String,Object>> getPrIndentByIds(Optional<List<Long>> prIndentIds) {
        List<PrIndentRepository.PrIndentViewInfo> result = prIndentRepository.getPrIndentByIds(
                prIndentIds.orElseThrow(() -> new RuntimeException("PrIndent id should not empty"))
        );
        return getProcessedResults(result);
    }

    private List<Map<String,Object>> getProcessedResult(List<PrIndentRepository.PrIndentViewInfo> result) {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> warehousKeyMap = new HashMap<>();
        for (PrIndentRepository.PrIndentViewInfo prIndentViewInfo : result) {

            Map<String, Object> item = new HashMap<>();
            String warehouseKey = prIndentViewInfo.getBrandName()+"_"+prIndentViewInfo.getPrAttribute() + "_" + prIndentViewInfo.getWarehouseId();

            Optional<Map<String, Object>> anyItemOp = items.stream().filter(itm -> {
                if (itm.get(BRAND_NAME_KEY) != null) {

                    return itm.get(BRAND_NAME_KEY).equals(prIndentViewInfo.getBrandName()) &&
                        itm.get(ITEM_NAME_KEY).equals(prIndentViewInfo.getPrAttribute());
                }else{
                    return itm.get(ITEM_NAME_KEY).equals(prIndentViewInfo.getPrAttribute());
                }
            }).findAny();

            if (anyItemOp.isEmpty()) {
                setItemInfo(prIndentViewInfo,item);
                Map<String, Object> warehouseInfo = new HashMap<>();
                warehouseInfo.put(ID, prIndentViewInfo.getPiwId());
                warehouseInfo.put(WAREHOUSE_NAME_KEY, prIndentViewInfo.getWarehouseName());
                warehouseInfo.put(KEY, prIndentViewInfo.getPrDetailId() + "_" + prIndentViewInfo.getPiwId());
                warehouseInfo.put(WAREHOUSE_ID_KEY, prIndentViewInfo.getWarehouseId());
                warehouseInfo.put(ORDER_QTY_KEY, prIndentViewInfo.getOrderQty());
                warehouseInfo.put(PR_QTY_KEY, prIndentViewInfo.getPrQty());

                List<Map<String, Object>> pdList = new ArrayList<>();
                setPrIndentViewInfo(prIndentViewInfo,pdList);

                if (prIndentViewInfo.getPdDate() != null && prIndentViewInfo.getPdQty() != null) {
                    warehouseInfo.put(PARTIAL_DELIVERIES_KEY, pdList);
                } else {
                    warehouseInfo.put(PARTIAL_DELIVERIES_KEY, new ArrayList<>());
                }
                warehousKeyMap.put(warehouseKey, warehouseInfo);
                item.put(WAREHOUSES_KEY, warehousKeyMap);
                item.put(PR_QTY_KEY, prIndentViewInfo.getPrQty());
                items.add(item);
            } else {
                Map<String, Object> existItem = anyItemOp.get();
                Long prDetailId = (Long) existItem.get(PR_DETAIL_ID_KEY);
                BigDecimal existingPrQty = (BigDecimal) existItem.get(PR_QTY_KEY);

                if (prDetailId.equals(prIndentViewInfo.getPrDetailId())) {
                    existItem.put(PR_QTY_KEY, existingPrQty.add(prIndentViewInfo.getPrQty()));

                }
                if (existItem.containsKey(WAREHOUSES_KEY)) {
                    Map<String, Object> existWarehouseProp = (Map<String, Object>) existItem.get(WAREHOUSES_KEY);

                    if (existWarehouseProp.containsKey(warehouseKey)) {
                        warehousKeyMap = (Map<String, Object>) existWarehouseProp.get(warehouseKey);
                        String key = (String) warehousKeyMap.get(KEY);
                        String currentkey = prIndentViewInfo.getPrDetailId() + "_" + prIndentViewInfo.getPiwId();
                        if (!key.contains(currentkey)) {
                            BigDecimal orderQty = (BigDecimal) warehousKeyMap.get(ORDER_QTY_KEY);
                            orderQty = orderQty.add(prIndentViewInfo.getOrderQty());
                            warehousKeyMap.replace(ORDER_QTY_KEY, orderQty);
                            BigDecimal wPrQty = (BigDecimal) warehousKeyMap.get(PR_QTY_KEY);
                            BigDecimal existingVal = (item.get(PR_QTY_KEY) != null) ? (BigDecimal) item.get(PR_QTY_KEY) : new BigDecimal(0L);
                            existItem.put(PR_QTY_KEY, existingVal.add(wPrQty));
                            wPrQty = wPrQty.add(prIndentViewInfo.getPrQty());
                            warehousKeyMap.replace(PR_QTY_KEY, wPrQty);

                            warehousKeyMap.replace(KEY, currentkey);
                        }

                        List<Map<String, Object>> pdList = (List<Map<String, Object>>) warehousKeyMap.get(PARTIAL_DELIVERIES_KEY);
                        setPrIndentViewInfo(prIndentViewInfo,pdList);
                    } else {
                        Map<String, Object> warehouseInfo = new HashMap<>();
                        warehouseInfo.put(ID, prIndentViewInfo.getPiwId());
                        warehouseInfo.put(WAREHOUSE_NAME_KEY, prIndentViewInfo.getWarehouseName());
                        warehouseInfo.put(KEY, prIndentViewInfo.getPrDetailId() + "_" + prIndentViewInfo.getPiwId());
                        warehouseInfo.put(WAREHOUSE_ID_KEY, prIndentViewInfo.getWarehouseId());
                        warehouseInfo.put(ORDER_QTY_KEY, prIndentViewInfo.getOrderQty());
                        warehouseInfo.put(PR_QTY_KEY, prIndentViewInfo.getPrQty());

                        List<Map<String, Object>> pdList = new ArrayList<>();
                        setPrIndentViewInfo(prIndentViewInfo,pdList);
                        if (prIndentViewInfo.getPdDate() != null && prIndentViewInfo.getPdQty() != null) {
                            warehouseInfo.put(PARTIAL_DELIVERIES_KEY, pdList);
                        } else {
                            warehouseInfo.put(PARTIAL_DELIVERIES_KEY, new ArrayList<>());
                        }
                        existWarehouseProp.put(warehouseKey, warehouseInfo);
                    }
                }
            }


        }

        return items.stream().map(itm -> {
            Map<String, Object> warehouses = (Map<String, Object>) itm.get(WAREHOUSES_KEY);
            itm.replace(WAREHOUSES_KEY, warehouses.values());
            return itm;
        }).toList();

    }

    private List<Map<String,Object>> getProcessedResults(List<PrIndentRepository.PrIndentViewInfo> result) {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> warehousKeyMap = new HashMap<>();
        for (PrIndentRepository.PrIndentViewInfo prIndentViewInfo : result) {

            Map<String, Object> item = new HashMap<>();
            String warehouseKey = prIndentViewInfo.getBrandName() + "_" + prIndentViewInfo.getPrAttribute() + "_" + prIndentViewInfo.getWarehouseId();
            Optional<Map<String, Object>> anyItemOp = items.stream().filter(itm -> {
                String itemName = (String) itm.get(ITEM_NAME_KEY);
                String brandName = (String) itm.get(BRAND_NAME_KEY);
                String prAttribute = prIndentViewInfo.getPrAttribute();
                if(brandName!=null){
                    return brandName.equals(prIndentViewInfo.getBrandName()) && itemName.equals(prAttribute);
                }
                return itemName.equals(prAttribute);
            }).findAny();

            if (anyItemOp.isEmpty()) {
                setItemInfo(prIndentViewInfo, item);
                Map<String, Object> warehouseInfo = new HashMap<>();
                warehouseInfo.put(ID, prIndentViewInfo.getPiwId());
                warehouseInfo.put(WAREHOUSE_NAME_KEY, prIndentViewInfo.getWarehouseName());
                warehouseInfo.put(KEY, prIndentViewInfo.getBrandName() + " " + prIndentViewInfo.getPrAttribute());
                warehouseInfo.put(WAREHOUSE_ID_KEY, prIndentViewInfo.getWarehouseId());
                warehouseInfo.put(ORDER_QTY_KEY, prIndentViewInfo.getOrderQty());
                warehouseInfo.put(PR_QTY_KEY, prIndentViewInfo.getPrQty());

                List<Map<String, Object>> pdList = new ArrayList<>();
                setPrIndentViewInfo(prIndentViewInfo,pdList);
                if (prIndentViewInfo.getPdDate() != null && prIndentViewInfo.getPdQty() != null) {
                    warehouseInfo.put(PARTIAL_DELIVERIES_KEY, pdList);
                } else {
                    warehouseInfo.put(PARTIAL_DELIVERIES_KEY, new ArrayList<>());
                }
                warehousKeyMap.put(warehouseKey, warehouseInfo);
                item.put(WAREHOUSES_KEY, warehousKeyMap);
                item.put(PR_QTY_KEY, prIndentViewInfo.getPrQty());
                item.put(ORDER_QTY_KEY, prIndentViewInfo.getOrderQty());
                items.add(item);
            } else {
                Map<String, Object> existItem = anyItemOp.get();
                BigDecimal existingPrQty = (BigDecimal) existItem.get(PR_QTY_KEY);
                BigDecimal existingOrderQty = (BigDecimal) existItem.get(ORDER_QTY_KEY);
                String ePrIds = (String) existItem.get(PRODUCT_REQUIREMENT_IDS_KEY);
                String brandName = (String) existItem.get(BRAND_NAME_KEY);
                String prAttribute = (String) existItem.get(ITEM_NAME_KEY);
                if(brandName!=null){
                    if (brandName.equals(prIndentViewInfo.getBrandName()) && prAttribute.equals(prIndentViewInfo.getPrAttribute())) {
                        existItem.put(PR_QTY_KEY, existingPrQty.add(prIndentViewInfo.getPrQty()));
                        existItem.put(ORDER_QTY_KEY, existingOrderQty.add( prIndentViewInfo.getOrderQty()));

                        List<String> prIds = new ArrayList<>();
                        prIds.add(ePrIds);
                        prIds.add(prIndentViewInfo.getProductRequirementsIds());
                        existItem.put(PRODUCT_REQUIREMENT_IDS_KEY, String.join(",", prIds));
                    }
                }else {
                    if (prAttribute.equals(prIndentViewInfo.getPrAttribute())) {
                        existItem.put(PR_QTY_KEY, existingPrQty.add(prIndentViewInfo.getPrQty()));
                        existItem.put(ORDER_QTY_KEY, existingOrderQty.add(prIndentViewInfo.getOrderQty()));

                        List<String> prIds = new ArrayList<>();
                        prIds.add(ePrIds);
                        prIds.add(prIndentViewInfo.getProductRequirementsIds());
                        existItem.put(PRODUCT_REQUIREMENT_IDS_KEY, String.join(",", prIds));
                    }
                }
                if (existItem.containsKey(WAREHOUSES_KEY)) {
                    Map<String, Object> existWarehouseProp = (Map<String, Object>) existItem.get(WAREHOUSES_KEY);

                    if (existWarehouseProp.containsKey(warehouseKey)) {
                        warehousKeyMap = (Map<String, Object>) existWarehouseProp.get(warehouseKey);
                        String key = (String) warehousKeyMap.get(KEY);
                        String currentkey = prIndentViewInfo.getBrandName() + " " + prIndentViewInfo.getPrAttribute();
                        if (!key.contains(currentkey)) {
                            BigDecimal orderQty = (BigDecimal) warehousKeyMap.get(ORDER_QTY_KEY);
                            orderQty = orderQty.add(prIndentViewInfo.getOrderQty());
                            warehousKeyMap.replace(ORDER_QTY_KEY, orderQty);
                            BigDecimal wPrQty = (BigDecimal) warehousKeyMap.get(PR_QTY_KEY);
                            BigDecimal existingVal = (item.get(PR_QTY_KEY) != null) ? (BigDecimal) item.get(PR_QTY_KEY) : new BigDecimal(0L);
                            existItem.put(PR_QTY_KEY, existingVal.add(wPrQty));
                            wPrQty = wPrQty.add(prIndentViewInfo.getPrQty());
                            warehousKeyMap.replace(PR_QTY_KEY, wPrQty);

                            warehousKeyMap.replace(KEY, currentkey);
                        }

                        List<Map<String, Object>> pdList = (List<Map<String, Object>>) warehousKeyMap.get(PARTIAL_DELIVERIES_KEY);
                        setPrIndentViewInfo(prIndentViewInfo,pdList);
                    } else {
                        Map<String, Object> warehouseInfo = new HashMap<>();
                        warehouseInfo.put(ID, prIndentViewInfo.getPiwId());
                        warehouseInfo.put(WAREHOUSE_NAME_KEY, prIndentViewInfo.getWarehouseName());
                        warehouseInfo.put(KEY, prIndentViewInfo.getBrandName() + " " + prIndentViewInfo.getPrAttribute());
                        warehouseInfo.put(WAREHOUSE_ID_KEY, prIndentViewInfo.getWarehouseId());
                        warehouseInfo.put(ORDER_QTY_KEY, prIndentViewInfo.getOrderQty());
                        warehouseInfo.put(PR_QTY_KEY, prIndentViewInfo.getPrQty());

                        List<Map<String, Object>> pdList = new ArrayList<>();
                        setPrIndentViewInfo(prIndentViewInfo,pdList);
                        if (prIndentViewInfo.getPdDate() != null && prIndentViewInfo.getPdQty() != null) {
                            warehouseInfo.put(PARTIAL_DELIVERIES_KEY, pdList);
                        } else {
                            warehouseInfo.put(PARTIAL_DELIVERIES_KEY, new ArrayList<>());
                        }
                        existWarehouseProp.put(warehouseKey, warehouseInfo);
                    }
                }
            }


        }

        return items.stream().map(itm -> {
            Map<String, Object> warehouses = (Map<String, Object>) itm.get(WAREHOUSES_KEY);
            itm.replace(WAREHOUSES_KEY, warehouses.values());
            return itm;
        }).toList();

    }

    private static void setItemInfo(PrIndentRepository.PrIndentViewInfo prIndentViewInfo, Map<String, Object> item) {
        item.put(ID, prIndentViewInfo.getId());
        item.put(PRODUCT_REQUIREMENT_IDS_KEY, prIndentViewInfo.getProductRequirementsIds());
        item.put(PR_DETAIL_ID_KEY, prIndentViewInfo.getPrDetailId());
        item.put(ITEM_NAME_KEY, prIndentViewInfo.getPrAttribute());
        item.put("categoryName", prIndentViewInfo.getCategoryName());
        item.put("subCategoryName", prIndentViewInfo.getSubCategoryName());
        item.put("categoryId", prIndentViewInfo.getCategoryId());
        item.put("subCategoryId", prIndentViewInfo.getSubCategoryId());
        item.put("daysRemain", prIndentViewInfo.getDaysRemain());
        item.put("priority", prIndentViewInfo.getPriority());
        item.put("priorityDate", prIndentViewInfo.getPriorityDate());
        item.put("brandId", prIndentViewInfo.getBrandId());
        item.put(BRAND_NAME_KEY, prIndentViewInfo.getBrandName());
    }

    private void setPrIndentViewInfo(PrIndentRepository.PrIndentViewInfo prIndentViewInfo,
                                     List<Map<String, Object>> pdList){
        Map<String, Object> pd = new HashMap<>();
        pd.put(ID, prIndentViewInfo.getPdId());
        pd.put(PD_DATE_KEY, prIndentViewInfo.getPdDate());
        pd.put("qty", prIndentViewInfo.getPdQty());
        if (prIndentViewInfo.getPdId() != null) {
            pdList.add(pd);
        }
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
