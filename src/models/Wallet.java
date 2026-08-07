package models;

import enums.WalletType;
import exception.InsufficientBalanceException;

public abstract class Wallet {
    protected String name;
    protected double balance;

    public Wallet(String name, double balance) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ví không được để trống!");
        }
        if (balance < 0) {
            throw new IllegalArgumentException("Số dư ban đầu không được nhỏ hơn 0!");
        }
        this.name = name.trim();
        this.balance = balance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0!");
        }
        this.balance += amount;
    }

    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0!");
        }
        if (amount > this.balance) {
            throw new InsufficientBalanceException("Số dư ví không đủ để thực hiện giao dịch!");
        }
        this.balance -= amount;
        System.out.printf("Đã rút %,.2f VND. Số dư còn lại %,.2f VND\n", amount, this.balance);
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public abstract WalletType getWalletType();
}

