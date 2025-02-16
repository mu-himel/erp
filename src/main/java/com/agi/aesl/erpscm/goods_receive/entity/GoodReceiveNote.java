package com.agi.aesl.erpscm.goods_receive.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "good_receive_notes")
public class GoodReceiveNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String grnNo;
    private String poNo;
    private Long remotePoId;

    @Enumerated(EnumType.STRING)
    private GrnMode grnMode;

    @OneToMany(mappedBy = "goodReceiveNote",cascade = CascadeType.ALL)
    private List<GoodReceiveItemDetail> goodReceiveItemDetails =  new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDate grnDate;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private GrnStatus grnStatus;

    private  Boolean isReceivedByStore;

    private String indentNo;
    private Long vendorId;
    private String vendorName;
    private String vendorPhone;
    private String vendorEmail;

    @ManyToOne
    private Warehouse warehouse;

    private String deliveryCharge;
    private String mushak;
    @Column(precision = 38, scale = 4)
    private BigDecimal deliveryChargeAmount;
    private Integer days;
    private String vatOption;
    private String vatType;
    private String aitOption;
    @Column(precision = 38, scale = 4)
    private BigDecimal totalPrice;
    @Column(precision = 38, scale = 4)
    private BigDecimal vat;
    @Column(precision = 38, scale = 4)
    private BigDecimal vatPctg;
    @Column(precision = 38, scale = 4)
    private BigDecimal subTotal;
    private String paymentType;

    private String declineNote;

    @Column(length = 1000)
    private String invoicePath;

    @ManyToOne
    private Employee createdBy;

    public GoodReceiveNote(Long id){
        this.id = id;
    }
}
