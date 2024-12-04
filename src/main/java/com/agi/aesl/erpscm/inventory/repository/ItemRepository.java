package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemFunctionalUnit;
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long>,ItemQuery {

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.itemCategory ic " +
            "LEFT JOIN FETCH i.itemParentCategory ipc " +
            "LEFT JOIN FETCH ic.parentCategory pc WHERE i.id=:id")
    Optional<Item> findById(@Param("id") Long id);

    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.itemCategory ic " +
            "LEFT JOIN  i.itemParentCategory ipc " +
            "LEFT JOIN  i.stocks s "+
            "LEFT JOIN  ic.parentCategory pc " +
            "LEFT JOIN  ic.attributes a " +
            "WHERE i.id=:id")
    Optional<ItemDetail> findByIdWithWarehouse(@Param("id") Long id);


    @Query(value = getItemsWithSearch,
            countQuery = countItemsWithSearch, nativeQuery = true)
    Page<PageItemList> findAllItems(
            @Param("name") String name,
            @Param("code") String code,
            @Param("reorderPercentage") Integer reorderPercentage,
            @Param("stockThresholdQty") Integer stockThresholdQty,
            @Param("categoryId") List<Long> categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("warehouseId") List<Long> warehouseId,
            @Param("warehouseStoreId") Long warehouseStoreId,
            Pageable pageable
    );

    @Query(value = getItemsWithSearch,nativeQuery = true)
    List<PageItemList> findAllItemList(
            @Param("name") String name,
            @Param("code") String code,
            @Param("reorderPercentage") Integer reorderPercentage,
            @Param("stockThresholdQty") Integer stockThresholdQty,
            @Param("categoryId") List<Long> categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("warehouseId") List<Long> warehouseId,
            @Param("warehouseStoreId") Long warehouseStoreId
    );

    @Query(value = getPendingItemsWithSearch,
            countQuery = countAllPendingItems, nativeQuery = true)
    Page<PageItemList> findAllPendingItems(
            @Param("name") String name,
            @Param("code") String code,
            @Param("reorderPercentage") Integer reorderPercentage,
            @Param("stockThresholdQty") Integer stockThresholdQty,
            @Param("categoryId") List<Long> categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("warehouseId") List<Long> warehouseId,
            @Param("warehouseStoreId") Long warehouseStoreId,
            Pageable pageable
    );

    @Query(value = getPendingVerificationItemsWithSearch,
            countQuery = countAllPendingVerificationItems, nativeQuery = true)
    Page<PageItemList> findAllPendingVerificationItems(
            @Param("name") String name,
            @Param("code") String code,
            @Param("reorderPercentage") Integer reorderPercentage,
            @Param("stockThresholdQty") Integer stockThresholdQty,
            @Param("categoryId") List<Long> categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("warehouseId") List<Long> warehouseId,
            @Param("warehouseStoreId") Long warehouseStoreId,
            Pageable pageable
    );


    List<ItemInfo> findAllByActiveAndNameLikeIgnoreCase(Boolean active, String name);

    List<ItemInfo> findAllByActiveAndCodeLikeIgnoreCase(Boolean active, String code);

    Optional<Item> findByCode(String code);

    @Query("select max(i.id) from Item i")
    Optional<Item> findMaxOrderById();

    @Query(value = getItemsBySubCategoryAttributeAndName,nativeQuery = true)
    List<ItemInfoExt> findAllItemBySubCategoryAndAttributeAndName(
            @Param("warehouseId") Long warehouseId,
            @Param("brandId") Long brandId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("name") String name,
            @Param("code") String code,
            @Param("attributeType") String attributeType,
            @Param("attributeValue") String attributeValue
            );

    List<ItemInfo> findAllByActiveAndItemCategoryIdOrItemParentCategoryIdAndNameLikeIgnoreCaseOrCodeLikeIgnoreCase(
            Boolean active,
            Optional<Long> categoryId, Optional<Long> categoryId1, String name, String code);

    @Query(value = """
            SELECT * FROM (SELECT i.id, i.brand_id ,i.active, s.warehouse_id, i.item_category_id,
                    GROUP_CONCAT(DISTINCT  ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit ORDER BY ia.id ASC separator ' - ') itemAttributes\s
            FROM scm_item_attributes ia
            LEFT JOIN scm_items i on i.id=ia.item_id
            LEFT JOIN scm_item_stocks s ON s.item_id = i.id
            GROUP BY i.id) p
            WHERE p.brand_id=:brandId AND itemAttributes = :attribute
             AND p.warehouse_id = :warehouseId
             AND p.item_category_id = :subCategoryId
            """,nativeQuery = true)
    List<ItemInfoByAttribute> findByAttributes(@Param("brandId") Long brandId,
                                               @Param("attribute") String attribute,
                                               @Param("subCategoryId") Long subCategoryId,
                                               @Param("warehouseId") Long warehouseId);

    Optional<Item> findByItemAttributeName(String itemAttribute);

    @Query(value = """
            SELECT w.id as wId, w.name as warehouseName, ws.id as storeId, ws.store_name as storeName, ipc.name as category,
                  ipc.code as categoryCode, ic.name as subCategory, ic.code as subCategoryCode,
                  i.active,
                  i.name as brandName, i.item_attribute_name  as itemAttributeName, i.item_unit as unitMeasurement,
                  COALESCE((SELECT SUM(stock_qty) from scm_item_stocks sis WHERE sis.item_id=i.id
                  AND (:warehouseId IS NULL OR sis.warehouse_id = :warehouseId)
                  AND (:warehouseStoreId IS NULL OR sis.warehouse_store_id = :warehouseStoreId)
                  ),0) as currentStock, COALESCE(i.stock_threshold_qty,0) as safetyStock, 
                  COALESCE(i.reorder_percentage,0) as reorderPercent
                FROM scm_items i
             LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
             LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
             LEFT JOIN scm_item_stocks is1 ON is1.item_id = i.id
             LEFT JOIN scm_warehouses w ON w.id = is1.warehouse_id
             LEFT JOIN scm_warehouse_stores ws ON ws.id = is1.warehouse_store_id
             WHERE (:categoryId IS NULL OR ipc.id = :categoryId)
                 AND (:subCategoryId IS NULL OR ic.id = :subCategoryId)
                 AND (:warehouseId IS NULL OR w.id = :warehouseId)
                 AND (:warehouseStoreId IS NULL OR ws.id = :warehouseStoreId)
                 AND i.active = true
                 GROUP BY i.name, i.item_attribute_name
                 ORDER BY i.name, i.item_attribute_name ASC
            """,nativeQuery = true)
    List<ItemTemplateInfo> fetchTemplateData(Long categoryId, Long subCategoryId,
                                             Long warehouseId, Long warehouseStoreId);

    @Query(value = """
            SELECT i FROM Item i WHERE LOWER(i.code) LIKE CONCAT('%',LOWER(:itemCode))
            """)
    List<Item> findByCodeLikeCode(String itemCode);

    @Modifying
    @Query(value = """
            UPDATE scm_items i SET i.active=1 WHERE 1=1
            """,nativeQuery = true)
    void forceActive();

    interface ItemTemplateInfo {

        Long getWId();
        String getWarehouseName();
        Long getStoreId();
        String getStoreName();
        String getBrandName();
        String getCategory();
        String getCategoryCode();
        String getSubCategory();
        String getSubCategoryCode();
        String getitemAttributeName();
        String getUnitMeasurement();
        String getCurrentStock();
        String getSafetyStock();
        String getReorderPercent();
    }

    interface ItemInfoByAttribute{
        Long getBrandId();
        Long getId();
        String getItemAttributes();
    }

    interface ItemInfoExt extends ItemInfo{
        Long getWarehouseId();
        Long getWarehouseStoreId();

        Long getBrandId();
        String getBrandName();

        String getAttributeTypes();
        String getAttributeValues();
        String getAttributeUnits();


    }

    interface ItemInfo{
        Long getId();
        String getName();
        String getItemAttributeName();
        String getCode();
    }

    interface PageItemList extends ItemInfo{
        Long getCategoryId();
        Long getSubCategoryId();
        String getCategoryName();
        String getCategoryCode();
        String getSubCategoryName();
        String getSubCategoryCode();
        String getItemUnit();
        BigDecimal getQty();
        BigDecimal getInTransit();
        BigDecimal getStockThresholdQty();
        BigDecimal getReorderPercentage();
        String getStatus();
    }

    interface ItemDetail extends ItemInfo{
        RefInfo getWarehouse();
        WarehouseStoreInfo getWarehouseStore();
        RefInfo getBrand();
        List<ItemStock> getStocks();
        String getItemAttributeName();
        CatInfo getItemCategory();
        CatInfo getItemParentCategory();
        String getSku();
        String getManufacturer();
        String getItemUnit();
        Integer getStockThresholdQty();
        Integer getReorderPercentage();
        List<ItemAttribute> getAttributes();
        List<ItemFunctionalUnit> getFunctionalUnits();
        Boolean getActive();

        LocalDateTime getCreatedAt();
        LocalDateTime getUpdatedAt();
    }

    /**
     *  CatInfo 
     */
    public interface  CatInfo extends RefInfo {
        String getCode();

    }

    interface RefInfo{
        Long getId();
        String getName();
        
    }

    interface WarehouseStoreInfo{
        Long getId();
        String getStoreName();
    }

    Optional<Item> findByBrandIdAndItemAttributeName(Long id, String itemAttributeName);

    List<Item> findAllByItemCategoryIdAndActive(Long id, boolean b);

    List<Item> findByBrandIdAndItemCategoryIdAndItemAttributeName(Long id, Long id2, String itemAttributeName);


}
