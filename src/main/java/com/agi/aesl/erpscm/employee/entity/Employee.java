package com.agi.aesl.erpscm.employee.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "acl_users")
public class Employee {

    @Id
    private String id;
    
    private String employeeId;

    private String employeeName;

    @Column(length = 500)
    private String emailAddress;
    private String phoneNo;

    private Long warehouseId;
    private String warehouseName;

    private String reportingManagerId;
    private String reportingManager;

    private Long reportingManagerDepartmentId;
    private String reportingManageDepartmentName;
    private Long level;
    private Long parentDepartmentId;
    private Long departmentId;
    private String departmentName;

    private Long designationId;
    private String designationName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Employee(String userId) {
        this.id = userId;
    }
    
}
