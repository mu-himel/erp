package com.agi.aesl.erpscm.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,String>{
    
}
