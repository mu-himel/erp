package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCategoryHistoryRepository extends JpaRepository<UserCategoryHistory,Long> {
}
