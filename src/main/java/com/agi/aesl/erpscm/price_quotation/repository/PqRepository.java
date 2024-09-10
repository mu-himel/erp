package com.agi.aesl.erpscm.price_quotation.repository;

import com.agi.aesl.erpscm.price_quotation.entity.PriceQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PqRepository extends JpaRepository<PriceQuotation,Long>,PqQuery {

    Optional<PriceQuotation> findByRemoteOfferId(Long id);

    @Query(value=prevPriceQuotationByVendorId,nativeQuery = true)
    Optional<PriceQuotationInfo> getPrevPqByVendorId(Long vendorId);

    @Query(value = getPriceQuotationsByIndentId,nativeQuery = true)
    List<PriceQuotationInfo> getPriceQuotationsByIndentId(@Param("indentId") Long id);

    interface PriceQuotationInfo {
        Long getId();
        Long getVendorId();
        String getVendorName();
        Integer getScore();
        String getPaymentMethod();
        Long getRfqQty();
        BigDecimal getTotalPrice();
        String getPriceQuotationStatus();
        String getStatus();
    }
}
