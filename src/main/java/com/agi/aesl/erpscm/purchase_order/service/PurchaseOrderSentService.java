package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.dto.request.PendingItemRequestDto;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.purchase_order.dto.request.PoRemoteDeliveryDetailDto;
import com.agi.aesl.erpscm.purchase_order.dto.request.PoRemoteDetailReqDto;
import com.agi.aesl.erpscm.purchase_order.dto.request.PoRemoteReqDto;
import com.agi.aesl.erpscm.purchase_order.entity.PoGroup;
import com.agi.aesl.erpscm.purchase_order.repository.PurchaseOrderRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.Data;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;

@Data
@Service
public class PurchaseOrderSentService {

    private PurchaseOrderRepository purchaseOrderRepository;
    private ClaimResolver claimResolver;
    private OrgService orgService;
    private CpsServerConfig cpsServerConfig;
    private NetworkService networkService;

    @Async
    public void sentPoToVendors(PoGroup poGroup) {
        // Replace Purchase Order Reference with Po group for this method
        // Get List of Purchase Orders and process sent po to vendor for that collection of po items
        List<PoRemoteReqDto> remotePos = new ArrayList<>();


        List<PurchaseOrderRepository.PurchaseOrderDetailInfo> purchaseOrders = purchaseOrderRepository.findAllByPoGroupId(poGroup.getId());

        for(PurchaseOrderRepository.PurchaseOrderDetailInfo po : purchaseOrders){
            PoRemoteReqDto poRemoteReqDto = new PoRemoteReqDto();
            List<PoRemoteDetailReqDto> orderDetails = new ArrayList<>();
            poRemoteReqDto.setId(po.getId());
            poRemoteReqDto.setPoNo(po.getPoNo());
            poRemoteReqDto.setPoDate(po.getCreatedAt().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            poRemoteReqDto.setCategoryCode(po.getCs().getIndent().getSubCategory().getCode().substring(2));
            poRemoteReqDto.setTenderNo(po.getCs().getIndent().getIndentNo());
            poRemoteReqDto.setDeliveryChargeType(po.getDeliveryChargeType());
            poRemoteReqDto.setDeliveryDate(po.getPoDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            po.getPurchaseOrderDetails().forEach(podi->{
                PoRemoteDetailReqDto prdr = new PoRemoteDetailReqDto();
                List<PoRemoteDeliveryDetailDto> prdds = new ArrayList<>();
                podi.getWarehouseDetailList().forEach(wd->{
                    PoRemoteDeliveryDetailDto prdd = new PoRemoteDeliveryDetailDto();
                    prdd.setItemQty(wd.getQty());
                    prdd.setDeliveryCharge(wd.getDeliveryCharge());
                    prdd.setWarehouse(new ReferenceObjectDto(wd.getWarehouse().getId()));
                    prdds.add(prdd);
                });
                prdr.setPoDeliveryDetailsDtoList(prdds);
                prdr.setDeliveryCharge(podi.getDeliveryCharge());
                prdr.setVatAmount(podi.getVatAmount());
                prdr.setVatPercent(podi.getVatPercent());
                prdr.setSubTotal(podi.getSubTotal());
                prdr.setTotalPrice(podi.getTotalPrice());
                prdr.setItemQty(podi.getDeliveryQty());

                prdr.setItemName(podi.getItemName());
                Long warehouseId=null;
                if(podi.getCsVendorDetail().getCsDetail().getIndentDetail().getIndent()
                        .getSingleWarehouse()!=null) {
                    warehouseId = podi.getCsVendorDetail().getCsDetail().getIndentDetail().getIndent()
                            .getSingleWarehouse().getId();
                }
                if(warehouseId==null){
                    warehouseId = podi.getCsVendorDetail().getCsDetail().getIndentDetail().getIndent().getWarehouse().getId();
                }


                poRemoteReqDto.setVendorId(podi.getCsVendorDetail().getVendorId());
                poRemoteReqDto.setOfferId(podi.getCsVendorDetail().getPriceQuotation().getRemoteOfferId());
                orderDetails.add(prdr);


            });
            poRemoteReqDto.setOrderDetails(orderDetails);
            remotePos.add(poRemoteReqDto);
        }

        try{
            HttpHeaders headers = new HttpHeaders();
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
            orgOp.ifPresent(org->
                    headers.set("orgId",org.getCpsVendorRegistrationId().toString())
            );
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String,List<PoRemoteReqDto>> payloadMap = new HashMap<>();
            payloadMap.put("purchaseOrders",remotePos);
            HttpEntity<Map<String,List<PoRemoteReqDto>>> payload = new HttpEntity<>(payloadMap,headers);
            String url = cpsServerConfig.getSentPoEndpoint();
            ResponseEntity<Void> response = networkService.post(url, payload,Void.class);
            if(!response.getStatusCode().equals(HttpStatus.CREATED)){
                throw new AesException("Sorry! Something wrong");
            }
        }catch(Exception ex){
            throw new AesException(ex.getMessage());
        }
    }

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
