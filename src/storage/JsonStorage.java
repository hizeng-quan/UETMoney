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
 * Cài đặt Storage dùng định dạng JSON.
 * Thể hiện tính đa hình: cùng interface Storage nhưng hành vi lưu trữ khác CsvStorage.
 */
public class JsonStorage implements Storage {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void saveTransactions(List<Transaction> transactions, String path) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("[\n");
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                writer.write("  {\n");

                String typeStr;
                if (t instanceof RecurringExpense) {
                    typeStr = "RECURRING";
                } else if (t instanceof Expense) {
                    typeStr = "EXPENSE";
                } else {
                    typeStr = "INCOME";
                }

                writeJsonField(writer, "id", t.getId(), true);
                writeJsonField(writer, "type", typeStr, true);
                writeJsonField(writer, "amount", String.valueOf(t.getAmount()), false);
                writeJsonField(writer, "date", t.getDate().format(DATE_FORMAT), true);
                writeJsonField(writer, "note", t.getNote(), true);
                writeJsonField(writer, "categoryName", t.getCategory().getName(), true);
                writeJsonField(writer, "walletName", t.getWallet().getName(), true);

                if (t instanceof Income) {
                    writeJsonField(writer, "source", ((Income) t).getSource(), true);
                } else if (t instanceof Expense) {
                    writeJsonField(writer, "paymentMethod", ((Expense) t).getPaymentMethod(), true);
                }

                if (t instanceof RecurringExpense) {
                    RecurringExpense re = (RecurringExpense) t;
                    writeJsonField(writer, "period", re.getPeriod().name(), true);
                    String lastProcessed = re.getLastProcessedDate() != null
                            ? re.getLastProcessedDate().format(DATE_FORMAT) : "";
                    writeJsonLastField(writer, "lastProcessedDate", lastProcessed, true);
                } else {
                    // Trường cuối cùng không có dấu phẩy
                    writeJsonLastField(writer, "recurring", "false", false);
                }

                writer.write("  }");
                if (i < transactions.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("]\n");
        }
    }

    @Override
    public List<Transaction> loadTransactions(String path, List<Category> categories, List<Wallet> wallets) throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return transactions;

        String content = readFileContent(file);
        List<Map<String, String>> objects = parseJsonArray(content);

        for (Map<String, String> obj : objects) {
            try {
                String id = obj.getOrDefault("id", "");
                String typeStr = obj.getOrDefault("type", "EXPENSE");
                double amount = Double.parseDouble(obj.getOrDefault("amount", "0"));
                LocalDate date = LocalDate.parse(obj.getOrDefault("date", "2026-01-01"), DATE_FORMAT);
                String note = obj.getOrDefault("note", "");
                String categoryName = obj.getOrDefault("categoryName", "");
                String walletName = obj.getOrDefault("walletName", "");

                Category category = findByName(categories, categoryName);
                Wallet wallet = findWalletByName(wallets, walletName);
                if (category == null || wallet == null) continue;

                Transaction transaction;
                switch (typeStr) {
                    case "INCOME":
                        String source = obj.getOrDefault("source", "");
                        transaction = new Income(id, amount, date, category, note, wallet, source);
                        break;
                    case "RECURRING":
                        String pm = obj.getOrDefault("paymentMethod", "");
                        Period period = Period.valueOf(obj.getOrDefault("period", "MONTHLY"));
                        RecurringExpense re = new RecurringExpense(id, amount, note, date, category, wallet, pm, period);
                        String lpd = obj.getOrDefault("lastProcessedDate", "");
                        if (!lpd.isEmpty()) {
                            re.setLastProcessedDate(LocalDate.parse(lpd, DATE_FORMAT));
                        }
                        transaction = re;
                        break;
                    default:
                        String payMethod = obj.getOrDefault("paymentMethod", "");
                        transaction = new Expense(id, amount, note, date, category, wallet, payMethod);
                        break;
                }
                transactions.add(transaction);
            } catch (Exception e) {
                System.out.println("Lỗi đọc giao dịch JSON: " + e.getMessage());
            }
        }
        return transactions;
    }

    // ======================== WALLET ========================

    @Override
    public void saveWallets(List<Wallet> wallets, String path) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("[\n");
            for (int i = 0; i < wallets.size(); i++) {
                Wallet w = wallets.get(i);
                writer.write("  {\n");
                writeJsonField(writer, "type", w.getWalletType().name(), true);
                writeJsonField(writer, "name", w.getName(), true);

                if (w instanceof BankAccount) {
                    BankAccount ba = (BankAccount) w;
                    writeJsonField(writer, "balance", String.valueOf(w.getBalance()), false);
                    writeJsonField(writer, "bankName", ba.getBankName(), true);
                    writeJsonLastField(writer, "accountNumber", ba.getAccountNumber(), true);
                } else if (w instanceof EWallet) {
                    writeJsonField(writer, "balance", String.valueOf(w.getBalance()), false);
                    writeJsonLastField(writer, "provider", ((EWallet) w).getProvider(), true);
                } else {
                    writeJsonLastField(writer, "balance", String.valueOf(w.getBalance()), false);
                }

                writer.write("  }");
                if (i < wallets.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("]\n");
        }
    }

    @Override
    public List<Wallet> loadWallets(String path) throws Exception {
        List<Wallet> wallets = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return wallets;

        String content = readFileContent(file);
        List<Map<String, String>> objects = parseJsonArray(content);

        for (Map<String, String> obj : objects) {
            try {
                WalletType type = WalletType.valueOf(obj.getOrDefault("type", "CASH"));
                String name = obj.getOrDefault("name", "");
                double balance = Double.parseDouble(obj.getOrDefault("balance", "0"));

                Wallet wallet;
                switch (type) {
                    case BANK:
                        wallet = new BankAccount(name, balance,
                                obj.getOrDefault("bankName", ""),
                                obj.getOrDefault("accountNumber", ""));
                        break;
                    case EWALLET:
                        wallet = new EWallet(name, balance, obj.getOrDefault("provider", ""));
                        break;
                    default:
                        wallet = new CashWallet(name, balance);
                        break;
                }
                wallets.add(wallet);
            } catch (Exception e) {
                System.out.println("Lỗi đọc ví JSON: " + e.getMessage());
            }
        }
        return wallets;
    }


    @Override
    public void saveCategories(List<Category> categories, String path) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("[\n");
            for (int i = 0; i < categories.size(); i++) {
                Category c = categories.get(i);
                writer.write("  {\n");
                writeJsonField(writer, "name", c.getName(), true);
                writeJsonLastField(writer, "type", c.getType().name(), true);
                writer.write("  }");
                if (i < categories.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("]\n");
        }
    }

    @Override
    public List<Category> loadCategories(String path) throws Exception {
        List<Category> categories = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return categories;

        String content = readFileContent(file);
        List<Map<String, String>> objects = parseJsonArray(content);

        for (Map<String, String> obj : objects) {
            String name = obj.getOrDefault("name", "");
            TransactionType type = TransactionType.valueOf(obj.getOrDefault("type", "EXPENSE"));
            categories.add(new Category(name, type));
        }
        return categories;
    }

    @Override
    public void saveBudgets(Map<Category, Budget> budgets, String path) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("[\n");
            int i = 0;
            for (Budget b : budgets.values()) {
                writer.write("  {\n");
                writeJsonField(writer, "categoryName", b.getCategory().getName(), true);
                writeJsonField(writer, "limit", String.valueOf(b.getLimit()), false);
                writeJsonLastField(writer, "period", b.getPeriod().name(), true);
                writer.write("  }");
                if (i < budgets.size() - 1) writer.write(",");
                writer.write("\n");
                i++;
            }
            writer.write("]\n");
        }
    }

    @Override
    public Map<Category, Budget> loadBudgets(String path, List<Category> categories) throws Exception {
        Map<Category, Budget> budgets = new HashMap<>();
        File file = new File(path);
        if (!file.exists()) return budgets;

        String content = readFileContent(file);
        List<Map<String, String>> objects = parseJsonArray(content);

        for (Map<String, String> obj : objects) {
            String catName = obj.getOrDefault("categoryName", "");
            double limit = Double.parseDouble(obj.getOrDefault("limit", "0"));
            Period period = Period.valueOf(obj.getOrDefault("period", "MONTHLY"));

            Category category = findByName(categories, catName);
            if (category != null) {
                budgets.put(category, new Budget(category, limit, period));
            }
        }
        return budgets;
    }


    private void writeJsonField(BufferedWriter writer, String key, String value, boolean isString) throws IOException {
        if (isString) {
            writer.write("    \"" + key + "\": \"" + escapeJson(value) + "\",\n");
        } else {
            writer.write("    \"" + key + "\": " + value + ",\n");
        }
    }

    private void writeJsonLastField(BufferedWriter writer, String key, String value, boolean isString) throws IOException {
        if (isString) {
            writer.write("    \"" + key + "\": \"" + escapeJson(value) + "\"\n");
        } else {
            writer.write("    \"" + key + "\": " + value + "\n");
        }
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Parse đơn giản JSON array chứa các object phẳng.
     * Trả về danh sách Map<key, value> cho mỗi object.
     */
    private List<Map<String, String>> parseJsonArray(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        json = json.trim();

        // Tìm từng object {} trong array []
        int i = 0;
        while (i < json.length()) {
            int objStart = json.indexOf('{', i);
            if (objStart == -1) break;
            int objEnd = json.indexOf('}', objStart);
            if (objEnd == -1) break;

            String objContent = json.substring(objStart + 1, objEnd).trim();
            Map<String, String> map = parseJsonObject(objContent);
            result.add(map);
            i = objEnd + 1;
        }
        return result;
    }

    /**
     * Parse nội dung bên trong một JSON object (các cặp key:value phẳng).
     */
    private Map<String, String> parseJsonObject(String content) {
        Map<String, String> map = new HashMap<>();
        // Tách theo dấu xuống dòng hoặc dấu phẩy, parse từng cặp key:value
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.endsWith(",")) line = line.substring(0, line.length() - 1).trim();

            int colonIdx = line.indexOf(':');
            if (colonIdx == -1) continue;

            String key = line.substring(0, colonIdx).trim();
            String value = line.substring(colonIdx + 1).trim();

            // Xóa dấu ngoặc kép bọc key
            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }

            // Xóa dấu ngoặc kép bọc value (nếu là string)
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
                // Unescape
                value = value.replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t");
            }

            map.put(key, value);
        }
        return map;
    }

    private Category findByName(List<Category> categories, String name) {
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
