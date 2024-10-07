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


    @PutMapping("/approve")
    public ResponseEntity<?> approve(
            @AuthenticationPrincipal Jwt token,
            @RequestBody ApproveDto approveDto){
        if(approveDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.approve(token, approveDto);
        }
        if(approveDto.getDomainType().equals(DomainType.ACCOUNT_LEDGER)){
            verificationService.setVerificationDomainService(accountService);
            verificationService.approve(token, approveDto);
        }
        if(approveDto.getDomainType().equals(DomainType.INDENT)){
            verificationService.setVerificationDomainService(indentService);
            verificationService.approve(token, approveDto);
        }
        if(approveDto.getDomainType().equals(DomainType.CS)){
            verificationService.setVerificationDomainService(csService);
            verificationService.approve(token, approveDto);
        }
        if(approveDto.getDomainType().equals(DomainType.PO)){
            verificationService.setVerificationDomainService(purchaseOrderService);
            verificationService.approve(token, approveDto);
        }
        if(approveDto.getDomainType().equals(DomainType.QC)){
            verificationService.setVerificationDomainService(qcService);
            verificationService.approve(token, approveDto);
        }
        if(approveDto.getDomainType().equals(DomainType.SRN)){
            verificationService.setVerificationDomainService(srnService);
            verificationService.approve(token, approveDto);
        }

        if(approveDto.getDomainType().equals(DomainType.INVENTORY_REQ_CATEGORY) ||
        approveDto.getDomainType().equals(DomainType.INVENTORY_REQ_SUB_CATEGORY)){
            verificationService.setVerificationDomainService(categoryRequestService);
            verificationService.approve(token, approveDto);
        }

        if(approveDto.getDomainType().equals(DomainType.INVENTORY_REQ_PRODUCT)){
            verificationService.setVerificationDomainService(inventoryRequestService);
            verificationService.approve(token, approveDto);
        }

        if(approveDto.getDomainType().equals(DomainType.IR)){
            verificationService.setVerificationDomainService(irService);
            verificationService.approve(token, approveDto);
        }

        if(approveDto.getDomainType().equals(DomainType.PSIR)){
            verificationService.setVerificationDomainService(irStoreService);
            verificationService.approve(token, approveDto);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/verify")
    public ResponseEntity<?> verify(
            @AuthenticationPrincipal Jwt token,
            @RequestBody VerifyDto verifyDto){
        if(verifyDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.verify(token, verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.ACCOUNT_LEDGER)){
            verificationService.setVerificationDomainService(accountService);
            verificationService.verify(token, verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.INDENT)){
            verificationService.setVerificationDomainService(indentService);
            verificationService.verify(token, verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.CS)){
            verificationService.setVerificationDomainService(csService);
            verificationService.verify(token, verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.PO)){
            verificationService.setVerificationDomainService(purchaseOrderService);
            verificationService.verify(token, verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.QC)){
            verificationService.setVerificationDomainService(qcService);
            verificationService.verify(token, verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.SRN)){
            verificationService.setVerificationDomainService(srnService);
            verificationService.verify(token, verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.INVENTORY_REQ_CATEGORY)||
                verifyDto.getDomainType().equals(DomainType.INVENTORY_REQ_SUB_CATEGORY)
        ){
            verificationService.setVerificationDomainService(categoryRequestService);
            verificationService.verify(token, verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.INVENTORY_REQ_PRODUCT)
        ){
            verificationService.setVerificationDomainService(inventoryRequestService);
            verificationService.verify(token, verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.IR)){
            verificationService.setVerificationDomainService(irService);
            verificationService.verify(token, verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.PSIR)){
            verificationService.setVerificationDomainService(irStoreService);
            verificationService.verify(token, verifyDto);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/review")
    public ResponseEntity<?> review(@RequestBody VerifyDto verifyDto){
        if(verifyDto.getDomainType().equals(DomainType.DEMAND)){
            verificationService.setVerificationDomainService(demandService);
            verificationService.review(verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.ACCOUNT_LEDGER)){
            verificationService.setVerificationDomainService(accountService);
            verificationService.review(verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.INDENT)){
            verificationService.setVerificationDomainService(indentService);
            verificationService.review(verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.CS)){
            verificationService.setVerificationDomainService(csService);
            verificationService.review(verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.PO)){
            verificationService.setVerificationDomainService(purchaseOrderService);
            verificationService.review(verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.QC)){
            verificationService.setVerificationDomainService(qcService);
            verificationService.review(verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.SRN)){
            verificationService.setVerificationDomainService(srnService);
            verificationService.review(verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.INVENTORY_REQ_CATEGORY)||
            verifyDto.getDomainType().equals(DomainType.INVENTORY_REQ_SUB_CATEGORY)
        ){
            verificationService.setVerificationDomainService(categoryRequestService);
            verificationService.review(verifyDto);
        }
        if(verifyDto.getDomainType().equals(DomainType.INVENTORY_REQ_PRODUCT)
        ){
            verificationService.setVerificationDomainService(inventoryRequestService);
            verificationService.review(verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.IR)){
            verificationService.setVerificationDomainService(irService);
            verificationService.review(verifyDto);
        }

        if(verifyDto.getDomainType().equals(DomainType.PSIR)){
            verificationService.setVerificationDomainService(irStoreService);
            verificationService.review(verifyDto);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/reject")
    public ResponseEntity<?> reject(
            @AuthenticationPrincipal Jwt token,
            @RequestBody RejectDto rejectDto){

        if(rejectDto.getDomainType().equals(DomainType.ACCOUNT_LEDGER)){
            verificationService.setVerificationDomainService(accountService);
            verificationService.reject(token, rejectDto);
        }
        if(rejectDto.getDomainType().equals(DomainType.INDENT)){
            verificationService.setVerificationDomainService(indentService);
            verificationService.reject(token, rejectDto);
        }

        if(rejectDto.getDomainType().equals(DomainType.CS)){
            verificationService.setVerificationDomainService(csService);
            verificationService.reject(token, rejectDto);
        }

        if(rejectDto.getDomainType().equals(DomainType.PO)){
            verificationService.setVerificationDomainService(purchaseOrderService);
            verificationService.reject(token, rejectDto);
        }
        if(rejectDto.getDomainType().equals(DomainType.QC)){
            verificationService.setVerificationDomainService(qcService);
            verificationService.reject(token, rejectDto);
        }
        if(rejectDto.getDomainType().equals(DomainType.SRN)){
            verificationService.setVerificationDomainService(srnService);
            verificationService.reject(token, rejectDto);
        }
        if(rejectDto.getDomainType().equals(DomainType.INVENTORY_REQ_CATEGORY)||
                rejectDto.getDomainType().equals(DomainType.INVENTORY_REQ_SUB_CATEGORY)
        ){
            verificationService.setVerificationDomainService(categoryRequestService);
            verificationService.reject(token, rejectDto);
        }

        if(rejectDto.getDomainType().equals(DomainType.INVENTORY_REQ_PRODUCT)
        ){
            verificationService.setVerificationDomainService(inventoryRequestService);
            verificationService.reject(token, rejectDto);
        }

        if(rejectDto.getDomainType().equals(DomainType.IR)){
            verificationService.setVerificationDomainService(irService);
            verificationService.reject(token, rejectDto);
        }

        if(rejectDto.getDomainType().equals(DomainType.PSIR)){
            verificationService.setVerificationDomainService(irStoreService);
            verificationService.reject(token, rejectDto);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
