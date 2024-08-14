package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;

// import com.agi.aesl.erpscm.authentication.dto.ClaimResponseDto;

//import com.agi.aesl.erpscm.demand.entity.DemandDetail;
//import com.agi.aesl.erpscm.demand.repository.DemandDetailRepository;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.dto.request.*;
import com.agi.aesl.erpscm.inventory.entity.*;
import com.agi.aesl.erpscm.inventory.enums.BudgetType;
import com.agi.aesl.erpscm.inventory.enums.CategoryStatus;
import com.agi.aesl.erpscm.inventory.repository.CategoryAttributeRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryBudgetRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryWarehouseStoreRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;

import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryBudgetRepository categoryBudgetRepository;

    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;

    @Autowired
    private CategoryBrandRepository categoryBrandRepository;

    @Autowired
    private CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CpsServerConfig cpsServerConfig;

    @Autowired
    private OrgService orgService;

//    @Autowired
//    private DemandDetailRepository demandDetailRepository;
//
//    @Autowired
//    private WarehouseRepository warehouseRepository;
//
//    @Autowired
//    private WarehouseStoreRepository warehouseStoreRepository;

    @Autowired
    private NetworkService networkService;

    @Override
    @Transactional
    public void addCategories(Jwt token,List<CategoryRequestDtoCustom> categoryRequestDtos) {
        if(categoryRequestDtos!=null && categoryRequestDtos.size()>0){
            List<ScmIdUpdateDto> dtos = new ArrayList<>();
            for(CategoryRequestDtoCustom categoryRequestDto : categoryRequestDtos){
                    ScmIdUpdateDto scmIdUpdateDto = new ScmIdUpdateDto();
                    CategoryRequestDto cr = new CategoryRequestDto();
                    cr.setCategoryStatus(CategoryStatus.APPROVED);
                    cr.setAttributes(categoryRequestDto.getAttributes());
                    cr.setCode(categoryRequestDto.getCode());
                    cr.setCpsCategoryId(categoryRequestDto.getCpsCategoryId());
                    cr.setName(categoryRequestDto.getName());
                    cr.setParentCategory(categoryRequestDto.getParentCategory());
                    cr.setRequestedBy(categoryRequestDto.getRequestedBy());
                    cr.setVat(categoryRequestDto.getVat());
                    cr.setWarehouse(categoryRequestDto.getWarehouse());
                    cr.setWarehouseStore(categoryRequestDto.getWarehouseStore());
                if(categoryRequestDto.getBrands()!=null && categoryRequestDto.getBrands().size()>0){
//                    cr.setBrands(categoryRequestDto.getBrands().stream().map(b->{
//                       return new CategoryBrand(null, b, null);
//                    }).collect(Collectors.toList()));
                }
                cr.setCurrentYearBudget(new BigDecimal(0));
                cr.setIsForCps(categoryRequestDto.getIsForCps());
                scmIdUpdateDto.setCategoryIdCps(categoryRequestDto.getCpsCategoryId());
                Optional<ItemCategory> catOp = this.addCategory(null,cr);

                if(catOp.isPresent()){
                    scmIdUpdateDto.setCategoryIdScm(catOp.get().getId());
                }
                dtos.add(scmIdUpdateDto);
            }
            if(dtos.size()>0){
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
                if(orgOp.isPresent()){
                    headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
                }
                HttpEntity<List<ScmIdUpdateDto>> payload = new HttpEntity<>(dtos,headers);
                String url = cpsServerConfig.getItemCategoriesEndpoint().concat("/update-scm-id");
                ResponseEntity<?> response = networkService.put(url,payload,Void.class);
                System.out.println(response.getStatusCode().value());
            }
        }
    }

    @Override
    @Transactional
    public Optional<ItemCategory> addCategory(Jwt token, CategoryRequestDto categoryRequestDto) {
        ItemCategory category = categoryRequestDto.getEntity();

        Optional<ItemCategory> itemCategoryOptional = categoryRepository.findByCode(category.getCode());
        if(itemCategoryOptional.isPresent()){
            category = itemCategoryOptional.get();
        }


        if(itemCategoryOptional.isEmpty() && categoryRequestDto.getCurrentYearBudget() != null){
            category.setBudgets(Arrays.asList(new CategoryBudget(category,
                    categoryRequestDto.getCurrentYearBudget(), LocalDate.now().getYear(), BudgetType.REGULAR)));
        }


        if(categoryRequestDto.getAttributes()!=null && categoryRequestDto.getAttributes().size()>0){
            
            
            ItemCategory finalCategory = category;
            
            category.setAttributes(categoryRequestDto.getAttributes().stream().map(categoryAttribute -> {
                Optional<CategoryAttribute> catAttrOp = finalCategory.getAttributes().stream().filter(fca->{
                    return fca.getAttributeType().trim().equals(categoryAttribute.getAttributeType().trim());
                }).findFirst();
                if(catAttrOp.isPresent()){
                    CategoryAttribute catAttribute = catAttrOp.get();
                    categoryAttribute.setId(catAttribute.getId());
                    categoryAttribute.setAttributeType(categoryAttribute.getAttributeType());
                    categoryAttribute.setAttributeUnit(categoryAttribute.getAttributeUnit());
                    categoryAttribute.setAttributeValue(categoryAttribute.getAttributeValue());

                    categoryAttribute.setCategory(finalCategory);
                    return categoryAttribute;
                }else{
                    if(categoryRequestDto.getId()==null){
                        categoryAttribute.setId(null);
                    }
                    categoryAttribute.setCategory(finalCategory);
                    return categoryAttribute;
                }
                
                
            }).collect(Collectors.toList()));
        }

        if(itemCategoryOptional.isEmpty() && categoryRequestDto.getBrands()!=null && categoryRequestDto.getBrands().size()>0){
            ItemCategory finalCategory = category;

            category.setBrands(categoryRequestDto.getBrands().stream().map(categoryBrand -> {

//                if(categoryRequestDto.getId()==null){
//                    categoryBrand.setId(null);
//                }
                CategoryBrand categoryBrand1 = new CategoryBrand(categoryBrand);
                categoryBrand1.setCategory(finalCategory);
                return categoryBrand1;
            }).collect(Collectors.toList()));
        }

        if(categoryRequestDto.getCpsCategoryId()!=null){
            category.setCpsCategoryId(categoryRequestDto.getCpsCategoryId());
        }


        if(categoryRequestDto.getIsForCps()==false){
            category.setActive(true);
        }else{
            category.setActive(false);
        }

        if(categoryRequestDto.getCategoryStatus()!=null){
            category.setCategoryStatus(categoryRequestDto.getCategoryStatus());
        }else{
            category.setCategoryStatus(CategoryStatus.PENDING);
        }

        categoryRepository.save(category);
        
        Optional<CategoryWarehouseStore> cwsOp =  categoryWarehouseStoreRepository.findByCategoryIdAndWarehouseId(category.getId() ,categoryRequestDto.getWarehouse().getId());
        
        if(cwsOp.isEmpty()){
            CategoryWarehouseStore categoryWarehouseStore = new CategoryWarehouseStore();
            categoryWarehouseStore.setCategory(category);
            categoryWarehouseStore.setWarehouse(new Warehouse(categoryRequestDto.getWarehouse().getId()));
            categoryWarehouseStore.setWarehouseStore(new WarehouseStore(categoryRequestDto.getWarehouseStore().getId()));
            categoryWarehouseStoreRepository.save(categoryWarehouseStore);
        }

        if(category.getId()!=null && categoryRequestDto.getIsForCps()){
            RemoteCategoryRequestDto remoteCategoryRequestDto = new RemoteCategoryRequestDto();
            remoteCategoryRequestDto.setName(categoryRequestDto.getName());
            remoteCategoryRequestDto.setCode(category.getCode());

            if(categoryRequestDto.getParentCategory()!=null) {
                Optional<ItemCategory> parentCategoryOp = categoryRepository.findById(category.getParentCategory().getId());

                if (parentCategoryOp.isPresent()) {
                    remoteCategoryRequestDto.setParentCategory(new ReferenceObjectDto(parentCategoryOp.get().getCpsCategoryId()));
                }

                remoteCategoryRequestDto.setAttributes(categoryRequestDto.getAttributes().stream().map(attr->{
                    CategoryAttribute ca = new CategoryAttribute();
                    ca.setAttributeType(attr.getAttributeType());
                    ca.setAttributeUnit(attr.getAttributeUnit());
                    ca.setAttributeValue(attr.getAttributeValue());
                    return ca;
                }).collect(Collectors.toList()));
                remoteCategoryRequestDto.setBrands(categoryRequestDto.getBrands());
                remoteCategoryRequestDto.setVat(categoryRequestDto.getVat());
            }
            remoteCategoryRequestDto.setScmCategoryId(category.getId());
            remoteCategoryRequestDto.setCreatedBy(categoryRequestDto.getEmployee());
            remoteCategoryRequestDto.setCategoryStatus("PENDING");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
            if(orgOp.isPresent()){
                headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
            }
            HttpEntity<RemoteCategoryRequestDto> payload = new HttpEntity<>(remoteCategoryRequestDto,headers);
            String url = cpsServerConfig.getItemCategoriesEndpoint();
            ResponseEntity<?> response = networkService.post(url,payload,Void.class);
            HttpHeaders httpHeaders = response.getHeaders();
            List<String> headerId = httpHeaders.get("id");
            if(headerId.size()>0){
                category.setCpsCategoryId(Long.parseLong(headerId.get(0)));
            }

        }
        return categoryRepository.findById(category.getId());

    }

    @Override
    @Transactional
    public void updateCategory(Long id, CategoryRequestDto categoryRequestDto) {
        Optional<ItemCategory> itemCategoryOptional = categoryRepository.findById(id);
        if(itemCategoryOptional.isEmpty()){
            throw new AesException("Category Not Found");
        }

        ItemCategory itemCategory = itemCategoryOptional.get();
        if(!itemCategory.getCode().equalsIgnoreCase(categoryRequestDto.getCode())){
            throw new AesException("Category Code should be unique");
        }

        if(itemCategory.getParentCategory()!=null){
            if(categoryRequestDto.getParentCategory()==null || categoryRequestDto.getParentCategory().getId()==null){
                throw new AesException("Parent Category Id missing");
            }
        }

        if(categoryRequestDto.getName()!=null) {
            itemCategory.setName(categoryRequestDto.getName());
        }

        if(categoryRequestDto.getBudgetId().isPresent()){

            Optional<CategoryBudget> categoryBudgetOp = categoryBudgetRepository
                                                        .findById(categoryRequestDto.getBudgetId().get());
            if(categoryBudgetOp.isPresent()) {
                CategoryBudget categoryBudget = categoryBudgetOp.get();
                if(categoryBudget.getAmount().compareTo(categoryRequestDto.getCurrentYearBudget())<0){
                    CategoryBudget extendedBudget = new CategoryBudget(itemCategory,
                            categoryRequestDto.getCurrentYearBudget(),
                            LocalDate.now().getYear(), BudgetType.EXTENDED);

                    extendedBudget.setAmount(categoryRequestDto.getCurrentYearBudget()
                                    .subtract(categoryBudget.getAmount()));
                    categoryBudgetRepository.save(extendedBudget);
                } else {
                    categoryBudget.setAmount(categoryRequestDto.getCurrentYearBudget());
                    categoryBudgetRepository.save(categoryBudget);

                }
            }
        }

        if(categoryRequestDto.getAttributes()!=null && categoryRequestDto.getAttributes().size()>0){
            itemCategory.setAttributes(categoryRequestDto.getAttributes().stream().map(categoryAttribute -> {
                categoryAttribute.setCategory(itemCategory);
                return categoryAttribute;
            }).collect(Collectors.toList()));
        }

        if(categoryRequestDto.getEntity().getParentCategory()!=null) {
            itemCategory.setParentCategory(categoryRequestDto.getEntity().getParentCategory());
        }
        if(categoryRequestDto.getVat()!=null) {
            itemCategory.setVat(categoryRequestDto.getVat());
        }
        categoryRepository.save(itemCategory);
    }

    @Override
    public Optional<ItemCategory> existByCode(String code) {
        return categoryRepository.findByCode(code);
    }

    @Override
    public Optional<ItemCategory> getItemCategory(Long id) {
        return categoryRepository.findById(id,LocalDate.now().getYear());
    }

    @Override
    public Optional<ItemCategory> getAnyItemCategory(Long id) {
        return categoryRepository.findAnyCategoryById(id);
    }

    @Override
    public Optional<ItemCategory> getPendingItemCategory(Long id) {
        return categoryRepository.findPendingCategoryById(id);
    }

    @Override
    public Optional<ItemCategory> getItemCategoryForInventoryControl(Long id) {
        return categoryRepository.findById(id);
    }

//    @Override
//    public Optional<CategoryWarehouseStore> getCategoryByCodeAndStore(Long warehouseId, CopyToStoreDto copyToStoreDto, CategoryRequestDto categoryRequestDto, Long storeId) {
//        Optional<ItemCategory> itemCategoryOptional = categoryRepository.findByCode(categoryRequestDto.getCode());
//        if(itemCategoryOptional.isPresent()){
//            ItemCategory category = itemCategoryOptional.get();
//            Optional<CategoryWarehouseStore> catWsOptional = categoryWarehouseStoreRepository.findByCategoryIdAndWarehouseStoreId(copyToStoreDto.getCategory().getId(), storeId);
//            Optional<CategoryWarehouseStore> subCatWsOptional = categoryWarehouseStoreRepository.findByCategoryIdAndWarehouseStoreId(category.getId(),storeId);
//
//            if(subCatWsOptional.isEmpty()){
//                if(catWsOptional.isEmpty()){
//                    CategoryWarehouseStore cws = new CategoryWarehouseStore();
////                    cws.setCategory(new ItemCategory(copyToStoreDto.getCategory().getId()));
////                    cws.setWarehouse(new Warehouse(warehouseId));
////                    cws.setWarehouseStore(new WarehouseStore(storeId));
//                    categoryWarehouseStoreRepository.save(cws);
//                }
//                CategoryWarehouseStore cws = new CategoryWarehouseStore();
//                cws.setCategory(category);
////                cws.setWarehouse(new Warehouse(warehouseId));
////                cws.setWarehouseStore(new WarehouseStore(storeId));
//                categoryWarehouseStoreRepository.save(cws);
//                return Optional.ofNullable(cws);
//            }else {
//                return catWsOptional;
//            }
//        }
//        return Optional.ofNullable(null);
//    }

    @Override
    public Page<?> getItemCategories(Optional<Integer> page, Optional<Integer> size,
                                        Optional<String> name, Optional<String> code,
                                     Optional<BigDecimal> currentYearBudget,
                                     Optional<Long> productCount,
                                     Optional<Long> warehouseId,
                                     Optional<Long> warehouseStoreId
                                     ) {
        Integer year  = LocalDate.now().getYear();
//        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(10));

            return categoryRepository.findAllByYear(name.orElse(null),
                    code.orElse(null), currentYearBudget.orElse(null),
                    productCount.orElse(null),year,
                    warehouseId.orElse(null),warehouseStoreId.orElse(null)
                    ,pageable);


    }

    @Override
    public Page<?> getItemCategories( Optional<Integer> page, Optional<Integer> size,
                                      Optional<String> name, Optional<String> code,
                                      Optional<BigDecimal> currentYearBudget, Optional<Long> productCount,
                                      Optional<Long> categoryId,
                                      Optional<Long> warehouseId,
                                      Optional<Long> warehouseStoreId
                                      ) {



        Integer year  = LocalDate.now().getYear();
//        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        
        Page<?> result = null;
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(10));

                                        
        result = categoryRepository.findAllSubCategories(
                            name.orElse(null),
                            code.orElse(null),
                            currentYearBudget.orElse(null),
                            productCount.orElse(null),
                            categoryId.orElse(null),
                            year,
                            warehouseId.orElse(null),
                            warehouseStoreId.orElse(null)
                            ,pageable);

        return result;
    }


    @Override
    public List<?> getCategories(Optional<Long> warehouseId,Optional<Long> warehouseStoreId,  Optional<String> name, Optional<String> code) {

        return categoryRepository.findAllMainCategories(
                warehouseId.orElse(null),
                warehouseStoreId.orElse(null),
                name.orElse(null),code.orElse(null));
    }

    @Override
    public List<?> getCategoriesForInventoryControl(Optional<Long> warehouseId,
                                                    Optional<Long> warehouseStoreId,
                                                    Optional<String> name,
                                                    Optional<String> code) {
        return categoryRepository.findAllMainCategoriesForInventoryControl(
                warehouseId.orElse(null),warehouseStoreId.orElse(null),
                name.orElse(null),code.orElse(null));
    }

    @Override
    public List<?> getSubCategories(Optional<Long> storeId, Optional<Long> id, Optional<String> name, Optional<String> code) {

        return categoryRepository.findAllSubCategories(
                storeId.orElse(null),
                id.orElse(null),
                name.orElse(null),
                code.orElse(null));
    }

    @Override
    public List<?> getSubCategoriesAll(Optional<Long> categoryId, Optional<String> name, Optional<String> code) {
        return categoryRepository.findAllSubCategories(
                name.orElse(null),
                code.orElse(null));
    }

    @Override
    public List<?> getSubCategoriesForInventoryControl(
            Optional<Long> categoryId,
            Optional<Long> warehouseId,
            Optional<Long> storeId,
            Optional<String> name, Optional<String> code) {
        return categoryRepository.findAllSubCategoriesForInventoryControl(
                categoryId.orElse(null),
                warehouseId.orElse(null),
                storeId.orElse(null),
                name.orElse(null),
                code.orElse(null));
    }

    @Override
    public List<?> getPendingSubCategoriesForInventoryControl(Optional<Long> categoryId,
                                                              Optional<Long> warehouseId,
                                                              Optional<Long> storeId,
                                                              Optional<String> name,
                                                              Optional<String> code) {
        return categoryRepository.findAllPendingSubCategoriesForInventoryControl(
                categoryId.orElse(null),
                warehouseId.orElse(null),
                storeId.orElse(null),
                name.orElse(null),
                code.orElse(null));
    }

    @Override
    @Transactional
    public void activeCategory(Long id, Long warehouseId, Long storeId) {
        Optional<ItemCategory> itemCategoryOptional = categoryRepository.findById(id);
        if(itemCategoryOptional.isEmpty()){
            throw new AesException("Sorry! Item Category not found");
        }
        ItemCategory itemCategory = itemCategoryOptional.get();
        itemCategory.setActive(true);
        

//        if(warehouseId!=null && storeId!=null){
//
//            Optional<Warehouse> wOptional = warehouseRepository.findById(warehouseId);
//            if(wOptional.isEmpty()){
//                throw new AesException("Sorry! Warehouse not found");
//            }
//
//            Optional<WarehouseStore> wsOptional = warehouseStoreRepository.findById(storeId);
//            if(wsOptional.isEmpty()){
//                throw new AesException("Sorry! Store not found");
//            }
//
//            CategoryWarehouseStore cws = new CategoryWarehouseStore();
//            cws.setCategory(itemCategory);
//            cws.setWarehouse(wOptional.get());
//            cws.setWarehouseStore(wsOptional.get());
//            categoryWarehouseStoreRepository.save(cws);
//        }
        
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, Long warehouseId, Long storeId) {

        Optional<ItemCategory> itemCategoryOptional = categoryRepository.findById(id);
        if(itemCategoryOptional.isPresent()){

            ItemCategory itemCategory = itemCategoryOptional.get();
            if(itemCategory.getParentCategory()==null){
                Optional<Long> countOptional = categoryRepository.countAllByParentCategoryAndActive(
                        itemCategoryOptional.get(),true);
                if(countOptional.isPresent() && countOptional.get() > 0){
                throw new AesException("Sorry! Unable to delete, Category already used in Child Category");
                }
            }

            if(warehouseId!=null && storeId!=null){
                categoryWarehouseStoreRepository
                        .deleteByCategoryIdAndWarehouseIdAndWarehouseStoreId(
                                itemCategory.getId(),
                                warehouseId,
                                storeId
                                );
            }

            List<Item> items = itemRepository.findAllByItemCategoryIdAndActive(itemCategory.getId(),true);

            List<Long> itemIds = items.stream().map(i->i.getId()).collect(Collectors.toList());
//            List<DemandDetail> demandDetails  = demandDetailRepository.findAllByItemId(itemIds);
//            if(demandDetails.size()>0){
//                throw new AesException("Sorry! un-checking sub category also trying to delete its items but item already used in different module.");
//            }
//
//            if(demandDetails.size()==0){
//                for(Item item:items){
//                    item.setActive(false);
//                    itemRepository.save(item);
//                }
//            }

            itemCategory.setActive(false);
            categoryRepository.save(itemCategory);
        }
    }

    @Override
    public String getNewCategoryCode() {
        Optional<ItemCategory> icOp = categoryRepository.findMaxOrderById();
        if(icOp.isPresent()){
            ItemCategory ic = icOp.get();
            Long newProductId = ic.getId() + 1;
            return String.format("%05d",newProductId);
        }
        return String.format("%05d",1);
    }

    @Override
    @Transactional
    public void deleteAttribute(Long categoryId, Long attributeId) {
        Optional<ItemCategory> icOp = categoryRepository.findById(categoryId);
        if(icOp.isPresent()){
            categoryAttributeRepository.deleteByIdAndCategoryId(attributeId,categoryId);

        }

    }

//    @Override
//    public void validateCategorySubCategoryRelation(ItemCategory _category, ItemCategory _subCategory) {
//        Optional<ItemCategory> itemCatOp = getItemCategory(_category.getId());
//        if(itemCatOp.isEmpty()){
//            throw new AesException("Sorry! Category not found");
//        }
//
//        Optional<ItemCategory> itemSubCatOp = getItemCategory(_subCategory.getId());
//        if(itemSubCatOp.isEmpty()){
//            throw new AesException("Sorry! SubCategory not found");
//        }
//
//        ItemCategory category = itemCatOp.get();
//        ItemCategory subCategory = itemSubCatOp.get();
//        if(!subCategory.getParentCategory().getId().equals(category.getId())){
//            throw new AesException("Sorry! " + subCategory.getName()+ " is not under category "+category.getName());
//        }
//
//    }

    @Override
    public Optional<ItemCategory> getItemCategoryByName(String catName) {
        return categoryRepository.findByName(catName);
    }

    @Override
    public Optional<ItemCategory> getCategoryByCode(String subCategoryCode) {
        return categoryRepository.findByCode(subCategoryCode);
    }


    @Override
    public List<?> getPendingCategories(
            Optional<Long> warehouseId,
            Optional<Long> warehouseStoreId,
            Optional<String> name,
            Optional<String> code
    ) {
        return categoryRepository.findAllPendingCategories(warehouseId.orElse(null),warehouseStoreId.orElse(null),
                name.orElse(null),code.orElse(null));
    }

    @Override
    @Transactional
    public void approveItemCategory(Jwt token, Long id, CategoryApproveRequestDto categoryApproveRequestDto) {
        Optional<ItemCategory> catOp = categoryRepository.findById(id);
        MergePendingCategoryDto mergePendingCategoryDto = categoryApproveRequestDto.getMergePendingCategoryDto();
        if(catOp.isPresent()) {
            ItemCategory category = catOp.get();
            if (categoryApproveRequestDto.getApproveStatus().equals(ApproveStatus.APPROVED)) {
                approvedWithBody(token, categoryApproveRequestDto,mergePendingCategoryDto);
                category.setActive(true);
                category.setCategoryStatus(CategoryStatus.APPROVED);
            } else if (categoryApproveRequestDto.getApproveStatus().equals(ApproveStatus.REJECTED)) {
                if(categoryApproveRequestDto.getMergePendingCategoryDto()!=null) {
                    mergeWithBody(token, category, mergePendingCategoryDto);
                }
                category.setActive(false);
                category.setCategoryStatus(CategoryStatus.REJECTED);
            }
        }
    }

    @Transactional
    private void approvedWithBody(Jwt token, CategoryApproveRequestDto categoryApproveRequestDto,
                                  MergePendingCategoryDto mergePendingCategoryDto){
        if (categoryApproveRequestDto.getCode() == null && mergePendingCategoryDto != null) {
            Optional<ItemCategory> replacedCatOp = categoryRepository.findByCode(mergePendingCategoryDto.getCode());
            if (categoryApproveRequestDto.getApproveStatus().equals(ApproveStatus.APPROVED)) {
                if (replacedCatOp.isPresent()) {
                    ItemCategory replacedCategory = replacedCatOp.get();
                    replacedCategory.setCategoryStatus(CategoryStatus.APPROVED);
                    replacedCategory.setActive(true);
                    approveAndUpdateCategory(mergePendingCategoryDto, replacedCategory);

                }
            }
        }
    }

    @Transactional
    private void mergeWithBody(Jwt token, ItemCategory category, MergePendingCategoryDto mergePendingCategoryDto){

            // merge category
            Optional<ItemCategory> existCatOp = categoryRepository.findByCode(mergePendingCategoryDto.getCode());
            if(existCatOp.isPresent()){
                // if exist then
                List<CategoryWarehouseStore> cws = categoryWarehouseStoreRepository.findByCategoryId(category.getId());
                for(CategoryWarehouseStore cw : cws){
                    cw.setCategory(existCatOp.get());
                }
            } else if (existCatOp.isEmpty()) {
                ItemCategory newCat = new ItemCategory();
                newCat.setCode(mergePendingCategoryDto.getCode());
                approveAndUpdateCategory(mergePendingCategoryDto, newCat);
                newCat.setCategoryStatus(CategoryStatus.APPROVED);
                newCat.setCpsCategoryId(mergePendingCategoryDto.getMergeCategoryId());
                categoryRepository.save(newCat);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
                if(orgOp.isPresent()){
                    headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
                }
                List<ScmIdUpdateDto> dtos  = new ArrayList<>();
                ScmIdUpdateDto scmIdUpdateDto = new ScmIdUpdateDto();
                scmIdUpdateDto.setCategoryIdScm(newCat.getId());
                scmIdUpdateDto.setCategoryIdCps(mergePendingCategoryDto.getMergeCategoryId());
                dtos.add(scmIdUpdateDto);
                HttpEntity<List<ScmIdUpdateDto>> payload = new HttpEntity<>(dtos,headers);
                String url = cpsServerConfig.getItemCategoriesEndpoint().concat("/update-scm-id");
                ResponseEntity<?> response = networkService.put(url,payload,Void.class);
//                System.out.println(response.getStatusCode().value());
            }
    }


    @Transactional
    private void approveAndUpdateCategory(MergePendingCategoryDto mergePendingCategoryDto, ItemCategory replacedCategory) {


//                    if(categoryApproveRequestDto.getMergePendingCategoryDto()!=null){

        if(mergePendingCategoryDto.getName()!=null){
            replacedCategory.setName(mergePendingCategoryDto.getName());
        }
        if(mergePendingCategoryDto.getParentCategory()!=null){
            replacedCategory.setParentCategory(mergePendingCategoryDto.getParentCategory());
        }
        if(mergePendingCategoryDto.getVat()!=null){
            replacedCategory.setVat(mergePendingCategoryDto.getVat());
        }
        if(mergePendingCategoryDto.getAttributes()!=null){
            List<CategoryAttribute> attributes = new ArrayList<>();
            categoryAttributeRepository.deleteByCategoryId(replacedCategory.getId());
            for (CategoryAttribute ca : mergePendingCategoryDto.getAttributes()) {
                Optional<CategoryAttribute> caOp = categoryAttributeRepository
                        .findAllByAttributeTypeAndAttributeUnit(
                                ca.getAttributeType(),
                                ca.getAttributeType()
                        );
                if(caOp.isPresent()){
                    ca.setId(caOp.get().getId());
                    ca.setAttributeType(caOp.get().getAttributeType());
                    ca.setAttributeUnit(caOp.get().getAttributeUnit());
                }else{
                    ca.setId(null);

                }

                ca.setAttributeValue(ca.getAttributeValue());
                ca.setCategory(replacedCategory);
                attributes.add(ca);
            }
            replacedCategory.setAttributes(attributes);
        }
        if(mergePendingCategoryDto.getBrands()!=null && mergePendingCategoryDto.getBrands().size()>0){
            List<String> brands = mergePendingCategoryDto.getBrands();
            List<CategoryBrand> cbs = new ArrayList<>();
            for(String brandName : brands) {
                Optional<CategoryBrand> catBrandOp = categoryBrandRepository
                                .findByCategoryIdAndName(replacedCategory.getId(), brandName);
                cbs= (replacedCategory.getBrands()!=null)? replacedCategory.getBrands(): new ArrayList<>();
                if(!catBrandOp.isPresent()){
                    CategoryBrand cb = new CategoryBrand();
                    cb.setCategory(replacedCategory);
                    cb.setName(brandName);
                    cbs.add(cb);
                }
            }
            replacedCategory.setBrands(cbs);
        }
    }
}
