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

    @ManyToOne
    private Warehouse warehouse;

    private String deliveryCharge;
    private String mushak;
    private BigDecimal deliveryChargeAmount;
    private Integer days;
    private String vatOption;
    private String aitOption;
    private BigDecimal totalPrice;
    private BigDecimal vat;
    private BigDecimal vatPctg;
    private BigDecimal subTotal;
    private String paymentType;

    @ManyToOne
    private Employee createdBy;

    public GoodReceiveNote(Long id){
        this.id = id;
    }
}
