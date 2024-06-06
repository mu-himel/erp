package com.agi.aesl.erpscm.user.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.user.entity.User;

@Service
public class UserServiceImpl implements UserService{

    @Override
    public void createUser(Jwt token, User user) {
        
        
    }
    
}
