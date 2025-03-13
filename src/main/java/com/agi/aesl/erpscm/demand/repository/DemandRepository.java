package com.agi.aesl.erpscm.demand.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.fasterxml.jackson.annotation.JsonFormat;

import static com.agi.aesl.erpscm.demand.repository.DemandQuery.*;


@Repository
public interface DemandRepository extends JpaRepository<Demand,Long>{
    
    @Query("SELECT max(d.id) FROM Demand d")
    Optional<Long> findMaxOrderById();

    @Query(value = DEMAND_DETAIL_QUERY,nativeQuery = true)
    List<DemandDetailItem> findByDemandId(Long id);

    @Query(value = GET_STOCK_BY_SUB_CAT_AND_WAREHOUSE_ID,nativeQuery = true)
    List<SubCatStockInfo> findStockBySubCatAndWarehouse(Long subCategoryId, Long warehouseId);

    interface SubCatStockInfo{
        Long getId();
        String getName();
        BigDecimal getStockQty();
        BigDecimal getInTransit();
    }

    interface DemandDetailItem{
        Long getId();
        Long getDemandId();
        Boolean getIsCanceled();
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
        LocalDate getDeliveryDate();
        String getDaysRemain();
        Long getBrandId();
        String getBrandName();

        String getAttributeTypes();
        String getAttributeValues();
        String getName();
        String getCode();
        String getSpecification();
        BigDecimal getRequestQuantity();
        BigDecimal getApprovedQuantity();
        BigDecimal getCurrentStockQty();
        BigDecimal getStockThresholdQty();
        BigDecimal getInTransit();
        LocalDateTime getDemandDate();
        String getDemandNo();
        DemandStatus getDemandStatus();
        DemandPriority getDemandPriority();
        DemandStatus getDemandDetailStatus();
        String getItemUnit();
        Integer getTotalStockInCurrentMonth();
        Integer getTotalConsumeInCurrentMonth();
        BigDecimal getAvgTotalConsumeInCurrentMonth();
        String getEmpId();
        String getEmployeeId();
        String getEmployeeName();
        String getReportingManager();
        String getDepartment();
        String getDesignation();
        Long getPrQty();
        Long getOpenPrQty();
        String getNextVerifierId();
        String getNextApproverId();
        String getReviewerId();
    }


    @Query(value = MY_DEMANDS, countQuery = COUNT_MY_DEMANDS)
    Page<DemandListInfo> findAllByRequestedById(String id,
                                                String demandNo,Long categoryId,
                                                LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @Query(value = GET_ALL_PENDING, countQuery = COUNT_ALL_PENDING)
    Page<DemandListInfo> findAllDemands(Long warehouseId, String demandNo,
                                        Long categoryId,
                                        LocalDateTime fromDate, LocalDateTime toDate,
                                        Integer daysRemain, Pageable pageable);

    @Query(value = GET_ALL_PENDING_VERIFICATION, countQuery = COUNT_ALL_PENDING_VERIFICATION, nativeQuery = true)
    Page<DemandPendingVerificationApprovalList> findAllDemandsByDemandStatusAndNextVerifierId(
            List<String> pendingVerification, String nextVerifierId,
            String demandNo, Long categoryId,
            LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    interface DemandListInfo{

        Long getId();
        String getDemandNo();
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getDemandDate();
        DemandStatus getStatus();
        ItemCategoryInfo getCategory();
    

        Long getItemsQty();

        Employee getRequestedBy();
        String getDaysRemain();
    }

    /**
     * ItemCategoryInfo
     */
    public interface ItemCategoryInfo {
        Long getId();
        String getName();
        String getCode();
        
    }

    public interface DemandPendingVerificationApprovalList {
        Long getId();
        String getDemandNo();
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getDemandDate();
        DemandStatus getStatus();
        DemandStatus getDemandStatus();
        Long getItemsQty();
        String getCategory();
        String getRequestedBy();
    }


    @Query(value = GET_ALL_CLOSE_DEMANDS, countQuery = COUNT_ALL_CLOSE_DEMANDS)
    Page<DemandListInfo> findAllCloseDemands(Long warehouseId,
                                             String demandNo, Long categoryId,
                                             LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @Query(value = GET_ALL_PENDING_APPROVAL_DEMANDS, countQuery=COUNT_ALL_PENDING_APPROVAL_DEMANDS, nativeQuery = true)
    Page<DemandPendingVerificationApprovalList> findAllDemandsByDemandStatusAndNextApproverId(List<String> demandStatus, String nextApproverId,
            String demandNo, Long categoryId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @Query(value = GET_ALL_DEMANDS_BY_CATEGORY, countQuery = COUNT_ALL_DEMANDS_BY_CATEGORY)
    Page<DemandListInfo> findAllDemandsByCategory(List<Long> categories, List<Long> warehouseId,
                                                  String demandNo, LocalDateTime fromDate,
            LocalDateTime toDate,Integer daysRemain, Pageable pageable);

    @Query(value=GET_ALL_FILTERED_PV_DEMANDS, countQuery = COUNT_ALL_FILTERED_PV_DEMANDS,nativeQuery = true)
    Page<DemandPendingVerificationApprovalList> findAllDemandsByCategoryAndDemandStatusAndNextVerifierId(
            List<Long> categories, String nextVerifierId,String demandNo,
            List<String> pendingVerification, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
    
    @Query(value = GET_ALL_FILTERED_PA_DEMANDS, countQuery = COUNT_ALL_FILTERED_PA_DEMANDS,nativeQuery = true)
    Page<DemandPendingVerificationApprovalList> findAllDemandsByCategoryAndDemandStatusAndNextApproverId(
            List<Long> categoryIds, String nextApproverId,String demandNo,
            List<String> demandStatuses, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

}
