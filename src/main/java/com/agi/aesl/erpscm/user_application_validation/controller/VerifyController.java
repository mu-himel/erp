package com.agi.aesl.erpscm.user_application_validation.controller;

import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.cs.service.CsService;
import com.agi.aesl.erpscm.indent.service.IndentService;
import com.agi.aesl.erpscm.internal_requisition.service.IrService;
import com.agi.aesl.erpscm.internal_requisition.service.IrStoreService;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryCategoryRequestService;
import com.agi.aesl.erpscm.inventory.user_request.service.InventoryRequestService;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.service.PurchaseOrderService;
import com.agi.aesl.erpscm.quality_control.service.QcService;
import com.agi.aesl.erpscm.store_receive.service.SrnService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.VerifyDto;
import org.springframework.beans.factory.annotation.Autowired;
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
public class VerifyController extends BaseController{

    @Autowired
    private UserApplicationValidatorService<?> verificationService;

    @Autowired
    private DemandService demandService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private IndentService indentService;

    @Autowired
    private CsService csService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private QcService qcService;

    @Autowired
    private SrnService srnService;

    @Autowired
    private InventoryCategoryRequestService categoryRequestService;

    @Autowired
    private InventoryRequestService inventoryRequestService;

    @Autowired
    private IrService irService;

    @Autowired
    private IrStoreService irStoreService;

    private void setVerifiableServices(){
        verificationService.addVerificationDomainService(DomainType.DEMAND,demandService);
        verificationService.addVerificationDomainService(DomainType.ACCOUNT_LEDGER,accountService);
        verificationService.addVerificationDomainService(DomainType.INDENT,indentService);
        verificationService.addVerificationDomainService(DomainType.CS,csService);
        verificationService.addVerificationDomainService(DomainType.PO,purchaseOrderService);
        verificationService.addVerificationDomainService(DomainType.QC,qcService);
        verificationService.addVerificationDomainService(DomainType.SRN,srnService);
        verificationService.addVerificationDomainService(DomainType.INVENTORY_REQ_CATEGORY,categoryRequestService);
        verificationService.addVerificationDomainService(DomainType.INVENTORY_REQ_SUB_CATEGORY,categoryRequestService);
        verificationService.addVerificationDomainService(DomainType.INVENTORY_REQ_PRODUCT,inventoryRequestService);
        verificationService.addVerificationDomainService(DomainType.IR,irService);
        verificationService.addVerificationDomainService(DomainType.PSIR,irStoreService);
    }
    @PutMapping("/approve")
    public ResponseEntity<?> approve(
            @AuthenticationPrincipal Jwt token,
            @RequestBody ApproveDto approveDto){
        setVerifiableServices();
        verificationService.approve(token, approveDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/verify")
    public ResponseEntity<?> verify(
            @AuthenticationPrincipal Jwt token,
            @RequestBody VerifyDto verifyDto){
        setVerifiableServices();
        verificationService.verify(token, verifyDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/review")
    public ResponseEntity<?> review(@RequestBody VerifyDto verifyDto){
        setVerifiableServices();
        verificationService.review(verifyDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/reject")
    public ResponseEntity<?> reject(
            @AuthenticationPrincipal Jwt token,
            @RequestBody RejectDto rejectDto){
        setVerifiableServices();
        verificationService.reject(token, rejectDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
