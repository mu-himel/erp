package com.agi.aesl.erpscm.common;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@NoArgsConstructor
@Data
@Component
public class AppVersion {

    @Value("${spring.application.version}")
    private String version;

    public void showVersion(){
        log.info(version);
    }

}
