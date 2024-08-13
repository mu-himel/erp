package com.agi.aesl.erpscm.common;

import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class DataFilter {
    private String uri;
    private ClaimResolver claimResolver;

    private IntegrationReaderService readerService;

    private DataFilterService dataFilterService;


    public Pageable pageable;


     public static String FILTER_BY_WAREHOUSE = "warehouse_id";


     List<Long> ids=new ArrayList<>();

    public DataFilter(String moduleUri, ClaimResolver claimResolver,Pageable pageable) {
        this.uri = moduleUri;
        this.claimResolver = claimResolver;
        this.pageable = pageable;
    }

    public DataFilter(String moduleUri, ClaimResolver claimResolver) {
        this.uri = moduleUri;
        this.claimResolver = claimResolver;
    }

    public List<Long> getFilterConfig(String key){
        Optional<Map<String, List<Long>>> modulePermission = readerService
                .getModuleFilterByUri(claimResolver.getToken(), uri);


        if(modulePermission.isPresent()){
            ids =  modulePermission.get().get(key);
            return ids;
        }

        return ids;
    }

    public Page<?> fetchData(){
        this.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        if(this.ids.size()>0) {
            return dataFilterService.getFilteredData(ids, pageable);
        }
        return Page.empty();
    }

}
