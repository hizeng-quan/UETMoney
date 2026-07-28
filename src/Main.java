import controllers.ExpenseManager;
import enums.TransactionType;
import models.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public  class Main {
    public static void main(String[] args) {
        ExpenseManager manager = new ExpenseManager();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("[1] Thêm giao dịch mới");
            System.out.println("[2] Xóa giao dịch theo mã");
            System.out.println("[3] Xem tất cả lịch sử giao dịch");
            System.out.println("[4] Xem tổng số dư các ví");
            System.out.println("[5] Thống kê theo tháng/năm");
            System.out.println("[6] Thêm ví tiền mới");
            System.out.println("[7] Thêm danh mục mới");
            System.out.println("[0] Thoát chương trình");
            System.out.print("Mời bạn chọn chức năng (0-7): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addTransactionUI(manager, scanner);
                    break;
                case "2":
                    System.out.print("Nhập mã giao dịch muốn xóa:");
                    String id = scanner.nextLine();
                    manager.removeTransaction(id);
                    break;
                case "3":
                    manager.displayAllTransactions();
                    break;
                case "4":
                    System.out.printf("TỔNG SỐ DƯ HIỆN TẠI: %,.2f VND\n", manager.calculateTotalBalance());
                    break;
                case "5":
                    System.out.println("Bạn muốn thống kê theo:");
                    System.out.println("[1] Từng tháng");
                    System.out.println("[2] Cả năm");

                    String statChoice = scanner.nextLine();
                    if (statChoice.equals("1")) {
                        int month = 0;
                        int year = 0;
                        while (true) {
                            try {
                                System.out.print("Nhập tháng: ");
                                month = Integer.parseInt(scanner.nextLine());
                                System.out.print("Nhập năm: ");
                                year = Integer.parseInt(scanner.nextLine());
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Lỗi: Vui lòng chỉ nhập số cho tháng và năm! Hãy nhập lại.");
                            }
                            manager.monthlySummary(month, year);
                        }
                    } else if (statChoice.equals("2")) {
                        int year = 0;
                        while (true) {
                            try {
                                System.out.print("Nhập năm: ");
                                year = Integer.parseInt(scanner.nextLine());
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Lỗi: Vui lòng chỉ nhập số cho tháng và năm! Hãy nhập lại.");
                            }
                        }
                        manager.yearlySummary(year);
                    } else {
                        System.out.println("Lựa chọn không hợp lệ, vui lòng nhập lại!");
                    }
                    break;
                case "6":
                    System.out.print("Nhập tên ví tiền: ");
                    String wName = scanner.nextLine();
                    double wBalance = 0;
                    while (true) {
                        try {
                            System.out.printf("Nhập số dư ban đầu: ");
                            wBalance = Double.parseDouble(scanner.nextLine());
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Lỗi: Số dư phải là số! Hãy nhập lại.");
                        }
                    }
                    manager.addWallet(new CashWallet(wName, wBalance));
                    break;
                case "7":
                    System.out.print("Nhập tên danh mục: ");
                    String cName = scanner.nextLine();
                    System.out.print("Loại danh mục - Thu nhập (1) / Chi tiêu (2)?: ");
                    String cType = scanner.nextLine();
                    TransactionType type = cType.equals("1") ? TransactionType.INCOME : TransactionType.EXPENSE;
                    manager.addCategory(new Category(cName, type));
                    System.out.println("Đã thêm danh mục: " + cName);
                    break;
                case "0":
                    System.out.println("Cảm ơn bạn đã sử dụng phần mềm. Tạm biệt!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ. Vui lòng nhập lại!");
            }
        }
    }

    private static void addTransactionUI(ExpenseManager manager, Scanner scanner) {
        System.out.println("\nTHÊM GIAO DỊCH MỚI");
        System.out.print("Nhập mã giao dịch: ");
        String id = scanner.nextLine();

        double amount = 0;
        while(true) {
            try {
                System.out.print("Nhập số tiền: ");
                amount = Double.parseDouble(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Số tiền nhập vào phải là chữ số! Hãy nhập lại.");
            }
        }

        LocalDate date = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        while(true) {
            try {
                System.out.print("Ngày giao dịch: ");
                String dateStr = scanner.nextLine();
                date = LocalDate.parse(dateStr, formatter);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Lỗi: Định dạng ngày tháng không đúng (Cần nhập dd/MM/yyyy)! Hãy nhập lại.");
            }
        }

        System.out.print("Ghi chú: ");
        String note = scanner.nextLine();

        Wallet wallet = null;
        while(true) {
            System.out.print("Nhập tên ví sử dụng: ");
            String walletName = scanner.nextLine();
            if (walletName.equalsIgnoreCase("huy")) return;
            wallet = manager.getWalletByName(walletName);
            if (wallet != null) break;
            System.out.println("Lỗi: Không tìm thấy ví '" + walletName + "'. Vui lòng thử lại!");
        }

        Category category = null;
        while(true) {
            System.out.print("Nhập tên danh mục: ");
            String catName = scanner.nextLine();
            if (catName.equalsIgnoreCase("huy")) return;
            category = manager.getCategoryByName(catName);
            if (category != null) break;
            System.out.println("Lỗi: Không tìm thấy danh mục '" + catName + "'. Vui lòng thử lại!");
        }

        Transaction t = null;
        while (true) {
            System.out.print("Đây là Thu nhập (1) / Chi tiêu (2)?: ");
            String type = scanner.nextLine();
            if (type.equalsIgnoreCase("huy")) return;

            if (type.equals("1")) {
                System.out.print("Nguồn thu (VD: Lương công ty, Tiền thưởng): ");
                String source = scanner.nextLine();
                t = new Income(id, amount, date, category, note, wallet, source);
                break;
            } else if (type.equals("2")) {
                System.out.print("Phương thức thanh toán (VD: Tiền mặt, Chuyển khoản): ");
                String paymentMethod = scanner.nextLine();
                t = new Expense(id, amount, note, date, category, wallet, paymentMethod);
                break;
            } else {
                System.out.println("Lựa chọn không hợp lệ.");
            }
        }
        manager.addTransaction(t);
    }
}