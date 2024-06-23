package com.agi.aesl.erpscm.employee.service;

import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;

import com.agi.aesl.erpscm.employee.entity.Employee;

public interface EmployeeService {
    void createUser(Jwt token, Employee user);

    Optional<Employee> getUserById(String subject);
}
