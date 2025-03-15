package com.agi.aesl.erpscm.user_application_validation.controller;

import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.cs.service.CsAccountService;
import com.agi.aesl.erpscm.cs.service.CsService;
import com.agi.aesl.erpscm.indent.service.IndentService;
import com.agi.aesl.erpscm.internal_requisition.service.IrService;
import com.agi.aesl.erpscm.internal_requisition.service.IrStoreService;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryCategoryRequestService;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryRequestService;
import com.agi.aesl.erpscm.purchase_order.service.PurchaseOrderService;
import com.agi.aesl.erpscm.quality_control.service.QcService;
import com.agi.aesl.erpscm.store_receive.service.SrnService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.VerifyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.demand.service.DemandService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.ApproveDto;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;


@RestController
@RequestMapping("/api/v1/verify")
@RequiredArgsConstructor
public class VerifyController extends BaseController{


    private final UserApplicationValidatorService<?> verificationService;


    private final DemandService demandService;


    private final AccountService accountService;


    private final IndentService indentService;


    private final CsService csService;


    private final CsAccountService acsService;


    private final PurchaseOrderService purchaseOrderService;


    private final QcService qcService;


    private final SrnService srnService;


    private final InventoryCategoryRequestService categoryRequestService;


    private final InventoryRequestService inventoryRequestService;


    private final IrService irService;


    private final IrStoreService irStoreService;

    private void setVerifiableServices(){
        verificationService.addVerificationDomainService(DomainType.DEMAND.name(),demandService);
        verificationService.addVerificationDomainService(DomainType.ACCOUNT_LEDGER.name(),accountService);
        verificationService.addVerificationDomainService(DomainType.INDENT.name(),indentService);
        verificationService.addVerificationDomainService(DomainType.CS.name(),csService);
        verificationService.addVerificationDomainService(DomainType.ACS.name(),acsService);
        verificationService.addVerificationDomainService(DomainType.PO.name(),purchaseOrderService);
        verificationService.addVerificationDomainService(DomainType.QC.name(),qcService);
        verificationService.addVerificationDomainService(DomainType.SRN.name(),srnService);
        verificationService.addVerificationDomainService(DomainType.INVENTORY_REQ_CATEGORY.name(),categoryRequestService);
        verificationService.addVerificationDomainService(DomainType.INVENTORY_REQ_SUB_CATEGORY.name(),categoryRequestService);
        verificationService.addVerificationDomainService(DomainType.INVENTORY_REQ_PRODUCT.name(),inventoryRequestService);
        verificationService.addVerificationDomainService(DomainType.IR.name(),irService);
        verificationService.addVerificationDomainService(DomainType.PSIR.name(),irStoreService);
        verificationService.addVerificationDomainService(DomainType.BANK_ACCOUNT.name(),null);
        verificationService.addVerificationDomainService(DomainType.LEDGER_SETUP.name(),null);
        verificationService.addVerificationDomainService(DomainType.PURCHASE_RECEIPT.name(),null);
        verificationService.addVerificationDomainService(DomainType.PURCHASE_VOUCHER.name(),null);
        verificationService.addVerificationDomainService(DomainType.PAYMENT_VOUCHER.name(),null);
    }
    @PutMapping("/approve")
    public ResponseEntity<Void> approve(
            @AuthenticationPrincipal Jwt token,
            @RequestBody ApproveDto approveDto){
        setVerifiableServices();
        verificationService.approve(token, approveDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/verify")
    public ResponseEntity<Void> verify(
            @AuthenticationPrincipal Jwt token,
            @RequestBody VerifyDto verifyDto){
        setVerifiableServices();
        verificationService.verify(token, verifyDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/review")
    public ResponseEntity<Void> review(@RequestBody VerifyDto verifyDto){
        setVerifiableServices();
        verificationService.review(verifyDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal Jwt token,
            @RequestBody RejectDto rejectDto){
        setVerifiableServices();
        verificationService.reject(token, rejectDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
