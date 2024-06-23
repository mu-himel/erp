package com.agi.aesl.erpscm.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.employee.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,String>{
    
}
