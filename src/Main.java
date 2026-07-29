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
            System.out.println("[10] Tìm kiếm giao dịch");
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
                case "10":
                    searchTransactionUI(manager, scanner);
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
        System.out.println("\n--- THÊM GIAO DỊCH MỚI ---");

        double amount = 0;
        LocalDate date = null;
        String note = "";
        Wallet wallet = null;
        Category category = null;

        String source = "";
        String paymentMethod = "";
        enums.Period periodEnum = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int step = 1;

        // Tổng cộng có tối đa 8 bước
        // Nếu step lùi về 0 thì sẽ quay về menu
        while (step > 0 && step <= 8) {
            System.out.println("-1: Quay lại | 0: Hủy");
            switch (step) {
                case 1: // BƯỚC 1: NHẬP SỐ TIỀN
                    System.out.print("1. Nhập số tiền: ");
                    String inputAmount = scanner.nextLine();

                    if (inputAmount.equals("0")) return;
                    if (inputAmount.equals("-1")) {
                        step--;
                        break;
                    }

                    try {
                        amount = Double.parseDouble(inputAmount);
                        step++;
                    } catch (NumberFormatException e) {
                        System.out.println("Lỗi: Số tiền nhập vào phải là chữ số!");
                    }
                    break;

                case 2: // BƯỚC 2: NGÀY GIAO DỊCH
                    System.out.print("2. Ngày giao dịch (dd/MM/yyyy): ");
                    String dateStr = scanner.nextLine();

                    if (dateStr.equals("0")) return;
                    if (dateStr.equals("-1")) {
                        step--;
                        break;
                    }

                    try {
                        date = LocalDate.parse(dateStr, formatter);
                        step++;
                    } catch (DateTimeParseException e) {
                        System.out.println("Lỗi: Định dạng ngày tháng không đúng (Cần dd/MM/yyyy)!");
                    }
                    break;

                case 3: // BƯỚC 3: GHI CHÚ
                    System.out.print("3. Ghi chú: ");
                    String inputNote = scanner.nextLine();

                    if (inputNote.equals("0")) return;
                    if (inputNote.equals("-1")) {
                        step--;
                        break;
                    }

                    note = inputNote;
                    step++;
                    break;

                case 4: // BƯỚC 4: CHỌN VÍ
                    System.out.print("4. Nhập tên ví sử dụng: ");
                    String walletName = scanner.nextLine();

                    if (walletName.equals("0")) return;
                    if (walletName.equals("-1")) {
                        step--;
                        break;
                    }

                    wallet = manager.getWalletByName(walletName);
                    if (wallet != null) {
                        step++;
                    } else {
                        System.out.println("Lỗi: Không tìm thấy ví '" + walletName + "'.");
                    }
                    break;

                case 5: // BƯỚC 5: CHỌN DANH MỤC
                    System.out.print("5. Nhập tên danh mục: ");
                    String catName = scanner.nextLine();

                    if (catName.equals("0")) return;
                    if (catName.equals("-1")) {
                        step--;
                        break;
                    }

                    category = manager.getCategoryByName(catName);
                    if (category != null) {
                        step++;
                    } else {
                        System.out.println("Lỗi: Không tìm thấy danh mục '" + catName + "'.");
                    }
                    break;

                case 6: // BƯỚC 6: CHI TIẾT DỰA TRÊN LOẠI DANH MỤC
                    if (category.getType() == TransactionType.INCOME) {
                        System.out.print("6. Nguồn thu: ");
                        source = scanner.nextLine();

                        if (source.equals("0")) return;
                        if (source.equals("-1")) {
                            step--;
                            break;
                        }

                        // Sinh mã và lưu Thu nhập
                        String autoId = generateTransactionId("THU", date, category);
                        manager.addTransaction(new Income(autoId, amount, date, category, note, wallet, source));
                        System.out.println("=> ĐÃ THÊM GIAO DỊCH THU NHẬP THÀNH CÔNG! (Mã GD: " + autoId + ")\n");
                        step = 9;
                    } else {
                        System.out.print("6. Phương thức thanh toán: ");
                        paymentMethod = scanner.nextLine();

                        if (paymentMethod.equals("0")) return;
                        if (paymentMethod.equals("-1")) {
                            step--;
                            break;
                        }

                        step++;
                    }
                    break;

                case 7: // BƯỚC 7: XÁC NHẬN CHI TIÊU ĐỊNH KỲ
                    System.out.print("7. Đây có phải chi tiêu định kỳ không? (y/n): ");
                    String isRecurring = scanner.nextLine();

                    if (isRecurring.equals("0")) return;
                    if (isRecurring.equals("-1")) {
                        step--;
                        break;
                    }

                    if (isRecurring.equalsIgnoreCase("n")) {
                        // Sinh mã và lưu Chi tiêu thông thường
                        String autoId = generateTransactionId("CHI", date, category);
                        manager.addTransaction(new Expense(autoId, amount, note, date, category, wallet, paymentMethod));
                        System.out.println("=> ĐÃ THÊM GIAO DỊCH CHI TIÊU THÀNH CÔNG! (Mã GD: " + autoId + ")\n");
                        step = 9; // Thoát vòng lặp
                    } else if (isRecurring.equalsIgnoreCase("y")) {
                        step++;
                    } else {
                        System.out.println("Lỗi: Vui lòng nhập 'y' hoặc 'n'.");
                    }
                    break;

                case 8: // BƯỚC 8: CHỌN CHU KỲ (Chỉ dành cho Chi tiêu định kỳ)
                    System.out.println("8. Chọn chu kỳ lặp lại:");
                    System.out.println("[1] Hàng ngày");
                    System.out.println("[2] Hàng tuần");
                    System.out.println("[3] Hàng tháng");
                    System.out.println("[4] Hàng năm");
                    System.out.print("Lựa chọn: ");
                    String pChoice = scanner.nextLine();

                    if (pChoice.equals("0")) return;
                    if (pChoice.equals("-1")) {
                        step--;
                        break;
                    }

                    switch (pChoice) {
                        case "1":
                            periodEnum = enums.Period.DAILY;
                            break;
                        case "2":
                            periodEnum = enums.Period.WEEKLY;
                            break;
                        case "3":
                            periodEnum = enums.Period.MONTHLY;
                            break;
                        case "4":
                            periodEnum = enums.Period.YEARLY;
                            break;
                        default:
                            System.out.println("Lỗi: Lựa chọn không hợp lệ, vui lòng chọn lại!");
                            periodEnum = null;
                            break;
                    }

                    if (periodEnum != null) {
                        // Sinh mã và lưu Chi tiêu định kỳ
                        String autoId = generateTransactionId("CHI", date, category);
                        manager.addTransaction(new RecurringExpense(autoId, amount, note, date, category, wallet, paymentMethod, periodEnum));
                        System.out.println("=> ĐÃ THÊM GIAO DỊCH CHI TIÊU ĐỊNH KỲ THÀNH CÔNG! (Mã GD: " + autoId + ")\n");
                        step = 9; // Thoát vòng lặp
                    }
                    break;
            }
        }

        // In thông báo khi step lùi về 0 và kết thúc vòng lặp
        if (step == 0) {
            System.out.println("Đã hủy quá trình thêm mới, quay lại Menu chính...");
        }
    }

    // Hàm gen ID tự động
    private static String generateTransactionId(String typePrefix, LocalDate date, Category category) {
        String datePart = date.format(DateTimeFormatter.ofPattern("ddMM"));
        String catName = category.getName().replaceAll("\\s+", "").toUpperCase();
        String catPart = catName.length() >= 3 ? catName.substring(0, 3) : catName;
        int randomPart = (int) (Math.random() * 9000) + 1000;

        return String.format("%s-%s-%s-%d", typePrefix, datePart, catPart, randomPart);
    }

    private static void searchTransactionUI(ExpenseManager manager, Scanner scanner) {
        System.out.println("\nTÌM KIẾM GIAO DỊCH");
        System.out.println("[1] Theo Mã giao dịch (ID)");
        System.out.println("[2] Theo Tên danh mục");
        System.out.println("[3] Theo Ngày cụ thể");
        System.out.println("[4] Theo Tháng / Năm");
        System.out.println("[5] Theo Khoảng số tiền");
        System.out.println("[0] Quay lại Menu chính");
        System.out.print("Mời bạn chọn tiêu chí (0-5): ");

        String searchChoice = scanner.nextLine();
        switch (searchChoice) {
            case "1":
                System.out.print("Nhập ID cần tìm: ");
                manager.searchById(scanner.nextLine());
                break;
            case "2":
                System.out.print("Nhập từ khóa Tên danh mục: ");
                manager.searchByCategory(scanner.nextLine());
                break;
            case "3":
                try {
                    System.out.print("Nhập ngày cần tìm (dd/MM/yyyy): ");
                    java.time.LocalDate d = java.time.LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    manager.searchByDate(d);
                } catch (DateTimeParseException e) {
                    System.out.println("Lỗi: Định dạng ngày tháng không đúng (Cần dd/MM/yyyy)!");
                }
                break;
            case "4":
                try {
                    System.out.print("Nhập tháng (Gõ 0 nếu muốn tìm toàn bộ của 1 năm): ");
                    int m = Integer.parseInt(scanner.nextLine());
                    System.out.print("Nhập năm: ");
                    int y = Integer.parseInt(scanner.nextLine());
                    manager.searchByMonthYear(m, y);
                } catch (NumberFormatException e) {
                    System.out.println("Lỗi: Bạn phải nhập số!");
                }
                break;
            case "5":
                try {
                    System.out.print("Số tiền TỐI THIỂU: ");
                    double min = Double.parseDouble(scanner.nextLine());
                    System.out.print("Số tiền TỐI ĐA: ");
                    double max = Double.parseDouble(scanner.nextLine());
                    manager.searchByAmountRange(min, max);
                } catch (NumberFormatException e) {
                    System.out.println("Lỗi: Số tiền không hợp lệ!");
                }
                break;
            case "0":
                System.out.println("Đã quay lại Menu chính.");
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ!");
        }
    }
}