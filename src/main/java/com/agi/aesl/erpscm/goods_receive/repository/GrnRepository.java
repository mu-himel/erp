package com.agi.aesl.erpscm.goods_receive.repository;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrnRepository extends JpaRepository<GoodReceiveNote, Long>, GrnQuery {

    @Query("select max(g.id) from GoodReceiveNote g")
    Optional<Long> findMaxOrderById();

    @Query(value = getAllGrn,
            countQuery = countAllGrn, nativeQuery = true)
    Page<GoodReceiveNoteInfo> findAllGrn(Pageable pageable,
                                         @Param("fromDate") LocalDateTime fromDate,
                                         @Param("toDate") LocalDateTime toDate
    );

    @Query(value = """
            SELECT grn FROM GoodReceiveNote grn WHERE grn.id=:id
            """)
    Optional<GoodReceiveNoteDetailInfo> findGrnById(Long id);

    @Query(value = getAllGrnByStatus,countQuery = countAllGrnByStatus, nativeQuery = true)
    Page<GoodReceiveNoteInfo> findAllGrnByStatus(String status, LocalDateTime fromDate,
                               LocalDateTime toDate,Pageable pageable);

    Optional<GoodReceiveNote> findByGrnNo(String grnNo);

    interface GoodReceiveNoteDetailInfo{
        Long getId();
        String getGrnNo();
        String getGrnStatus();
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getCreatedAt();

        Boolean getIsReceivedByStore();
        String getIndentNo();
        Long getVendorId();
        String getVendorName();
        GrnMode getGrnMode();
//        PurchaseOrder getPurchaseOrder();
        WarehouseInfo getWarehouse();
        Employee getCreatedBy();

        String getDeliveryCharge();
        String getMushak();

        Integer getDays();
        String getVatOption();

        String getAitOption();
        BigDecimal getTotalPrice();
        BigDecimal getVat();
        BigDecimal getVatPctg();
        BigDecimal getSubTotal();

        BigDecimal getDeliveryChargeAmount();

        List<GoodReceiveNoteItemDetailInfo> getGoodReceiveItemDetails();
    }

    interface WarehouseInfo{
        Long getId();
        String getName();
        String getLocation();
    }
    interface GoodReceiveNoteInfo{
        Long getId();
        LocalDate getCreatedAt();
        String getGrnNo();
        String getIndentNo();
        GrnStatus getGrnStatus();
        String getCategoryName();
        Integer getItems();
        Long getReceivedQty();
        Integer getQcPending();
        Integer getQcPass();
        Integer getQcFail();
        Integer getQcHold();
    }

    interface CategoryInfo{
        Long getId();
        String getName();
        String getCode();
    }
    interface GoodReceiveNoteItemDetailInfo{
        Long getId();
        GrnItemInfo getItem();
        LocalDate getManufactureDate();
        LocalDate getExpireDate();
        BigDecimal getReceiveQty();
        BigDecimal getTotalApprovedQty();
        BigDecimal getTotalDeclinedQty();
        BigDecimal getDeclaredQty();
        BigDecimal getInspectedQty();
        BigDecimal getPricePerUnit();
        WarehouseInfo getWarehouse();

        CategoryInfo getCategory();
        CategoryInfo getSubCategory();
    }

    interface GrnItemInfo{
        Long getId();
        String getCode();
        String getName();

        String getItemAttributeName();

//        List<ItemStock> getStocks();
//        List<ItemAttribute> getAttributes();
//
//         WarehouseStoreInfo getWarehouseStore();
    }

    interface WarehouseStoreInfo{
        Long getId();
        String getStoreName();
    }
}
