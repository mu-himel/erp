package com.agi.aesl.erpscm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "cps")
public class CpsServerConfig {
    
    private String orgCode;

    private String host;

    private String tenderEndpoint;

    private String vendorCountEndpoint;
    private String vendorListEndpoint;
    private String counterOfferEndpoint;
    private String lockOfferEndpoint;
    private String declineOfferEndpoint;
    private String awardedOfferEndpoint;

    private String sentPoEndpoint;

    private String orgRegisterEndpoint;

    private String poReceiveEndpoint;
    private String poRejectEndpoint;

    private String poQcPassEndpoint;
    private String poQcFailEndpoint;

    private String itemFetchEndpoint;

    private String pendingItemReqEndpoint;

    private  String itemCategoriesEndpoint;

    private String itemsEndpoint;

    private String erpIpAddress;

    public String getTenderEndpoint(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.tenderEndpoint);
        return sb.toString();
    }

    public String getCounterOfferEndpoint(String rfqNo){
        StringBuilder sb =  new StringBuilder();
        sb.append(this.host).append(this.counterOfferEndpoint.replace("{rfqNo}",rfqNo));
        return sb.toString();
    }

    public String getLockOfferEndpoint(Long offerId, Long vendorId){
        StringBuilder sb =  new StringBuilder();
        sb.append(this.host).append(this.lockOfferEndpoint
            .replace("{remoteOfferId}", offerId.toString())
            .replace("{vendorId}",vendorId.toString()));
        return sb.toString();
    }
    public String getDeclineOfferEndpoint(Long offerId, Long vendorId){
        StringBuilder sb =  new StringBuilder();
        sb.append(this.host).append(this.declineOfferEndpoint
            .replace("{remoteOfferId}", offerId.toString())
            .replace("{vendorId}",vendorId.toString()));
        return sb.toString();
    }

    public String getAwardedOfferEndpoint(Long offerId, Long vendorId){
        StringBuilder sb =  new StringBuilder();
        sb.append(this.host).append(this.awardedOfferEndpoint
                .replace("{remoteOfferId}", offerId.toString())
                .replace("{vendorId}",vendorId.toString()));
        return sb.toString();
    }

    public String getVendorCountEndpoint(String subCatCode){
        StringBuilder sb =  new StringBuilder();
        sb.append(this.host).append(this.vendorCountEndpoint.replace("{subCatCode}",subCatCode));
        return sb.toString();
    }

    public String getVendorListEndpoint(String name){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.vendorListEndpoint);
        if(name!=null){
            sb.append("?name="+name);
        }
        return sb.toString();
    }

    public String getSentPoEndpoint(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.sentPoEndpoint);
        return sb.toString();
    }

    public String getOrgRegisterEndpoint(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.orgRegisterEndpoint);
        return sb.toString();
    }

    public String getPoReceiveEndpoint(String  id){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.poReceiveEndpoint.replace("{id}", id));
        return sb.toString();
    }

    public String getPoRejectEndpoint(String id){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.poRejectEndpoint.replace("{id}", id));
        return sb.toString();
    }

    public String getPoQcPassEndpoint(Long id){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.poQcPassEndpoint.replace("{id}", id.toString()));
        return sb.toString();
    }

    public String getPoQcFailEndpoint(Long id){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.poQcFailEndpoint.replace("{id}", id.toString()));
        return sb.toString();
    }

    public String getItemFetchEndpoint(String subCatCode){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.itemFetchEndpoint.replace("{subCatCode}", subCatCode));
        return sb.toString();
    }

    public String getPendingItemReqEndpoint(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.pendingItemReqEndpoint);
        return sb.toString();
    }

    public String getItemCategoriesEndpoint(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.itemCategoriesEndpoint);
        return sb.toString();
    }

    public String getItemsEndpoint(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.host).append(this.itemsEndpoint);
        return sb.toString();
    }
}
