package in.ralo.moneymanager.service;

import in.ralo.moneymanager.dto.ExpenseDTO;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseDTO addExpense(ExpenseDTO expenseDTO);

    //get all current month expenses for current user
    List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser();

    //delete expense by id for current user
    void deleteExpenseById(Long expenseId);

    //get latest top 5 expenses for current user
    List<ExpenseDTO> getLatest5ExpenseForCurrentUser();

    //get total expenses for current user
    BigDecimal getTotalExpenseForCurrentUser();

    //filter expenses
    List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort);

    //notifications
    List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date);

    void sendExpenseExcelEmail() throws IOException;

    byte[] generateExpenseExcel() throws IOException;
}