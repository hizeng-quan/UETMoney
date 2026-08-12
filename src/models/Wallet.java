package models;

import enums.WalletType;
import exception.InsufficientBalanceException;

public abstract class Wallet {
    protected String name;
    protected double balance;

    public Wallet(String name, double initialBalance) {
        this.name = name;
        this.balance = Math.max(0, initialBalance);
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
        } else {
            throw new InsufficientBalanceException("Số dư của bạn không đủ!");
        }
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public abstract WalletType getWalletType();
}

