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

    List<PriceQuotation> findByRfqIdAndVendorId(Long id, Long vendorId);

    @Query(value = """
        select pqd.est_delivery_days as estDeliveryDays, pqdd.warehouse_id as warehouseId
        from price_quotation_details pqd 
        LEFT JOIN price_quotation_delivery_details pqdd ON pqdd.price_quotation_detail_id = pqd.id
        WHERE pqd.price_quotation_id = :pqId
        and pqd.item_attribute = :itemAttribute
            """,nativeQuery = true)
    Optional<PriceQuotationDetailExt> getPriceQuotationDetailByPqIdAndItemAttr(Long pqId, String itemAttribute);

    interface PriceQuotationDetailExt{
        Integer getEstDeliveryDays();
        Long getWarehouseId();
    }

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
