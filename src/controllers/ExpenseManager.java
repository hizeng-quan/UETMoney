package controllers;

import exception.InsufficientBalanceException;
import models.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class ExpenseManager {
    private List<Transaction> transactions;
    private List<Wallet> wallets;
    private List<Category> categories;
    private Map<Category, Budget> budgets;

    public ExpenseManager() {
        this.transactions = new ArrayList<>();
        this.wallets = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.budgets = new HashMap<>();
    }

    /**
     * Support Functions.
     */

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
    /**
     * CRUD Functions.
     */

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
        } else {
            System.out.println("LỊCH SỬ GIAO DỊCH");
            for (Transaction t : transactions) {
                t.printInfo();
            }
        }
    }

    /**
     * Statistics Functions.
     */

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
        System.out.printf("\n THỐNG KÊ THÁNG %d/%d\n", month,year);
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

        System.out.printf("\n THỐNG KÊ NĂM %d\n",year);
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

    /**
     * Budget Limit.
     */

    public void setBudget(Category category, double limit, enums.Period period) {
        budgets.put(category, new Budget(category, limit, period));
        System.out.printf("Đã đặt ngân sách tối đa cho '%s' là %,.0f VND (%s).\n", category.getName(), limit, period);
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
            System.out.println("CẢNH BÁO NGÂN SÁCH");
            System.out.printf("Bạn đã chi tiêu VƯỢT HẠN MỨC danh mục '%s'!\n", category.getName());

            String periodStr = "";
            if (budget.getPeriod() == enums.Period.DAILY) periodStr = "Ngày";
            else if (budget.getPeriod() == enums.Period.WEEKLY) periodStr = "Tuần";
            else if (budget.getPeriod() == enums.Period.MONTHLY) periodStr = "Tháng";
            else if (budget.getPeriod() == enums.Period.YEARLY) periodStr = "Năm";

            System.out.printf("Ngân sách (%s): %,.0f | Đã tiêu: %,.0f (Vượt quá %,.0f VND)\n", periodStr, budget.getLimit(), spent, (spent - budget.getLimit()));
        }
    }

    /**
     * Searching Functions.
     */

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
