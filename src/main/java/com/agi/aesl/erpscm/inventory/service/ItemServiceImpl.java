package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.common.ItemAttributeInterface;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseStoreService;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.repository.DemandDetailRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.fileupload.service.FileUploadService;
import com.agi.aesl.erpscm.inventory.dto.request.*;
import com.agi.aesl.erpscm.inventory.dto.response.ItemDetail;
import com.agi.aesl.erpscm.inventory.dto.response.ItemListWithAttributesDto;
import com.agi.aesl.erpscm.inventory.dto.response.SyncItemDetail;
import com.agi.aesl.erpscm.inventory.dto.response.SyncItemDto;
import com.agi.aesl.erpscm.inventory.entity.*;
import com.agi.aesl.erpscm.inventory.enums.ItemHeader;
import com.agi.aesl.erpscm.inventory.enums.ItemInactiveStatus;
import com.agi.aesl.erpscm.inventory.enums.StockType;
import com.agi.aesl.erpscm.inventory.repository.*;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserItem;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserItemRepository;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.network.NetworkService;

import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {


    private final ItemRepository itemRepository;


    private final ClaimResolver claimResolver;



    private final ItemStockRepository itemStockRepository;


    private final FileUploadService fileUploadService;


    private final CategoryService categoryService;


    private final CategoryBrandRepository catBrandRepo;


    private final OrgService orgService;


    private final CpsServerConfig cpsConfig;


    private final NetworkService networkService;


    private final AccountService accountService;


    private final WarehouseService warehouseService;


    private final WarehouseStoreService warehouseStoreService;

    private final CategoryBrandRepository categoryBrandRepository;


    private final CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;


    private final ItemAttributeRepository itemAttributeRepository;


    private final DemandDetailRepository demandDetailRepository;


    private final ItemImportLogRepository itemImportLogRepository;


    private final WarehouseStoreRepository warehouseStoreRepository;


    private final IntegrationReaderService integrationReaderService;


    private final ItemFuncationalUnitRepository itemFuncationalUnitRepository;


    private final UserItemRepository userItemRepository;

    private final ItemStockService itemStockService;

    @Value("${upload.dir}")
    private String uploadDir;

    private static final String ERR_WAREHOUSE_STORE_NOT_FOUND="Sorry! Warehouse Store not found";
    private static final String ERR_WAREHOUSE_NOT_FOUND="Sorry! Warehouse not found";
    private static final String WAREHOUSE_ID_KEY="warehouseId";
    private static final String WAREHOUSE_STORE_ID_KEY="warehouseStoreId";

    @Override
    public Optional<Item> getItemDetail(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public Optional<ItemDetail> getItemDetailWithWarehouse(Long id) {
        Optional<ItemRepository.ItemDetail> itemDetailOptional = itemRepository.findByIdWithWarehouse(id);
        if(itemDetailOptional.isEmpty()){
            throw new AesException("Sorry! Item not found");
        }

        ItemRepository.ItemDetail detail = itemDetailOptional.get();
        List<ItemFunctionalUnit> functionalUnits = itemFuncationalUnitRepository.findAllByItemId(detail.getId());
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setId(detail.getId());
        itemDetail.setFunctionalUnits(functionalUnits);
        itemDetail.setName(detail.getName());
        itemDetail.setCode(detail.getCode());
        itemDetail.setActive(detail.getActive());
        itemDetail.setItemAttributeName(detail.getItemAttributeName());
        itemDetail.setAttributes(detail.getAttributes());
        itemDetail.setItemCategory(detail.getItemCategory());
        itemDetail.setItemParentCategory(detail.getItemParentCategory());
        itemDetail.setItemUnit(detail.getItemUnit());
        itemDetail.setBrand(detail.getBrand());
        itemDetail.setReorderPercentage(detail.getReorderPercentage());
        itemDetail.setStockThresholdQty(detail.getStockThresholdQty());
        Map<String, List<Map<String,Object>>> warehouses = new HashMap<>();
        detail.getStocks().forEach(itemStock -> {
            Optional<BigDecimal> inTransit  = itemRepository.findInTransitByItemAndWarehouse(itemStock.getItem().getId(),itemStock.getWarehouse().getId());
           if(warehouses.containsKey(""+itemStock.getWarehouse().getId())) {
               List<Map<String,Object>> itemStocks = warehouses.get(""+itemStock.getWarehouse().getId());

               Optional<Map<String,Object>> mapOp = itemStocks.stream().filter(
                       iStock->
                           ((Long)iStock.get(WAREHOUSE_ID_KEY)).equals(itemStock.getWarehouse().getId())
                                   && ((Long)iStock.get(WAREHOUSE_STORE_ID_KEY))
                                   .equals(itemStock.getWarehouseStore().getId())
                       ).findFirst();
                 processItemStock(itemStock,inTransit,warehouses,itemStocks,mapOp);

           }else{
               List<Map<String,Object>> itemStocks = new ArrayList<>();
               processItemStock(itemStock,inTransit,warehouses,itemStocks,Optional.empty());

           }
        });
        itemDetail.setWarehouses(warehouses);

        return Optional.of(itemDetail);
    }

    @Override
    public Optional<ItemDetail> getItemDetailWithWarehouseWithoutInTransit(Long id) {
        Optional<ItemRepository.ItemDetail> itemDetailOptional = itemRepository.findByIdWithWarehouse(id);
        if(itemDetailOptional.isEmpty()){
            throw new AesException("Sorry! Item not found");
        }

        ItemRepository.ItemDetail detail = itemDetailOptional.get();
        List<ItemFunctionalUnit> functionalUnits = itemFuncationalUnitRepository.findAllByItemId(detail.getId());
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setId(detail.getId());
        itemDetail.setFunctionalUnits(functionalUnits);
        itemDetail.setName(detail.getName());
        itemDetail.setCode(detail.getCode());
        itemDetail.setActive(detail.getActive());
        itemDetail.setItemAttributeName(detail.getItemAttributeName());
        itemDetail.setAttributes(detail.getAttributes());
        itemDetail.setItemCategory(detail.getItemCategory());
        itemDetail.setItemParentCategory(detail.getItemParentCategory());
        itemDetail.setItemUnit(detail.getItemUnit());
        itemDetail.setBrand(detail.getBrand());
        itemDetail.setReorderPercentage(detail.getReorderPercentage());
        itemDetail.setStockThresholdQty(detail.getStockThresholdQty());
        Map<String, List<Map<String,Object>>> warehouses = new HashMap<>();
        detail.getStocks().forEach(itemStock -> {
            Optional<BigDecimal> inTransit  = Optional.empty();
            if(warehouses.containsKey(""+itemStock.getWarehouse().getId())) {
                List<Map<String,Object>> itemStocks = warehouses.get(""+itemStock.getWarehouse().getId());

                Optional<Map<String,Object>> mapOp = itemStocks.stream().filter(
                        iStock->
                            ((Long)iStock.get(WAREHOUSE_ID_KEY)).equals(itemStock.getWarehouse().getId())
                                    && ((Long)iStock.get(WAREHOUSE_STORE_ID_KEY))
                                    .equals(itemStock.getWarehouseStore().getId())
                        ).findFirst();
                processItemStock(itemStock,inTransit,warehouses,itemStocks,mapOp);

            }else{
                List<Map<String,Object>> itemStocks = new ArrayList<>();
                processItemStock(itemStock,inTransit,warehouses,itemStocks,Optional.ofNullable(null));

            }
        });
        itemDetail.setWarehouses(warehouses);

        return Optional.of(itemDetail);
    }

    private void processItemStock(ItemStock itemStock, Optional<BigDecimal> inTransit,
                                  Map<String, List<Map<String,Object>>> warehouses,
                                  List<Map<String,Object>> itemStocks, Optional<Map<String,Object>> mapOp){
        Map<String,Object> stockInfo = new HashMap<>();
        if(mapOp.isPresent()){
            stockInfo = mapOp.get();
        }
        BigDecimal sQty = (BigDecimal) stockInfo.get("stockQty");

        stockInfo.put("inTransit",inTransit.orElse(new BigDecimal(0)));
        stockInfo.put("stockQty",((sQty!=null)?sQty:new BigDecimal(0)).add(itemStock.getStockQty()));
        stockInfo.put(WAREHOUSE_ID_KEY,itemStock.getWarehouse().getId());
        stockInfo.put("warehouseName",itemStock.getWarehouse().getName());
        stockInfo.put(WAREHOUSE_STORE_ID_KEY,itemStock.getWarehouseStore().getId());
        stockInfo.put("warehouseStoreName",itemStock.getWarehouseStore().getStoreName());
        if(mapOp.isEmpty()) {
            itemStocks.add(stockInfo);
        }
        warehouses.put(""+itemStock.getWarehouse().getId(), itemStocks);
    }

    @Override
    public Page<ItemRepository.PageItemList> getAllItems(
            Jwt token,
            String uri,
            Optional<Integer> page, Optional<Integer> size,
                               Optional<String> name,
                               Optional<String> code,
                               Optional<Integer> reorderPercentage,
                               Optional<Integer> stockThresholdQty,
                               Optional<Long> categoryId,
                               Optional<Long> subCategoryId,
                               Optional<Long> warehouseId,
                               Optional<Long> warehouseStoreId

    ) {
        claimResolver.setToken(token);


        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);

        List<Long> warehouseIds = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds =  dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);

        }

        if(categoryId.isPresent()){
            categoryIds.add(categoryId.get());
        }else{
            categoryIds = dataFilter.getCategoryIds();
        }


        return itemRepository.findAllItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryIds,
                subCategoryId.orElse(null),
                warehouseIds,
                warehouseStoreId.orElse(null),
                pageable);

    }

    @Override
    public Page<ItemRepository.PageItemList> getPendingAllItems(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> name, Optional<String> code, Optional<Integer> reorderPercentage, Optional<Integer> stockThresholdQty, Optional<Long> categoryId, Optional<Long> subCategoryId, Optional<Long> warehouseId, Optional<Long> warehouseStoreId) {
        claimResolver.setToken(token);
        String uri = "inventory-control/product";

        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> filterBy = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> warehouseIds = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = filterBy;
        }

        if(categoryId.isPresent()){
            categoryIds.add(categoryId.get());
        }else{
            categoryIds = dataFilter.getCategoryIds();
        }

        return itemRepository.findAllPendingItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryIds,
                subCategoryId.orElse(null),
                warehouseIds,
                warehouseStoreId.orElse(null),
                pageable);

    }

    @Override
    public Page<ItemRepository.PageItemList> getPendingVerificationAllItems(
            Jwt token,
            Optional<Integer> page, Optional<Integer> size, Optional<String> name, Optional<String> code, Optional<Integer> reorderPercentage, Optional<Integer> stockThresholdQty, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                  Optional<Long> warehouseId, Optional<Long> warehouseStoreId) {
        claimResolver.setToken(token);
        String uri = "inventory-control/product";

        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> filterBy = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        List<Long> warehouseIds = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.clear();
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = filterBy;
        }

        if(categoryId.isPresent()){
            categoryIds.add(categoryId.get());
        }else{
            categoryIds = dataFilter.getCategoryIds();
        }

        return itemRepository.findAllPendingVerificationItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryIds,
                subCategoryId.orElse(null),
                warehouseIds,
                warehouseStoreId.orElse(null),
                pageable);

    }

    @Override
    public List<ItemRepository.ItemInfo> getAllItems(Optional<Long> categoryId, Optional<String> name, Optional<String> code) {


        if(categoryId.isPresent() && name.isPresent()){
                return  itemRepository
                        .findAllByActiveAndItemCategoryIdOrItemParentCategoryIdAndNameLikeIgnoreCaseOrCodeLikeIgnoreCase(
                        true,categoryId,categoryId,name.get()+"%",name.get()+"%");


        }

        if(name.isPresent() && code.isEmpty()){
            return itemRepository.findAllByActiveAndNameLikeIgnoreCase(true,name.get()+"%");
        }
        if(name.isEmpty() && code.isPresent()){
            return  itemRepository.findAllByActiveAndCodeLikeIgnoreCase(true, code.get()+"%");
        }
        return new ArrayList<>();
    }

    @Override
    public List<ItemListWithAttributesDto> getAllItemsBySubCategoryAndAttribute(
                                                        Optional<Long> warehouseId,
                                                        Optional<Long> brandId,
                                                        Optional<Long> subCategoryId,
                                                        Optional<String> name,
                                                        Optional<String> code,
                                                        Optional<String> sattributes,
                                                        Optional<String> attriButeType,
                                                        Optional<String> attriButeValue) {
        if(subCategoryId.isEmpty()){
            throw new AesException("Sub Category ID Missing");
        }

        List<ItemRepository.ItemInfoExt> items=  itemRepository.findAllItemBySubCategoryAndAttributeAndName(
                warehouseId.orElse(null),
                brandId.orElse(null),
                subCategoryId.orElse(null),
                name.orElse(null),
                code.orElse(null),
                null,
                null
        );

        List<ItemListWithAttributesDto> itemListWithAttributesDtos = new ArrayList<>();

        for(ItemRepository.ItemInfoExt itemInfoExt : items){
            
            if(itemInfoExt.getAttributeTypes()==null){
                continue;
            }
            StringBuilder sb = new StringBuilder();
            ItemListWithAttributesDto dto = new ItemListWithAttributesDto();
            dto.setId(itemInfoExt.getId());
            dto.setCode(itemInfoExt.getCode());
            dto.setName(itemInfoExt.getName());
            dto.setBrandId(itemInfoExt.getBrandId());
            dto.setBrandName(itemInfoExt.getBrandName());
            dto.setWarehouseId(itemInfoExt.getWarehouseId());
            dto.setWarehouseStoreId(itemInfoExt.getWarehouseStoreId());
            List<Map<String,Object>> attributes = new ArrayList<>();
            String[] attrTypes = itemInfoExt.getAttributeTypes().split(",");
            String[] attrValues = itemInfoExt.getAttributeValues().split(",");
            String[] attrUnits = itemInfoExt.getAttributeUnits().split(",");
            for(int i=0; i<attrTypes.length; i++){
                Map<String,Object> attrs = new HashMap<>();

                String attributeType = attrTypes[i];
                String attributeValue = attrValues[i];
                String attributeUnit = attrUnits[i];
                
                attributeType = attributeType.substring(0, attributeType.indexOf("_"));
                attributeValue = attributeValue.substring(0, attributeValue.indexOf("_"));
                attributeUnit = attributeUnit.substring(0, attributeUnit.indexOf("_"));

                if(!attrs.containsKey(attributeType)){
                    attrs.put("attributeValue",attributeValue);
                    attrs.put("attributeType",attributeType);
                    attrs.put("attributeUnit",attributeUnit);
                    attributes.add(attrs);
                    sb.append(attributeType).append(" ").append(attributeValue).append(" ").append(attributeUnit)
                    .append(" - ");
                }
                dto.setAttributes(attributes);
            }
            dto.setItemAttribute(sb.substring(0,sb.length()-3));
            itemListWithAttributesDtos.add(dto);

        }
        List<ItemListWithAttributesDto> filteredList  = itemListWithAttributesDtos;
        
        if(sattributes.isPresent()){
            String attrStr = sattributes.get().replaceAll("  "," ");
            
            filteredList = itemListWithAttributesDtos.stream().filter(itemListWithAttributesDto->{
                String perItemAttr = "";
                if(attrStr.contains(itemListWithAttributesDto.getBrandName()) &&  itemListWithAttributesDto.getBrandName()!=null){
                    perItemAttr = itemListWithAttributesDto.getBrandName() + " - " + itemListWithAttributesDto.getItemAttribute().replaceAll("  ", " ");
                }else{
                    perItemAttr = itemListWithAttributesDto.getItemAttribute();
                }
                return (attrStr.contains(perItemAttr) || perItemAttr.contains(attrStr));
            }).toList();

        }

        return filteredList;

    }


    private String generateItemAttributeName(List<ItemAttribute> attributes){
        StringBuilder sb = new StringBuilder();

        attributes.forEach(itemAttribute -> {
            String attrType = itemAttribute.getAttributeType().trim();
            String attrValue = itemAttribute.getAttributeValue().trim();
            String attrUnit = itemAttribute.getAttributeUnit().trim();
            if(!attrType.isEmpty() && !attrValue.isEmpty() && !attrUnit.isEmpty()){
                sb.append(attrType +" "+attrValue +" "+attrUnit);
                sb.append(" - ");
            }
        });

        return (sb.isEmpty())? "" : sb.toString().substring(0,sb.length()-3);
    }

    @Override
    @Transactional
    public void createItem(Jwt loggedInUser, ItemRequestDto itemRequestDto) {
        claimResolver.setToken(loggedInUser);
        Item item = itemRequestDto.getEntity();
        if(itemRequestDto.getUserItemId()!=null) {
            item.setUserItemId(itemRequestDto.getUserItemId());
        }
        String itemAttributeName = generateItemAttributeName(itemRequestDto.getAttributes());

       Warehouse warehouse = null;
       WarehouseStore warehouseStore = null;
       Long warehouseId = (itemRequestDto.getWarehouse().getId()!=null)?itemRequestDto.getWarehouse().getId():
               claimResolver.getEmployee().get().getWarehouseId();
        Optional<Warehouse> wOp = warehouseService.getWarehouse(warehouseId);
        if(wOp.isEmpty()){
            throw new AesException("Sorry! Warehouse not found");
        }
        Optional<WarehouseStore> wsOp = warehouseStoreRepository.findById(itemRequestDto.getWarehouseStore().getId());
        if(wsOp.isEmpty()){
            throw new AesException("Sorry! Warehouse Store not found");
        }
        warehouse= wOp.get();
        warehouseStore = wsOp.get();
        Long brandId = (itemRequestDto.getBrand()!=null)? itemRequestDto.getBrand().getId() : null;
       List<?> itemExistByAttr = this.getByAttributes(brandId,itemAttributeName,item.getItemCategory().getId(),warehouse.getId());
       if(!itemExistByAttr.isEmpty()){
           throw new AesException("Sorry! Item Already exist with same attributes for this brand");
       }

        Optional<Item> itemOp = itemRepository.findByCode(item.getCode());
        if(itemOp.isPresent()){
            Item itemExist = itemOp.get();
            Optional<ItemImportLog> iilOp = itemImportLogRepository.findByItemIdAndWarehouseId(itemExist.getId(),warehouse.getId());
            if(iilOp.isPresent()){
                throw new AesException("Item already exist with code("+itemExist.getCode()+") and status is "+iilOp.get().getItemInactiveStatus());
            }

        }

       if(item.getItemParentCategory()==null && item.getItemCategory()==null){
           throw new AesException("Item Sub Category Missing");
       }

        if(item.getItemParentCategory()==null){
            throw new AesException("Item Main Category Missing");
        }


        if(itemRequestDto.getBrand()!=null && itemRequestDto.getBrand().getId()!=null){
            item.setBrand(new CategoryBrand(itemRequestDto.getBrand().getId()));
        }

        item.setStocks(Arrays.asList(new ItemStock(
                ((itemRequestDto.getCurrentStockQty()!=null)? itemRequestDto.getCurrentStockQty() : new BigDecimal(0)),
                item,
                StockType.STOCK_IN,warehouse,warehouseStore
                )));
        if(itemRequestDto.getAttributes()!=null && !itemRequestDto.getAttributes().isEmpty()) {
            item.setAttributes(itemRequestDto.getAttributes().stream().map(itemAttribute -> {
                itemAttribute.setItem(item);
                itemAttribute.setAttributeType(itemAttribute.getAttributeType().trim());
                itemAttribute.setAttributeValue(itemAttribute.getAttributeValue().trim());
                itemAttribute.setAttributeUnit(itemAttribute.getAttributeUnit().trim());
                return itemAttribute;
            }).toList());
        }
        item.setActive(false);
        ItemImportLog iil = new ItemImportLog();
        iil.setItemInactiveStatus(ItemInactiveStatus.PENDING);
        iil.setWarehouse(warehouse);
        iil.setItem(item);
        item.setItemImportLogs(Arrays.asList(iil));
        item.setItemAttributeName(itemAttributeName);
        item.setCreatedBy(claimResolver.getUserId());
        itemRepository.save(item);



        if(item.getId()!=null){
            sendItemToCps(claimResolver,itemRequestDto.getEmployee(),item, item.getItemAttributes(),warehouseStore);

        }

    }



    @Override
    public  <T extends Item> void sendItemToCps(ClaimResolver claimResolver, String emp, T item,
                                                         List<ItemAttributeInterface> attributes,
                                                         WarehouseStore warehouseStore
                                                         ) {
        PendingItemRequestDto pendingItemRequestDto = new PendingItemRequestDto();
        Employee employee = claimResolver.getEmployee().orElse(null);
        pendingItemRequestDto.setItemAttributeName(item.getItemAttributeName());
        if(employee!=null) {
            pendingItemRequestDto.setRequestedBy(emp);
            pendingItemRequestDto.setDesignation(employee.getDesignationName());
            pendingItemRequestDto.setDepartment(employee.getDepartmentName());
            pendingItemRequestDto.setWarehouseId(employee.getWarehouseId());
            if(warehouseStore!=null){
                pendingItemRequestDto.setWarehouseStoreId(warehouseStore.getId());
            }

            pendingItemRequestDto.setWarehouseName(employee.getWarehouseName());
        }else{
            throw new AesException("Sorry! Employee Info missing");
        }
        Optional<ItemCategory> catOp = categoryService.getAnyItemCategory(item.getCategory().getId());


        pendingItemRequestDto.setSubCategoryCode(catOp.get().getCode().substring(2));
        if(item.getBrand()!=null) {
            Optional<CategoryBrand> brandOp = categoryBrandRepository.findById(item.getBrand().getId());
            pendingItemRequestDto.setBrand(brandOp.get().getName());
        }
        pendingItemRequestDto.setReportingManager(employee.getReportingManager());
        pendingItemRequestDto.setEmployeeId(employee.getId());
        pendingItemRequestDto.setAttributes(attributes.stream().map(attr->{
            PendingItemAttributeDto attribute = new PendingItemAttributeDto();
            attribute.setAttributeType(attr.getAttributeType());
            attribute.setAttributeUnit(attr.getAttributeUnit());
            attribute.setAttributeValue(attr.getAttributeValue());
            return attribute;
        }).toList());
        pendingItemRequestDto.setItemUnit(item.getItemUnit());
        pendingItemRequestDto.setScmItemId(item.getId());

        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        if(orgOp.isPresent()){
            pendingItemRequestDto.setOrganizationId(orgOp.get().getCpsVendorRegistrationId());
            headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
        }
        HttpEntity<PendingItemRequestDto> payload = new HttpEntity<>(pendingItemRequestDto,headers);
        String url = cpsConfig.getPendingItemReqEndpoint();
        ResponseEntity<?> response = networkService.post(url,payload,Map.class);
        Map<String,Object> map = (Map<String, Object>) response.getBody();
        if(map!=null && map.containsKey("code") && map.containsKey("pendingItemRequestId") ) {
            item.setCode(warehouseStore.getStoreName().substring(0,1).toUpperCase().concat("-").concat(map.get("code").toString()));
            item.setPendingReqItemId(Long.parseLong(map.get("pendingItemRequestId").toString()));
        }
    }

    @Override
    public void createItem(Jwt loggedInUser, RemoteItemRequestDto itemRequestDto) {
        Item item = itemRequestDto.getEntity();

        String itemAttributeName = generateItemAttributeName(itemRequestDto.getAttributes());

       Warehouse warehouse = null;
       WarehouseStore warehouseStore = null;

        if(itemRequestDto.getWarehouse().getId()!=null) {
           warehouse = new Warehouse(itemRequestDto.getWarehouse().getId()) ;
        }else{
           warehouse = new Warehouse();//loggedInUser.getEmployee().getWarehouseId()
        }

        Optional<ItemCategory> catOp = categoryService.getCategoryByCode(itemRequestDto.getCategoryCode());
        if(catOp.isEmpty()){
            throw new AesException("Sorry! Category Not found");
        }

        item.setItemCategory(catOp.get());
        item.setItemParentCategory(catOp.get().getParentCategory());
        Optional<CategoryBrand> catBrandOp = categoryBrandRepository.findByCategoryIdAndName(catOp.get().getId(), itemRequestDto.getBrandName());

        Long brandId = (catBrandOp.isPresent())? catBrandOp.get().getId() : null;
       List<?> itemExistByAttr = this.getByAttributes(brandId,itemAttributeName,item.getItemCategory().getId(), warehouse.getId());
       if(!itemExistByAttr.isEmpty()){
           throw new AesException("Sorry! Item Already exist with same attributes for this brand");
       }

        item.setItemAttributeName(itemAttributeName);

       Optional<CategoryWarehouseStore> cwsOp = categoryWarehouseStoreRepository.findByCategoryIdAndWarehouseId(catOp.get().getId(),warehouse.getId());

       if(cwsOp.isPresent()){
           warehouseStore = cwsOp.get().getWarehouseStore();
       }

        Optional<Item> itemOp = itemRepository.findByCode(item.getCode());
        if(itemOp.isPresent()){
            Item itemExist = itemOp.get();
            List<ItemStock> itemStocks = itemExist.getStocks();
            itemStocks.add(new ItemStock(
                    itemRequestDto.getCurrentStockQty(),
                    itemExist,
                    StockType.STOCK_IN,warehouse,warehouseStore
            ));
            itemExist.setStocks(itemStocks);
            return;
        }

        if(item.getItemParentCategory()==null){
            throw new AesException("Item Main Category Missing");
        }


        catBrandOp.ifPresent(item::setBrand);

        item.setStocks(List.of(new ItemStock(
                itemRequestDto.getCurrentStockQty(),
                item,
                StockType.STOCK_IN, warehouse, warehouseStore
        )));
        if(itemRequestDto.getAttributes()!=null && !itemRequestDto.getAttributes().isEmpty()) {
            item.setAttributes(itemRequestDto.getAttributes().stream().map(itemAttribute -> {
                ItemAttribute itemAttr = new ItemAttribute();
                itemAttr.setItem(item);
                itemAttr.setAttributeType(itemAttribute.getAttributeType().trim());
                itemAttr.setAttributeValue(itemAttribute.getAttributeValue().trim());
                itemAttr.setAttributeUnit(itemAttribute.getAttributeUnit().trim());
                return itemAttr;
            }).toList());
        }
        item.setActive(false);
        itemRepository.save(item);
        
    }

    @Override
    @Transactional
    public void updateItem(Long id, ItemRequestDto itemRequestDto) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if(itemOptional.isEmpty()){
            throw new AesException("Item not found");
        }
        Item item = itemOptional.get();

        if(itemRequestDto.getName()!=null) {
            item.setName(itemRequestDto.getName());
        }

        itemOptional = itemRepository.findByCodeAndActive(itemRequestDto.getCode(),true);
        if(itemOptional.isPresent() && !id.equals(itemOptional.get().getId())){
            throw new AesException("Item already exist with same attributes");
        }

        if(itemRequestDto.getCode()!=null){
            item.setCode(itemRequestDto.getCode());
        }
        if(itemRequestDto.getItemCategory()!=null) {
            item.setItemCategory(itemRequestDto.getItemCategory());
        }


        if(itemRequestDto.getItemUnit()!=null) {
            item.setItemUnit(itemRequestDto.getItemUnit());
        }
        if(itemRequestDto.getStockThresholdQty()!=null) {
            item.setStockThresholdQty(itemRequestDto.getStockThresholdQty());
        }
        if(itemRequestDto.getReorderPercentage()!=null) {
            item.setReorderPercentage(itemRequestDto.getReorderPercentage());
        }
        if(itemRequestDto.getReorderPercentage()!=null) {
            item.setReorderPercentage(itemRequestDto.getReorderPercentage());
        }

        if(itemRequestDto.getCurrentStockQty()!=null){
            List<ItemStock> itemStocks = item.getStocks();

           List<ItemStock> filteredItemStocks = itemStocks.stream().filter(stock->stock.getWarehouse().getId()
                            .equals(itemRequestDto.getWarehouse().getId())
            ).toList();
           if(filteredItemStocks.size()==1 &&
                   (filteredItemStocks.get(0).getStockQty().equals(new BigDecimal("0.00")) ||
                           filteredItemStocks.get(0).getStockQty().equals(new BigDecimal("0.0000"))
                   )){
                itemStocks.add(new ItemStock(
                        itemRequestDto.getCurrentStockQty(),
                        item,
                        StockType.STOCK_IN,
                        new Warehouse(itemRequestDto.getWarehouse().getId()),
                        new WarehouseStore(itemRequestDto.getWarehouseStore().getId())
                ));
                item.setStocks(itemStocks);
           }
        }
        

        if(itemRequestDto.getAttributes()!=null && !itemRequestDto.getAttributes().isEmpty()) {
            item.setAttributes(itemRequestDto.getAttributes().stream().map(itemAttribute -> {
                itemAttribute.setItem(item);
                itemAttribute.setAttributeType(itemAttribute.getAttributeType().trim());
                itemAttribute.setAttributeValue(itemAttribute.getAttributeValue().trim());
                itemAttribute.setAttributeUnit(itemAttribute.getAttributeUnit().trim());
                return itemAttribute;
            }).toList());
        }

        if(itemRequestDto.getFunctionalUnits()!=null&& !itemRequestDto.getFunctionalUnits().isEmpty()){
            item.setItemFunctionalUnits(itemRequestDto.getFunctionalUnits().stream().map(itemFunctionalUnit -> {
                ItemFunctionalUnit functionalUnit = new ItemFunctionalUnit();
                functionalUnit.setId(itemFunctionalUnit.getId());
                functionalUnit.setValue(itemFunctionalUnit.getValue());
                functionalUnit.setUnit(itemFunctionalUnit.getUnit());
                functionalUnit.setItem(item);
                return functionalUnit;
            }).toList());
        }
        itemRepository.save(item);
    }

    @Override
    public void deleteItem(Long id, Long warehouseId, Long warehouseStoreId) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if(itemOptional.isEmpty()){
            throw new AesException("Sorry! item not found");
        }

        List<ItemStock> stocks = itemStockRepository.findByItemsAndWarehosueId(List.of(id),warehouseId);
        if(stocks.size()>1){
            throw new AesException("Sorry! Item has stock");
        }
        List<DemandDetail> demandDetails  = demandDetailRepository.findAllByItemIdAndWarehouseId(List.of(id),warehouseId);
        if(!demandDetails.isEmpty()){
            throw new AesException("Sorry! Item has some demand in this warehouse, so unable to remove");
        }

        for(ItemStock s : stocks){
            itemStockRepository.delete(s);
        }

    }

    @Override
    public void stockOut(Item item, BigDecimal qty,Long warehouseId, Long warehouseStoreId) {
        qty = qty.multiply(new BigDecimal(-1));
        itemStockService.setItemStockRepository(itemStockRepository);
        itemStockService.updateStock(item,qty,StockType.STOCK_OUT,warehouseId,warehouseStoreId);
    }

    

   @Override
   public void stockUpdateByDemand(Long warehouseId,
                                   DemandDetail demandDetail,
                                   StockType stockType) {

       var itemDetailOp = this.getItemDetailWithWarehouse(demandDetail.getItem().getId());

       if(itemDetailOp instanceof Optional && itemDetailOp.isPresent()){
           ItemDetail itemDetail = itemDetailOp.get();
           List<Map<String,Object>> warehouses = itemDetail.getWarehouses().get(warehouseId.toString());
           if(!warehouses.isEmpty()){
               Map<String,Object> warehouseStoreInfo = warehouses.get(0);
               if(stockType == StockType.STOCK_IN){

                   this.stockIn(new Item(itemDetail.getId()), demandDetail.getApprovedQuantity(),
                   (Long)warehouseStoreInfo.get(WAREHOUSE_ID_KEY),
                   (Long)warehouseStoreInfo.get(WAREHOUSE_STORE_ID_KEY));
               }
               if(stockType == StockType.STOCK_OUT){
                   this.stockOut(new Item(itemDetail.getId()), demandDetail.getApprovedQuantity(),
                   (Long)warehouseStoreInfo.get(WAREHOUSE_ID_KEY),
                   (Long)warehouseStoreInfo.get(WAREHOUSE_STORE_ID_KEY));
               }

           }

       }

   }

    @Override
    public void stockIn(Item item, BigDecimal qty,Long warehouseId, Long warehouseStoreId) {
        itemStockService.setItemStockRepository(itemStockRepository);
        itemStockService.updateStock(item,qty,StockType.STOCK_IN,warehouseId,warehouseStoreId);
    }



    @Override
    public String getNextItemCode() {
        Optional<Item> itemOp = itemRepository.findMaxOrderById();
        if(itemOp.isPresent()){
            Item item = itemOp.get();
            Long newProductId = item.getId() + 1;
            return String.format("%05d",newProductId);
        }
        return String.format("%05d",1);
    }

    @Override
    public List<ItemRepository.ItemInfoByAttribute> getByAttributes(Long brandId, String attribute, Long subCatId, Long warehouseId) {
        return itemRepository.findByAttributes(brandId,attribute,subCatId,warehouseId);
    }

    @Override
    @Transactional
    public void importItems(Optional<MultipartFile> fileOp) {
        Path path = Path.of(uploadDir+"/inventory-mgm/items");
        FileUploadResponse fileUploadResponse = null;
        if(fileOp.isPresent()){
            fileUploadResponse = fileUploadService.uploadFile(path, fileOp.get());
            try {
                Iterable<CSVRecord> records = getItemRecords(fileUploadResponse);
                for(CSVRecord r : records){
                    String subCatName = r.get("SUB_CATEGORY");
                    String brandName = r.get("BRAND_NAME");
                    String itemAttribute = r.get("ITEM_ATTRIBUTE_NAME");
                    String storeId = r.get("STORE_ID");
                    String currentStock = r.get("CURRENT_STOCK");
                    String safetyStock = r.get("SAFETY_STOCK");
                    String unitMeasurement = r.get("UNIT_MEASUREMENT");
                    String reorderPercent = r.get("REORDER_PERCENTAGE");

                    Optional<ItemCategory> subCatOp = categoryService.getItemCategoryByName(subCatName);
                    if(subCatOp.isEmpty()){
                        throw new AesException("Item Sub Category Not found");
                    }

                    ItemCategory subCat = subCatOp.get();
                    Optional<CategoryBrand> catBrandOp = catBrandRepo.findByCategoryIdAndName(subCat.getId(),brandName);
                    if(catBrandOp.isEmpty()){
                        throw new AesException("Brand not found");
                    }

                    if(!itemAttribute.isEmpty()){
                        CategoryBrand catBrand = catBrandOp.get();
                        Optional<Item> itemOp = itemRepository.findByBrandIdAndItemAttributeName(catBrand.getId(),itemAttribute);
                        if(itemOp.isPresent() && storeId !=null){
                            Optional<WarehouseStore> wsOp = warehouseStoreRepository.findById(Long.parseLong(storeId));
                            if(wsOp.isPresent()){
                                WarehouseStore ws = wsOp.get();
                                Item item = itemOp.get();
                                BigDecimal stockQty = new BigDecimal(0);
                                for(ItemStock stock : item.getStocks()){
                                    stockQty = stockQty.add(stock.getStockQty());
                                }
                                if(stockQty.compareTo(new BigDecimal(0)) == 0){
                                    stockIn(itemOp.get(), BigDecimal.valueOf(Double.parseDouble(currentStock)), ws.getWarehouse().getId(), ws.getId());
                                }
                                if(!safetyStock.isEmpty()){
                                    item.setStockThresholdQty(BigDecimal.valueOf(Double.parseDouble(safetyStock)));
                                }
                                if(!unitMeasurement.isEmpty()){
                                    item.setItemUnit(unitMeasurement);
                                }

                                if(!reorderPercent.isEmpty()){
                                    item.setReorderPercentage(BigDecimal.valueOf(Double.parseDouble(reorderPercent)));
                                }


                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                throw new AesException(e.getMessage());
            } catch (IOException e) {
                throw new AesException("File Columns are not valid for extracting value: "+e.getMessage());
            }
        }
    }

    private Iterable<CSVRecord> getItemRecords(FileUploadResponse fileUploadResponse) throws IOException{
        FileReader in = new FileReader(fileUploadResponse.getPath()+"/"+fileUploadResponse.getFilename());
        Iterable<CSVRecord> records  = CSVFormat.DEFAULT.builder().setHeader(ItemHeader.class).build().parse(in);
        records.iterator().next();
        return records;
    }

    @Override
    @Transactional
    public void syncItemsBySubCatCode(Jwt token, Long warehouseId, Long warehouseStoreId, String subCatCode) {

        Optional<ItemCategory> categoryOp = categoryService.getCategoryByCode(subCatCode);

        if(categoryOp.isEmpty()) {
            throw new AesException("Sorry! Category Not found");
        }
        ItemCategory category = categoryOp.get();
        categoryService.syncCategories(token, cpsConfig, warehouseId, warehouseStoreId,
                Collections.singletonList(category.getCpsCategoryId()));
        List<SyncItemDetail> items = this.fetchItemsBySubCat(token,subCatCode.substring(2));
        List<ScmItemUpdateDto> dtos = new ArrayList<>();
        items.forEach(i->{
           ScmItemUpdateDto scmItemUpdateDto = new ScmItemUpdateDto();
           Item item = this.createItem(token,warehouseId,warehouseStoreId,i);
           scmItemUpdateDto.setItemIdCps(i.getId());
           scmItemUpdateDto.setItemIdScm(item.getId());
           dtos.add(scmItemUpdateDto);
        });
        if(!dtos.isEmpty()){
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
            orgOp.ifPresent(org->
                headers.set("orgId", org.getCpsVendorRegistrationId().toString())
            );
            HttpEntity<List<ScmItemUpdateDto>> payload = new HttpEntity<>(dtos,headers);
            String url = cpsConfig.getItemsEndpoint().concat("/update-scm-id");
            networkService.put(url,payload,Void.class);
        }
    }

    /**
     * @Description import from cps
     * @param warehouseId
     * @param warehouseStoreId
     * @param syncItemDetail
     * @return
     */
    @Transactional
    private Item createItem(Jwt token,Long warehouseId, Long warehouseStoreId, SyncItemDetail syncItemDetail){
        claimResolver.setToken(token);
        Item item = syncItemDetail.getEntity();
        String itemAttributeName = generateItemAttributeName(syncItemDetail.getAttributes());

        Optional<Warehouse> warehouseOp = warehouseService.getWarehouse(warehouseId);
        if(warehouseOp.isEmpty()){
            throw new AesException("Sorry! Warehouse not found");
        }

        Optional<WarehouseStore> warehouseStoreOp = warehouseStoreService.getStoreById(warehouseStoreId);
        if(warehouseStoreOp.isEmpty()){
            throw new AesException("Sorry! Warehouse Store not found");
        }

        Warehouse warehouse = warehouseOp.get();
        WarehouseStore warehouseStore = warehouseStoreOp.get();

        item.setCode(warehouseStore.getStoreName().substring(0,1).toUpperCase()+"-"+item.getCode());

        String subCategoryCode = warehouseStore.getStoreName().substring(0,1).toUpperCase()+"-"+syncItemDetail.getItemCategory().code();
        // Get Subcategory By Code
        Optional<ItemCategory> subCatOp = categoryService.getCategoryByCode(subCategoryCode);
        if(subCatOp.isEmpty()){
            throw new AesException("Sorry! Sub Category not found");
        }
        ItemCategory subCat = subCatOp.get();

        // Get Category Brand
        CategoryBrand catBrand = null;
        if(syncItemDetail.getBrand()!=null) {
            Optional<CategoryBrand> catBrandOp = categoryBrandRepository.findByCategoryIdAndName(subCat.getId(), syncItemDetail.getBrand().name());
            if (catBrandOp.isEmpty()) {
                catBrand = new CategoryBrand();
                catBrand.setCategory(subCat);
                catBrand.setName(syncItemDetail.getBrand().name());
                categoryBrandRepository.save(catBrand);
            } else {
                catBrand = catBrandOp.get();
            }
        }

        item.setItemCategory(subCat);
        item.setItemParentCategory(subCat.getParentCategory());
        item.setBrand(catBrand);
        item.setCpsItemId(syncItemDetail.getId());

        List<?> itemExistByAttr = this.getByAttributes(catBrand.getId(),itemAttributeName,subCat.getId(),warehouseId);
        if(!itemExistByAttr.isEmpty()){
            List<Item> items = itemRepository.findByBrandIdAndItemCategoryIdAndItemAttributeName(catBrand.getId(), subCat.getId(), itemAttributeName);
            for(Item i : items){
                Optional<ItemImportLog> itemImportExistOp = itemImportLogRepository.findByItemIdAndWarehouseId(i.getId(), warehouseId);
                if(itemImportExistOp.isPresent()){
                    ItemImportLog iil = itemImportExistOp.get();

                    i.setActive(iil.getItemInactiveStatus().equals(ItemInactiveStatus.APPROVED));

                }else{
                    i.setActive(false);
                }
            }
        } else {

            Optional<Item> itemExistByCode = itemRepository.findByCode(item.getCode());
            if(itemExistByCode.isPresent()){
                item = itemExistByCode.get();
                List<ItemStock> stocks = item.getStocks();
                if(stocks.isEmpty()) {
                    stocks.add(new ItemStock(
                            new BigDecimal(0l),
                            item,
                            StockType.STOCK_IN,
                            warehouse,
                            warehouseStore
                    ));
                    item.setStocks(stocks);
                }else{
                    Boolean warehouseExist=false;
                    for(ItemStock s : stocks){
                        if(s.getWarehouse().getId().equals(warehouseId)){
                            warehouseExist=true;
                        }
                        Optional<ItemImportLog> importLogExist = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),s.getWarehouse().getId());
                        if(importLogExist.isPresent()) {
                            ItemImportLog itemImportLog = importLogExist.get();
                            ItemImportLog iil = new ItemImportLog();
                            iil.setItem(itemImportLog.getItem());
                            iil.setWarehouse(warehouse);
                            iil.setItemInactiveStatus(itemImportLog.getItemInactiveStatus());
                            itemImportLogRepository.save(iil);
                        }
                    }
                    if(Boolean.FALSE.equals(warehouseExist)){
                        stocks.add(new ItemStock(
                                new BigDecimal(0L),
                                item,
                                StockType.STOCK_IN,
                                warehouse,
                                warehouseStore
                        ));
                        item.setStocks(stocks);
                    }
                }


            }else{
                item.setItemUnit(syncItemDetail.getItemUnit());
                item.setManufacturer(syncItemDetail.getManufacturer());
                item.setName(syncItemDetail.getName());
                item.setItemAttributeName(itemAttributeName);
                item.setActive(false);
                item.setStocks(Arrays.asList(new ItemStock(
                        new BigDecimal(0l),
                        item,
                        StockType.STOCK_IN,
                        warehouse,
                        warehouseStore
                )));
//                item.setItemInactiveStatus(ItemInactiveStatus.PENDING_VERIFICATION);

                if(syncItemDetail.getAttributes()!=null && !syncItemDetail.getAttributes().isEmpty()) {

                    Item finalItem = item;
                    item.setAttributes(syncItemDetail.getAttributes().stream().map(itemAttribute -> {

                        itemAttribute.setId(null);
                        itemAttribute.setItem(finalItem);
                        return itemAttribute;
                    }).toList());
                }

                if(syncItemDetail.getFunctionalUnits()!=null && !syncItemDetail.getFunctionalUnits().isEmpty()) {
                    Item finalItem = item;
                    item.setItemFunctionalUnits(syncItemDetail.getFunctionalUnits().stream().map(itemFunctionalUnit -> {
                        itemFunctionalUnit.setId(null);
                        itemFunctionalUnit.setItem(finalItem);
                        return itemFunctionalUnit;
                    }).toList());
                }

                itemRepository.save(item);

                Optional<ItemImportLog> importLogExist = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),warehouseId);
                if(importLogExist.isEmpty()) {
                    ItemImportLog iil = new ItemImportLog();
                    iil.setItem(item);
                    iil.setWarehouse(warehouse);
                    iil.setItemInactiveStatus(ItemInactiveStatus.PENDING_VERIFICATION);
                    // added for inactive account service
                    item.setActive(false);
                    itemImportLogRepository.save(iil);
                }
                accountService.setItemService(this);
                accountService.createItemLedger(claimResolver,item,warehouse,warehouseStore);
            }


    

        }
        return item;
    }

    @Transactional
    private List<SyncItemDetail> fetchItemsBySubCat(Jwt token, String subCatCode){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
        if(orgOp.isPresent()){
            headers.setBearerAuth(token.getTokenValue());
            headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
        }
        HttpEntity<?> payload = new HttpEntity<>(headers);
        String url = cpsConfig.getItemFetchEndpoint(subCatCode);
        ResponseEntity<?> response = networkService.get(url, payload, SyncItemDto.class);
        if(response.getStatusCode()!=HttpStatus.OK){
            throw new AesException("Unable to fetch Items from CPS");
        }
        var responseBody = response.getBody();
        
        SyncItemDto syncItemDto = (SyncItemDto)responseBody;
        
        return syncItemDto.getItems();
    }

    @Override
    public Optional<Item> getByBrandAndAttributeName(String brandName, Long subCatId, String itemAttributeName) {
        
        Optional<CategoryBrand> catBrandOp = categoryBrandRepository.findByCategoryIdAndName(subCatId, brandName);
        if(catBrandOp.isPresent()){
            
            return itemRepository.findByBrandIdAndItemAttributeName(catBrandOp.get().getId(),itemAttributeName);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public void approveItemFromCps(Jwt token, Long id, ItemApproveRequestDto approveRequestDto) {
        claimResolver.setToken(token);
        Optional<Warehouse> warehouseOp = warehouseService.getWarehouse(approveRequestDto.getWarehouseId());
        if(warehouseOp.isEmpty()){
            throw new AesException("Warehouse Missing");
        }
        Optional<WarehouseStore> warehouseStoreOp = warehouseStoreService.getStoreById(approveRequestDto.getWarehouseStoreId());
        if(warehouseStoreOp.isEmpty()){
            throw new AesException("Warehouse Store Missing");
        }
        WarehouseStore ws = warehouseStoreOp.get();
        Optional<Item> itemOp = itemRepository.findById(id);
        if(itemOp.isPresent()){
            Item item = itemOp.get();
            ItemMergeRequestDto itemMergeRequestDto = approveRequestDto.getItemMergeRequestDto();
            if(approveRequestDto.getApproveStatus().equals(ApproveStatus.APPROVED)) {
                if(approveRequestDto.getCode()==null && approveRequestDto.getItemMergeRequestDto()==null) {
                    Optional<ItemImportLog> iilOp = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),warehouseOp.get().getId());
                    if(iilOp.isPresent()){
                        ItemImportLog iil = iilOp.get();
                        iil.setItemInactiveStatus(ItemInactiveStatus.PENDING_VERIFICATION);
                        item.setActive(false);
                        if(item.getUserItemId()!=null){
                            Optional<UserItem> userItemOp = userItemRepository.findById(item.getUserItemId());
                            userItemOp.ifPresent(ui->
                                ui.setItemStatus(UserCategoryStatus.PENDING_FROM_ACCOUNT)
                            );
                        }
                    }
                    accountService.setItemService(this);
                    accountService.createItemLedger(item, warehouseOp.get(),warehouseStoreOp.get());
                }
                if(approveRequestDto.getCode()==null && approveRequestDto.getItemMergeRequestDto()!=null){
                    item.setItemInactiveStatus(null);
                    Optional<ItemImportLog> iilOp = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),warehouseOp.get().getId());
                    if(iilOp.isPresent()){
                        ItemImportLog iil = iilOp.get();
                        iil.setItemInactiveStatus(ItemInactiveStatus.PENDING_VERIFICATION);
                        item.setActive(false);
                        if(item.getUserItemId()!=null){
                            Optional<UserItem> userItemOp = userItemRepository.findById(item.getUserItemId());
                            userItemOp.ifPresent(ui->
                                ui.setItemStatus(UserCategoryStatus.PENDING_FROM_ACCOUNT)
                            );
                        }
                    }
                    mergeItem(item, approveRequestDto.getWarehouseStoreId(), itemMergeRequestDto);
                    accountService.setItemService(this);
                    accountService.createItemLedger(item, warehouseOp.get(),warehouseStoreOp.get());

                }
            }else if(approveRequestDto.getApproveStatus().equals(ApproveStatus.REJECTED)){
                UserItem ui = null;
//                item.setItemInactiveStatus(ItemInactiveStatus.REJECTED);
                Optional<ItemImportLog> iilOp = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),warehouseOp.get().getId());
                if(iilOp.isPresent()){
                    ItemImportLog iil = iilOp.get();
                    iil.setItemInactiveStatus(ItemInactiveStatus.REJECTED);
                    if(item.getUserItemId()!=null){
                        Optional<UserItem> userItemOp = userItemRepository.findById(item.getUserItemId());
                        if(userItemOp.isPresent()){
                            ui = userItemOp.get();
                                if(Boolean.TRUE.equals(approveRequestDto.getIsMerged())) {
                                    ui.setItemStatus(UserCategoryStatus.MERGED);
                                }else{
                                    ui.setItemStatus(UserCategoryStatus.REJECTED);
                                }
                        }

                    }
                }
                if(approveRequestDto.getItemMergeRequestDto()!=null) {
                    String storePrefixedItemCode = ws.getStoreName().substring(0,1)+"-"+approveRequestDto.getCode();
                    itemOp = itemRepository.findByCodeAndActive(storePrefixedItemCode,true);
                    if(itemOp.isEmpty()){
                        throw new AesException("Sorry! Item not found using code ["+approveRequestDto.getCode()+"]");
                    }
                    Item existItem = itemOp.get();
                    if(ui!=null){
                        String itemName = (existItem.getName().trim()!=null)? existItem.getName():"";
                        if(!itemName.isEmpty()){
                            itemName+="-";
                        }
                        itemName += (existItem.getItemAttributeName().trim()!=null)? existItem.getItemAttributeName():"";
                        ui.setMergedItem(itemName);
                    }
                    mergeItem(existItem, approveRequestDto.getWarehouseStoreId(), itemMergeRequestDto);
                }
            }
        }
    }

    private void mergeItem(Item item, Long warehouseStoreId, ItemMergeRequestDto itemMergeRequestDto) {

        Optional<WarehouseStore> wsOp = warehouseStoreRepository.findById(warehouseStoreId);
        if(wsOp.isEmpty()){
            throw new AesException("Sorry! Warehouse Store not found");
        }
        WarehouseStore ws = wsOp.get();
        Optional<ItemCategory> categoryOp = categoryService.getAnyItemCategory(itemMergeRequestDto.getItemParentCategory().getId());
        Optional<ItemCategory> subCategoryOp = categoryService.getAnyItemCategory(itemMergeRequestDto.getItemCategory().getId());
        if(categoryOp.isPresent()){
            item.setItemCategory(subCategoryOp.get());
        }
        if(subCategoryOp.isPresent()){
            item.setItemParentCategory(categoryOp.get());
        }
        item.setName(itemMergeRequestDto.getName());
        item.setItemAttributeName(itemMergeRequestDto.getItemAttributeName());
        List<ItemAttribute> attributes = itemMergeRequestDto.getAttributes().stream().map(attr->{
            ItemAttribute iAttr = new ItemAttribute();
            Optional<ItemAttribute> itemAttrOp = itemAttributeRepository.findAllByAttributeTypeAndAttributeUnitAndItemId(attr.getAttributeType(),
                    attr.getAttributeUnit(), item.getId());
            if(itemAttrOp.isPresent()){
                iAttr = itemAttrOp.get();
                iAttr.setAttributeValue(attr.getAttributeValue());
            }else{
                iAttr.setItem(item);
                iAttr.setAttributeType(attr.getAttributeType());
                iAttr.setAttributeUnit(attr.getAttributeUnit());
                iAttr.setAttributeValue(attr.getAttributeValue());
            }

            return iAttr;
        }).toList();
        item.setItemUnit(itemMergeRequestDto.getItemUnit());
        item.setAttributes(attributes);
        Optional<CategoryBrand> categoryBrandOp = categoryBrandRepository
                .findByCategoryIdAndName(item.getItemCategory().getId(),
                        itemMergeRequestDto.getBrand());
        categoryBrandOp.ifPresent(item::setBrand);

        String storeWiseItemCode= ws.getStoreName().substring(0,1)+"-"+itemMergeRequestDto.getCode();
        item.setCode(storeWiseItemCode);
    }

    @Override
    public List<ItemRepository.ItemTemplateInfo> getTemplateData(Long categoryId, Long subCategoryId,
                                                                 Long warehouseId, Long warehouseStoreId) {
        return itemRepository.fetchTemplateData(categoryId,subCategoryId,
                warehouseId,warehouseStoreId);
    }

    @Override
    @Transactional
    public void approveItemFromAcc(Long id, Long warehouseId) {
        List<ItemImportLog> itemImportLogs = itemImportLogRepository.findByItemId(id);
        if(!itemImportLogs.isEmpty()){
            for(ItemImportLog iil : itemImportLogs) {
                iil.setItemInactiveStatus(ItemInactiveStatus.APPROVED);
                Item item = iil.getItem();
                item.setActive(true);
                if(item.getUserItemId()!=null){
                    Optional<UserItem> userItemOp = userItemRepository.findById(item.getUserItemId());
                    userItemOp.ifPresent(ui->
                        ui.setItemStatus(UserCategoryStatus.COMPLETED)
                    );
                }
            }
        }
    }

    @Override
    @Transactional
    public void rejectItemFromAcc(Long id, Long warehouseId) {
        List<ItemImportLog> itemImportLogs = itemImportLogRepository.findByItemId(id);
        if(itemImportLogs.isEmpty()){
            throw new AesException("Sorry! Item doesn't exist");
        }

        for(ItemImportLog iil : itemImportLogs) {
            iil.setItemInactiveStatus(ItemInactiveStatus.REJECTED);
            Item item = iil.getItem();
            item.setActive(true);
            if(item.getUserItemId()!=null){
                Optional<UserItem> userItemOp = userItemRepository.findById(item.getUserItemId());
                userItemOp.ifPresent(ui->
                    ui.setItemStatus(UserCategoryStatus.REJECTED)
                );
            }
        }

    }

    @Override
    public List<Item> getByCode(String itemCode) {
        return itemRepository.findByCodeLikeCode(itemCode);
    }

    @Override
    public List<ItemStock> getByItemAndWarehouse(List<Long> itemIds, Long warehouseId) {
        return itemStockRepository.findByItemsAndWarehosueId(itemIds,warehouseId);
    }

    @Override
    @Transactional
    public void forceActivev2(Jwt token,ForceActiveRequestDto forceActiveRequestDto) {
        claimResolver.setToken(token);
        if(Boolean.TRUE.equals(claimResolver.isAdmin()) && forceActiveRequestDto.getPhoneNo()!=null &&
                    !forceActiveRequestDto.getPhoneNo().equals("01714112912")){
            throw new AesException("Page Not Found");
        }
        itemImportLogRepository.forceActive();
        itemRepository.forceActive();

    }

    @Override
    @Transactional
    public void forceActive() {
        itemImportLogRepository.forceActive();
        itemRepository.forceActive();
    }
}
