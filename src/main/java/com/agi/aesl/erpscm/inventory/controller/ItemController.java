package com.agi.aesl.erpscm.inventory.controller;

// import com.agi.aesl.erpscm.authentication.dto.ClaimResponseDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.RemoteItemRequestDto;
import com.agi.aesl.erpscm.inventory.service.ItemService;

import jakarta.validation.Valid;

// import io.swagger.annotations.ApiOperation;
// import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    private record SyncItemReqDto(Long warehouseId, Long warehouseStoreId, String subCatCode){};

    @PostMapping
    // @ApiOperation(value = "Create Item")
    public ResponseEntity<?> addItem(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody @Valid ItemRequestDto itemRequestDto){
        itemService.createItem(loggedInUser,itemRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/receive-from-cps")
    public ResponseEntity<?> receiveItemFromCps(
        @AuthenticationPrincipal Jwt loggedInUser,
        @RequestBody @Valid RemoteItemRequestDto remoteItemRequestDto
    ){
        itemService.createItem(loggedInUser,remoteItemRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    // @ApiOperation(value = "Update Item")
    public ResponseEntity<?> updateItem(
        // @ApiParam(value = "Item Id",example = "1", required = true)
                                        @PathVariable("id") Long id,
                                        @RequestBody ItemRequestDto itemRequestDto){
        itemService.updateItem(id,itemRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    // @ApiOperation(value = "Get Items with Pagination")
    public ResponseEntity<?> getItems(@RequestParam("page") Optional<Integer> page,
                                      @RequestParam("size") Optional<Integer> size,
                                      @RequestParam("name") Optional<String> name,
                                      @RequestParam("code") Optional<String> code,
                                      @RequestParam("reorderPercentage") Optional<Integer> reorderPercentage,
                                      @RequestParam("stockThresholdQty") Optional<Integer> stockThresholdQty,
                                      @RequestParam("categoryId") Optional<Long> categoryId,
                                      @RequestParam("subCategoryId") Optional<Long> subCategoryId,
                                      @RequestParam("warehouseId") Optional<Long> warehouseId,
                                      @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId
    ){

        return new ResponseEntity<>(
                itemService.getAllItems(page,size, name,code,reorderPercentage,stockThresholdQty,
                        categoryId,subCategoryId, warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/list")
    // @ApiOperation(value = "Get Items as List with search by name and code")
    public ResponseEntity<?> getItems(@RequestParam("categoryId") Optional<Long> categoryId,
                                      @RequestParam("name") Optional<String> name,
                                      @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                itemService.getAllItems(categoryId,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/search")
    // @ApiOperation(value = "Get Items as List with search by name and code")
    public ResponseEntity<?> getItems(
                                    @RequestParam("warehouseId") Optional<Long> warehouseId,
                                    @RequestParam("brandId") Optional<Long> brandId,
                                    @RequestParam("subCategoryId") Optional<Long> subCategoryId,
                                    @RequestParam("name") Optional<String> name,
                                    @RequestParam("code") Optional<String> code,
                                    @RequestParam("attributes") Optional<String> attributes,
                                    @RequestParam("attributeType")
                                    // @ApiParam(value = "Comma Separated Value, ie. COLOR,SIZE")
                                      Optional<String> attributeType,
                                    // @ApiParam(value = "Comma Separated Value, ie. RED,MEDIUM")
                                          @RequestParam("attributeValue") Optional<String> attributeValue
                                      ){
        return new ResponseEntity<>(
                itemService.getAllItemsBySubCategoryAndAttribute(warehouseId,brandId,subCategoryId,name,code,attributes,attributeType,attributeValue),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    // @ApiOperation(value = "Get Item Detail")
    public ResponseEntity<?> getItem(
        // @ApiParam(value = "Item Id", example = "1", required = true)
                                     @PathVariable("id") Long id){
        return new ResponseEntity<>(
                itemService.getItemDetailWithWarehouse(id),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    // @ApiOperation(value = "Delete Item")
    public ResponseEntity<?>  deleteItem(
        // @ApiParam(value = "Item Id", example = "1", required = true)
                                         @PathVariable("id") Long id){
        itemService.deleteItem(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

   @PutMapping("/sync-item")
    public ResponseEntity<?> syncItem(@RequestBody SyncItemReqDto syncItemReqDto
   ){
       itemService.syncItemsBySubCatCode(syncItemReqDto.warehouseId(), 
                        syncItemReqDto.warehouseStoreId(),
                         syncItemReqDto.subCatCode());
       return new ResponseEntity<>(
               HttpStatus.NO_CONTENT
       );
   }

    @GetMapping("/next-id")
    // @ApiOperation(value = "Get New Product Id")
    public ResponseEntity<?> getNextId(){
        Map<String,Object> response = new HashMap<>();
        response.put("code",itemService.getNextItemCode());
        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

    @PostMapping("/import")
    public ResponseEntity<?> importItems(
        @RequestPart("file") Optional<MultipartFile> file
    ){
        itemService.importItems(file);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
