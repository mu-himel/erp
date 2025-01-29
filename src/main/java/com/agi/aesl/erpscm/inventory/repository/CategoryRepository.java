package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<ItemCategory, Long>, CategoryQuery {


    @Query(value = getCategoriesWithSearch,countQuery = countCategoriesWithSearch,nativeQuery = true)
    Page<ItemCategoryInfoExt> findAllByYear(
            @Param("name") String name,
            @Param("code") String code,
            @Param("currentYearBudget") BigDecimal currentYearBudget,
            @Param("productCount") Long productCurrent,
            @Param("year") Integer year,
            @Param("warehouseId") List<Long> warehouseId,
            @Param("warehouseStoreId") Long warehouseStoreId,
            Pageable pageable);


    Optional<ItemCategory> findByCode(String code);


    @Query("SELECT ic FROM ItemCategory ic LEFT JOIN FETCH ic.budgets b " +
            "WHERE ic.id=:id and b.category.id=:id and (:year IS NULL OR b.currentYear<=:year) " +
            "GROUP BY ic.id")
    Optional<ItemCategory> findById(@Param("id") Long id, @Param("year") Integer Year);



    @Query("SELECT ic FROM ItemCategory ic " +
            "WHERE ic.id=:id AND ic.active=false " +
            "GROUP BY ic.id")
    Optional<ItemCategory> findPendingCategoryById(@Param("id") Long id);

    @Query(value = "SELECT ic FROM ItemCategory ic WHERE ic.id=:id")
    Optional<ItemCategory> findAnyCategoryById(Long id);


    @Query(value = getSubCategoriesWithSearch,countQuery = countSubCategoriesWithSearch,nativeQuery = true)
    Page<SubCategoryInfoExt> findAllSubCategories(
            @Param("name") String name,
            @Param("code") String code,
            @Param("currentYearBudget") BigDecimal currentYearBudget,
            @Param("productCount") Long productCurrent,
            @Param("categoryId") Long categoryId,
            @Param("year") Integer year,
            @Param("warehouseId") List<Long> warehouseId,
            @Param("warehouseStoreId") Long warehouseStoreId,
            Pageable pageable);

    @Query(value = "select ic.id,ic.name,pc.name as mainCategoryName,ic.code, sum(amount) currentYearBudget," +
            "(select count(i.id) from scm_items i where i.item_category_id in (ic.id)) as productCount " +
            "FROM scm_item_categories ic\n" +
            " LEFT JOIN scm_item_categories as pc on pc.id = ic.parent_category_id" +
            " LEFT JOIN scm_category_budgets cb on ic.id = cb.category_id \n" +
            "WHERE ic.parent_category_id =:parentCategoryId AND cb.current_year=:year " +
            "GROUP BY ic.id",nativeQuery = true)
    Page<SubCategoryInfoExt> findAllSubCategories(@Param("parentCategoryId") Long id,
                                                   @Param("year") Integer year, Pageable pageable);


    @Query(value = """
                SELECT COUNT(*) FROM (
                    SELECT ic.id FROM scm_item_categories ic
                    LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
                    WHERE 
                        ic.category_status IN ('APPROVED') 
                        AND ic.active=:active 
                        AND ic.parent_category_id IN (:itemCategoryId)
                    AND cws.warehouse_id IN (:warehouseId) 
                    AND cws.warehouse_store_id IN (:storeId)
                ) as total
            """, nativeQuery = true)
    Optional<Long> countAllByParentCategoryAndActive(Long itemCategoryId,Long warehouseId, Long storeId,
                                                     Boolean active);

    boolean existsByCode(String code);

    @Query(value = "SELECT ic.cps_category_id as cpsCategoryId, ic.id as id, ic.name as name, ic.code as code, " +
            "ic.active as active,cws.warehouse_id as warehouseId, cws.warehouse_store_id as storeId " +
            "FROM scm_item_categories ic " +
            "LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id " +
            "WHERE ic.active = 1 AND ic.category_status IN ('APPROVED') AND ic.parent_category_id IS NULL " +
            " AND (:name IS NULL OR ic.name LIKE concat(:name,'%')) " +
            " AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))" +
            " AND (:warehouseId IS NULL OR cws.warehouse_id IN (:warehouseId)) " +
            " AND (:warehouseStoreId IS NULL OR cws.warehouse_store_id IN (:warehouseStoreId)) " +
            "GROUP BY ic.id ", nativeQuery = true)
    List<MainCategoriesInfo> findAllMainCategories(Long warehouseId,Long warehouseStoreId, String name, String code);

    @Query(value = getMainCategoriesForInventoryControl,countQuery = countMainCategoriesForInventoryControl, nativeQuery = true)
    Page<ItemCategoryInfo> findAllMainCategoriesForInventoryControl(List<Long> warehouseId,Long warehouseStoreId, String name, String code,Pageable pageable);
    @Query(value = """
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses,
            ic.cps_category_id as cpsCategoryId,
            (SELECT COUNT(*) FROM scm_item_categories subCat 
            LEFT JOIN scm_category_warehouse_stores subCws ON subCws.category_id=subCat.id
            WHERE subCat.active=1 AND subCat.parent_category_id = ic.id
            AND (:warehouseId IS NULL OR subCws.warehouse_id = :warehouseId)
            AND (:warehouseStoreId IS NULL OR subCws.warehouse_store_id = :warehouseStoreId)
            ) as subcategoryCount
            FROM scm_item_categories ic
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            WHERE ic.parent_category_id IS NULL AND ic.active=true AND ic.cps_category_id IS NOT NULL
            AND ic.category_status IN ('APPROVED')
            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
            AND (:warehouseStoreId IS NULL OR cws.warehouse_store_id = :warehouseStoreId)
            AND (:name IS NULL OR ic.name LIKE concat(:name,'%'))
            AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))
            GROUP BY ic.id
            """, nativeQuery = true)
    List<ItemCategoryInfo> findAllMainCategoriesForInventoryControl(List<Long> warehouseId, Long warehouseStoreId,
                                                                    String name, String code);

    @Query(value = """
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ipc.name as parentCategoryName, ipc.code as parentCategoryCode,
            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses
            FROM scm_item_categories ic
            LEFT JOIN scm_item_categories ipc ON ipc.id = ic.parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            WHERE ic.parent_category_id IS NOT NULL AND ic.cps_category_id IS NOT NULL
            AND ic.active=true
            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
            AND (:storeId IS NULL OR cws.warehouse_store_id = :storeId)
            AND (COALESCE(:parentCategoryId) IS NULL OR ic.parent_category_id IN (:parentCategoryId))
            AND (:name IS NULL OR LOWER(ic.name) LIKE concat('%',LOWER(:name),'%'))
            AND (:code IS NULL OR LOWER(ic.code) LIKE concat('%',LOWER(:code),'%'))
            GROUP BY ic.id
            """,nativeQuery = true)
    List<ItemCategoryInfo> findAllSubCategoriesForInventoryControl(
            List<Long> parentCategoryId,
            List<Long> warehouseId,
            Long storeId,
            String name, String code);

    @Query(value = getSubCategoriesForInventoryControl, countQuery = countSubCategoriesForInventoryControl,
                nativeQuery = true)
    Page<ItemCategoryInfo> findAllSubCategoriesForInventoryControl(
            List<Long> parentCategoryId,
            List<Long> warehouseId,
            Long storeId,
            String name, String code,Pageable pageable);

    @Query(value = """
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ipc.name as parentCategoryName, ipc.code as parentCategoryCode,
            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses
            FROM scm_item_categories ic
            LEFT JOIN scm_item_categories ipc ON ipc.id = ic.parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            WHERE ic.parent_category_id IS NOT NULL AND ic.cps_category_id IS NOT NULL
            AND ic.active=false
            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
            AND (:storeId IS NULL OR cws.warehouse_store_id = :storeId)
            AND (:parentCategoryId IS NULL OR ic.parent_category_id = :parentCategoryId)
            AND (:name IS NULL OR LOWER(ic.name) LIKE concat('%',LOWER(:name),'%'))
            AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))
            GROUP BY ic.id
            """,nativeQuery = true)
    List<ItemCategoryInfo> findAllPendingSubCategoriesForInventoryControl(
            Long parentCategoryId,
            List<Long> warehouseId,
            Long storeId,
            String name, String code);

    @Query(value = getPendingSubcategoriesForInventoryControl, countQuery = countPendingSubCategoriesForInventoryControl,
            nativeQuery = true)
    Page<ItemCategoryInfo> findAllPendingSubCategoriesForInventoryControl(
            Long parentCategoryId,
            List<Long> warehouseId,
            Long storeId,
            String name, String code,Pageable pageable);

    @Query(value = findAllSubCategories,nativeQuery = true)
    List<ItemCategoryInfo> findAllSubCategories(
            Long storeId,
            Long parentCategoryId,
            String name, String code);

    @Query(value = "SELECT ic.id as id, ic.name as name, ic.code as code," +
            "ic.active as active FROM ItemCategory ic " +
            "WHERE ic.active = true " +
            " AND (:name IS NULL OR ic.name LIKE concat(:name,'%')) " +
            " AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))")
    List<ItemCategoryInfo> findAllSubCategories(@Param("name") String name, @Param("code") String code);


    @Query("select max(ic.id) from ItemCategory ic")
    Optional<ItemCategory> findMaxOrderById();

    @Query(value = """
            SELECT ic.id as id, ic.name as name, ic.code as code,
            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses,
            ic.cps_category_id as cpsCategoryId,
            (SELECT COUNT(*) FROM scm_item_categories subCat 
            LEFT JOIN scm_category_warehouse_stores subCws ON subCws.category_id=subCat.id
            WHERE subCat.active=0 AND subCat.parent_category_id = ic.id
            AND (:warehouseId IS NULL OR subCws.warehouse_id = :warehouseId)
            AND (:warehouseStoreId IS NULL OR subCws.warehouse_store_id = :warehouseStoreId)
            ) as subcategoryCount
            FROM scm_item_categories ic
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id
            WHERE ic.parent_category_id IS NULL AND ic.active=false AND ic.category_status IN ('PENDING')
            AND ic.cps_category_id IS NOT NULL
            AND (COALESCE(:warehouseId) IS NULL OR cws.warehouse_id IN (:warehouseId))
            AND (:warehouseStoreId IS NULL OR cws.warehouse_store_id = :warehouseStoreId)
            AND (:name IS NULL OR ic.name LIKE concat(:name,'%'))
            AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))
            GROUP BY ic.id
            """, nativeQuery = true)
    List<ItemCategoryInfo> findAllPendingCategories(List<Long> warehouseId,
                                                    Long warehouseStoreId,
                                                    String name, String code);


    @Query(value = getPendingMainCategories,countQuery = countPendingMainCategories,nativeQuery = true)
    Page<ItemCategoryInfo> findAllPendingCategories(List<Long> warehouseId,
                                                    Long warehouseStoreId,
                                                    String name, String code,Pageable pageable);

    @Query(value = "SELECT ic.id as id, ic.name as name, ic.code as code," +
            "            ic.active as active,GROUP_CONCAT(cws.warehouse_id) as warehouses" +
            "            FROM scm_item_categories ic" +
            "            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id=ic.id" +
            "            WHERE ic.parent_category_id IS NOT NULL" +
            "            AND (:categoryCode IS NULL OR ic.code = :categoryCode)" +
            "            AND (:name IS NULL OR ic.name LIKE concat(:name,'%'))" +
            "            AND (:code IS NULL OR ic.code LIKE concat(:code,'%'))" +
            "            AND (:storeId IS NULL OR cws.warehouse_store_id = :storeId)" +
            "            GROUP BY ic.id",nativeQuery = true)
    List<ItemCategoryInfo> findAllSubCategories(Long storeId, String categoryCode, String name, String code);

    Optional<ItemCategory> findByUserCategoryId(Long id);

    @Query(value = getSubCategoriesTemplate, nativeQuery = true)
    List<SubcategoryTemplate> findSubCategoryTemplate(Long categoryId, Long warehouseId, Long warehouseStoreId);

    interface SubcategoryTemplate{
        Long getId();
        Long getWarehouseId();
        String getWarehouseName();
        Long getWarehouseStoreId();
        String getStoreName();
        String getCode();
        String getCategoryName();
        String getSubCategoryName();
        String getBudgetYear();
        BigDecimal getAmount();
    }

    interface MainCategoriesInfo extends ItemCategoryInfo{
        Long getWarehouseId();
        Long getStoreId();
    }
    interface ItemCategoryInfo {
        Long getId();
        String getName();
        String getCode();
        String getParentCategoryCode();
        String getParentCategoryName();
        String getWarehouses();
        String getWarehouseName();
        String getStoreName();
        Long getSubCategoryCount();
        Boolean getActive();
        Long getCpsCategoryId();
    }

    interface ItemCategoryInfo2 {
        Long getId();
        String getName();
        String getCode();
        String getParentCategoryCode();
        String getParentCategoryName();
        String getWarehouses();
        Long getSubcategoryCount();
        Boolean getActive();
        Long getCpsCategoryId();
    }

    interface ItemCategoryInfoExt extends ItemCategoryInfo{


        BigDecimal getCurrentYearBudget();
        Long getProductCount();
        Long getWarehouseId();
        Long getWarehouseStoreId();

    }

    interface SubCategoryInfoExt extends ItemCategoryInfoExt{
        Long getMainCategoryId();
        String getMainCategoryName();
        String getMainCategoryCode();
    }

    Optional<ItemCategory> findByName(String catName);
}
