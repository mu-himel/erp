package com.agi.aesl.erpscm.inventory.controller;


import com.agi.aesl.erpscm.inventory.repository.AttributeUnitRepository;
import com.agi.aesl.erpscm.inventory.service.CategoryAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/attributes")
public class AttributeController {

    @Autowired
    private CategoryAttributeService categoryAttributeService;

    @Autowired
    private AttributeUnitRepository attributeUnitRepository;


    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getAttributes(@PathVariable("categoryId") Long categoryId){
        return new ResponseEntity<>(
              categoryAttributeService.getAttributesByCategory(categoryId),
              HttpStatus.OK
        );
    }

    @GetMapping("/units")
    public ResponseEntity<?> getAttributeUnits(){
        List<String> attributes = attributeUnitRepository.findAll().stream().map(attributeUnit -> {
            return attributeUnit.getName();
        }).collect(Collectors.toList());
        return new ResponseEntity<>(
                attributes,
                HttpStatus.OK);
    }


}
