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
            System.out.println("\nPHẦN MỀM QUẢN LÝ CHI TIÊU CÁ NHÂN");
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
                    manager.displayWalletBalances();
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
                    System.out.println("Chọn loại ví muốn thêm:");
                    System.out.println("[1] Ví tiền mặt (Cash)");
                    System.out.println("[2] Tài khoản ngân hàng (Bank)");
                    System.out.println("[3] Ví điện tử (E-Wallet)");
                    String wChoice = scanner.nextLine();

                    System.out.print("Nhập tên ví: ");
                    String wName = scanner.nextLine();

                    double wBalance = 0;
                    while(true) {
                        try {
                            System.out.print("Nhập số dư ban đầu: ");
                            wBalance = Double.parseDouble(scanner.nextLine());
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Lỗi: Số dư phải là số! Hãy nhập lại.");
                        }
                    }

                    if (wChoice.equals("1")) {
                        manager.addWallet(new CashWallet(wName, wBalance));
                    } else if (wChoice.equals("2")) {
                        System.out.print("Nhập tên ngân hàng: ");
                        String bankName = scanner.nextLine();
                        System.out.print("Nhập số tài khoản: ");
                        String accNum = scanner.nextLine();
                        manager.addWallet(new BankAccount(wName, wBalance, bankName, accNum));
                    } else if (wChoice.equals("3")) {
                        System.out.print("Nhập tên ứng dụng cung cấp: ");
                        String provider = scanner.nextLine();
                        manager.addWallet(new EWallet(wName, wBalance, provider));
                    } else {
                        System.out.println("Lỗi: Lựa chọn không hợp lệ. Đã hủy thêm ví!");
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
                case "8":
                    System.out.print("Nhập tên danh mục để đặt ngân sách: ");
                    String bCatName = scanner.nextLine();
                    Category bCat = manager.getCategoryByName(bCatName);
                    if (bCat != null) {
                        try {
                            System.out.print("Nhập số tiền giới hạn: ");
                            double limit = Double.parseDouble(scanner.nextLine());

                            System.out.print("Chu kỳ (1: Hàng ngày, 2: Hàng tuần, 3: Hàng tháng, 4: Hàng năm): ");
                            String pChoice = scanner.nextLine();
                            enums.Period period = enums.Period.MONTHLY;
                            if (pChoice.equals("1")) period = enums.Period.DAILY;
                            else if (pChoice.equals("2")) period = enums.Period.WEEKLY;
                            else if (pChoice.equals("4")) period = enums.Period.YEARLY;

                            manager.setBudget(bCat, limit, period);
                        } catch (NumberFormatException e) {
                            System.out.println("Lỗi: Số tiền không hợp lệ.");
                        }
                    } else {
                        System.out.println("Lỗi: Không tìm thấy danh mục này.");
                    }
                    break;
                case "9":
                    try {
                        System.out.print("Nhập tháng cần xem chi tiết: ");
                        int m = Integer.parseInt(scanner.nextLine());
                        System.out.print("Nhập năm: ");
                        int y = Integer.parseInt(scanner.nextLine());
                        manager.advancedStatistics(m, y);
                    } catch (NumberFormatException e) {
                        System.out.println("Lỗi: Vui lòng chỉ nhập số cho tháng và năm!");
                    }
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
        if (category.getType() == TransactionType.INCOME) {
            System.out.print("Nguồn thu: ");
            String source = scanner.nextLine();
            t = new Income(id, amount, date, category, note, wallet, source);
        } else {
            System.out.print("Phương thức thanh toán: ");
            String paymentMethod = scanner.nextLine();

            System.out.print("Đây có phải chi tiêu định kỳ không? (y/n): ");
            String isRecurring = scanner.nextLine();

            if (isRecurring.equalsIgnoreCase("y")) {
                enums.Period periodEnum = null;
                while (periodEnum == null) {
                    System.out.println("Chọn chu kỳ lặp lại:");
                    System.out.println("[1] Hàng ngày");
                    System.out.println("[2] Hàng tuần");
                    System.out.println("[3] Hàng tháng");
                    System.out.println("[4] Hàng năm");
                    String pChoice = scanner.nextLine();

                    switch (pChoice) {
                        case "1": periodEnum = enums.Period.DAILY; break;
                        case "2": periodEnum = enums.Period.WEEKLY; break;
                        case "3": periodEnum = enums.Period.MONTHLY; break;
                        case "4": periodEnum = enums.Period.YEARLY; break;
                        default: System.out.println("Lỗi: Lựa chọn không hợp lệ, vui lòng chọn lại!");
                    }
                }
                t = new RecurringExpense(id, amount, note, date, category, wallet, paymentMethod, periodEnum);
            } else {
                t = new Expense(id, amount, note, date, category, wallet, paymentMethod);
            }
        }
        manager.addTransaction(t);
    }
}