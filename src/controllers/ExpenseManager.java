package controllers;

import models.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseManager {
    private List<Transaction> transactions;
    private List<Wallet> wallets;
    private List<Category> categories;

    public ExpenseManager() {
        this.transactions = new ArrayList<>();
        this.wallets = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    /**
     * ADD WALLETS AND CATEGORIES.
     */

    public void addWallet(Wallet wallet) {
        wallets.add(wallet);
        System.out.println("Đã thêm ví: " + wallet.getName());
    }

    public void addCategory(Category category) {
        categories.add(category);
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
}
