package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.dto.IncomeDTO;
import in.ralo.moneymanager.model.Category;
import in.ralo.moneymanager.model.Income;
import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.repository.CategoryRepo;
import in.ralo.moneymanager.repository.IncomeRepo;
import in.ralo.moneymanager.service.EmailService;
import in.ralo.moneymanager.service.IncomeService;
import in.ralo.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final CategoryRepo categoryRepo;
    private final IncomeRepo incomeRepo;
    private final EmailService emailService;
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

    @Override
    public byte[] generateIncomeExcel() throws IOException {
        Profile currentUser = profileService.getCurrentProfile();
        List<Income> incomes = incomeRepo.findByProfileIdOrderByDateDesc(currentUser.getId());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Income Details");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Date", "Amount", "Name", "Category"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Data rows
            int rowNum = 1;
            for (Income income : incomes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(income.getDate().toString());
                row.createCell(1).setCellValue(income.getAmount().doubleValue());
                row.createCell(2).setCellValue(income.getName() != null ? income.getName() : "");
                row.createCell(3).setCellValue(income.getCategory() != null ? income.getCategory().getName() : "");
            }

            // Write to byte array
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new IOException("Error generating Excel file: " + e.getMessage());
        }
    }

    @Override
    public void sendIncomeExcelEmail() throws IOException {
        byte[] excelData = generateIncomeExcel();

        // Prepare email content
        Profile currentUser = profileService.getCurrentProfile();
        String toEmail = currentUser.getEmail(); // Use current user's email
        String subject = "Income Details Report";
        String body = "Dear User,<br><br>Please find your income details attached.<br><br>Generated on: " + new java.util.Date() + "<br><br>Best regards,<br>Money Manager Team";

        // Use EmailService to send email with attachment
        try {
            emailService.sendMail(toEmail, subject, body, "income_details.xlsx", excelData);
        } catch (Exception e) {
            throw new RuntimeException("Error sending email with Excel attachment: " + e.getMessage());
        }
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