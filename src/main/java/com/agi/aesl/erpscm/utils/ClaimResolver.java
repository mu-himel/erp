package com.agi.aesl.erpscm.utils;

import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class ClaimResolver {
    
    private Jwt token;

    public void setToken(Jwt token){
        this.token = token;
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
}
