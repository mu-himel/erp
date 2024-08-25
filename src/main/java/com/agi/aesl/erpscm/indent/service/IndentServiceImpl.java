package com.agi.aesl.erpscm.indent.service;

import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.common.enums.IndentPriority;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.indent.dto.request.IndentRequestDto;
import com.agi.aesl.erpscm.indent.dto.request.MoveIndentRequestDto;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.entity.IndentDeliveryDetail;
import com.agi.aesl.erpscm.indent.entity.IndentDetail;
import com.agi.aesl.erpscm.indent.entity.IndentPartialDelivery;
import com.agi.aesl.erpscm.indent.enums.IndentVerificationStatus;
import com.agi.aesl.erpscm.indent.enums.RfqStatus;
import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.indent.repository.IndentVerificationApprovalRepository;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.pr_indent.repository.PrIndentRepository;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IndentServiceImpl implements IndentService{

    @Autowired
    private IndentRepository indentRepository;

    @Autowired
    private PrIndentRepository prIndentRepository;

    @Autowired
    private IndentVerificationApprovalRepository indentVARepository;

    @Autowired
    private UserApplicationValidatorService<Indent> verificationService;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Autowired
    private ClaimResolver claimResolver;

    private final Integer PAGE_SIZE = 10;

    @Override
    public String getNextIndentNo() {
        return null;
    }

    @Override
    public void createIndent(Jwt token, String uri, IndentRequestDto indentRequestDto) {
        claimResolver.setToken(token);
        List<String> ids = new ArrayList<>();
        ids.add(indentRequestDto.getCategoryId().toString());

        Indent indent = indentRequestDto.getEntity();

        if(claimResolver.getEmployee().isPresent()) {
            indent.setWarehouse(new Warehouse(claimResolver.getEmployee().get().getWarehouseId()));
        }
        if(indentRequestDto.getIsDevliverToSingleWarehouse()){
            indent.setIsDevliverToSingleWarehouse(true);
            indent.setSingleWarehouse(new Warehouse(indentRequestDto.getSingleWarehouse().getId()));
        }

        indent.setCategory(new ItemCategory(indentRequestDto.getCategoryId()));
        indent.setSubCategory(new ItemCategory(indentRequestDto.getSubCategoryId()));
        indent.setIndentNo(getNextIndentNo());
        if(claimResolver.getEmployee().isPresent()) {
            indent.setRequestedBy(new Employee(claimResolver.getEmployee().get().getId()));
        }
        indent.setPriority(IndentPriority.valueOf(indentRequestDto.getPriority()));
        indent.setPriorityDateTime(indentRequestDto.getPriorityDate());


        indent.setIndentDetails(indentRequestDto.getItems().stream().map(item->{

            IndentDetail indentDetail = new IndentDetail();
            indentDetail.setIndent(indent);
            indentDetail.setProductRequirementsIds(item.getProductRequirementsIds());
            indentDetail.setItemAttribute(item.getAttribute());
            indentDetail.setBrandId(item.getBrandId());
            indentDetail.setSubCategory(new ItemCategory(item.getSubCategoryId()));
            ids.add(item.getSubCategoryId().toString());


            indentDetail.setWarehouses(item.getWarehouses().stream().map(w->{
                IndentDeliveryDetail idd = new IndentDeliveryDetail();
                idd.setRfqQty(w.getRfqQty());
                idd.setWarehouse(new Warehouse(w.getWarehouseId()));
                idd.setIndentDetail(indentDetail);
                idd.setOrderQty(w.getOrderQty());
                idd.setPrQty(w.getPrQty());

                idd.setPartialDeliveries(w.getPartialDeliveries().stream().map(pd->{
                    IndentPartialDelivery ipd = new IndentPartialDelivery();
                    ipd.setPdDate(pd.getPdDate());
                    ipd.setQty(pd.getQty());
                    ipd.setIndentDeliveryDetail(idd);
                    return ipd;
                }).collect(Collectors.toList()));
                return idd;
            }).collect(Collectors.toList()));

            return indentDetail;
        }).collect(Collectors.toList()));

        AppliedVADto appliedVa = verificationService.applyVerifyApprovalProcess(indent, DomainType.INDENT, IndentVerificationStatus.APPROVED.toString(),
                uri, "CATEGORY", ids, null);

        if(appliedVa.getVerifiers().isEmpty() && appliedVa.getPanels().isEmpty()){
            indent.setRfqStatus(RfqStatus.INIT);
            indent.setIndentStatus(IndentVerificationStatus.APPROVED);
        }

        indent.setReviewerId(null);
        indent.setReviewDate(null);

        indentRepository.save(indent);

        verificationService.removeVerification(indent.getId(), DomainType.INDENT);
        indentVARepository.deleteAllByIndentId(indent.getId());

    }

    @Override
    public Page<?> getAllIndents(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<Long> categoryId,
                                 Optional<Long> subCategoryId, Optional<String> priority) {
        claimResolver.setToken(token);
        String uri="";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig();

        List<Long> categoryIds = dataFilter.getCategoryIds();
        categoryId.ifPresent(categoryIds::add);
        subCategoryId.ifPresent(categoryIds::add);
        return indentRepository.getAllIndents(categoryIds,warehouseIds,pageable);
    }

    @Override
    public Page<?> getAllPendingVerificationIndents(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                 Optional<String> priority, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        String uri="";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig();
        List<Long> categoryIds = dataFilter.getCategoryIds();
        categoryId.ifPresent(categoryIds::add);
        subCategoryId.ifPresent(categoryIds::add);
        return indentRepository.getAllPendingVerifications(
                claimResolver.getUserId(),
                categoryIds,warehouseIds,pageable);
    }

    @Override
    public Page<?> getAllPendingApprovalIndents(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId, Optional<String> priority, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        String uri = "";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig();
        List<Long> categoryIds = dataFilter.getCategoryIds();
        categoryId.ifPresent(categoryIds::add);
        subCategoryId.ifPresent(categoryIds::add);
        return indentRepository.getAllPendingApprovals(claimResolver.getUserId(),
                categoryIds,warehouseIds,pageable);
    }

    @Override
    public Map<String, Object> getIndentById(Optional<Long> indentId) {
        return null;
    }

    @Override
    public Optional<Indent> getIndentByCode(String code) {
        return Optional.empty();
    }

    @Override
    public Optional<Indent> getIndentById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<?> getIndentByIds(Optional<List<Long>> indentIds) {
        return null;
    }

    @Override
    public int moveIndentByIds(MoveIndentRequestDto moveIndent) {
        return 0;
    }

    @Override
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {

    }

    @Override
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {

    }

    @Override
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {

    }

    @Override
    public void approveComplete(Long id) {

    }

    @Override
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {

    }

    @Override
    public void onRejected(Employee verifier, Long domainId) {

    }
}
