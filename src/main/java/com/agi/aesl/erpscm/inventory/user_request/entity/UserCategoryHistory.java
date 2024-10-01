package com.agi.aesl.erpscm.inventory.user_request.entity;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.enums.IndentVerificationStatus;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_category_histories")
public class UserCategoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private UserCategory userCategory;

    @ManyToOne
    @JsonIgnore
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private UserCategoryStatus categoryStatus;

    @CreationTimestamp
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime verificationDate;


}
