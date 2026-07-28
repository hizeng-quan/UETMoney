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
    //thêm giao dịch
    private static void addTransactionUI(ExpenseManager manager, Scanner scanner) {
        System.out.println("\n--- THÊM GIAO DỊCH MỚI ---");

        // Thêm biến typeChoice để lưu lại lựa chọn Loại giao dịch ở Bước 1
        String typeChoice = "";
        double amount = 0;
        LocalDate date = null;
        String note = "";
        Wallet wallet = null;
        Category category = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int step = 1;

        // Tổng cộng có 7 bước
        // Nếu nhỏ hơn 1 thì sẽ quay về menu
        while (step > 0 && step <= 7) {
            System.out.println("-1: Quay lại \n 0: Hủy");
            switch (step) {
                case 1: // BƯỚC 1: CHỌN LOẠI GIAO DỊCH
                    System.out.print("1. Đây là Thu nhập (1) hay Chi tiêu (2)?: ");
                    String inputType = scanner.nextLine();

                    if (inputType.equals("0")) return;
                    if (inputType.equals("-1")) { step--; break; }

                    if (inputType.equals("1") || inputType.equals("2")) {
                        typeChoice = inputType;
                        step++;
                    } else {
                        System.out.println("Lỗi: Lựa chọn không hợp lệ. Vui lòng nhập 1 hoặc 2.");
                    }
                    break;

                case 2: // BƯỚC 2: NHẬP SỐ TIỀN
                    System.out.print("2. Nhập số tiền: ");
                    String inputAmount = scanner.nextLine();

                    if (inputAmount.equals("0")) return;
                    if (inputAmount.equals("-1")) { step--; break; }

                    try {
                        amount = Double.parseDouble(inputAmount);
                        step++;
                    } catch (NumberFormatException e) {
                        System.out.println("Lỗi: Số tiền nhập vào phải là chữ số!");
                    }
                    break;

                case 3: // BƯỚC 3: NGÀY GIAO DỊCH
                    System.out.print("3. Ngày giao dịch (dd/MM/yyyy): ");
                    String dateStr = scanner.nextLine();

                    if (dateStr.equals("0")) return;
                    if (dateStr.equals("-1")) { step--; break; }

                    try {
                        date = LocalDate.parse(dateStr, formatter);
                        step++;
                    } catch (DateTimeParseException e) {
                        System.out.println("Lỗi: Định dạng ngày tháng không đúng (Cần dd/MM/yyyy)!");
                    }
                    break;

                case 4: // BƯỚC 4: GHI CHÚ
                    System.out.print("4. Ghi chú: ");
                    String inputNote = scanner.nextLine();

                    if (inputNote.equals("0")) return;
                    if (inputNote.equals("-1")) { step--; break; }

                    note = inputNote;
                    step++;
                    break;

                case 5: // BƯỚC 5: CHỌN VÍ
                    System.out.print("5. Nhập tên ví sử dụng: ");
                    String walletName = scanner.nextLine();

                    if (walletName.equals("0")) return;
                    if (walletName.equals("-1")) { step--; break; }

                    wallet = manager.getWalletByName(walletName);
                    if (wallet != null) { step++; }
                    else { System.out.println("Lỗi: Không tìm thấy ví '" + walletName + "'."); }
                    break;

                case 6: // BƯỚC 6: CHỌN DANH MỤC
                    System.out.print("6. Nhập tên danh mục: ");
                    String catName = scanner.nextLine();

                    if (catName.equals("0")) return;
                    if (catName.equals("-1")) { step--; break; }

                    category = manager.getCategoryByName(catName);
                    if (category != null) { step++; }
                    else { System.out.println("Lỗi: Không tìm thấy danh mục '" + catName + "'."); }
                    break;

                case 7: // BƯỚC 7: CHI TIẾT + LƯU GIAO DỊCH
                    if (typeChoice.equals("1")) {
                        System.out.print("7. Nguồn thu: ");
                        String source = scanner.nextLine();

                        if (source.equals("0")) return;
                        if (source.equals("-1")) { step--; break; }

                        // Sinh mã và lưu Thu nhập
                        String autoId = generateTransactionId("THU", date, category);
                        manager.addTransaction(new Income(autoId, amount, date, category, note, wallet, source));
                        System.out.println("=> ĐÃ THÊM GIAO DỊCH THÀNH CÔNG! (Mã GD: " + autoId + ")\n");
                        step++;

                    } else if (typeChoice.equals("2")) {
                        System.out.print("7. Phương thức thanh toán: ");
                        String paymentMethod = scanner.nextLine();

                        if (paymentMethod.equals("0")) return;
                        if (paymentMethod.equals("-1")) { step--; break; }

                        // Sinh mã và lưu Chi tiêu
                        String autoId = generateTransactionId("CHI", date, category);
                        manager.addTransaction(new Expense(autoId, amount, note, date, category, wallet, paymentMethod));
                        System.out.println("=> ĐÃ THÊM GIAO DỊCH THÀNH CÔNG! (Mã GD: " + autoId + ")\n");
                        step++;
                    }
                    break;
            }
        }
        // In thông báo khi step lùi về 0 và kết thúc vòng lặp
        if (step == 0) {
            System.out.println("Đã hủy quá trình thêm mới, quay lại Menu chính...");
        }
    }
    // đây là hàm gen ID tự động
    private static String generateTransactionId(String typePrefix, LocalDate date, Category category) {
        String datePart = date.format(DateTimeFormatter.ofPattern("ddMM"));
        String catName = category.getName().replaceAll("\\s+", "").toUpperCase();
        String catPart = catName.length() >= 3 ? catName.substring(0, 3) : catName;
        int randomPart = (int) (Math.random() * 9000) + 1000;

        return String.format("%s-%s-%s-%d", typePrefix, datePart, catPart, randomPart);
    }
}

