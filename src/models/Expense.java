package models;

import enums.TransactionType;

import java.time.LocalDate;

public class Expense extends Transaction{
    private String paymentMethod;

    public Expense(String id, double amount, String note, LocalDate date, Category category, Wallet wallet, String paymentMethod) {
        super(id, amount, date, note, category, wallet);
        this.paymentMethod = paymentMethod;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public double getSignedAmount() {
        return -this.amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
