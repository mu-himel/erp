package com.agi.aesl.erpscm.employee.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.employee.repository.EmployeeRepository;

import jakarta.transaction.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    private EmployeeRepository userRepository;

    @Override
    public void createUser(Jwt token, Employee user) {
        userRepository.save(user);
    }

    @Override
    public Optional<Employee> getUserById(String subject) {
        return userRepository.findById(subject);
    }

    @Override
    @Transactional
    public void deleteUser(Employee user) {
        Optional<Employee> eOptional = userRepository.findByEmployeeId(user.getEmployeeId());
        if(eOptional.isPresent()){
            userRepository.delete(eOptional.get());
        }
    }

    

    
    
}
