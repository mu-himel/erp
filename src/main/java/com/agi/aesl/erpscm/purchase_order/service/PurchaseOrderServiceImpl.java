package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.common.enums.DeliveryCharge;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.cs.entity.CsVendorDetail;
import com.agi.aesl.erpscm.cs.repository.CsRepository;
import com.agi.aesl.erpscm.cs.repository.CsVendorDetailRepository;
import com.agi.aesl.erpscm.cs.service.CsService;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.entity.Indent;
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
import com.agi.aesl.erpscm.price_quotation.repository.PqRepository;
import com.agi.aesl.erpscm.price_quotation.repository.PqTermAndConditionRepository;
import com.agi.aesl.erpscm.purchase_order.dto.request.*;
import com.agi.aesl.erpscm.purchase_order.entity.PoGroup;
import com.agi.aesl.erpscm.purchase_order.entity.PoVerificationApprovalHistory;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrderDetail;
import com.agi.aesl.erpscm.purchase_order.enums.PurchaseOrderStatus;
import com.agi.aesl.erpscm.purchase_order.repository.PoGroupRepository;
import com.agi.aesl.erpscm.purchase_order.repository.PoVaHistoryRepository;
import com.agi.aesl.erpscm.purchase_order.repository.PurchaseOrderRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService{
    private static final Integer PAGE_SIZE = 20;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PoGroupRepository poGroupRepository;

    @Autowired
    private UserApplicationValidatorService<PoGroup> verificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PqTermAndConditionRepository pqTermAndConditionRepository;

    @Autowired
    private ItemService itemService;
    List<PqTermsAndCondition> termsAndConditions = new ArrayList<>();

    @Autowired
    private PoVaHistoryRepository poVaHistoryRepository;

    @Autowired
    private CsVendorDetailRepository csVendorDetailRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrgService orgService;

    @Autowired
    private CsRepository csRepository;
    
    @Autowired
    private NetworkService networkService;

    @Autowired
    private CpsServerConfig cpsServerConfig;

    @Autowired
    private PqRepository pqRepository;


    @Override
    @Transactional
    public void createPurchaseOrder(List<PurchaseOrder> purchaseOrders) {
        purchaseOrderRepository.saveAll(purchaseOrders);
    }

    private String generatePoNo(Cs cs){
        Long count = purchaseOrderRepository.countAllByCsId(cs.getId());
        count = ++count;
        return cs.getCsNo()+"-"+ ((count<10)? "0"+count.toString() : count.toString());
    }
    @Override
    @Transactional
    public void generatePurchaseOrder(Jwt token, String uri, PurchaseRequestDto purchaseRequestDto){
            claimResolver.setToken(token);
            Optional<Employee> empOp = claimResolver.getEmployee();
            Employee employee = empOp.get();
            if(empOp.isEmpty()){
                throw new RuntimeException("Sorry! Employee Profile required");
            }
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
            if(orgOp.isEmpty()){
                throw new RuntimeException("Sorry! Organization not found");
            }
            Optional<Cs> csOp = csRepository.findById(purchaseRequestDto.getCsId());
            if(csOp.isEmpty()){
                throw new RuntimeException("Sorry! Cs not found");
            }
            Cs cs = csOp.get();
            List<CsVendorDetailRepository.PendingItemBrandInfo> itemBrandInfos = csVendorDetailRepository.getPendingItemAndBrandInfoByCsId(purchaseRequestDto.getCsId());
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



            List<PurchaseOrder> purchaseOrders = new ArrayList<>();

            PoGroup poGroup = new PoGroup();
            poGroup.setCs(cs);
            poGroup.setPhoneNo(purchaseRequestDto.getPhoneNo());
            poGroup.setVendorName(purchaseRequestDto.getVendorName());
            poGroup.setVendorEmail(purchaseRequestDto.getVendorEmail());
            poGroup.setPoDate(LocalDateTime.now());
            poGroupRepository.save(poGroup);

            PurchaseOrder po = new PurchaseOrder();
            po.setCs(cs);
            po.setPoDate(poGroup.getPoDate().toLocalDate());
            po.setVendorId(purchaseRequestDto.getVendorId());
            po.setDeliveryChargeType(purchaseRequestDto.getDeliveryChargeType());
            po.setPoNo(generatePoNo(cs));
            po.setPoGroup(poGroup);
            po.setRequestedBy(employee);
            po.setTermsConditions(purchaseRequestDto.getTermsConditions().stream().map(tnc->{
                tnc.setPurchaseOrder(po);
                return tnc;
            }).collect(Collectors.toList()));
            List<String> ids = new ArrayList<>();
            List<PurchaseOrderDetail> pods = new ArrayList<>();
            for(PoDetailReqDto poDetailReqDto: purchaseRequestDto.getDetails()){
                PurchaseOrderDetail pod = new PurchaseOrderDetail();
                pod.setItemName(poDetailReqDto.getItemName());
                pod.setUnitPrice(poDetailReqDto.getUnitPrice());
                pod.setTransactionType(poDetailReqDto.getTransactionType());
                pod.setEstimatedDeliveryDays(poDetailReqDto.getEstimatedDeliveryDays());
                pod.setCreditDays(poDetailReqDto.getCreditDays());
                pod.setTotalPrice(poDetailReqDto.getTotalPrice());
                pod.setSubTotal(poDetailReqDto.getSubTotal());
                pod.setWarrantyUnit(poDetailReqDto.getWarrantyUnit());
                pod.setWarrantyDuration(poDetailReqDto.getWarrantyDuration());
                pod.setVatPercent(poDetailReqDto.getVatPercent());
                pod.setVatAmount(poDetailReqDto.getVatAmount());
                pod.setCsVendorDetail(new CsVendorDetail(poDetailReqDto.getCsVendorDetail().getId()));
                pod.setDeliveryDate(poDetailReqDto.getDeliveryDate());

                pod.setRemainingQty(poDetailReqDto.getRemainingQty());
                pod.setDeliveryCharge(poDetailReqDto.getDeliveryCharge());
                pod.setPurchaseOrder(po);
                pod.setWarehouse(new Warehouse(poDetailReqDto.getWarehouse().getId()));
                AtomicReference<BigDecimal> i = new AtomicReference<>(new BigDecimal(0L));
                pod.setWarehouseDetailList(poDetailReqDto.getWarehouseDetailList().stream().map(wd->{
                    i.updateAndGet(w->w.add(wd.getQty()));
                    wd.setPurchaseOrderDetail(pod);
                    return wd;
                }).collect(Collectors.toList()));
                pod.setDeliveryQty(i.get());
                pods.add(pod);
                ids.add(poDetailReqDto.getCategoryId().toString());
                ids.add(poDetailReqDto.getSubCategoryId().toString());

            }

            po.setPurchaseOrderDetails(pods);
            po.setStatus(PurchaseOrderStatus.PENDING);
            purchaseOrders.add(po);
            createPurchaseOrder(purchaseOrders);

        AppliedVADto vaResult = verificationService.applyVerifyApprovalProcess(poGroup, DomainType.PO, PurchaseOrderStatus.APPROVED.toString(),
                uri, "CATEGORY", ids, null);
        if(vaResult.getVerifiers().isEmpty() && vaResult.getPanels().isEmpty()){
            throw new RuntimeException("Sorry! PO generation required Verify or Approval process");
        }
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

    private LocalDateTime parseDate(Optional<String> dateStr,String endTime){
        LocalDateTime date = null;
        if(dateStr.isPresent()){
            String time = (endTime!=null && endTime.trim().length()==8)? "T"+endTime:"T00:00:00";
            date = LocalDateTime.parse(dateStr.get()+time);
        }
        return date;
    }


    @Override
    public Page<?> getPendingPOs(Optional<String>csNo,
                                 Optional<String> poNo,Optional<Long> categoryId,
                                 Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                 Optional<String> toDateStr, Optional<String> status,
                                 Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                PurchaseOrderStatus.PENDING.name(),
                PurchaseOrderStatus.PENDING_VERIFICATION.name(),
                PurchaseOrderStatus.PENDING_APPROVAL.name(),
                PurchaseOrderStatus.REVIEW.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses = statuses.stream().filter(st->{
               return st.equals(status.get());
            }).toList();

        }
        return purchaseOrderRepository.findAllPendingPOs(csNo.orElse(null),
                poNo.orElse(null),
                categoryId.orElse(null),
                subCategoryId.orElse(null),
                fromDate,toDate,statuses
                ,pageable);
    }

    @Override
    public Page<?> getPendingVerificationPOs(Jwt token,
                                             Optional<String>csNo,
                                             Optional<String> poNo,Optional<Long> categoryId,
                                             Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                             Optional<String> toDateStr, Optional<String> status,
                                             Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                PurchaseOrderStatus.PENDING_VERIFICATION.name(),
                PurchaseOrderStatus.VERIFIED.name(),
                PurchaseOrderStatus.REVIEW.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses = statuses.stream().filter(st->{
                return st.equals(status.get());
            }).toList();
        }
        return purchaseOrderRepository.findAllPendingVerificationPOs(
                claimResolver.getUserId(),
                csNo.orElse(null), poNo.orElse(null),
                categoryId.orElse(null), subCategoryId.orElse(null),
                fromDate,toDate,statuses,
                pageable);
    }

    @Override
    public Page<?> getPendingApprovalPOs(Jwt token,
                                         Optional<String>csNo,
                                         Optional<String> poNo,Optional<Long> categoryId,
                                         Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                         Optional<String> toDateStr, Optional<String> status,
                                         Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                PurchaseOrderStatus.PENDING_APPROVAL.name(),
                PurchaseOrderStatus.APPROVED.name(),
                PurchaseOrderStatus.REVIEW.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses = statuses.stream().filter(st->{
                return st.equals(status.get());
            }).toList();
        }
        return purchaseOrderRepository.findAllPendingApprovalPOs(
                claimResolver.getUserId(),
                csNo.orElse(null), poNo.orElse(null),
                categoryId.orElse(null), subCategoryId.orElse(null),
                fromDate,toDate,statuses,
                pageable);
    }

    @Override
    public Page<?> getApprovedPOs(Jwt token,
                                  Optional<String>csNo,
                                  Optional<String> poNo,Optional<Long> categoryId,
                                  Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                  Optional<String> toDateStr, Optional<String> status,
                                  Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                PurchaseOrderStatus.APPROVED.name(),
                PurchaseOrderStatus.VERIFIED.name(),
                PurchaseOrderStatus.COMPLETED.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses = statuses.stream().filter(st->{
                return st.equals(status.get());
            }).toList();
        }
        return purchaseOrderRepository.findAllApprovedPos(
                csNo.orElse(null), poNo.orElse(null),
                categoryId.orElse(null), subCategoryId.orElse(null),
                fromDate,toDate,statuses,
                pageable);
    }

    @Override
    public Page<?> getClosedPOs(Jwt token,
                                Optional<String>csNo,
                                Optional<String> poNo,Optional<Long> categoryId,
                                Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                Optional<String> toDateStr, Optional<String> status,
                                Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE));
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,"23:59:59");
        List<String> statuses = Arrays.asList(
                PurchaseOrderStatus.APPROVED.name(),
                PurchaseOrderStatus.VERIFIED.name(),
                PurchaseOrderStatus.COMPLETED.name(),
                PurchaseOrderStatus.REJECTED.name()
        );
        if(status.isPresent()){
            statuses = new ArrayList<>();
            statuses = statuses.stream().filter(st->{
                return st.equals(status.get());
            }).toList();
        }
        return purchaseOrderRepository.findAllClosedPOs(csNo.orElse(null), poNo.orElse(null),
                categoryId.orElse(null), subCategoryId.orElse(null),
                fromDate,toDate,statuses,pageable);
    }

    @Override
    public Map<String, Object> getPurchaseOrderDetail(Long csId) {
        // Need to update here cause now po verifing based on collection of po
        // first find poGroup from csId
        System.out.println("HEREEEE "+ csId);
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(csId);
        Map<String,Object> map = new HashMap<>();
        if(poGroupOp.isPresent()){
            PoGroup poGroup = poGroupOp.get();
            // then get list of PO and its child using pogroup id
            List<PurchaseOrderRepository.PurchaseOrderDetailInfo> purchaseOrders = purchaseOrderRepository.findAllByPoGroupId(poGroup.getId());
            List<Map<String,Object>> polist = new ArrayList<>();

            if(!poGroup.getPurchaseOrderStatus().equals(PurchaseOrderStatus.PENDING)) {
                List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
                List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
                List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                        .getVerificationsByDomainTypeAndDomainId(DomainType.PO, poGroup.getId());
                vrs.stream().forEach(verifier -> {
                    if (verifier.getIsApproval() == false) {
                        verifiers.add(verifier);
                    } else {
                        approvers.add(verifier);
                    }
                });
                List<?> comments = commentService.getCommentsByDomain(DomainType.PO, poGroup.getId());
                map.put("verifiers", verifiers);
                map.put("approvers", approvers);
                map.put("comments", comments);
            }

            Cs cs = poGroup.getCs();
            Indent indent = cs.getIndent();
            map.put("nextVerifierId", poGroup.getNextVerifierId());
            map.put("nextApproverId", poGroup.getNextApproverId());
            map.put("reviewerId", poGroup.getReviewerId());
            map.put("declineNote", poGroup.getDeclineNote());
            map.put("reviewDate",poGroup.getReviewDate());
            map.put("status", poGroup.getPurchaseOrderStatus());
            map.put("csNo",cs.getCsNo());
            map.put("csId",cs.getId());
            map.put("poGroupId",poGroup.getId());
            map.put("indentId",indent.getId());

            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.PO, poGroup.getId());
            vrs.stream().forEach(verifier->{
                if(verifier.getIsApproval()==false){
                    verifiers.add(verifier);
                }else{
                    approvers.add(verifier);
                }
            });

            List<?> comments = commentService.getCommentsByDomain(DomainType.PO, poGroup.getId());

            map.put("verifiers",verifiers);
            map.put("approvers",approvers);
            map.put("comments",comments);



            // Indent indent = poGroup.getCs().getIndent();
            map.put("categories",indent.getCategory().getId()+","+indent.getSubCategory().getId());
            map.put("categoryName", indent.getCategory().getName()+"-"+indent.getSubCategory().getName());

            polist = purchaseOrders.stream().map(po->{
                Map<String,Object> poItem = new HashMap<>();
                List<Map<String,Object>> poDetailList = new ArrayList<>();
                List<PurchaseOrderRepository.PqDetailInfo> pqDetailInfo = purchaseOrderRepository.getPurchaseOrderDetail(po.getId());
                poItem.put("poNo", po.getPoNo());
                poItem.put("date", po.getPoDate());
                poItem.put("vendorPartialVatAmount", po.getPoDate());
                AtomicReference<BigDecimal> vendorPartialVatAmount = new AtomicReference<>();
//                AtomicReference<BigDecimal> totalPrice = new AtomicReference<>(new BigDecimal(0));
                for (PurchaseOrderRepository.PqDetailInfo pqDetail : pqDetailInfo){
                    Optional<PurchaseOrderRepository.POD> podOp = po.getPurchaseOrderDetails().stream()
                            .filter(_pod->_pod.getId().equals(pqDetail.getPodId())).findFirst();
                    Map<String,Object> detailMap = new HashMap<>();
                    detailMap.put("poId",pqDetail.getPoId());
                    detailMap.put("vendorPartialVatAmount", pqDetail.getVendorPartialVatAmount());
                    detailMap.put("itemAttribute", pqDetail.getItemName());
                    detailMap.put("unitPrice", pqDetail.getUnitPrice());
                    detailMap.put("deliveryCharge", pqDetail.getDeliveryCharge());
                    detailMap.put("deliveryQty", pqDetail.getDeliveryQty());
                    detailMap.put("remainingQty", pqDetail.getRemainingQty());
                    detailMap.put("transactionType", pqDetail.getTransactionType());
                    detailMap.put("totalPrice",  pqDetail.getTotalPrice());
                    detailMap.put("vatAmount",  pqDetail.getVatAmount());
                    detailMap.put("subTotal",  pqDetail.getSubTotal());
                    detailMap.put("orderQty",   pqDetail.getOrderQty());
                    detailMap.put("deliveryOrderQty",pqDetail.getDeliveryOrderQty());
                    detailMap.put("deliveryDate",pqDetail.getDeliveryDate());
                    detailMap.put("priceQuotationId",pqDetail.getPriceQuotationId());
                    detailMap.put("warehouseId", pqDetail.getWarehouseId());
                    detailMap.put("isAitAdded",pqDetail.getIsAitAdded());
                    detailMap.put("isVatAdded" , pqDetail.getIsVatAdded());
                    if(podOp.isPresent()){
                        detailMap.put("warehouses", podOp.get().getWarehouseDetailList());
                    }

//                    totalPrice.set(pqDetail.getTotalPrice());
                    vendorPartialVatAmount.set(pqDetail.getVendorPartialVatAmount());

                    String[] summary = pqDetail.getSummary().split(",");

                    termsAndConditions = pqTermAndConditionRepository.findAllByVendorIdAndPriceQuotationId(Long.parseLong(summary[1]),Long.parseLong(summary[16]));


                    String itemAttributeToMatch = (summary[12].trim()!="")? summary[10].trim()+" - "+ summary[12].trim() : summary[10].trim();
                    System.out.println("brandName:"+summary[11].trim());
                    System.out.println("itemAttributeToMatch:"+itemAttributeToMatch);
                    System.out.println("subCat:"+indent.getSubCategory().getId());
                    Optional<Item> itemOp = itemService
                            .getByBrandAndAttributeName(summary[11].trim(),
                                    indent.getSubCategory().getId(),
                                    itemAttributeToMatch);
                    poItem.put("orderQty",pqDetail.getOrderQty());
                    if(po.getPoDate().equals(pqDetail.getDeliveryDate())) {
                        poItem.put("deliveryOrderQty", pqDetail.getDeliveryOrderQty());
                        poItem.put("deliveryDate", pqDetail.getDeliveryDate());
                        poItem.put("warehouseId", pqDetail.getWarehouseId());
                    }
                    poItem.put("isAitAdded",pqDetail.getIsAitAdded());
                    poItem.put("isVatAdded",pqDetail.getIsVatAdded());
//                    detailMap.put("transactionType",pqDetail.getTransactionType());
                    poItem.put("transactionType",pqDetail.getTransactionType());

                    poItem.put("vendorName",summary[0]);
                    poItem.put("vendorId",summary[1]);
                    poItem.put("creditDays",summary[2]);
                    poItem.put("estDeliveryDays",summary[4]);
                    poItem.put("deliveryChargeType",summary[6].equals("1")? DeliveryCharge.INCLUDED.toString():
                            DeliveryCharge.EXCLUDED.toString());
                    detailMap.put("vatPercent",summary[8]);
                    detailMap.put("itemName",summary[10]);
                    detailMap.put("brandName",summary[11]);
                    detailMap.put("extendedAttribute",summary[12]);
                    detailMap.put("isItemExist",itemOp.isPresent());
                    detailMap.put("warrantyDuration",summary[13]);
                    detailMap.put("warrantyUnit",summary[14]);
                    poItem.put("vendorType",summary[15]);
                    poItem.put("pqId",summary[16]);
                    poItem.put("vendorEmail" , summary[17]);
                    poItem.put("vendorPhoneNo" , summary[18]);
                    poItem.put("totalPrice",pqDetail.getTotalPrice());
                    poDetailList.add(detailMap);
                }
                // poItem.put("vendor",po.get)
                map.put("requestedBy",po.getRequestedBy());
                poItem.put("details",poDetailList);
                poItem.put("termsAndConditions", termsAndConditions);

//                poItem.put("totalPrice",totalPrice.get());
                poItem.put("vendorPartialVatAmount",vendorPartialVatAmount);
                return poItem;
            }).collect(Collectors.toList());

            map.put("purchaseOrder",polist.stream().findFirst());
        }
        return map;
    }

    @Override
    public void setVerificationAndApproval(Jwt token, String uri, Long csId) {
        claimResolver.setToken(token);
        Optional<PoGroup> poGroupOp = poGroupRepository.findByCsId(csId);
        if(poGroupOp.isEmpty()){
            throw new RuntimeException("Sorry! Po not found");
        }

        PoGroup po = poGroupOp.get();
        po.setPurchaseOrderStatus(PurchaseOrderStatus.PENDING_VERIFICATION);
        Indent indent = po.getCs().getIndent();
        List<String> ids =new ArrayList<>();
        ids.add(indent.getCategory().getId().toString());
        ids.add(indent.getSubCategory().getId().toString());
//        StringBuilder sb = new StringBuilder();
//        sb.append(indent.getCategory().getId()).append(",").append(indent.getSubCategory().getId());
//
//        String categories = sb.toString();


//        @SuppressWarnings("unchecked")
//        Optional<Map<String, Object>> verifierOp = (Optional<Map<String, Object>>) verificationService.getVerifiers(loggedInUser, uri, categories);
//
//        List<Verifier> verifiers = new ArrayList<>();
//        if (verifierOp.isPresent()) {
//            Map<String, Object> verification = verifierOp.get();
//
//            verifiers = (List<Verifier>) verification.get("verifiers");
//
//            Boolean verificationRequired = (Boolean) verification.get("verificationRequired");
//            if (verificationRequired != null && verificationRequired == true && verifiers != null && verifiers.size() > 0) {
//                po.setStatus(PurchaseOrderStatus.PENDING_VERIFICATION);
//            } else {
//                po.setStatus(PurchaseOrderStatus.PENDING);
//            }
//        }
//
//        List<ApprovalSettingQuery.ApprovalPanel> approvalPanels = approvalSettingService.getModuleWiseApprovalSetting(uri,
//                Optional.ofNullable(categories.toString()),Optional.empty());
//
//        if(po.getStatus().equals(PurchaseOrderStatus.PENDING) && approvalPanels.size()>0){
//            po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
//        }

        // po.setStatus(PurchaseOrderStatus.PENDING_VERIFICATION);
        po.setIsVerifyApproveEnabled(true);

        verificationService.applyVerifyApprovalProcess(po, DomainType.PO, PurchaseOrderStatus.APPROVED.toString(),
                uri,"CATEGORY",ids,null);

//        verificationService.setVerifiers(po, verifiers, DomainType.PO);
//        verificationService.setApprovers(po, approvalPanels, DomainType.PO);
    }

    @Override
    @Transactional
    public void reviewPo(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(id);
        if(poGroupOp.isEmpty()){
            throw new RuntimeException("Sorry! PO not found");
        }

        PoGroup po = poGroupOp.get();
        po.setReviewerId(null);
        po.setReviewDate(LocalDateTime.now());
        if(po.getNextApproverId()!=null && po.getNextApproverId() == null){
            po.setPurchaseOrderStatus(PurchaseOrderStatus.PENDING_VERIFICATION);
        }
        if(po.getNextVerifierId()!=null && po.getNextApproverId() != null){
            po.setPurchaseOrderStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        }

        commentService.addComment(
                commentService.prepareComment(claimResolver.getEmployee().get(),DomainType.PO,po.getId(),noteDto.getNote(),
                        noteDto.getAttachments())
        );



    }

    @Override
    @Transactional
    public void rejectPo(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<PurchaseOrder> poOp = purchaseOrderRepository.findById(id);
        if(poOp.isEmpty()){
            throw new RuntimeException("Sorry! PO not found");
        }
        PurchaseOrder po = poOp.get();
        po.setStatus(PurchaseOrderStatus.REJECTED);
        commentService.addComment(
                commentService.prepareComment(claimResolver.getEmployee().get(),DomainType.PO,po.getId(),noteDto.getNote(),
                        noteDto.getAttachments())
        );
    }

    @Transactional
    private void setVAHistory(PoGroup po, PurchaseOrderStatus status){
        PoVerificationApprovalHistory poVaHistory = new PoVerificationApprovalHistory();
        poVaHistory.setPo(po);
        if(status.equals(PurchaseOrderStatus.VERIFIED)) {
            poVaHistory.setEmployee(new Employee(po.getNextVerifierId()));
        }else if(status.equals(PurchaseOrderStatus.APPROVED)){
            poVaHistory.setEmployee(new Employee(po.getNextApproverId()));
        }
        poVaHistory.setPoStatus(status);
        poVaHistoryRepository.save(poVaHistory);
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification,
                         UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(id);
        if(poGroupOp.isPresent()){
            PoGroup po = poGroupOp.get();
            po.setNextVerifierId(nextVerifier.getVerifier().getId());
            po.setPurchaseOrderStatus(PurchaseOrderStatus.VERIFIED);
            setVAHistory(po,PurchaseOrderStatus.VERIFIED);
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification,
                          UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(id);
        if(poGroupOp.isPresent()){
            PoGroup po = poGroupOp.get();
            po.setNextApproverId(nextApprover.getVerifier().getId());
            po.setPurchaseOrderStatus(PurchaseOrderStatus.APPROVED);
            setVAHistory(po, PurchaseOrderStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(id);
        if(poGroupOp.isPresent()){
            PoGroup poGroup = poGroupOp.get();
            if(firstApprover.isPresent()){
                poGroup.setNextApproverId(firstApprover.get().getVerifier().getId());
                poGroup.setPurchaseOrderStatus(PurchaseOrderStatus.PENDING_APPROVAL);
            }else {
                poGroup.setPurchaseOrderStatus(PurchaseOrderStatus.VERIFIED);
                sentPoToVendors(poGroup);
            }
            setVAHistory(poGroup,PurchaseOrderStatus.VERIFIED);
        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<PoGroup> poGroup = poGroupRepository.findById(id);
        if(poGroup.isPresent()){
            PoGroup po = poGroup.get();
            po.setPurchaseOrderStatus(PurchaseOrderStatus.APPROVED);
            setVAHistory(po,PurchaseOrderStatus.APPROVED);
            sentPoToVendors(po);
        }
    }

    @Async
    public void sentPoToVendors(PoGroup poGroup) {
        // Replace Purchase Order Reference with Po group for this method
        // Get List of Purchase Orders and process sent po to vendor for that collection of po items
        List<PoRemoteReqDto> remotePos = new ArrayList<>();


        List<PurchaseOrderRepository.PurchaseOrderDetailInfo> purchaseOrders = purchaseOrderRepository.findAllByPoGroupId(poGroup.getId());

        for(PurchaseOrderRepository.PurchaseOrderDetailInfo po : purchaseOrders){
            PoRemoteReqDto poRemoteReqDto = new PoRemoteReqDto();
            List<PoRemoteDetailReqDto> orderDetails = new ArrayList<>();
//          List<PurchaseOrderRepository.PqDetailInfo> pqDetailInfos = purchaseOrderRepository.getPurchaseOrderDetail(po.getId());
            poRemoteReqDto.setId(po.getId());
            poRemoteReqDto.setPoNo(po.getPoNo());
//            System.out.println(po.getCreatedAt().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            poRemoteReqDto.setPoDate(po.getCreatedAt().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            poRemoteReqDto.setCategoryCode(po.getCs().getIndent().getSubCategory().getCode().substring(2));
            poRemoteReqDto.setTenderNo(po.getCs().getIndent().getIndentNo());
            poRemoteReqDto.setDeliveryChargeType(po.getDeliveryChargeType());
            poRemoteReqDto.setDeliveryDate(po.getPoDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            po.getPurchaseOrderDetails().stream().forEach(podi->{
//                if(po.getId().equals(pqdi.get)){
                    PoRemoteDetailReqDto prdr = new PoRemoteDetailReqDto();
//                    prdr.setWarehouse(new ReferenceObjectDto(pqdi.getWarehouse().getId()));
                    List<PoRemoteDeliveryDetailDto> prdds = new ArrayList<>();
                    podi.getWarehouseDetailList().stream().forEach(wd->{
                        PoRemoteDeliveryDetailDto prdd = new PoRemoteDeliveryDetailDto();
                        prdd.setItemQty(wd.getQty());
                        prdd.setDeliveryCharge(wd.getDeliveryCharge());
                        prdd.setWarehouse(new ReferenceObjectDto(wd.getWarehouse().getId()));
                        prdds.add(prdd);
                    });
                    prdr.setPoDeliveryDetailsDtoList(prdds);
                    prdr.setDeliveryCharge(podi.getDeliveryCharge());
                    prdr.setVatAmount(podi.getVatAmount());
                    prdr.setVatPercent(podi.getVatPercent());
                    prdr.setSubTotal(podi.getSubTotal());
                    prdr.setTotalPrice(podi.getTotalPrice());
                    prdr.setItemQty(podi.getDeliveryQty());
//                    List<ItemInfo> items = pqdi.getCsVendorDetail().getPriceQuotation().getQuotationDetails().stream().map(
//                            q->{
//                               return new ItemInfo(q.getBrandName(),q.getItemAttribute(),q.getExtendedAttributes());
//                            }).collect(Collectors.toList());
//                    String[] summary = pqdi.getSummary().split(",");
                    prdr.setItemName(podi.getItemName());
                    Long warehouseId=null;
                    if(podi.getCsVendorDetail().getCsDetail().getIndentDetail().getIndent()
                            .getSingleWarehouse()!=null) {
                        warehouseId = podi.getCsVendorDetail().getCsDetail().getIndentDetail().getIndent()
                                .getSingleWarehouse().getId();
                    }
                    if(warehouseId==null){
                        warehouseId = podi.getCsVendorDetail().getCsDetail().getIndentDetail().getIndent().getWarehouse().getId();
                    }


                    poRemoteReqDto.setVendorId(podi.getCsVendorDetail().getVendorId());
                    poRemoteReqDto.setOfferId(podi.getCsVendorDetail().getPriceQuotation().getRemoteOfferId());
                    orderDetails.add(prdr);
//                }

            });
            poRemoteReqDto.setOrderDetails(orderDetails);
            remotePos.add(poRemoteReqDto);
        }

        try{
            HttpHeaders headers = new HttpHeaders();
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
            if(orgOp.isPresent()){
                headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String,List<PoRemoteReqDto>> payloadMap = new HashMap<>();
            payloadMap.put("purchaseOrders",remotePos);
            HttpEntity<Map<String,List<PoRemoteReqDto>>> payload = new HttpEntity<>(payloadMap,headers);
            String url = cpsServerConfig.getSentPoEndpoint();
            ResponseEntity<Void> response = networkService.post(url, payload,Void.class);
            if(!response.getStatusCode().equals(HttpStatus.CREATED)){
                throw new RuntimeException("Sorry! Something wrong");
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<PoGroup> poOp  = poGroupRepository.findById(domainId);
        if(poOp.isPresent()){
            PoGroup po = poOp.get();
            po.setReviewerId(reviewer.getId());
            po.setPurchaseOrderStatus(PurchaseOrderStatus.REVIEW);
            po.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
        Optional<PurchaseOrder> poOp = purchaseOrderRepository.findById(domainId);
        if(poOp.isEmpty()){
            throw new RuntimeException("Sorry! PO not found");
        }
        PurchaseOrder po = poOp.get();
        po.setStatus(PurchaseOrderStatus.REJECTED);
    }
}
