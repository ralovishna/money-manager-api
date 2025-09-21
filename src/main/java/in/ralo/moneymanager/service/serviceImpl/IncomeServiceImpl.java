package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.dto.IncomeDTO;
import in.ralo.moneymanager.model.Category;
import in.ralo.moneymanager.model.Income;
import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.repository.CategoryRepo;
import in.ralo.moneymanager.repository.IncomeRepo;
import in.ralo.moneymanager.service.IncomeService;
import in.ralo.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final CategoryRepo categoryRepo;
    private final IncomeRepo incomeRepo;
    private final ProfileService profileService;

    @Override
    public IncomeDTO addIncome(IncomeDTO incomeDTO) {
        Profile currentUser = profileService.getCurrentProfile();
        Category category = categoryRepo.findById(incomeDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found " + incomeDTO.getCategoryId()));
        Income newIncome = toEntity(incomeDTO, currentUser, category);

        return toDTO(incomeRepo.save(newIncome));
    }

    //get all current month incomes for current user
    @Override
    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        Profile currentUser = profileService.getCurrentProfile();
        LocalDate date = LocalDate.now();
        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());
        List<Income> incomes = incomeRepo.findByProfileIdAndDateBetween(currentUser.getId(), startDate, endDate);

        return incomes.stream().map(this::toDTO).toList();
    }

    //delete expense by id for current user
    @Override
    public void deleteIncomeById(Long incomeId) {
        Profile currentUser = profileService.getCurrentProfile();
        Income incomeToBeDeleted = incomeRepo.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found with " + incomeId));

        if (!incomeToBeDeleted.getProfile().getId().equals(currentUser.getId()))
            throw new RuntimeException("Unauthorized to delete this expense");

        incomeRepo.delete(incomeToBeDeleted);
    }

    //get latest top 5 incomes for current user
    @Override
    public List<IncomeDTO> getLatest5IncomeForCurrentUser() {
        Profile currentUser = profileService.getCurrentProfile();
        List<Income> incomes = incomeRepo.findTop5ByProfileIdOrderByDateDesc(currentUser.getId());

        return incomes.stream().map(this::toDTO).toList();
    }

    //get total incomes for current user
    @Override
    public BigDecimal getTotalIncomeForCurrentUser() {
        Profile currentUser = profileService.getCurrentProfile();
        BigDecimal total = incomeRepo.findTotalIncomeByProfileId(currentUser.getId());

        return total != null ? total : BigDecimal.ZERO;
    }

    //filter incomes
    @Override
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        Profile currentUser = profileService.getCurrentProfile();
        List<Income> incomeDTOList = incomeRepo.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(currentUser.getId(), startDate, endDate, keyword, sort);

        return incomeDTOList.stream().map(this::toDTO).toList();
    }

    // helper methods
    public Income toEntity(IncomeDTO incomeDTO, Profile profile, Category category) {
        if (incomeDTO == null)
            return null;

        return Income.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    public IncomeDTO toDTO(Income income) {
        if (income == null)
            return null;

        return IncomeDTO.builder()
                .id(income.getId())
                .name(income.getName())
                .icon(income.getIcon())
                .amount(income.getAmount())
                .categoryId(income.getCategory() != null ? income.getCategory().getId() : null)
                .categoryName(income.getCategory() != null ? income.getCategory().getName() : null)
                .date(income.getDate())
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }
}