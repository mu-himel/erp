package com.agi.aesl.erpscm.integration.tender;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.price_quotation.dto.request.CounterPqDto;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TenderServiceImpl implements TenderService{


    private final NetworkService networkService;


    private final OrgService orgService;


    private final CpsServerConfig cpsConfig;

    @Override
    public Optional<Long> sentCounterOffer(ClaimResolver claimResolver, Indent indent, CounterPqDto counterPqDto) {
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        orgOp.ifPresent(org->
            headers.set("orgId",org.getCpsVendorRegistrationId().toString())
        );
        HttpEntity<CounterPqDto> payload = new HttpEntity<>(counterPqDto,headers);
        String url = cpsConfig.getCounterOfferEndpoint(indent.getIndentNo());
        ResponseEntity<?> response = networkService.put(url, payload, Void.class);
        if(response.getStatusCode()!= HttpStatus.NO_CONTENT){
            throw new AesException("Unable to send Offer to CPS");
        }

        if(response.getHeaders().get("id")!=null){
            List<String> ids = response.getHeaders().get("id");
            if(ids!=null) {
                return Optional.of(Long.valueOf(ids.get(0)));
            }
        }
        return Optional.empty();
    }


}
