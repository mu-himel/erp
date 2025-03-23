package com.agi.aesl.erpscm.email.service;

import com.agi.aesl.erpscm.email.config.EmailServerConfig;
import com.agi.aesl.erpscm.email.dto.request.EmailSentRequestDto;
import com.agi.aesl.erpscm.email.dto.response.EmailLoginResponse;
import com.agi.aesl.erpscm.email.dto.response.EmailSentResponse;
import com.agi.aesl.erpscm.network.NetworkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService{

    private List<String> recipients = new ArrayList<>();


    private final EmailServerConfig emailServerConfig;


    private final NetworkService networkService;

    @Override
    public void refreshRecipient() {
        this.recipients=new ArrayList<>();
    }

    @Override
    public void addRecipient(String recipient) {
        this.recipients.add(recipient);
    }

    @Async
    @Override
    public void sendEmail(String subject, String mailContent) {
        if(!recipients.isEmpty()){
            Map<String,Object> loginDto = new HashMap<>();
            loginDto.put("username", emailServerConfig.getUser());
            loginDto.put("password", emailServerConfig.getPassword());
            String token = login(loginDto);
            if(token != null){
                try {
                    send(token, subject, mailContent, recipients);
                }catch (RuntimeException ex){
                    log.error(ex.getMessage());
                }
            }
        }
    }

    private String login(Map<String,Object> loginDto){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> payload = new HttpEntity<>(loginDto,headers);
        ResponseEntity<EmailLoginResponse> response = networkService.post(emailServerConfig.getLoginUrl(),
                payload,EmailLoginResponse.class);
        if(response!=null && response.getBody()!=null){

            EmailLoginResponse body = response.getBody();
            LinkedHashMap<String,Object> content = body!=null? (LinkedHashMap<String, Object>) body.getContent() : null;
            if(content!=null) {
                return (String) content.get("token");
            }
        }
        return null;
    }

    private void send(String token, String subject, String mailContent, List<String> to){
        HttpHeaders headers = networkService.setHttpHeaders(token);
        EmailSentRequestDto emailSentRequestDto = new EmailSentRequestDto();
        emailSentRequestDto.setSubject(subject);
        emailSentRequestDto.setContent(mailContent);
        emailSentRequestDto.setTo(to);
        HttpEntity<EmailSentRequestDto> payload = new HttpEntity<>(emailSentRequestDto,headers);
        ResponseEntity<EmailSentResponse> response = networkService.post(emailServerConfig.getSentUrl(),
                payload, EmailSentResponse.class);
        if(response!=null && response.getBody()!=null){

            log.info("email sent successfully");
        }
    }
}
