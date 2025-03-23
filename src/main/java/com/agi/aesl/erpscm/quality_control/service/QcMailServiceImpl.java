package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.email.service.EmailSenderService;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.modules.dto.UserAssignInfo;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QcMailServiceImpl implements QcMailService{

    private String template;

    private  String name;

    private GoodReceiveNote qualityControl;

    private ClaimResolver claimResolver;

    private List<UserAssignInfo> users = new ArrayList<>();


    private final EmailSenderService emailSenderService;


    private final ModuleService moduleService;

    @Override
    @Transactional
    public void prepareMailContentForInitiator(String name, String actionType, GoodReceiveNote qualityControl) {
        template = setMailFor(name);
        template = template.replace("pending demand","pending qc");
        processTemplate(actionType,qualityControl);
    }

    @Override
    @Transactional
    public void prepareMailContent(String name, String actionType, GoodReceiveNote domain) {
        template = setMailFor(name);
        processTemplate(actionType,domain);
    }

    private String setMailFor(String mailFor){
        return QC_MAIL_TPL.replaceAll("\\{mailFor\\}",mailFor);
    }

    private String setInitiatorName(String tmp, String name){
        return tmp.replaceAll("\\{initiatorName\\}",name);
    }


    private void processTemplate(String actionType, GoodReceiveNote domain){
        template = setInitiatorName(
                setActionType(template,actionType),domain.getCreatedBy().getEmployeeName()
        );
        String productDetail = "";
        int i=0;
        for(GoodReceiveItemDetail detail : domain.getGoodReceiveItemDetails()){
            ++i;
            String productName = "<div><h4>Item "+i+"</h4><p style='font-size:15px;font-weight:bold;color:#333;'><span style='color:#cacaca;'>Product</span> ";
            if(detail.getItem().getBrand()!=null) {
                productName += detail.getItem().getBrand().getName() + " - ";
            }
            productName += generateItemAttribute(detail.getItem().getAttributes())+"</p></div>";
            productDetail = productDetail.concat(productName);
        }

        template = setProductDetail(template,productDetail);
    }

    private String generateItemAttribute(List<ItemAttribute> attributes){
        StringBuilder sb = new StringBuilder();

        attributes.forEach(attribute -> {
            String s = (attribute.getAttributeType().trim()
                    +" "+attribute.getAttributeValue().trim()
                    +" "+attribute.getAttributeUnit().trim());
            sb.append(s).append(" - ");
        });

        return (sb.isEmpty())? "" :  sb.substring(0,sb.length()-3);
    }

    private String setActionType(String tmp, String actionType){
        return tmp.replace("\\{actionType\\}",(actionType!=null)? actionType:"");
    }

    private String setProductDetail(String tmp, String productDetail){
        return tmp.replace("\\{itemList\\}",productDetail);
    }

    @Override
    public void setClaimResolver(ClaimResolver claimResolver) {
        this.claimResolver = claimResolver;
    }

    @Override
    public void setQualityControl(GoodReceiveNote qualityControl) {
        this.qualityControl = qualityControl;
    }

    @Override
    public List<UserAssignInfo> getAuthorizedUsers(String uri) {
        this.users = moduleService.getUsersByPermission(claimResolver,uri);
        return this.users;
    }

    @Override
    @Async
    @Transactional
    public void sentMail(String to, String subject) {
        if(template!=null){
            emailSenderService.refreshRecipient();
            emailSenderService.addRecipient(to);
//                emailSenderService.sendEmail(subject,template);
        }else{
            if(!this.users.isEmpty() && to==null){
                for(UserAssignInfo uai :users){
                    emailSenderService.refreshRecipient();
                    template = setMailFor(uai.getUser().getEmployeeName());
                    emailSenderService.addRecipient(uai.getUser().getEmail());
                    processTemplate(null,qualityControl);
//                    emailSenderService.sendEmail(subject,template);
                }
            }
        }
    }
}
