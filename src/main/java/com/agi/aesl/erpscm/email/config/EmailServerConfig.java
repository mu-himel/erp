package com.agi.aesl.erpscm.email.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mail")
public class EmailServerConfig {

    private String address;
    private String loginUrl;
    private String sendUrl;
    private String user;
    private String password;
    private String resetPassLink;

    public String getLoginUrl(){
        StringBuilder sb = new StringBuilder();
        sb.append(address).append(loginUrl);
        return sb.toString();
    }

    public String getSentUrl(){
        StringBuilder sb = new StringBuilder();
        sb.append(address).append(sendUrl);
        return sb.toString();
    }
}
