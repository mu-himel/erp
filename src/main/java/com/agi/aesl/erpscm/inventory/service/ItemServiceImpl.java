package com.agi.aesl.erpscm.inventory.service;

// import com.agi.aesl.erpscm.authentication.dto.ClaimResponseDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.common.ItemAttributeInterface;
import com.agi.aesl.erpscm.common.ItemInterface;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseStoreService;
import com.agi.aesl.erpscm.demand.dto.request.PendingAttributeDto;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
//import com.agi.aesl.erpscm.demand.entity.DemandDetail;
//import com.agi.aesl.erpscm.demand.repository.DemandDetailRepository;
//import com.agi.aesl.erpscm.demand.repository.DemandRepository;
import com.agi.aesl.erpscm.demand.repository.DemandDetailRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.fileupload.service.FileUploadService;
// import com.agi.aesl.erpscm.indent.entity.Indent;
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
// import com.agi.aesl.erpscm.scm.dto.request.OfferRequestDto;
import com.agi.aesl.erpscm.network.NetworkService;

import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.parameters.P;
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
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ClaimResolver claimResolver;


//    @Autowired
//    private DemandDetailRepository demandDetailRepository;

    @Autowired
    private ItemStockRepository itemStockRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryBrandRepository catBrandRepo;

    @Autowired
    private OrgService orgService;

    @Autowired
    private CpsServerConfig cpsConfig;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseStoreService warehouseStoreService;
    @Autowired
    private CategoryBrandRepository categoryBrandRepository;

    @Autowired
    private CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;

    @Autowired
    private ItemAttributeRepository itemAttributeRepository;

    @Autowired
    private DemandDetailRepository demandDetailRepository;

    @Autowired
    private ItemImportLogRepository itemImportLogRepository;

    @Autowired
    private WarehouseStoreRepository warehouseStoreRepository;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Autowired
    private ItemFuncationalUnitRepository itemFuncationalUnitRepository;

    @Autowired
    private UserItemRepository userItemRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public Optional<Item> getItemDetail(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public Optional<?> getItemDetailWithWarehouse(Long id) {
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
        detail.getStocks().stream().forEach(itemStock -> {
            Optional<BigDecimal> inTransit  = itemRepository.findInTransitByItemAndWarehouse(itemStock.getItem().getId(),itemStock.getWarehouse().getId());
           if(warehouses.containsKey(""+itemStock.getWarehouse().getId())) {
               List<Map<String,Object>> itemStocks = warehouses.get(""+itemStock.getWarehouse().getId());

               Optional<Map<String,Object>> mapOp = itemStocks.stream().filter(
                       iStock->{
                           return ((Long)iStock.get("warehouseId")).equals(itemStock.getWarehouse().getId())
                                   && ((Long)iStock.get("warehouseStoreId")).equals(itemStock.getWarehouseStore().getId());
                       }).findFirst();
                 processItemStock(itemStock,inTransit,warehouses,itemStocks,mapOp);

           }else{
               List<Map<String,Object>> itemStocks = new ArrayList<>();
               processItemStock(itemStock,inTransit,warehouses,itemStocks,Optional.ofNullable(null));

           }
        });
        itemDetail.setWarehouses(warehouses);

        return Optional.ofNullable(itemDetail);
    }

    @Override
    public Optional<?> getItemDetailWithWarehouseWithoutInTransit(Long id) {
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
        detail.getStocks().stream().forEach(itemStock -> {
            Optional<BigDecimal> inTransit  = Optional.empty();// itemRepository.findInTransitByItemAndWarehouse(itemStock.getItem().getId(),itemStock.getWarehouse().getId());
            if(warehouses.containsKey(""+itemStock.getWarehouse().getId())) {
                List<Map<String,Object>> itemStocks = warehouses.get(""+itemStock.getWarehouse().getId());

                Optional<Map<String,Object>> mapOp = itemStocks.stream().filter(
                        iStock->{
                            return ((Long)iStock.get("warehouseId")).equals(itemStock.getWarehouse().getId())
                                    && ((Long)iStock.get("warehouseStoreId")).equals(itemStock.getWarehouseStore().getId());
                        }).findFirst();
                processItemStock(itemStock,inTransit,warehouses,itemStocks,mapOp);

            }else{
                List<Map<String,Object>> itemStocks = new ArrayList<>();
                processItemStock(itemStock,inTransit,warehouses,itemStocks,Optional.ofNullable(null));

            }
        });
        itemDetail.setWarehouses(warehouses);

        return Optional.ofNullable(itemDetail);
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
        stockInfo.put("warehouseId",itemStock.getWarehouse().getId());
        stockInfo.put("warehouseName",itemStock.getWarehouse().getName());
        stockInfo.put("warehouseStoreId",itemStock.getWarehouseStore().getId());
        stockInfo.put("warehouseStoreName",itemStock.getWarehouseStore().getStoreName());
        if(mapOp.isEmpty()) {
            itemStocks.add(stockInfo);
        }
        warehouses.put(""+itemStock.getWarehouse().getId(), itemStocks);
    }

    @Override
    public Page<?> getAllItems(
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
            List<Long> filterBy = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
            warehouseIds = filterBy;
        }

        if(categoryId.isPresent()){
            categoryIds.add(categoryId.get());
        }else{
            categoryIds = dataFilter.getCategoryIds();
        }


        Page<?> result  = itemRepository.findAllItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryIds,
                subCategoryId.orElse(null),
                warehouseIds,
                warehouseStoreId.orElse(null),
                pageable);


        return result;
    }

    @Override
    public Page<?> getPendingAllItems(Jwt token,Optional<Integer> page, Optional<Integer> size, Optional<String> name, Optional<String> code, Optional<Integer> reorderPercentage, Optional<Integer> stockThresholdQty, Optional<Long> categoryId, Optional<Long> subCategoryId, Optional<Long> warehouseId, Optional<Long> warehouseStoreId) {
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

        Page<?> result  = itemRepository.findAllPendingItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryIds,
                subCategoryId.orElse(null),
                warehouseIds,
                warehouseStoreId.orElse(null),
                pageable);


        return result;
    }

    @Override
    public Page<?> getPendingVerificationAllItems(
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
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = filterBy;
        }

        if(categoryId.isPresent()){
            categoryIds.add(categoryId.get());
        }else{
            categoryIds = dataFilter.getCategoryIds();
        }

        Page<?> result  = itemRepository.findAllPendingVerificationItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryIds,
                subCategoryId.orElse(null),
                warehouseIds,
                warehouseStoreId.orElse(null),
                pageable);


        return result;
    }

    @Override
    public List<?> getAllItems(Optional<Long> categoryId, Optional<String> name, Optional<String> code) {


        if(categoryId.isPresent()){
            List<?> result = new ArrayList<>();
            if(name.isPresent()){
                result = itemRepository
                        .findAllByActiveAndItemCategoryIdOrItemParentCategoryIdAndNameLikeIgnoreCaseOrCodeLikeIgnoreCase(
                        true,categoryId,categoryId,name.get()+"%",name.get()+"%");

            }
            return result;
        }

        if(name.isPresent() && code.isEmpty()){
            System.out.println(name.get());
            return itemRepository.findAllByActiveAndNameLikeIgnoreCase(true,name.get()+"%");
        }
        if(name.isEmpty() && code.isPresent()){
            return  itemRepository.findAllByActiveAndCodeLikeIgnoreCase(true, code.get()+"%");
        }
        return new ArrayList<>();
    }

    @Override
    public List<?> getAllItemsBySubCategoryAndAttribute(
                                                        Optional<Long> warehouseId,
                                                        Optional<Long> brandId,
                                                        Optional<Long> subCategoryId,
                                                        Optional<String> name,
                                                        Optional<String> code,
                                                        Optional<String> sattributes,
                                                        Optional<String> attributeType,
                                                        Optional<String> attributeValue) {
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

                String _attributeType = attrTypes[i];
                String _attributeValue = attrValues[i];
                String _attributeUnit = attrUnits[i];
                
                _attributeType = _attributeType.substring(0, _attributeType.indexOf("_"));
                _attributeValue = _attributeValue.substring(0, _attributeValue.indexOf("_"));
                _attributeUnit = _attributeUnit.substring(0, _attributeUnit.indexOf("_"));

                if(!attrs.containsKey(_attributeType)){
                    attrs.put("attributeValue",_attributeValue);
                    attrs.put("attributeType",_attributeType);
                    attrs.put("attributeUnit",_attributeUnit);
                    attributes.add(attrs);
                    sb.append(_attributeType).append(" ").append(_attributeValue).append(" ").append(_attributeUnit)
                    .append(" - ");
                }
                dto.setAttributes(attributes);
            }
            dto.setItemAttribute(sb.toString().substring(0,sb.length()-3));
            itemListWithAttributesDtos.add(dto);

        }
        List<ItemListWithAttributesDto> filteredList = new ArrayList<>();
        filteredList = itemListWithAttributesDtos;
        
        if(sattributes.isPresent()){
            String _attr = sattributes.get().replaceAll("  "," ");
            
            filteredList = itemListWithAttributesDtos.stream().filter(itemListWithAttributesDto->{
                String _perItemAttr = "";
                if(_attr.contains(itemListWithAttributesDto.getBrandName()) &&  itemListWithAttributesDto.getBrandName()!=null){
                    _perItemAttr = itemListWithAttributesDto.getBrandName() + " - " + itemListWithAttributesDto.getItemAttribute().replaceAll("  ", " ");
                }else{
                    _perItemAttr = itemListWithAttributesDto.getItemAttribute();;
                }
                return (_attr.contains(_perItemAttr) || _perItemAttr.contains(_attr));
            }).collect(Collectors.toList());

        }

        return filteredList;

    }

//    private String generateItemAttribute(List<ItemAttribute> attributes){
//        StringBuilder sb = new StringBuilder();
//
//        attributes.stream().forEach(itemAttribute -> {
//            sb.append(itemAttribute.getAttributeType().trim()
//                    +" "+itemAttribute.getAttributeValue().trim()
//                    +" "+itemAttribute.getAttributeUnit().trim());
//            sb.append(" - ");
//        });
//
//        return (sb.isEmpty())? "" :  sb.toString().substring(0,sb.length()-3);
//    }

    private String generateItemAttributeName(List<ItemAttribute> attributes){
        StringBuilder sb = new StringBuilder();

        attributes.stream().forEach(itemAttribute -> {
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
            throw new RuntimeException("Sorry! Warehouse not found");
        }
        Optional<WarehouseStore> wsOp = warehouseStoreRepository.findById(itemRequestDto.getWarehouseStore().getId());
        if(wsOp.isEmpty()){
            throw new RuntimeException("Sorry! Warehouse Store not found");
        }
        warehouse= wOp.get();
        warehouseStore = wsOp.get();
//        item.setCode(itemRequestDto.getCode());
        Long brandId = (itemRequestDto.getBrand()!=null)? itemRequestDto.getBrand().getId() : null;
       List<?> itemExistByAttr = this.getByAttributes(brandId,itemAttributeName,item.getItemCategory().getId(),warehouse.getId());
       if(itemExistByAttr.size()>0){
           throw new AesException("Sorry! Item Already exist with same attributes for this brand");
       }

        Optional<Item> itemOp = itemRepository.findByCode(item.getCode());
        if(itemOp.isPresent()){
            Item itemExist = itemOp.get();
            Optional<ItemImportLog> iilOp = itemImportLogRepository.findByItemIdAndWarehouseId(itemExist.getId(),warehouse.getId());
            if(iilOp.isPresent()){
                throw new RuntimeException("Item already exist with code("+itemExist.getCode()+") and status is "+iilOp.get().getItemInactiveStatus());
            }
//            List<ItemStock> itemStocks = itemExist.getStocks();
//            itemStocks.add(new ItemStock(
//                    new BigDecimal(0L),
//                    itemExist,
//                    StockType.STOCK_IN,
//                    warehouse,
//                    warehouseStore
//            ));
//            itemExist.setStocks(itemStocks);
//            return;
        }

       if(item.getItemParentCategory()==null && item.getItemCategory()==null){
           throw new AesException("Item Sub Category Missing");
       }

        if(item.getItemParentCategory()==null){
            throw new AesException("Item Main Category Missing");
        }

//        item.setWarehouse(null);
//        item.setWarehouseStore(null);

        if(itemRequestDto.getBrand()!=null && itemRequestDto.getBrand().getId()!=null){
            item.setBrand(new CategoryBrand(itemRequestDto.getBrand().getId()));
        }

        item.setStocks(Arrays.asList(new ItemStock(
                ((itemRequestDto.getCurrentStockQty()!=null)? itemRequestDto.getCurrentStockQty() : new BigDecimal(0)),
                item,
                StockType.STOCK_IN,warehouse,warehouseStore
                )));
        if(itemRequestDto.getAttributes()!=null && itemRequestDto.getAttributes().size()>0) {
            item.setAttributes(itemRequestDto.getAttributes().stream().map(itemAttribute -> {
                itemAttribute.setItem(item);
                itemAttribute.setAttributeType(itemAttribute.getAttributeType().trim());
                itemAttribute.setAttributeValue(itemAttribute.getAttributeValue().trim());
                itemAttribute.setAttributeUnit(itemAttribute.getAttributeUnit().trim());
                return itemAttribute;
            }).collect(Collectors.toList()));
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
    public  <T extends Item> void sendItemToCps(ClaimResolver claimResolver, String _employee, T item,
                                                         List<ItemAttributeInterface> attributes,
                                                         WarehouseStore warehouseStore
                                                         ) {
        PendingItemRequestDto pendingItemRequestDto = new PendingItemRequestDto();
        Employee employee = claimResolver.getEmployee().orElse(null);
        pendingItemRequestDto.setItemAttributeName(item.getItemAttributeName());
        if(employee!=null) {
            pendingItemRequestDto.setRequestedBy(_employee);
            pendingItemRequestDto.setDesignation(employee.getDesignationName());
            pendingItemRequestDto.setDepartment(employee.getDepartmentName());
            pendingItemRequestDto.setWarehouseId(employee.getWarehouseId());
            if(warehouseStore!=null){
                pendingItemRequestDto.setWarehouseStoreId(warehouseStore.getId());
            }

            pendingItemRequestDto.setWarehouseName(employee.getWarehouseName());
        }else{
            throw new RuntimeException("Sorry! Employee Info missing");
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
        }).collect(Collectors.toList()));
        pendingItemRequestDto.setItemUnit(item.getItemUnit());
        pendingItemRequestDto.setScmItemId(item.getId());
//        pendingItemRequestDto.setCode(item.getCode().substring(2));

        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        if(orgOp.isPresent()){
            pendingItemRequestDto.setOrganizationId(orgOp.get().getCpsVendorRegistrationId());
            headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
//            itemRequestDto.setOrgId(orgOp.get().getId());
        }
        HttpEntity<PendingItemRequestDto> payload = new HttpEntity<>(pendingItemRequestDto,headers);
        String url = cpsConfig.getPendingItemReqEndpoint();
        ResponseEntity<?> response = networkService.post(url,payload,Map.class);
        Map<String,Object> map = (Map<String, Object>) response.getBody();
        if(map!=null && map.containsKey("code") && map.containsKey("pendingItemRequestId") ) {
            item.setCode(warehouseStore.getStoreName().substring(0,1).toUpperCase().concat("-").concat(map.get("code").toString()));
            item.setPendingReqItemId(Long.parseLong(map.get("pendingItemRequestId").toString()));
        }
        System.out.println("STATUS CODE: "+response.getStatusCode());
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
       if(itemExistByAttr.size()>0){
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

//        item.setWarehouse(null);
//        item.setWarehouseStore(null);

        if(catBrandOp.isPresent()){
            item.setBrand(catBrandOp.get());
        }

        item.setStocks(Arrays.asList(new ItemStock(
                itemRequestDto.getCurrentStockQty(),
                item,
                StockType.STOCK_IN,warehouse,warehouseStore
                )));
        if(itemRequestDto.getAttributes()!=null && itemRequestDto.getAttributes().size()>0) {
            item.setAttributes(itemRequestDto.getAttributes().stream().map(itemAttribute -> {
                ItemAttribute _itemAttribute = new ItemAttribute();
                _itemAttribute.setItem(item);
                _itemAttribute.setAttributeType(itemAttribute.getAttributeType().trim());
                _itemAttribute.setAttributeValue(itemAttribute.getAttributeValue().trim());
                _itemAttribute.setAttributeUnit(itemAttribute.getAttributeUnit().trim());
                return _itemAttribute;
            }).collect(Collectors.toList()));
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
            ).collect(Collectors.toList());
           if(filteredItemStocks.size()==1 && filteredItemStocks.get(0).getStockQty().equals(new BigDecimal("0.00"))){
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
        

        if(itemRequestDto.getAttributes()!=null && itemRequestDto.getAttributes().size()>0) {
            item.setAttributes(itemRequestDto.getAttributes().stream().map(itemAttribute -> {
                itemAttribute.setItem(item);
                itemAttribute.setAttributeType(itemAttribute.getAttributeType().trim());
                itemAttribute.setAttributeValue(itemAttribute.getAttributeValue().trim());
                itemAttribute.setAttributeUnit(itemAttribute.getAttributeUnit().trim());
                return itemAttribute;
            }).collect(Collectors.toList()));
        }

        if(itemRequestDto.getFunctionalUnits()!=null&& itemRequestDto.getFunctionalUnits().size()>0){
            item.setItemFunctionalUnits(itemRequestDto.getFunctionalUnits().stream().map(itemFunctionalUnit -> {
                ItemFunctionalUnit _functionalUnit = new ItemFunctionalUnit();
                _functionalUnit.setId(itemFunctionalUnit.getId());
                _functionalUnit.setValue(itemFunctionalUnit.getValue());
                _functionalUnit.setUnit(itemFunctionalUnit.getUnit());
                _functionalUnit.setItem(item);
                return _functionalUnit;
            }).collect(Collectors.toList()));
        }
        itemRepository.save(item);
    }

    @Override
    public void deleteItem(Long id, Long warehouseId, Long warehosueStoreId) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if(itemOptional.isEmpty()){
            throw new RuntimeException("Sorry! item not found");
        }

        List<ItemStock> stocks = itemStockRepository.findByItemsAndWarehosueId(List.of(id),warehouseId);
        if(stocks.size()>1){
            throw new RuntimeException("Sorry! Item has stock");
        }
        List<DemandDetail> demandDetails  = demandDetailRepository.findAllByItemIdAndWarehouseId(List.of(id),warehouseId);
        if(!demandDetails.isEmpty()){
            throw new RuntimeException("Sorry! Item has some demand in this warehouse, so unable to remove");
        }

        for(ItemStock s : stocks){
            itemStockRepository.delete(s);
        }

    }

    @Override
    @Transactional
    public void stockOut(Item item, BigDecimal qty,Long warehouseId, Long warehouseStoreId) {
        qty = qty.multiply(new BigDecimal(-1));
        this.updateStock(item,qty,StockType.STOCK_OUT,warehouseId,warehouseStoreId);
    }

    

   @Override
   public void stockUpdateByDemand(Long warehouseId,
                                   DemandDetail demandDetail,
                                   StockType stockType) {

       var itemDetailOp = this.getItemDetailWithWarehouse(demandDetail.getItem().getId());

       if(itemDetailOp instanceof Optional && itemDetailOp.isPresent()){
           ItemDetail itemDetail = (ItemDetail) itemDetailOp.get();
           List<Map<String,Object>> warehouses = (List<Map<String,Object>>)itemDetail.getWarehouses().get(warehouseId.toString());
           if(warehouses.size()>0){
               Map<String,Object> warehouseStoreInfo = warehouses.get(0);
               if(stockType == StockType.STOCK_IN){
                   this.stockIn(new Item(itemDetail.getId()), demandDetail.getApprovedQuantity(),
                   (Long)warehouseStoreInfo.get("warehouseId"),
                   (Long)warehouseStoreInfo.get("warehouseStoreId"));
               }
               if(stockType == StockType.STOCK_OUT){
                   this.stockOut(new Item(itemDetail.getId()), demandDetail.getApprovedQuantity(),
                   (Long)warehouseStoreInfo.get("warehouseId"),
                   (Long)warehouseStoreInfo.get("warehouseStoreId"));
               }

           }

       }

   }

    @Override
    @Transactional
    public void stockIn(Item item, BigDecimal qty,Long warehouseId, Long warehouseStoreId) {
        this.updateStock(item,qty,StockType.STOCK_IN,warehouseId,warehouseStoreId);
    }

    @Transactional
    private void updateStock(Item item,BigDecimal qty, StockType stockType, Long warehouseId, Long warehouseStoreId){
        
       ItemStock itemStock = new ItemStock(qty, item,stockType,new Warehouse(warehouseId),new WarehouseStore(warehouseStoreId));
       itemStockRepository.save(itemStock);
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
    public List<?> getByAttributes(Long brandId, String attribute,Long subCatId, Long warehouseId) {
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
                Map<String,Object> _item = new HashMap<>();
                for(CSVRecord r : records){
                    String catName = r.get("CATEGORY");
                    String subCatName = r.get("SUB_CATEGORY");
//                    String itemName = r.get("ITEM_NAME");
//                    String vat = r.get("VAT");
//                    String attrType = r.get("ATTRIBUTE_TYPE");
//                    String attrValue = r.get("ATTRIBUTE_VALUE");
//                    String attrUnit = r.get("ATTRIBUTE_UNIT");
//                    String brands = r.get("BRANDS");
//                    String _key = subCatName.replaceAll(" ","_").toLowerCase();

                    System.out.println(r);
                    String brandName = r.get("BRAND_NAME");
                    String itemAttribute = r.get("ITEM_ATTRIBUTE_NAME");
                    String storeId = r.get("STORE_ID");
                    String currentStock = r.get("CURRENT_STOCK");
                    String safetyStock = r.get("SAFETY_STOCK");
                    String unitMeasurement = r.get("UNIT_MEASUREMENT");
                    String reorderPercent = r.get("REORDER_PERCENTAGE");

//                    Optional<ItemCategory> icOp = categoryService.getItemCategoryByName(catName);
//                    if(icOp.isEmpty()){
//                        throw new AesException("Item Category Not found");
//                    }
                    Optional<ItemCategory> subCatOp = categoryService.getItemCategoryByName(subCatName);
                    if(subCatOp.isEmpty()){
                        throw new AesException("Item Sub Category Not found");
                    }
                   
//                    ItemCategory ic = icOp.get();
                    ItemCategory subCat = subCatOp.get();
                    Optional<CategoryBrand> catBrandOp = catBrandRepo.findByCategoryIdAndName(subCat.getId(),brandName);
                    if(catBrandOp.isEmpty()){
                        throw new AesException("Brand not found");
                    }
//
//                    StringBuilder itemCode = new StringBuilder();
//                    itemCode.append(ic.getName().substring(0,1)).append(ic.getId());
//                    itemCode.append(subCat.getName().substring(0, 1)).append(subCat.getId());
//                    itemCode.append("B").append(catBrandOp.get().getId());
//
//
//                    if(!item.containsKey(_key)){
//                        Map<String, Object> itemProps = new HashMap<>();
//                        itemProps.put("name",itemName);
//                    }


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
                                    stockIn(itemOp.get(), new BigDecimal(Double.parseDouble(currentStock)), ws.getWarehouse().getId(), ws.getId());
                                }
                                if(!safetyStock.isEmpty()){
                                    item.setStockThresholdQty(Integer.parseInt(safetyStock));
                                }
                                if(!unitMeasurement.isEmpty()){
                                    item.setItemUnit(unitMeasurement);
                                }

                                if(!reorderPercent.isEmpty()){
                                    item.setReorderPercentage(new BigDecimal(Double.parseDouble(reorderPercent)));
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
        Iterable<CSVRecord> records  = CSVFormat.RFC4180.withHeader(ItemHeader.class).parse(in);
        records.iterator().next();
        return records;
    }

    @Override
    @Transactional
    public void syncItemsBySubCatCode(Jwt token, Long warehouseId, Long warehouseStoreId, String subCatCode) {
        
        List<SyncItemDetail> items = this.fetchItemsBySubCat(token,subCatCode);
        List<ScmItemUpdateDto> dtos = new ArrayList<>();
        items.stream().forEach(i->{
           ScmItemUpdateDto scmItemUpdateDto = new ScmItemUpdateDto();
           Item item = this.createItem(token,warehouseId,warehouseStoreId,i);
           scmItemUpdateDto.setItemIdCps(i.getId());
           scmItemUpdateDto.setItemIdScm(item.getId());
           dtos.add(scmItemUpdateDto);
        });
        if(dtos!=null && dtos.size()>0){
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
            if(orgOp.isPresent()){
                headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
            }
            HttpEntity<List<ScmItemUpdateDto>> payload = new HttpEntity<>(dtos,headers);
            String url = cpsConfig.getItemsEndpoint().concat("/update-scm-id");
            ResponseEntity<?> response = networkService.put(url,payload,Void.class);
            System.out.println(response.getStatusCode().value());
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
            throw new RuntimeException("Sorry! Warehouse not found");
        }

        Optional<WarehouseStore> warehouseStoreOp = warehouseStoreService.getStoreById(warehouseStoreId);
        if(warehouseStoreOp.isEmpty()){
            throw new RuntimeException("Sorry! Warehouse Store not found");
        }

        Warehouse warehouse = warehouseOp.get();
        WarehouseStore warehouseStore = warehouseStoreOp.get();

        item.setCode(warehouseStore.getStoreName().substring(0,1)+"-"+item.getCode());

        // Get Subcategory By Code
        Optional<ItemCategory> subCatOp = categoryService.getCategoryByCode(syncItemDetail.getItemCategory().code());
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
                // throw new AesException("Sorry! Brand not found");
            } else {
                catBrand = catBrandOp.get();
            }
        }

        item.setItemCategory(subCat);
        item.setItemParentCategory(subCat.getParentCategory());
        item.setBrand(catBrand);
        item.setCpsItemId(syncItemDetail.getId());

        List<?> itemExistByAttr = this.getByAttributes(catBrand.getId(),itemAttributeName,subCat.getId(),warehouseId);
        if(itemExistByAttr.size()>0){
            List<Item> items = itemRepository.findByBrandIdAndItemCategoryIdAndItemAttributeName(catBrand.getId(), subCat.getId(), itemAttributeName);
            for(Item i : items){
                if(i.getItemInactiveStatus()!=null && i.getItemInactiveStatus().equals(ItemInactiveStatus.APPROVED)){
                    i.setActive(true);
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
                            ItemImportLog _iil = importLogExist.get();
                            ItemImportLog iil = new ItemImportLog();
                            iil.setItem(_iil.getItem());
                            iil.setWarehouse(warehouse);
                            iil.setItemInactiveStatus(_iil.getItemInactiveStatus());
                            itemImportLogRepository.save(iil);
                        }
                    }
                    if(!warehouseExist){
                        stocks.add(new ItemStock(
                                new BigDecimal(0l),
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

                if(itemExistByCode.isEmpty() && syncItemDetail.getAttributes()!=null && syncItemDetail.getAttributes().size()>0) {

                    Item finalItem = item;
                    item.setAttributes(syncItemDetail.getAttributes().stream().map(itemAttribute -> {

                        itemAttribute.setId(null);
                        itemAttribute.setItem(finalItem);
                        return itemAttribute;
                    }).collect(Collectors.toList()));
                }

                if(itemExistByCode.isEmpty() && syncItemDetail.getFunctionalUnits()!=null && syncItemDetail.getFunctionalUnits().size()>0) {
                    Item finalItem = item;
                    item.setItemFunctionalUnits(syncItemDetail.getFunctionalUnits().stream().map(itemFunctionalUnit -> {
                        itemFunctionalUnit.setId(null);
                        itemFunctionalUnit.setItem(finalItem);
                        return itemFunctionalUnit;
                    }).collect(Collectors.toList()));
                }

                itemRepository.save(item);

                Optional<ItemImportLog> importLogExist = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),warehouseId);
                if(importLogExist.isEmpty()) {
                    ItemImportLog iil = new ItemImportLog();
                    iil.setItem(item);
                    iil.setWarehouse(warehouse);
//                    iil.setItemInactiveStatus(ItemInactiveStatus.PENDING_VERIFICATION);
                    // added for inactive account service
                    iil.setItemInactiveStatus(ItemInactiveStatus.APPROVED);
                    item.setActive(true);
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
            throw new RuntimeException("Warehouse Missing");
        }
        Optional<WarehouseStore> warehouseStoreOp = warehouseStoreService.getStoreById(approveRequestDto.getWarehouseStoreId());
        if(warehouseStoreOp.isEmpty()){
            throw new RuntimeException("Warehouse Store Missing");
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
//                        iil.setItemInactiveStatus(ItemInactiveStatus.PENDING_VERIFICATION);
                        iil.setItemInactiveStatus(ItemInactiveStatus.APPROVED);
                        item.setActive(true);
                        if(item.getUserItemId()!=null){
                            Optional<UserItem> userItemOp = userItemRepository.findById(item.getUserItemId());
                            userItemOp.ifPresent((ui)->{
                                ui.setItemStatus(UserCategoryStatus.COMPLETED);
                            });
                        }
                    }
//                    accountService.setItemService(this);
//                    forceActive();
//                    accountService.createItemLedger(item, warehouseOp.get(),warehouseStoreOp.get());
                }
                if(approveRequestDto.getCode()==null && approveRequestDto.getItemMergeRequestDto()!=null){
                    item.setItemInactiveStatus(null);
                    Optional<ItemImportLog> iilOp = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),warehouseOp.get().getId());
                    if(iilOp.isPresent()){
                        ItemImportLog iil = iilOp.get();
//                        iil.setItemInactiveStatus(ItemInactiveStatus.PENDING_VERIFICATION);
                        iil.setItemInactiveStatus(ItemInactiveStatus.APPROVED);
                        item.setActive(true);
                        if(item.getUserItemId()!=null){
                            Optional<UserItem> userItemOp = userItemRepository.findById(item.getUserItemId());
                            userItemOp.ifPresent((ui)->{
                                ui.setItemStatus(UserCategoryStatus.COMPLETED);
                            });
                        }
                    }
                    mergeItem(item, approveRequestDto.getWarehouseStoreId(), itemMergeRequestDto);
//                    accountService.setItemService(this);
//                    forceActive();
//                    accountService.createItemLedger(item, warehouseOp.get(),warehouseStoreOp.get());

                }
            }else if(approveRequestDto.getApproveStatus().equals(ApproveStatus.REJECTED)){

//                item.setItemInactiveStatus(ItemInactiveStatus.REJECTED);
                Optional<ItemImportLog> iilOp = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),warehouseOp.get().getId());
                if(iilOp.isPresent()){
                    ItemImportLog iil = iilOp.get();
                    iil.setItemInactiveStatus(ItemInactiveStatus.REJECTED);
                    if(item.getUserItemId()!=null){
                        Optional<UserItem> userItemOp = userItemRepository.findById(item.getUserItemId());
                        userItemOp.ifPresent((ui)->{
                            if(approveRequestDto.getIsMerged()) {
                                ui.setItemStatus(UserCategoryStatus.MERGED);
                            }else{
                                ui.setItemStatus(UserCategoryStatus.REJECTED);
                            }
                        });
                    }
                }
                if(approveRequestDto.getItemMergeRequestDto()!=null) {
                    String storePrefixedItemCode = ws.getStoreName().substring(0,1)+"-"+approveRequestDto.getCode();
                    itemOp = itemRepository.findByCodeAndActive(storePrefixedItemCode,true);
                    if(itemOp.isEmpty()){
                        throw new RuntimeException("Sorry! Item not found using code ["+approveRequestDto.getCode()+"]");
                    }
                    Item existItem = itemOp.get();
                    mergeItem(existItem, approveRequestDto.getWarehouseStoreId(), itemMergeRequestDto);
                }
            }
        }
    }

    private void mergeItem(Item item, Long warehouseStoreId, ItemMergeRequestDto itemMergeRequestDto) {

        Optional<WarehouseStore> wsOp = warehouseStoreRepository.findById(warehouseStoreId);
        if(wsOp.isEmpty()){
            throw new RuntimeException("Sorry! Warehouse Store not found");
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
        item.setName(item.getName());
        item.setItemAttributeName(itemMergeRequestDto.getItemAttributeName());
        List<ItemAttribute> attributes = itemMergeRequestDto.getAttributes().stream().map(attr->{
            ItemAttribute _attr = new ItemAttribute();
            Optional<ItemAttribute> itemAttrOp = itemAttributeRepository.findAllByAttributeTypeAndAttributeUnitAndItemId(attr.getAttributeType(),
                    attr.getAttributeUnit(), item.getId());
            if(itemAttrOp.isPresent()){
                _attr = itemAttrOp.get();
                _attr.setAttributeValue(attr.getAttributeValue());
            }else{
                _attr.setItem(item);
                _attr.setAttributeType(attr.getAttributeType());
                _attr.setAttributeUnit(attr.getAttributeUnit());
                _attr.setAttributeValue(attr.getAttributeValue());
            }

            return _attr;
        }).collect(Collectors.toList());
        item.setItemUnit(itemMergeRequestDto.getItemUnit());
        item.setAttributes(attributes);
        Optional<CategoryBrand> categoryBrandOp = categoryBrandRepository
                .findByCategoryIdAndName(item.getItemCategory().getId(),
                        itemMergeRequestDto.getBrand());
        if(categoryBrandOp.isPresent()){
           item.setBrand(categoryBrandOp.get());
        }

        String storeWiseItemCode= ws.getStoreName().substring(0,1)+"-"+itemMergeRequestDto.getCode();
        item.setCode(storeWiseItemCode);
    }

    @Override
    public List<?> getTemplateData(Long categoryId, Long subCategoryId,
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
        if(claimResolver.isAdmin() && forceActiveRequestDto.getPhoneNo()!=null &&
                    !forceActiveRequestDto.getPhoneNo().equals("01714112912")){
            throw new RuntimeException("Page Not Found");
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
