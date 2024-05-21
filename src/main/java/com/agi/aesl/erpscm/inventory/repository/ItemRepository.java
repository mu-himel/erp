package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import com.agi.aesl.erpscm.inventory.enums.ItemUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            @Param("categoryId") Long categoryId,
            @Param("subCategoryId") Long subCategoryId,
            @Param("warehouseId") Long warehouseId,
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
            SELECT * FROM (SELECT i.id, i.brand_id ,i.active, s.warehouse_id,
                    GROUP_CONCAT(DISTINCT  ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit ORDER BY ia.id ASC separator ' - ') itemAttributes\s
            FROM scm_item_attributes ia
            LEFT JOIN scm_items i on i.id=ia.item_id
            LEFT JOIN scm_item_stocks s ON s.item_id = i.id
            GROUP BY i.id) p
            WHERE p.brand_id=:brandId AND itemAttributes = :attribute
             AND p.warehouse_id = :warehouseId
            """,nativeQuery = true)
    List<ItemInfoByAttribute> findByAttributes(@Param("brandId") Long brandId,
                                               @Param("attribute") String attribute,
                                               @Param("warehouseId") Long warehouseId);

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
        String getCode();
    }

    interface PageItemList extends ItemInfo{
        Long getCategoryId();
        Long getSubCategoryId();
        String getCategoryName();
        String getCategoryCode();
        String getSubCategoryName();
        String getSubCategoryCode();
        Integer getQty();
        Integer getStockThresholdQty();
        Integer getReorderPercentage();
    }

    interface ItemDetail extends ItemInfo{
        RefInfo getWarehouse();
        WarehouseStoreInfo getWarehouseStore();
        RefInfo getBrand();
        List<ItemStock> getStocks();

        CatInfo getItemCategory();
        CatInfo getItemParentCategory();
        String getSku();
        String getManufacturer();
        ItemUnit getItemUnit();
        Integer getStockThresholdQty();
        Integer getReorderPercentage();
        List<ItemAttribute> getAttributes();
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
