package com.agi.aesl.erpscm.indent.entity;

import com.agi.aesl.erpscm.common.enums.IndentPriority;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.enums.IndentStatus;
import com.agi.aesl.erpscm.indent.enums.IndentVerificationStatus;
import com.agi.aesl.erpscm.indent.enums.RfqStatus;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "indents")
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Indent extends VerifyableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String indentNo;

    @CreationTimestamp
    private LocalDateTime indentDate;

    private LocalDateTime sentDate;

    private LocalDateTime priorityDateTime;



    private Integer rfqDays;

    private String rfqUuid;

    private LocalDateTime expireDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory subCategory;

    @Enumerated(EnumType.STRING)
    private IndentPriority priority;

    @OneToMany(mappedBy = "indent", cascade = CascadeType.ALL)
    private List<IndentDetail> indentDetails;

    @ManyToOne
    private Warehouse warehouse;

    private Boolean isDevliverToSingleWarehouse;

    @ManyToOne
    private Warehouse singleWarehouse;

    @Enumerated(EnumType.STRING)
    private IndentStatus istatus;

    @Enumerated(EnumType.STRING)
    private RfqStatus rfqStatus;

    @Enumerated(EnumType.STRING)
    private IndentVerificationStatus indentStatus;
    @ManyToOne
    private Employee requestedBy;

    // Verification
    @Enumerated(EnumType.STRING)
    private IndentVerificationStatus reviewPrevStatus;


    public Indent(Long id) {
        this.id = id;
    }

    @Override
    public void setStatus(String status) {
        this.indentStatus = IndentVerificationStatus.valueOf(status);
    }
}
