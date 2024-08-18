package com.agi.aesl.erpscm.goods_receive.repository;

import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrnDetailRepository extends JpaRepository<GoodReceiveItemDetail,Long> {
}
