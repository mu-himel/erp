package com.agi.aesl.erpscm.goods_receive.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.enums.GrnStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    private Employee createdBy;

    public GoodReceiveNote(Long id){
        this.id = id;
    }
}
