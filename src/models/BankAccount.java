package models;

import enums.WalletType;
import exception.InsufficientBalanceException;

public class BankAccount extends Wallet{
    private String bankName;
    private String accountNumber;

    public BankAccount(String name, double initialBalance, String bankName, String accountNumber) {
        super(name, initialBalance);
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.BANK;
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0!");
        }

        if (this.balance < amount) {
            throw new InsufficientBalanceException("Số dư không đủ để rút tiền!");
        }

        this.balance -= amount;
        System.out.printf("Đã rút %,.2f VND. Số dư còn lại %,.2f VND\n", amount, this.balance);
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}