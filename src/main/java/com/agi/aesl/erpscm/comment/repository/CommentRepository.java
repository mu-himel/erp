package com.agi.aesl.erpscm.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.comment.dto.CommentInfo;
import com.agi.aesl.erpscm.comment.entity.Comment;
import com.agi.aesl.erpscm.comment.enums.DomainType;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long>{

    List<CommentInfo> findAllByDomainTypeAndDomainId(DomainType domainType, Long domainId);
    
}
