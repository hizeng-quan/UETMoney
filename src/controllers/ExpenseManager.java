package controllers;

import enums.Period;
import enums.TransactionType;
import models.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Lớp điều phối chính: quản lý danh sách Transaction, Wallet, Category, Budget.
 * Cung cấp các chức năng CRUD, thống kê cơ bản và nâng cao, kiểm tra hạn mức ngân sách.
 */
public class ExpenseManager {
    private List<Transaction> transactions;
    private List<Wallet> wallets;
    private List<Category> categories;
    private Map<String, Budget> budgets; // key = category name (lowercase)

    public ExpenseManager() {
        this.transactions = new ArrayList<>();
        this.wallets = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.budgets = new HashMap<>();
    }

    // ========== SUPPORT FUNCTIONS ==========

    public void addWallet(Wallet wallet) {
        wallets.add(wallet);
        System.out.println("Da them vi: " + wallet.getName());
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

    // ========== CRUD FUNCTIONS ==========

    /**
     * Thêm giao dịch mới và cập nhật số dư ví.
     * Nếu là chi tiêu, tự động kiểm tra hạn mức ngân sách.
     */
    public void addTransaction(Transaction t) {
        transactions.add(t);
        Wallet wallet = t.getWallet();

        if (t.getSignedAmount() > 0) {
            wallet.deposit(t.getAmount());
        } else {
            wallet.withdraw(t.getAmount());
        }
        System.out.println("Da ghi nhan giao dich va cap nhat so du!");

        // Kiểm tra hạn mức ngân sách nếu là chi tiêu
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
                wallet.withdraw(target.getAmount());
            } else {
                wallet.deposit(target.getAmount());
            }

            transactions.remove(target);
            System.out.println("Da xoa giao dich " + id + " va cap nhat lai so du!");
        } else {
            System.out.println("Khong tim thay giao dich ma " + id);
        }
    }

    public void displayAllTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("Chua co giao dich nao.");
            return;
        } else {
            System.out.println("LICH SU GIAO DICH");
            for (Transaction t : transactions) {
                t.printInfo();
            }
        }
    }

    // ========== BUDGET FUNCTIONS ==========

    /**
     * Thêm hạn mức ngân sách cho danh mục.
     */
    public void addBudget(Budget budget) {
        budgets.put(budget.getCategory().getName().toLowerCase(), budget);
        System.out.printf("Da dat han muc %,.0f VND cho danh muc '%s' (chu ky: %s)\n",
                budget.getLimit(), budget.getCategory().getName(), budget.getPeriod());
    }

    /**
     * Xóa hạn mức ngân sách theo danh mục.
     */
    public void removeBudget(String categoryName) {
        Budget removed = budgets.remove(categoryName.toLowerCase());
        if (removed != null) {
            System.out.println("Da xoa han muc cua danh muc: " + categoryName);
        } else {
            System.out.println("Khong tim thay han muc cho danh muc: " + categoryName);
        }
    }

    /**
     * Hiển thị tất cả hạn mức đã đặt và trạng thái hiện tại.
     */
    public void displayAllBudgets() {
        if (budgets.isEmpty()) {
            System.out.println("Chua co han muc ngan sach nao.");
            return;
        }

        System.out.println("\n===== HAN MUC NGAN SACH =====");
        for (Budget budget : budgets.values()) {
            double spent = calculateMonthlySpentByCategory(budget.getCategory());
            double remaining = budget.getRemaining(spent);
            String status = budget.isExceeded(spent) ? "[VUOT HAN MUC!]" : "[Trong han muc]";

            System.out.printf("  Danh muc: %-15s | Han muc: %,15.0f VND | Da chi: %,15.0f VND | Con lai: %,15.0f VND | %s\n",
                    budget.getCategory().getName(), budget.getLimit(), spent, remaining, status);
        }
    }

    /**
     * Kiểm tra và cảnh báo nếu chi tiêu danh mục vượt hạn mức.
     */
    public void checkBudgetWarning(Category category) {
        Budget budget = budgets.get(category.getName().toLowerCase());
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

    // ========== BASIC STATISTICS ==========

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
        System.out.printf("\n THONG KE THANG %d/%d\n", month, year);
        System.out.printf("Tong thu: %,.2f VND\n", totalIncome);
        System.out.printf("Tong chi: %,.2f VND\n", totalExpense);
        System.out.printf("Thuc nhan: %,.2f VND\n", (totalIncome - totalExpense));
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

        System.out.printf("\n THONG KE NAM %d\n", year);
        System.out.printf("Tong thu: %,.2f VND\n", totalIncome);
        System.out.printf("Tong chi: %,.2f VND\n", totalExpense);
        System.out.printf("Thuc nhan: %,.2f VND\n", (totalIncome - totalExpense));
    }

    // ========== ADVANCED STATISTICS ==========

    /**
     * Thống kê chi tiêu theo từng danh mục.
     * @return Map<tên danh mục, tổng chi tiêu>
     */
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
     * Hiển thị chi tiêu theo từng danh mục.
     */
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

    /**
     * Chi tiêu theo từng tháng trong năm.
     */
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
     * Tìm khoản chi lớn nhất.
     * @return Transaction có amount lớn nhất trong danh sách chi tiêu
     */
    public Transaction findLargestExpense() {
        Transaction largest = null;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                if (largest == null || t.getAmount() > largest.getAmount()) {
                    largest = t;
                }
            }
        }
        return largest;
    }

    /**
     * Tìm khoản chi nhỏ nhất.
     * @return Transaction có amount nhỏ nhất trong danh sách chi tiêu
     */
    public Transaction findSmallestExpense() {
        Transaction smallest = null;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                if (smallest == null || t.getAmount() < smallest.getAmount()) {
                    smallest = t;
                }
            }
        }
        return smallest;
    }

    /**
     * Top N danh mục tốn kém nhất.
     */
    public void displayTopExpensiveCategories(int n) {
        Map<String, Double> stats = statisticsByCategory();
        if (stats.isEmpty()) {
            System.out.println("Chua co giao dich chi tieu nao.");
            return;
        }

        // Sắp xếp theo giá trị giảm dần
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(stats.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int limit = Math.min(n, sorted.size());
        System.out.printf("\n===== TOP %d DANH MUC TON KEM =====\n", limit);
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = sorted.get(i);
            System.out.printf("  #%d %-20s: %,15.0f VND\n", (i + 1), entry.getKey(), entry.getValue());
        }
    }

    /**
     * Hiển thị khoản chi lớn nhất và nhỏ nhất.
     */
    public void displayMinMaxExpense() {
        Transaction largest = findLargestExpense();
        Transaction smallest = findSmallestExpense();

        if (largest == null) {
            System.out.println("Chua co giao dich chi tieu nao.");
            return;
        }

        System.out.println("\n===== KHOAN CHI LON NHAT / NHO NHAT =====");
        System.out.printf("  LON NHAT: ");
        largest.printInfo();
        System.out.printf("  NHO NHAT: ");
        smallest.printInfo();
    }

    // ========== RECURRING TRANSACTION FUNCTIONS ==========

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
     * Kiểm tra và hiển thị các giao dịch định kỳ đến hạn.
     * @return danh sách giao dịch định kỳ đến hạn hôm nay
     */
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
     * Tạo giao dịch mới từ giao dịch định kỳ đến hạn.
     */
    public Expense createFromRecurring(RecurringExpense recurring) {
        String newId = recurring.getId() + "_" + LocalDate.now().toString();
        return new Expense(newId, recurring.getAmount(), recurring.getNote(),
                LocalDate.now(), recurring.getCategory(), recurring.getWallet(),
                recurring.getPaymentMethod());
    }

    // ========== HELPER: Tính tổng chi tiêu theo danh mục trong tháng hiện tại ==========

    /**
     * Tính tổng chi tiêu của một danh mục trong tháng hiện tại.
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

    // ========== GETTERS ==========

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Map<String, Budget> getBudgets() {
        return budgets;
    }

    /**
     * Nhập dữ liệu đã load từ CSV vào manager.
     * Hợp nhất (merge) Category và Wallet — nếu đã tồn tại thì dùng lại, không tạo trùng.
     */
    public void importTransactions(List<Transaction> loadedTransactions) {
        for (Transaction t : loadedTransactions) {
            // Hợp nhất Category
            Category existingCat = getCategoryByName(t.getCategory().getName());
            if (existingCat == null) {
                addCategory(t.getCategory());
            }

            // Hợp nhất Wallet
            Wallet existingWallet = getWalletByName(t.getWallet().getName());
            if (existingWallet == null) {
                wallets.add(t.getWallet());
            }

            // Thêm transaction (không cập nhật số dư vì đây là dữ liệu đã lưu)
            transactions.add(t);
        }
    }
}
