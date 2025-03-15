package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.common.BrandInterface;
import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;


import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.repository.DemandDetailRepository;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.fileupload.service.FileUploadService;
import com.agi.aesl.erpscm.inventory.dto.request.*;
import com.agi.aesl.erpscm.inventory.entity.*;
import com.agi.aesl.erpscm.inventory.enums.BudgetType;
import com.agi.aesl.erpscm.inventory.enums.CategoryHeader;
import com.agi.aesl.erpscm.inventory.enums.CategoryStatus;
import com.agi.aesl.erpscm.inventory.repository.*;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategory;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryAttribute;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryBrand;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryAttributeRepository;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryRepository;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Integer PAGE_SIZE = 20;

    private final CategoryRepository categoryRepository;


    private final CategoryBudgetRepository categoryBudgetRepository;


    private final CategoryAttributeRepository categoryAttributeRepository;


    private final CategoryBrandRepository categoryBrandRepository;


    private final CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;


    private final ItemRepository itemRepository;


    private final ItemImportLogRepository itemImportLogRepository;


    private final ItemStockRepository itemStockRepository;


    private final CpsServerConfig cpsServerConfig;


    private final OrgService orgService;


    private final ClaimResolver claimResolver;


    private final IntegrationReaderService integrationReaderService;

    private final DemandDetailRepository demandDetailRepository;


    private final WarehouseRepository warehouseRepository;


    private final WarehouseStoreRepository warehouseStoreRepository;


    private final NetworkService networkService;


    private final UserCategoryRepository userCategoryRepository;


    private final UserCategoryAttributeRepository userCategoryAttributeRepository;

    private FileUploadService fileUploadService;
    @Value("${upload.dir}")
    private String uploadDir;

    private static final String ERR_STORE_NOT_FOUND="Sorry! Store not found";
    private static final String URI_INVENTORY_CONTROL_SUBCATEGORIES="inventory-control/sub-categories";

    @Override
    @Transactional
    public void addCategories(Jwt token,List<CategoryRequestDtoCustom> categoryRequestDtos) {
        if(categoryRequestDtos!=null && !categoryRequestDtos.isEmpty()){
            List<ScmIdUpdateDto> dtos = new ArrayList<>();
           for(CategoryRequestDtoCustom categoryRequestDto : categoryRequestDtos){
               Optional<WarehouseStore> warehouseStoreOp = warehouseStoreRepository
                                        .findById(categoryRequestDto.getWarehouseStore().getId());
                if(warehouseStoreOp.isEmpty()){
                    throw new AesException(ERR_STORE_NOT_FOUND);
                }
               WarehouseStore ws = warehouseStoreOp.get();
               String code = ws.getStoreName().substring(0,1).toUpperCase().concat("-").concat(categoryRequestDto.getCode());
               ScmIdUpdateDto scmIdUpdateDto = new ScmIdUpdateDto();
                    CategoryRequestDto cr = new CategoryRequestDto();
                    cr.setCategoryStatus(CategoryStatus.APPROVED);
                    cr.setAttributes(categoryRequestDto.getAttributes());
                    cr.setIsActive(false);
                    List<ItemCategory> codeExists = categoryRepository.findByCodeContaining(categoryRequestDto.getCode());
                    if(!codeExists.isEmpty()){
                        Optional<ItemCategory> codeExist = codeExists.stream().filter(ce->ce.getCode().equals(code)).findAny();
                        if(codeExist.isPresent()) {
                            int cwsOp = categoryWarehouseStoreRepository
                                    .getCountCategoryCodeExistsInWarehouse(categoryRequestDto.getCode(),
                                            categoryRequestDto.getWarehouse().getId());

                            if (cwsOp>0 && !categoryRequestDto.getIsSync()) {
                                throw new AesException("Sorry! This Category[" + codeExist.get().getName() + "] Already Imported in this Warehouse");
                            }

                            ItemCategory itemCategory = codeExist.get();
                            if (itemCategory.getCode().equals(code)) {
                                itemCategory.setActive(true);
                                cr.setIsActive(true);
                                cr.setParentCategory(itemCategory.getParentCategory());
                                cr.setId(itemCategory.getId());
                            }
                        }
                    }
                    cr.setCode(code);
                    cr.setCpsCategoryId(categoryRequestDto.getCpsCategoryId());
                    cr.setName(categoryRequestDto.getName());
                    if(categoryRequestDto.getParentCategory()!=null) {
                        cr.setParentCategory(categoryRequestDto.getParentCategory());
                    }
                    cr.setRequestedBy(categoryRequestDto.getRequestedBy());
                    cr.setVat(categoryRequestDto.getVat());
                    cr.setWarehouse(categoryRequestDto.getWarehouse());
                    cr.setWarehouseStore(categoryRequestDto.getWarehouseStore());
                    if(categoryRequestDto.getBrands()!=null && !categoryRequestDto.getBrands().isEmpty()){
                        cr.setBrands(categoryRequestDto.getBrands());
                    }
                cr.setCurrentYearBudget(new BigDecimal(0));
                cr.setIsForCps(categoryRequestDto.getIsForCps());
                scmIdUpdateDto.setCategoryIdCps(categoryRequestDto.getCpsCategoryId());
               Optional<ItemCategory> catOp = Optional.empty();
               if(Boolean.TRUE.equals(cr.getIsActive())){
                   cr.setBudgetId(Optional.empty());
                    this.updateCategoryDuringImport(cr.getId(),cr);
                }else {
                    catOp = this.addCategory(null, cr);
                }
               catOp.ifPresent(itemCategory -> scmIdUpdateDto.setCategoryIdScm(itemCategory.getId()));
                dtos.add(scmIdUpdateDto);
            }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
                orgOp.ifPresent(organization -> headers.set("orgId", organization.getCpsVendorRegistrationId().toString()));
                HttpEntity<List<ScmIdUpdateDto>> payload = new HttpEntity<>(dtos,headers);
                String url = cpsServerConfig.getItemCategoriesEndpoint().concat("/update-scm-id");
                networkService.put(url,payload,Void.class);

        }
    }

    @Transactional
    private void updateCategoryDuringImport(Long id, CategoryRequestDto cr) {
        Optional<ItemCategory> itemCategoryOptional = categoryRepository.findById(id);
        if(itemCategoryOptional.isEmpty()){
            throw new AesException("Category Not Found");
        }

        ItemCategory itemCategory = itemCategoryOptional.get();
        if(!itemCategory.getCode().equalsIgnoreCase(cr.getCode())){
            throw new AesException("Category Code should be unique");
        }

        if(itemCategory.getParentCategory()!=null && cr.getParentCategory()==null || cr.getParentCategory().getId()==null){
                throw new AesException("Parent Category Id missing");
        }

        if(cr.getName()!=null) {
            itemCategory.setName(cr.getName());
        }

        if(cr.getBudgetId().isPresent()){

            Optional<CategoryBudget> categoryBudgetOp = categoryBudgetRepository
                    .findById(cr.getBudgetId().get());
            if(categoryBudgetOp.isPresent()) {
                CategoryBudget categoryBudget = categoryBudgetOp.get();
                if(categoryBudget.getAmount().compareTo(cr.getCurrentYearBudget())<0){
                    CategoryBudget extendedBudget = new CategoryBudget(itemCategory,
                            cr.getCurrentYearBudget(),
                            LocalDate.now().getYear(), BudgetType.EXTENDED);

                    extendedBudget.setAmount(cr.getCurrentYearBudget()
                            .subtract(categoryBudget.getAmount()));
                    categoryBudgetRepository.save(extendedBudget);
                } else {
                    categoryBudget.setAmount(cr.getCurrentYearBudget());
                    categoryBudgetRepository.save(categoryBudget);

                }
            }
        }

        if(cr.getBrands()!=null && !cr.getBrands().isEmpty()){
            cr.getBrands().stream().forEach(b->{
                Optional<CategoryBrand> cbOp = itemCategory.getBrands().stream()
                                            .filter(cb-> b.equals(cb.getName())).findFirst();
                if(cbOp.isEmpty()){
                    CategoryBrand cb1 = new CategoryBrand();
                    cb1.setCategory(itemCategory);
                    cb1.setName(b);
                    categoryBrandRepository.save(cb1);
                }else{
                    CategoryBrand cb = cbOp.get();
                    cb.setIsActive(true);
                }

            });
        }
        if(cr.getAttributes()!=null && !cr.getAttributes().isEmpty()){

            cr.getAttributes().forEach(categoryAttribute -> {
                Optional<CategoryAttributeRepository.ICategoryAttribute> categoryAttributeOp= categoryAttributeRepository.findAllByAttributeTypeAndAttributeUnit(
                        cr.getWarehouse().getId(), cr.getCode(), categoryAttribute.getAttributeType(),categoryAttribute.getAttributeUnit());
                if(categoryAttributeOp.isEmpty()){
                    categoryAttribute.setCategory(itemCategory);
                    categoryAttributeRepository.save(categoryAttribute);
                }else{
                    CategoryAttributeRepository.ICategoryAttribute caExist = categoryAttributeOp.get();
                    CategoryAttribute categoryAttr = new CategoryAttribute();
                    categoryAttr.setCategory(itemCategory);
                    categoryAttr.setId(caExist.getId());

                    if(categoryAttribute.getAttributeValue().length() > caExist.getAttributeValue().length()){
                        categoryAttr.setAttributeValue(categoryAttribute.getAttributeValue());
                    }else {
                        categoryAttr.setAttributeValue(caExist.getAttributeValue());
                    }

                    categoryAttr.setAttributeType(caExist.getAttributeType());
                    categoryAttr.setAttributeUnit(caExist.getAttributeUnit());
                    categoryAttributeRepository.save(categoryAttr);
                }


            });
        }

        if(cr.getEntity().getParentCategory()!=null) {
            itemCategory.setParentCategory(cr.getEntity().getParentCategory());
        }
        if(cr.getVat()!=null) {
            itemCategory.setVat(cr.getVat());
        }
        itemCategory.setActive(true);
        categoryRepository.save(itemCategory);
    }

    @Override
    @Transactional
    public Optional<ItemCategory> addCategory(Jwt token, CategoryRequestDto categoryRequestDto) {
        ItemCategory category = categoryRequestDto.getEntity();

        if(categoryRequestDto.getUserCategoryId()!=null){
            category.setUserCategoryId(categoryRequestDto.getUserCategoryId());
        }
        Optional<ItemCategory> itemCategoryOptional = categoryRepository.findByCode(category.getCode());
        if(itemCategoryOptional.isPresent()){
            category = itemCategoryOptional.get();
        }


        if(itemCategoryOptional.isEmpty() && categoryRequestDto.getCurrentYearBudget() != null){
            category.setBudgets(Arrays.asList(new CategoryBudget(category,
                    categoryRequestDto.getCurrentYearBudget(), LocalDate.now().getYear(), BudgetType.REGULAR)));
        }


        if(categoryRequestDto.getAttributes()!=null && !categoryRequestDto.getAttributes().isEmpty()){
            
            
            ItemCategory finalCategory = category;
            
            category.setAttributes(categoryRequestDto.getAttributes().stream().map(categoryAttribute -> {
                Optional<CategoryAttribute> catAttrOp = finalCategory.getAttributes().stream().filter(fca->
                    fca.getAttributeType().trim().equals(categoryAttribute.getAttributeType().trim())
                ).findFirst();
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
            }).toList());
        }

        if(itemCategoryOptional.isEmpty() && categoryRequestDto.getBrands()!=null && !categoryRequestDto.getBrands().isEmpty()){
            ItemCategory finalCategory = category;

            category.setBrands(categoryRequestDto.getBrands().stream().map(categoryBrand -> {

                CategoryBrand categoryBrand1 = new CategoryBrand(categoryBrand);
                categoryBrand1.setCategory(finalCategory);
                return categoryBrand1;
            }).toList());
        }

        if(categoryRequestDto.getCpsCategoryId()!=null){
            category.setCpsCategoryId(categoryRequestDto.getCpsCategoryId());
        }


        category.setActive(Boolean.FALSE.equals(categoryRequestDto.getIsForCps()));

        if(categoryRequestDto.getCategoryStatus()!=null){
            category.setCategoryStatus(categoryRequestDto.getCategoryStatus());
        }else{
            category.setCategoryStatus(CategoryStatus.PENDING);
        }

        categoryRepository.save(category);
        
        Optional<CategoryWarehouseStore> cwsOp =  categoryWarehouseStoreRepository.findByCategoryIdAndWarehouseId(category.getId() ,categoryRequestDto.getWarehouse().getId());
        Optional<Warehouse> warehouseOp = warehouseRepository.findById(categoryRequestDto.getWarehouse().getId());
        if(warehouseOp.isEmpty()){
            throw new AesException("Sorry! Warehouse not found");
        }

        Optional<WarehouseStore> warehouseStoreOp = warehouseStoreRepository.findById(categoryRequestDto.getWarehouseStore().getId());
        if(warehouseStoreOp.isEmpty()){
            throw new AesException(ERR_STORE_NOT_FOUND);
        }
        if(cwsOp.isEmpty()){
            CategoryWarehouseStore categoryWarehouseStore = new CategoryWarehouseStore();
            categoryWarehouseStore.setCategory(category);
            categoryWarehouseStore.setWarehouse(new Warehouse(categoryRequestDto.getWarehouse().getId()));
            categoryWarehouseStore.setWarehouseStore(new WarehouseStore(categoryRequestDto.getWarehouseStore().getId()));
            categoryWarehouseStoreRepository.save(categoryWarehouseStore);
        }

        if(category.getId()!=null && categoryRequestDto.getIsForCps()){
            WarehouseStore ws = warehouseStoreOp.get();
            String storePrefix = ws.getStoreName().substring(0,1);
            storePrefix = storePrefix.toUpperCase();
            this.sendToCps(token,category,ws.getId(),storePrefix,categoryRequestDto.getPrefix(),categoryRequestDto.getEmployee());
        }
        return categoryRepository.findById(category.getId());

    }

    public void sendToCps(Jwt token, ItemCategory category, Long storeId, String storePrefix,String prefix, String employee){
        RemoteCategoryRequestDto remoteCategoryRequestDto = new RemoteCategoryRequestDto();
        remoteCategoryRequestDto.setName(category.getName());
        remoteCategoryRequestDto.setStoreTypeId(storeId);
        remoteCategoryRequestDto.setPrefix(prefix);

        if(category.getParentCategory()!=null) {
            Optional<ItemCategory> parentCategoryOp = categoryRepository.findById(category.getParentCategory().getId());

            if (parentCategoryOp.isPresent()) {
                remoteCategoryRequestDto.setParentCategory(new ReferenceObjectDto(parentCategoryOp.get().getCpsCategoryId()));
            }

            remoteCategoryRequestDto.setAttributes(category.getAttributeInterfaces().stream().map(attr->{
                CategoryAttribute ca = new CategoryAttribute();
                ca.setAttributeType(attr.getAttributeType());
                ca.setAttributeUnit(attr.getAttributeUnit());
                ca.setAttributeValue(attr.getAttributeValue());
                return ca;
            }).toList());
            remoteCategoryRequestDto.setBrands(category.getBrandInterfaces().stream().map((BrandInterface::getName))
                    .toList());
            remoteCategoryRequestDto.setVat(category.getVat());
        }
        remoteCategoryRequestDto.setScmCategoryId(category.getId());

        remoteCategoryRequestDto.setCreatedBy(employee);
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
        List<String> headerCode = httpHeaders.get("code");
        if(headerId!=null && !headerId.isEmpty() &&  headerCode!=null && !headerCode.isEmpty()){
            category.setCode(storePrefix.concat("-").concat(headerCode.get(0)));
            category.setCpsCategoryId(Long.parseLong(headerId.get(0)));
        }
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

        if(itemCategory.getParentCategory()!=null && categoryRequestDto.getParentCategory()==null || categoryRequestDto.getParentCategory().getId()==null){
                throw new AesException("Parent Category Id missing");
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

        if(categoryRequestDto.getAttributes()!=null && !categoryRequestDto.getAttributes().isEmpty()){
            itemCategory.setAttributes(categoryRequestDto.getAttributes().stream().map(categoryAttribute -> {
                categoryAttribute.setCategory(itemCategory);
                return categoryAttribute;
            }).toList());

        }

        if(categoryRequestDto.getEntity().getParentCategory()!=null) {
            itemCategory.setParentCategory(categoryRequestDto.getEntity().getParentCategory());
        }
        if(categoryRequestDto.getVat()!=null) {
            itemCategory.setVat(categoryRequestDto.getVat());
        }
        itemCategory.setActive(true);
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

    @Override
    public Page<CategoryRepository.ItemCategoryInfoExt> getItemCategories(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                          Optional<String> name, Optional<String> code,
                                                                          Optional<Integer> year,
                                                                          Optional<BigDecimal> currentYearBudget,
                                                                          Optional<Long> productCount,
                                                                          Optional<Long> warehouseId,
                                                                          Optional<Long> warehouseStoreId
                                     ) {

        claimResolver.setToken(token);
        String uri = "inventory-management/main-category";

        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(10));

        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            DataFilter dataFilter = new DataFilter(uri,claimResolver);
            dataFilter.setReaderService(integrationReaderService);
            warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }

            return categoryRepository.findAllByYear(name.orElse(null),
                    code.orElse(null), currentYearBudget.orElse(null),
                    productCount.orElse(null),year.orElse(null),
                    warehouseIds,warehouseStoreId.orElse(null)
                    ,pageable);


    }

    @Override
    public Page<CategoryRepository.SubCategoryInfoExt> getItemCategories(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                         Optional<String> name, Optional<String> code, Optional<Integer> year,
                                                                         Optional<BigDecimal> currentYearBudget, Optional<Long> productCount,
                                                                         Optional<Long> categoryId,
                                                                         Optional<Long> warehouseId,
                                                                         Optional<Long> warehouseStoreId
                                      ) {

        claimResolver.setToken(token);
        String uri = "inventory-management/sub-category";



        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(10));

        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            DataFilter dataFilter = new DataFilter(uri,claimResolver);
            dataFilter.setReaderService(integrationReaderService);
            warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }

        return categoryRepository.findAllSubCategories(
                            name.orElse(null),
                            code.orElse(null),
                            currentYearBudget.orElse(null),
                            productCount.orElse(null),
                            categoryId.orElse(null),
                            year.orElse(null),
                            warehouseIds,
                            warehouseStoreId.orElse(null)
                            ,pageable);


    }


    @Override
    public List<?> getCategories(Optional<Long> warehouseId,Optional<Long> warehouseStoreId,  Optional<String> name, Optional<String> code) {

        return categoryRepository.findAllMainCategories(
                warehouseId.orElse(null),
                warehouseStoreId.orElse(null),
                name.orElse(null),code.orElse(null));
    }

    @Override
    public Page<?> getCategories(Jwt token,Optional<Long> warehouseId, Optional<Long> warehouseStoreId, Optional<String> name,
                                 Optional<String> code, Optional<Integer> page, Optional<Integer> size) {

        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE));
        String uri=URI_INVENTORY_CONTROL_SUBCATEGORIES;
        List<Long> warehouseIds = new ArrayList<>();


        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> filterBy = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = filterBy;
        }


        return categoryRepository.findAllMainCategoriesForInventoryControl(
                warehouseIds,
                warehouseStoreId.orElse(null),
                name.orElse(null),code.orElse(null),pageable);
    }

    @Override
    public List<?> getCategoriesForInventoryControl(
                                                    Jwt token,
                                                    Optional<Long> warehouseId,
                                                    Optional<Long> warehouseStoreId,
                                                    Optional<String> name,
                                                    Optional<String> code) {
        claimResolver.setToken(token);
        String uri="inventory-control/categories";
        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            DataFilter dataFilter = new DataFilter(uri,claimResolver);
            dataFilter.setReaderService(integrationReaderService);
            warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }

        return categoryRepository.findAllMainCategoriesForInventoryControl(
                warehouseIds,warehouseStoreId.orElse(null),
                name.orElse(null),code.orElse(null));
    }

    @Override
    public List<CategoryRepository.ItemCategoryInfo> getSubCategories(Optional<Long> storeId, Optional<Long> id, Optional<String> name, Optional<String> code) {

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
            Jwt token,
            Optional<Long> categoryId,
            Optional<Long> warehouseId,
            Optional<Long> storeId,
            Optional<String> name, Optional<String> code) {

        claimResolver.setToken(token);
        List<Long> warehouseIds = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();

        DataFilter dataFilter = new DataFilter(URI_INVENTORY_CONTROL_SUBCATEGORIES,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> filterBy = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
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

        return categoryRepository.findAllSubCategoriesForInventoryControl(
                categoryIds,
                warehouseIds,
                storeId.orElse(null),
                name.orElse(null),
                code.orElse(null));
    }

    @Override
    public Page<?> getSubCategoriesForInventoryControl(Jwt token, Optional<Long> categoryId, Optional<Long> warehouseId, Optional<Long> storeId, Optional<String> name, Optional<String> code, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));

        List<Long> warehouseIds = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();

        DataFilter dataFilter = new DataFilter(URI_INVENTORY_CONTROL_SUBCATEGORIES,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> filterBy = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
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

        return categoryRepository.findAllSubCategoriesForInventoryControl(
                categoryIds,
                warehouseIds,
                storeId.orElse(null),
                name.orElse(null),
                code.orElse(null), pageable);
    }

    @Override
    public List<CategoryRepository.ItemCategoryInfo> getPendingSubCategoriesForInventoryControl(
            Jwt token,
            Optional<Long> categoryId,
                                                              Optional<Long> warehouseId,
                                                              Optional<Long> storeId,
                                                              Optional<String> name,
                                                              Optional<String> code) {

        claimResolver.setToken(token);
        DataFilter dataFilter = new DataFilter(URI_INVENTORY_CONTROL_SUBCATEGORIES,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }

        return categoryRepository.findAllPendingSubCategoriesForInventoryControl(
                categoryId.orElse(null),
                warehouseIds,
                storeId.orElse(null),
                name.orElse(null),
                code.orElse(null));
    }

    @Override
    public Page<CategoryRepository.ItemCategoryInfo> getPendingSubCategoriesForInventoryControl(Jwt token, Optional<Long> categoryId, Optional<Long> warehouseId, Optional<Long> storeId, Optional<String> name, Optional<String> code, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);

        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        DataFilter dataFilter = new DataFilter(URI_INVENTORY_CONTROL_SUBCATEGORIES,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }

        return categoryRepository.findAllPendingSubCategoriesForInventoryControl(
                categoryId.orElse(null),
                warehouseIds,
                storeId.orElse(null),
                name.orElse(null),
                code.orElse(null),pageable);
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
        

        if(warehouseId!=null && storeId!=null){

            Optional<Warehouse> wOptional = warehouseRepository.findById(warehouseId);
            if(wOptional.isEmpty()){
                throw new AesException("Sorry! Warehouse not found");
            }

            Optional<WarehouseStore> wsOptional = warehouseStoreRepository.findById(storeId);
            if(wsOptional.isEmpty()){
                throw new AesException(ERR_STORE_NOT_FOUND);
            }

            CategoryWarehouseStore cws = new CategoryWarehouseStore();
            cws.setCategory(itemCategory);
            cws.setWarehouse(wOptional.get());
            cws.setWarehouseStore(wsOptional.get());
            categoryWarehouseStoreRepository.save(cws);
        }
        
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, Long warehouseId, Long storeId) {

        Optional<ItemCategory> itemCategoryOptional = categoryRepository.findById(id);
        if(itemCategoryOptional.isPresent()){

            ItemCategory itemCategory = itemCategoryOptional.get();
            if(itemCategory.getParentCategory()==null){
                Optional<Long> countOptional = categoryRepository.countAllByParentCategoryAndActive(
                        itemCategoryOptional.get().getId(),warehouseId,storeId, true);
                if(countOptional.isPresent() && countOptional.get() > 0){
                 throw new AesException("Sorry! Unable to delete, Category already used in Child Category");
                }
            }



            List<Item> items = itemRepository.findAllByItemCategoryIdAndActive(itemCategory.getId(),true);
            if(items.isEmpty()){
                categoryAttributeRepository.deleteByCategoryId(itemCategory.getId());
                List<Item> inactiveItems = itemRepository.findAllByItemCategoryIdAndActive(itemCategory.getId(),false);
                for(Item item : inactiveItems){
                    itemImportLogRepository.deleteByItemId(item.getId());
                    itemRepository.deleteById(item.getId());
                }
                if (warehouseId != null && storeId != null) {
                    categoryWarehouseStoreRepository
                            .deleteByCategoryIdAndWarehouseIdAndWarehouseStoreId(
                                    itemCategory.getId(),
                                    warehouseId,
                                    storeId
                            );
                }
                categoryRepository.deleteById(itemCategory.getId());
            }else {

                List<Long> itemIds = items.stream().map(i -> i.getId()).toList();
                List<ItemStock> stockExist = itemStockRepository.findByItemsAndWarehosueId(itemIds, warehouseId);
                if (!stockExist.isEmpty()) {
                    throw new AesException("Sorry! Item exist under this category in this warehouse");
                }
                List<DemandDetail> demandDetails = demandDetailRepository.findAllByItemIdAndWarehouseId(itemIds, warehouseId);
                if (!demandDetails.isEmpty()) {
                    throw new AesException("Sorry! Item under this category has some demand in this warehouse, so unable to remove");
                }

                if (warehouseId != null && storeId != null) {
                    categoryWarehouseStoreRepository
                            .deleteByCategoryIdAndWarehouseIdAndWarehouseStoreId(
                                    itemCategory.getId(),
                                    warehouseId,
                                    storeId
                            );
                }
                categoryRepository.save(itemCategory);
            }


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

    @Override
    public Optional<ItemCategory> getItemCategoryByName(String catName) {
        return categoryRepository.findByName(catName);
    }

    @Override
    public Optional<ItemCategory> getCategoryByCode(String subCategoryCode) {
        return categoryRepository.findByCode(subCategoryCode);
    }


    @Override
    public List<CategoryRepository.ItemCategoryInfo> getPendingCategories(
            Jwt token,
            Optional<Long> warehouseId,
            Optional<Long> warehouseStoreId,
            Optional<String> name,
            Optional<String> code
    ) {

        claimResolver.setToken(token);
        String uri="inventory-control/categories";
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else {
            warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }
        return categoryRepository.findAllPendingCategories(warehouseIds,warehouseStoreId.orElse(null),
                name.orElse(null),code.orElse(null));
    }

    @Override
    public Page<CategoryRepository.ItemCategoryInfo> getPendingCategories(Jwt token, Optional<Long> warehouseId,
                                                                          Optional<Long> warehouseStoreId, Optional<String> name,
                                                                          Optional<String> code, Optional<Integer> page, Optional<Integer> size) {

        claimResolver.setToken(token);
        String uri="inventory-control/categories";
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else {
            warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }
        Pageable pageable =PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return categoryRepository.findAllPendingCategories(warehouseIds,warehouseStoreId.orElse(null),
                name.orElse(null),code.orElse(null),pageable);
    }

    @Override
    @Transactional
    public void approveItemCategory(Jwt token, Long id, CategoryApproveRequestDto categoryApproveRequestDto) {
        claimResolver.setToken(token);
        Optional<ItemCategory> catOp = categoryRepository.findById(id);
        MergePendingCategoryDto mergePendingCategoryDto = categoryApproveRequestDto.getMergePendingCategoryDto();
        if(catOp.isPresent()) {
            ItemCategory category = catOp.get();
            setYearlyBudget(LocalDate.now().getYear(),category);

            if (categoryApproveRequestDto.getApproveStatus().equals(ApproveStatus.APPROVED)) {
                approvedWithBody(token, categoryApproveRequestDto,mergePendingCategoryDto);
                category.setActive(true);
                category.setCategoryStatus(CategoryStatus.APPROVED);
                if(category.getUserCategoryId()!=null){
                    Optional<UserCategory> userCategoryOp = userCategoryRepository.findById(category.getUserCategoryId());
                    userCategoryOp.ifPresent((uc->{
                        approveAndUpdateCategory(mergePendingCategoryDto,uc);
                        uc.setCategoryStatus(UserCategoryStatus.COMPLETED);
                    }));
                }
            } else if (categoryApproveRequestDto.getApproveStatus().equals(ApproveStatus.REJECTED)) {
                if(categoryApproveRequestDto.getMergePendingCategoryDto()!=null) {
                    UserCategory uc = null;
                    if(category.getUserCategoryId()!=null){
                        Optional<UserCategory> userCategoryOp = userCategoryRepository.findById(category.getUserCategoryId());
                        if(userCategoryOp.isPresent()){
                            uc=userCategoryOp.get();
                            uc.setCategoryStatus(UserCategoryStatus.MERGED);
                        }
                    }
                    mergeWithBody(token, uc, category,categoryApproveRequestDto.getWarehouseStoreId(), mergePendingCategoryDto);
                }else{
                    if(category.getUserCategoryId()!=null){
                        Optional<UserCategory> userCategoryOp = userCategoryRepository.findById(category.getUserCategoryId());
                        userCategoryOp.ifPresent((uc->
                            uc.setCategoryStatus(UserCategoryStatus.REJECTED)
                        ));
                    }
                }
                category.setActive(false);
                category.setCategoryStatus(CategoryStatus.REJECTED);
            }
        }
    }

    @Transactional
    private void approvedWithBody(Jwt token, CategoryApproveRequestDto categoryApproveRequestDto,
                                  MergePendingCategoryDto mergePendingCategoryDto){
        Optional<WarehouseStore> wsOp = warehouseStoreRepository.findById(categoryApproveRequestDto.getWarehouseStoreId());
        if(wsOp.isEmpty()){
            throw new AesException("Sorry! Warehouse Store not found");
        }
        WarehouseStore ws = wsOp.get();
        String storeWisePrefixCode = ws.getStoreName().substring(0,1)+"-"+mergePendingCategoryDto.getCode();
        if (categoryApproveRequestDto.getCode() == null ) {
            Optional<ItemCategory> replacedCatOp = categoryRepository.findByCode(storeWisePrefixCode);
            if (categoryApproveRequestDto.getApproveStatus()
                    .equals(ApproveStatus.APPROVED) && replacedCatOp.isPresent()) {
                    ItemCategory replacedCategory = replacedCatOp.get();
                    replacedCategory.setCategoryStatus(CategoryStatus.APPROVED);
                    replacedCategory.setActive(true);
                    approveAndUpdateCategory(mergePendingCategoryDto, replacedCategory);
            }
        }
    }

    @Transactional
    private void mergeWithBody(Jwt token, UserCategory uc, ItemCategory category,Long warehouseStoreId, MergePendingCategoryDto mergePendingCategoryDto){

            Optional<WarehouseStore> wsOp = warehouseStoreRepository.findById(warehouseStoreId);
            if(wsOp.isEmpty()){
                throw new AesException("Sorry! Warehouse Store not found");
            }

            WarehouseStore ws = wsOp.get();
            String storeWisePrefixCode = ws.getStoreName().substring(0,1)+"-"+mergePendingCategoryDto.getCode();

            // merge category
            Optional<ItemCategory> existCatOp = categoryRepository.findByCode(storeWisePrefixCode);
            if(existCatOp.isPresent()){
                // if exist then
                ItemCategory catExist = existCatOp.get();
                if(uc!=null){
                    uc.setMergedCategory(catExist.getName());
                }
                List<CategoryWarehouseStore> cws = categoryWarehouseStoreRepository.findByCategoryId(category.getId());
                for(CategoryWarehouseStore cw : cws){
                    categoryWarehouseStoreRepository.delete(cw);
                }
            } else {
                ItemCategory newCat = new ItemCategory();
                newCat.setCode(mergePendingCategoryDto.getCode());
                approveAndUpdateCategory(mergePendingCategoryDto, newCat);
                newCat.setCategoryStatus(CategoryStatus.APPROVED);
                newCat.setCpsCategoryId(mergePendingCategoryDto.getMergeCategoryId());
                categoryRepository.save(newCat);
                if(uc!=null){
                    uc.setMergedCategory(newCat.getName());
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
                orgOp.ifPresent(org->
                    headers.set("orgId", org.getCpsVendorRegistrationId().toString())
                );
                List<ScmIdUpdateDto> dtos  = new ArrayList<>();
                ScmIdUpdateDto scmIdUpdateDto = new ScmIdUpdateDto();
                scmIdUpdateDto.setCategoryIdScm(newCat.getId());
                scmIdUpdateDto.setCategoryIdCps(mergePendingCategoryDto.getMergeCategoryId());
                dtos.add(scmIdUpdateDto);
                HttpEntity<List<ScmIdUpdateDto>> payload = new HttpEntity<>(dtos,headers);
                String url = cpsServerConfig.getItemCategoriesEndpoint().concat("/update-scm-id");
                networkService.put(url,payload,Void.class);
            }
    }


    @Transactional
    private void approveAndUpdateCategory(MergePendingCategoryDto mergePendingCategoryDto, ItemCategory replacedCategory) {



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
        if(mergePendingCategoryDto.getBrands()!=null && !mergePendingCategoryDto.getBrands().isEmpty()){
            List<String> brands = mergePendingCategoryDto.getBrands();
            List<CategoryBrand> cbs = new ArrayList<>();
            for(String brandName : brands) {
                Optional<CategoryBrand> catBrandOp = categoryBrandRepository
                                .findByCategoryIdAndName(replacedCategory.getId(), brandName);
                cbs= (replacedCategory.getBrands()!=null)? replacedCategory.getBrands(): new ArrayList<>();
                if(catBrandOp.isEmpty()){
                    CategoryBrand cb = new CategoryBrand();
                    cb.setCategory(replacedCategory);
                    cb.setName(brandName);
                    cbs.add(cb);
                }
            }
            replacedCategory.setBrands(cbs);
        }
    }

    @Transactional
    private void approveAndUpdateCategory(MergePendingCategoryDto mergePendingCategoryDto, UserCategory replacedCategory) {


        if(mergePendingCategoryDto.getName()!=null){
            replacedCategory.setName(mergePendingCategoryDto.getName());
        }
        if(mergePendingCategoryDto.getParentCategory()!=null){
            replacedCategory.setActiveParentCategory(mergePendingCategoryDto.getParentCategory());
        }
        if(mergePendingCategoryDto.getVat()!=null){
            replacedCategory.setVat(mergePendingCategoryDto.getVat());
        }
        if(mergePendingCategoryDto.getAttributes()!=null){
            List<UserCategoryAttribute> attributes = new ArrayList<>();
            categoryAttributeRepository.deleteByCategoryId(replacedCategory.getId());
            for (CategoryAttribute ca : mergePendingCategoryDto.getAttributes()) {
                Optional<UserCategoryAttribute> caOp = userCategoryAttributeRepository
                        .findAllByAttributeTypeAndAttributeUnit(
                                ca.getAttributeType(),
                                ca.getAttributeType()
                        );
                UserCategoryAttribute uca = new UserCategoryAttribute();
                if(caOp.isPresent()){
                    uca.setId(caOp.get().getId());
                    uca.setAttributeType(caOp.get().getAttributeType());
                    uca.setAttributeUnit(caOp.get().getAttributeUnit());
                }else{
                    uca.setId(null);

                }

                uca.setAttributeValue(ca.getAttributeValue());
                uca.setCategory(replacedCategory);
                attributes.add(uca);
            }
            replacedCategory.setAttributes(attributes);
        }
        if(mergePendingCategoryDto.getBrands()!=null && !mergePendingCategoryDto.getBrands().isEmpty()){
            List<String> brands = mergePendingCategoryDto.getBrands();
            List<UserCategoryBrand> cbs = new ArrayList<>();
            for(String brandName : brands) {
                Optional<CategoryBrand> catBrandOp = categoryBrandRepository
                        .findByCategoryIdAndName(replacedCategory.getId(), brandName);
                cbs= (replacedCategory.getBrands()!=null)? replacedCategory.getBrands(): new ArrayList<>();
                if(catBrandOp.isEmpty()){
                    UserCategoryBrand cb = new UserCategoryBrand();
                    cb.setCategory(replacedCategory);
                    cb.setName(brandName);
                    cbs.add(cb);
                }
            }
            replacedCategory.setBrands(cbs);
        }
    }

    @Override
    public void validateCategorySubCategoryRelation(ItemCategory cat, ItemCategory subCat) {

        Optional<ItemCategory> itemCatOp = getAnyItemCategory(cat.getId());
        if(itemCatOp.isEmpty()){
            throw new AesException("Sorry! Category not found");
        }

        Optional<ItemCategory> itemSubCatOp = getAnyItemCategory(subCat.getId());
        if(itemSubCatOp.isEmpty()){
            throw new AesException("Sorry! SubCategory not found");
        }

        ItemCategory category = itemCatOp.get();
        ItemCategory subCategory = itemSubCatOp.get();
        if(!subCategory.getParentCategory().getId().equals(category.getId())){
            throw new AesException("Sorry! " + subCategory.getName()+ " is not under category "+category.getName());
        }
    }

    @Override
    public void setYearlyBudget(Integer year, ItemCategory category) {
        CategoryBudget cb = new CategoryBudget();
        cb.setCategory(category);
        cb.setAmount(new BigDecimal(0));
        cb.setBudgetType(BudgetType.REGULAR);
        cb.setCurrentYear(year);
        categoryBudgetRepository.save(cb);
    }

    @Override
    public Optional<Map<String, Object>> getItemCategoryDetail(Long id) {
        record CategoryWarehouse(Long id, String warehouseName,String storeName){}
        Optional<ItemCategory> catOp = categoryRepository.findAnyCategoryById(id);
        if(catOp.isPresent()) {

            ItemCategory category = catOp.get();
            List<CategoryWarehouseStore> cwsList = categoryWarehouseStoreRepository.findByCategoryId(category.getId());
            List<CategoryWarehouse> cwses = cwsList.stream().map(cws->{
                Warehouse warehouse = cws.getWarehouse();
                return new CategoryWarehouse(warehouse.getId(), cws.getWarehouse().getName(),cws.getWarehouseStore().getStoreName());
            }).toList();
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("id",category.getId());
            detailMap.put("categoryStatus",category.getCategoryStatus());
            detailMap.put("cpsCategoryId",category.getCpsCategoryId());
            detailMap.put("name",category.getName());
            detailMap.put("parentCategory",category.getParentCategory());
            detailMap.put("code",category.getCode());
            detailMap.put("active",category.getActive());
            detailMap.put("attributes",category.getAttributes());
            detailMap.put("brands",category.getBrands());
            detailMap.put("budgets",category.getBudgets());
            detailMap.put("vat",category.getVat());
            detailMap.put("cws",cwses);
            return Optional.of(detailMap);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ItemCategory> getItemCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<ItemCategory> getCategoryByUserCategory(Long id) {
        return categoryRepository.findByUserCategoryId(id);
    }
    
    private Iterable<CSVRecord> getItemRecords(FileUploadResponse fileUploadResponse) throws IOException{
        FileReader in = new FileReader(fileUploadResponse.getPath()+"/"+fileUploadResponse.getFilename());
        Iterable<CSVRecord> records  = CSVFormat.DEFAULT.builder().setHeader(CategoryHeader.class).build().parse(in);
        records.iterator().next();
        return records;
    }

    @Async
    @Override
    public void importCategories(Optional<MultipartFile> fileOp) {
        Path path = Path.of(uploadDir+"/inventory-mgm/categories");
        FileUploadResponse fileUploadResponse = null;
        if(fileOp.isPresent()){
            fileUploadResponse = fileUploadService.uploadFile(path, fileOp.get());
            try {
                Iterable<CSVRecord> records = getItemRecords(fileUploadResponse);
                for(CSVRecord r : records){

                    Long id = Long.valueOf(r.get("ID"));

                    String budgetYearStr = (r.get("BUDGET_YEAR")).trim();
                    String amount = (r.get("AMOUNT")).trim();
                    if(budgetYearStr.isEmpty()){
                        throw new AesException("Budget Year field should not be blank or empty string");
                    }
                    if(amount.isEmpty()){
                        throw new AesException("Amount field should not be blank or empty string");
                    }
                    Integer budgetYear = Integer.parseInt(budgetYearStr);
                    List<CategoryBudget> categoryBudgetOp = categoryBudgetRepository.findByCategoryIdAndBudgetTypeAndCurrentYear(id,BudgetType.REGULAR,budgetYear);
                    if(!categoryBudgetOp.isEmpty()){
                        CategoryBudget categoryBudget = categoryBudgetOp.get(categoryBudgetOp.size()-1);
                        if(categoryBudget.getAmount().compareTo(new BigDecimal(amount))<0){
                            CategoryBudget extendedBudget = new CategoryBudget(categoryBudget.getCategory(),
                                    new BigDecimal(amount),
                                    LocalDate.now().getYear(), BudgetType.EXTENDED);

                            extendedBudget.setAmount(new BigDecimal(amount)
                                    .subtract(categoryBudget.getAmount()));
                            categoryBudgetRepository.save(extendedBudget);
                        } else {
                            categoryBudget.setAmount(new BigDecimal(amount));
                            categoryBudgetRepository.save(categoryBudget);

                        }
                    }

                }
            } catch (NumberFormatException e){
                throw new AesException("Sorry! Number fields might not have number value pls check (ID, WAREHOUSE ID, WAREHOUSE STORE ID)");
            }
            catch (FileNotFoundException e) {
                throw new AesException(e.getMessage());
            } catch (IOException e) {
                throw new AesException("File Columns are not valid for extracting value: "+e.getMessage());
            }
        }
    }

    @Override
    public List<CategoryRepository.SubcategoryTemplate> getTemplateData(Long categoryId, Long warehouseId, Long warehouseStoreId) {
        return categoryRepository.findSubCategoryTemplate(categoryId,warehouseId,warehouseStoreId);
    }

    @Override
    @Transactional
    public void syncCategories(Jwt token,
                               CpsServerConfig cpsServerConfig,
                               Long warehouseId,
                               Long warehouseStoreId, List<Long> categoryIds) {

        claimResolver.setToken(token);
        String userId = claimResolver.getUserId();

        Map<String,Object> data = new HashMap<>();
        data.put("id",categoryIds);
        data.put("userId",userId);
        data.put("warehouseId",warehouseId);
        data.put("warehouseStoreId",warehouseStoreId);
        data.put("isForCps",false);
        data.put("isSync",true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.getTokenValue());
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
        orgOp.ifPresent(organization -> headers.set("orgId", organization.getCpsVendorRegistrationId().toString()));
        HttpEntity<Map<String,Object>> payload = new HttpEntity<>(data,headers);
        String url = cpsServerConfig.getItemCategoriesEndpoint().concat("/bulk");
        networkService.post(url,payload,Void.class);
    }
}
