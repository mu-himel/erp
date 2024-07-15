package com.agi.aesl.erpscm.employee.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.employee.service.EmployeeService;

@RestController
@RequestMapping("/api/v1/users")
public class EmployeeController extends BaseController{
    
    @Autowired
    private EmployeeService userService;

    @PostMapping
    public ResponseEntity<?> createUser(
        @AuthenticationPrincipal Jwt token,
        @RequestBody Employee user
    ){
        userService.createUser(token, user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(
        @AuthenticationPrincipal Jwt token,
        @RequestBody Employee user
    ){
        userService.deleteUser(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
