package models;

import enums.TransactionType;

import java.time.LocalDate;

public class Income extends Transaction{
    private String source;

    public Income(String id, double amount, LocalDate date, Category category, String note, Wallet wallet, String source) {
        super(id, amount, date, note, category, wallet);
        this.source = source;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.INCOME;
    }

    @Override
    public double getSignedAmount() {
        return this.amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
