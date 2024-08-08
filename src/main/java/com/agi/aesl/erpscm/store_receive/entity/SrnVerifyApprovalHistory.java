package com.agi.aesl.erpscm.store_receive.entity;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.store_receive.enums.SrnStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "srn_verify_approval_histories")
public class SrnVerifyApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private SrnStatus srnStatus;

    @ManyToOne
    @JsonIgnore
    private StoreReceiveNote storeReceiveNote;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime verificationDate;
}
