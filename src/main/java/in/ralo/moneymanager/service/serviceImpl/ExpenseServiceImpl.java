package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.dto.ExpenseDTO;
import in.ralo.moneymanager.model.Category;
import in.ralo.moneymanager.model.Expense;
import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.repository.CategoryRepo;
import in.ralo.moneymanager.repository.ExpenseRepo;
import in.ralo.moneymanager.service.ExpenseService;
import in.ralo.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final CategoryRepo categoryRepo;
    private final ExpenseRepo expenseRepo;
    private final ProfileService profileService;

    @Override
    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {
        Profile currentUser = profileService.getCurrentProfile();
        Category category = categoryRepo.findById(expenseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found " + expenseDTO.getCategoryId()));
        Expense newExpense = toEntity(expenseDTO, currentUser, category);

        return toDTO(expenseRepo.save(newExpense));
    }

    //get all current month expenses for current user
    @Override
    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        Profile currentUser = profileService.getCurrentProfile();
        LocalDate date = LocalDate.now();
        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());
        List<Expense> expenses = expenseRepo.findByProfileIdAndDateBetween(currentUser.getId(), startDate, endDate);

        return expenses.stream().map(this::toDTO).toList();
    }

    //delete expense by id for current user
    @Override
    public void deleteExpenseById(Long expenseId) {
        Profile currentUser = profileService.getCurrentProfile();
        Expense expenseToBeDeleted = expenseRepo.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with " + expenseId));

        if (!expenseToBeDeleted.getProfile().getId().equals(currentUser.getId()))
            throw new RuntimeException("Unauthorized to delete this expense");

        expenseRepo.delete(expenseToBeDeleted);
    }

    //get latest top 5 expenses for current user
    @Override
    public List<ExpenseDTO> getLatest5ExpenseForCurrentUser() {
        Profile currentUser = profileService.getCurrentProfile();
        List<Expense> expenses = expenseRepo.findTop5ByProfileIdOrderByDateDesc(currentUser.getId());

        return expenses.stream().map(this::toDTO).toList();
    }

    //get total expenses for current user
    @Override
    public BigDecimal getTotalExpenseForCurrentUser() {
        Profile currentUser = profileService.getCurrentProfile();
        BigDecimal total = expenseRepo.findTotalExpenseByProfileId(currentUser.getId());

        return total != null ? total : BigDecimal.ZERO;
    }

    //filter expenses
    @Override
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        Profile currentUser = profileService.getCurrentProfile();
        List<Expense> expenseDTOList = expenseRepo.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(currentUser.getId(), startDate, endDate, keyword, sort);

        return expenseDTOList.stream().map(this::toDTO).toList();
    }

    //notifications
    @Override
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<Expense> expenseDTOList = expenseRepo.findByProfileIdAndDate(profileId, date);

        return expenseDTOList.stream().map(this::toDTO).toList();
    }

    // helper methods
    public Expense toEntity(ExpenseDTO expenseDTO, Profile profile, Category category) {
        if (expenseDTO == null)
            return null;

        return Expense.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    public ExpenseDTO toDTO(Expense expense) {
        if (expense == null)
            return null;

        return ExpenseDTO.builder()
                .id(expense.getId())
                .name(expense.getName())
                .icon(expense.getIcon())
                .amount(expense.getAmount())
                .categoryId(expense.getCategory() != null ? expense.getCategory().getId() : null)
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}