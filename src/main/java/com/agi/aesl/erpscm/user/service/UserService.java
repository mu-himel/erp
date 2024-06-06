package com.agi.aesl.erpscm.user.service;

import org.springframework.security.oauth2.jwt.Jwt;

import com.agi.aesl.erpscm.user.entity.User;

public interface UserService {
    void createUser(Jwt token, User user);
}
