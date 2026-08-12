package interfaces;

import models.*;

import java.util.List;
import java.util.Map;

public interface Storage {

    // --- Transaction ---
    void saveTransactions(List<Transaction> transactions, String path) throws Exception;
    List<Transaction> loadTransactions(String path, List<Category> categories, List<Wallet> wallets) throws Exception;

    // --- Wallet ---
    void saveWallets(List<Wallet> wallets, String path) throws Exception;
    List<Wallet> loadWallets(String path) throws Exception;

    // --- Category ---
    void saveCategories(List<Category> categories, String path) throws Exception;
    List<Category> loadCategories(String path) throws Exception;

    // --- Budget ---
    void saveBudgets(Map<Category, Budget> budgets, String path) throws Exception;
    Map<Category, Budget> loadBudgets(String path, List<Category> categories) throws Exception;
}
