package com.agi.aesl.erpscm.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DataFilterService {

    Page<?> getFilteredData(List<Long> ids, Pageable pageable);
}
