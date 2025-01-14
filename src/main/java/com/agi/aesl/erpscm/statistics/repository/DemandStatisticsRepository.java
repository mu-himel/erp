package com.agi.aesl.erpscm.statistics.repository;

import com.agi.aesl.erpscm.demand.entity.Demand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemandStatisticsRepository extends JpaRepository<Demand,Long> {

    @Query(value = """
            SELECT count(b.pending) as pending, count(c.pendingQc) as inTransit,count(d.rejected) as rejected,
            	count(e.received) as received FROM (
            select sd.id, sdd.id as did ,sd.status
            from scm_demands sd
            left join scm_demand_details sdd on sdd.demand_id = sd.id
            WHERE (:subCategoryId IS NULL OR sdd.item_category_id = :subCategoryId)
            AND (:categoryId IS NULL OR sdd.item_parent_category_id = :categoryId)
            AND (:warehouseId IS NULL OR sd.warehouse_id = :warehouseId)
            AND (COALESCE(:fromDate) IS NULL OR sd.created_at BETWEEN :fromDate AND :toDate)
            GROUP BY sd.id
            ) a
            LEFT JOIN (
            SELECT 'PENDING' as pending ) b ON b.pending = a.status
            LEFT JOIN (
            SELECT 'PENDING_QC' as pendingQc
            ) c ON c.pendingQc = a.status
            LEFT JOIN (
            SELECT 'REJECTED' as rejected
            ) d ON d.rejected = a.status
            LEFT JOIN (
            SELECT 'RECEIVED' as received
            ) e ON e.received = a.status
            """,nativeQuery = true)
    List<DemandStats> getDemandStatistics(Long categoryId, Long subCategoryId,Long warehouseId, LocalDateTime fromDate,
                                          LocalDateTime toDate);
    interface DemandStats{
        Long getPending();
        Long getInTransit();
        Long getRejected();
        Long getReceived();
    }
}
