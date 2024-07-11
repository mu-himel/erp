package com.agi.aesl.erpscm.demand.service;

import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.entity.DemandDetailAttribute;
import com.agi.aesl.erpscm.email.service.EmailSenderService;
import com.agi.aesl.erpscm.modules.dto.UserAssignInfo;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemandMailServiceImpl implements DemandMailService{

    private String template;

    private  String name;

    private  Demand demand;

    private ClaimResolver claimResolver;

    private List<UserAssignInfo> users = new ArrayList<>();

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private ModuleService moduleService;

    @Value("${scm.frontend.demandDetailLink}")
    private String demandDetailLink;



    @Override
    @Transactional
    public void prepareMailContent(String name, String actionType, Demand demand) {
        template = setMailFor(name);
        processTemplate(actionType,demand);
    }

    @Override
    @Transactional
    public void prepareMailContentForInitiator(String name, String actionType, Demand demand) {
        template = setMailFor(name);
        template.replaceAll("pending demand","pending qc");
        processTemplate(actionType,demand);
    }

    @Transactional
    private void processTemplate(String actionType, Demand demand){
        template = setInitiatorName(
                setActionType(template,actionType),demand.getRequestedBy().getEmployeeName()
        );
        String productDetail = "";
        int i=0;
        for(DemandDetail demandDetail : demand.getDemandDetails()){
            ++i;
            String productName = "<div><h4>Item "+i+"</h4><p style='font-size:15px;font-weight:bold;color:#333;'><span style='color:#cacaca;'>Product</span> ";
            if(demandDetail.getBrand()!=null) {
                productName += demandDetail.getBrand().getName() + " - ";
            }
            productName += generateItemAttribute(demandDetail.getAttributes())+"</p></div>";
            productDetail += productName;
        }

        template = setProductDetail(template,productDetail);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");
        template = setDemandDate(template,demand.getDemandDate().format(dateFormat));
        template = setDemandViewLink(template,demandDetailLink+demand.getId());
    }

    private String setDemandDate(String tmp, String date){
        return tmp.replaceAll("\\{demandDate\\}",date);
    }

    private String setDemandViewLink(String tmp, String link){
        return tmp.replaceAll("\\{demandViewLink\\}",link);
    }

    private String setActionType(String tmp, String actionType){
        return tmp.replaceAll("\\{actionType\\}",(actionType!=null)? actionType:"");
    }

    private String setProductDetail(String tmp, String productDetail){
        return tmp.replaceAll("\\{itemList\\}",productDetail);
    }
    private String setInitiatorName(String tmp, String name){
        return tmp.replaceAll("\\{initiatorName\\}",name);
    }
    private String setMailFor(String mailFor){
        return demandDetailMsgTpl.replaceAll("\\{mailFor\\}",mailFor);
    }

    private String generateItemAttribute(List<DemandDetailAttribute> attributes){
        StringBuilder sb = new StringBuilder();

        attributes.stream().forEach(attribute -> {
            sb.append(attribute.getAttributeType().trim()
                    +" "+attribute.getAttributeValue().trim()
                    +" "+attribute.getAttributeUnit().trim());
            sb.append(" - ");
        });

        return (sb.isEmpty())? "" :  sb.toString().substring(0,sb.length()-3);
    }

    @Override
    @Async
    @Transactional
    public void sentMail(String to, String subject) {
        if(template!=null){
                emailSenderService.refreshRecipient();
                emailSenderService.addRecipient(to);
                emailSenderService.sendEmail(subject,template);
        }else{
            if(this.users.size()>0 && to==null){
                for(UserAssignInfo uai :users){
                    emailSenderService.refreshRecipient();
                    template = setMailFor(uai.getUser().getEmployeeName());
                    emailSenderService.addRecipient(uai.getUser().getEmail());
                    processTemplate(null,demand);
                    emailSenderService.sendEmail(subject,template);
                }
            }
        }
    }

    @Override
    public void setClaimResolver(ClaimResolver claimResolver) {
        this.claimResolver = claimResolver;
    }

    @Override
    @Transactional
    public void setDemand(Demand demand) {
        this.demand = demand;
    }

    @Override
    @Transactional
    public List<UserAssignInfo> getStoreUsers(String uri) {
        this.users = moduleService.getUsersByPermission(claimResolver,uri);
        return this.users;
    }


}
