package in.ralo.moneymanager.service;

public interface EmailService {
    void sendMail(String toEmail, String subject, String body);
}