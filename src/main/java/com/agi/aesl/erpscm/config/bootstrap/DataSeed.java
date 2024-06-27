package com.agi.aesl.erpscm.config.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.agi.aesl.erpscm.network.NetworkService;

@Component
public class DataSeed implements CommandLineRunner{
    
    @Autowired
    private NetworkService networkService;

    private void initialize(){

    }

    @Override
    public void run(String... args) throws Exception {
       this.initialize();
        
    }


    
}
