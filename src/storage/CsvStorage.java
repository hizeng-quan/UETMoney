package storage;

import enums.Period;
import enums.TransactionType;
import enums.WalletType;
import interfaces.Storage;
import models.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Cài đặt Storage dùng định dạng CSV.
 * Mỗi loại dữ liệu được lưu vào một file riêng biệt.
 * Thể hiện tính đa hình: cùng interface Storage nhưng lưu trữ bằng CSV.
 */
public class CsvStorage implements Storage {

    private static final String DELIMITER = ",";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ======================== TRANSACTION ========================

    @Override
    public void saveTransactions(List<Transaction> transactions, String path) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            // Header
            writer.write("id,type,amount,date,note,categoryName,walletName,extraField,period");
            writer.newLine();

            for (Transaction t : transactions) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeCsv(t.getId())).append(DELIMITER);

                // Xác định type string
                String typeStr;
                if (t instanceof RecurringExpense) {
                    typeStr = "RECURRING";
                } else if (t instanceof Expense) {
                    typeStr = "EXPENSE";
                } else {
                    typeStr = "INCOME";
                }
                sb.append(typeStr).append(DELIMITER);
                sb.append(t.getAmount()).append(DELIMITER);
                sb.append(t.getDate().format(DATE_FORMAT)).append(DELIMITER);
                sb.append(escapeCsv(t.getNote())).append(DELIMITER);
                sb.append(escapeCsv(t.getCategory().getName())).append(DELIMITER);
                sb.append(escapeCsv(t.getWallet().getName())).append(DELIMITER);

                // Extra field: source (Income) hoặc paymentMethod (Expense)
                if (t instanceof Income) {
                    sb.append(escapeCsv(((Income) t).getSource()));
                } else if (t instanceof Expense) {
                    sb.append(escapeCsv(((Expense) t).getPaymentMethod()));
                }
                sb.append(DELIMITER);

                // Period (chỉ cho RecurringExpense)
                if (t instanceof RecurringExpense) {
                    RecurringExpense re = (RecurringExpense) t;
                    sb.append(re.getPeriod().name());
                    sb.append(DELIMITER);
                    // lastProcessedDate
                    if (re.getLastProcessedDate() != null) {
                        sb.append(re.getLastProcessedDate().format(DATE_FORMAT));
                    }
                } else {
                    sb.append(DELIMITER); // period rỗng
                    // lastProcessedDate rỗng
                }

                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }

    @Override
    public List<Transaction> loadTransactions(String path, List<Category> categories, List<Wallet> wallets) throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return transactions; // File chưa tồn tại → trả danh sách rỗng
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine(); // Bỏ qua header
            if (header == null) return transactions;

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 8) {
                        System.out.println("⚠ Dòng " + lineNumber + " bị thiếu dữ liệu, bỏ qua.");
                        continue;
                    }

                    String id = parts[0];
                    String typeStr = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    LocalDate date = LocalDate.parse(parts[3], DATE_FORMAT);
                    String note = parts[4];
                    String categoryName = parts[5];
                    String walletName = parts[6];
                    String extraField = parts.length > 7 ? parts[7] : "";
                    String periodStr = parts.length > 8 ? parts[8] : "";
                    String lastProcessedStr = parts.length > 9 ? parts[9] : "";

                    // Tìm category và wallet theo tên
                    Category category = findCategoryByName(categories, categoryName);
                    Wallet wallet = findWalletByName(wallets, walletName);

                    if (category == null) {
                        System.out.println("⚠ Dòng " + lineNumber + ": Không tìm thấy danh mục '" + categoryName + "', bỏ qua.");
                        continue;
                    }
                    if (wallet == null) {
                        System.out.println("⚠ Dòng " + lineNumber + ": Không tìm thấy ví '" + walletName + "', bỏ qua.");
                        continue;
                    }

                    Transaction transaction;
                    switch (typeStr) {
                        case "INCOME":
                            transaction = new Income(id, amount, date, category, note, wallet, extraField);
                            break;
                        case "RECURRING":
                            Period period = Period.valueOf(periodStr);
                            RecurringExpense re = new RecurringExpense(id, amount, note, date, category, wallet, extraField, period);
                            if (!lastProcessedStr.isEmpty()) {
                                re.setLastProcessedDate(LocalDate.parse(lastProcessedStr, DATE_FORMAT));
                            }
                            transaction = re;
                            break;
                        case "EXPENSE":
                        default:
                            transaction = new Expense(id, amount, note, date, category, wallet, extraField);
                            break;
                    }

                    transactions.add(transaction);
                } catch (Exception e) {
                    System.out.println("⚠ Lỗi đọc dòng " + lineNumber + ": " + e.getMessage() + ", bỏ qua.");
                }
            }
        }

        return transactions;
    }

    // ======================== WALLET ========================

    @Override
    public void saveWallets(List<Wallet> wallets, String path) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("type,name,balance,bankName,accountNumber,provider");
            writer.newLine();

            for (Wallet w : wallets) {
                StringBuilder sb = new StringBuilder();
                sb.append(w.getWalletType().name()).append(DELIMITER);
                sb.append(escapeCsv(w.getName())).append(DELIMITER);
                sb.append(w.getBalance()).append(DELIMITER);

                if (w instanceof BankAccount) {
                    BankAccount ba = (BankAccount) w;
                    sb.append(escapeCsv(ba.getBankName())).append(DELIMITER);
                    sb.append(escapeCsv(ba.getAccountNumber())).append(DELIMITER);
                } else {
                    sb.append(DELIMITER).append(DELIMITER); // bankName, accountNumber rỗng
                }

                if (w instanceof EWallet) {
                    sb.append(escapeCsv(((EWallet) w).getProvider()));
                }

                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }

    @Override
    public List<Wallet> loadWallets(String path) throws Exception {
        List<Wallet> wallets = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return wallets;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Bỏ header

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    String[] parts = parseCsvLine(line);
                    String typeStr = parts[0];
                    String name = parts[1];
                    double balance = Double.parseDouble(parts[2]);

                    WalletType type = WalletType.valueOf(typeStr);
                    Wallet wallet;
                    switch (type) {
                        case BANK:
                            String bankName = parts.length > 3 ? parts[3] : "";
                            String accNum = parts.length > 4 ? parts[4] : "";
                            wallet = new BankAccount(name, balance, bankName, accNum);
                            break;
                        case EWALLET:
                            String provider = parts.length > 5 ? parts[5] : "";
                            wallet = new EWallet(name, balance, provider);
                            break;
                        case CASH:
                        default:
                            wallet = new CashWallet(name, balance);
                            break;
                    }
                    wallets.add(wallet);
                } catch (Exception e) {
                    System.out.println("⚠ Lỗi đọc ví dòng " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        return wallets;
    }

    // ======================== CATEGORY ========================

    @Override
    public void saveCategories(List<Category> categories, String path) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("name,type");
            writer.newLine();

            for (Category c : categories) {
                writer.write(escapeCsv(c.getName()) + DELIMITER + c.getType().name());
                writer.newLine();
            }
        }
    }

    @Override
    public List<Category> loadCategories(String path) throws Exception {
        List<Category> categories = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return categories;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Bỏ header

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = parseCsvLine(line);
                if (parts.length >= 2) {
                    String name = parts[0];
                    TransactionType type = TransactionType.valueOf(parts[1]);
                    categories.add(new Category(name, type));
                }
            }
        }
        return categories;
    }

    // ======================== BUDGET ========================

    @Override
    public void saveBudgets(Map<Category, Budget> budgets, String path) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("categoryName,limit,period");
            writer.newLine();

            for (Map.Entry<Category, Budget> entry : budgets.entrySet()) {
                Budget b = entry.getValue();
                writer.write(escapeCsv(b.getCategory().getName()) + DELIMITER
                        + b.getLimit() + DELIMITER
                        + b.getPeriod().name());
                writer.newLine();
            }
        }
    }

    @Override
    public Map<Category, Budget> loadBudgets(String path, List<Category> categories) throws Exception {
        Map<Category, Budget> budgets = new HashMap<>();
        File file = new File(path);
        if (!file.exists()) return budgets;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Bỏ header

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = parseCsvLine(line);
                if (parts.length >= 3) {
                    String catName = parts[0];
                    double limit = Double.parseDouble(parts[1]);
                    Period period = Period.valueOf(parts[2]);

                    Category category = findCategoryByName(categories, catName);
                    if (category != null) {
                        budgets.put(category, new Budget(category, limit, period));
                    }
                }
            }
        }
        return budgets;
    }

    // ======================== HELPER METHODS ========================

    /**
     * Escape giá trị CSV: nếu chứa dấu phẩy, dấu ngoặc kép, hoặc xuống dòng
     * thì bọc trong ngoặc kép và escape ngoặc kép bên trong.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Parse một dòng CSV, hỗ trợ giá trị có dấu ngoặc kép.
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    // Kiểm tra escaped quote ""
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // Bỏ qua ký tự quote tiếp theo
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
                    result.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        result.add(current.toString()); // Thêm field cuối cùng

        return result.toArray(new String[0]);
    }

    private Category findCategoryByName(List<Category> categories, String name) {
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    private Wallet findWalletByName(List<Wallet> wallets, String name) {
        for (Wallet w : wallets) {
            if (w.getName().equalsIgnoreCase(name)) return w;
        }
        return null;
    }
}
