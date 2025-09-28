package in.ralo.moneymanager.service;

import in.ralo.moneymanager.dto.IncomeDTO;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {
    IncomeDTO addIncome(IncomeDTO incomeDTO);

    //get all current month incomes for current user
    List<IncomeDTO> getCurrentMonthIncomesForCurrentUser();

    //delete expense by id for current user
    void deleteIncomeById(Long incomeId);

    //get latest top 5 incomes for current user
    List<IncomeDTO> getLatest5IncomeForCurrentUser();

    //get total incomes for current user
    BigDecimal getTotalIncomeForCurrentUser();

    //filter incomes
    List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort);

    byte[] generateIncomeExcel() throws IOException;

    void sendIncomeExcelEmail() throws IOException, MessagingException;
}