package controllers;

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
    /**
     * CRUD Functions.
     */

    public void addTransaction(Transaction t) {
        transactions.add(t);
        Wallet wallet = t.getWallet();

        if (t.getSignedAmount() > 0) {
            wallet.deposit(t.getAmount());
        } else {
            wallet.withdraw(t.getAmount());
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
                wallet.withdraw(target.getAmount());
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

    public void advancedStat(int month, int year) {
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
}
