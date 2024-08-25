package com.agi.aesl.erpscm.pr_indent.entity;

import com.agi.aesl.erpscm.common.enums.IndentPriority;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.pr_indent.enums.PrIndentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pr_indents")
public class PrIndent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDateTime prIndentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private ItemCategory subCategory;

    @Enumerated(EnumType.STRING)
    private IndentPriority priority;

    private LocalDateTime priorityDate;

    @OneToMany(mappedBy = "prIndent", cascade = CascadeType.PERSIST)
    private List<PrIndentDetail> prIndentDetails;

    private String productRequirementsIds;

    @Enumerated(EnumType.STRING)
    private PrIndentStatus status;

    public PrIndent(Long id) {
        this.id = id;
    }
}
