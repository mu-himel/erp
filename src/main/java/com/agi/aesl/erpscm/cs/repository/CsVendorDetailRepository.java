package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsVendorDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CsVendorDetailRepository extends JpaRepository<CsVendorDetail,Long> {
    Optional<CsVendorDetail> findByCsDetailIdAndVendorId(Long csDetailId, Long vendorId);
}
