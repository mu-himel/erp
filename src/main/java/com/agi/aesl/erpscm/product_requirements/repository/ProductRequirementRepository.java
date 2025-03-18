package com.agi.aesl.erpscm.product_requirements.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.product_requirements.entity.ProductRequirement;
import com.agi.aesl.erpscm.product_requirements.enums.ProductRequirementStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.transaction.Transactional;

import static com.agi.aesl.erpscm.product_requirements.repository.ProductRequirementQuery.*;

@Repository
public interface ProductRequirementRepository extends JpaRepository<ProductRequirement,Long>{

    @Query(value = GET_PRODUCT_REQUIREMENT_WITH_SEARCH,
    countQuery = COUNT_PRODUCT_REQUIREMENT_WITH_SEARCH,
    nativeQuery = true)
    Page<ProductRequirementInfo> findAllProductRequirements(
            Long categoryId, 
            Long subCategoryId, 
            LocalDateTime startDate, 
            LocalDateTime endDate,
            Integer daysRemain,
            Pageable pageable);

    @Query(value = GET_PRODUCT_REQUIREMENT_VIEW_WITH_SEARCH_V2,nativeQuery = true)
    List<ProductRequirementViewInfoV2> getAllProductRequirementView(Long categoryId, Long subCategoryId);

    @Modifying
    @Query(value = "UPDATE product_requirements pr SET pr.status='OPEN' WHERE pr.id IN :ids",nativeQuery = true)
    void updateStatusByIds(List<Long> ids);

    @Query(value = GET_DEMAND_WITH_SEARCH,
            nativeQuery = true
    )
    List<PrDemandView> getDemandByProductRequirementIds(
            @Param("prIds") List<Long> prIds
    );

    interface PrDemandView{
        Long getId();
        String getDemandNo();
        String getWarehouseName();
        LocalDateTime getDemandDate();
        BigDecimal getItemQty();
        BigDecimal getApprovedQty();
        String getEmployeeName();
        String getDepartmentName();

    }
    interface ProductRequirementInfo {
        String getProductRequirementIds();
        Long getCategoryId();

        String getCategoryName();

        Long getSubCategoryId();

        String getSubCategoryName();

        LocalDateTime getDemandDeadline();

        Long getDaysRemain();

        Long getItemsQty();

    }

    interface ProductRequirementViewInfoV2 {
        String getProductRequirementsIds();
        Long getCategoryId();
        Long getDemandId();
        Long getBrandId();
        String getBrandName();
        String getCategoryName();

        Long getSubCategoryId();

        String getSubCategoryName();

        Long getItemId();

        String getItemName();

        String getItemDescription();

        BigDecimal getPrQty();
        BigDecimal getApprovedQty();

        BigDecimal getItemsQty();

        BigDecimal getCurrentStock();

        BigDecimal getSafetytStock();

        BigDecimal getTransitQty();

        Long getDaysRemain();

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime getDemandDeadline();
        String getDemandPriority();
        String getWarehouses();
        String getWarehouseIds();

    }

    interface ProductRequirementViewInfo {
        String getProductRequirementsIds();
        Long getCategoryId();
        Long getDemandId();
        Long getBrandId();
        String getBrandName();
        String getCategoryName();

        Long getSubCategoryId();

        String getSubCategoryName();

        Long getItemId();

        String getItemName();

        String getItemDescription();

        BigDecimal getPrQty();
        BigDecimal getApprovedQty();

        BigDecimal getItemsQty();

        BigDecimal getCurrentStock();

        Long getSafetytStock();

        Long getTransitQty();

        Long getDaysRemain();

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime getDemandDeadline();
        String getDemandPriority();
        String getWarehouses();
        String getWarehouseIds();

    }

    interface WarehouseRequirement {
        Long getId();
        Long getStockThresholdQty();
        String getItemAttribute();
        Long getWarehouseId();
        Long getWarehouseStoreId();
        String getWarehouseName();
        String getName();
        Integer getStockQty();
        Integer getPrQty();
        Integer getInTransit();
    }

    @Query(value = GET_WAREHOUSE_REQUIREMENTS,nativeQuery = true)
    List<WarehouseRequirement> getWarehouseRequirements(String attribute);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE ProductRequirement p SET p.status = :toStatus 
                WHERE p.status = :fromStatus AND p.category.id = :categoryId 
                    AND p.subCategory.id = :subCategoryId
            """)
    int updateStatusByCategoryAndSubCategory(ProductRequirementStatus toStatus, ProductRequirementStatus fromStatus,
            Long categoryId, Long subCategoryId);
    
}
