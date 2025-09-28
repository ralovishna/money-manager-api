package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    public void sendMail(String toEmail, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("a39krishna@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            javaMailSender.send(message);

            System.out.println("✅ Email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail + ": " + e.getMessage());
        }
    }

    @Override
    public void sendMail(String toEmail, String subject, String body, String attachmentFileName, byte[] excelData) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("a39krishna@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true for HTML

            // Add attachment
            ByteArrayDataSource dataSource = new ByteArrayDataSource(excelData, "application/vnd.ms-excel");
            helper.addAttachment(attachmentFileName, dataSource);

            javaMailSender.send(message);

            System.out.println("✅ Email with attachment sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email with attachment to " + toEmail + ": " + e.getMessage());
        }
    }
}