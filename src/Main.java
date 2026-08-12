import controllers.ExpenseManager;
import javafx.application.Application;
import storage.CsvStorage;
import interfaces.Storage;

import java.util.Scanner;
import ConsoleUI.UIManager;
import ControllerUI.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Chọn UI / Console \n");
        String res;
        Scanner scanner = new Scanner(System.in);
        res = scanner.nextLine();
        if (res.equals("UI")) {
            Application.launch(Launcher.class);
        } else if (res.equalsIgnoreCase("Console")){
            ExpenseManager manager = new ExpenseManager();

            // Thiết lập Storage (đa hình: có thể đổi sang JsonStorage)
            Storage storage = new CsvStorage();
            manager.setStorage(storage);

            // Nạp dữ liệu từ file khi khởi động
            System.out.println("\n Đang nạp dữ liệu...");
            manager.loadAllData();

            // Khởi tạo class quản lý giao diện
            UIManager ui = new UIManager(manager, scanner);

            // Kiểm tra và nhắc nhở giao dịch định kỳ đến hạn (Tùy chọn B)
            ui.checkRecurringOnStartup();

            while (true) {
                System.out.println("\n--- PHẦN MỀM QUẢN LÝ CHI TIÊU CÁ NHÂN ---");
                System.out.println("[1] Thêm giao dịch mới");
                System.out.println("[2] Xóa giao dịch theo mã");
                System.out.println("[3] Xem tất cả lịch sử giao dịch");
                System.out.println("[4] Xem số dư (Tổng & Chi tiết từng ví)");
                System.out.println("[5] Thống kê theo tháng/năm");
                System.out.println("[6] Thêm ví tiền mới");
                System.out.println("[7] Thêm danh mục mới");
                System.out.println("[8] Đặt/Kiểm tra hạn mức ngân sách");
                System.out.println("[9] Thống kê nâng cao (Danh mục, Tháng, Max/Min)");
                System.out.println("[10] Tìm kiếm giao dịch");
                System.out.println("[11] Quản lý giao dịch định kỳ");
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
                    case "11": ui.recurringTransactionUI(); break;
                    case "0":
                        // Tự động lưu dữ liệu khi thoát
                        System.out.println("\n Đang lưu dữ liệu...");
                        manager.saveAllData();
                        System.out.println("Cảm ơn bạn đã sử dụng phần mềm. Tạm biệt!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ. Vui lòng nhập lại!");
                }
            }
        }
    }
}