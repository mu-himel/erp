package com.agi.aesl.erpscm.email.service;

public interface EmailSenderService {

    void refreshRecipient();
    void addRecipient(String recipient);
    void sendEmail(String subject, String mailContent);
}
