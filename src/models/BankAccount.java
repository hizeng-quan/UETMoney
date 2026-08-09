package models;

import enums.WalletType;
import exception.InsufficientBalanceException;

public class BankAccount extends Wallet{
    private String bankName;
    private String accountNumber;

    public BankAccount(String name, double balance, String bankName, String accountNumber) {
        super(name, balance);

        if (bankName == null || bankName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ngân hàng không được để trống!");
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Số tài khoản không được để trống!");
        }

        this.bankName = bankName.trim();
        this.accountNumber = accountNumber.trim();
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.BANK;
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException {
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