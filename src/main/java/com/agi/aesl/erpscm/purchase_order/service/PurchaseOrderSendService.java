package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.dto.request.PendingItemRequestDto;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.Data;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;

import java.util.Optional;

@Data
public class PurchaseOrderSendService {

    private OrgService orgService;
    private ClaimResolver claimResolver;
    private CpsServerConfig cpsServerConfig;
    private NetworkService networkService;

    @Async
    public void sendPendingItemRequest(PendingItemRequestDto payloadDto) {
        try{
            HttpHeaders headers = new HttpHeaders();
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
            orgOp.ifPresent(org->
                    headers.set("orgId",org.getCpsVendorRegistrationId().toString())
            );
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PendingItemRequestDto> payload = new HttpEntity<>(payloadDto,headers);
            String url = cpsServerConfig.getPendingItemReqEndpoint();
            ResponseEntity<Void> response = networkService.post(url, payload,Void.class);
            if(!response.getStatusCode().equals(HttpStatus.CREATED)){
                throw new AesException("Sorry! Something wrong");
            }
        }catch(Exception ex){
            throw new AesException(ex.getMessage());
        }
    }
}
