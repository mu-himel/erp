package com.agi.aesl.erpscm.user_application_validation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.user_application_validation.dto.response.Verifier;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;

@Service
public class UserApplicationValidatorServiceImpl implements UserApplicationValidatorService{

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private UserApplicationValidationRepository verificationRepository;

    @Override
    public Optional<?> getVerifiers(ClaimResolver claimResolver, String uri, String criteriaGroup, String categories) {
        
        // Map<String,Object> data = new HashMap<>();
        Optional<?> moduleVerifierConfigs = moduleService.getVerifierConfigByModuleAndCriteriaGroup(claimResolver, uri, criteriaGroup, categories);
        // Optional<VerifierConfig> verifierConfigOp = moduleVerifierConfigs.stream().findFirst();
        
        // if(claimResolver.getEmployee()!=null && claimResolver.getEmployee().isPresent() && verifierConfigOp.isPresent()){
        //     VerifierConfig verifierConfig = verifierConfigOp.get();
        //     Employee employee = claimResolver.getEmployee().get();
        //     if (employee.getReportingManagerId() == null && employee
        //                     .getLevel().equals(verifierConfig.getLevel())) {

        //         data = prepareVerifierResponse(employee,false, new ArrayList<>());

        //     }else if(employee.getReportingManagerId() == null &&
        //         employee.getLevel()>verifierConfig.getLevel()) {

        //     }else if(employee.getReportingManagerId() !=null &&
        //         employee.getLevel()>=verifierConfig.getLevel()){

        //     }
        //     return Optional.ofNullable(data);
        // }
        
        return Optional.empty();
    }

    private Map<String,Object> prepareVerifierResponse(Employee employee,
                                                       Boolean verificationStatus,
                                                       List<?> verifers){
        Map<String,Object> data = new HashMap<>();
        data.put("verificationRequired",verificationStatus);
        data.put("employeeDepartment", employee.getDepartmentId());
        data.put("employeeDepartmentLevel", employee.getLevel());
        // data.put("parentDepartment",employee.getParentDepartmentId());
        data.put("reportingManager", employee.getReportingManagerId());
        data.put("verifiers",verifers);
        return data;
    }

    private List<Verifier> getReportingManagers(Employee employee, Integer fromLevel, Integer toLevel) {
        List<Verifier> verifiers =  verificationRepository.getVerifierPanel(employee,fromLevel,toLevel);
        List<Verifier> filteredVerifiers = new ArrayList<>();
        if(verifiers.size()>0){            
            filteredVerifiers.add(verifiers.get(0));
            //
            for(int i=0,j=1; i<verifiers.size()-1;i++){
                Verifier verifier = verifiers.get(i);
                Verifier nextVerifier = verifiers.get(j);
                if(verifier.getParentDepartmentId().equals(nextVerifier.getDepartmentId())){
                    filteredVerifiers.add(nextVerifier);
                }
                j++;
            }
        }
        return filteredVerifiers;
    }
    
}
