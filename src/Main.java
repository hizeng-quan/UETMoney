import controllers.ExpenseManager;
import javafx.application.Application;
import storage.CsvStorage;
import interfaces.Storage;

import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "data/transactions.csv";

    public static void main(String[] args) {
        ExpenseManager manager = new ExpenseManager();
        CsvStorage storage = new CsvStorage();
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
                    }
                    manager.displayTopExpensiveCategories(n);
                } catch (NumberFormatException e) {
                    System.out.println("Loi: Vui long nhap so hop le!");
                }
                break;
            default:
                System.out.println("Lua chon khong hop le!");
        }
    }

    // ==================== [10] THÊM GIAO DỊCH ĐỊNH KỲ ====================

    private static void addRecurringExpenseUI(ExpenseManager manager, Scanner scanner) {
        try {
            System.out.println("\nTHEM GIAO DICH DINH KY");
            System.out.print("Nhap ma giao dich: ");
            String id = scanner.nextLine();

            System.out.print("Nhap so tien: ");
            double amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) {
                System.out.println("Loi: So tien phai lon hon 0!");
                return;
            }

            System.out.print("Ngay bat dau (dd/MM/yyyy): ");
            String dateStr = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(dateStr, formatter);

            System.out.print("Ghi chu (vd: Tien nha, Internet...): ");
            String note = scanner.nextLine();

            System.out.print("Nhap ten vi su dung: ");
            String walletName = scanner.nextLine();
            Wallet wallet = manager.getWalletByName(walletName);
            if (wallet == null) {
                System.out.println("Loi: Khong tim thay vi '" + walletName + "'!");
                return;
            }

            System.out.print("Nhap ten danh muc: ");
            String catName = scanner.nextLine();
            Category category = manager.getCategoryByName(catName);
            if (category == null) {
                System.out.println("Loi: Khong tim thay danh muc '" + catName + "'!");
                return;
            }

            System.out.print("Phuong thuc thanh toan: ");
            String paymentMethod = scanner.nextLine();

            System.out.println("Chu ky lap lai:");
            System.out.println("[1] Hang ngay  [2] Hang tuan  [3] Hang thang  [4] Hang nam");
            System.out.print("Chon: ");
            String periodChoice = scanner.nextLine();
            Period period;
            switch (periodChoice) {
                case "1":
                    period = Period.DAILY;
                    break;
                case "2":
                    period = Period.WEEKLY;
                    break;
                case "3":
                    period = Period.MONTHLY;
                    break;
                case "4":
                    period = Period.YEARLY;
                    break;
                default:
                    System.out.println("Lua chon khong hop le, mac dinh la MONTHLY.");
                    period = Period.MONTHLY;
            }

            RecurringExpense re = new RecurringExpense(id, amount, note, date,
                    category, wallet, paymentMethod, period);
            manager.addTransaction(re);

            System.out.printf("Dao han tiep theo: %s\n", re.nextDueDate());

        } catch (NumberFormatException e) {
            System.out.println("Loi: So tien nhap vao khong hop le!");
        } catch (DateTimeParseException e) {
            System.out.println("Loi: Dinh dang ngay thang khong dung (Can nhap dd/MM/yyyy)!");
        }
    }

    // ==================== [11] KIỂM TRA GIAO DỊCH ĐỊNH KỲ ĐẾN HẠN
    // ====================

    private static void checkRecurringDueUI(ExpenseManager manager, Scanner scanner) {
        List<RecurringExpense> allRecurring = manager.getRecurringExpenses();

        if (allRecurring.isEmpty()) {
            System.out.println("Chua co giao dich dinh ky nao.");
            return;
        }

        System.out.println("\n===== DANH SACH GIAO DICH DINH KY =====");
        for (RecurringExpense re : allRecurring) {
            re.printInfo();
        }

        // Kiểm tra giao dịch đến hạn hôm nay
        List<RecurringExpense> dueList = manager.checkDueRecurring();
        if (dueList.isEmpty()) {
            System.out.println("\nKhong co giao dich dinh ky nao den han hom nay.");
        } else {
            System.out.printf("\nCo %d giao dich dinh ky DEN HAN hom nay:\n", dueList.size());
            for (RecurringExpense re : dueList) {
                System.out.printf("  - %s: %,.0f VND (%s)\n",
                        re.getNote(), re.getAmount(), re.getPeriod());

                System.out.print("  Ban co muon tao giao dich tu dong? (y/n): ");
                String answer = scanner.nextLine().trim().toLowerCase();
                if (answer.equals("y")) {
                    Expense newExpense = manager.createFromRecurring(re);
                    manager.addTransaction(newExpense);
                    System.out.println("  Da tao giao dich: " + newExpense.getId());
                }
            }
        }
    }
}