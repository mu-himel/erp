package com.agi.aesl.erpscm.inventory.controller;


import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.inventory.entity.AttributeUnit;
import com.agi.aesl.erpscm.inventory.repository.AttributeUnitRepository;
import com.agi.aesl.erpscm.inventory.service.CategoryAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/attributes")
@RequiredArgsConstructor
public class AttributeController extends BaseController{


    private final CategoryAttributeService categoryAttributeService;


    private final AttributeUnitRepository attributeUnitRepository;


    @GetMapping("/{categoryId}")
    public ResponseEntity<Object> getAttributes(@PathVariable("categoryId") Long categoryId){
        return new ResponseEntity<>(
              categoryAttributeService.getAttributesByCategory(categoryId),
              HttpStatus.OK
        );
    }

    @GetMapping("/units")
    public ResponseEntity<Object> getAttributeUnits(){
        List<String> attributes = attributeUnitRepository.findAll().stream().map(AttributeUnit::getName).toList();
        return new ResponseEntity<>(
                attributes,
                HttpStatus.OK);
    }


}
