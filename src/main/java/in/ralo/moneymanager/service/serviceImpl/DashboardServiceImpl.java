package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.dto.ExpenseDTO;
import in.ralo.moneymanager.dto.IncomeDTO;
import in.ralo.moneymanager.dto.RecentTransactionDTO;
import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.service.DashboardService;
import in.ralo.moneymanager.service.ExpenseService;
import in.ralo.moneymanager.service.IncomeService;
import in.ralo.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    @Override
    public Map<String, Object> getDashboardData() {

        Map<String, Object> returnValue = new LinkedHashMap<>();

        Profile currentUser = profileService.getCurrentProfile();
        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomeForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpenseForCurrentUser();
        List<RecentTransactionDTO> recentTransactionDTOList = concat(latestIncomes.stream().map(
                        incomeDTO -> RecentTransactionDTO.builder()
                                .id(incomeDTO.getId())
                                .profileId(currentUser.getId())
                                .name(incomeDTO.getName())
                                .icon(incomeDTO.getIcon())
                                .amount(incomeDTO.getAmount())
                                .date(incomeDTO.getDate())
                                .createdAt(incomeDTO.getCreatedAt())
                                .updatedAt(incomeDTO.getUpdatedAt())
                                .type("income")
                                .build()),
                latestExpenses.stream().map(
                        expenseDTO -> RecentTransactionDTO.builder()
                                .id(expenseDTO.getId())
                                .profileId(currentUser.getId())
                                .name(expenseDTO.getName())
                                .icon(expenseDTO.getIcon())
                                .amount(expenseDTO.getAmount())
                                .date(expenseDTO.getDate())
                                .createdAt(expenseDTO.getCreatedAt())
                                .updatedAt(expenseDTO.getUpdatedAt())
                                .type("expense")
                                .build()))
                .sorted((a, b) -> {
                    int compare = b.getDate().compareTo(a.getDate());

                    if (compare == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null)
                        return b.getCreatedAt().compareTo(a.getCreatedAt());

                    return compare;
                }).toList();

        returnValue.put("totalBalance", incomeService.getTotalIncomeForCurrentUser()
                .subtract(expenseService.getTotalExpenseForCurrentUser()));
        returnValue.put("totalIncome", incomeService.getTotalIncomeForCurrentUser());
        returnValue.put("totalExpense", expenseService.getTotalExpenseForCurrentUser());
        returnValue.put("recent5Incomes", latestIncomes);
        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recentTransactions", recentTransactionDTOList);

        return returnValue;
    }
}