package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.dto.ExpenseDTO;
import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.repository.ProfileRepo;
import in.ralo.moneymanager.service.EmailService;
import in.ralo.moneymanager.service.ExpenseService;
import in.ralo.moneymanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final ProfileRepo profileRepo;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "IST") // once a day
    public void sendDailyIncomeExpenseRemainder() {
        log.info("Job started: sendDailyIncomeExpenseRemainder()");
        List<Profile> profiles = profileRepo.findAll();

        for (Profile profile : profiles) {
            String body = "Hi " + profile.getFullName() + "<br><br>"
                    + "This is a friendly remainder to add income and expense for today in Money Manager app.<br><br>"
                    + "<a href=" + frontendUrl + " style='background-color:#4CAF50; color:#fff; text-decoration:none; border-radius:5px; font-weight:bold;'>Go to Money Manager</a>"
                    + "<br><br>Best regards,<br>Money Manager Team";

            emailService.sendMail(profile.getEmail(), "Daily remainder: Add your income and Expenses", body);
        }
        log.info("Job completed: sendDailyIncomeExpenseRemainder()");
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "IST") // once a day
    public void sendDailyExpenseSummary() {
        log.info("Job started: sendDailyExpenseSummary()");
        List<Profile> profiles = profileRepo.findAll();

        for (Profile profile : profiles) {
            List<ExpenseDTO> todayExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());

            if (!todayExpenses.isEmpty()) {
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse:collapse; width:100%'>");
                table.append("<tr style='background-color:#f2f2f2;'> <th style='border:1px solid #ddd; padding: 8px;'>S.No</th> <th style='border:1px solid #ddd; padding: 8px;'>Name</th> <th style='border:1px solid #ddd; padding: 8px;'>Amount</th> <th style='border:1px solid #ddd; padding: 8px;'>Category</th> </tr>");
                int i = 1;
                for (ExpenseDTO todayExpense : todayExpenses) {
                    table.append("<tr>");
                    table.append("<td style ='border:1px solid #ddd; padding:8px;'>").append(i++).append("</td>");
                    table.append("<td style ='border:1px solid #ddd; padding:8px;'>").append(todayExpense.getName()).append("</td>");
                    table.append("<td style ='border:1px solid #ddd; padding:8px;'>").append(todayExpense.getAmount()).append("</td>");
                    table.append("<td style ='border:1px solid #ddd; padding:8px;'>").append(todayExpense.getCategoryId() != null ? todayExpense.getCategoryName() : "N/A").append("</td>");
                    table.append("</tr>");
                }
                table.append("</table>");
                String body = "Hi " + profile.getFullName() + ",<br><br> Here is a summary of your expenses for today: <br><br>" + table + "<br><br>Best regards,<br>Money Manager Team";

                emailService.sendMail(profile.getEmail(), "Your daily Expense summary", body);
            }
        }
        log.info("Job completed: sendDailyExpenseSummary()");
    }
}