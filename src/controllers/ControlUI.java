package controllers;

import exception.InsufficientBalanceException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.*;
import enums.Period;
import enums.TransactionType;
import enums.WalletType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import storage.CsvStorage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    @FXML private Button btnImportCsv;
    @FXML private Button btnExportJson;
    @FXML private Button btnImportJson;

    // Sidebar Nav Buttons
    @FXML private Button btnNavTransaction;
    @FXML private Button btnNavWallet;
    @FXML private Button btnNavBudget;
    @FXML private Button btnNavStatistic;
    @FXML private Button btnNavRecurring;
    @FXML private Button btnNavCategory;
    @FXML private VBox sideMenu;

    // View Containers (Views)
    @FXML private VBox viewTransaction;
    @FXML private VBox viewWallet;
    @FXML private VBox viewBudget;
    @FXML private VBox viewStatistic;
    @FXML private VBox viewRecurring;
    @FXML private VBox viewCategory;

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
    @FXML private DatePicker dpSearchFrom;
    @FXML private DatePicker dpSearchTo;
    @FXML private TextField txtSearchMinAmount;
    @FXML private TextField txtSearchMaxAmount;
    @FXML private Button btnClearSearch;
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
    @FXML private Button btnDeleteWallet;
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
    @FXML private Button btnDeleteBudget;
    @FXML private TableView<BudgetWrapper> tblBudgets;
    @FXML private TableColumn<BudgetWrapper, String> colBudgetCategory;
    @FXML private TableColumn<BudgetWrapper, Double> colBudgetLimit;
    @FXML private TableColumn<BudgetWrapper, Double> colBudgetSpent;
    @FXML private TableColumn<BudgetWrapper, Period> colBudgetPeriod;
    @FXML private TableColumn<BudgetWrapper, String> colBudgetStatus;

    //View 4: Chi phi định kỳ
    @FXML private TableView<RecurringExpense> tblRecurring;
    @FXML private TableColumn<RecurringExpense, String> colRecurId;
    @FXML private TableColumn<RecurringExpense, String> colRecurCategory;
    @FXML private TableColumn<RecurringExpense, Double> colRecurAmount;
    @FXML private TableColumn<RecurringExpense, Period> colRecurPeriod;
    @FXML private TableColumn<RecurringExpense, LocalDate> colRecurNextDate;
    @FXML private TableColumn<RecurringExpense, String> colRecurWallet;

    // View 5: Thống Kê (Mới)
    @FXML private PieChart chartExpenseByCategory;
    @FXML private ImageView imgBudgetStatus;
    @FXML private Label lblBudgetMascotText;
    @FXML private DatePicker dpStatDate;
    @FXML private ComboBox<String> cbStatPeriod;
    @FXML private Button btnUpdateStat;

    @FXML private Label lblStatTotalIncome;
    @FXML private Label lblStatTotalExpense;
    @FXML private Label lblStatBalance;
    @FXML private Label lblStatMaxExpense;
    @FXML private Label lblStatMinExpense;
    @FXML private Label lblStatTopCategory;

    // View 6: Danh Mục
    @FXML private TextField txtCategoryName;
    @FXML private ComboBox<TransactionType> cbCategoryGroup;
    @FXML private Button btnAddCategory;
    @FXML private Button btnDeleteCategory;
    @FXML private TableView<Category> tblCategories;
    @FXML private TableColumn<Category, String> colCategoryName;
    @FXML private TableColumn<Category, TransactionType> colCategoryGroup;
    @FXML private TableColumn<Category, String> colCategoryNote;


    // Ảnh
    private Image imgHappy;
    private Image imgNormal;
    private Image imgWarning;
    private Image imgDanger;

    // ==========================================
    // 2. DATA MODELS & FIELDS
    // ==========================================
    private final ExpenseManager expenseManager = new ExpenseManager();

    // Observable Lists cho JavaFX TableViews & ComboBoxes
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final ObservableList<Wallet> walletList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ObservableList<BudgetWrapper> budgetList = FXCollections.observableArrayList();
    private final ObservableList<RecurringExpense> recurringList = FXCollections.observableArrayList();

    private boolean isMenuExpanded = true;

    // ==========================================
    // 3. INITIALIZATION
    // ==========================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nạp dữ liệu khi khởi động UI
        expenseManager.setStorage(new CsvStorage());
        expenseManager.loadAllData();

        // Đăng ký lưu dữ liệu tự động khi đóng ứng dụng
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Đang lưu dữ liệu trước khi thoát (UI)...");
            expenseManager.saveAllData();
        }));

        loadImages();
        setupTableColumns();
        setupComboBoxes();
        setupNavigation();
        setupActions();
        refreshAllViews();
    }

    private void loadImages() {
        try {
            imgHappy = new Image(getClass().getResourceAsStream("/images/0.jpg"));
            imgNormal = new Image(getClass().getResourceAsStream("/images/1.jpg"));
            imgWarning = new Image(getClass().getResourceAsStream("/images/2.jpg"));
            imgDanger = new Image(getClass().getResourceAsStream("/images/3.jpg"));
        } catch (Exception e) {
            System.out.println("Không tìm thấy file ảnh 1, 2, 3!");
        }
    }

    private void setupTableColumns() {
        // TableView Giao Dịch
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getType() != null ? cell.getValue().getType().toString() : ""
        ));

        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(tc -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", amount));
                }
            }
        });
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
        colWalletBalance.setCellFactory(tc -> new TableCell<Wallet, Double>() {
            @Override
            protected void updateItem(Double balance, boolean empty) {
                super.updateItem(balance, empty);
                if (empty || balance == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%,.0f VNĐ", balance));
                    setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                }
            }
        });
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
        colBudgetLimit.setCellFactory(tc -> new TableCell<BudgetWrapper, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", amount));
                }
            }
        });
        colBudgetSpent.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getSpentAmount()
        ));
        colBudgetSpent.setCellFactory(tc -> new TableCell<BudgetWrapper, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", amount));
                }
            }
        });
        colBudgetPeriod.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getBudget().getPeriod()
        ));
        colBudgetStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        tblBudgets.setItems(budgetList);

        // TableView Danh Mục
        colCategoryName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategoryGroup.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCategoryNote.setCellValueFactory(cell -> {
            Category cat = cell.getValue();
            List<Transaction> transactions = expenseManager.getTransactions();

            // 1. Tính tổng chi tiêu của app
            double totalAllExpenses = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            // 2. Tính tổng chi tiêu của riêng danh mục này
            double catSum = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory().equals(cat))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            if (cat.getType() == TransactionType.EXPENSE && totalAllExpenses > 0) {
                double percentage = (catSum / totalAllExpenses) * 100;
                return new javafx.beans.property.SimpleStringProperty(
                        String.format("Tổng: %,.0f VNĐ - Chiếm: %.1f%%", catSum, percentage)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        tblCategories.setItems(categoryList);

        // TableView chi phí định kỳ
        colRecurId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRecurCategory.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCategory().getName()));
        colRecurAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colRecurPeriod.setCellValueFactory(new PropertyValueFactory<>("period"));
        colRecurNextDate.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().nextDueDate()));
        colRecurWallet.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getWallet().getName()));
        tblRecurring.setItems(recurringList);
    }

    private void setupComboBoxes() {
        cbWalletType.setItems(FXCollections.observableArrayList(WalletType.values()));
        cbCategoryGroup.setItems(FXCollections.observableArrayList(TransactionType.values()));
        cbPeriod.setItems(FXCollections.observableArrayList(Period.values()));
        cbBudgetPeriod.setItems(FXCollections.observableArrayList(Period.values()));

        cbStatPeriod.setItems(FXCollections.observableArrayList("Theo Tháng", "Theo Năm"));
        cbStatPeriod.setValue("Theo Tháng");

        cbWallet.setConverter(new javafx.util.StringConverter<Wallet>() {
        @Override
        public String toString(Wallet wallet) {
            return (wallet == null) ? null : wallet.getName();
        }

        @Override
        public Wallet fromString(String string) {
            return null;
        }
    });

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
        btnNavRecurring.setOnAction(e -> switchView(viewRecurring, btnNavRecurring));
        btnNavStatistic.setOnAction(e -> switchView(viewStatistic, btnNavStatistic));
        btnNavCategory.setOnAction(e -> switchView(viewCategory, btnNavCategory));

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
        viewRecurring.setVisible(activeView == viewRecurring);
        viewStatistic.setVisible(activeView == viewStatistic);
        viewCategory.setVisible(activeView == viewCategory);

        Button[] navButtons = {btnNavTransaction, btnNavWallet, btnNavBudget, btnNavRecurring, btnNavStatistic, btnNavCategory};
        for (Button btn : navButtons) {
            if (btn == activeBtn) {
                btn.setStyle("-fx-background-color: #2ed573; -fx-text-fill: #121212; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0; -fx-background-radius: 5; -fx-cursor: hand;");
            }
        }

        if (activeView == viewStatistic) {
            updateStatisticsView();
        }
    }

    private void checkBudgetAlerts(Category currentCategory) {
        StringBuilder alerts = new StringBuilder();
        for (BudgetWrapper bw : budgetList) {
            if (currentCategory != null && !bw.getBudget().getCategory().equals(currentCategory)) continue;
            
            double spent = bw.getSpentAmount();
            double limit = bw.getBudget().getLimit();
            if (spent > limit) {
                alerts.append("Vượt hạn mức: ").append(bw.getBudget().getCategory().getName()).append(" (Đã chi ").append(String.format("%,.0f", spent)).append(" / ").append(String.format("%,.0f", limit)).append(")\n");
            } else if (spent >= limit * 0.8) {
                alerts.append("Gần vượt mức: ").append(bw.getBudget().getCategory().getName()).append(" (Đã chi ").append(String.format("%,.0f", spent)).append(" / ").append(String.format("%,.0f", limit)).append(")\n");
            }
        }
        if (alerts.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo Ngân Sách", alerts.toString());
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
        btnClearSearch.setOnAction(e -> handleClearSearch());

        // Ví
        btnAddWallet.setOnAction(e -> handleAddWallet());
        btnDeleteWallet.setOnAction(e -> {
            Wallet selected = tblWallets.getSelectionModel().getSelectedItem();
            try {
                if (selected != null) {
                    expenseManager.removeWallet(selected);
                    refreshAllViews();
                    showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã xóa ví thành công!");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ví cần xóa!");
                }
            } catch (InsufficientBalanceException er) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ví đã thực hiện giao dịch không thể xoá!");
            }
        });

        // Ngân Sách
        btnSetBudget.setOnAction(e -> handleSetBudget());
        btnDeleteBudget.setOnAction(e -> {
            BudgetWrapper selected = tblBudgets.getSelectionModel().getSelectedItem();
            if (selected != null) {
                expenseManager.removeBudget(selected.getBudget().getCategory().getName());
                refreshAllViews();
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã xóa ngân sách!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ngân sách cần xóa!");
            }
        });

        // Danh Mục
        btnAddCategory.setOnAction(e -> handleAddCategory());
        btnDeleteCategory.setOnAction(e -> {
            Category selected = tblCategories.getSelectionModel().getSelectedItem();
            if (selected != null) {
                expenseManager.removeCategory(selected);
                refreshAllViews();
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã xóa danh mục!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn danh mục cần xóa!");
            }
        });

        // Thống Kê
        btnUpdateStat.setOnAction(e -> updateStatisticsView());

        // Xuất/Nhập File
        btnExportCsv.setOnAction(event -> handleExportCsv());
        btnImportCsv.setOnAction(event -> handleImportCsv());
        btnExportJson.setOnAction(event -> handleExportJson());
        btnImportJson.setOnAction(event -> handleImportJson());

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
            if (txtAmount.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền!");
                return;
            }

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

            String id = Transaction.generateId(category, date);
            Transaction t;

            String source = category.getName();
            String paymentMethod;
            if (wallet instanceof BankAccount) {
                paymentMethod = "Chuyển khoản - " + ((BankAccount) wallet).getBankName();
            } else if (wallet instanceof EWallet) {
                paymentMethod = ((EWallet) wallet).getProvider();
            } else {
                paymentMethod = "Tiền mặt";
            }

            if (category.getType() == TransactionType.INCOME) {
                t = new Income(id, amount, date, category, note, wallet, source);
            } else {
                if (period != null) {
                    t = new RecurringExpense(id, amount, note, date, category, wallet, paymentMethod, period);
                } else {
                    t = new Expense(id, amount, note, date, category, wallet, paymentMethod);
                }
            }

            // Gọi hàm thêm giao dịch từ Manager (có kiểm tra ngoại lệ số dư)
            expenseManager.addTransaction(t);

            // Xóa trắng form & Cập nhật UI
            refreshAllViews();
            clearTransactionForm();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm giao dịch thành công!");

            // Kiểm tra cảnh báo ngân sách cho danh mục vừa thêm
            if (category.getType() == TransactionType.EXPENSE) {
                checkBudgetAlerts(category);
            }

        } catch (InsufficientBalanceException e) {
            // Bắt lỗi không đủ số dư từ ExpenseManager và báo lên màn hình
            showAlert(Alert.AlertType.ERROR, "Lỗi số dư", "Giao dịch thất bại: " + e.getMessage());
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
        LocalDate fromDate = dpSearchFrom.getValue();
        LocalDate toDate = dpSearchTo.getValue();

        Double minAmount = null;
        Double maxAmount = null;
        try {
            if (!txtSearchMinAmount.getText().trim().isEmpty()) {
                minAmount = Double.parseDouble(txtSearchMinAmount.getText().trim());
            }
            if (!txtSearchMaxAmount.getText().trim().isEmpty()) {
                maxAmount = Double.parseDouble(txtSearchMaxAmount.getText().trim());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Số tiền lọc phải là một số hợp lệ!");
            return;
        }

        List<Transaction> filtered = new ArrayList<>();
        List<Transaction> allTransactions = expenseManager.getTransactions();

        for (Transaction t : allTransactions) {
            boolean match = true;

            // Lọc từ khóa
            if (!keyword.isEmpty()) {
                if (!(t.getNote().toLowerCase().contains(keyword) ||
                      t.getCategory().getName().toLowerCase().contains(keyword) ||
                      t.getWallet().getName().toLowerCase().contains(keyword) ||
                      t.getId().toLowerCase().contains(keyword))) {
                    match = false;
                }
            }

            // Lọc ngày
            if (fromDate != null && t.getDate().isBefore(fromDate)) {
                match = false;
            }
            if (toDate != null && t.getDate().isAfter(toDate)) {
                match = false;
            }

            // Lọc số tiền
            if (minAmount != null && Math.abs(t.getAmount()) < minAmount) {
                match = false;
            }
            if (maxAmount != null && Math.abs(t.getAmount()) > maxAmount) {
                match = false;
            }

            if (match) {
                filtered.add(t);
            }
        }
        transactionList.setAll(filtered);
    }

    private void handleClearSearch() {
        txtSearch.clear();
        dpSearchFrom.setValue(null);
        dpSearchTo.setValue(null);
        txtSearchMinAmount.clear();
        txtSearchMaxAmount.clear();
        refreshAllViews();
    }

    private void handleAddWallet() {
        try {
            String walletName = txtWalletName.getText();
            double balance = Double.parseDouble(txtWalletBalance.getText());
            WalletType type = cbWalletType.getValue();

            Wallet newWallet = null;

            if (type == WalletType.BANK) {
                String bankName = txtBankName.getText();
                String accountNumber = txtAccountNumber.getText();
                if (bankName == null || bankName.trim().isEmpty() || accountNumber == null || accountNumber.trim().isEmpty()) {
                    throw new IllegalArgumentException("Tên ngân hàng và số tài khoản không được để trống!");
                }
                newWallet = new BankAccount(walletName, balance, bankName, accountNumber);
            } else if (type == WalletType.EWALLET) {
                String provider = txtProvider.getText();
                if (provider == null || provider.trim().isEmpty()) {
                    throw new IllegalArgumentException("Nhà cung cấp ví điện tử không được để trống!");
                }
                newWallet = new EWallet(walletName, balance, provider);

            } else {
                newWallet = new CashWallet(walletName, balance);
            }

            expenseManager.addWallet(newWallet);
            clearWalletForm();
            refreshAllViews();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo ví mới thành công!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Số dư ban đầu phải là một số hợp lệ!");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Thiếu thông tin", e.getMessage());
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

            if (limit <= 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Hạn mức phải lớn hơn 0!");
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

        try {
            Category category = new Category(name, type);
            expenseManager.addCategory(category);
            txtCategoryName.clear();
            refreshAllViews();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm danh mục mới!");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Trùng tên", e.getMessage());
        }
    }

    private void handleExportCsv() {
        if (expenseManager.getTransactions() == null || expenseManager.getTransactions().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dữ liệu giao dịch nào để xuất!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn nơi lưu file CSV");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("danh_sach_chi_tieu_" + java.time.LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv")
        );

        File file = fileChooser.showSaveDialog(txtWalletName.getScene().getWindow());

        if (file != null) {
            try {
                CsvStorage csvStorage = new CsvStorage();
                csvStorage.saveTransactions(expenseManager.getTransactions(), file.getAbsolutePath());

                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Xuất dữ liệu ra file CSV thành công!\nĐường dẫn: " + file.getAbsolutePath());

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống",
                        "Không thể xuất file CSV. Vui lòng thử lại!\nChi tiết: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleImportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file CSV để nạp giao dịch");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        File file = fileChooser.showOpenDialog(txtWalletName.getScene().getWindow());

        if (file != null) {
            try {
                CsvStorage csvStorage = new CsvStorage();
                List<Transaction> imported = csvStorage.loadTransactions(file.getAbsolutePath(), expenseManager.getCategories(), expenseManager.getWallets());
                for (Transaction t : imported) {
                    expenseManager.addTransaction(t);
                }
                refreshAllViews();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã nạp " + imported.size() + " giao dịch từ file CSV!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Không thể nạp file CSV!\nChi tiết: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleExportJson() {
        if (expenseManager.getTransactions() == null || expenseManager.getTransactions().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dữ liệu giao dịch nào để xuất!");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn nơi lưu file JSON");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("danh_sach_chi_tieu_" + java.time.LocalDate.now() + ".json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        File file = fileChooser.showSaveDialog(txtWalletName.getScene().getWindow());

        if (file != null) {
            try {
                storage.JsonStorage jsonStorage = new storage.JsonStorage();
                jsonStorage.saveTransactions(expenseManager.getTransactions(), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất dữ liệu ra file JSON thành công!\nĐường dẫn: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể xuất file JSON. Vui lòng thử lại!\nChi tiết: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleImportJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file JSON để nạp giao dịch");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(txtWalletName.getScene().getWindow());

        if (file != null) {
            try {
                storage.JsonStorage jsonStorage = new storage.JsonStorage();
                List<Transaction> imported = jsonStorage.loadTransactions(file.getAbsolutePath(), expenseManager.getCategories(), expenseManager.getWallets());
                for (Transaction t : imported) {
                    expenseManager.addTransaction(t);
                }
                refreshAllViews();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã nạp " + imported.size() + " giao dịch từ file JSON!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Không thể nạp file JSON!\nChi tiết: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // 6. UI REFRESH & HELPER METHODS
    // ==========================================
    private void refreshAllViews() {
        // Cập nhật dữ liệu mới
        List<Wallet> wallets = expenseManager.getWallets();
        List<Category> categories = expenseManager.getCategories();
        List<Transaction> transactions = expenseManager.getTransactions();
        Map<Category, Budget> budgetsMap = expenseManager.getBudgets();
        List<BudgetWrapper> bList = new ArrayList<>();

        // Cập nhật cb
        cbWallet.setItems(FXCollections.observableArrayList(wallets));
        cbCategory.setItems(FXCollections.observableArrayList(categories));
        cbBudgetCategory.setItems(FXCollections.observableArrayList(categories));

        // Cập nhật Danh sách Ví
        walletList.setAll(wallets);

        // Cập nhật Danh sách Danh mục
        categoryList.setAll(categories);

        // Cập nhật Giao dịch
        transactionList.setAll(transactions);

        // Cập nhật Ngân sách
        LocalDate now = LocalDate.now();
        budgetsMap.forEach((cat, b) -> {
            // Tính tổng tiền đã chi của danh mục này TRONG THÁNG HIỆN TẠI
            double totalSpentThisMonth = transactions.stream()
                    .filter(t -> t.getCategory().equals(cat)
                            && t.getType() == TransactionType.EXPENSE
                            && t.getDate().getMonth() == now.getMonth()
                            && t.getDate().getYear() == now.getYear())
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            boolean isExceeded = b.isExceeded(totalSpentThisMonth);
            String status = isExceeded ? " VƯỢT MỨC (" + String.format("%,.0f", totalSpentThisMonth) + ")" : " An toàn";

            bList.add(new BudgetWrapper(b, status, totalSpentThisMonth));
        });
        budgetList.setAll(bList);

        // Cập nhật CHI PHÍ ĐỊNH KỲ
        recurringList.setAll(expenseManager.getRecurringExpenses());

        // Cập nhật tổng số dư hiển thị Header
        double totalBalance = expenseManager.calculateTotalBalance();
        lblTotalBalance.setText(String.format("%,.0f VNĐ", totalBalance));

        // Câp nhật ảnh
        updateBudgetStatusImage();

        // Cập nhật biểu đồ
        updateStatisticsView();
    }

    private void updateStatisticsView() {
        if (chartExpenseByCategory == null) return;

        LocalDate filterDate = (dpStatDate != null && dpStatDate.getValue() != null)
                ? dpStatDate.getValue()
                : LocalDate.now();

        int month = filterDate.getMonthValue();
        int year = filterDate.getYear();
        boolean isYearly = "Theo Năm".equals(cbStatPeriod.getValue());

        List<Transaction> transactions = expenseManager.getTransactions();
        List<Transaction> filteredT = new ArrayList<>();

        for (Transaction t : transactions) {
            boolean match = t.getDate().getYear() == year;
            if (!isYearly) {
                match = match && t.getDate().getMonthValue() == month;
            }
            if (match) filteredT.add(t);
        }

        double totalIncome = 0;
        double totalExpense = 0;
        double maxExpense = 0;
        double minExpense = Double.MAX_VALUE;
        Transaction maxT = null;
        Transaction minT = null;

        Map<String, Double> categorySum = new HashMap<>();

        for (Transaction t : filteredT) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome += t.getAmount();
            } else if (t.getType() == TransactionType.EXPENSE) {
                double amt = t.getAmount();
                totalExpense += amt;

                if (amt > maxExpense) {
                    maxExpense = amt;
                    maxT = t;
                }
                if (amt < minExpense) {
                    minExpense = amt;
                    minT = t;
                }

                String catName = t.getCategory().getName();
                categorySum.put(catName, categorySum.getOrDefault(catName, 0.0) + amt);
            }
        }

        if (minExpense == Double.MAX_VALUE) minExpense = 0;

        if (lblStatTotalIncome != null) lblStatTotalIncome.setText(String.format("%,.0f VNĐ", totalIncome));
        if (lblStatTotalExpense != null) lblStatTotalExpense.setText(String.format("%,.0f VNĐ", totalExpense));
        if (lblStatBalance != null) lblStatBalance.setText(String.format("%,.0f VNĐ", totalIncome - totalExpense));

        if (lblStatMaxExpense != null) {
            String maxNote = (maxT != null && maxT.getNote() != null && !maxT.getNote().trim().isEmpty()) ? " (" + maxT.getNote() + ")" : "";
            lblStatMaxExpense.setText(maxT != null ? String.format("%,.0f VNĐ%s", maxExpense, maxNote) : "0 VNĐ");
        }
        
        if (lblStatMinExpense != null) {
            String minNote = (minT != null && minT.getNote() != null && !minT.getNote().trim().isEmpty()) ? " (" + minT.getNote() + ")" : "";
            lblStatMinExpense.setText(minT != null ? String.format("%,.0f VNĐ%s", minExpense, minNote) : "0 VNĐ");
        }

        // Tìm Top 1 Danh Mục
        String topCat = "Không có";
        double maxCatSum = 0;
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : categorySum.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            if (entry.getValue() > maxCatSum) {
                maxCatSum = entry.getValue();
                topCat = entry.getKey();
            }
        }

        if (lblStatTopCategory != null) lblStatTopCategory.setText(topCat.equals("Không có") ? topCat : String.format("%s (%,.0f VNĐ)", topCat, maxCatSum));

        chartExpenseByCategory.setData(pieChartData);
        if (isYearly) {
            chartExpenseByCategory.setTitle("Thống kê chi tiêu năm " + year);
        } else {
            chartExpenseByCategory.setTitle("Thống kê chi tiêu tháng " + month + "/" + year);
        }
    }

    private void updateBudgetStatusImage() {
        Map<Category, Budget> budgetsMap = expenseManager.getBudgets();
        List<Transaction> transactions = expenseManager.getTransactions();

        if (budgetsMap.isEmpty()) {
            imgBudgetStatus.setImage(imgHappy);
            lblBudgetMascotText.setText("Living the dream!");
            return;
        }

        int totalBudgets = budgetsMap.size();
        int exceededCount = 0;

        for (Map.Entry<Category, Budget> entry : budgetsMap.entrySet()) {
            Category cat = entry.getKey();
            Budget b = entry.getValue();

            double spent = transactions.stream()
                    .filter(t -> t.getCategory().equals(cat) && t.getType() == TransactionType.EXPENSE)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            if (b.isExceeded(spent)) {
                exceededCount++;
            }
        }

        // Logic đổi ảnh theo yêu cầu của bạn
        if (exceededCount == 0) {
            imgBudgetStatus.setImage(imgNormal);
            lblBudgetMascotText.setText("Good life!");
        } else if (exceededCount < totalBudgets) {
            imgBudgetStatus.setImage(imgWarning);
            lblBudgetMascotText.setText("Everything is fine, except your wallet.");
        } else {
            imgBudgetStatus.setImage(imgDanger);
            lblBudgetMascotText.setText("Bro, stop spending.");
        }
    }

    private void clearTransactionForm() {
        txtAmount.clear();
        txtNote.clear();
        cbCategory.getSelectionModel().clearSelection();
        cbCategory.setValue(null);
        cbWallet.getSelectionModel().clearSelection();
        cbWallet.setValue(null);
        cbPeriod.getSelectionModel().clearSelection();
        cbPeriod.setValue(null);
        dpDate.setValue(LocalDate.now());

        tblTransactions.getSelectionModel().clearSelection();
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
        private final double spentAmount;

        public BudgetWrapper(Budget budget, String statusText, double spentAmount) {
            this.budget = budget;
            this.statusText = statusText;
            this.spentAmount = spentAmount;
        }

        public Budget getBudget() { return budget; }
        public String getStatusText() { return statusText; }
        public double getSpentAmount() { return spentAmount; }
    }
}
