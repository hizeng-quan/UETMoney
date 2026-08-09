import controllers.ExpenseManager;
import javafx.application.Application;
import models.*;
import java.util.Scanner;
import ConsoleUI.UIManager;
import ControllerUI.*;
import storage.CsvStorage;
import java.util.List;

public class Main {
    private static final String DATA_FILE = "data/transactions.csv";

    public static void main(String[] args) {
        System.out.println("Chọn UI / Console \n");
        String res;
        Scanner scanner = new Scanner(System.in);
        res = scanner.nextLine();
        if (res.equals("UI")) {
            Application.launch(Launcher.class);
        } else if (res.equals("Console")){
            ExpenseManager manager = new ExpenseManager();
            CsvStorage storage = new CsvStorage();

            // Nạp dữ liệu từ file CSV khi khởi động bản Console
            try {
                List<Transaction> loaded = storage.load(DATA_FILE);
                if (!loaded.isEmpty()) {
                    manager.importTransactions(loaded);
                    System.out.println(">>> Đã nạp " + loaded.size() + " giao dịch từ file " + DATA_FILE);
                }
            } catch (Exception e) {
                System.out.println(">>> Bắt đầu với dữ liệu mới (Không đọc được file CSV: " + e.getMessage() + ")");
            }

            // Khởi tạo class quản lý giao diện Console
            UIManager ui = new UIManager(manager, scanner);

            while (true) {
                System.out.println("\n--- PHẦN MỀM QUẢN LÝ CHI TIÊU CÁ NHÂN (UETMoney) ---");
                System.out.println("[1] Thêm giao dịch mới");
                System.out.println("[2] Xóa giao dịch theo mã");
                System.out.println("[3] Xem tất cả lịch sử giao dịch");
                System.out.println("[4] Xem số dư (Tổng & Chi tiết từng ví)");
                System.out.println("[5] Thống kê theo tháng/năm");
                System.out.println("[6] Thêm ví tiền mới");
                System.out.println("[7] Thêm danh mục mới");
                System.out.println("[8] Đặt/Kiểm tra hạn mức ngân sách");
                System.out.println("[9] Thống kê nâng cao (Max/Min, Top chi tiêu, Phân loại...)");
                System.out.println("[10] Tìm kiếm giao dịch");
                System.out.println("[11] Quản lý & Kiểm tra giao dịch định kỳ đến hạn");
                System.out.println("[0] Thoát chương trình");
                System.out.print("Mời bạn chọn chức năng (0-11): ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1": ui.addTransactionUI(); break;
                    case "2": ui.removeTransactionUI(); break;
                    case "3": ui.showAllTransactionsUI(); break;
                    case "4": ui.showBalancesUI(); break;
                    case "5": ui.showStatisticsUI(); break;
                    case "6": ui.addWalletUI(); break;
                    case "7": ui.addCategoryUI(); break;
                    case "8": ui.setBudgetUI(); break;
                    case "9": ui.advancedStatisticsUI(); break;
                    case "10": ui.searchTransactionUI(); break;
                    case "11": ui.checkAndManageRecurringUI(); break; // Chức năng tích hợp mới
                    case "0":
                        // Tự động lưu dữ liệu vào file CSV khi thoát bản Console
                        try {
                            storage.save(manager.getTransactions(), DATA_FILE);
                            System.out.println(">>> Đã tự động lưu thành công dữ liệu vào " + DATA_FILE);
                        } catch (Exception e) {
                            System.out.println("Lỗi lưu dữ liệu: " + e.getMessage());
                        }
                        System.out.println("Cảm ơn bạn đã sử dụng phần mềm. Tạm biệt!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ. Vui lòng nhập lại!");
                }
            }
        } else {
            System.out.println("Lựa chọn không hợp lệ. Chương trình kết thúc.");
        }
    }
}