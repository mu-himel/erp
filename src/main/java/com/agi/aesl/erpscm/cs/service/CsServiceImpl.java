package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.cs.dto.CsRequestDto;
import com.agi.aesl.erpscm.cs.dto.CsUpdateRequestDto;
import com.agi.aesl.erpscm.cs.dto.CsVendorDetailDto;
import com.agi.aesl.erpscm.cs.entity.*;
import com.agi.aesl.erpscm.cs.enums.CsOperation;
import com.agi.aesl.erpscm.cs.enums.CsStatus;
import com.agi.aesl.erpscm.cs.repository.CsDetailRepository;
import com.agi.aesl.erpscm.cs.repository.CsRepository;
import com.agi.aesl.erpscm.cs.repository.CsVaHistoryRepository;
import com.agi.aesl.erpscm.cs.repository.CsVendorDetailRepository;
import com.agi.aesl.erpscm.demand.dto.request.PendingAttributeDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.entity.IndentDetail;
import com.agi.aesl.erpscm.indent.repository.IndentDetailRepository;
import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.inventory.dto.request.PendingItemAttributeDto;
import com.agi.aesl.erpscm.inventory.dto.request.PendingItemRequestDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.service.CategoryService;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.price_quotation.entity.PqTermsAndCondition;
import com.agi.aesl.erpscm.price_quotation.entity.PriceQuotation;
import com.agi.aesl.erpscm.price_quotation.repository.PqRepository;
import com.agi.aesl.erpscm.price_quotation.repository.PqTermAndConditionRepository;
import com.agi.aesl.erpscm.product_requirements.service.ProductRequirementService;
import com.agi.aesl.erpscm.purchase_order.entity.PoGroup;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrderDetail;
import com.agi.aesl.erpscm.purchase_order.enums.PurchaseOrderStatus;
import com.agi.aesl.erpscm.purchase_order.repository.PoGroupRepository;
import com.agi.aesl.erpscm.purchase_order.service.PurchaseOrderService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CsServiceImpl implements CsService{

    private static final Integer PAGE_SIZE = 20;

    @Autowired
    private ClaimResolver claimResolver;
    @Autowired
    private IndentRepository indentRepository;

    @Autowired
    private IndentDetailRepository indentDetailRepository;

    @Autowired
    private CsRepository csRepository;

    @Autowired
    private CsDetailRepository csDetailRepository;

    @Autowired
    private CsVaHistoryRepository csVaHistoryRepository;

    @Autowired
    private UserApplicationValidatorService<Cs> verificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProductRequirementService productRequirementService;

    @Autowired
    private PqTermAndConditionRepository pqTermAndConditionRepository;

    @Autowired
    private CsVendorDetailRepository csVendorDetailRepository;

    @Autowired
    private OrgService orgService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private CpsServerConfig cpsServerConfig;

    @Autowired
    private ItemService itemService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PoGroupRepository poGroupRepository;

    @Autowired
    private PqRepository pqRepository;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private CsAccountService csAccountService;

    private LocalDateTime parseDate(Optional<String> dateStr,String endTime){
        LocalDateTime date = null;
        if(dateStr.isPresent()){
            String time = (endTime!=null && endTime.trim().length()==8)? "T"+endTime:"T00:00:00";
            date = LocalDateTime.parse(dateStr.get()+time);
        }
        return date;
    }


    @Override
    public Page<?> getAllPendingCs(Jwt token,
                                   Optional<String> indentNo, Optional<String> status,
                                   Optional<String> fromDateStr, Optional<String> toDateStr,
                                   Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort );

        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(CsStatus.PENDING.name(),
                CsStatus.PENDING_VERIFICATION.name(),
                CsStatus.PENDING_APPROVAL.name(),
                CsStatus.REVIEW.name());
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses.add(status.get());
        }
        System.out.println(statuses);
        return indentRepository.getAllIndentsByExpireDateTime(LocalDateTime.now(),
                indentNo.orElse(null),
                statuses,
                fromDate,toDate,
                pageable);
    }

    @Override
    @Transactional
    public void createCs(Jwt token, String uri, CsRequestDto csRequestDto) {
        claimResolver.setToken(token);

        Cs cs = new Cs();

        Optional<Indent> indentOp = indentRepository.findById(csRequestDto.getIndentId());
        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! Rfq not found");
        }

        Indent indent = indentOp.get();
        cs.setIndent(indent);
        cs.setCsNo(indent.getIndentNo());
        cs.setWarehouse(indent.getWarehouse());
        cs.setTotalPrice(csRequestDto.getTotalPrice());
        cs.setDeliveryCharge(csRequestDto.getDeliveryCharge());
        cs.setSubTotalPrice(csRequestDto.getSubTotalPrice());
        cs.setVatAmount(csRequestDto.getVatAmount());
        cs.setValidityDate(csRequestDto.getValidityDate());
        Optional<Employee> empOp = claimResolver.getEmployee();
        if(empOp.isEmpty()){
            throw new RuntimeException("Sorry! requested by information missing");
        }
        Employee employee = empOp.get();
        cs.setRequestedBy(employee);
        StringBuilder categories = new StringBuilder(indent.getCategory().getId().toString());
        categories.append(",").append(indent.getSubCategory().getId().toString());

        // add details cs info here
        List<String> ids =new ArrayList<>();
        cs.setCsDetails(csRequestDto.getDetails().stream().map(v->{
            CsDetail csDetail = new CsDetail();
            csDetail.setCs(cs);
            Optional<IndentDetail> indentDetailOp = indentDetailRepository.findById(v.getIndentDetailId());
            if(indentDetailOp.isEmpty()){
                throw new RuntimeException("Sorry! Indent Detail not found");
            }
            IndentDetail indentDetail = indentDetailOp.get();
            ids.add(indentDetail.getSubCategory().getId().toString());
            ids.add(indentDetail.getSubCategory().getParentCategory().getId().toString());

            csDetail.setIndentDetail(indentDetailOp.get());
            csDetail.setVendorDetails(v.getVendors().stream().map(vendorDetail->{
                CsVendorDetail csVendorDetail = new CsVendorDetail();
                csVendorDetail.setVendorId(vendorDetail.getVendorId());
                csVendorDetail.setVatAmount(vendorDetail.getVatAmount());
                csVendorDetail.setDiscountAmount(vendorDetail.getDiscountAmount());
                csVendorDetail.setPriceQuotation(new PriceQuotation(vendorDetail.getPriceQuotation().getId()));
                csVendorDetail.setOrderQty(vendorDetail.getOrderQty());
                csVendorDetail.setTotalPrice(vendorDetail.getTotalPrice());
                csVendorDetail.setTransactionType(vendorDetail.getTransactionType());
                csVendorDetail.setCsDetail(csDetail);
                csVendorDetail.setVendorDeliveryDetails(vendorDetail.getWarehouses().stream().map(w->{
                    CsDeliveryDetail csDeliveryDetail = new CsDeliveryDetail();
                    csDeliveryDetail.setWarehouseId(w.getWarehouseId());
                    csDeliveryDetail.setDeliveryDate(w.getDeliveryDate());
                    csDeliveryDetail.setDeliveryQty(w.getDeliveryQty());
                    csDeliveryDetail.setVendorDeliveryDetail(csVendorDetail);
                    return csDeliveryDetail;
                }).collect(Collectors.toList()));
                return csVendorDetail;
            }).collect(Collectors.toList()));
            return csDetail;
        }).collect(Collectors.toList()));

        csRepository.save(cs);

        AppliedVADto verifyApproval = verificationService.applyVerifyApprovalProcess(cs, DomainType.CS, CsStatus.COMPLETED.toString(),
                uri, "CATEGORY", ids, null);


        if(verifyApproval.getVerifiers().isEmpty() && verifyApproval.getPanels().isEmpty()){
            cs.setCsStatus(CsStatus.COMPLETED);
            csAccountService.createCsAccount(cs);
        }

//        @SuppressWarnings("unchecked")
//        Optional<Map<String, Object>> verifierOp = (Optional<Map<String, Object>>) verificationService.getVerifiers(loggedInUser, uri, categories.toString());

//        List<Verifier> verifiers = new ArrayList<>();
//        if (verifierOp.isPresent()) {
//            Map<String, Object> verification = verifierOp.get();
//
//            verifiers = (List<Verifier>) verification.get("verifiers");
//
//            Boolean verificationRequired = (Boolean) verification.get("verificationRequired");
//            if (verificationRequired != null && verificationRequired == true && verifiers != null && verifiers.size() > 0) {
//                cs.setStatus(CsStatus.PENDING_VERIFICATION);
//            } else {
//                cs.setStatus(CsStatus.PENDING);
//
//            }
//
//        }else{
//            cs.setStatus(CsStatus.APPROVED);
//            generatePO(cs.getRequestedBy(),cs);
//        }
//
//        List<ApprovalSettingQuery.ApprovalPanel> approvalPanels = approvalSettingService.getModuleWiseApprovalSetting(uri,
//                Optional.ofNullable(categories.toString()),Optional.empty());
//
//        verificationService.setVerifiers(cs, verifiers, DomainType.CS);
//        verificationService.setApprovers(cs, approvalPanels, DomainType.CS);
    }

    @Override
    public Optional<?> getDetailById(Long id) {
        Optional<Cs> csOp = csRepository.findById(id);
        if(csOp.isPresent()){
            Cs cs = csOp.get();
            List<Map<String,Object>> result = new ArrayList<>();
            cs.getCsDetails().stream().forEach(csd->{
                Map<String,Object> csdMap = new HashMap<>();
                csdMap.put("indentDetailId",csd.getIndentDetail().getId());

                csdMap.put("pdDeliveries",csd.getVendorDetails().stream().map(w->{
                    Map<String,Object> wMap = new HashMap<>();
                    wMap.put("vendorId",w.getVendorId());
                    wMap.put("id",w.getId());
                    wMap.put("totalPrice",w.getTotalPrice());
                    wMap.put("orderQty",w.getOrderQty());
                    wMap.put("priceQuotationId",w.getPriceQuotation().getId());
                    wMap.put("pds",w.getVendorDeliveryDetails().stream().map(pd->{
                        Map<String,Object> pdMap = new HashMap<>();
                        pdMap.put("id",pd.getId());
                        pdMap.put("qty",pd.getDeliveryQty());
                        pdMap.put("deliveryDate", pd.getDeliveryDate());
                        return pdMap;
                    }).collect(Collectors.toList()));
                    return wMap;
                }).collect(Collectors.toList()));
                csdMap.put("csDetailId",csd.getId());

                csdMap.put("vendors",csd.getVendorDetails().stream().map(vd->{
                    return vd.getVendorId();
                }).collect(Collectors.toList()));
                result.add(csdMap);
            });

            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.CS, cs.getId());
            vrs.stream().forEach(verifier->{
                if(verifier.getIsApproval()==false){
                    verifiers.add(verifier);
                }else{
                    approvers.add(verifier);
                }
            });

            List<?> comments = commentService.getCommentsByDomain(DomainType.CS, cs.getId());

            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("details",result);
            resultMap.put("validityDate",cs.getValidityDate());
            resultMap.put("declineNote",cs.getDeclineNote());
            resultMap.put("requestedBy",cs.getRequestedBy());
            resultMap.put("requestedBy",cs.getRequestedBy());
            resultMap.put("verifiers",verifiers);
            resultMap.put("approvers",approvers);
            resultMap.put("comments",comments);

            return Optional.ofNullable(resultMap);
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public void updateCsByInitiator(Jwt token, String uri, Long id, CsRequestDto csRequestDto) {
        Optional<Cs> csOp = csRepository.findById(id);
        if(csOp.isEmpty()){
            throw new RuntimeException("Sorry! No Cs found");
        }
        Cs cs = csOp.get();
        cs.setTotalPrice(csRequestDto.getTotalPrice());
        cs.setDeliveryCharge(csRequestDto.getDeliveryCharge());
        cs.setSubTotalPrice(csRequestDto.getSubTotalPrice());
        cs.setVatAmount(csRequestDto.getVatAmount());
        cs.setValidityDate((csRequestDto.getValidityDate()));
        Indent indent = cs.getIndent();
        StringBuilder categories = new StringBuilder(indent.getCategory().getId().toString());
        categories.append(",").append(indent.getSubCategory().getId().toString());
        List<String> ids =new ArrayList<>();
        cs.setCsDetails(csRequestDto.getDetails().stream().map(v->{
            Optional<CsDetail> csDetailOp = csDetailRepository.findById(v.getId());
            if(csDetailOp.isEmpty()){
                throw new RuntimeException("Sorry! Cs Detail not found");
            }
            Optional<IndentDetail> indentDetailOp = indentDetailRepository.findById(v.getIndentDetailId());
            if(indentDetailOp.isEmpty()){
                throw new RuntimeException("Sorry! Indent Detail not found");
            }
            IndentDetail indentDetail = indentDetailOp.get();
            ids.add(indentDetail.getSubCategory().getId().toString());
            ids.add(indentDetail.getSubCategory().getParentCategory().getId().toString());

            CsDetail csDetail = csDetailOp.get();



            csDetail.setCs(cs);
            csDetail.setIndentDetail(indentDetail);
            v.getVendors().stream().filter(vd -> vd.getIsDeleted()).forEach(vd->{
                csVendorDetailRepository.deleteById(vd.getId());
            });

            csDetail.setVendorDetails(v.getVendors().stream().filter(vd->!vd.getIsDeleted()).map(vendorDetail->{
                CsVendorDetail csVendorDetail = new CsVendorDetail();
                csVendorDetail.setId(v.getId());
                csVendorDetail.setVendorId(vendorDetail.getVendorId());
                csVendorDetail.setVatAmount(vendorDetail.getVatAmount());
                csVendorDetail.setDiscountAmount(vendorDetail.getDiscountAmount());
                csVendorDetail.setPriceQuotation(new PriceQuotation(vendorDetail.getPriceQuotation().getId()));
                csVendorDetail.setOrderQty(vendorDetail.getOrderQty());
                csVendorDetail.setTotalPrice(vendorDetail.getTotalPrice());
                csVendorDetail.setTransactionType(vendorDetail.getTransactionType());
                csVendorDetail.setCsDetail(csDetail);
                csVendorDetail.setVendorDeliveryDetails(vendorDetail.getWarehouses().stream().map(w->{
                    CsDeliveryDetail csDeliveryDetail = new CsDeliveryDetail();
                    csDeliveryDetail.setId(w.getId());
                    csDeliveryDetail.setWarehouseId(w.getWarehouseId());
                    csDeliveryDetail.setDeliveryDate(w.getDeliveryDate());
                    csDeliveryDetail.setDeliveryQty(w.getDeliveryQty());
                    csDeliveryDetail.setVendorDeliveryDetail(csVendorDetail);
                    return csDeliveryDetail;
                }).collect(Collectors.toList()));
                return csVendorDetail;
            }).collect(Collectors.toList()));
            return csDetail;
        }).collect(Collectors.toList()));


        csRepository.save(cs);

//        verificationService.removeVerification(cs.getId(), DomainType.CS);
//        csVaHistoryRepository.deleteAllByCsId(cs.getId());

//        verificationService.applyVerifyApprovalProcess(cs, DomainType.CS, CsStatus.COMPLETED.toString(),
//                uri,"CATEGORY",ids, null);

//        @SuppressWarnings("unchecked")
//        Optional<Map<String, Object>> verifierOp = (Optional<Map<String, Object>>) verificationService.getVerifiers(loggedInUser, uri, categories.toString());
//
//        List<Verifier> verifiers = new ArrayList<>();
//        if (verifierOp.isPresent()) {
//            Map<String, Object> verification = verifierOp.get();
//
//            verifiers = (List<Verifier>) verification.get("verifiers");
//
//            Boolean verificationRequired = (Boolean) verification.get("verificationRequired");
//            if (verificationRequired != null && verificationRequired == true && verifiers != null && verifiers.size() > 0) {
//                cs.setStatus(CsStatus.PENDING_VERIFICATION);
//            } else {
//                cs.setStatus(CsStatus.PENDING);
//
//            }
//
//        }else{
//            cs.setStatus(CsStatus.APPROVED);
//            // generatePO(cs.getRequestedBy(),cs);
//        }
//
//        List<ApprovalSettingQuery.ApprovalPanel> approvalPanels = approvalSettingService.getModuleWiseApprovalSetting(uri,
//                Optional.ofNullable(categories.toString()),Optional.empty());
//
//        verificationService.removeVerification(cs.getId(), DomainType.CS);
//        csVaHistoryRepository.deleteAllByCsId(cs.getId());
//
//        verificationService.setVerifiers(cs, verifiers, DomainType.CS);
//        verificationService.setApprovers(cs, approvalPanels, DomainType.CS);
    }

    @Override
    public Optional<?> getAllItemsByVendorAndCs(Long vendorId, String csNo) {
        List<CsDetailRepository.CsVendorItemInfo> allItemsByVendor = csDetailRepository.findAllItemsByVendor(csNo, vendorId);
        record CsVendorItemsResult(Object result,List<?> termsConditions,List<?> warehouses){}
        List<Long> ids = new ArrayList<>();
        allItemsByVendor.stream().forEach(i->{
            ids.add(i.getPqId());
        });
        List<?> warehoues = csDetailRepository.findAllVendorWarehouses(csNo,vendorId);
        List<PqTermsAndCondition> allByVendorIdAndPriceQuotationId = pqTermAndConditionRepository.findAllByVendorIdAndPriceQuotationId(vendorId, ids);
        var result = new CsVendorItemsResult(allItemsByVendor,allByVendorIdAndPriceQuotationId,warehoues);
        return Optional.of(result);
    }

    @Override
    public List<?> getItemWiseVendors(Long tenderId, String brandName,String itemName) {
        Optional<Indent> indentOp = indentRepository.findById(tenderId);
        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! Rfq not found");
        }
        return csRepository.findLockedVendorsByItemName(tenderId, brandName,itemName);
    }

    @Override
    public Map<String, Object> getItemWiseVendors(Long tenderId, Long vendorId, String itemName) {
        Optional<Indent> indentOp = indentRepository.findById(tenderId);
        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! Rfq not found");
        }
        List<PqTermsAndCondition> termsAndConditions = pqTermAndConditionRepository.findAllByRfqIdAndVendorId(tenderId, vendorId);
        Map<String,Object> result = new HashMap<>();
        List<CsRepository.ItemWiseVendorDetail> itemWiseVendorDetails = new ArrayList<>();
        if(itemName!=null){
            itemWiseVendorDetails = csRepository.findLockedVendorsByItemName(tenderId,vendorId, itemName);
        }else{
            itemWiseVendorDetails = csRepository.findLockedVendorsByItemName(tenderId,vendorId, null);
        }

        result.put("itemWiseVendor",itemWiseVendorDetails);
        result.put("termsAndConditions",termsAndConditions);
        return result;
    }

    @Override
    @Transactional
    public void updateCs(Jwt token, Long id, CsUpdateRequestDto csDto, CsOperation csOperation) {
        Optional<Cs> csOp = csRepository.findById(id);
        if(csOp.isEmpty()){
            throw new RuntimeException("Sorry! Cs Not Found");
        }

        Cs cs = csOp.get();
        CsVendorDetailDto vendorDetailDto = csDto.getCsVendorDetailDto();
        if(csOperation.equals(CsOperation.ADD_VENDOR)){
            CsDetail csDetail = null;
            Optional<CsDetail> csDetailOp = cs.getCsDetails().stream().filter(csd->csd.getId().equals(csDto.getCsDetailId())).findFirst();
            if(csDetailOp.isPresent()){
                csDetail = csDetailOp.get();
            }else{
                csDetail = new CsDetail();
                csDetail.setCs(cs);
                csDetail.setIndentDetail(new IndentDetail(csDto.getIndentDetailId()));
                csDetail.setVendorDetails(new ArrayList<>());
            }


            List<CsVendorDetail> csVendorDetails = csDetail.getVendorDetails();
            CsVendorDetail csVendorDetail = new CsVendorDetail();

            csVendorDetail.setVendorId(vendorDetailDto.getVendorId());
            csVendorDetail.setVatAmount(vendorDetailDto.getVatAmount());
            csVendorDetail.setDiscountAmount(vendorDetailDto.getDiscountAmount());
            csVendorDetail.setPriceQuotation(new PriceQuotation(vendorDetailDto.getPriceQuotation().getId()));
            csVendorDetail.setOrderQty(vendorDetailDto.getOrderQty());
            csVendorDetail.setTotalPrice(vendorDetailDto.getTotalPrice());
            csVendorDetail.setTransactionType(vendorDetailDto.getTransactionType());
            csVendorDetail.setCsDetail(csDetail);
            csVendorDetails.add(csVendorDetail);
            csDetail.setVendorDetails(csVendorDetails);
        }
        if(csOperation.equals(CsOperation.REMOVE_VENDOR)){
            Optional<CsVendorDetail> csDetailOp = csVendorDetailRepository
                    .findByCsDetailIdAndVendorId(csDto.getCsDetailId(),
                            vendorDetailDto.getVendorId());
            if(csDetailOp.isPresent()){
                CsVendorDetail csVendorDetail = csDetailOp.get();
                csVendorDetailRepository.delete(csVendorDetail);
            }
        }
    }

    @Override
    public Page<?> getPendingVerificationCs(Jwt token,
                                            Optional<String> indentNo, Optional<String> status,
                                            Optional<String> formDateStr, Optional<String> toDateStr,
                                            Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort);
        LocalDateTime fromDate = parseDate(formDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                CsStatus.PENDING_VERIFICATION.name(),
                CsStatus.REVIEW.name(),
                CsStatus.VERIFIED.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses.add(status.get());
        }
        return csRepository.findPendingVerificationCs(claimResolver.getUserId(),
                indentNo.orElse(null), statuses,
                fromDate,toDate,
                pageable);
    }

    @Override
    public Page<?> getPendingApprovalCs(Jwt token,
                                        Optional<String> indentNo, Optional<String> status,
                                        Optional<String> fromDateStr, Optional<String> toDateStr,
                                        Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                CsStatus.PENDING_APPROVAL.name(),
                CsStatus.REVIEW.name(),
                CsStatus.APPROVED.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses.add(status.get());
        }
        return csRepository.findPendingApprovalCs(claimResolver.getUserId(),
                indentNo.orElse(null),statuses,
                fromDate,toDate,
                pageable);
    }

    @Override
    public Page<?> getApprovedCs(Jwt token,
                                 Optional<String> indentNo, Optional<String> status,
                                 Optional<String> fromDateStr, Optional<String> toDateStr,
                                 Optional<Integer> page, Optional<Integer> size
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                CsStatus.VERIFIED.name(),
                CsStatus.APPROVED.name(),
                CsStatus.COMPLETED.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses.add(status.get());
        }
        return csRepository.findApprovedCs(indentNo.orElse(null),statuses,fromDate,toDate,pageable);
    }

    @Override
    public Page<?> getClosedCs(Jwt token,
                               Optional<String> indentNo, Optional<String> status,
                               Optional<String> fromDateStr, Optional<String> toDateStr,
                               Optional<Integer> page, Optional<Integer> size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                CsStatus.APPROVED.name(),
                CsStatus.REJECTED.name(),
                CsStatus.VERIFIED.name(),
                CsStatus.COMPLETED.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses.add(status.get());
        }
        return csRepository.findClosedCs(indentNo.orElse(null),statuses,
                fromDate,toDate,
                pageable);
    }

    @Override
    @Transactional
    public void rejectCs(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<Cs> csOp = csRepository.findById(id);
        if(csOp.isPresent()){
            Cs cs = csOp.get();
            if(noteDto.getNote()!=null && !noteDto.getNote().isEmpty()){
                cs.setDeclineNote(noteDto.getNote());
                commentService.addComment(commentService.prepareComment(claimResolver.getEmployee().get(),
                        DomainType.CS, cs.getId(), noteDto.getNote(),noteDto.getAttachments()));
            }
            cs.setCsStatus(CsStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void reviewCs(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<Cs> csOp = csRepository.findById(id);
        if(csOp.isEmpty()){
            throw new RuntimeException("Sorry! Cs not found");
        }

        Cs cs = csOp.get();
        cs.setReviewerId(null);
        cs.setReviewDate(LocalDateTime.now());
        cs.setCsStatus(cs.getReviewPrevStatus());
        cs.setReviewPrevStatus(null);
        // if(cs.getNextVerifierId() != null && cs.getNextApproverId() == null){
        //     cs.setStatus(CsStatus.PENDING_VERIFICATION);
        // }
        // if(cs.getNextVerifierId()!= null && cs.getNextApproverId() != null){
        //     cs.setStatus(CsStatus.PENDING_APPROVAL);
        // }

        commentService.addComment(
                commentService.prepareComment(claimResolver.getEmployee().get(),
                        DomainType.CS,cs.getId(),noteDto.getNote(),noteDto.getAttachments())
        );
    }

    @Override
    @Transactional
    public void resentToPr(Jwt token, Long id) {
        claimResolver.setToken(token);
        Optional<Cs> csOp = csRepository.findById(id);
        if(csOp.isEmpty()){
            throw new RuntimeException("Sorry! Cs not found");
        }
        Cs cs = csOp.get();
        cs.setCsStatus(CsStatus.REJECTED);
        cs.getIndent().getIndentDetails().stream().forEach(ide->{
            productRequirementService.reOpen(ide.getProductRequirementsIds());
        });
        commentService.addComment(
                commentService.prepareComment(claimResolver.getEmployee().get(),
                        DomainType.CS,cs.getId(),"Rejected & Resent To PR",new ArrayList<>())
        );
    }

    @Override
    @Transactional
    public void resubmit(Jwt token, Long id) {
        claimResolver.setToken(token);
        Optional<Cs> csOp = csRepository.findById(id);
        if(csOp.isEmpty()){
            throw new RuntimeException("Sorry! Cs not found");
        }
        Cs cs = csOp.get();
        cs.setCsStatus(CsStatus.PENDING_VERIFICATION);
        cs.setReviewPrevStatus(null);
        cs.setReviewerId(null);
        csVaHistoryRepository.removeByCsId(cs.getId());
        List<UserApplicationValidation> verifiers = verificationService.getVerifyersByDomainId(DomainType.CS,cs.getId());
        verifiers.stream().forEach(verifer->{
            verifer.setVerificationDate(null);
            verifer.setVerified(false);
        });
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<Cs> csOp  = csRepository.findById(id);
        if(csOp.isPresent()){
            Cs cs = csOp.get();
            cs.setNextVerifierId(nextVerifier.getVerifier().getId());
            cs.setCsStatus(CsStatus.VERIFIED);
            setVAHistory(cs, CsStatus.VERIFIED);
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<Cs> csOp  = csRepository.findById(id);
        if(csOp.isPresent()){
            Cs cs = csOp.get();
            cs.setNextApproverId(nextApprover.getVerifier().getId());
//            cs.setCsStatus(CsStatus.APPROVED);
            setVAHistory(cs, CsStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<Cs> csOp  = csRepository.findById(id);
        if(csOp.isPresent()){
            Cs cs = csOp.get();
            if(firstApprover.isPresent()){
                cs.setNextApproverId(firstApprover.get().getVerifier().getId());
                cs.setCsStatus(CsStatus.PENDING_APPROVAL);
                setVAHistory(cs,CsStatus.VERIFIED);

            }else {

                cs.setCsStatus(CsStatus.VERIFIED);
                setVAHistory(cs,CsStatus.VERIFIED);
                csAccountService.createCsAccount(cs);
//                generatePO(cs.getRequestedBy(),cs,PurchaseOrderStatus.PENDING);
            }

        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<Cs> csOp  = csRepository.findById(id);
        if(csOp.isPresent()){
            Cs cs = csOp.get();
            cs.setCsStatus(CsStatus.APPROVED);
            setVAHistory(cs,CsStatus.APPROVED);

            csAccountService.createCsAccount(cs);
//            generatePO(cs.getRequestedBy(),cs,PurchaseOrderStatus.PENDING);
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<Cs> csOp  = csRepository.findById(domainId);
        if(csOp.isPresent()){
            Cs cs = csOp.get();
            cs.setReviewerId(reviewer.getId());
            cs.setReviewPrevStatus(cs.getCsStatus());
            cs.setCsStatus(CsStatus.REVIEW);

        }
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {

    }

    @Transactional
    private void setVAHistory(Cs cs, CsStatus status){
        CsVerificationApprovalHistory csVaHistory = new CsVerificationApprovalHistory();
        String empId = null;
        if (status.equals(CsStatus.VERIFIED)){
            empId =cs.getNextVerifierId();
        }else if(status.equals(CsStatus.APPROVED)){
            empId = cs.getNextApproverId();
        }
        csVaHistory.setCs(cs);
        csVaHistory.setEmployee(new Employee(empId));
        csVaHistory.setCsStatus(status);
        csVaHistoryRepository.save(csVaHistory);
    }


    @Async
    private void generatePO(Employee employee, Cs cs,PurchaseOrderStatus status){

        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        if(orgOp.isEmpty()){
            throw new RuntimeException("Sorry! Organization not found");
        }

        List<CsVendorDetailRepository.PendingItemBrandInfo> itemBrandInfos = csVendorDetailRepository.getPendingItemAndBrandInfoByCsId(cs.getId());
        for(CsVendorDetailRepository.PendingItemBrandInfo csDetail : itemBrandInfos){
            String itemAttributeName = (csDetail.getExtendedAttributes()!=null)? csDetail.getItemAttributeName()+" - "+csDetail.getExtendedAttributes() : csDetail.getItemAttributeName();
            Optional<Item> itemOp = itemService.getByBrandAndAttributeName(csDetail.getBrandName(),csDetail.getSubCatId(),itemAttributeName);
            if(itemOp.isEmpty()){

                Optional<ItemCategory> subCatOp = categoryService.getItemCategory(csDetail.getSubCatId());
                if(subCatOp.isEmpty()){
                    throw new RuntimeException("Sorry! Sub Cat missing");
                }
                ItemCategory subCat = subCatOp.get();

                PendingItemRequestDto pendingItemRequestDto = new PendingItemRequestDto();
                pendingItemRequestDto.setSubCategoryCode(csDetail.getSubCategoryCode());
                pendingItemRequestDto.setDepartment(employee.getDepartmentName());
                pendingItemRequestDto.setDesignation(employee.getDesignationName());
                pendingItemRequestDto.setEmployeeId(employee.getEmployeeId());
                if(employee.getReportingManager()!=null){
                    pendingItemRequestDto.setReportingManager(employee.getReportingManager());
                }
                pendingItemRequestDto.setRequestedBy(employee.getEmployeeName());
                pendingItemRequestDto.setWarehouseId(employee.getWarehouseId());
                pendingItemRequestDto.setWarehouseLocation(employee.getWarehouseName());
                pendingItemRequestDto.setWarehouseName(employee.getWarehouseName());
                pendingItemRequestDto.setBrand(csDetail.getBrandName());


                List<PendingItemAttributeDto> attributes = extractAttributesFromItemAttributeName(subCat,csDetail.getItemAttributeName());
                List<PendingItemAttributeDto> attributesFromItemAttributeName = extractAttributesFromItemAttributeName(subCat,csDetail.getExtendedAttributes());
                attributes.addAll(attributesFromItemAttributeName);
                pendingItemRequestDto.setAttributes(attributes);
                pendingItemRequestDto.setOrganizationId(orgOp.get().getCpsVendorRegistrationId());
                pendingItemRequestDto.setExtendedAttributes(csDetail.getExtendedAttributes());
                sendPendingItemRequest(pendingItemRequestDto);
            }

        }



        // Map<String,PurchaseOrder> poMap = new HashMap<>();
        List<PurchaseOrder> purchaseOrders =new ArrayList<>();
        // StringBuilder sb = new StringBuilder();
        PoGroup poGroup = new PoGroup();
        poGroup.setCs(cs);
        poGroup.setPurchaseOrderStatus(status);
        poGroupRepository.save(poGroup);
        List<CsRepository.PotentialPoListItem> poListItems = csRepository.getPotentialPoListFromCs(cs.getId());
        int i=1;
        for(CsRepository.PotentialPoListItem pol : poListItems){
            if(pol.getDeliveryDate()!=null){
                PurchaseOrder vPo = new PurchaseOrder();
                vPo.setCs(cs);
                vPo.setPoDate(pol.getDeliveryDate());
                vPo.setVendorId(pol.getVendorId());

                vPo.setPoNo(generatePoNo(cs, i));
                if(cs.getNextApproverId()!=null){
                    vPo.setRequestedBy(new Employee(cs.getNextApproverId()));
                }else{
                    vPo.setRequestedBy(cs.getRequestedBy());
                }

                List<PurchaseOrderDetail> pods = new ArrayList<>();
                for(String csvdId : List.of(pol.getCsVendorDetailId().split(","))){
                    PurchaseOrderDetail pod = new PurchaseOrderDetail();
                    pod.setItemName(null);
                    pod.setCsVendorDetail(new CsVendorDetail(Long.valueOf(csvdId)));
                    pod.setDeliveryDate(pol.getDeliveryDate());
                    pod.setDeliveryQty(pol.getDeliveryQty());
                    pod.setPurchaseOrder(vPo);
                    pod.setWarehouse(new Warehouse(pol.getWarehouseId()));
                    pods.add(pod);
                }
                vPo.setPurchaseOrderDetails(pods);
                vPo.setPoGroup(poGroup);
                vPo.setStatus(PurchaseOrderStatus.PENDING);
                purchaseOrders.add(vPo);

            }else{
                Optional<PqRepository.PriceQuotationDetailExt> pqDetailOp = pqRepository
                        .getPriceQuotationDetailByPqIdAndItemAttr(pol.getPriceQuotationId(),pol.getItemAttribute());

                if(pqDetailOp.isPresent()){
                    PurchaseOrder vPo = new PurchaseOrder();
                    vPo.setCs(cs);
                    LocalDate currentDate = LocalDate.now();
                    currentDate = currentDate.plusDays(pqDetailOp.get().getEstDeliveryDays());
                    vPo.setPoDate(currentDate.atTime(LocalTime.now()).toLocalDate());
                    vPo.setVendorId(pol.getVendorId());

                    vPo.setPoNo(generatePoNo(cs, i));
                    if(cs.getNextApproverId()!=null){
                        vPo.setRequestedBy(new Employee(cs.getNextApproverId()));
                    }else{
                        vPo.setRequestedBy(cs.getRequestedBy());
                    }

                    List<PurchaseOrderDetail> pods = new ArrayList<>();
                    for(String csvdId : List.of(pol.getCsVendorDetailId().split(","))){
                        PurchaseOrderDetail pod = new PurchaseOrderDetail();
                        pod.setCsVendorDetail(new CsVendorDetail(Long.valueOf(csvdId)));
                        pod.setDeliveryDate(currentDate);
                        pod.setPurchaseOrder(vPo);
                        pod.setWarehouse(new Warehouse(pqDetailOp.get().getWarehouseId()));
                        pods.add(pod);
                    }
                    vPo.setPurchaseOrderDetails(pods);
                    vPo.setStatus(PurchaseOrderStatus.PENDING);
                    vPo.setPoGroup(poGroup);
                    purchaseOrders.add(vPo);
                }
            }
            i++;
        }
        // cs.getCsDetails().stream().forEach(csd->{
        //     sb.append(csd.getIndentDetail().getSubCategory().getId()).append(",");
        //     csd.getVendorDetails().stream().forEach(vd->{

        //         if(!poMap.containsKey(vd.getVendorId().toString())){
        //             System.out.println(vd.getVendorId()+"_"+vd.getId());
        //             PurchaseOrder vPo = new PurchaseOrder();
        //             List<PurchaseOrderDetail> pods = new ArrayList<>();
        //             vPo.setPurchaseOrderDetails(pods);
        //             setPoDetail(cs,vPo,vd);
        //             poMap.put(vd.getVendorId().toString(), vPo);
        //             vPo.setRequestedBy(new Employee(cs.getNextApproverId()));

        //         }else{
        //             System.out.println(vd.getVendorId()+"_"+vd.getId());
        //             PurchaseOrder vPo = (PurchaseOrder) poMap.get(vd.getVendorId().toString());
        //             setPoDetail(cs,vPo, vd);
        //         }
        //     });
        // });

        // String categories = sb.substring(0,sb.toString().length()-1);
        // purchaseOrders = poMap.entrySet().stream().map(et->{
        //     return et.getValue();
        // }).collect(Collectors.toList());


        purchaseOrderService.createPurchaseOrder(purchaseOrders);

        // setVerificationAndApproval(categories,employee,poGroup);

    }

    private String generatePoNo(Cs cs,Integer i){
        return cs.getCsNo()+"-"+ ((i<10)? "0"+i.toString() : i.toString());
    }

    @Async
    private void sendPendingItemRequest(PendingItemRequestDto payloadDto) {
        try{
            HttpHeaders headers = new HttpHeaders();
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
            if(orgOp.isPresent()){
                headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
            }
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PendingItemRequestDto> payload = new HttpEntity<>(payloadDto,headers);
            String url = cpsServerConfig.getPendingItemReqEndpoint();
            ResponseEntity<Void> response = (ResponseEntity<Void>)networkService.post(url, payload,Void.class);
            if(!response.getStatusCode().equals(HttpStatus.CREATED)){
                throw new RuntimeException("Sorry! Something wrong");
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    private List<PendingItemAttributeDto> extractAttributesFromItemAttributeName(ItemCategory cat, String itemAttributeName){

        List<PendingItemAttributeDto> pendingItemAttrList = new ArrayList<>();
        if(itemAttributeName!=null){

            String[] attrs = itemAttributeName.split(" - ");


            for(String attr : attrs){
                String _attr="";
                Optional<CategoryAttribute> catAttrOp = cat.getAttributes().stream().filter(c->{
                    return attr.contains(c.getAttributeType());

                }).findFirst();

                if(catAttrOp.isPresent()){
                    _attr = attr.replace(catAttrOp.get().getAttributeType(),"");

                    String[] args = _attr.trim().split(" ");
                    PendingItemAttributeDto pia = new PendingItemAttributeDto();
                    pia.setAttributeType(catAttrOp.get().getAttributeType());
                    pia.setAttributeValue(args[0].trim());
                    pia.setAttributeUnit(catAttrOp.get().getAttributeUnit());
                    pendingItemAttrList.add(pia);
                }
            }
        }
        return pendingItemAttrList;
    }
}
