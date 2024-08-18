package com.agi.aesl.erpscm.vendor.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.vendor.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController extends BaseController {

    @Autowired
    private VendorService vendorService;

    @GetMapping("/available-vendors")
    public ResponseEntity<?> getAvailableVendors(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("name") Optional<String> name){
        return new ResponseEntity<>(
                vendorService.getAvailableVendors(token, name),
                HttpStatus.OK
        );
    }
}
