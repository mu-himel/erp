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
     public static String FILTER_BY_CATEGORY = "category_id";


     List<Long> ids=new ArrayList<>();
     List<Long> categoryIds=new ArrayList<>();

    public DataFilter(String moduleUri, ClaimResolver claimResolver,Pageable pageable) {
        this.uri = moduleUri;
        this.claimResolver = claimResolver;
        this.pageable = pageable;
    }

    public DataFilter(String moduleUri, ClaimResolver claimResolver) {
        this.uri = moduleUri;
        this.claimResolver = claimResolver;
    }

    public List<Long> getFilterConfig(){

        Optional<Map<String, List<Long>>> modulePermission = readerService
                .getModuleFilterByUri(claimResolver.getToken(), uri);

        if(modulePermission.isPresent()){
            ids =  modulePermission.get().get(DataFilter.FILTER_BY_WAREHOUSE);
            categoryIds = modulePermission.get().get(DataFilter.FILTER_BY_CATEGORY);
        }
        if(ids.isEmpty()){
            if(claimResolver.getEmployee().isPresent()) {
                ids.add(claimResolver.getEmployee().get().getWarehouseId());
            }

        }
        return ids;
    }

    public List<Long> getFilterConfig(String key){

        Optional<Map<String, List<Long>>> modulePermission = readerService
                .getModuleFilterByUri(claimResolver.getToken(), uri);


        if(modulePermission.isPresent()){
            String warehouseKey = (!key.isEmpty()? key:DataFilter.FILTER_BY_WAREHOUSE);
            ids =  modulePermission.get().get(warehouseKey);
            categoryIds = modulePermission.get().get(DataFilter.FILTER_BY_CATEGORY);
        }
        if(ids.size()==0){
            if(claimResolver.getEmployee().isPresent()) {
                ids.add(claimResolver.getEmployee().get().getWarehouseId());
            }

        }
        return ids;
    }

    public List<Long> getCategoryIds(){
        return this.categoryIds;
    }

    public Page<?> fetchData(){
        this.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        if(this.ids.size()>0) {
            return dataFilterService.getFilteredData(ids, pageable);
        }
        return Page.empty();
    }

}
