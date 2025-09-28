package in.ralo.moneymanager.controller;

import in.ralo.moneymanager.dto.IncomeDTO;
import in.ralo.moneymanager.service.IncomeService;
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
@RequestMapping("/incomes")
public class IncomeController {
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDTO> addIncome(@RequestBody IncomeDTO incomeDTO) {
        IncomeDTO savedIncome = incomeService.addIncome(incomeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIncome);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getIncomes() {
        List<IncomeDTO> incomeDTOList = incomeService.getCurrentMonthIncomesForCurrentUser();
        return ResponseEntity.ok(incomeDTOList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable("id") Long incomeId) {
        incomeService.deleteIncomeById(incomeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/excel/download/income")
    public ResponseEntity<ByteArrayResource> downloadIncomeDetails() {
        try {
            byte[] excelData = incomeService.generateIncomeExcel();
            ByteArrayResource resource = new ByteArrayResource(excelData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income_details.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(excelData.length)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file: " + e.getMessage());
        }
    }

    @GetMapping("/email/income-excel")
    public ResponseEntity<String> emailIncomeDetails() {
        try {
            incomeService.sendIncomeExcelEmail();
            return ResponseEntity.ok("{\"message\":\"Income details emailed successfully\"}");
        } catch (Exception e) {
            throw new RuntimeException("Failed to email income details: " + e.getMessage());
        }
    }
}