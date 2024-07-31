package com.agi.aesl.erpscm.comment.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.agi.aesl.erpscm.comment.enums.ActionType;
import org.hibernate.annotations.CreationTimestamp;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.employee.entity.Employee;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "scm_comments")
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long domainId;

    @Enumerated(EnumType.STRING)
    private DomainType domainType;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @ManyToOne
    private Employee commentedBy;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL)
    private List<CommentAttachment> attachments = new ArrayList<>();

    private String message;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
