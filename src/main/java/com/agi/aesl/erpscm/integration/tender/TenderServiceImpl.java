package com.agi.aesl.erpscm.integration.tender;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.price_quotation.dto.request.CounterPqDto;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenderServiceImpl implements TenderService{

    @Autowired
    private NetworkService networkService;

    @Autowired
    private OrgService orgService;

    @Autowired
    private CpsServerConfig cpsConfig;

    @Override
    public Optional<Long> sentCounterOffer(ClaimResolver claimResolver, Indent indent, CounterPqDto counterPqDto) {
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        if(orgOp.isPresent()){
            headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
        }
        HttpEntity<CounterPqDto> payload = new HttpEntity<>(counterPqDto,headers);
        String url = cpsConfig.getCounterOfferEndpoint(indent.getIndentNo());
        ResponseEntity<?> response = networkService.put(url, payload, Void.class);
        if(response.getStatusCode()!= HttpStatus.NO_CONTENT){
            throw new RuntimeException("Unable to send Offer to CPS");
        }
        String idStr = response.getHeaders().get("id").get(0);
        return Optional.ofNullable(Long.valueOf(idStr));
    }
}
