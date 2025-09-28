package in.ralo.moneymanager.service.serviceImpl;

import in.ralo.moneymanager.dto.ExpenseDTO;
import in.ralo.moneymanager.model.Category;
import in.ralo.moneymanager.model.Expense;
import in.ralo.moneymanager.model.Profile;
import in.ralo.moneymanager.repository.CategoryRepo;
import in.ralo.moneymanager.repository.ExpenseRepo;
import in.ralo.moneymanager.service.EmailService;
import in.ralo.moneymanager.service.ExpenseService;
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
public class ExpenseServiceImpl implements ExpenseService {

    private final CategoryRepo categoryRepo;
    private final ExpenseRepo expenseRepo;
    private final ProfileService profileService;
    private final EmailService emailService;

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

    @Override
    public void sendExpenseExcelEmail() throws IOException {
        byte[] excelData = generateExpenseExcel();

        // Prepare email content
        Profile currentUser = profileService.getCurrentProfile();
        String toEmail = currentUser.getEmail(); // Use current user's email
        String subject = "Expense Details Report";
        String body = "Dear User,<br><br>Please find your expense details attached.<br><br>Generated on: " + new java.util.Date() + "<br><br>Best regards,<br>Money Manager Team";

        // Use EmailService to send email with attachment
        try {
            emailService.sendMail(toEmail, subject, body, "expense_details.xlsx", excelData);
        } catch (Exception e) {
            throw new RuntimeException("Error sending email with Excel attachment: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateExpenseExcel() throws IOException {
        Profile currentUser = profileService.getCurrentProfile();
        List<Expense> expenses = expenseRepo.findByProfileIdOrderByDateDesc(currentUser.getId());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expense Details");

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
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(expense.getDate().toString());
                row.createCell(1).setCellValue(expense.getAmount().doubleValue());
                row.createCell(2).setCellValue(expense.getName() != null ? expense.getName() : "");
                row.createCell(3).setCellValue(expense.getCategory() != null ? expense.getCategory().getName() : "");
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