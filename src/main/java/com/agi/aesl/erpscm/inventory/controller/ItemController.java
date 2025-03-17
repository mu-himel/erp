package com.agi.aesl.erpscm.inventory.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.inventory.dto.request.ForceActiveRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemApproveRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.RemoteItemRequestDto;
import com.agi.aesl.erpscm.inventory.service.ItemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ItemController extends BaseController{


    private final ItemService itemService;

    private record SyncItemReqDto(Long warehouseId, Long warehouseStoreId, String subCatCode){}

    @PostMapping
    public ResponseEntity<Void> addItem(
            @AuthenticationPrincipal Jwt loggedInUser,
            @RequestBody @Valid ItemRequestDto itemRequestDto){
        itemService.createItem(loggedInUser,itemRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/receive-from-cps")
    public ResponseEntity<Void> receiveItemFromCps(
        @AuthenticationPrincipal Jwt loggedInUser,
        @RequestBody @Valid RemoteItemRequestDto remoteItemRequestDto
    ){
        itemService.createItem(loggedInUser,remoteItemRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateItem(
                                        @PathVariable("id") Long id,
                                        @RequestBody ItemRequestDto itemRequestDto){
        itemService.updateItem(id,itemRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(
                                      @AuthenticationPrincipal Jwt token,
                                      @RequestHeader("uri") String uri,
                                      @RequestParam("page") Optional<Integer> page,
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
                itemService.getAllItems(token,uri,
                        page,size, name,code,reorderPercentage,stockThresholdQty,
                        categoryId,subCategoryId, warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<Object> getPendingItems(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
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
                itemService.getPendingAllItems(
                        token,
                        page,size, name,code,reorderPercentage,stockThresholdQty,
                        categoryId,subCategoryId, warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending-verifications")
    public ResponseEntity<Object> getPendingVerificationItems(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
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
                itemService.getPendingVerificationAllItems(
                        token,
                        page,size, name,code,reorderPercentage,stockThresholdQty,
                        categoryId,subCategoryId, warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/list")
    public ResponseEntity<Object> getItems(@RequestParam("categoryId") Optional<Long> categoryId,
                                      @RequestParam("name") Optional<String> name,
                                      @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                itemService.getAllItems(categoryId,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItems(
                                    @RequestParam("warehouseId") Optional<Long> warehouseId,
                                    @RequestParam("brandId") Optional<Long> brandId,
                                    @RequestParam("subCategoryId") Optional<Long> subCategoryId,
                                    @RequestParam("name") Optional<String> name,
                                    @RequestParam("code") Optional<String> code,
                                    @RequestParam("attributes") Optional<String> attributes,
                                    @RequestParam("attributeType")
                                      Optional<String> attributeType,
                                          @RequestParam("attributeValue") Optional<String> attributeValue
                                      ){
        return new ResponseEntity<>(
                itemService.getAllItemsBySubCategoryAndAttribute(warehouseId,brandId,subCategoryId,name,code,attributes,attributeType,attributeValue),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(
                                     @PathVariable("id") Long id){
        return new ResponseEntity<>(
                itemService.getItemDetailWithWarehouse(id),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteItem(
                @PathVariable("id") Long id,
                @RequestParam("warehouseId") Long warehouseId,
                @RequestParam("warehouseStoreId") Long warehouseStoreId
    ){
        itemService.deleteItem(id, warehouseId, warehouseStoreId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

   @PutMapping("/sync-item")
    public ResponseEntity<Void> syncItem(
        @AuthenticationPrincipal Jwt token,
        @RequestBody SyncItemReqDto syncItemReqDto
   ){
       itemService.syncItemsBySubCatCode(token,syncItemReqDto.warehouseId(), 
                        syncItemReqDto.warehouseStoreId(),
                         syncItemReqDto.subCatCode());
       return new ResponseEntity<>(
               HttpStatus.NO_CONTENT
       );
   }

    @GetMapping("/next-id")
    public ResponseEntity<Object> getNextId(){
        Map<String,Object> response = new HashMap<>();
        response.put("code",itemService.getNextItemCode());
        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importItems(
        @RequestPart("file") Optional<MultipartFile> file
    ){
        itemService.importItems(file);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/approve/{warehouseId}/acc/{id}")
    public ResponseEntity<Void> approveItemFromAcc(
            @PathVariable("warehouseId") Long warehouseId,
            @PathVariable("id") Long id
    ){
        itemService.approveItemFromAcc(id,warehouseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/reject/{warehouseId}/acc/{id}")
    public ResponseEntity<Void> rejectItemFromAcc(
            @PathVariable("warehouseId") Long warehouseId,
            @PathVariable("id") Long id
    ){
        itemService.rejectItemFromAcc(id,warehouseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Void> approveItemFromCps(
                    @AuthenticationPrincipal Jwt token,
                    @PathVariable("id") Long id,
                    @RequestBody ItemApproveRequestDto approveRequestDto){
        itemService.approveItemFromCps(token, id, approveRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/force-active")
    public ResponseEntity<Void> forceActive(
            @AuthenticationPrincipal Jwt token,
            @RequestBody ForceActiveRequestDto forceActiveRequestDto
    ){
        itemService.forceActivev2(token,forceActiveRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/download-template")
    public ResponseEntity<Object> downloadTemplate(
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("subCategoryId") Optional<Long> subCategoryId,
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId
    ){

        return new ResponseEntity<>(itemService.getTemplateData(
                categoryId.orElse(null),
                subCategoryId.orElse(null),
                warehouseId.orElse(null),
                warehouseStoreId.orElse(null)
                ),
                HttpStatus.OK
        );
    }
}
