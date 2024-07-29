package com.agi.aesl.erpscm.inventory.service;

// import com.agi.aesl.erpscm.authentication.dto.ClaimResponseDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
//import com.agi.aesl.erpscm.demand.entity.DemandDetail;
//import com.agi.aesl.erpscm.demand.repository.DemandDetailRepository;
//import com.agi.aesl.erpscm.demand.repository.DemandRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.fileupload.service.FileUploadService;
// import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.PendingItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.RemoteItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.response.ItemDetail;
import com.agi.aesl.erpscm.inventory.dto.response.ItemListWithAttributesDto;
import com.agi.aesl.erpscm.inventory.dto.response.SyncItemDetail;
import com.agi.aesl.erpscm.inventory.dto.response.SyncItemDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.CategoryWarehouseStore;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import com.agi.aesl.erpscm.inventory.enums.ItemHeader;
import com.agi.aesl.erpscm.inventory.enums.ItemInactiveStatus;
import com.agi.aesl.erpscm.inventory.enums.StockType;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryWarehouseStoreRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemStockRepository;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
// import com.agi.aesl.erpscm.scm.dto.request.OfferRequestDto;
import com.agi.aesl.erpscm.network.NetworkService;

import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
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
    private CategoryBrandRepository categoryBrandRepository;

    @Autowired
    private CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;

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
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setId(detail.getId());
        itemDetail.setName(detail.getName());
        itemDetail.setCode(detail.getCode());
        itemDetail.setActive(detail.getActive());
        itemDetail.setAttributes(detail.getAttributes());
        itemDetail.setItemCategory(detail.getItemCategory());
        itemDetail.setItemParentCategory(detail.getItemParentCategory());
        itemDetail.setItemUnit(detail.getItemUnit());
        itemDetail.setBrand(detail.getBrand());
        itemDetail.setReorderPercentage(detail.getReorderPercentage());
        itemDetail.setStockThresholdQty(detail.getStockThresholdQty());
        Map<String, List<Map<String,Object>>> warehouses = new HashMap<>();
        detail.getStocks().stream().forEach(itemStock -> {
           if(warehouses.containsKey(""+itemStock.getWarehouse().getId())) {
               List<Map<String,Object>> itemStocks = warehouses.get(""+itemStock.getWarehouse().getId());

               Optional<Map<String,Object>> mapOp = itemStocks.stream().filter(
                       iStock->{
                           return ((Long)iStock.get("warehouseId")).equals(itemStock.getWarehouse().getId())
                                   && ((Long)iStock.get("warehouseStoreId")).equals(itemStock.getWarehouseStore().getId());
                       }).findFirst();
                 processItemStock(itemStock,warehouses,itemStocks,mapOp);

           }else{
               List<Map<String,Object>> itemStocks = new ArrayList<>();
               processItemStock(itemStock,warehouses,itemStocks,Optional.ofNullable(null));

           }
        });
        itemDetail.setWarehouses(warehouses);
        return Optional.ofNullable(itemDetail);
    }

    private void processItemStock(ItemStock itemStock,
                                  Map<String, List<Map<String,Object>>> warehouses,
                                  List<Map<String,Object>> itemStocks,Optional<Map<String,Object>> mapOp){
        Map<String,Object> stockInfo = new HashMap<>();
        if(mapOp.isPresent()){
            stockInfo = mapOp.get();
        }
        BigDecimal sQty = (BigDecimal) stockInfo.get("stockQty");
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
    public Page<?> getAllItems(Optional<Integer> page, Optional<Integer> size,
                               Optional<String> name,
                               Optional<String> code,
                               Optional<Integer> reorderPercentage,
                               Optional<Integer> stockThresholdQty,
                               Optional<Long> categoryId,
                               Optional<Long> subCategoryId,
                               Optional<Long> warehouseId,
                               Optional<Long> warehouseStoreId

    ) {

        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        Page<?> result  = itemRepository.findAllItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryId.orElse(null),
                subCategoryId.orElse(null),
                warehouseId.orElse(null),
                warehouseStoreId.orElse(null),
                pageable);


        return result;
    }

    @Override
    public Page<?> getPendingAllItems(Optional<Integer> page, Optional<Integer> size, Optional<String> name, Optional<String> code, Optional<Integer> reorderPercentage, Optional<Integer> stockThresholdQty, Optional<Long> categoryId, Optional<Long> subCategoryId, Optional<Long> warehouseId, Optional<Long> warehouseStoreId) {
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        Page<?> result  = itemRepository.findAllPendingItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryId.orElse(null),
                subCategoryId.orElse(null),
                warehouseId.orElse(null),
                warehouseStoreId.orElse(null),
                pageable);


        return result;
    }

    @Override
    public Page<?> getPendingVerificationAllItems(Optional<Integer> page, Optional<Integer> size, Optional<String> name, Optional<String> code, Optional<Integer> reorderPercentage, Optional<Integer> stockThresholdQty, Optional<Long> categoryId, Optional<Long> subCategoryId, Optional<Long> warehouseId, Optional<Long> warehouseStoreId) {
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        Page<?> result  = itemRepository.findAllPendingVerificationItems(
                name.orElse(null),
                code.orElse(null),
                reorderPercentage.orElse(null),
                stockThresholdQty.orElse(null),
                categoryId.orElse(null),
                subCategoryId.orElse(null),
                warehouseId.orElse(null),
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

    private String generateItemAttribute(List<ItemAttribute> attributes){
        StringBuilder sb = new StringBuilder();

        attributes.stream().forEach(itemAttribute -> {
            sb.append(itemAttribute.getAttributeType().trim()
                    +" "+itemAttribute.getAttributeValue().trim()
                    +" "+itemAttribute.getAttributeUnit().trim());
            sb.append(" - ");
        });

        return (sb.isEmpty())? "" :  sb.toString().substring(0,sb.length()-3);
    }

    @Override
    @Transactional
    public void createItem(Jwt loggedInUser, ItemRequestDto itemRequestDto) {
        claimResolver.setToken(loggedInUser);
        Item item = itemRequestDto.getEntity();

        String itemAttributeName = generateItemAttribute(itemRequestDto.getAttributes());

       Warehouse warehouse = null;
       WarehouseStore warehouseStore = null;

        if(itemRequestDto.getWarehouse().getId()!=null) {
           warehouse = new Warehouse(itemRequestDto.getWarehouse().getId()) ;
        }else{
           warehouse = new Warehouse();//loggedInUser.getEmployee().getWarehouseId()
        }

        if(itemRequestDto.getWarehouseStore() !=null && itemRequestDto.getWarehouseStore().getId() != null){
           warehouseStore = new WarehouseStore(itemRequestDto.getWarehouseStore().getId());

        }

        Long brandId = (itemRequestDto.getBrand()!=null)? itemRequestDto.getBrand().getId() : null;
       List<?> itemExistByAttr = this.getByAttributes(brandId,itemAttributeName,warehouse.getId());
       if(itemExistByAttr.size()>0){
           throw new AesException("Sorry! Item Already exist with same attributes for this brand");
       }

        Optional<Item> itemOp = itemRepository.findByCode(item.getCode());
        if(itemOp.isPresent()){
            Item itemExist = itemOp.get();
            List<ItemStock> itemStocks = itemExist.getStocks();
            itemStocks.add(new ItemStock(
                    itemRequestDto.getCurrentStockQty(),
                    itemExist,
                    StockType.STOCK_IN,
                    warehouse,
                    warehouseStore
            ));
            itemExist.setStocks(itemStocks);
            return;
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
        item.setItemInactiveStatus(ItemInactiveStatus.PENDING);
        item.setItemAttributeName(itemAttributeName);
        item.setCreatedBy(claimResolver.getUserId());
        itemRepository.save(item);

        if(item.getId()!=null){

            PendingItemRequestDto pendingItemRequestDto = new PendingItemRequestDto();
            Employee employee = claimResolver.getEmployee().orElse(null);
            pendingItemRequestDto.setItemAttributeName(itemAttributeName);
            if(employee!=null) {
                pendingItemRequestDto.setRequestedBy(itemRequestDto.getEmployee());
                pendingItemRequestDto.setDesignation(employee.getDesignationName());
                pendingItemRequestDto.setDepartment(employee.getDepartmentName());
                pendingItemRequestDto.setWarehouseId(employee.getWarehouseId());
                pendingItemRequestDto.setWarehouseName(employee.getWarehouseName());
            }else{
                throw new RuntimeException("Sorry! Employee Info missing");
            }
            Optional<ItemCategory> catOp = categoryService.getAnyItemCategory(item.getItemCategory().getId());

            pendingItemRequestDto.setSubCategoryCode(catOp.get().getCode());
            if(item.getBrand()!=null) {
                Optional<CategoryBrand> brandOp = categoryBrandRepository.findById(item.getBrand().getId());
                pendingItemRequestDto.setBrand(brandOp.get().getName());
            }
            pendingItemRequestDto.setReportingManager(employee.getReportingManager());
            pendingItemRequestDto.setEmployeeId(employee.getId());
            pendingItemRequestDto.setAttributes(itemRequestDto.getAttributes());
            pendingItemRequestDto.setItemUnit(item.getItemUnit());
            pendingItemRequestDto.setScmItemId(item.getId());
            pendingItemRequestDto.setCode(item.getCode());

            HttpHeaders headers =  new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(loggedInUser.getTokenValue());
            if(orgOp.isPresent()){
                pendingItemRequestDto.setOrganizationId(orgOp.get().getCpsVendorRegistrationId());
                headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
                itemRequestDto.setOrgId(orgOp.get().getId());
            }
            HttpEntity<PendingItemRequestDto> payload = new HttpEntity<>(pendingItemRequestDto,headers);
            String url = cpsConfig.getPendingItemReqEndpoint();
            ResponseEntity<?> response = networkService.post(url,payload,Void.class);
            System.out.println("STATUS CODE: "+response.getStatusCode());
        }

    }

    

    @Override
    public void createItem(Jwt loggedInUser, RemoteItemRequestDto itemRequestDto) {
        Item item = itemRequestDto.getEntity();

        String itemAttributeName = generateItemAttribute(itemRequestDto.getAttributes());

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
       List<?> itemExistByAttr = this.getByAttributes(brandId,itemAttributeName,warehouse.getId());
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

        itemOptional = itemRepository.findByCode(itemRequestDto.getCode());
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
            itemStocks.add(new ItemStock(
                    itemRequestDto.getCurrentStockQty(),
                    item,
                    StockType.STOCK_IN,
                    new Warehouse(itemRequestDto.getWarehouse().getId()),
                    new WarehouseStore(itemRequestDto.getWarehouseStore().getId())
            ));
            item.setStocks(itemStocks);
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
        itemRepository.save(item);
    }

    @Override
    public void deleteItem(Long id) {
        Optional<Item> itemOptional = itemRepository.findById(id);

//        Optional<DemandDetail> ddOp = demandDetailRepository.findByItemId(id);

//        if(ddOp.isPresent()){
//            throw new AesException("Sorry! Item Cannot be deleted, this item used in demand");
//        }

        if(itemOptional.isPresent()) {
            Item item = itemOptional.get();
            item.setActive(false);
            itemRepository.save(item);
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
    public List<?> getByAttributes(Long brandId, String attribute, Long warehouseId) {
        return itemRepository.findByAttributes(brandId,attribute,warehouseId);
    }

    @Override
    public void importItems(Optional<MultipartFile> fileOp) {
        Path path = Path.of("./uploads/inventory-mgm/items");
        FileUploadResponse fileUploadResponse = null;
        if(fileOp.isPresent()){
            fileUploadResponse = fileUploadService.uploadFile(path, fileOp.get());
            try {
                Iterable<CSVRecord> records = getItemRecords(fileUploadResponse);
                Map<String,Object> item = new HashMap<>();
                for(CSVRecord r : records){
                    String catName = r.get("CATEGORY_NAME");
                    String subCatName = r.get("SUB_CATEGORY_NAME");
                    String itemName = r.get("ITEM_NAME");
                    String vat = r.get("VAT");
                    String attrType = r.get("ATTRIBUTE_TYPE");
                    String attrValue = r.get("ATTRIBUTE_VALUE");
                    String attrUnit = r.get("ATTRIBUTE_UNIT");
                    String brands = r.get("BRANDS");
                    String _key = subCatName.replaceAll(" ","_").toLowerCase();

                    Optional<ItemCategory> icOp = categoryService.getItemCategoryByName(catName);
                    if(icOp.isEmpty()){
                        throw new AesException("Item Category Not found");
                    }
                    Optional<ItemCategory> subCatOp = categoryService.getItemCategoryByName(subCatName);
                    if(subCatOp.isEmpty()){
                        throw new AesException("Item Sub Category Not found");
                    }
                   
                    ItemCategory ic = icOp.get();
                    ItemCategory subCat = subCatOp.get();
                    Optional<CategoryBrand> catBrandOp = catBrandRepo.findByCategoryIdAndName(subCat.getId(),brands);
                    if(catBrandOp.isEmpty()){
                        throw new AesException("Brand not found");
                    }

                    StringBuilder itemCode = new StringBuilder();
                    itemCode.append(ic.getName().substring(0,1)).append(ic.getId());
                    itemCode.append(subCat.getName().substring(0, 1)).append(subCat.getId());
                    itemCode.append("B").append(catBrandOp.get().getId());
                    

                    if(!item.containsKey(_key)){
                        Map<String, Object> itemProps = new HashMap<>();
                        itemProps.put("name",itemName);
                    }
                }
            } catch (FileNotFoundException e) {
                throw new AesException(e.getMessage());
            } catch (IOException e) {
                throw new AesException(e.getMessage());
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
        items.stream().forEach(i->{
           this.createItem(warehouseId,warehouseStoreId,i);
        });
        
    }

    @Transactional
    private void createItem(Long warehouseId, Long warehouseStoreId, SyncItemDetail syncItemDetail){
        Item item = syncItemDetail.getEntity();
        String itemAttributeName = generateItemAttribute(syncItemDetail.getAttributes());
        // Get Subcategory By Code
        Optional<ItemCategory> subCatOp = categoryService.getCategoryByCode(syncItemDetail.getItemCategory().code());
        if(subCatOp.isEmpty()){
            throw new AesException("Sorry! Sub Category not found");
        }
        ItemCategory subCat = subCatOp.get();

        // Get Category Brand
        CategoryBrand catBrand = null;
        Optional<CategoryBrand> catBrandOp = categoryBrandRepository.findByCategoryIdAndName(subCat.getId(),syncItemDetail.getBrand().name());
        if(catBrandOp.isEmpty()){
            catBrand = new CategoryBrand();
            catBrand.setCategory(subCat);
            catBrand.setName(syncItemDetail.getBrand().name());
            categoryBrandRepository.save(catBrand);
            // throw new AesException("Sorry! Brand not found");
        }else{
            catBrand = catBrandOp.get();
        }

        item.setItemCategory(subCat);
        item.setItemParentCategory(subCat.getParentCategory());
        item.setBrand(catBrand);


        List<?> itemExistByAttr = this.getByAttributes(catBrand.getId(),itemAttributeName,warehouseId);
        if(itemExistByAttr.size()>0){
            List<Item> items = itemRepository.findByBrandIdAndItemCategoryIdAndItemAttributeName(catBrand.getId(), subCat.getId(), itemAttributeName);
            for(Item i : items){
                i.setActive(true);
            }
            // throw new AesException("Sorry! Item Already exist with same attributes for this brand");
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
                new Warehouse(warehouseId),
                new WarehouseStore(warehouseStoreId)
            )));
    
            if(syncItemDetail.getAttributes()!=null && syncItemDetail.getAttributes().size()>0) {
                
                item.setAttributes(syncItemDetail.getAttributes().stream().map(itemAttribute -> {
                    
                    itemAttribute.setId(null);
                    itemAttribute.setItem(item);
                    return itemAttribute;
                }).collect(Collectors.toList()));
            }
            itemRepository.save(item);
        }

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

    

    
    
    
}
