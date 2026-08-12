package controllers;

import enums.TransactionType;
import exception.InsufficientBalanceException;
import interfaces.Storage;
import models.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Lớp điều phối trung tâm: quản lý danh sách Transaction, Wallet, Category, Budget.
 * Tham chiếu tới Storage (interface) để lưu/nạp dữ liệu — không phụ thuộc cài đặt cụ thể.
 */
public class ExpenseManager {
    private List<Transaction> transactions;
    private List<Wallet> wallets;
    private List<Category> categories;
    private Map<Category, Budget> budgets;
    private Storage storage;

    // Đường dẫn mặc định cho các file dữ liệu
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
    }

    /**
     * Gán Storage implementation (CsvStorage hoặc JsonStorage).
     * Thể hiện tính đa hình: ExpenseManager dùng interface, không biết cài đặt cụ thể.
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
        return ".csv"; // Mặc định CSV
    }

    // ======================== LOAD / SAVE ========================

    /**
     * Nạp toàn bộ dữ liệu từ file khi khởi động.
     * Thứ tự: categories → wallets → budgets → transactions
     * (vì transaction phụ thuộc vào category và wallet)
     */
    public void loadAllData() {
        if (storage == null) {
            System.out.println("⚠ Chưa thiết lập Storage. Dữ liệu sẽ không được lưu trữ.");
            return;
        }

        String ext = getFileExtension();
        try {
            // 1. Nạp categories trước (transaction phụ thuộc)
            categories = storage.loadCategories(CATEGORIES_FILE + ext);
            System.out.println("✓ Đã nạp " + categories.size() + " danh mục.");

            // 2. Nạp wallets (transaction phụ thuộc)
            wallets = storage.loadWallets(WALLETS_FILE + ext);
            System.out.println("✓ Đã nạp " + wallets.size() + " ví tiền.");

            // 3. Nạp budgets (phụ thuộc category)
            budgets = storage.loadBudgets(BUDGETS_FILE + ext, categories);
            System.out.println("✓ Đã nạp " + budgets.size() + " hạn mức ngân sách.");

            // 4. Nạp transactions (phụ thuộc cả category và wallet)
            transactions = storage.loadTransactions(TRANSACTIONS_FILE + ext, categories, wallets);
            System.out.println("✓ Đã nạp " + transactions.size() + " giao dịch.");

        } catch (Exception e) {
            System.out.println("⚠ Lỗi khi nạp dữ liệu: " + e.getMessage());
            System.out.println("Chương trình sẽ khởi động với dữ liệu rỗng.");
        }
    }

    /**
     * Lưu toàn bộ dữ liệu xuống file khi thoát.
     */
    public void saveAllData() {
        if (storage == null) {
            System.out.println("⚠ Chưa thiết lập Storage. Dữ liệu không được lưu.");
            return;
        }

        String ext = getFileExtension();

        // Tạo thư mục data nếu chưa tồn tại
        java.io.File dataDir = new java.io.File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try {
            storage.saveCategories(categories, CATEGORIES_FILE + ext);
            storage.saveWallets(wallets, WALLETS_FILE + ext);
            storage.saveBudgets(budgets, BUDGETS_FILE + ext);
            storage.saveTransactions(transactions, TRANSACTIONS_FILE + ext);
            System.out.println("✓ Đã lưu toàn bộ dữ liệu thành công!");
        } catch (Exception e) {
            System.out.println("✗ Lỗi khi lưu dữ liệu: " + e.getMessage());
        }
    }

    // ======================== SUPPORT FUNCTIONS ========================

    public void addWallet(Wallet wallet) {
        wallets.add(wallet);
        System.out.println("Đã thêm ví: " + wallet.getName());
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public Wallet getWalletByName(String name) {
        for (Wallet w : wallets) {
            if (w.getName().equalsIgnoreCase(name)) {
                return w;
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
            System.out.println("Chưa có ví nào trong hệ thống.");
            return;
        }

        System.out.println("\n SỐ DƯ CHI TIẾT TỪNG VÍ");
        for (Wallet w : wallets) {
            System.out.printf("- Ví %s (%s): %,.2f VND%n", w.getName(), w.getWalletType(), w.getBalance());
        }
    }

    // Getter cho danh sách (hỗ trợ lưu file và hiển thị)
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Map<Category, Budget> getBudgets() {
        return budgets;
    }

    // ======================== CRUD FUNCTIONS ========================

    public void addTransaction(Transaction t) {
        transactions.add(t);
        Wallet wallet = t.getWallet();

        if (t.getSignedAmount() > 0) {
            wallet.deposit(t.getAmount());
        } else {
            try {
                wallet.withdraw(t.getAmount());
            } catch (InsufficientBalanceException e) {
                System.out.println("Lỗi giao dịch: " + e.getMessage());
                transactions.remove(t); // Rollback nếu không đủ tiền
                return;
            }
            checkBudgetWarning(t);
        }
        System.out.println("Đã ghi nhận giao dịch và cập nhật số dư!");
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
                    System.out.println("Cảnh báo: Không thể hoàn trả số dư khi xóa: " + e.getMessage());
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

    public void displayAllTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("Chưa có giao dịch nào.");
            return;
        }
        System.out.println("LỊCH SỬ GIAO DỊCH");
        for (Transaction t : transactions) {
            t.printInfo();
            System.out.println("---");
        }
    }

    // ======================== STATISTICS ========================

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
        System.out.printf("%n THỐNG KÊ THÁNG %d/%d%n", month, year);
        System.out.printf("Tổng thu: %,.2f VND%n", totalIncome);
        System.out.printf("Tổng chi: %,.2f VND%n", totalExpense);
        System.out.printf("Thực nhận: %,.2f VND%n", (totalIncome - totalExpense));
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

        System.out.printf("%n THỐNG KÊ NĂM %d%n", year);
        System.out.printf("Tổng thu: %,.2f VND%n", totalIncome);
        System.out.printf("Tổng chi: %,.2f VND%n", totalExpense);
        System.out.printf("Thực nhận: %,.2f VND%n", (totalIncome - totalExpense));
    }

    // ======================== ADVANCED STATISTICS ========================

    /**
     * Thống kê chi tiêu theo từng danh mục trong một tháng.
     * Trả về Map<Category, Double> chứa tổng chi tiêu từng danh mục.
     *
     * @param month tháng cần thống kê (1-12)
     * @param year  năm
     * @return Map mapping từ Category → tổng chi tiêu (dương)
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
     * Trả về Map<Integer (tháng), Double (tổng chi)>.
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

        // Tìm khoản chi lớn nhất / nhỏ nhất
        Expense maxExp = monthlyExpenses.get(0);
        Expense minExp = monthlyExpenses.get(0);
        for (Expense e : monthlyExpenses) {
            if (Math.abs(e.getSignedAmount()) > Math.abs(maxExp.getSignedAmount())) maxExp = e;
            if (Math.abs(e.getSignedAmount()) < Math.abs(minExp.getSignedAmount())) minExp = e;
        }

        System.out.printf("%nBÁO CÁO CHI TIẾT THÁNG %d/%d%n", month, year);
        System.out.println("═══════════════════════════════════════");
        System.out.println("Khoản chi LỚN NHẤT:");
        maxExp.printInfo();
        System.out.println("\nKhoản chi NHỎ NHẤT:");
        minExp.printInfo();

        // Top danh mục tốn kém
        List<Map.Entry<String, Double>> sortedCats = new ArrayList<>(categorySum.entrySet());
        sortedCats.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        System.out.println("\n TOP DANH MỤC TỐN KÉM NHẤT:");
        System.out.println("───────────────────────────────────────");
        int rank = 1;
        for (Map.Entry<String, Double> entry : sortedCats) {
            System.out.printf("%d. %s: %,.0f VND%n", rank++, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Hiển thị chi tiêu so sánh theo từng tháng trong năm.
     */
    public void displayExpenseByMonth(int year) {
        Map<Integer, Double> monthlyData = expenseByMonth(year);

        System.out.printf("%n CHI TIÊU THEO THÁNG NĂM %d%n", year);
        System.out.println("═══════════════════════════════════════");

        double totalYear = 0;
        for (int m = 1; m <= 12; m++) {
            double amount = monthlyData.get(m);
            totalYear += amount;
            if (amount > 0) {
                // Thanh biểu đồ đơn giản
                int barLength = (int) (amount / 1000000); // Mỗi █ = 1 triệu
                String bar = "█".repeat(Math.max(1, Math.min(barLength, 30)));
                System.out.printf("Tháng %2d: %,15.0f VND  %s%n", m, amount, bar);
            } else {
                System.out.printf("Tháng %2d: %,15.0f VND%n", m, amount);
            }
        }
        System.out.println("───────────────────────────────────────");
        System.out.printf("TỔNG CẢ NĂM: %,.0f VND%n", totalYear);
    }

    /**
     * Hiển thị chi tiêu chi tiết theo từng danh mục trong tháng.
     */
    public void displayStatisticsByCategory(int month, int year) {
        Map<Category, Double> stats = statisticsByCategory(month, year);

        if (stats.isEmpty()) {
            System.out.println("Không có chi tiêu trong tháng " + month + "/" + year);
            return;
        }

        System.out.printf("%n CHI TIÊU THEO DANH MỤC — THÁNG %d/%d%n", month, year);
        System.out.println("═══════════════════════════════════════");

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
        System.out.println("───────────────────────────────────────");
        System.out.printf("TỔNG CHI:               %,12.0f VND%n", total);
    }

    // ======================== BUDGET LIMIT ========================

    public void setBudget(Category category, double limit, enums.Period period) {
        budgets.put(category, new Budget(category, limit, period));
        System.out.printf("Đã đặt ngân sách tối đa cho '%s' là %,.0f VND (%s).%n", category.getName(), limit, period);
    }

    private void checkBudgetWarning(Transaction currentTx) {
        Category category = currentTx.getCategory();
        if (!budgets.containsKey(category)) return;

        Budget budget = budgets.get(category);
        double spent = 0;
        LocalDate targetDate = currentTx.getDate();

        for (Transaction t : transactions) {
            if (t.getCategory().equals(category) && t.getSignedAmount() < 0) {
                LocalDate txDate = t.getDate();
                boolean inSamePeriod = false;

                switch (budget.getPeriod()) {
                    case DAILY:
                        inSamePeriod = txDate.isEqual(targetDate);
                        break;
                    case WEEKLY:
                        int txWeek = txDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                        int targetWeek = targetDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                        int txYear = txDate.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR);
                        int targetYear = targetDate.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR);
                        inSamePeriod = (txWeek == targetWeek && txYear == targetYear);
                        break;
                    case MONTHLY:
                        inSamePeriod = (txDate.getMonthValue() == targetDate.getMonthValue()
                                && txDate.getYear() == targetDate.getYear());
                        break;
                    case YEARLY:
                        inSamePeriod = (txDate.getYear() == targetDate.getYear());
                        break;
                }

                if (inSamePeriod) {
                    spent += Math.abs(t.getSignedAmount());
                }
            }
        }

        if (budget.isExceeded(spent)) {
            System.out.println("\n⚠ CẢNH BÁO NGÂN SÁCH ⚠");
            System.out.printf("Bạn đã chi tiêu VƯỢT HẠN MỨC danh mục '%s'!%n", category.getName());

            String periodStr;
            switch (budget.getPeriod()) {
                case DAILY: periodStr = "Ngày"; break;
                case WEEKLY: periodStr = "Tuần"; break;
                case MONTHLY: periodStr = "Tháng"; break;
                case YEARLY: periodStr = "Năm"; break;
                default: periodStr = budget.getPeriod().name();
            }

            System.out.printf("Ngân sách (%s): %,.0f | Đã tiêu: %,.0f (Vượt quá %,.0f VND)%n",
                    periodStr, budget.getLimit(), spent, (spent - budget.getLimit()));
        }
    }

    // ======================== RECURRING TRANSACTIONS ========================

    /**
     * Lấy danh sách tất cả giao dịch định kỳ.
     */
    public List<RecurringExpense> getRecurringExpenses() {
        List<RecurringExpense> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof RecurringExpense) {
                result.add((RecurringExpense) t);
            }
        }
        return result;
    }

    /**
     * Lấy danh sách các giao dịch định kỳ đã đến hạn (Tùy chọn B: chỉ nhắc nhở).
     */
    public List<RecurringExpense> getDueRecurringExpenses() {
        List<RecurringExpense> dueList = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof RecurringExpense) {
                RecurringExpense re = (RecurringExpense) t;
                if (re.isDue()) {
                    dueList.add(re);
                }
            }
        }
        return dueList;
    }

    /**
     * Tạo một giao dịch mới từ template giao dịch định kỳ.
     * Cập nhật lastProcessedDate và tạo Expense mới với ngày = nextDueDate.
     *
     * @param recurring giao dịch định kỳ gốc
     * @return giao dịch mới đã tạo, hoặc null nếu thất bại
     */
    public Expense generateRecurringTransaction(RecurringExpense recurring) {
        LocalDate dueDate = recurring.nextDueDate();

        // Tạo ID mới cho giao dịch
        String newId = String.format("CHI-AUTO-%s-%s-%d",
                dueDate.format(java.time.format.DateTimeFormatter.ofPattern("ddMM")),
                recurring.getCategory().getName().replaceAll("\\s+", "").toUpperCase().substring(0,
                        Math.min(3, recurring.getCategory().getName().length())),
                (int) (Math.random() * 9000) + 1000);

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
        addTransaction(newExpense);

        // Cập nhật lastProcessedDate
        recurring.setLastProcessedDate(dueDate);

        return newExpense;
    }

    // ======================== SEARCHING FUNCTIONS ========================

    private void displaySearchResults(List<Transaction> results) {
        if (results.isEmpty()) {
            System.out.println("Không tìm thấy giao dịch nào phù hợp với tiêu chí!");
        } else {
            System.out.println("\n KẾT QUẢ TÌM KIẾM (" + results.size() + " giao dịch)");
            for (Transaction t : results) {
                t.printInfo();
                System.out.println("---");
            }
        }
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
}
