package com.agi.aesl.erpscm.goods_receive.repository;

import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnRepository extends JpaRepository<GoodReceiveNote, Long> {

    @Query("select max(g.id) from GoodReceiveNote g")
    Optional<Long> findMaxOrderById();
}
