package com.agi.aesl.erpscm.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.employee.service.EmployeeService;

@Data
@Component
@RequiredArgsConstructor
public class ClaimResolver {
    
    private Jwt token;

    private final EmployeeService employeeService;

    private Map<String,Object> getRealmAccess(){
        return token.getClaimAsMap("realm_access");
    }

    public Boolean isAdmin(){
        var reamRoles = getRealmAccess().get("roles");
        if(reamRoles instanceof List){
            List<String> roles = (List<String>) reamRoles;
            return roles.contains(Role.ADMIN.toString());

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
