package com.agi.aesl.erpscm.demand.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.enums.DemandPriority;
import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import com.agi.aesl.erpscm.inventory.enums.ItemUnit;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository.ItemCategoryInfo;
import com.fasterxml.jackson.annotation.JsonFormat;


@Repository
public interface DemandRepository extends JpaRepository<Demand,Long>, DemandQuery{
    
    @Query("SELECT max(d.id) FROM Demand d")
    Optional<Long> findMaxOrderById();

    @Query(value = demandDetailQuery,nativeQuery = true)
    List<DemandDetailItem> findByDemandId(Long id);


    interface DemandDetailItem{
        Long getId();
        Long getDemandId();
        Long getDemandDetailId();
        Long getItemCategoryId();
        Long getItemParentCategoryId();
        String getCategory();
        String getParentCategory();
        String getCategoryCode();
        String getReceiveNote();
        String getStoreNote();
        String getDeclineNote();
        Long getWarehouseId();
        String getWarehouseName();
        String getWarehouseLocation();
        String getParentCategoryCode();

        Long getBrandId();
        String getBrandName();

        String getAttributeTypes();
        String getAttributeValues();
        String getName();
        String getCode();
        String getSpecification();
        Integer getRequestQuantity();
        Integer getApprovedQuantity();
        Integer getCurrentStockQty();
        Integer getStockThresholdQty();
        Integer getInTransit();
        LocalDateTime getDemandDate();
        String getDemandNo();
        DemandStatus getDemandStatus();
        DemandPriority getDemandPriority();
        DemandStatus getDemandDetailStatus();
        ItemUnit getItemUnit();
        Integer getTotalStockInCurrentMonth();
        Integer getTotalConsumeInCurrentMonth();
        BigDecimal getAvgTotalConsumeInCurrentMonth();
        Long getEmpId();
        String getEmployeeId();
        String getEmployeeName();
        String getReportingManager();
        String getDepartment();
        String getDesignation();
        Long getPrQty();
        Long getOpenPrQty();
        Long getNextVerifierId();
        Long getNextApproverId();
        Long getReviewerId();
    }


    @Query(value = myDemandSql, countQuery = countMyDemandSql)
    Page<DemandListInfo> findAllByRequestedById(String id, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);


    interface DemandListInfo{

        Long getId();
        String getDemandNo();
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getDemandDate();
        DemandStatus getStatus();

        ItemCategoryInfo getCategory();

        Long getItemsQty();

        String getRequestedBy();
    }

    @Query(value = getAllPending, countQuery = countAllPending)
    Page<?> findAllDemands(Long warehouseId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

}
