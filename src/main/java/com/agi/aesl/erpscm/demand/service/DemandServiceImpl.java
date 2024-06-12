package com.agi.aesl.erpscm.demand.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.dto.request.DemandReceiveDto;
import com.agi.aesl.erpscm.demand.dto.request.DemandRequestDto;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.demand.dto.response.DemandDetailItemResDto;
import com.agi.aesl.erpscm.demand.dto.response.DemandDetailResDto;
import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.entity.DemandDetail;
import com.agi.aesl.erpscm.demand.entity.DemandDetailAttribute;
import com.agi.aesl.erpscm.demand.repository.DemandDetailAttributeRepository;
import com.agi.aesl.erpscm.demand.repository.DemandRepository;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.user.entity.User;
import com.agi.aesl.erpscm.utils.ClaimResolver;

import jakarta.transaction.Transactional;

@Service
public class DemandServiceImpl implements DemandService{

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private DemandDetailAttributeRepository demandDetailAttributeRepository;

    

    

    @Override
    public void closeDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto) {
        // TODO Auto-generated method stub
        
    }

    @Override
    @Transactional
    public Optional<DemandDetailResDto> createDemand(Jwt token, String uri, DemandRequestDto demandRequestDto) {
        
        ClaimResolver claimResolver = new ClaimResolver();
        claimResolver.setToken(token);
        
        // Optional<Map<String,Object>> verifierOp = (Optional<Map<String,Object>>)verificationService
        //         .getVerifiers(loggedInUser,uri,demandRequestDto.getCategories());
        // List<Verifier> verifiers = getVerifiers(demand, verifierOp);

        
        Demand demand = demandRequestDto.getEntity();

        demand.setDemandDate(LocalDateTime.now());
        if(demandRequestDto.getCategory()!=null) {
            demand.setCategory(new ItemCategory(demandRequestDto.getCategory().getId()));
        }
        if(demandRequestDto.getSubCategory()!=null) {
            demand.setSubCategory(new ItemCategory(demandRequestDto.getSubCategory().getId()));
        }
        if(demandRequestDto.getRequestedBy()!=null) {
            demand.setRequestedBy(new User(claimResolver.getUserId()));
        }
        // if(loggedInUser.getEmployee().getWarehouseId()!=null){
        //     demand.setWarehouse(new Warehouse(loggedInUser.getEmployee().getWarehouseId()));
        // }
        setDemandDetail(demandRequestDto, demand);
        demandRepository.save(demand);
        // setVerifiers(demand, verifiers);
        // setApprovers(demand, getApprovalPanels(uri, demandRequestDto));

        return this.getDemandDetail(demand.getId());
    }

    @Transactional
    private static void setDemandDetail(DemandRequestDto demandRequestDto, Demand demand) {
        demand.setDemandDetails(demandRequestDto.getDemandDetails().stream().map(demandDetailDto -> {
            DemandDetail demandDetail = new DemandDetail();
            if(demandDetailDto.getId()!=null){
                demandDetail.setId(demandDetailDto.getId());
            }
            if(demandDetailDto.getItem()!=null && demandDetailDto.getItem().getId()!=null) {
                demandDetail.setItem(new Item(demandDetailDto.getItem().getId()));
            }
            if(demandDetailDto.getSubCategory()!=null) {
                demandDetail.setItemCategory(new ItemCategory(demandDetailDto.getSubCategory().getId()));
            }
            if(demandDetailDto.getCategory()!=null) {
                demandDetail.setItemParentCategory(new ItemCategory(demandDetailDto.getCategory().getId()));
            }

            if(demandDetailDto.getBrand()!=null && demandDetailDto.getBrand().getId()!=null){
                demandDetail.setBrand(new CategoryBrand(demandDetailDto.getBrand().getId()));
            }

            demandDetail.setAttributes(demandDetailDto.getAttributes()
                    .stream().map(demandDetailAttribute -> {
                        demandDetailAttribute.setDemandDetail(demandDetail);
                        return demandDetailAttribute;
                    }).collect(Collectors.toList()));

            demandDetail.setRequestQuantity(demandDetailDto.getRequestQuantity());
            demandDetail.setDemand(demand);
            demandDetail.setPriority(demandDetailDto.getPriority());
            demandDetail.setSpecification(demandDetailDto.getSpecification());
            demandDetail.setStatus(demand.getStatus());
            return demandDetail;
        }).collect(Collectors.toList()));
    }

    @Override
    public void declineDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Page<?> getAllCloseDemands(Jwt loggedInUser, Optional<Integer> page, Optional<Integer> size,
            Optional<String> fromDate, Optional<String> toDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page<?> getAllDemands(Jwt loggedInUser, Optional<Integer> page, Optional<Integer> size,
            Optional<String> fromDateStr, Optional<String> toDateStr) {
                String moduleUri = "demand/pending";
                // get filter options according to module permission
                Sort sort = Sort.by(Sort.Direction.ASC,"id");
                Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);
        
                // Optional<Map<String,List<Long>>> modulePermission = moduleAccessPermissionService
                //             .getModulePermissionFilterByUri(loggedInUser,moduleUri);

                List<Long> categoryIds = new ArrayList<>();
                List<Long> warehouseIds = new ArrayList<>();
        
        
                LocalDateTime fromDate = null;
                LocalDateTime toDate = null;
                if(fromDateStr.isPresent()){
                    fromDate = LocalDateTime.parse(fromDateStr.get()+"T00:00:00");
                }
                if(toDateStr.isPresent()){
                    toDate = LocalDateTime.parse(toDateStr.get()+"T23:59:59");
                }
        
                // if(modulePermission.isPresent()){
                //     categoryIds = modulePermission.get().get("category_id");
                //     warehouseIds = modulePermission.get().get("warehouse_id");
                //     return demandRepository.findAllDemandsByCategory(categoryIds,
                //             (warehouseIds !=null && warehouseIds.size()>0)? warehouseIds : List.of(1L),
                //             fromDate,toDate,
                //             pageable);
                // }
        
        
                return demandRepository.findAllDemands(1L,fromDate,toDate,pageable);
    }

    @Override
    public Page<?> getAllPendingApprovalDemands(Jwt loggedInUser, Optional<Integer> page, Optional<Integer> size,
            Optional<String> fromDate, Optional<String> toDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page<?> getAllPendingVerificationDemands(Jwt loggedInUser, Optional<Integer> page, Optional<Integer> size,
            Optional<String> fromDate, Optional<String> toDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<DemandDetailResDto> getDemandDetail(Long id) {
        LocalDateTime localDateTime = LocalDateTime.now();

        List<DemandRepository.DemandDetailItem> demandList = demandRepository.findByDemandId(id);
        DemandDetailResDto resDto = DemandDetailResDto.builder().build();
        for(DemandRepository.DemandDetailItem demandDetailItem: demandList){

            List<DemandDetailAttribute> demandDetailAttrs= demandDetailAttributeRepository.findByDemandDetailId(demandDetailItem.getDemandDetailId());
            
            
            resDto.setDemandNo(demandDetailItem.getDemandNo());
            resDto.setDemandId(demandDetailItem.getDemandId());
            resDto.setDemandDate(demandDetailItem.getDemandDate());
            resDto.setDemandStatus(demandDetailItem.getDemandStatus());
            resDto.setEmpId(demandDetailItem.getEmpId());
            resDto.setEmployeeId(demandDetailItem.getEmployeeId());
            resDto.setEmployeeName(demandDetailItem.getEmployeeName());
            resDto.setReportingManager(demandDetailItem.getReportingManager());
            resDto.setDepartment(demandDetailItem.getDepartment());
            resDto.setDesignation(demandDetailItem.getDesignation());
            resDto.setWarehouseId(demandDetailItem.getWarehouseId());
            resDto.setWarehouseName(demandDetailItem.getWarehouseName());
            resDto.setWarehouseLocation(demandDetailItem.getWarehouseLocation());
            resDto.setNextApproverId(demandDetailItem.getNextApproverId());
            resDto.setNextVerifierId(demandDetailItem.getNextVerifierId());
            resDto.setReviewerId(demandDetailItem.getReviewerId());
            resDto.addDetail(
                
                DemandDetailItemResDto.builder()
                .itemId(demandDetailItem.getId())
                .attributes(demandDetailAttrs)
                .itemCategoryId(demandDetailItem.getItemCategoryId())
                .itemParentCategoryId(demandDetailItem.getItemParentCategoryId())
                .category(demandDetailItem.getCategory())
                .categoryCode(demandDetailItem.getCategoryCode())
                .parentCategory(demandDetailItem.getParentCategory())
                .parentCategoryCode(demandDetailItem.getParentCategoryCode())
                .name(demandDetailItem.getName())
                .code(demandDetailItem.getCode())
                .attributeTypes(demandDetailItem.getAttributeTypes())
                .attributeValues(demandDetailItem.getAttributeValues())
                .brandId(demandDetailItem.getBrandId())
                .brandName(demandDetailItem.getBrandName())
                .demandDetailId(demandDetailItem.getDemandDetailId())
                .prQty(demandDetailItem.getPrQty())
                .openPrQty(demandDetailItem.getOpenPrQty())
                .receivedNote(demandDetailItem.getReceiveNote())
                .storeNote(demandDetailItem.getStoreNote())
                .declineNote(demandDetailItem.getDeclineNote())
                .demandItemStatus(demandDetailItem.getDemandDetailStatus())
                .requestedQuantity(demandDetailItem.getRequestQuantity())
                .stockThresholdQty(demandDetailItem.getStockThresholdQty())
                .specification(demandDetailItem.getSpecification())
                .currentStock(demandDetailItem.getCurrentStockQty())
                .approvedQuantity(demandDetailItem.getApprovedQuantity())
                .demandPriority(demandDetailItem.getDemandPriority())
                .itemUnit(demandDetailItem.getItemUnit())
                        .inTransit(demandDetailItem.getInTransit())
//                .totalStockInCurrentMonth(demandDetailItem.getTotalStockInCurrentMonth())
//                .avgTotalConsumeInCurrentMonth(demandDetailItem.getAvgTotalConsumeInCurrentMonth())
//                .totalConsumeInCurrentMonth(demandDetailItem.getTotalConsumeInCurrentMonth())
                .build()
            );

        }

        if(resDto.getDemandId()!=null) {
            // List<VerificationResponse> verifiers = new ArrayList<>();
            // List<VerificationResponse> approvers = new ArrayList<>();
            // verificationService
            //         .getVerificationsByDomainTypeAndDomainId(DomainType.DEMAND, resDto.getDemandId())
            //         .stream().forEach(verifier->{
            //             if(verifier.getIsApproval()==false){
            //                 verifiers.add(verifier);
            //             }else{
            //                 approvers.add(verifier);
            //             }
            //         });
            // resDto.setVerifiers(verifiers);
            // resDto.setApprovers(approvers);
        }
        // List<?> comments = commentService.getCommentsByDomain(DomainType.DEMAND, resDto.getDemandId());
        resDto.setComments(new ArrayList<>());
        return Optional.ofNullable(resDto);
    }

    @Override
    public Page<?> getMyDemands(Jwt token, Optional<Integer> page, Optional<Integer> size,
            Optional<String> fromDateStr, Optional<String> toDateStr) {
        ClaimResolver claimResolver = new ClaimResolver();
        claimResolver.setToken(token);

    //    Optional<Employee> employeeOptional = employeeService.getEmployeeByUserId(loggedInUser.getId());

        // if(employeeOptional.isEmpty()){
        //     throw new AesException("No Employee Profile Found");
        // }
        Sort sort = Sort.by(Sort.Direction.DESC,"reviewDate","demandDate");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10),sort);

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+"T00:00:00");
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+"T23:59:59");
        }


        return demandRepository.findAllByRequestedById(claimResolver.getUserId(),fromDate,toDate,pageable);
    }

    @Override
    public String getNextDemandNo() {
        Optional<Long> demandOptional = demandRepository.findMaxOrderById();
        if(demandOptional.isPresent()){
            Long demandNo = demandOptional.get();
            Long newDemandNo = demandNo + 1;
            return String.format("%05d",newDemandNo);
        }
        return String.format("%05d",1);
    }

    @Override
    public void receiveDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rejectDemand(Jwt loggedInUser, DemandReceiveDto demandReceiveDto) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void rejectDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resendDemandItem(Jwt loggedInUser, DemandReceiveDto demandReceiveDto) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void reviewDemand(Jwt loggedInUser, Long id, ReviewDto reviewDto) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sentDemandItem(DemandReceiveDto demandReceiveDto) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Optional<DemandDetailResDto> updateDemand(Jwt loggedInUser, String uri, Long id,
            DemandRequestDto demandRequestDto) {
        
            if(id==null){
                throw new AesException("Sorry! demand id is missing");
            }
            Optional<Demand> demandOp = demandRepository.findById(id);
            if(demandOp.isEmpty()){
                throw new AesException("Sorry! demand not found for update");
            }
            Demand demand = demandOp.get();
            
            setDemandDetail(demandRequestDto, demand);
            demand.setReviewerId(null);
            demandRepository.save(demand);
            // remove all previous verification and approval request
            // verificationService.removeVerification(demand.getId(), DomainType.DEMAND);
            // dvahistoryRepository.deleteAllByDemandId(demand.getId());

            // Optional<Map<String,Object>> verifierOp = (Optional<Map<String,Object>>)verificationService
            //     .getVerifiers(loggedInUser,uri,demandRequestDto.getCategories());

            // setVerifiers(demand, getVerifiers(demand, verifierOp));
            // setApprovers(demand, getApprovalPanels(uri, demandRequestDto));

        return this.getDemandDetail(demand.getId());
    }
    
}
