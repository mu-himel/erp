package com.agi.aesl.erpscm.product_requirements.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.product_requirements.entity.ProductRequirement;
import com.agi.aesl.erpscm.product_requirements.enums.ProductRequirementStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.transaction.Transactional;

@Repository
public interface ProductRequirementRepository extends JpaRepository<ProductRequirement,Long>, ProductRequirementQuery{

    @Query(value = getProductRequirementWithSearch,
    countQuery = countProductRequirementWithSearch,
    nativeQuery = true)
    Page<ProductRequirementInfo> findAllProductRequirements(
            Long categoryId, 
            Long subCategoryId, 
            LocalDateTime startDate, 
            LocalDateTime endDate,
            Pageable pageable);

    @Query(value = getProductRequirementViewWithSearch,nativeQuery = true)
    List<ProductRequirementViewInfo> getAllProductRequirementView(Long categoryId, Long subCategoryId);

    @Modifying
    @Query(value = "UPDATE product_requirements pr SET pr.status='OPEN' WHERE pr.id IN :ids",nativeQuery = true)
    void updateStatusByIds(List<Long> ids);


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

        Long getPrQty();

        Long getItemsQty();

        Long getCurrentStock();

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

    @Query(value = getWarehouseRequirements,nativeQuery = true)
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
