package com.agi.aesl.erpscm.comment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;

@Repository
public interface CommentAttachmentRepository extends JpaRepository<CommentAttachment,Long>{

    Optional<CommentAttachment> findByCommentIdAndAttachmentPath(Long id, String filename);
    
}
