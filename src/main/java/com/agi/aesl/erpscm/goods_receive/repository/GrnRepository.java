package com.agi.aesl.erpscm.goods_receive.repository;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    Optional<GoodReceiveNoteDetailInfo> findGrnById(Long id);

    interface GoodReceiveNoteDetailInfo{
        Long getId();
        String getGrnNo();
        String getGrnStatus();
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getCreatedAt();

        Boolean getIsReceivedByStore();
//        PurchaseOrder getPurchaseOrder();
        WarehouseInfo getWarehouse();
        Employee getCreatedBy();
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
        String getPoNo();
        GrnStatus getGrnStatus();
        String getCategoryName();
        Integer getItems();
        Long getReceivedQty();
        Integer getQcPending();
        Integer getQcPass();
        Integer getQcFail();
        Integer getQcHold();
    }

    interface GoodReceiveNoteItemDetailInfo{
        Long getId();
    }
}
