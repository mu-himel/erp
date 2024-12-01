package com.agi.aesl.erpscm.erpn_integration.service;

import java.util.*;


import com.agi.aesl.erpscm.account_finance.dto.request.RemoteLedgerAccDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.cs.entity.CsAccount;
import com.agi.aesl.erpscm.cs.repository.CsAccountRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.dto.request.PurchaseRequest;
import com.agi.aesl.erpscm.erpn_integration.dto.request.PurchaseRequestItem;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.enums.ItemInactiveStatus;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.repository.PurchaseOrderRepository;
import com.agi.aesl.erpscm.store_receive.entity.StoreReceiveNote;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.network.NetworkService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntegrationWriterServiceImpl implements IntegrationWriterService{

    @Autowired
    private ClaimResolver claimResolver;
    @Autowired
    private NetworkService networkService;

    @Autowired
    @Lazy
    private WarehouseService warehouseService;

    @Value("${app.hr.create.warehouse}")
    private String warehouseCreateEndpoint;

    @Value("${app.hr.update.warehouse}")
    private String warehouseUpdateEndpoint;

    @Value("${app.hr.delete.warehouse}")
    private String warehouseDeleteEndpoint;

    @Value("${app.acc.ledger.item.create}")
    private String ledgerItemCreateEndpoint;

    @Value("${app.acc.purchase_voucher.create}")
    private String purchaseReceivedEndpoint;


    @Value("${service.acc}")
    private String clientId;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private CsAccountRepository csAccountRepository;

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
        claimResolver.setToken(token);
        Optional<Employee> employeeOptional = claimResolver.getEmployee();
        if(employeeOptional.isEmpty()){
            throw new RuntimeException("Sorry! required employee profile");
        }
        Employee employee = employeeOptional.get();
        Optional<Warehouse> warehouseOp = warehouseService.getWarehouse(employee.getWarehouseId());
        if(warehouseOp.isEmpty()){
            throw new RuntimeException("Sorry! warehouse not found");
        }
        Warehouse warehouse = warehouseOp.get();

        HttpHeaders headers = networkService.setHttpHeaders(token);

        Item item = ledgerAccount.getItem();
        ItemCategory category = item.getItemParentCategory();
        ItemCategory subCategory = item.getItemCategory();
        item.setItemInactiveStatus(ItemInactiveStatus.APPROVED);

        RemoteLedgerAccDto remoteLedgerAccountDto = new RemoteLedgerAccDto();
        remoteLedgerAccountDto.setWarehouseId(ledgerAccount.getWarehouse().getId());
        remoteLedgerAccountDto.setItemId(item.getId());
        remoteLedgerAccountDto.setItemCode(item.getCode());
        remoteLedgerAccountDto.setBrandName(item.getName());
        remoteLedgerAccountDto.setAtrName(item.getItemAttributeName());
        remoteLedgerAccountDto.setCategoryId(category.getId());
        remoteLedgerAccountDto.setCategory(category.getName());
        remoteLedgerAccountDto.setCategoryCode(category.getCode());
        remoteLedgerAccountDto.setSubCategoryId(subCategory.getId());
        remoteLedgerAccountDto.setSubCategory(subCategory.getName());
        remoteLedgerAccountDto.setSubCategoryCode(subCategory.getCode());
//        LedgerInitiatorDto ledgerInitiatorDto = new LedgerInitiatorDto();
//        ledgerInitiatorDto.setEmployeeId(employee.getEmployeeId());
//        ledgerInitiatorDto.setEmployeeName(employee.getEmployeeName());
//        ledgerInitiatorDto.setEmployeeDepartment(employee.getDepartmentName());
//        ledgerInitiatorDto.setEmployeeDesignation(employee.getDesignationName());
//        ledgerInitiatorDto.setReportingManager(employee.getReportingManager());
//        ledgerInitiatorDto.setEmployeeWarehouse(employee.getWarehouseName());
//        ledgerInitiatorDto.setWarehouseLocation(warehouse.getLocation());
//        remoteLedgerAccountDto.setInitiatorDetailsDto(ledgerInitiatorDto);

//        remoteLedgerAccountDto.setUom(item.getItemUnit());
//        remoteLedgerAccountDto.setItemName(item.getItemAttributeName());
//        remoteLedgerAccountDto.setItemGroup(category.getName());
//        remoteLedgerAccountDto.setItemSubGroup(subCategory.getName());
//        remoteLedgerAccountDto.setWarehouse(ledgerAccount.getStore());
//        remoteLedgerAccountDto.setOpeningCredit(ledgerAccount.getOpeningCreditAmount());
//        remoteLedgerAccountDto.setOpeningDebit(ledgerAccount.getOpeningDebitAmount());

        HttpEntity<RemoteLedgerAccDto> payload = new HttpEntity<>(remoteLedgerAccountDto,headers);
        Optional<?> serviceExist = integrationReaderService.getActiveServiceByClientId(token,clientId);

        if(serviceExist.isPresent()) {
            networkService.post(ledgerItemCreateEndpoint, payload, Void.class);
        }
    }

    @Override
    @Transactional
    public void purchaseReceived(Jwt token, StoreReceiveNote receiveNote) {
        Optional<?> serviceExist = integrationReaderService.getActiveServiceByClientId(token,clientId);
        if(serviceExist.isPresent()) {
            Optional<PurchaseOrder> poOp = purchaseOrderRepository.findById(receiveNote.getGrn().getRemotePoId());
            Optional<CsAccount> csAccountOp=Optional.empty();
            if(poOp.isPresent()){
                csAccountOp = csAccountRepository.findByCsId(poOp.get().getPoGroup().getCs().getId());
            }
            if(csAccountOp.isEmpty()){
                throw new RuntimeException("Sorry! Vat Type not found in Account Cs");
            }
            PurchaseRequest purchaseRequest = new PurchaseRequest();
            purchaseRequest.setSrnNo(receiveNote.getSrnNo());
            GoodReceiveNote grn = receiveNote.getGrn();
            purchaseRequest.setVendorCpsId(grn.getVendorId().toString());
            List<PurchaseRequestItem> items = new ArrayList<>();

            purchaseRequest.setVatType(csAccountOp.get().getVatType().replaceAll("_","").trim().toUpperCase());
            purchaseRequest.setInvoice(grn.getInvoicePath());
            receiveNote.getSrnDetails().stream().forEach(srnd->{
                Item item = srnd.getItem();
                Optional<GoodReceiveItemDetail> grndetailOp = grn.getGoodReceiveItemDetails().stream().filter(grnd->grnd.getItem().getId().equals(item.getId())).findFirst();

                PurchaseRequestItem pri = new PurchaseRequestItem();
                pri.setItemCode(srnd.getItem().getCode());
                pri.setQty(srnd.getStockInQty());
                pri.setTransactionType(grn.getPaymentType());
                pri.setCreditDays(grn.getDays().toString());
                pri.setPricePerUnit(srnd.getGoodReceiveItemDetail().getPricePerUnit());
                if(grndetailOp.isPresent()){
                    pri.setEstDeliveryTime(grndetailOp.get().getEstimatedDeliveryDays().toString());
                    pri.setVat(grn.getVat());
                    pri.setDeliveryCharge(grn.getDeliveryChargeAmount());
                }
                items.add(pri);
            });
            purchaseRequest.setItemList(items);

            HttpHeaders headers = networkService.setHttpHeaders(token);
            HttpEntity<PurchaseRequest> payload = new HttpEntity<>(purchaseRequest,headers);
            System.out.println(purchaseReceivedEndpoint);
            networkService.post(purchaseReceivedEndpoint, payload, Void.class);
        }
    }
}
