package storage;

import enums.Period;
import enums.TransactionType;
import interfaces.Storage;
import models.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt Storage bằng file CSV.
 * Format mỗi dòng: type,id,amount,date,categoryName,categoryType,walletName,note,extraField,period
 * - type: INCOME / EXPENSE / RECURRING
 * - extraField: source (Income) hoặc paymentMethod (Expense/Recurring)
 * - period: DAILY/WEEKLY/MONTHLY/YEARLY (chỉ có ở RECURRING, để trống nếu không phải)
 */
public class CsvStorage implements Storage {
    private static final String HEADER = "type,id,amount,date,categoryName,categoryType,walletName,note,extraField,period";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String SEPARATOR = ",";

    @Override
    public void save(List<Transaction> transactions, String path) throws Exception {
        File file = new File(path);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(HEADER);
            writer.newLine();

            for (Transaction t : transactions) {
                String line = transactionToCSV(t);
                writer.write(line);
                writer.newLine();
            }
        }
    }

    @Override
    public List<Transaction> load(String path) throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(path);

        if (!file.exists()) {
            return transactions; // Trả về danh sách rỗng nếu file chưa tồn tại
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine(); // Bỏ qua header

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    Transaction t = csvToTransaction(line);
                    if (t != null) {
                        transactions.add(t);
                    }
                } catch (Exception e) {
                    System.out.println("Cảnh báo: Không thể đọc dòng CSV: " + line);
                    System.out.println("  Lý do: " + e.getMessage());
                }
            }
        }
        return transactions;
    }

    /**
     * Chuyển đổi một Transaction thành dòng CSV.
     */
    private String transactionToCSV(Transaction t) {
        StringBuilder sb = new StringBuilder();

        // Xác định type
        String type;
        String extraField;
        String period = "";

        if (t instanceof RecurringExpense) {
            RecurringExpense re = (RecurringExpense) t;
            type = "RECURRING";
            extraField = re.getPaymentMethod();
            period = re.getPeriod().name();
        } else if (t instanceof Income) {
            type = "INCOME";
            extraField = ((Income) t).getSource();
        } else if (t instanceof Expense) {
            type = "EXPENSE";
            extraField = ((Expense) t).getPaymentMethod();
        } else {
            type = "UNKNOWN";
            extraField = "";
        }

        sb.append(type).append(SEPARATOR);
        sb.append(escapeCSV(t.getId())).append(SEPARATOR);
        sb.append(t.getAmount()).append(SEPARATOR);
        sb.append(t.getDate().format(DATE_FORMAT)).append(SEPARATOR);
        sb.append(escapeCSV(t.getCategory().getName())).append(SEPARATOR);
        sb.append(t.getCategory().getType().name()).append(SEPARATOR);
        sb.append(escapeCSV(t.getWallet().getName())).append(SEPARATOR);
        sb.append(escapeCSV(t.getNote())).append(SEPARATOR);
        sb.append(escapeCSV(extraField)).append(SEPARATOR);
        sb.append(period);

        return sb.toString();
    }

    /**
     * Parse một dòng CSV thành Transaction.
     * Cần tham chiếu Category và Wallet — ở đây tạo mới tạm thời,
     * sau đó ExpenseManager sẽ hợp nhất (merge) với danh sách đã có.
     */
    private Transaction csvToTransaction(String line) {
        String[] parts = parseCSVLine(line);
        if (parts.length < 9) {
            throw new IllegalArgumentException("Dong CSV khong du truong (can it nhat 9, co " + parts.length + ")");
        }

        String type = parts[0].trim();
        String id = unescapeCSV(parts[1].trim());
        double amount = Double.parseDouble(parts[2].trim());
        LocalDate date = LocalDate.parse(parts[3].trim(), DATE_FORMAT);
        String categoryName = unescapeCSV(parts[4].trim());
        TransactionType categoryType = TransactionType.valueOf(parts[5].trim());
        String walletName = unescapeCSV(parts[6].trim());
        String note = unescapeCSV(parts[7].trim());
        String extraField = unescapeCSV(parts[8].trim());

        // Tạo Category và Wallet tạm (CashWallet mặc định, balance = 0)
        Category category = new Category(categoryName, categoryType);
        Wallet wallet = new CashWallet(walletName, 0);

        switch (type) {
            case "INCOME":
                return new Income(id, amount, date, category, note, wallet, extraField);
            case "EXPENSE":
                return new Expense(id, amount, note, date, category, wallet, extraField);
            case "RECURRING":
                Period period = Period.MONTHLY; // Mặc định
                if (parts.length > 9 && !parts[9].trim().isEmpty()) {
                    period = Period.valueOf(parts[9].trim());
                }
                return new RecurringExpense(id, amount, note, date, category, wallet, extraField, period);
            default:
                throw new IllegalArgumentException("Loai giao dich khong hop le: " + type);
        }
    }

    /**
     * Escape giá trị CSV: nếu chứa dấu phẩy hoặc dấu nháy kép, bọc trong dấu nháy kép.
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Unescape giá trị CSV: bỏ dấu nháy kép bọc ngoài.
     */
    private String unescapeCSV(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\"");
        }
        return value;
    }

    /**
     * Parse dòng CSV có hỗ trợ dấu nháy kép (quoted fields).
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    // Kiểm tra escaped quote ""
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // Bỏ qua ký tự kế tiếp
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString()); // Thêm field cuối cùng

        return fields.toArray(new String[0]);
    }
}