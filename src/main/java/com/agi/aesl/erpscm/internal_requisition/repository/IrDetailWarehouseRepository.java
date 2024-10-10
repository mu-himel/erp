package com.agi.aesl.erpscm.internal_requisition.repository;

import com.agi.aesl.erpscm.internal_requisition.entity.InternalRequisitionDetailWarehouse;
import com.agi.aesl.erpscm.internal_requisition.entity.StoreIR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IrDetailWarehouseRepository extends JpaRepository<InternalRequisitionDetailWarehouse,Long> {
}
