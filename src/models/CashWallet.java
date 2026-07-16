package models;

import enums.WalletType;

public class CashWallet extends Wallet {
    public CashWallet(String name, double initialBalance) {
        super(name, initialBalance);
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.CASH;
    }
}
