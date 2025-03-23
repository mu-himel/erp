package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Data
public class QcSentService {

    private OrgService orgService;
    private CpsServerConfig cpsServerConfig;
    private NetworkService networkService;

    @Transactional
    public void sentQcStatus(String token, Long id, GrnStatus status, List<?> qcDetails, NoteDto noteDto, String qcResult){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token);
        orgOp.ifPresent(organization -> headers.set("orgId", organization.getCpsVendorRegistrationId().toString()));

        Map<String,Object> map = new HashMap<>();
        map.put("note",noteDto.getNote());
        map.put("status",status);
        map.put("qcDetails",qcDetails);
        map.put("qcResult",qcResult);
        HttpEntity<Map<String,Object>> payload = new HttpEntity<>(map,headers);
        String url = (status.equals(GrnStatus.QC_PASS))? cpsServerConfig.getPoQcPassEndpoint(id):
                cpsServerConfig.getPoQcFailEndpoint(id);
        ResponseEntity<?> response = networkService.put(url, payload, Void.class);
        if(response.getStatusCode()!= HttpStatus.NO_CONTENT){
            throw new AesException("Sorry! Something wrong");
        }

    }
}
