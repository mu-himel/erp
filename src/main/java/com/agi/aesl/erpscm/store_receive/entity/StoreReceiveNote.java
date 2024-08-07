package com.agi.aesl.erpscm.store_receive.entity;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "store_receive_notes")
public class StoreReceiveNote extends VerifyableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private GoodReceiveNote grn;

    private String srnNo;

    @Column(length = 500)
    private String comment;

    @OneToMany(mappedBy = "storeReceiveNote", cascade = CascadeType.ALL)
    private List<StoreReceiveDetail> srnDetails=new ArrayList<>();

    @CreationTimestamp
    private LocalDate createdAt;

    @ManyToOne
    private Employee employee;
}
