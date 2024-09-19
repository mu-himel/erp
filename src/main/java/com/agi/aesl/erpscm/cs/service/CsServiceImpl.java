package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.cs.dto.CsRequestDto;
import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.cs.entity.CsDeliveryDetail;
import com.agi.aesl.erpscm.cs.entity.CsDetail;
import com.agi.aesl.erpscm.cs.entity.CsVendorDetail;
import com.agi.aesl.erpscm.cs.enums.CsStatus;
import com.agi.aesl.erpscm.cs.repository.CsDetailRepository;
import com.agi.aesl.erpscm.cs.repository.CsRepository;
import com.agi.aesl.erpscm.cs.repository.CsVaHistoryRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.entity.IndentDetail;
import com.agi.aesl.erpscm.indent.repository.IndentDetailRepository;
import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.price_quotation.entity.PqTermsAndCondition;
import com.agi.aesl.erpscm.price_quotation.entity.PriceQuotation;
import com.agi.aesl.erpscm.price_quotation.repository.PqTermAndConditionRepository;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private PqTermAndConditionRepository pqTermAndConditionRepository;



    @Override
    public Page<?> getAllPendingCs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort );
        return indentRepository.getAllIndentsByExpireDateTime(LocalDateTime.now(), pageable);
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

        verificationService.applyVerifyApprovalProcess(cs, DomainType.CS, CsStatus.COMPLETED.toString(),
                uri,"CATEGORY",ids, null);

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
            resultMap.put("declineNote",cs.getDeclineNote());
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
            csDetail.setVendorDetails(v.getVendors().stream().map(vendorDetail->{
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

        verificationService.removeVerification(cs.getId(), DomainType.CS);
        csVaHistoryRepository.deleteAllByCsId(cs.getId());

        verificationService.applyVerifyApprovalProcess(cs, DomainType.CS, CsStatus.COMPLETED.toString(),
                uri,"CATEGORY",ids, null);

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
    public List<?> getItemWiseVendors(Long tenderId, String itemName) {
        Optional<Indent> indentOp = indentRepository.findById(tenderId);
        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! Rfq not found");
        }
        return csRepository.findLockedVendorsByItemName(tenderId, itemName);
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
}
