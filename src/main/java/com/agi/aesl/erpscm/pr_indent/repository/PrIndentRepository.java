package com.agi.aesl.erpscm.pr_indent.repository;


import com.agi.aesl.erpscm.pr_indent.entity.PrIndent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrIndentRepository extends JpaRepository<PrIndent, Long>, PrIndentQuery {

    @Query(value = getReadyIndentWithSearch,
            countQuery = countReadyPrIndentWithSearch,
            nativeQuery = true
    )
    Page<PrIndentInfo> getAllPrIndents(
            @Param("categoryId") Long categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query(value = getPrIndentByIdWithSearch,
            nativeQuery = true
    )
    List<PrIndentViewInfo> getPrIndentById(
            @Param("id") Long id
    );

    @Query(value = getPrIndentByIdsWithSearch,
            nativeQuery = true
    )
    List<PrIndentViewInfo> getPrIndentByIds(
            @Param("ids") List<Long> ids
    );


    @Modifying
    @Query(value = """
                update pr_indent_warehouses  piw
                set piw.order_qty = :orderQty
                where piw.pr_indent_detail_id = :prIndentDetailId
            """,nativeQuery = true)
    int updatePrIndentDetailsOrderQty(
            @Param("orderQty") Long orderQty,
            @Param("prIndentDetailId") Long prIndentDetailId
    );

    @Modifying
    @Query(value = """
                UPDATE PrIndent 
                SET status = 'CLOSE'
                WHERE id IN (:prIds)
            """)
    int updatePrIndentToClose(@Param("prIds") List<Long> ids);


    interface PrIndentInfo {
        Long getId();

        Long getIndentNo();

        Long getCategoryId();

        String getCategoryName();

        Long getSubCategoryId();

        String getSubCategoryName();

        Long getOrderQty();
        Long getPrQty();
        Long getWarehouseId();

        String getPriority();
        String getProductRequirementIds();

    }

    interface PrIndentViewInfo {
        Long getId();
        Long getPiwId();
        Long getPdId();
        LocalDate getPdDate();
        LocalDateTime getPriorityDate();
        BigDecimal getPdQty();
        Long getPrDetailId();

        Long getCategoryId();
        Long getWarehouseId();

        String getCategoryName();
        String getPrAttribute();

        Long getSubCategoryId();

        String getSubCategoryName();

        Long getItemId();

        String getItemName();
        String getWarehouseName();

        String getItemDescription();

        BigDecimal getOrderQty();

        BigDecimal getPrQty();

        String getPriority();

        Long getCurrentStock();

        Long getSafetyStock();

        BigDecimal getTransitQty();

        Long getDaysRemain();

        String getProductRequirementsIds();
        Long getBrandId();
        String getBrandName();
    }

        
}
