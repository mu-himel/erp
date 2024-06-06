package com.agi.aesl.erpscm.user.controller;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.user.entity.User;
import com.agi.aesl.erpscm.user.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController{
    
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(
        @AuthenticationPrincipal Jwt token,
        @RequestBody User user
    ){

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
