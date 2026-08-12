import controllers.ExpenseManager;
import controllers.Launcher;
import interfaces.Storage;
import javafx.application.Application;
import java.util.Scanner;
import controllers.UIManager;
import storage.CsvStorage;

public class Main {

    public static void main(String[] args) {
        System.out.println("Chọn UI / Console \n");
        String res;
        Scanner scanner = new Scanner(System.in);
        res = scanner.nextLine();
        if (res.equals("UI")) {
            Application.launch(Launcher.class);
        } else if (res.equals("Console")){
            ExpenseManager manager = new ExpenseManager();

            // Thiết lập Storage (có thể đổi sang JsonStorage để lưu JSON)
            Storage storage = new CsvStorage();
            manager.setStorage(storage);

            // Nạp toàn bộ dữ liệu từ file khi khởi động
            System.out.println("\n Đang nạp dữ liệu...");
            manager.loadAllData();

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
                System.out.println("[12] Nhập/Xuất dữ liệu giao dịch (CSV/JSON)");
                System.out.println("[0] Thoát chương trình");
                System.out.print("Mời bạn chọn chức năng (0-12): ");

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
                    case "12": ui.importExportUI(); break;
                    case "0":
                        // Tự động lưu toàn bộ dữ liệu khi thoát
                        manager.saveAllData();
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