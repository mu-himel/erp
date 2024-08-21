package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.modules.dto.UserAssignInfo;
import com.agi.aesl.erpscm.quality_control.entity.QualityControl;
import com.agi.aesl.erpscm.user_application_validation.service.VerifierMailService;
import com.agi.aesl.erpscm.utils.ClaimResolver;

import java.util.List;

public interface QcMailService extends VerifierMailService<QualityControl> {

    String removed= """
            <p>Demand Initiate Date: {demandDate}</p>
            <p><strong>Demand Link: <a href="{demandViewLink}"><u>Click here to view the full demand</u></a></strong></p>
            """;
    String qcMailTpl = """
            <h5>{mailFor},</h5>
            <p>Greetings from Anwar Enterprise system. There is a pending QC waiting for 
            you.</p>
            <p>Initiator Name: {initiatorName}</p>
            <div>{itemList}</div>
            
            <p style="color:#ff0000">N.B. This is a system generated email. Please do not reply!</p>
            """;

    void prepareMailContentForInitiator(String name, String actionType, QualityControl qualityControl);

    void setClaimResolver(ClaimResolver claimResolver);

    void setQualityControl(QualityControl qualityControl);

    List<UserAssignInfo> getAuthorizedUsers(String uri);

}
