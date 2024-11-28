package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.common.ItemAttributeInterface;
import com.agi.aesl.erpscm.common.ItemInterface;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
// import com.agi.aesl.erpscm.authentication.dto.ClaimResponseDto;
//import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.inventory.dto.request.ForceActiveRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemApproveRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.RemoteItemRequestDto;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import com.agi.aesl.erpscm.inventory.enums.StockType;

import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ItemService {


    void createItem(Jwt loggedInUser, ItemRequestDto itemRequestDto);
    void createItem(Jwt loggedInUser, RemoteItemRequestDto itemRequestDto);


    void updateItem(Long id, ItemRequestDto itemRequestDto);

    void deleteItem(Long id, Long warehouseId, Long warehouseStoreId);

    Optional<Item> getItemDetail(Long id);

    List<?> getByAttributes(Long brandId,String attribute,Long subCategoryId,Long warehouseId);
    Optional<?> getItemDetailWithWarehouse(Long id);

    <T extends ItemInterface> void sendItemToCps(ClaimResolver claimResolver, String _employee,T item,
                                                 List<ItemAttributeInterface> attributes,
                                                 WarehouseStore warehouseStore
    );

    Page<?> getAllItems(
            Jwt token, String uri,Optional<Integer> page, Optional<Integer> size,
                                   Optional<String> name,
                                   Optional<String> code,
                                   Optional<Integer> reorderPercentage,
                                   Optional<Integer> stockThresholdQty,
                                   Optional<Long> categoryId,
                                   Optional<Long> subCategoryId,
                                   Optional<Long> warehouseId,
                                   Optional<Long> warehouseStoreId

    );

    Page<?> getPendingAllItems(
            Jwt token,
            Optional<Integer> page, Optional<Integer> size,
                        Optional<String> name,
                        Optional<String> code,
                        Optional<Integer> reorderPercentage,
                        Optional<Integer> stockThresholdQty,
                        Optional<Long> categoryId,
                        Optional<Long> subCategoryId,
                        Optional<Long> warehouseId,
                        Optional<Long> warehouseStoreId

    );

    Page<?> getPendingVerificationAllItems(
            Jwt token,
            Optional<Integer> page, Optional<Integer> size,
                        Optional<String> name,
                        Optional<String> code,
                        Optional<Integer> reorderPercentage,
                        Optional<Integer> stockThresholdQty,
                        Optional<Long> categoryId,
                        Optional<Long> subCategoryId,
                        Optional<Long> warehouseId,
                        Optional<Long> warehouseStoreId

    );

    List<?> getAllItems(Optional<Long> categoryId,Optional<String> name, Optional<String> code);
    List<?> getAllItemsBySubCategoryAndAttribute(
            Optional<Long> warehouseId,
            Optional<Long> brandId,
            Optional<Long> subCategoryId,
            Optional<String> name,
            Optional<String> code,
            Optional<String> attributes,
            Optional<String> attributeType,
            Optional<String> attributeValue
            );

    void stockIn(Item item,BigDecimal qty,Long warehouseId, Long warehouseStoreId);
    void stockOut(Item item, BigDecimal qty,Long warehouseId, Long warehouseStoreId);

   void stockUpdateByDemand(Long warehouseId, DemandDetail demandDetail, StockType stockType);

    String getNextItemCode();

    void importItems(Optional<MultipartFile> file);

    void syncItemsBySubCatCode(Jwt token, Long warehouseId,Long warehosueStoreId, String subCatCode);

    Optional<Item> getByBrandAndAttributeName(String string,Long subCatId, String string2);

    void approveItemFromCps(Long id, ItemApproveRequestDto approveRequestDto);

    List<?> getTemplateData(Long categoryId, Long subCategoryId,
                            Long warehouseId, Long warehouseStoreId);

    void approveItemFromAcc(Long id, Long warehouseId);

    List<Item> getByCode(String itemCode);

    List<ItemStock> getByItemAndWarehouse(List<Long>itemId, Long warehouseId);

    void forceActive(Jwt token,ForceActiveRequestDto forceActiveRequestDto);
}
