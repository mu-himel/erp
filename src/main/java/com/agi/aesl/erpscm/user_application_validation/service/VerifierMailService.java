package com.agi.aesl.erpscm.user_application_validation.service;

public interface VerifierMailService<T> {
    void prepareMailContent(String name, String actionType, T domain);
    void sentMail(String to, String subject);
}
