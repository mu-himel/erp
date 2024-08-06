package com.agi.aesl.erpscm.quality_control.repository;

import com.agi.aesl.erpscm.quality_control.entity.QualityControl;
import com.agi.aesl.erpscm.quality_control.entity.QualityControlKpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QcRepository extends JpaRepository<QualityControl,Long>,QcQuery {

    @Query(value = qcResultByGrnId,nativeQuery = true)
    List<QcResultItem> getQcResultByGrn(Long id);
}
