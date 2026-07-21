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
                    try {
                        System.out.println("Bạn muốn thống kê theo:");
                        System.out.println("[1] Từng tháng");
                        System.out.println("[2] Cả năm");

                        String statChoice = scanner.nextLine();
                        if (statChoice.equals("1")) {
                            System.out.print("Nhập tháng: ");
                            int month = Integer.parseInt(scanner.nextLine());
                            System.out.print("Nhập năm: ");
                            int year = Integer.parseInt(scanner.nextLine());
                            manager.monthlySummary(month, year);
                        } else if (statChoice.equals("2")) {
                            System.out.print("Nhập năm: ");
                            int year = Integer.parseInt(scanner.nextLine());
                            manager.yearlySummary(year);
                        } else {
                            System.out.println("Lựa chọn không hợp lệ, vui lòng nhập lại!");
                        }
                    }   catch (NumberFormatException e) {
                        System.out.println("Lỗi: Vui lòng chỉ nhập số cho tháng và năm!");
                    }
                    break;
                case "6":
                    try {
                        System.out.print("Nhập tên ví tiền: ");
                        String wName = scanner.nextLine();
                        System.out.printf("Nhập số dư ban đầu: ");
                        double wBalance = Double.parseDouble(scanner.nextLine());
                        manager.addWallet(new CashWallet(wName, wBalance));
                    } catch (NumberFormatException e) {
                        System.out.println("Lỗi: Số dư phải là số!");
                    }
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
        try {
            System.out.println("\nTHÊM GIAO DỊCH MỚI");
            System.out.print("Nhập mã giao dịch: ");
            String id = scanner.nextLine();

            System.out.print("Nhập số tiền: ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("Ngày giao dịch: ");
            String dateStr = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(dateStr, formatter);

            System.out.print("Ghi chú: ");
            String note = scanner.nextLine();

            System.out.print("Nhập tên ví sử dụng: ");
            String walletName = scanner.nextLine();
            Wallet wallet = manager.getWalletByName(walletName);
            if (wallet == null) {
                System.out.println("Lỗi: Không tìm thấy ví '" + walletName + "'!");
                return;
            }

            System.out.print("Nhập tên danh mục: ");
            String catName = scanner.nextLine();
            Category category = manager.getCategoryByName(catName);
            if (category == null) {
                System.out.println("Lỗi: Không tìm thấy danh mục '" + catName + "'!");
                return;
            }

            System.out.println("Đây là Thu nhập (1) hay Chi tiêu (2)?");
            String type = scanner.nextLine();

            Transaction t;
            if (type.equals("1")) {
                System.out.print("Nguồn thu: ");
                String source = scanner.nextLine();
                t = new Income(id, amount, date, category, note, wallet, source);
            } else if (type.equals("2")) {
                System.out.print("Phương thức thanh toán: ");
                String paymentMethod = scanner.nextLine();
                t = new Expense(id, amount, note, date, category, wallet, paymentMethod);
            } else {
                System.out.println("Lỗi: Loại giao dịch không hợp lệ!");
                return;
            }

            manager.addTransaction(t);
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Số tiền nhập vào không hợp lệ (Phải là chữ số)!");
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi: Định dạng ngày tháng không đúng (Cần nhập dd/MM/yyyy)!");
        }
    }
}