package models;

import enums.WalletType;

public class EWallet extends Wallet {
    private String provider;

    public EWallet(String name, double initialBalance, String provider) {
        super(name, initialBalance);
        this.provider = provider;
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.EWALLET;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
