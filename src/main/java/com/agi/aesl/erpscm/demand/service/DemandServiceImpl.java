package com.agi.aesl.erpscm.demand.service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.demand.dto.request.*;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.service.CategoryService;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.dto.response.DemandDetailItemResDto;
import com.agi.aesl.erpscm.demand.dto.response.DemandDetailResDto;
import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.entity.DemandDetailAttribute;
import com.agi.aesl.erpscm.demand.entity.DemandVerificationApprovalHistory;
import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import com.agi.aesl.erpscm.demand.repository.DemandDetailAttributeRepository;
import com.agi.aesl.erpscm.demand.repository.DemandDetailRepository;
import com.agi.aesl.erpscm.demand.repository.DemandRepository;
import com.agi.aesl.erpscm.demand.repository.DemandVerificationApprovalHistoryRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.employee.service.EmployeeService;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.dto.response.ItemDetail;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.enums.StockType;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository.VerificationResponse;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandServiceImpl implements DemandService{


    private final ClaimResolver claimResolver;


    private final DemandRepository demandRepository;


    private final DemandDetailRepository demandDetailRepository;


    private final DemandDetailAttributeRepository demandDetailAttributeRepository;


    private final DemandVerificationApprovalHistoryRepository dvahistoryRepository;

    

    private final EmployeeService userService;


    private final ItemService itemService;


    private final CommentService commentService;


    private final IntegrationReaderService integrationReaderService;


    private final UserApplicationValidatorService<Demand> verificationService;


    private final CategoryBrandRepository categoryBrandRepository;


    private final DemandMailService demandMailService;


    private final ModuleService moduleService;


    private final NetworkService networkService;


    private final CpsServerConfig cpsServerConfig;


    private final OrgService orgService;


    private final CategoryService categoryService;

    private static final String DATE_TIME_START="T00:00:00";
    private static final String DATE_TIME_END="T23:59:59";
    private static final String CRITERIA_GROUP="CATEGORY";
    private static final String FILTER_CATEGORY_KEY="category_id";
    private static final String FILTER_WAREHOUSE_KEY="warehouse_id";
    private static final String MAIL_INITIAL="Approval";
    private static final String PENDING_URI="demand/pending";
    private static final String PENDING_DEMAND_APPROVAL_REQUEST="Pending Demand Approval Request";


    private static final String ERR_DEMAND_NOT_FOUND="Demand not found";

    @Override
    @Transactional
    public void closeDemandItem(Jwt token, DemandReceiveDto demandReceiveDto) {

        claimResolver.setToken(token);

        Optional<DemandDetail> demandDetailOp = demandDetailRepository
                .findById(demandReceiveDto.getDemandDetailId());

        if(demandDetailOp.isPresent()){
            DemandDetail demandDetail = demandDetailOp.get();
            Demand demand = demandDetail.getDemand();
            int detailCount=0;
            List<DemandDetail> details = demand.getDemandDetails();
            if(!demand.getDemandDetails().isEmpty()){
                for(DemandDetail _dd : details){
                    if(_dd.getApprovedQuantity()!=null && _dd.getStatus().equals(DemandStatus.DECLINED)){
                        ++detailCount;
                    }
                }
            }
            if(demandReceiveDto.getNote() !=null && !demandReceiveDto.getNote().isEmpty()){
                claimResolver.getEmployee().ifPresent(emp->
                commentService.addComment(commentService.prepareComment(emp,
                        DomainType.DEMAND, demandDetail.getId(), demandReceiveDto.getNote(),
                        demandReceiveDto.getAttachments()
                        ))
                );
            }
            demandDetail.setStatus(DemandStatus.CLOSED_BY_STORE);
            if(details.size()==detailCount) {
                demand.setStatus(DemandStatus.CLOSED_BY_STORE);
            }
        }
        
    }

    private void setVerifiers(Demand demand, List<VerifierInfo> verifiers) {
        if(!verifiers.isEmpty()){
            Optional<VerifierInfo> firstOp = verifiers.stream().findFirst();
            VerifierInfo nextVerifier = firstOp.get();

            demandMailService.prepareMailContent(nextVerifier.getName(), "Verification", demand);
            demandMailService.sentMail(nextVerifier.getEmail(),"Pending Demand Verification Request");

            List<UserApplicationValidation> verifications = verifiers.stream().map(verifier -> {
                UserApplicationValidation verification = new UserApplicationValidation();
                verification.setDomainId(demand.getId());
                verification.setDomainType(DomainType.DEMAND);
                verification.setVerified(false);
                verification.setIsApproval(false);
                verification.setVerifier(new Employee(verifier.getId()));
                return verification;
            }).toList();
            demand.setNextVerifierId(nextVerifier.getId());
            verificationService.addVerification(verifications);
        }
    }

    private void setApprovers(Demand demand, List<ApprovalPanel> approvalPanels) {
        if(!approvalPanels.isEmpty()){
            List<UserApplicationValidation> verifications = approvalPanels.stream().map(approvalPanel -> {
                UserApplicationValidation verification = new UserApplicationValidation();
                verification.setDomainId(demand.getId());
                verification.setDomainType(DomainType.DEMAND);
                verification.setVerified(false);
                verification.setIsApproval(true);
                verification.setVerifier(new Employee(approvalPanel.getUserId()));
                return verification;
            }).toList();
            
            verificationService.addVerification(verifications);
        }
    }

    @Override
    @Transactional
    public Optional<DemandDetailResDto> createDemand(Jwt token, String uri, DemandRequestDto demandRequestDto) {
        
        
        claimResolver.setToken(token);
        Optional<Employee> employeeOp = claimResolver.getEmployee();
        if(employeeOp.isEmpty()){
            throw new AesException("Sorry! Employee not found");
        }
        
        Optional<VerifierConfig> verifierOp = verificationService.prepareLogicForVerifiers(claimResolver,uri,CRITERIA_GROUP,demandRequestDto.getCategories());
        
        Demand demand = demandRequestDto.getEntity();

        List<VerifierInfo> verifiers = getVerifiers(demand, verifierOp);

        demand.setDemandDate(LocalDateTime.now());

        if(demandRequestDto.getDeliveryDate()!=null){

            demand.setDeliveryDate(LocalDate.parse(demandRequestDto.getDeliveryDate()));
        }

        if(demandRequestDto.getCategory()!=null) {
            demand.setCategory(new ItemCategory(demandRequestDto.getCategory().getId()));
        }
        if(demandRequestDto.getSubCategory()!=null) {
            demand.setSubCategory(new ItemCategory(demandRequestDto.getSubCategory().getId()));
        }
        if(demandRequestDto.getRequestedBy()!=null) {
            demand.setRequestedBy(employeeOp.get());
        }
        claimResolver.getEmployee().ifPresent( emp->
            demand.setWarehouse(new Warehouse(emp.getWarehouseId()))
        );
        List<PendingAttributeDto> pendingAttributes = new ArrayList<>();
        setDemandDetail(demandRequestDto, demand,pendingAttributes);
        demand.setDemandNo(getNextDemandNo());
        demandRepository.save(demand);
        setVerifiers(demand, verifiers);
        List<ApprovalPanel> panels = getApprovalPanels(claimResolver, uri, demandRequestDto.getCategories());
        if(verifiers.isEmpty() && !panels.isEmpty()){
            demand.setStatus(DemandStatus.PENDING_APPROVAL);
            Optional<ApprovalPanel> firstPanel = panels.stream().findFirst();
            if(firstPanel.isPresent()){
                ApprovalPanel panel = firstPanel.get();
                demandMailService.prepareMailContent(panel.getName(), MAIL_INITIAL, demand);
                demandMailService.sentMail(panel.getEmail(),PENDING_DEMAND_APPROVAL_REQUEST);
                demand.setNextApproverId(panel.getUserId());
            }
        }
        setApprovers(demand, panels);
        if(!pendingAttributes.isEmpty()) {
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
            if (orgOp.isPresent()) {
                HttpHeaders httpHeaders = networkService.setHttpHeaders(orgOp.get());
                PendingAttributeRemoteDto pendingAttributeRemoteDto = new PendingAttributeRemoteDto(pendingAttributes);
                HttpEntity<PendingAttributeRemoteDto> payload = new HttpEntity<>(pendingAttributeRemoteDto, httpHeaders);
                networkService.post(cpsServerConfig.getPendingItemReqEndpoint() + "/pending-attributes", payload, Void.class);
            }
        }

        return this.getDemandDetail(demand.getId());
    }

    @Transactional
    private void setDemandDetail(DemandRequestDto demandRequestDto, Demand demand, List<PendingAttributeDto> pendingAttributes) {
        demand.setDemandDetails(demandRequestDto.getDemandDetails().stream().map(demandDetailDto -> {
            DemandDetail demandDetail = new DemandDetail();
            if(demandDetailDto.getId()!=null){
                demandDetail.setId(demandDetailDto.getId());
            }
            if(demandDetailDto.getItem()!=null && demandDetailDto.getItem().getId()!=null) {
                demandDetail.setItem(new Item(demandDetailDto.getItem().getId()));
            }
            Optional<ItemCategory> catOp = categoryService.getItemCategory(demandDetailDto.getSubCategory().getId());
            if(demandDetailDto.getSubCategory()!=null && catOp.isPresent()) {
                demandDetail.setItemCategory(catOp.get());
            }
            if(demandDetailDto.getCategory()!=null) {
                demandDetail.setItemParentCategory(new ItemCategory(demandDetailDto.getCategory().getId()));
            }

            if(demandDetailDto.getBrand()!=null && demandDetailDto.getBrand().getId()!=null){
                Optional<CategoryBrand> catBrandOp = categoryBrandRepository.findById(demandDetailDto.getBrand().getId());
                if(catBrandOp.isPresent()){
                    demandDetail.setBrand(catBrandOp.get());
                }

            }

            demandDetail.setAttributes(demandDetailDto.getAttributes()
                    .stream().map(demandDetailAttribute -> {
                        if(Boolean.TRUE.equals(demandDetailAttribute.getIsCustom())) {
                            PendingAttributeDto pendingAttributeDto = new PendingAttributeDto();
                            if(catOp.isPresent()) {
                                ItemCategory categoryOp = catOp.get();
                                pendingAttributeDto.setSubCategory(new ReferenceObjectDto(categoryOp.getCpsCategoryId()));
                            }
                            pendingAttributeDto.setAttributeType(demandDetailAttribute.getAttributeType());
                            pendingAttributeDto.setAttributeUnit(demandDetailAttribute.getAttributeUnit());
                            pendingAttributeDto.setAttributeValue(demandDetailAttribute.getAttributeValue());
                            pendingAttributes.add(pendingAttributeDto);
                        }
                        demandDetailAttribute.setDemandDetail(demandDetail);
                        return demandDetailAttribute;
                    }).toList());

            if(demandDetailDto.getItemUnit()!=null){
                demandDetail.setItemUnit(demandDetailDto.getItemUnit());
            }
            demandDetail.setRequestQuantity(demandDetailDto.getRequestQuantity());
            demandDetail.setDemand(demand);
            demandDetail.setPriority(demandDetailDto.getPriority());
            demandDetail.setSpecification(demandDetailDto.getSpecification());
            demandDetail.setStatus(demand.getStatus());
            return demandDetail;
        }).toList());
    }

    private List<ApprovalPanel> getApprovalPanels(ClaimResolver claimResolver,String uri, String categories) {
        return moduleService.getModuleWiseApprovalSetting(claimResolver,uri,
                Optional.ofNullable(categories),Optional.empty());
    }

    private List<VerifierInfo> getVerifiers(Demand demand, Optional<VerifierConfig> verifierOp) {
        List<VerifierInfo> verifiers = new ArrayList<>();
        if(verifierOp.isPresent()){
            VerifierConfig verification = verifierOp.get();
            verifiers = verification.getVerifiers();
            Boolean verificationRequired = verification.getVerificationRequired();
            if(Boolean.TRUE.equals(verificationRequired) && !verifiers.isEmpty()){
                demand.setStatus(DemandStatus.PENDING_VERIFICATION);
            }else{
                demand.setStatus(DemandStatus.PENDING);
            }

        }else{
            demand.setStatus(DemandStatus.PENDING);
        }
        return verifiers;
    }

    @Override
    @Transactional
    public void declineDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto) {
        Optional<Demand> demandOptional = demandRepository.findById(demandReceiveDto.getDemandId());
        if(demandOptional.isEmpty()){
            throw new AesException(ERR_DEMAND_NOT_FOUND);
        }

        if(demandReceiveDto.getNote() == null || demandReceiveDto.getNote().isEmpty()){
            throw new AesException("Note Required");
        }

        Demand demand = demandOptional.get();

        demand.setDemandDetails(demand.getDemandDetails().stream().map(demandDetail -> {
            Long demandDetailId = demandDetail.getId();
            if(demandDetailId.equals(demandReceiveDto.getDemandDetailId())) {

                if (demandReceiveDto.getNote() != null && !demandReceiveDto.getNote().isEmpty()) {
                    demandDetail.setDeclineNote(demandReceiveDto.getNote());
                }

                demandDetail.setReceivedByUserDate(LocalDateTime.now());
                demandDetail.setStatus(DemandStatus.DECLINED);
            }
            return  demandDetail;
        }).toList());
        demand.setStatus(DemandStatus.PENDING);
    }

    @Override
    public Page<DemandRepository.DemandListInfo> getAllCloseDemands(Jwt loggedInUser, Optional<Integer> page, Optional<Integer> size,
                                                                    Optional<String> demandNo, Optional<Long> categoryId,
                                                                    Optional<String> fromDateStr, Optional<String> toDateStr) {

        Optional<Employee> userOp = userService.getUserById(loggedInUser.getSubject());
        if(userOp.isEmpty()){
            throw new AesException("Sorry! User not found");
        }

        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+DATE_TIME_START);
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+DATE_TIME_END);
        }

        


        return demandRepository.findAllCloseDemands(userOp.get().getWarehouseId(),
                demandNo.orElse(null),
                categoryId.orElse(null),
                fromDate,toDate,
                pageable);
    }

    @Override
    public Page<DemandRepository.DemandListInfo> getAllDemands(Jwt loggedInUser, Optional<Integer> page, Optional<Integer> size,
                                                               Optional<String> demandNo, Optional<Long> categoryId,
                                                               Optional<String> fromDateStr, Optional<String> toDateStr, Optional<Integer> daysRemain) {
                claimResolver.setToken(loggedInUser);
                if(claimResolver.getEmployee().isEmpty()){
                    throw new AesException("Sorry! Only Store Personnel can access this");
                }
                Long warehouseId = claimResolver.getEmployee().get().getWarehouseId();


                // get filter options according to module permission
                Sort sort = Sort.by(Sort.Direction.ASC,"id");
                Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        
                Optional<Map<String,List<Long>>> modulePermission = integrationReaderService
                                        .getModuleFilterByUri(loggedInUser, PENDING_URI);

                List<Long> categoryIds = new ArrayList<>();
        
        
                LocalDateTime fromDate = null;
                LocalDateTime toDate = null;
                if(fromDateStr.isPresent()){
                    fromDate = LocalDateTime.parse(fromDateStr.get()+DATE_TIME_START);
                }
                if(toDateStr.isPresent()){
                    toDate = LocalDateTime.parse(toDateStr.get()+DATE_TIME_END);
                }

                if(categoryId.isPresent()){
                    categoryIds.add(categoryId.get());
                }
        
                if(modulePermission.isPresent()){
                    categoryIds = modulePermission.get().get(FILTER_CATEGORY_KEY);
                    List<Long> warehouseIds = modulePermission.get().get(FILTER_WAREHOUSE_KEY);
                    return demandRepository.findAllDemandsByCategory(categoryIds,
                            (!warehouseIds.isEmpty())? warehouseIds : List.of(warehouseId),
                            demandNo.orElse(null),
                            fromDate,toDate,daysRemain.orElse(null),
                            pageable);
                }
        

                return demandRepository.findAllDemands(warehouseId,demandNo.orElse(null),
                        categoryId.orElse(null),
                        fromDate,toDate, daysRemain.orElse(null),pageable);
    }

    @Override
    public Page<DemandRepository.DemandPendingVerificationApprovalList> getAllPendingApprovalDemands(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                                                     Optional<String> demandNo, Optional<Long> categoryId,
                                                                                                     Optional<String> fromDateStr, Optional<String> toDateStr) {
                claimResolver.setToken(token);
        
                String moduleUri = "demand/pending-approval";
                Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10));
                Optional<Map<String,List<Long>>> modulePermission = integrationReaderService
                        .getModuleFilterByUri(token, moduleUri);
        
                LocalDateTime fromDate = null;
                LocalDateTime toDate = null;
                if(fromDateStr.isPresent()){
                    fromDate = LocalDateTime.parse(fromDateStr.get()+DATE_TIME_START);
                }
                if(toDateStr.isPresent()){
                    toDate = LocalDateTime.parse(toDateStr.get()+DATE_TIME_END);
                }
        
                List<String> demandStatuses = new ArrayList<>();
                demandStatuses.add(DemandStatus.PENDING_APPROVAL.toString());
                demandStatuses.add(DemandStatus.REVIEW.toString());

                if(modulePermission.isPresent()){
                    List<Long> categoryIds = modulePermission.get().get(FILTER_CATEGORY_KEY);
                    if(categoryIds!=null && categoryId.isPresent()){
                        categoryIds.add(categoryId.get());
                    }else if (categoryIds==null && categoryId.isPresent()){
                        categoryIds = new ArrayList<>();
                        categoryIds.add(categoryId.get());
                    }
                    return demandRepository.findAllDemandsByCategoryAndDemandStatusAndNextApproverId(categoryIds,
                            claimResolver.getUserId(),
                            demandNo.orElse(null),
                            demandStatuses,
                            fromDate,toDate,
                            pageable);
                }
                return demandRepository.findAllDemandsByDemandStatusAndNextApproverId(
                        demandStatuses,
                        claimResolver.getUserId(),
                        demandNo.orElse(null),
                        categoryId.orElse(null),
                        fromDate,toDate,
                        pageable);
    }

    @Override
    public Page<DemandRepository.DemandPendingVerificationApprovalList> getAllPendingVerificationDemands(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                                                                         Optional<String> demandNo, Optional<Long> categoryId,
                                                                                                         Optional<String> fromDateStr, Optional<String> toDateStr) {
        claimResolver.setToken(token);
        
        String moduleUri = "demand/pending-verification";
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10));

        Optional<Map<String,List<Long>>> modulePermission = integrationReaderService
                .getModuleFilterByUri(token, moduleUri);



        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+DATE_TIME_START);
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+DATE_TIME_END);
        }
        List<String> demandStatuses = new ArrayList<>();
        demandStatuses.add(DemandStatus.PENDING_VERIFICATION.toString());
        demandStatuses.add(DemandStatus.REVIEW.toString());
        
        if(modulePermission.isPresent()){
            List<Long> categoryIds = modulePermission.get().get(FILTER_CATEGORY_KEY);
            if(categoryIds!=null && categoryId.isPresent()){
                categoryIds.add(categoryId.get());
            }else if (categoryIds==null && categoryId.isPresent()){
                categoryIds = new ArrayList<>();
                categoryIds.add(categoryId.get());
            }

            return demandRepository.findAllDemandsByCategoryAndDemandStatusAndNextVerifierId(categoryIds,
                    claimResolver.getUserId(),
                    demandNo.orElse(null),
                    demandStatuses,
                    fromDate,toDate,
                    pageable);
        }

        return demandRepository.findAllDemandsByDemandStatusAndNextVerifierId(
            demandStatuses,
                claimResolver.getUserId(),
                demandNo.orElse(null),
                categoryId.orElse(null),
                fromDate,toDate,
                pageable);
    }



    @Override
    public Optional<DemandDetailResDto> getDemandDetail(Long id) {

        List<DemandRepository.DemandDetailItem> demandList = demandRepository.findByDemandId(id);
        DemandDetailResDto resDto = DemandDetailResDto.builder().build();
        for(DemandRepository.DemandDetailItem demandDetailItem: demandList){



            List<DemandDetailAttribute> demandDetailAttrs= demandDetailAttributeRepository.findByDemandDetailId(demandDetailItem.getDemandDetailId());
            resDto.setIsCanceled(demandDetailItem.getIsCanceled());
            resDto.setDaysRemain(demandDetailItem.getDaysRemain());
            resDto.setDeliveryDate(demandDetailItem.getDeliveryDate());
            resDto.setDemandNo(demandDetailItem.getDemandNo());
            resDto.setDemandId(demandDetailItem.getDemandId());
            resDto.setDemandDate(demandDetailItem.getDemandDate());
            resDto.setDemandStatus(demandDetailItem.getDemandStatus());
            resDto.setEmpId(demandDetailItem.getEmpId());
            resDto.setEmployeeId(demandDetailItem.getEmployeeId());
            resDto.setEmployeeName(demandDetailItem.getEmployeeName());
            resDto.setReportingManager(demandDetailItem.getReportingManager());
            resDto.setDepartment(demandDetailItem.getDepartment());
            resDto.setDesignation(demandDetailItem.getDesignation());
            resDto.setWarehouseId(demandDetailItem.getWarehouseId());
            resDto.setWarehouseName(demandDetailItem.getWarehouseName());
            resDto.setWarehouseLocation(demandDetailItem.getWarehouseLocation());
            resDto.setNextApproverId(demandDetailItem.getNextApproverId());
            resDto.setNextVerifierId(demandDetailItem.getNextVerifierId());
            resDto.setReviewerId(demandDetailItem.getReviewerId());
            resDto.addDetail(
                
                DemandDetailItemResDto.builder()
                .itemId(demandDetailItem.getId())
                .attributes(demandDetailAttrs)
                .itemCategoryId(demandDetailItem.getItemCategoryId())
                .itemParentCategoryId(demandDetailItem.getItemParentCategoryId())
                .category(demandDetailItem.getCategory())
                .categoryCode(demandDetailItem.getCategoryCode())
                .parentCategory(demandDetailItem.getParentCategory())
                .parentCategoryCode(demandDetailItem.getParentCategoryCode())
                .name(demandDetailItem.getName())
                .code(demandDetailItem.getCode())
                .attributeTypes(demandDetailItem.getAttributeTypes())
                .attributeValues(demandDetailItem.getAttributeValues())
                .brandId(demandDetailItem.getBrandId())
                .brandName(demandDetailItem.getBrandName())
                .demandDetailId(demandDetailItem.getDemandDetailId())
                .prQty(demandDetailItem.getPrQty())
                .openPrQty(demandDetailItem.getOpenPrQty())
                .receivedNote(demandDetailItem.getReceiveNote())
                .storeNote(demandDetailItem.getStoreNote())
                .declineNote(demandDetailItem.getDeclineNote())
                .demandItemStatus(demandDetailItem.getDemandDetailStatus())
                .requestedQuantity(demandDetailItem.getRequestQuantity())
                .stockThresholdQty(demandDetailItem.getStockThresholdQty())
                .specification(demandDetailItem.getSpecification())
                .currentStock(demandDetailItem.getCurrentStockQty())
                .approvedQuantity(demandDetailItem.getApprovedQuantity())
                .demandPriority(demandDetailItem.getDemandPriority())
                .itemUnit(demandDetailItem.getItemUnit())
                        .inTransit(demandDetailItem.getInTransit())
                .build()
            );

        }

        if(resDto.getDemandId()!=null) {
            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.DEMAND, resDto.getDemandId())
                    .forEach(verifier->{
                        if(Boolean.FALSE.equals(verifier.getIsApproval())){
                            verifiers.add(verifier);
                        }else{
                            approvers.add(verifier);
                        }
                    });
            resDto.setVerifiers(verifiers);
            resDto.setApprovers(approvers);
        }
        List<?> comments = commentService.getCommentsByDomain(DomainType.DEMAND, resDto.getDemandId());
        resDto.setComments(comments);
        return Optional.ofNullable(resDto);
    }

    @Override
    public Page<DemandRepository.DemandListInfo> getMyDemands(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                              Optional<String> demandNo, Optional<Long> categoryId,
                                                              Optional<String> fromDateStr, Optional<String> toDateStr) {
        claimResolver.setToken(token);

       Optional<Employee> employeeOptional = userService.getUserById(claimResolver.getUserId());

        if(employeeOptional.isEmpty()){
            throw new AesException("No Employee Profile Found");
        }

        Sort sort = Sort.by(Sort.Direction.DESC,"reviewDate","demandDate");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+DATE_TIME_START);
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+DATE_TIME_END);
        }


        return demandRepository.findAllByRequestedById(claimResolver.getUserId(),
                demandNo.orElse(null),categoryId.orElse(null),
                fromDate,toDate,pageable);
    }

    @Override
    public String getNextDemandNo() {
        Optional<Long> demandOptional = demandRepository.findMaxOrderById();
        if(demandOptional.isPresent()){
            Long demandNo = demandOptional.get();
            Long newDemandNo = demandNo + 1;
            return String.format("%05d",newDemandNo);
        }
        return String.format("%05d",1);
    }

    @Override
    @Transactional
    public void receiveDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto) {
        Optional<Demand> demandOptional = demandRepository.findById(demandReceiveDto.getDemandId());
        if(demandOptional.isEmpty()){
            throw new AesException(ERR_DEMAND_NOT_FOUND);
        }

        Demand demand = demandOptional.get();

        Integer completeCount  = demandDetailRepository.countByStatusAndDemandId(DemandStatus.RECEIVED,demand.getId());
        AtomicReference<Integer> demandCount = new AtomicReference<>(demand.getDemandDetails().size());

        demand.setDemandDetails(demand.getDemandDetails().stream().map(demandDetail -> {
            Long demandDetailId = demandDetail.getId();
            if(demandDetailId.equals(demandReceiveDto.getDemandDetailId())) {
                Item item = demandDetail.getItem();
                List<Map<String,Object>> stocks = null;
                Long warehouseStoreId = null;
                Optional<ItemDetail> itemDetailOp = (Optional<ItemDetail>) itemService.getItemDetailWithWarehouse(item.getId());
                if(itemDetailOp.isPresent()){
                    ItemDetail itemDetail = itemDetailOp.get();
                    stocks = itemDetail.getWarehouses().get(demandReceiveDto.getWarehouseId().toString());
                    if(!stocks.isEmpty()){
                        Map<String,Object> lastStock = stocks.get(0);
                        warehouseStoreId = (Long)lastStock.get("warehouseStoreId");
                    }
                }


                if(demandReceiveDto.getQty()!=null && (demandDetail.getApprovedQuantity().compareTo(demandReceiveDto.getQty()) >= 0)) {
                    itemService.stockOut(item, demandReceiveDto.getQty(),demandReceiveDto.getWarehouseId(),
                    warehouseStoreId);
                }else{
                    itemService.stockOut(item, demandDetail.getApprovedQuantity(),demandReceiveDto.getWarehouseId(),
                    warehouseStoreId);
                }
                if(demandReceiveDto.getNote()!=null && !demandReceiveDto.getNote().isEmpty()){
                    demandDetail.setReceiveNote(demandReceiveDto.getNote());
                }

                demandDetail.setReceivedByUserDate(LocalDateTime.now());

                setRemainingQtyDetail(demand,demandDetail);
                if(demandDetail.getStatus().equals(DemandStatus.PENDING_QC) &&
                        demandDetail.getApprovedQuantity().compareTo(demandDetail.getRequestQuantity())<0){
                    demandCount.set(demandCount.get() + 1);
                }
                demandDetail.setStatus(DemandStatus.RECEIVED);
            }
           return demandDetail;
        }).toList());

        if((demandCount.get() -completeCount) == 1){
            demand.setStatus(DemandStatus.RECEIVED);
        }
        
    }

    @Transactional
    private void setRemainingQtyDetail(Demand demand, DemandDetail demandDetail){
        if(demandDetail.getStatus().equals(DemandStatus.PENDING_QC) &&
            demandDetail.getApprovedQuantity().compareTo(demandDetail.getRequestQuantity())<0){
            BigDecimal remainingQty = demandDetail.getRequestQuantity().subtract(demandDetail.getApprovedQuantity());
            DemandDetail demandDetailNew = new DemandDetail();
            demandDetailNew.setDemand(demand);
            demand.setStatus(DemandStatus.PARTIAL);
            demandDetailNew.setRequestQuantity(remainingQty);
            demandDetailNew.setItem(demandDetail.getItem());
            demandDetailNew.setBrand(demandDetail.getBrand());

            if(!demandDetail.getAttributes().isEmpty()) {
                demandDetailNew.setAttributes(demandDetail.getAttributes().stream().map(attr -> {
                    DemandDetailAttribute dda = new DemandDetailAttribute();
                    dda.setDemandDetail(demandDetailNew);
                    dda.setAttributeType(attr.getAttributeType());
                    dda.setAttributeValue(attr.getAttributeValue());
                    dda.setAttributeUnit(attr.getAttributeUnit());
                    return dda;
                }).toList());
            }
            demandDetailNew.setItemCategory(demandDetail.getItemCategory());
            demandDetailNew.setItemParentCategory(demandDetail.getItemParentCategory());
            demandDetailNew.setStatus(DemandStatus.PENDING);
            demandDetailNew.setPriority(demandDetail.getPriority());
            demandDetailNew.setSpecification(demandDetail.getSpecification());
            demandDetailRepository.save(demandDetailNew);
        }
    }
    @Override
    @Transactional
    public void rejectDemand(Jwt token, DemandReceiveDto demandReceiveDto) {
        claimResolver.setToken(token);
        Optional<Demand> demandOp = demandRepository.findById(demandReceiveDto.getDemandId());
        if(demandOp.isPresent()){
            Demand demand = demandOp.get();

            if(demandReceiveDto.getNote()!=null && !demandReceiveDto.getNote().isEmpty()){
                claimResolver.getEmployee().ifPresent(emp->
                commentService.addComment(commentService.prepareComment(emp,
                        DomainType.DEMAND, demand.getId(), demandReceiveDto.getNote(),
                        demandReceiveDto.getAttachments()
                        ))
                );
            }
            demand.setStatus(DemandStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void rejectDemandItem(Jwt token, DemandReceiveDto demandReceiveDto) {
        claimResolver.setToken(token);
        Optional<DemandDetail> demandDetailOp = demandDetailRepository
                                        .findById(demandReceiveDto.getDemandDetailId());

        if(demandDetailOp.isPresent()){
            DemandDetail demandDetail = demandDetailOp.get();

            itemService.stockUpdateByDemand(demandReceiveDto.getWarehouseId(), demandDetail, StockType.STOCK_IN);

            if(demandReceiveDto.getNote()!=null && !demandReceiveDto.getNote().isEmpty()){
                claimResolver.getEmployee().ifPresent(emp->
                commentService.addComment(commentService.prepareComment(emp,
                        DomainType.DEMAND, demandDetail.getId(), demandReceiveDto.getNote(),
                        demandReceiveDto.getAttachments()))
                );
            }
            demandDetail.setStatus(DemandStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void resendDemandItem(Jwt token, DemandReceiveDto demandReceiveDto) {

        claimResolver.setToken(token);
        Optional<DemandDetail> demandDetailOp = demandDetailRepository
                .findById(demandReceiveDto.getDemandDetailId());

        if(demandDetailOp.isPresent()){
            DemandDetail demandDetail = demandDetailOp.get();
            if(demandReceiveDto.getNote()!=null && !demandReceiveDto.getNote().isEmpty()){

                claimResolver.getEmployee().ifPresent(emp->
                commentService.addComment(commentService.prepareComment(emp,
                        DomainType.DEMAND, demandDetail.getId(), demandReceiveDto.getNote(),
                        demandReceiveDto.getAttachments()
                        ))
                );
            }
            demandDetail.setStatus(DemandStatus.PENDING_QC);
            demandDetail.getDemand().setStatus(DemandStatus.PENDING_QC);
        }
        
    }

    @Override
    public void onRejected(Employee verifier, Long id, RejectDto rejectDto) {
        log.info("Rejected");
    }

    @Override
    @Transactional
    public void reviewDemand(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        Optional<Demand> demandOp = demandRepository.findById(id);
        if(demandOp.isEmpty()){
            throw new AesException(ERR_DEMAND_NOT_FOUND);
        }
        Demand demand = demandOp.get();
        demand.setReviewerId(null);
        if(demand.getReviewPrevStatus()!=null) {
            demand.setStatus(demand.getReviewPrevStatus());
        }
        demand.setReviewPrevStatus(null);
        demand.setReviewDate(LocalDateTime.now());

        claimResolver.getEmployee().ifPresent(emp->commentService.addComment(commentService.prepareComment(
                emp,
                reviewDto.getDomainType(),
                demand.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        )));

    }

    @Override
    @Transactional
    public void sentDemandItem(Jwt token, DemandReceiveDto demandReceiveDto) {
        Optional<Demand> demandOptional = demandRepository.findById(demandReceiveDto.getDemandId());
        if(demandOptional.isEmpty()){
            throw new AesException(ERR_DEMAND_NOT_FOUND);
        }
        Demand demand = demandOptional.get();
        Integer pendingQcCount  = demandDetailRepository.countByStatusAndDemandId(DemandStatus.PENDING_QC,demand.getId());


        demandMailService.prepareMailContentForInitiator(demand.getRequestedBy().getEmployeeName(),null,demand);
        demandMailService.sentMail(demand.getRequestedBy().getEmailAddress(),"Pending RN");



        Integer demandCount = demand.getDemandDetails().size();

        demand.setDemandDetails(demand.getDemandDetails().stream().map(demandDetail -> {
            Long demandDetailId = demandDetail.getId();
            if(demandDetailId.equals(demandReceiveDto.getDemandDetailId())) {
                demandDetail.setApprovedQuantity(demandReceiveDto.getQty());
                demandDetail.setStatus(DemandStatus.PENDING_QC);
                Item item = demandDetail.getItem();
                if(item == null) {
                    Optional<Item> itemOp = itemService.getItemDetail(demandReceiveDto.getItemId());
                    if(itemOp.isPresent()){
                        item = itemOp.get();
                    }
                }

                if(item ==  null){
                    throw new AesException("Please select a product");
                }
                if(item!=null) {
                    
                    demandDetail.setItem(item);
                    if(demandReceiveDto.getNote()!=null && !demandReceiveDto.getNote().isEmpty()){
                        demandDetail.setStoreNote(demandReceiveDto.getNote());
                    }

                    demandDetail.setSendToUserDate(LocalDateTime.now());
                }
            }
            return demandDetail;
        }).toList());

        if((demandCount-pendingQcCount)>1){
            demand.setStatus(DemandStatus.PARTIAL);
        }else {
            demand.setStatus(DemandStatus.PENDING_QC);
        }
        
    }

    @Override
    @Transactional
    public Optional<DemandDetailResDto> updateDemand(Jwt loggedInUser, String uri, Long id,
            DemandRequestDto demandRequestDto) {
            claimResolver.setToken(loggedInUser);
            if(id==null){
                throw new AesException("Sorry! demand id is missing");
            }
            Optional<Demand> demandOp = demandRepository.findById(id);
            if(demandOp.isEmpty()){
                throw new AesException("Sorry! demand not found for update");
            }
            Demand demand = demandOp.get();

            if(demandRequestDto.getDeliveryDate()!=null){

                demand.setDeliveryDate(LocalDate.parse(demandRequestDto.getDeliveryDate()));
            }
            setDemandDetail(demandRequestDto, demand, new ArrayList<>());
            demand.setReviewerId(null);
            demandRepository.save(demand);
            // remove all previous verification and approval request
            verificationService.removeVerification(demand.getId(), DomainType.DEMAND);
            dvahistoryRepository.deleteAllByDemandId(demand.getId());
            Optional<VerifierConfig> verifierOp = verificationService
                .prepareLogicForVerifiers(claimResolver,uri,CRITERIA_GROUP,demandRequestDto.getCategories());

            setVerifiers(demand, getVerifiers(demand, verifierOp));
            setApprovers(demand, getApprovalPanels(claimResolver,uri, demandRequestDto.getCategories()));

        return this.getDemandDetail(demand.getId());
    }

    @Override
    @Transactional
    public void cancelDemand(Jwt token, Long id, String uri, String categories, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<Demand> demandOp = demandRepository.findById(id);
        if(demandOp.isEmpty()){
            throw new AesException("Sorry! Demand not found");
        }

        Demand demand = demandOp.get();
        demand.setIsCanceled(true);
        verificationService.removeVerification(demand.getId(), DomainType.DEMAND);
        dvahistoryRepository.deleteAllByDemandId(demand.getId());
        Optional<VerifierConfig> verifierOp = verificationService
                .prepareLogicForVerifiers(claimResolver,uri,CRITERIA_GROUP,categories);

        setVerifiers(demand, getVerifiers(demand, verifierOp));
        setApprovers(demand, getApprovalPanels(claimResolver,uri, categories));

        claimResolver.getEmployee().ifPresent(emp->
        commentService.addComment(commentService.prepareComment(emp,DomainType.DEMAND,demand.getId(),noteDto.getNote(),
                noteDto.getAttachments()))
        );
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<Demand> demandOp  = demandRepository.findById(id);
        if(demandOp.isPresent()){
            Demand demand = demandOp.get();
            if(demand.getIsCanceled()!=null && demand.getIsCanceled()) {
                demand.setStatus(DemandStatus.CANCELED);
            }else{
                demandMailService.setClaimResolver(claimResolver);
                demandMailService.setDemand(demand);
                demandMailService.getStoreUsers(PENDING_URI);
                demandMailService.sentMail(null, "Pending Demand");

                demand.setStatus(DemandStatus.PENDING);
                demand.setDemandDetails(
                        demand.getDemandDetails().stream().map(demandDetail -> {
                            demandDetail.setStatus(DemandStatus.PENDING);
                            return demandDetail;
                        }).toList());
            }
            DemandVerificationApprovalHistory demandVAHistory = new DemandVerificationApprovalHistory();
            demandVAHistory.setDemand(demand);
            demandVAHistory.setEmployee(new Employee(demand.getNextApproverId()));
            demandVAHistory.setDemandStatus(DemandStatus.APPROVED);
            dvahistoryRepository.save(demandVAHistory);
        }
        
    }



    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, VerificationResponse verificationResponse) {
        Optional<Demand> demandOp  = demandRepository.findById(id);
        if(demandOp.isPresent()){
            Demand demand = demandOp.get();
            demandMailService.prepareMailContent(verificationResponse.getVerifier().getEmployeeName(),MAIL_INITIAL,demand);
            demandMailService.sentMail(verificationResponse.getVerifier().getEmailAddress(),PENDING_DEMAND_APPROVAL_REQUEST);

            DemandVerificationApprovalHistory demandVAHistory = new DemandVerificationApprovalHistory();
            demandVAHistory.setDemand(demand);
            demandVAHistory.setEmployee(verification.getVerifier());
            demandVAHistory.setDemandStatus(DemandStatus.APPROVED);
            dvahistoryRepository.save(demandVAHistory);
            demand.setNextApproverId(verificationResponse.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, VerificationResponse nextVerifier) {
        Optional<Demand> demandOp  = demandRepository.findById(id);
        if(demandOp.isPresent()){
            Demand demand = demandOp.get();

            demandMailService.prepareMailContent(nextVerifier.getVerifier().getEmployeeName(),"Verification",demand);
            demandMailService.sentMail(nextVerifier.getVerifier().getEmailAddress(),"Pending Demand Verification Request");

            DemandVerificationApprovalHistory demandVAHistory = new DemandVerificationApprovalHistory();
            demandVAHistory.setDemand(demand);
            demandVAHistory.setEmployee(verification.getVerifier());
            demandVAHistory.setDemandStatus(DemandStatus.VERIFIED);
            dvahistoryRepository.save(demandVAHistory);
            demand.setNextVerifierId(nextVerifier.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long id, RefDto reviewer, String comment) {
        Optional<Demand> demandOp  = demandRepository.findById(id);
        if(demandOp.isPresent()){
            Demand demand = demandOp.get();
            if(!demand.getStatus().equals(DemandStatus.REVIEW)){
                demand.setReviewPrevStatus(demand.getStatus());
                demand.setStatus(DemandStatus.REVIEW);
            }
            demand.setReviewerId(reviewer.getId());
            demand.setReviewDate(LocalDateTime.now());
        }
        
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<VerificationResponse> firstApprover) {
        Optional<Demand> demandOp  = demandRepository.findById(id);
        if(demandOp.isPresent()){
            Demand demand = demandOp.get();

            DemandVerificationApprovalHistory demandVAHistory = new DemandVerificationApprovalHistory();
            demandVAHistory.setDemand(demand);
            demandVAHistory.setEmployee(new Employee(demand.getNextVerifierId()));
            demandVAHistory.setDemandStatus(DemandStatus.VERIFIED);
            dvahistoryRepository.save(demandVAHistory);

            if(firstApprover.isPresent()){
                demandMailService.prepareMailContent(firstApprover.get().getVerifier().getEmployeeName(),MAIL_INITIAL,demand);
                demandMailService.sentMail(firstApprover.get().getVerifier().getEmailAddress(),PENDING_DEMAND_APPROVAL_REQUEST);

                demand.setNextApproverId(firstApprover.get().getVerifier().getId());
                demand.setStatus(DemandStatus.PENDING_APPROVAL);
                demand.setDemandDetails(
                        demand.getDemandDetails().stream().map(demandDetail -> {
                            demandDetail.setStatus(DemandStatus.PENDING_APPROVAL);
                            return demandDetail;
                        }).toList()
                );
            }else {

                if(demand.getIsCanceled()!=null && demand.getIsCanceled()) {
                    demand.setStatus(DemandStatus.CANCELED);
                }else{
                    demandMailService.setClaimResolver(claimResolver);
                    demandMailService.setDemand(demand);
                    demandMailService.getStoreUsers(PENDING_URI);
                    demandMailService.sentMail(null, "Pending Demand");

                    demand.setStatus(DemandStatus.PENDING);
                    demand.setDemandDetails(
                            demand.getDemandDetails().stream().map(demandDetail -> {
                                demandDetail.setStatus(DemandStatus.PENDING);
                                return demandDetail;
                            }).toList()
                    );
                }
                
            }

        }
    }

    @Override
    public List<DemandRepository.SubCatStockInfo> getStockBySubCategory(Long subCategoryId, Long warehouseId) {
        return demandRepository.findStockBySubCatAndWarehouse(subCategoryId,warehouseId);
    }
}
