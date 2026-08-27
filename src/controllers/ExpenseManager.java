package controllers;

import enums.TransactionType;
import exception.InsufficientBalanceException;
import interfaces.Storage;
import models.*;

import java.time.LocalDate;
import java.util.*;

public class ExpenseManager {
    private List<Transaction> transactions;
    private List<Wallet> wallets;
    private List<Category> categories;
    private Map<Category, Budget> budgets;
    private Map<String, Budget> budgetsByStr;
    private Storage storage;

    private static final String DATA_DIR = "data/";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "transactions";
    private static final String WALLETS_FILE = DATA_DIR + "wallets";
    private static final String CATEGORIES_FILE = DATA_DIR + "categories";
    private static final String BUDGETS_FILE = DATA_DIR + "budgets";

    public ExpenseManager() {
        this.transactions = new ArrayList<>();
        this.wallets = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.budgets = new HashMap<>();
        this.budgetsByStr = new HashMap<>();
        categories.add(new Category("Chi tiêu", TransactionType.EXPENSE));
        categories.add(new Category("Thu", TransactionType.INCOME));
    }

    /**
     * Gán Storage implementation (CsvStorage hoặc JsonStorage).
     */
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    /**
     * Trả về phần mở rộng file dựa trên loại Storage đang dùng.
     */
    private String getFileExtension() {
        if (storage instanceof storage.JsonStorage) {
            return ".json";
        }
        return ".csv";
    }

    /**
     * Nạp toàn bộ dữ liệu từ file khi khởi động.
     */
    public void loadAllData() {
        if (storage == null) {
            System.out.println("Chưa thiết lập Storage. Dữ liệu sẽ không được lưu trữ.");
            return;
        }

        String ext = getFileExtension();
        try {
            // 1. Nạp categories trước (transaction phụ thuộc)
            List<Category> loadedCategories = storage.loadCategories(CATEGORIES_FILE + ext);
            if (!loadedCategories.isEmpty()) {
                categories.clear();
                categories.addAll(loadedCategories);
            }
            System.out.println("Đã nạp " + categories.size() + " danh mục.");

            // 2. Nạp wallets (transaction phụ thuộc)
            wallets = storage.loadWallets(WALLETS_FILE + ext);
            System.out.println("Đã nạp " + wallets.size() + " ví tiền.");

            // 3. Nạp budgets (phụ thuộc category)
            budgets = storage.loadBudgets(BUDGETS_FILE + ext, categories);
            // Đồng bộ budgetsByStr
            budgetsByStr.clear();
            for (Map.Entry<Category, Budget> entry : budgets.entrySet()) {
                budgetsByStr.put(entry.getKey().getName().toLowerCase(), entry.getValue());
            }
            System.out.println("Đã nạp " + budgets.size() + " hạn mức ngân sách.");

            // 4. Nạp transactions (phụ thuộc cả category và wallet)
            transactions = storage.loadTransactions(TRANSACTIONS_FILE + ext, categories, wallets);
            System.out.println("Đã nạp " + transactions.size() + " giao dịch.");

            // 5. Tự động xử lý các giao dịch định kỳ đã đến hạn
            processDueRecurringExpenses();

        } catch (Exception e) {
            System.out.println("Lỗi khi nạp dữ liệu: " + e.getMessage());
            System.out.println("Chương trình sẽ khởi động với dữ liệu rỗng.");
        }
    }

    /**
     * Lưu toàn bộ dữ liệu xuống file khi thoát.
     */
    public void saveAllData() {
        if (storage == null) {
            System.out.println("Chưa thiết lập Storage. Dữ liệu không được lưu.");
            return;
        }

        String ext = getFileExtension();

        java.io.File dataDir = new java.io.File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try {
            storage.saveCategories(categories, CATEGORIES_FILE + ext);
            storage.saveWallets(wallets, WALLETS_FILE + ext);
            storage.saveBudgets(budgets, BUDGETS_FILE + ext);
            storage.saveTransactions(transactions, TRANSACTIONS_FILE + ext);
            System.out.println("Đã lưu toàn bộ dữ liệu thành công!");
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu dữ liệu: " + e.getMessage());
        }
    }

    public void addWallet(Wallet newWallet) {
        // 1. Kiểm tra trùng tên ví (áp dụng cho tất cả các loại ví)
        for (Wallet w : wallets) {
            if (w.getName().equalsIgnoreCase(newWallet.getName())) {
                throw new IllegalArgumentException("Tên ví '" + newWallet.getName() + "' đã tồn tại!");
            }
        }

        if (newWallet instanceof BankAccount) {
            BankAccount newBank = (BankAccount) newWallet;
            for (Wallet w : wallets) {
                if (w instanceof BankAccount) {
                    BankAccount existingBank = (BankAccount) w;
                    if (existingBank.getAccountNumber().equals(newBank.getAccountNumber()) || existingBank.getName().equalsIgnoreCase(newBank.getName())) {
                        throw new IllegalArgumentException("Tài khoản số " + newBank.getAccountNumber() + " tại ngân hàng " + newBank.getBankName() + " đã tồn tại trên hệ thống!");
                    }
                }
            }
        }
        if (newWallet instanceof EWallet) {
            EWallet newEWallet = (EWallet) newWallet;
            for (Wallet w : wallets) {
                if (w instanceof EWallet) {
                    EWallet existingEWallet = (EWallet) w;
                    if (existingEWallet.getName().equals(newEWallet.getName())) {
                        throw new IllegalArgumentException("Ví điện tử " + newEWallet.getName() + " với NNC " + newEWallet.getProvider() + " đã tồn tại trên hệ thống!");
                    }
                }
            }
        }
        if (newWallet instanceof CashWallet) {
            CashWallet newCashWallet = (CashWallet) newWallet;
            for (Wallet w : wallets) {
                if (w instanceof CashWallet) {
                    Wallet existingCashWallet = (Wallet) w; // Fix lỗi ép kiểu sai từ EWallet sang Wallet/CashWallet ở code gốc của bạn
                    if (existingCashWallet.getName().equals(newCashWallet.getName())) {
                        throw new IllegalArgumentException("Ví tiền mặt " + newCashWallet.getName() + " đã tồn tại trên hệ thống!");
                    }
                }
            }
        }

        wallets.add(newWallet);
        System.out.println("Đã thêm ví: " + newWallet.getName());
    }

    public void addCategory(Category category) {
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(category.getName())) {
                throw new IllegalArgumentException("Danh mục '" + category.getName() + "' đã tồn tại!");
            }
        }
        categories.add(category);
    }

    public Wallet getWalletByName(String name) {
        for (Wallet w : wallets) {
            if (w.getName().equalsIgnoreCase(name)) {
                return  w;
            }
        }
        return null;
    }

    public Category getCategoryByName(String name) {
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public void displayWalletBalances() {
        if (wallets.isEmpty()) {
            System.out.println("Chưa có ví nào trong hệ thống");
            return;
        }

        System.out.println("\n SỐ DƯ CHI TIẾT TỪNG VÍ");
        for (Wallet w : wallets) {
            System.out.printf("- Ví %s (%s): %,.2f VND\n", w.getName(), w.getWalletType(), w.getBalance());
        }
    }

    public void addTransaction(Transaction t) throws InsufficientBalanceException {
        Wallet wallet = t.getWallet();

        if (wallet == null) {
            throw new IllegalArgumentException("Chưa chọn ví!");
        }
        if (t.getType() == TransactionType.INCOME) {
            wallet.deposit(t.getAmount());
        } else {
            wallet.withdraw(t.getAmount());
        }

        transactions.add(t);

        if (t.getType() == TransactionType.EXPENSE) {
            checkBudgetWarning(t.getCategory());
        }
    }

    public void removeTransaction(String id) {
        Transaction target = null;
        for (Transaction t : transactions) {
            if (t.getId().equals(id)) {
                target = t;
                break;
            }
        }

        if (target != null) {
            Wallet wallet = target.getWallet();
            if (target.getSignedAmount() > 0) {
                try {
                    wallet.withdraw(target.getAmount());
                } catch (InsufficientBalanceException e) {
                    System.out.println("LỖI: Không thể xóa giao dịch " + id
                            + " vì ví '" + wallet.getName() + "' không đủ số dư để hoàn lại ("
                            + String.format("%,.0f VND", target.getAmount()) + ").");
                    return;
                }
            } else {
                wallet.deposit(target.getAmount());
            }

            transactions.remove(target);
            System.out.println("Đã xóa giao dịch " + id + " và cập nhật lại số dư!");
        } else {
            System.out.println("Không tìm thấy giao dịch mã " + id);
        }
    }

    public void removeWallet(Wallet wallet) throws InsufficientBalanceException {
        for (Transaction t : transactions) {
            if (t.getWallet() == wallet) {
                throw new InsufficientBalanceException(
                        "Ví '" + wallet.getName() + "' đã có giao dịch liên quan, không thể xóa!");
            }
        }
        wallets.remove(wallet);
    }

    public void removeCategory(Category category) {
        transactions.removeIf(t -> t.getCategory().equals(category));
        categories.remove(category);
        budgets.remove(category);
        budgetsByStr.remove(category.getName().toLowerCase());
    }

    public void displayAllTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("Chưa có giao dịch nào.");
            return;
        } else {
            System.out.println("LỊCH SỬ GIAO DỊCH");
            for (Transaction t : transactions) {
                t.printInfo();
            }
        }
    }

    public void addBudget(Budget budget) {
        budgets.put(budget.getCategory(), budget);
        budgetsByStr.put(budget.getCategory().getName().toLowerCase(), budget);
        System.out.printf("Đã đặt hạn mức %,.0f VND cho danh mục '%s' (chu kỳ: %s)\n",
                budget.getLimit(), budget.getCategory().getName(), budget.getPeriod());
    }

    public void removeBudget(String categoryName) {
        Budget removed = budgetsByStr.remove(categoryName.toLowerCase());
        if (removed != null) {
            budgets.remove(removed.getCategory());
            System.out.println("Đã xóa hạn mức của danh mục: " + categoryName);
        } else {
            System.out.println("Không tìm thấy hạn mức của danh mục: " + categoryName);
        }
    }

    public void displayAllBudgets() {
        if (budgetsByStr.isEmpty()) {
            System.out.println("Chưa có hạn mức ngân sách nào.");
            return;
        }

        System.out.println("\n===== HẠN MỨC NGÂN SÁCH =====");
        for (Budget budget : budgetsByStr.values()) {
            double spent = calculateMonthlySpentByCategory(budget.getCategory());
            double remaining = budget.getRemaining(spent);
            String status = budget.isExceeded(spent) ? "[VƯỢT HẠN MỨC!]" : "[Trong hạn mức]";

            System.out.printf("Danh mục: %-15s | Hạn mức: %,15.0f VND | Đã chi: %,15.0f VND | Còn lại: %,15.0f VND | %s\n",
                    budget.getCategory().getName(), budget.getLimit(), spent, remaining, status);
        }
    }

    public void checkBudgetWarning(Category category) {
        Budget budget = budgetsByStr.get(category.getName().toLowerCase());
        if (budget != null) {
            double spent = calculateMonthlySpentByCategory(category);
            if (budget.isExceeded(spent)) {
                System.out.printf("!! CẢNH BÁO: Chi tiêu danh mục '%s' đã VƯỢT hạn mức! " +
                                "(Đã chi: %,.0f / Hạn mức: %,.0f VND)\n",
                        category.getName(), spent, budget.getLimit());
            } else {
                double remaining = budget.getRemaining(spent);
                if (remaining < budget.getLimit() * 0.2) {
                    System.out.printf("!! LƯU Ý: Danh mục '%s' sắp đặt hạn mức! " +
                                    "(Còn lại: %,.0f / Hạn mức: %,.0f VND)\n",
                            category.getName(), remaining, budget.getLimit());
                }
            }
        }
    }

    public double calculateTotalBalance() {
        double total = 0;
        for (Wallet w : wallets) {
            total += w.getBalance();
        }
        return total;
    }

    public void monthlySummary(int month, int year) {
        double totalExpense = 0;
        double totalIncome = 0;

        for (Transaction t : transactions) {
            LocalDate date = t.getDate();
            if (date.getMonthValue() == month && date.getYear() == year) {
                if (t.getSignedAmount() > 0) {
                    totalIncome += t.getAmount();
                } else {
                    totalExpense += t.getAmount();
                }
            }
        }
        System.out.printf("\n THỐNG KÊ THÁNG %d/%d\n", month, year);
        System.out.printf("Tổng thu: %,.2f VND\n", totalIncome);
        System.out.printf("Tổng chi: %,.2f VND\n", totalExpense);
        System.out.printf("Thực nhận: %,.2f VND\n", (totalIncome - totalExpense));
    }

    public void yearlySummary(int year) {
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction t : transactions) {
            LocalDate date = t.getDate();
            if (date.getYear() == year) {
                if (t.getSignedAmount() > 0) {
                    totalIncome += t.getAmount();
                } else {
                    totalExpense += t.getAmount();
                }
            }
        }

        System.out.printf("\n THỐNG KÊ NĂM %d\n", year);
        System.out.printf("Tổng thu: %,.2f VND\n", totalIncome);
        System.out.printf("Tổng chi: %,.2f VND\n", totalExpense);
        System.out.printf("Thực nhận: %,.2f VND\n", (totalIncome - totalExpense));
    }

    public void advancedStatistics(int month, int year) {
        List<Expense> monthlyExpenses = new ArrayList<>();
        Map<String, Double> categorySum = new HashMap<>();

        for (Transaction t : transactions) {
            if (t instanceof Expense && t.getDate().getMonthValue() == month && t.getDate().getYear() == year) {
                Expense exp = (Expense) t;
                monthlyExpenses.add(exp);

                String catName = exp.getCategory().getName();
                double amount = Math.abs(exp.getSignedAmount());
                categorySum.put(catName, categorySum.getOrDefault(catName, 0.0) + amount);
            }
        }

        if (monthlyExpenses.isEmpty()) {
            System.out.println("Không có khoản chi tiêu nào trong tháng " + month + "/" + year);
            return;
        }

        Expense maxExp = monthlyExpenses.get(0);
        Expense minExp = monthlyExpenses.get(0);
        for (Expense e : monthlyExpenses) {
            if (Math.abs(e.getSignedAmount()) > Math.abs(maxExp.getSignedAmount())) maxExp = e;
            if (Math.abs(e.getSignedAmount()) < Math.abs(minExp.getSignedAmount())) minExp = e;
        }

        System.out.printf("\nBÁO CÁO CHI TIẾT THÁNG %d/%d\n", month, year);
        System.out.println("Khoản chi LỚN NHẤT:");
        maxExp.printInfo();
        System.out.println("Khoản chi NHỎ NHẤT:");
        minExp.printInfo();

        List<Map.Entry<String, Double>> sortedCats = new ArrayList<>(categorySum.entrySet());
        sortedCats.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        System.out.println("\nDANH MỤC TỐN KÉM NHẤT:");
        for (Map.Entry<String, Double> entry : sortedCats) {
            System.out.printf("- %s: %,.0f VND\n", entry.getKey(), entry.getValue());
        }
    }

    public void setBudget(Category category, double limit, enums.Period period) {
        Budget budget = new Budget(category, limit, period);
        budgets.put(category, budget);
        budgetsByStr.put(category.getName().toLowerCase(), budget);
        System.out.printf("Đã đặt ngân sách tối đa cho '%s' là %,.0f VND (%s).%n", category.getName(), limit, period);
    }

    public void searchById(String keyword) {
        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getId().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(t);
            }
        }
        displaySearchResults(results);
    }

    public void searchByCategory(String keyword) {
        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getCategory().getName().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(t);
            }
        }
        displaySearchResults(results);
    }

    public void searchByDate(LocalDate date) {
        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getDate().isEqual(date)) results.add(t);
        }
        displaySearchResults(results);
    }

    public void searchByMonthYear(int month, int year) {
        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if ((month == 0 || t.getDate().getMonthValue() == month) && t.getDate().getYear() == year) {
                results.add(t);
            }
        }
        displaySearchResults(results);
    }

    public void searchByAmountRange(double min, double max) {
        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            double actualAmount = Math.abs(t.getSignedAmount());
            if (actualAmount >= min && actualAmount <= max) {
                results.add(t);
            }
        }
        displaySearchResults(results);
    }

    private void displaySearchResults(List<Transaction> results) {
        if (results.isEmpty()) {
            System.out.println("Không tìm thấy giao dịch nào phù hợp với tiêu chí!");
        } else {
            System.out.println("\n KẾT QUẢ TÌM KIẾM (" + results.size() + " giao dịch)");
            for (Transaction t : results) {
                t.printInfo();
            }
        }
    }

    /**
     * CÁC HÀM BỔ SUNG MỚI.
     */

    private double calculateMonthlySpentByCategory(Category category) {
        double total = 0;
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE
                    && t.getCategory().getName().equalsIgnoreCase(category.getName())
                    && t.getDate().getMonthValue() == currentMonth
                    && t.getDate().getYear() == currentYear) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public Map<String, Double> statisticsByCategory() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                String catName = t.getCategory().getName();
                result.put(catName, result.getOrDefault(catName, 0.0) + t.getAmount());
            }
        }
        return result;
    }

    /**
     * Thống kê chi tiêu theo từng danh mục trong một tháng.
     */
    public Map<Category, Double> statisticsByCategory(int month, int year) {
        Map<Category, Double> result = new HashMap<>();

        for (Transaction t : transactions) {
            if (t instanceof Expense
                    && t.getDate().getMonthValue() == month
                    && t.getDate().getYear() == year) {
                Category cat = t.getCategory();
                double amount = Math.abs(t.getSignedAmount());
                result.put(cat, result.getOrDefault(cat, 0.0) + amount);
            }
        }
        return result;
    }

    /**
     * Thống kê chi tiêu theo từng tháng trong một năm.
     */
    public Map<Integer, Double> expenseByMonth(int year) {
        Map<Integer, Double> result = new HashMap<>();
        for (int m = 1; m <= 12; m++) {
            result.put(m, 0.0);
        }

        for (Transaction t : transactions) {
            if (t instanceof Expense && t.getDate().getYear() == year) {
                int m = t.getDate().getMonthValue();
                result.put(m, result.get(m) + Math.abs(t.getSignedAmount()));
            }
        }
        return result;
    }

    public void displayStatisticsByCategory() {
        Map<String, Double> stats = statisticsByCategory();
        if (stats.isEmpty()) {
            System.out.println("Chưa có giao dịch chi tiêu nào.");
            return;
        }

        System.out.println("\n===== CHI TIÊU THEO DANH MỤC =====");
        double total = 0;
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            System.out.printf("  %-20s: %,15.0f VND\n", entry.getKey(), entry.getValue());
            total += entry.getValue();
        }
        System.out.printf("  %-20s: %,15.0f VND\n", "TỔNG CHI TIÊU", total);
    }

    /**
     * Hiển thị chi tiêu chi tiết theo từng danh mục trong tháng (version nâng cao).
     */
    public void displayStatisticsByCategory(int month, int year) {
        Map<Category, Double> stats = statisticsByCategory(month, year);

        if (stats.isEmpty()) {
            System.out.println("Không có chi tiêu trong tháng " + month + "/" + year);
            return;
        }

        System.out.printf("%n CHI TIÊU THEO DANH MỤC — THÁNG %d/%d%n", month, year);

        double total = 0;
        List<Map.Entry<Category, Double>> sorted = new ArrayList<>(stats.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<Category, Double> entry : sorted) {
            total += entry.getValue();
        }

        int rank = 1;
        for (Map.Entry<Category, Double> entry : sorted) {
            double percent = (total > 0) ? (entry.getValue() / total * 100) : 0;
            System.out.printf("%d. %-20s %,12.0f VND  (%5.1f%%)%n",
                    rank++, entry.getKey().getName(), entry.getValue(), percent);
        }
        System.out.printf("TỔNG CHI:               %,12.0f VND%n", total);
    }

    /**
     * Hiển thị chi tiêu so sánh theo từng tháng trong năm.
     */
    public void displayExpenseByMonth(int year) {
        Map<Integer, Double> monthlyData = expenseByMonth(year);

        System.out.printf("%n CHI TIÊU THEO THÁNG NĂM %d%n", year);

        double totalYear = 0;
        for (int m = 1; m <= 12; m++) {
            double amount = monthlyData.get(m);
            totalYear += amount;
            if (amount > 0) {
                int barLength = (int) (amount / 1000000);
                String bar = "█".repeat(Math.max(1, Math.min(barLength, 30)));
                System.out.printf("Tháng %2d: %,15.0f VND  %s%n", m, amount, bar);
            } else {
                System.out.printf("Tháng %2d: %,15.0f VND%n", m, amount);
            }
        }
        System.out.printf("TỔNG CẢ NĂM: %,.0f VND%n", totalYear);
    }

    public void displayMonthlyExpenseBreakdown(int year) {
        Map<Integer, Double> monthlyExpense = new TreeMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE && t.getDate().getYear() == year) {
                int month = t.getDate().getMonthValue();
                monthlyExpense.put(month, monthlyExpense.getOrDefault(month, 0.0) + t.getAmount());
            }
        }

        if (monthlyExpense.isEmpty()) {
            System.out.println("Không có chi tiêu nào trong năm: " + year);
            return;
        }

        System.out.printf("\n===== CHI TIÊU THEO THÁNG - NĂM %d =====\n", year);
        double total = 0;
        for (Map.Entry<Integer, Double> entry : monthlyExpense.entrySet()) {
            System.out.printf("  Tháng %2d: %,15.0f VND\n", entry.getKey(), entry.getValue());
            total += entry.getValue();
        }
        System.out.printf("  %-10s: %,15.0f VND\n", "TỔNG", total);
    }

    /**
     * Thống kê khoản chi tiêu lớn nhất và nhỏ nhất
     */
    public void displayMinMaxExpense() {
        // Lọc ra các giao dịch thuộc loại CHI TIÊU (EXPENSE)
        java.util.List<Transaction> expenses = transactions.stream()
                .filter(t -> t.getCategory() != null && t.getCategory().getType() == enums.TransactionType.EXPENSE)
                .toList();

        if (expenses.isEmpty()) {
            System.out.println("Chưa có dữ liệu chi tiêu nào để thống kê Max/Min.");
            return;
        }

        // Tìm giao dịch có số tiền lớn nhất
        Transaction maxExpense = expenses.stream()
                .max(java.util.Comparator.comparingDouble(Transaction::getAmount))
                .orElse(null);

        // Tìm giao dịch có số tiền nhỏ nhất
        Transaction minExpense = expenses.stream()
                .min(java.util.Comparator.comparingDouble(Transaction::getAmount))
                .orElse(null);

        System.out.println("\n--- KHOẢN CHI LỚN NHẤT & NHỎ NHẤT ---");
        if (maxExpense != null) {
            System.out.printf("  [+] LỚN NHẤT: Mã: %s | Danh mục: %s | Số tiền: %,.0f VND | Ghi chú: %s\n",
                    maxExpense.getId(), maxExpense.getCategory().getName(), maxExpense.getAmount(), maxExpense.getNote());
        }
        if (minExpense != null) {
            System.out.printf("  [-] NHỎ NHẤT: Mã: %s | Danh mục: %s | Số tiền: %,.0f VND | Ghi chú: %s\n",
                    minExpense.getId(), minExpense.getCategory().getName(), minExpense.getAmount(), minExpense.getNote());
        }
    }

    /**
     * Thống kê Top N danh mục tốn tiền nhất
     */
    public void displayTopExpensiveCategories(int n) {
        // Nhóm và cộng tổng số tiền chi tiêu theo từng tên danh mục
        java.util.Map<String, Double> categoryTotals = transactions.stream()
                .filter(t -> t.getCategory() != null && t.getCategory().getType() == enums.TransactionType.EXPENSE)
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        java.util.stream.Collectors.summingDouble(Transaction::getAmount)
                ));

        if (categoryTotals.isEmpty()) {
            System.out.println("Chưa có dữ liệu chi tiêu nào để xếp hạng danh mục.");
            return;
        }

        System.out.printf("\n--- TOP %d DANH MỤC TIÊU TỐN KÉM NHẤT ---\n", n);

        // Sắp xếp giảm dần theo số tiền tổng và lấy ra N danh mục đầu tiên
        categoryTotals.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(n)
                .forEach(entry -> System.out.printf("  - %s: %,.0f VND\n", entry.getKey(), entry.getValue()));
    }

    public List<RecurringExpense> getRecurringExpenses() {
        List<RecurringExpense> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof RecurringExpense) {
                result.add((RecurringExpense) t);
            }
        }
        return result;
    }

    public List<RecurringExpense> checkDueRecurring() {
        List<RecurringExpense> dueList = new ArrayList<>();
        for (RecurringExpense re : getRecurringExpenses()) {
            if (re.isDue()) {
                dueList.add(re);
            }
        }
        return dueList;
    }

    /**
     * Lấy danh sách các giao dịch định kỳ đã đến hạn.
     */
    public List<RecurringExpense> getDueRecurringExpenses() {
        return checkDueRecurring();
    }

    public Expense createFromRecurring(RecurringExpense recurring) {
        String newId = recurring.getId() + "_" + LocalDate.now().toString();
        return new Expense(newId, recurring.getAmount(), recurring.getNote(),
                LocalDate.now(), recurring.getCategory(), recurring.getWallet(),
                recurring.getPaymentMethod());
    }

    /**
     * Tạo một giao dịch mới từ template giao dịch định kỳ.
     */
    public Expense generateRecurringTransaction(RecurringExpense recurring) {
        LocalDate dueDate = recurring.nextDueDate();

        // Tạo ID mới cho giao dịch
        String newId = Transaction.generateId(recurring.getCategory(), dueDate);

        // Tạo Expense thông thường từ recurring template
        Expense newExpense = new Expense(
                newId,
                recurring.getAmount(),
                "Tự động từ giao dịch định kỳ: " + recurring.getId(),
                dueDate,
                recurring.getCategory(),
                recurring.getWallet(),
                recurring.getPaymentMethod()
        );

        // Thử thêm giao dịch (bao gồm kiểm tra số dư)
        try {
            addTransaction(newExpense);
        } catch (exception.InsufficientBalanceException e) {
            System.out.println("Lỗi tạo giao dịch định kỳ: " + e.getMessage());
            return null;
        }

        // Cập nhật lastProcessedDate
        recurring.setLastProcessedDate(dueDate);

        return newExpense;
    }

    public void importTransactions(List<Transaction> loadedTransactions) {
        for (Transaction t : loadedTransactions) {
            Category existingCat = getCategoryByName(t.getCategory().getName());
            if (existingCat == null) {
                addCategory(t.getCategory());
            }

            Wallet existingWallet = getWalletByName(t.getWallet().getName());
            if (existingWallet == null) {
                wallets.add(t.getWallet());
            }
            transactions.add(t);
        }
    }

    /**
     * Tự động xử lý các giao dịch định kỳ đã đến hạn.
     */
    public void processDueRecurringExpenses() {
        List<RecurringExpense> dueList = getDueRecurringExpenses();
        if (dueList.isEmpty()) return;

        int count = 0;
        for (RecurringExpense re : dueList) {
            while (re.isDue()) {
                Expense newExp = generateRecurringTransaction(re);
                if (newExp != null) {
                    count++;
                } else {
                    break;
                }
            }
        }

        if (count > 0) {
            System.out.println("Đã tự động tạo " + count + " giao dịch từ các khoản định kỳ đến hạn.");
            saveAllData(); // Lưu lại ngay để tránh tạo lặp nếu đóng app
        }
    }

    // ========== GETTERS ==========
    public List<Transaction> getTransactions() { return transactions; }
    public List<Wallet> getWallets() { return wallets; }
    public List<Category> getCategories() { return categories; }
    public Map<Category, Budget> getBudgets() { return budgets; }
}