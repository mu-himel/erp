package com.agi.aesl.erpscm.internal_requisition.entity;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.internal_requisition.enums.IrStatus;
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
@Table(name = "store_ir_va_histories")
public class StoreIRVAHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private StoreIR storeIr;

    @ManyToOne
    @JsonIgnore
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private IrStatus irStatus;

    @CreationTimestamp
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime verificationDate;
}
