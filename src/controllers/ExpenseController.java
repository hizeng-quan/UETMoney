package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ExpenseController {

    @FXML private Button btnToggleMenu;
    @FXML private VBox sideMenu;

    // Các View trong StackPane
    @FXML private VBox viewTransaction;
    @FXML private VBox viewWallet;
    @FXML private VBox viewBudget;
    @FXML private VBox viewStatistic;

    // Các nút chuyển trang trong Menu
    @FXML private Button btnNavTransaction;
    @FXML private Button btnNavWallet;
    @FXML private Button btnNavBudget;
    @FXML private Button btnNavStatistic;

    @FXML
    public void initialize() {
        // 1. Xử lý Bật/Tắt (Ẩn/Hiện) thanh Menu 3 gạch
        btnToggleMenu.setOnAction(e -> {
            boolean isVisible = sideMenu.isVisible();
            sideMenu.setVisible(!isVisible);
            sideMenu.setManaged(!isVisible); // Co giãn lại không gian layout khi ẩn
        });

        // 2. Xử lý chuyển đổi giữa các màn hình
        btnNavTransaction.setOnAction(e -> showView(viewTransaction, btnNavTransaction));
        btnNavWallet.setOnAction(e -> showView(viewWallet, btnNavWallet));
        btnNavBudget.setOnAction(e -> showView(viewBudget, btnNavBudget));
        btnNavStatistic.setOnAction(e -> showView(viewStatistic, btnNavStatistic));
    }

    private void showView(VBox targetView, Button activeBtn) {
        // Ẩn tất cả các view
        viewTransaction.setVisible(false);
        viewWallet.setVisible(false);
        viewBudget.setVisible(false);
        viewStatistic.setVisible(false);

        // Hiển thị view được chọn
        targetView.setVisible(true);

        // Reset màu tất cả nút về transparent
        btnNavTransaction.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0;");
        btnNavWallet.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0;");
        btnNavBudget.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0;");
        btnNavStatistic.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0;");

        // Highlight nút đang chọn màu xanh
        activeBtn.setStyle("-fx-background-color: #2ed573; -fx-text-fill: #121212; -fx-font-weight: bold;");
    }
}