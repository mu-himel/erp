package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.modules.dto.UserAssignInfo;
import com.agi.aesl.erpscm.user_application_validation.service.VerifierMailService;
import com.agi.aesl.erpscm.utils.ClaimResolver;

import java.util.List;

public interface QcMailService extends VerifierMailService<GoodReceiveNote> {

    String REMOVED= """
            <p>Demand Initiate Date: {demandDate}</p>
            <p><strong>Demand Link: <a href="{demandViewLink}"><u>Click here to view the full demand</u></a></strong></p>
            """;
    String QC_MAIL_TPL = """
            <h5>{mailFor},</h5>
            <p>Greetings from Anwar Enterprise system. There is a pending QC waiting for 
            you.</p>
            <p>Initiator Name: {initiatorName}</p>
            <div>{itemList}</div>
            
            <p style="color:#ff0000">N.B. This is a system generated email. Please do not reply!</p>
            """;

    void prepareMailContentForInitiator(String name, String actionType, GoodReceiveNote qualityControl);

    void setClaimResolver(ClaimResolver claimResolver);

    void setQualityControl(GoodReceiveNote qualityControl);

    List<UserAssignInfo> getAuthorizedUsers(String uri);

}
