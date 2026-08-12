package models;

import enums.WalletType;

public class BankAccount extends Wallet {
    private String bankName;
    private String accountNumber;

    private static final double WITHDRAW_FEE = 1100.0;

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
            return;
        }
        if (this.balance < amount) {
            throw new InsufficientBalanceException(
                    String.format("Số dư không đủ để rút %,.0f VND (hiện có %,.0f VND).", amount, this.balance));
        }
        this.balance -= amount;
        System.out.printf("Đã rút %,.2f VND. Số dư còn lại %,.2f VND%n", amount, this.balance);
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
