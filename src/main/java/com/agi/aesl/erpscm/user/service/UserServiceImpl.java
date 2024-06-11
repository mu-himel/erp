package com.agi.aesl.erpscm.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.user.entity.User;
import com.agi.aesl.erpscm.user.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public void createUser(Jwt token, User user) {
        userRepository.save(user);
    }
    
}
