package in.ralo.moneymanager.controller;

import in.ralo.moneymanager.dto.ExpenseDTO;
import in.ralo.moneymanager.dto.FilterDTO;
import in.ralo.moneymanager.dto.IncomeDTO;
import in.ralo.moneymanager.service.ExpenseService;
import in.ralo.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filterDTO) {
        //preparing the data or validation
        LocalDate startDate = filterDTO.getStartDate() != null ? filterDTO.getStartDate() : null;
        LocalDate endDate = filterDTO.getEndDate() != null ? filterDTO.getEndDate() : null;
        String keyword = filterDTO.getKeyword() != null ? filterDTO.getKeyword() : "";
        String sortField = filterDTO.getSortField() != null ? filterDTO.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filterDTO.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        if ("income".equals(filterDTO.getType())) {
            List<IncomeDTO> incomeDTOList = incomeService.filterIncomes(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(incomeDTOList);
        } else if ("expense".equals(filterDTO.getType())) {
            List<ExpenseDTO> expenseDTOList = expenseService.filterExpenses(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(expenseDTOList);
        } else
            return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'");
    }
}