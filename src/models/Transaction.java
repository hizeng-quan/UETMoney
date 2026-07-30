package models;

import enums.TransactionType;

import java.time.LocalDate;

public abstract class Transaction {
    protected String id;
    protected double amount;
    protected LocalDate date;
    protected String note;
    protected Category category;
    protected Wallet wallet;

    public Transaction(String id, double amount, LocalDate date, String note, Category category, Wallet wallet) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.category = category;
        this.wallet = wallet;
    }

    public abstract TransactionType getType();
    public abstract double getSignedAmount();

    public void printInfo() {
        System.out.println("Giao dịch " + id + " | " + getCategory() + " | " + getSignedAmount());
        System.out.println("Ví: " + getWallet().getName() + " | " + getDate());
        if (note != null && !note.isEmpty()) {
            System.out.println("   Ghi chú: " + note);
        }
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public Wallet getWallet() {
        return wallet;
    }
}
