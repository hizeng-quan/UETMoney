package models;

import enums.WalletType;

public class EWallet extends Wallet {
    private String provider;

    public EWallet(String name, double balance, String provider) {
        super(name, balance);

        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("Nhà cung cấp ví không được để trống!");
        }

        this.provider = provider.trim();
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
