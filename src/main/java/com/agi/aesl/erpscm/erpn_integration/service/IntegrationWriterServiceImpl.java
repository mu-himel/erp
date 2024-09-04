package com.agi.aesl.erpscm.erpn_integration.service;

import java.util.*;

import com.agi.aesl.erpscm.account_finance.dto.request.RemoteLedgerAccountDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.erpn_integration.dto.request.PurchaseRequest;
import com.agi.aesl.erpscm.erpn_integration.dto.request.PurchaseRequestItem;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.enums.ItemInactiveStatus;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.network.NetworkService;

@Service
public class IntegrationWriterServiceImpl implements IntegrationWriterService{
    
    @Autowired
    private NetworkService networkService;

    @Value("${app.hr.create.warehouse}")
    private String warehouseCreateEndpoint;

    @Value("${app.hr.update.warehouse}")
    private String warehouseUpdateEndpoint;

    @Value("${app.hr.delete.warehouse}")
    private String warehouseDeleteEndpoint;

    @Value("${app.hr.ledger.item.create}")
    private String ledgerItemCreateEndpoint;

    @Value("${app.hr.purchase_receipt.create}")
    private String purchaseReceivedEndpoint;


    @Value("${service.hr}")
    private String clientId;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Override
    @Transactional
    public void createWarehouse(Jwt token, Warehouse warehouse) {

        HttpHeaders headers = networkService.setHttpHeadersForHr(token);
        Map<String,Object> data = new HashMap<>();
        data.put("warehouseName",warehouse.getName());
        data.put("warehouseLocation",warehouse.getLocation());
        data.put("warehouseId",warehouse.getId());
        HttpEntity<?> payload = new HttpEntity<>(data,headers);
        Optional<?> serviceExist = integrationReaderService.getActiveServiceByClientId(token,clientId);
        if(serviceExist.isPresent()){
            ResponseEntity<Void> response = networkService.post(warehouseCreateEndpoint, payload, Void.class);
            System.out.println(response.getStatusCode());
        }

        
    }

    @Override
    @Transactional
    public void deleteWarehouse(Jwt token, String warehouseName) {
        HttpHeaders headers = networkService.setHttpHeadersForHr(token);
        Map<String,Object> data = new HashMap<>();
        data.put("warehouseName",warehouseName);
        HttpEntity<?> payload = new HttpEntity<>(data,headers);
        Optional<?> serviceExist = integrationReaderService.getActiveServiceByClientId(token,clientId);
        if(serviceExist.isPresent()) {
            networkService.delete(warehouseDeleteEndpoint, payload, Void.class);
        }
        
    }

    @Override
    @Transactional
    public void updateWarehouse(Jwt token, String oldName, Warehouse warehouse) {
        HttpHeaders headers = networkService.setHttpHeadersForHr(token);
        Map<String,Object> data = new HashMap<>();
        data.put("oldName",oldName);
        data.put("warehouseName",warehouse.getName());
        data.put("warehouseLocation",warehouse.getLocation());
        HttpEntity<?> payload = new HttpEntity<>(data,headers);
        Optional<?> serviceExist = integrationReaderService.getActiveServiceByClientId(token,clientId);
        if(serviceExist.isPresent()) {
            networkService.put(warehouseUpdateEndpoint, payload, Void.class);
        }
        
    }

    @Override
    @Transactional
    public void createLedgerItem(Jwt token, LedgerAccount ledgerAccount) {
        HttpHeaders headers = networkService.setHttpHeadersForHr(token);

        Item item = ledgerAccount.getItem();
        ItemCategory category = item.getItemParentCategory();
        ItemCategory subCategory = item.getItemCategory();
        item.setItemInactiveStatus(ItemInactiveStatus.APPROVED);

        RemoteLedgerAccountDto remoteLedgerAccountDto = new RemoteLedgerAccountDto();
        remoteLedgerAccountDto.setItemCode(item.getCode());
        remoteLedgerAccountDto.setUom(item.getItemUnit());
        remoteLedgerAccountDto.setItemName(item.getItemAttributeName());
        remoteLedgerAccountDto.setItemGroup(category.getName());
        remoteLedgerAccountDto.setItemSubGroup(subCategory.getName());
        remoteLedgerAccountDto.setWarehouse(ledgerAccount.getStore());
        remoteLedgerAccountDto.setOpeningCredit(ledgerAccount.getOpeningCreditAmount());
        remoteLedgerAccountDto.setOpeningDebit(ledgerAccount.getOpeningDebitAmount());

        HttpEntity<RemoteLedgerAccountDto> payload = new HttpEntity<>(remoteLedgerAccountDto,headers);
        Optional<?> serviceExist = integrationReaderService.getActiveServiceByClientId(token,clientId);
        System.out.println(ledgerItemCreateEndpoint);
        if(serviceExist.isPresent()) {
            networkService.put(ledgerItemCreateEndpoint, payload, Void.class);
        }else{
            throw new RuntimeException("Sorry! Hr Service not available to create item ledger");
        }
    }

    @Override
    public void purchaseReceived(Jwt token, StoreReceiveNote receiveNote) {

        Optional<?> serviceExist = integrationReaderService.getActiveServiceByClientId(token,clientId);
        if(serviceExist.isPresent()) {
            PurchaseRequest purchaseRequest = new PurchaseRequest();
            purchaseRequest.setSupplierName(receiveNote.getGrn().getVendorEmail());
            purchaseRequest.setCostCenter(receiveNote.getCostCenter());
            List<PurchaseRequestItem> items = new ArrayList<>();
            receiveNote.getSrnDetails().stream().forEach(srnd->{
                PurchaseRequestItem pri = new PurchaseRequestItem();
                pri.setItemCode(srnd.getItem().getCode());
                pri.setAcceptedQty(srnd.getStockInQty());
                pri.setRate(srnd.getGoodReceiveItemDetail().getPricePerUnit());
//                pri.setCostCenter(srnd.getCostCenter());
                items.add(pri);
            });
            purchaseRequest.setItems(items);

            HttpHeaders headers = networkService.setHttpHeadersForHr(token);
            HttpEntity<PurchaseRequest> payload = new HttpEntity<>(purchaseRequest,headers);

            networkService.put(purchaseReceivedEndpoint, payload, Void.class);
        }else{
            throw new RuntimeException("Sorry! Hr Service not available to create item ledger");
        }
    }
}
