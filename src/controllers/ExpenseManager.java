package controllers;

import enums.TransactionType;
import exception.InsufficientBalanceException;
import models.*;

import java.time.LocalDate;
import java.util.*;

public class ExpenseManager {
    private List<Transaction> transactions;
    private List<Wallet> wallets;
    private List<Category> categories;
    private Map<Category, Budget> budgets;
    private Map<String, Budget> budgetsByStr;

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
     * ==========================================
     * ĐOẠN CODE GỐC CỦA BẠN (ĐƯỢC GIỮ NGUYÊN BẢN)
     * ==========================================
     */

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
        categories.add(category);
    }

    public Wallet getWalletByName(String name) {
        for (Wallet w : wallets) {
            if (w.getName().equalsIgnoreCase(name)) {
                return  w;
            }
        }
        return  null;
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
                    // Bắt ngoại lệ số dư
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
        try {
            wallets.remove(wallet);
        } catch (Exception e) {
            throw new InsufficientBalanceException("Ví đã thực hiện giao dịch không thể xoá!");
        }
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
        System.out.printf("Da dat han muc %,.0f VND cho danh muc '%s' (chu ky: %s)\n",
                budget.getLimit(), budget.getCategory().getName(), budget.getPeriod());
    }

    public void removeBudget(String categoryName) {
        Budget removed = budgetsByStr.remove(categoryName.toLowerCase());
        if (removed != null) {
            budgets.remove(removed.getCategory());
            System.out.println("Da xoa han muc cua danh muc: " + categoryName);
        } else {
            System.out.println("Khong tim thay han muc cho danh muc: " + categoryName);
        }
    }

    public void displayAllBudgets() {
        if (budgetsByStr.isEmpty()) {
            System.out.println("Chua co han muc ngan sach nao.");
            return;
        }

        System.out.println("\n===== HAN MUC NGAN SACH =====");
        for (Budget budget : budgetsByStr.values()) {
            double spent = calculateMonthlySpentByCategory(budget.getCategory());
            double remaining = budget.getRemaining(spent);
            String status = budget.isExceeded(spent) ? "[VUOT HAN MUC!]" : "[Trong han muc]";

            System.out.printf("  Danh muc: %-15s | Han muc: %,15.0f VND | Da chi: %,15.0f VND | Con lai: %,15.0f VND | %s\n",
                    budget.getCategory().getName(), budget.getLimit(), spent, remaining, status);
        }
    }

    public void checkBudgetWarning(Category category) {
        Budget budget = budgetsByStr.get(category.getName().toLowerCase());
        if (budget != null) {
            double spent = calculateMonthlySpentByCategory(category);
            if (budget.isExceeded(spent)) {
                System.out.printf("!! CANH BAO: Chi tieu danh muc '%s' da VUOT han muc! " +
                                "(Da chi: %,.0f / Han muc: %,.0f VND)\n",
                        category.getName(), spent, budget.getLimit());
            } else {
                double remaining = budget.getRemaining(spent);
                if (remaining < budget.getLimit() * 0.2) {
                    System.out.printf("!! LUU Y: Danh muc '%s' sap dat han muc! " +
                                    "(Con lai: %,.0f / Han muc: %,.0f VND)\n",
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
        budgets.put(category, new Budget(category, limit, period));
        budgetsByStr.put(category.getName().toLowerCase(), new Budget(category, limit, period));
        System.out.printf("Đã đặt ngân sách tối đa cho '%s' là %,.0f VND (%s).\n", category.getName(), limit, period);
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
     * ========================================================
     * CÁC HÀM BỔ SUNG MỚI (KHÔNG TRÙNG CHỨC NĂNG VỚI CODE TRÊN)
     * ========================================================
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

    public void displayStatisticsByCategory() {
        Map<String, Double> stats = statisticsByCategory();
        if (stats.isEmpty()) {
            System.out.println("Chua co giao dich chi tieu nao.");
            return;
        }

        System.out.println("\n===== CHI TIEU THEO DANH MUC =====");
        double total = 0;
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            System.out.printf("  %-20s: %,15.0f VND\n", entry.getKey(), entry.getValue());
            total += entry.getValue();
        }
        System.out.printf("  %-20s: %,15.0f VND\n", "TONG CHI TIEU", total);
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
            System.out.println("Khong co chi tieu nao trong nam " + year);
            return;
        }

        System.out.printf("\n===== CHI TIEU THEO THANG - NAM %d =====\n", year);
        double total = 0;
        for (Map.Entry<Integer, Double> entry : monthlyExpense.entrySet()) {
            System.out.printf("  Thang %2d: %,15.0f VND\n", entry.getKey(), entry.getValue());
            total += entry.getValue();
        }
        System.out.printf("  %-10s: %,15.0f VND\n", "TONG", total);
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

    public Expense createFromRecurring(RecurringExpense recurring) {
        String newId = recurring.getId() + "_" + LocalDate.now().toString();
        return new Expense(newId, recurring.getAmount(), recurring.getNote(),
                LocalDate.now(), recurring.getCategory(), recurring.getWallet(),
                recurring.getPaymentMethod());
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

    // ========== GETTERS ==========
    public List<Transaction> getTransactions() { return transactions; }
    public List<Wallet> getWallets() { return wallets; }
    public List<Category> getCategories() { return categories; }
    public Map<Category, Budget> getBudgets() { return budgets; }
}