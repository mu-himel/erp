package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserItemHistoryRepository extends JpaRepository<UserItemHistory,Long> {
}
