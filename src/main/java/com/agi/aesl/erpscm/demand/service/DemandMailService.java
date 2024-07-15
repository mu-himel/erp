package com.agi.aesl.erpscm.demand.service;


import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.modules.dto.UserAssignInfo;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.utils.ClaimResolver;

import java.util.List;

public interface DemandMailService {

    String demandDetailMsgTpl = """
            <h5>{mailFor},</h5>
            <p>Greetings from Anwar Enterprise system. There is a pending demand waiting for 
            your {actionType}</p>
            <p>Initiator Name: {initiatorName}</p>
            <div>{itemList}</div>
            <p>Demand Initiate Date: {demandDate}</p>
            <p><strong>Demand Link: <a href="{demandViewLink}"><u>Click here to view the full demand</u></a></strong></p>
            <p style="color:#ff0000">N.B. This is a system generated email. Please do not reply!</p>
            """;

    void prepareMailContent(String name, String actionType, Demand demand);
    void prepareMailContentForInitiator(String name, String actionType, Demand demand);

    void sentMail(String to, String subject);

    void setClaimResolver(ClaimResolver claimResolver);

    void setDemand(Demand demand);

    List<UserAssignInfo> getStoreUsers(String uri);
}
