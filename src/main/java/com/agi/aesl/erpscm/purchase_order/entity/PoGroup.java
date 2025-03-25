package com.agi.aesl.erpscm.purchase_order.entity;

import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.purchase_order.enums.PurchaseOrderStatus;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cs_po")
@EqualsAndHashCode(callSuper = true)
public class PoGroup extends VerifyableEntity {

    @ManyToOne
    private Cs cs;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus purchaseOrderStatus;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus reviewPrevStatus;

    @Column(length = 500)
    private String declineNote;

    private Boolean isVerifyApproveEnabled;

    private String vendorName;
    private Long vendorId;
    private String vendorEmail;
    private String phoneNo;

    @CreationTimestamp
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime poDate;

    @Override
    public void setStatus(String status) {
        this.purchaseOrderStatus = PurchaseOrderStatus.valueOf(status);
    }

}
