package com.agi.aesl.erpscm.demand.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.enums.DemandItemStatus;
import com.agi.aesl.erpscm.demand.enums.DemandStatus;

@Repository
public interface DemandDetailRepository extends JpaRepository<DemandDetail,Long>{
    Integer countByStatus(DemandItemStatus pending);
    Integer countByStatusAndDemandId(DemandStatus pendingQc, Long id);
    Optional<DemandDetail> findByItemId(Long id);

    @Query(value="SELECT dd FROM DemandDetail dd WHERE dd.item.id IN :ids")
    List<DemandDetail> findAllByItemId(List<Long> ids);

    @Query(value="SELECT dd FROM DemandDetail dd " +
            "LEFT JOIN dd.demand d " +
            "LEFT JOIN d.warehouse w " +
            " WHERE dd.item.id IN (:ids) AND w.id=:wId")
    List<DemandDetail> findAllByItemIdAndWarehouseId(List<Long> ids, Long wId);
}
