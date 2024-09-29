package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsVendorDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CsVendorDetailRepository extends JpaRepository<CsVendorDetail,Long> {
    Optional<CsVendorDetail> findByCsDetailIdAndVendorId(Long csDetailId, Long vendorId);
    @Query(value="""
            SELECT pq.id as id, pqd.item_attribute as itemAttributeName, pqd.brand_name as brandName,
            ic.id as subCatId,
            ic.code as subCategoryCode,
            pqd.extended_attributes as extendedAttributes
            FROM price_quotation_details pqd
            LEFT JOIN price_quotations pq ON pq.id = pqd.price_quotation_id
            LEFT JOIN cs_vendor_details cvd ON cvd.price_quotation_id = pq.id 
            LEFT JOIN cs_details csd ON csd.id = cvd.cs_detail_id
            LEFT JOIN indent_details id ON id.id = csd.indent_detail_id
            LEFT JOIN scm_item_categories ic ON ic.id = id.sub_category_id
            WHERE csd.cs_id = :csId AND pqd.extended_attributes IS NOT NULL
            GROUP BY pqd.brand_name,pqd.brand_name ,pqd.extended_attributes
            """,nativeQuery = true)
    List<PendingItemBrandInfo> getPendingItemAndBrandInfoByCsId(Long csId);

    interface PendingItemBrandInfo{
        Long getId();
        String getItemAttributeName();
        String getExtendedAttributes();
        String getBrandName();
        String getSubCategoryCode();
        Long getSubCatId();

    }
}
