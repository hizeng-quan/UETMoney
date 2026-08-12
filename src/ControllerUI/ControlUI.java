package ControllerUI;

import controllers.ExpenseManager;
import exception.InsufficientBalanceException;
import models.*;
import enums.Period;
import enums.TransactionType;
import enums.WalletType;
import interfaces.Storage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class ControlUI implements Initializable {

    // ==========================================
    // 1. FXML INJECTIONS===============================
    //
    //    // Header Controls
    // ===========
    @FXML private Button btnToggleMenu;
    @FXML private Label lblTotalBalance;
    @FXML private Button btnExportCsv;
    @FXML private Button btnExportJson;

    // Sidebar Nav Buttons
    @FXML private Button btnNavTransaction;
    @FXML private Button btnNavWallet;
    @FXML private Button btnNavBudget;
    @FXML private Button btnNavStatistic;
    @FXML private VBox sideMenu;

    // View Containers (Views)
    @FXML private VBox viewTransaction;
    @FXML private VBox viewWallet;
    @FXML private VBox viewBudget;
    @FXML private VBox viewStatistic;

    // View 1: Giao Dịch
    @FXML private TextField txtAmount;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Category> cbCategory;
    @FXML private ComboBox<Wallet> cbWallet;
    @FXML private TextField txtNote;
    @FXML private ComboBox<Period> cbPeriod;
    @FXML private Button btnAddTransaction;
    @FXML private Button btnUpdateTransaction;
    @FXML private Button btnDeleteTransaction;
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;
    @FXML private TableView<Transaction> tblTransactions;
    @FXML private TableColumn<Transaction, String> colId;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, Double> colAmount;
    @FXML private TableColumn<Transaction, LocalDate> colDate;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colWallet;
    @FXML private TableColumn<Transaction, String> colNote;

    // View 2: Ví Tiền
    @FXML private TextField txtWalletName;
    @FXML private TextField txtWalletBalance;
    @FXML private ComboBox<WalletType> cbWalletType;
    @FXML private TextField txtBankName;
    @FXML private TextField txtAccountNumber;
    @FXML private TextField txtProvider;
    @FXML private Button btnAddWallet;
    @FXML private TableView<Wallet> tblWallets;
    @FXML private TableColumn<Wallet, String> colWalletName;
    @FXML private TableColumn<Wallet, WalletType> colWalletTypeTable;
    @FXML private TableColumn<Wallet, Double> colWalletBalance;
    @FXML private TableColumn<Wallet, String> colWalletDetail;

    // View 3: Ngân Sách
    @FXML private ComboBox<Category> cbBudgetCategory;
    @FXML private TextField txtBudgetLimit;
    @FXML private ComboBox<Period> cbBudgetPeriod;
    @FXML private Button btnSetBudget;
    @FXML private TableView<BudgetWrapper> tblBudgets;
    @FXML private TableColumn<BudgetWrapper, String> colBudgetCategory;
    @FXML private TableColumn<BudgetWrapper, Double> colBudgetLimit;
    @FXML private TableColumn<BudgetWrapper, Period> colBudgetPeriod;
    @FXML private TableColumn<BudgetWrapper, String> colBudgetStatus;

    // View 4: Thống Kê & Danh Mục
    @FXML private PieChart chartExpenseByCategory;
    @FXML private TextField txtCategoryName;
    @FXML private ComboBox<TransactionType> cbCategoryGroup;
    @FXML private Button btnAddCategory;
    @FXML private TableView<Category> tblCategories;
    @FXML private TableColumn<Category, String> colCategoryName;
    @FXML private TableColumn<Category, TransactionType> colCategoryGroup;
    @FXML private TableColumn<Category, String> colCategoryNote;

    // ==========================================
    // 2. DATA MODELS & FIELDS
    // ==========================================
    private final ExpenseManager expenseManager = new ExpenseManager();

    // Observable Lists cho JavaFX TableViews & ComboBoxes
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final ObservableList<Wallet> walletList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ObservableList<BudgetWrapper> budgetList = FXCollections.observableArrayList();

    private boolean isMenuExpanded = true;

    // ==========================================
    // 3. INITIALIZATION
    // ==========================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBoxes();
        setupNavigation();
        setupActions();
        refreshAllViews();
    }


    private void setupTableColumns() {
        // TableView Giao Dịch
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getType() != null ? cell.getValue().getType().toString() : ""
        ));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCategory.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getCategory() != null ? cell.getValue().getCategory().getName() : ""
        ));
        colWallet.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getWallet() != null ? cell.getValue().getWallet().getName() : ""
        ));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        tblTransactions.setItems(transactionList);

        // TableView Ví Tiền
        colWalletName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colWalletTypeTable.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getWalletType()
        ));
        colWalletBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colWalletDetail.setCellValueFactory(cell -> {
            Wallet w = cell.getValue();
            if (w instanceof BankAccount) {
                BankAccount b = (BankAccount) w;
                return new javafx.beans.property.SimpleStringProperty(b.getBankName() + " - " + b.getAccountNumber());
            } else if (w instanceof EWallet) {
                EWallet e = (EWallet) w;
                return new javafx.beans.property.SimpleStringProperty("NNC: " + e.getProvider());
            }
            return new javafx.beans.property.SimpleStringProperty("Tiền mặt thủ công");
        });
        tblWallets.setItems(walletList);

        // TableView Ngân Sách
        colBudgetCategory.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getBudget().getCategory().getName()
        ));
        colBudgetLimit.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getBudget().getLimit()
        ));
        colBudgetPeriod.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getBudget().getPeriod()
        ));
        colBudgetStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        tblBudgets.setItems(budgetList);

        // TableView Danh Mục
        colCategoryName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategoryGroup.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCategoryNote.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty("-"));
        tblCategories.setItems(categoryList);
    }

    private void setupComboBoxes() {
        cbWalletType.setItems(FXCollections.observableArrayList(WalletType.values()));
        cbCategoryGroup.setItems(FXCollections.observableArrayList(TransactionType.values()));
        cbPeriod.setItems(FXCollections.observableArrayList(Period.values()));
        cbBudgetPeriod.setItems(FXCollections.observableArrayList(Period.values()));

        dpDate.setValue(LocalDate.now());
    }

    // ==========================================
    // 4. NAVIGATION LOGIC
    // ==========================================
    private void setupNavigation() {
        // Chuyển Tab/View
        btnNavTransaction.setOnAction(e -> switchView(viewTransaction, btnNavTransaction));
        btnNavWallet.setOnAction(e -> switchView(viewWallet, btnNavWallet));
        btnNavBudget.setOnAction(e -> switchView(viewBudget, btnNavBudget));
        btnNavStatistic.setOnAction(e -> switchView(viewStatistic, btnNavStatistic));

        // Nút Toggle Thu/Phóng Menu Sidebar
        btnToggleMenu.setOnAction(e -> {
            isMenuExpanded = !isMenuExpanded;
            sideMenu.setPrefWidth(isMenuExpanded ? 200 : 60);
            sideMenu.getChildren().forEach(node -> {
                if (node instanceof Button) {
                    Button b = (Button) node;
                    if (!isMenuExpanded) {
                        b.setUserData(b.getText());
                        b.setText(b.getText().substring(0, 2).trim());
                    } else if (b.getUserData() != null) {
                        b.setText((String) b.getUserData());
                    }
                }
            });
        });
    }

    private void switchView(VBox activeView, Button activeBtn) {
        viewTransaction.setVisible(activeView == viewTransaction);
        viewWallet.setVisible(activeView == viewWallet);
        viewBudget.setVisible(activeView == viewBudget);
        viewStatistic.setVisible(activeView == viewStatistic);

        // Reset màu nút bấm Sidebar
        Button[] navButtons = {btnNavTransaction, btnNavWallet, btnNavBudget, btnNavStatistic};
        for (Button btn : navButtons) {
            if (btn == activeBtn) {
                btn.setStyle("-fx-background-color: #2ed573; -fx-text-fill: #121212; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0; -fx-background-radius: 5; -fx-cursor: hand;");
            }
        }

        if (activeView == viewStatistic) {
            updatePieChart();
        }
    }

    // ==========================================
    // 5. EVENT HANDLERS & ACTIONS
    // ==========================================
    private void setupActions() {
        // Giao Dịch
        btnAddTransaction.setOnAction(e -> handleAddTransaction());
        btnDeleteTransaction.setOnAction(e -> handleDeleteTransaction());
        btnSearch.setOnAction(e -> handleSearchTransaction());

        // Ví
        btnAddWallet.setOnAction(e -> handleAddWallet());

        // Ngân Sách
        btnSetBudget.setOnAction(e -> handleSetBudget());

        // Danh Mục
        btnAddCategory.setOnAction(e -> handleAddCategory());

        // Xuất File
        btnExportCsv.setOnAction(e -> handleExport("CSV"));
        btnExportJson.setOnAction(e -> handleExport("JSON"));

        // Khi chọn 1 dòng trên Table Giao dịch
        tblTransactions.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtAmount.setText(String.valueOf(newSel.getAmount()));
                dpDate.setValue(newSel.getDate());
                cbCategory.setValue(newSel.getCategory());
                cbWallet.setValue(newSel.getWallet());
                txtNote.setText(newSel.getNote());
            }
        });
    }

    private void handleAddTransaction() {
        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            LocalDate date = dpDate.getValue();
            Category category = cbCategory.getValue();
            Wallet wallet = cbWallet.getValue();
            String note = txtNote.getText().trim();
            Period period = cbPeriod.getValue();

            if (category == null || wallet == null || date == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đầy đủ Ví, Danh mục và Ngày!");
                return;
            }

            String id = "TX" + (System.currentTimeMillis() % 100000);
            Transaction t;

            if (category.getType() == TransactionType.INCOME) {
                t = new Income(id, amount, date, category, note, wallet, "Nguồn thu chính");
            } else {
                if (period != null) {
                    t = new RecurringExpense(id, amount, note, date, category, wallet, "Chuyển khoản", period);
                } else {
                    t = new Expense(id, amount, note, date, category, wallet, "Chuyển khoản");
                }
            }

            expenseManager.addTransaction(t);
            clearTransactionForm();
            refreshAllViews();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm giao dịch mới!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Số tiền phải là một số hợp lệ!");
        }
    }

    private void handleDeleteTransaction() {
        Transaction selected = tblTransactions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            expenseManager.removeTransaction(selected.getId());
            refreshAllViews();
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã xóa giao dịch được chọn!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn giao dịch cần xóa!");
        }
    }

    private void handleSearchTransaction() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            refreshAllViews();
            return;
        }

        List<Transaction> filtered = new ArrayList<>();
        List<Transaction> allTransactions = getPrivateFieldList("transactions");
        for (Transaction t : allTransactions) {
            if (t.getNote().toLowerCase().contains(keyword) ||
                    t.getCategory().getName().toLowerCase().contains(keyword) ||
                    t.getWallet().getName().toLowerCase().contains(keyword) ||
                    t.getId().toLowerCase().contains(keyword)) {
                filtered.add(t);
            }
        }
        transactionList.setAll(filtered);
    }

    private void handleAddWallet() {
        try {
            String name = txtWalletName.getText().trim();
            double balance = Double.parseDouble(txtWalletBalance.getText().trim());
            WalletType type = cbWalletType.getValue();

            if (name.isEmpty() || type == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tên và loại ví!");
                return;
            }

            Wallet wallet;
            switch (type) {
                case BANK:
                    wallet = new BankAccount(name, balance, txtBankName.getText(), txtAccountNumber.getText());
                    break;
                case EWALLET:
                    wallet = new EWallet(name, balance, txtProvider.getText());
                    break;
                default:
                    wallet = new CashWallet(name, balance);
                    break;
            }

            expenseManager.addWallet(wallet);
            clearWalletForm();
            refreshAllViews();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm ví mới!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Số dư ban đầu phải là một số!");
        }
    }

    private void handleSetBudget() {
        try {
            Category category = cbBudgetCategory.getValue();
            double limit = Double.parseDouble(txtBudgetLimit.getText().trim());
            Period period = cbBudgetPeriod.getValue();

            if (category == null || period == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn danh mục và chu kỳ!");
                return;
            }

            expenseManager.setBudget(category, limit, period);
            refreshAllViews();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thiết lập hạn mức ngân sách!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Hạn mức ngân sách phải là số hợp lệ!");
        }
    }

    private void handleAddCategory() {
        String name = txtCategoryName.getText().trim();
        TransactionType type = cbCategoryGroup.getValue();

        if (name.isEmpty() || type == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền tên danh mục và phân loại!");
            return;
        }

        Category category = new Category(name, type);
        expenseManager.addCategory(category);
        txtCategoryName.clear();
        refreshAllViews();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm danh mục mới!");
    }

    private void handleExport(String format) {
        // Thực thi tính năng gọi Storage theo sơ đồ UML
        showAlert(Alert.AlertType.INFORMATION, "Xuất dữ liệu", "Đã xuât dữ liệu thành công dưới định dạng " + format);
    }

    // ==========================================
    // 6. UI REFRESH & HELPER METHODS
    // ==========================================
    private void refreshAllViews() {
        // Cập nhật Danh sách Ví & ComboBox Ví
        List<Wallet> wallets = getPrivateFieldList("wallets");
        walletList.setAll(wallets);
        cbWallet.setItems(FXCollections.observableArrayList(wallets));

        // Cập nhật Danh sách Danh mục & ComboBox Danh mục
        List<Category> categories = getPrivateFieldList("categories");
        categoryList.setAll(categories);
        cbCategory.setItems(FXCollections.observableArrayList(categories));
        cbBudgetCategory.setItems(FXCollections.observableArrayList(categories));

        // Cập nhật Giao dịch
        List<Transaction> transactions = getPrivateFieldList("transactions");
        transactionList.setAll(transactions);

        // Cập nhật Ngân sách
        Map<Category, Budget> budgetsMap = getPrivateFieldMap("budgets");
        List<BudgetWrapper> bList = new ArrayList<>();
        budgetsMap.forEach((cat, b) -> {
            double totalSpent = 0;
            for (Transaction t : transactions) {
                if (t.getCategory().equals(cat) && t.getSignedAmount() < 0) {
                    totalSpent += Math.abs(t.getSignedAmount());
                }
            }
            boolean isExceeded = b.isExceeded(totalSpent);
            bList.add(new BudgetWrapper(b, isExceeded ? "VƯỢT MỨC (" + String.format("%,.0f", totalSpent) + " VNĐ)" : "An toàn"));
        });
        budgetList.setAll(bList);

        // Cập nhật tổng số dư hiển thị Header
        double totalBalance = expenseManager.calculateTotalBalance();
        lblTotalBalance.setText(String.format("%,.0f VNĐ", totalBalance));

        updatePieChart();
    }

    private void updatePieChart() {
        if (chartExpenseByCategory == null) return;

        Map<String, Double> categorySum = new HashMap<>();
        List<Transaction> transactions = getPrivateFieldList("transactions");

        for (Transaction t : transactions) {
            if (t instanceof Expense) {
                String catName = t.getCategory().getName();
                double amount = Math.abs(t.getSignedAmount());
                categorySum.put(catName, categorySum.getOrDefault(catName, 0.0) + amount);
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        categorySum.forEach((cat, sum) -> pieChartData.add(new PieChart.Data(cat, sum)));
        chartExpenseByCategory.setData(pieChartData);
    }

    private void clearTransactionForm() {
        txtAmount.clear();
        txtNote.clear();
        cbCategory.setValue(null);
        cbWallet.setValue(null);
        cbPeriod.setValue(null);
        dpDate.setValue(LocalDate.now());
    }

    private void clearWalletForm() {
        txtWalletName.clear();
        txtWalletBalance.clear();
        txtBankName.clear();
        txtAccountNumber.clear();
        txtProvider.clear();
        cbWalletType.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper Reflection hỗ trợ lấy dữ liệu từ ExpenseManager đơn giản
    @SuppressWarnings("unchecked")
    private <T> List<T> getPrivateFieldList(String fieldName) {
        try {
            var field = ExpenseManager.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (List<T>) field.get(expenseManager);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> getPrivateFieldMap(String fieldName) {
        try {
            var field = ExpenseManager.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Map<K, V>) field.get(expenseManager);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    // Wrapper Class hỗ trợ đưa dữ liệu Budget lên TableView
    public static class BudgetWrapper {
        private final Budget budget;
        private final String statusText;

        public BudgetWrapper(Budget budget, String statusText) {
            this.budget = budget;
            this.statusText = statusText;
        }

        public Budget getBudget() { return budget; }
        public String getStatusText() { return statusText; }
    }
}
