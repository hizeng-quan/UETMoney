import controllers.ExpenseManager;
import enums.TransactionType;
import models.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import ConsoleUI.UIManager;

public class Main {
    public static void main(String[] args) {
        ExpenseManager manager = new ExpenseManager();
        Scanner scanner = new Scanner(System.in);

        // Khởi tạo class quản lý giao diện
        UIManager ui = new UIManager(manager, scanner);

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
            System.out.println("[9] Thống kê nâng cao (Max/Min, Top chi tiêu)");
            System.out.println("[0] Thoát chương trình");
            System.out.print("Mời bạn chọn chức năng (0-9): ");

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
                case "0":
                    System.out.println("Cảm ơn bạn đã sử dụng phần mềm. Tạm biệt!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ. Vui lòng nhập lại!");
            }
        }
    }
}