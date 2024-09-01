package com.agi.aesl.erpscm.config.bootstrap;

import com.agi.aesl.erpscm.common.AppVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.agi.aesl.erpscm.network.NetworkService;

@Component
public class DataSeed implements CommandLineRunner{
    
    @Autowired
    private NetworkService networkService;

    @Autowired
    private AppVersion appVersion;

    private void initialize(){
        appVersion.showVersion();
    }

    @Override
    public void run(String... args) throws Exception {
       this.initialize();
        
    }


    
}
