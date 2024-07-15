package com.agi.aesl.erpscm.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.employee.service.EmployeeService;

@Component
public class ClaimResolver {
    
    private Jwt token;

    @Autowired
    private EmployeeService employeeService;

    public void setToken(Jwt token){
        this.token = token;
    }

    public Jwt getToken(){
        return this.token;
    }

    private Map<String,Object> getRealmAccess(){
        return token.getClaimAsMap("realm_access");
    }

    public Boolean isAdmin(){
        var reamRoles = getRealmAccess().get("roles");
        if(reamRoles instanceof List){
            List<String> roles = (List<String>) reamRoles;
            if(roles.contains(Role.ADMIN.toString())){
                return true;
            }
            return false;
        }
        return false;
    }

    public String getUserId(){
        return token.getSubject();
    }

    public Optional<Employee> getEmployee(){
        return employeeService.getUserById(getUserId());
    }
}
