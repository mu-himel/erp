package com.agi.aesl.erpscm.config.bootstrap;

import com.agi.aesl.erpscm.common.AppVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.agi.aesl.erpscm.network.NetworkService;

@Component
@RequiredArgsConstructor
public class DataSeed implements CommandLineRunner{
    

    private final NetworkService networkService;

    private final AppVersion appVersion;

    private void initialize(){
        appVersion.showVersion();
    }

    @Override
    public void run(String... args) throws Exception {
       this.initialize();
        
    }


    
}
