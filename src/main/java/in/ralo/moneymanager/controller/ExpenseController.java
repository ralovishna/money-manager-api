package in.ralo.moneymanager.controller;

import in.ralo.moneymanager.dto.ExpenseDTO;
import in.ralo.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> addExpense(@RequestBody ExpenseDTO expenseDTO) {
        ExpenseDTO savedExpense = expenseService.addExpense(expenseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExpense);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses() {
        List<ExpenseDTO> expenseDTOList = expenseService.getCurrentMonthExpensesForCurrentUser();
        return ResponseEntity.ok(expenseDTOList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable("id") Long expenseId) {
        expenseService.deleteExpenseById(expenseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/excel/download/expense")
    public ResponseEntity<ByteArrayResource> downloadExpenseDetails() {
        try {
            byte[] excelData = expenseService.generateExpenseExcel();
            ByteArrayResource resource = new ByteArrayResource(excelData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expense_details.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(excelData.length)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file: " + e.getMessage());
        }
    }

    @GetMapping("/email/expense-excel")
    public ResponseEntity<String> emailExpenseDetails() {
        try {
            expenseService.sendExpenseExcelEmail();
            return ResponseEntity.ok("{\"message\":\"Expense details emailed successfully\"}");
        } catch (Exception e) {
            throw new RuntimeException("Failed to email expense details: " + e.getMessage());
        }
    }
}