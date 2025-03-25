package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualRequestDto;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.quality_control.service.QcMailService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.Data;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
public class CreateGrnService {

    private ClaimResolver claimResolver;
    private ItemService itemService;
    private GrnRepository grnRepository;
    private QcMailService qcMailService;
    private Employee createdBy;

    public String getNextGrnNumber() {
        Optional<Long> grnOp = grnRepository.findMaxOrderById();
        if(grnOp.isPresent()){
            Long grnNo = grnOp.get();
            Long newGrnNo = grnNo +1;
            return String.format("%05d",newGrnNo);
        }
        return String.format("%05d",1);
    }

    @Transactional
    public String createGrn(Jwt token, GrnManualRequestDto grnManualDto,  GrnMode mode){
        claimResolver.setToken(token);
        String uri = "";

        GoodReceiveNote grn = new GoodReceiveNote();
        grn.setGrnDate(LocalDate.now());
        if(mode.equals(GrnMode.AUTO)) {
            grn.setRemotePoId(grnManualDto.getPoId());
        }
        grn.setGrnStatus(GrnStatus.PENDING);
        grn.setPoNo(grnManualDto.getPoNo());
        grn.setIndentNo(grnManualDto.getIndentNo() );
        grn.setGrnMode(mode);

        grn.setWarehouse(new Warehouse(grnManualDto.getWarehouseId()));
        if(token != null && claimResolver.getEmployee().isPresent()){
            grn.setCreatedBy(createdBy);
        }


        grn.setGoodReceiveItemDetails(grnManualDto.getGrnDetails().stream().map(detailDto->{
                    GoodReceiveItemDetail grid = new GoodReceiveItemDetail();

                    Optional<Item> itemOp = Optional.empty();
                    if(mode.equals(GrnMode.MANUAL)) {
                        itemOp = itemService.getItemDetail(detailDto.getItem().getId());
                    }else if (mode.equals(GrnMode.AUTO)){
                        List<Item> items = itemService.getByCode(detailDto.getItemCode());
                        List<Long> itemIds = items.stream().filter(Item::getActive).map(Item::getId).toList();
                        List<ItemStock> stocks = itemService.getByItemAndWarehouse(itemIds,grnManualDto.getWarehouseId());
                        if(!stocks.isEmpty()){
                            ItemStock stock = stocks.get(0);
                            itemOp = Optional.of(stock.getItem());
                        }
                    }
                    if(itemOp.isPresent()){
                        Item item = itemOp.get();
                        grid.setItem(item);
                        grid.setBrandName(item.getName());
                        grid.setCategory(item.getItemParentCategory());
                        grid.setSubCategory(item.getItemCategory());
                    }
                    grid.setVatAmount(detailDto.getVatAmount());
                    grid.setExpireDate(detailDto.getExpireDate());
                    grid.setManufactureDate(detailDto.getProductionDate());
                    grid.setEstimatedDeliveryDays(detailDto.getEstDeliveryDays());
                    grid.setReceiveQty(detailDto.getOrderQty());
                    grid.setPricePerUnit(detailDto.getPricePerUnit());
                    grid.setDeliveryCharge(detailDto.getDeliveryChargeAmount());
                    grid.setWarehouse(new Warehouse(grnManualDto.getWarehouseId()));
                    grid.setGoodReceiveNote(grn);
                    return grid;
                }).toList()
        );



        grn.setAitOption(grnManualDto.getAitOption());
        grn.setVatOption(grnManualDto.getVatOption());
        grn.setVatType(grnManualDto.getVatType()!=null?grnManualDto.getVatType().toUpperCase():null);
        grn.setVat(grnManualDto.getTotalVat());
        grn.setVatPctg(grnManualDto.getVatPctg());
        grn.setDeliveryChargeAmount(grnManualDto.getDeliveryChargeAmount());
        grn.setDeliveryCharge(grnManualDto.getDeliveryCharge());
        grn.setDays(grnManualDto.getDays());
        grn.setInvoicePath(grnManualDto.getInvoicePath());
        grn.setSubTotal(grnManualDto.getInTotal());
        grn.setTotalPrice(grnManualDto.getTotalPrice());

        grn.setVendorId(grnManualDto.getVendor().getId());
        grn.setVendorName(grnManualDto.getVendor().getName());
        grn.setVendorPhone(grnManualDto.getVendor().getVendorPhone());
        grn.setVendorEmail(grnManualDto.getVendor().getVendorEmail());

        grn.setMushak(grnManualDto.getMushak());
        grn.setPaymentType(grnManualDto.getPayment());
        grn.setGrnNo(getNextGrnNumber());
        grnRepository.save(grn);

        if(qcMailService!=null) {
            qcMailService.setClaimResolver(claimResolver);
            qcMailService.setQualityControl(grn);
            qcMailService.getAuthorizedUsers(uri);
            qcMailService.sentMail(null, "Pending QC");
        }
        return grn.getGrnNo();
    }
}
