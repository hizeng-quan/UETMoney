package ConsoleUI;

import controllers.ExpenseManager;
import enums.TransactionType;
import models.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Tầng hiển thị dòng lệnh (Console View).
 * Tách biệt khỏi logic nghiệp vụ (ExpenseManager) theo nguyên tắc OOP.
 */
public class UIManager {
    private ExpenseManager manager;
    private Scanner scanner;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public UIManager(ExpenseManager manager, Scanner scanner) {
        this.manager = manager;
        this.scanner = scanner;
    }

    // ======================== 1. HELPER / NAVIGATION ========================

    /**
     * Exception tùy chỉnh dùng để điều hướng quay lại / hủy trong các wizard.
     */
    static class NavException extends Exception {
        String action;
        public NavException(String action) { this.action = action; }
    }

    /**
     * Lấy input chung, tự động bắt -1 (Quay lại) và 0 (Hủy).
     */
    private String getInput() throws NavException {
        String input = scanner.nextLine().trim();
        if (input.equals("0")) throw new NavException("CANCEL");
        if (input.equals("-1")) throw new NavException("BACK");
        return input;
    }

    // ======================== 2. CASE 1: THÊM GIAO DỊCH ========================

    public void addTransactionUI() {
        System.out.println("\n--- THÊM GIAO DỊCH MỚI ---");

        double amount = 0;
        LocalDate date = null;
        String note = "", source = "", paymentMethod = "";
        Wallet wallet = null;
        Category category = null;

        int step = 1;

        while (step > 0 && step <= 8) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.print("1. Nhập số tiền: ");
                        amount = Double.parseDouble(getInput());
                        if (amount <= 0) {
                            System.out.println("Lỗi: Số tiền phải lớn hơn 0!");
                            break;
                        }
                        step++;
                        break;

                    case 2:
                        System.out.print("2. Ngày giao dịch (dd/MM/yyyy): ");
                        date = LocalDate.parse(getInput(), formatter);
                        step++;
                        break;

                    case 3:
                        System.out.print("3. Ghi chú: ");
                        note = getInput();
                        step++;
                        break;

                    case 4:
                        System.out.print("4. Nhập tên ví sử dụng: ");
                        String walletName = getInput();
                        wallet = manager.getWalletByName(walletName);
                        if (wallet != null) step++;
                        else System.out.println("Lỗi: Không tìm thấy ví '" + walletName + "'.");
                        break;

                    case 5:
                        System.out.print("5. Nhập tên danh mục: ");
                        String catName = getInput();
                        category = manager.getCategoryByName(catName);
                        if (category != null) step++;
                        else System.out.println("Lỗi: Không tìm thấy danh mục '" + catName + "'.");
                        break;

                    case 6:
                        if (category.getType() == TransactionType.INCOME) {
                            System.out.print("6. Nguồn thu: ");
                            source = getInput();

                            String autoId = generateTransactionId("THU", date, category);
                            manager.addTransaction(new Income(autoId, amount, date, category, note, wallet, source));
                            System.out.println("=> ĐÃ THÊM THU NHẬP THÀNH CÔNG! (Mã GD: " + autoId + ")");
                            step = 9;
                        } else {
                            System.out.print("6. Phương thức thanh toán: ");
                            paymentMethod = getInput();
                            step++;
                        }
                        break;

                    case 7:
                        System.out.print("7. Đây có phải chi tiêu định kỳ không? (y/n): ");
                        String isRecurring = getInput();

                        if (isRecurring.equalsIgnoreCase("n")) {
                            String autoId = generateTransactionId("CHI", date, category);
                            manager.addTransaction(new Expense(autoId, amount, note, date, category, wallet, paymentMethod));
                            System.out.println("=> ĐÃ THÊM CHI TIÊU THÀNH CÔNG! (Mã GD: " + autoId + ")");
                            step = 9;
                        } else if (isRecurring.equalsIgnoreCase("y")) {
                            step++;
                        } else {
                            System.out.println("Lỗi: Vui lòng nhập 'y' hoặc 'n'.");
                        }
                        break;

                    case 8:
                        System.out.print("8. Chọn chu kỳ lặp lại (1: Hàng ngày, 2: Hàng tuần, 3: Hàng tháng, 4: Hàng năm): ");
                        String pChoice = getInput();
                        enums.Period periodEnum = null;
                        switch (pChoice) {
                            case "1": periodEnum = enums.Period.DAILY; break;
                            case "2": periodEnum = enums.Period.WEEKLY; break;
                            case "3": periodEnum = enums.Period.MONTHLY; break;
                            case "4": periodEnum = enums.Period.YEARLY; break;
                        }

                        if (periodEnum != null) {
                            String autoId = generateTransactionId("CHI", date, category);
                            manager.addTransaction(new RecurringExpense(autoId, amount, note, date, category, wallet, paymentMethod, periodEnum));
                            System.out.println("=> ĐÃ THÊM CHI TIÊU ĐỊNH KỲ THÀNH CÔNG! (Mã GD: " + autoId + ")");
                            step = 9;
                        } else {
                            System.out.println("Lỗi: Lựa chọn không hợp lệ!");
                        }
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Dữ liệu nhập vào phải là chữ số hợp lệ!");
            } catch (DateTimeParseException e) {
                System.out.println("Lỗi: Định dạng ngày tháng không đúng (Cần dd/MM/yyyy)!");
            }
        }

        if (step == 0) {
            System.out.println("Đã quay lại Menu chính...");
        }
    }

    private static String generateTransactionId(String typePrefix, LocalDate date, Category category) {
        String datePart = date.format(DateTimeFormatter.ofPattern("ddMM"));
        String catName = category.getName().replaceAll("\\s+", "").toUpperCase();
        String catPart = catName.length() >= 3 ? catName.substring(0, 3) : catName;
        int randomPart = (int) (Math.random() * 9000) + 1000;

        return String.format("%s-%s-%s-%d", typePrefix, datePart, catPart, randomPart);
    }

    // ======================== 3. CASE 6: THÊM VÍ ========================

    public void addWalletUI() {
        System.out.println("\n--- THÊM VÍ TIỀN MỚI ---");
        int step = 1;
        String wChoice = "", wName = "", bankName = "", accNum = "", provider = "";
        double wBalance = 0;

        while (step > 0 && step <= 4) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.println("1. Chọn loại ví muốn thêm:");
                        System.out.println("[1] Ví tiền mặt (Cash) \n[2] Tài khoản ngân hàng (Bank) \n[3] Ví điện tử (E-Wallet)");
                        System.out.print("Lựa chọn của bạn: ");
                        wChoice = getInput();
                        if (wChoice.matches("[123]")) step++;
                        else System.out.println("Lỗi: Vui lòng nhập 1, 2 hoặc 3.");
                        break;

                    case 2:
                        System.out.print("2. Nhập tên ví: ");
                        wName = getInput();
                        step++;
                        break;

                    case 3:
                        System.out.print("3. Nhập số dư ban đầu: ");
                        wBalance = Double.parseDouble(getInput());
                        step++;
                        break;

                    case 4:
                        if (wChoice.equals("1")) {
                            manager.addWallet(new CashWallet(wName, wBalance));
                            System.out.println("=> ĐÃ THÊM VÍ TIỀN MẶT THÀNH CÔNG!");
                            step = 5;
                        } else if (wChoice.equals("2")) {
                            System.out.print("4a. Nhập tên ngân hàng: ");
                            bankName = getInput();
                            System.out.print("4b. Nhập số tài khoản: ");
                            accNum = getInput();

                            manager.addWallet(new BankAccount(wName, wBalance, bankName, accNum));
                            System.out.println("=> ĐÃ THÊM TÀI KHOẢN NGÂN HÀNG THÀNH CÔNG!");
                            step = 5;
                        } else if (wChoice.equals("3")) {
                            System.out.print("4. Nhập tên ứng dụng cung cấp (Momo, ZaloPay...): ");
                            provider = getInput();

                            manager.addWallet(new EWallet(wName, wBalance, provider));
                            System.out.println("=> ĐÃ THÊM VÍ ĐIỆN TỬ THÀNH CÔNG!");
                            step = 5;
                        }
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Định dạng số không hợp lệ!");
            }
        }
        if (step == 0) System.out.println("Đã hủy thêm ví mới.");
    }

    // ======================== 4. CASE 5: THỐNG KÊ ========================

    public void showStatisticsUI() {
        System.out.println("\n--- THỐNG KÊ CHI TIÊU ---");
        int step = 1;
        String statChoice = "";
        int month = 0, year = 0;

        while (step > 0 && step <= 3) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.println("1. Bạn muốn thống kê theo:\n[1] Từng tháng\n[2] Cả năm");
                        System.out.print("Lựa chọn: ");
                        statChoice = getInput();
                        if (statChoice.equals("1") || statChoice.equals("2")) step++;
                        else System.out.println("Lỗi: Chọn 1 hoặc 2.");
                        break;

                    case 2:
                        if (statChoice.equals("1")) {
                            System.out.print("2. Nhập tháng cần thống kê: ");
                            month = Integer.parseInt(getInput());
                            if (month >= 1 && month <= 12) step++;
                            else System.out.println("Lỗi: Tháng phải từ 1-12.");
                        } else {
                            step++;
                        }
                        break;

                    case 3:
                        System.out.print("3. Nhập năm (YYYY): ");
                        year = Integer.parseInt(getInput());

                        if (statChoice.equals("1")) {
                            manager.monthlySummary(month, year);
                        } else {
                            manager.yearlySummary(year);
                        }
                        step = 4;
                        System.out.println("Bấm phím Enter để quay lại Menu chính...");
                        scanner.nextLine();
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) {
                    step--;
                    if (step == 2 && statChoice.equals("2")) step--;
                }
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Tháng/Năm phải là số!");
            }
        }
    }

    // ======================== 5. CASE 2: XÓA GIAO DỊCH ========================

    public void removeTransactionUI() {
        System.out.println("\n--- XÓA GIAO DỊCH ---");
        System.out.println("0: Hủy");
        try {
            System.out.print("Nhập mã giao dịch muốn xóa: ");
            String id = getInput();
            manager.removeTransaction(id);
        } catch (NavException e) {
            System.out.println("Đã hủy thao tác xóa.");
        }
    }

    // ======================== 6. CASE 3, 4: HIỂN THỊ ========================

    public void showAllTransactionsUI() {
        manager.displayAllTransactions();
        System.out.println("\nBấm phím Enter để tiếp tục...");
        scanner.nextLine();
    }

    public void showBalancesUI() {
        manager.displayWalletBalances();
        System.out.printf("TỔNG SỐ DƯ HIỆN TẠI: %,.2f VND%n", manager.calculateTotalBalance());
        System.out.println("\nBấm phím Enter để tiếp tục...");
        scanner.nextLine();
    }

    // ======================== 7. CASE 7: THÊM DANH MỤC ========================

    public void addCategoryUI() {
        System.out.println("\n--- THÊM DANH MỤC MỚI ---");
        int step = 1;
        String cName = "";
        TransactionType type = null;

        while (step > 0 && step <= 2) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.print("1. Nhập tên danh mục: ");
                        cName = getInput();
                        step++;
                        break;

                    case 2:
                        System.out.print("2. Loại danh mục - Thu nhập (1) / Chi tiêu (2)?: ");
                        String cType = getInput();

                        if (cType.equals("1")) {
                            type = TransactionType.INCOME;
                            step++;
                        } else if (cType.equals("2")) {
                            type = TransactionType.EXPENSE;
                            step++;
                        } else {
                            System.out.println("Lỗi: Vui lòng nhập 1 hoặc 2.");
                        }
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            }
        }

        if (step == 0) {
            System.out.println("Đã hủy quá trình thêm danh mục.");
        } else {
            manager.addCategory(new Category(cName, type));
            System.out.println("=> ĐÃ THÊM DANH MỤC THÀNH CÔNG: " + cName);
        }
    }

    // ======================== 8. CASE 8: ĐẶT/KIỂM TRA HẠN MỨC ========================

    public void setBudgetUI() {
        System.out.println("\n--- ĐẶT HẠN MỨC NGÂN SÁCH ---");
        int step = 1;
        Category bCat = null;
        double limit = 0;
        enums.Period period = null;

        while (step > 0 && step <= 3) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.print("1. Nhập tên danh mục để đặt ngân sách: ");
                        String bCatName = getInput();
                        bCat = manager.getCategoryByName(bCatName);

                        if (bCat != null) step++;
                        else System.out.println("Lỗi: Không tìm thấy danh mục này. Vui lòng thử lại.");
                        break;

                    case 2:
                        System.out.print("2. Nhập số tiền giới hạn: ");
                        limit = Double.parseDouble(getInput());
                        step++;
                        break;

                    case 3:
                        System.out.print("3. Chu kỳ (1: Hàng ngày, 2: Hàng tuần, 3: Hàng tháng, 4: Hàng năm): ");
                        String pChoice = getInput();

                        switch (pChoice) {
                            case "1": period = enums.Period.DAILY; break;
                            case "2": period = enums.Period.WEEKLY; break;
                            case "3": period = enums.Period.MONTHLY; break;
                            case "4": period = enums.Period.YEARLY; break;
                            default: System.out.println("Lỗi: Lựa chọn không hợp lệ."); break;
                        }

                        if (period != null) step++;
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Số tiền nhập vào phải là chữ số hợp lệ.");
            }
        }

        if (step == 0) {
            System.out.println("Đã hủy thiết lập ngân sách.");
        } else {
            manager.setBudget(bCat, limit, period);
            System.out.println("=> ĐÃ THIẾT LẬP NGÂN SÁCH THÀNH CÔNG CHO DANH MỤC: " + bCat.getName());
        }
    }

    // ======================== 9. CASE 9: THỐNG KÊ NÂNG CAO ========================

    public void advancedStatisticsUI() {
        System.out.println("\n--- THỐNG KÊ NÂNG CAO ---");
        int step = 1;
        String advChoice = "";

        while (step > 0 && step <= 3) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.println("Chọn loại thống kê:");
                        System.out.println("[1] Chi tiêu theo danh mục (tháng cụ thể)");
                        System.out.println("[2] Chi tiêu theo tháng (cả năm)");
                        System.out.println("[3] Báo cáo chi tiết (Max/Min, Top danh mục)");
                        System.out.print("Lựa chọn: ");
                        advChoice = getInput();
                        if (advChoice.matches("[123]")) step++;
                        else System.out.println("Lỗi: Vui lòng chọn 1, 2 hoặc 3.");
                        break;

                    case 2:
                        if (advChoice.equals("2")) {
                            // Chỉ cần nhập năm
                            System.out.print("Nhập năm (YYYY): ");
                            int y = Integer.parseInt(getInput());
                            manager.displayExpenseByMonth(y);
                            step = 4; // Hoàn thành
                        } else {
                            // Cần nhập tháng
                            System.out.print("Nhập tháng (1-12): ");
                            int m = Integer.parseInt(getInput());
                            if (m >= 1 && m <= 12) {
                                // Lưu tháng tạm
                                System.out.print("Nhập năm (YYYY): ");
                                int yr = Integer.parseInt(getInput());

                                if (advChoice.equals("1")) {
                                    manager.displayStatisticsByCategory(m, yr);
                                } else {
                                    manager.advancedStatistics(m, yr);
                                }
                                step = 4;
                            } else {
                                System.out.println("Lỗi: Tháng phải từ 1 đến 12.");
                            }
                        }
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Vui lòng chỉ nhập chữ số nguyên.");
            }
        }

        if (step >= 4) {
            System.out.println("\nBấm phím Enter để quay lại Menu chính...");
            scanner.nextLine();
        } else if (step == 0) {
            System.out.println("Đã hủy thao tác thống kê nâng cao.");
        }
    }

    // ======================== 10. CASE 10: TÌM KIẾM ========================

    public void searchTransactionUI() {
        System.out.println("\n--- TÌM KIẾM GIAO DỊCH ---");
        int step = 1;
        String searchChoice = "";

        while (step > 0 && step <= 2) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.println("1. Chọn tiêu chí tìm kiếm:");
                        System.out.println("[1] Theo Mã ID \n[2] Theo Tên danh mục \n[3] Theo Ngày cụ thể \n[4] Theo Tháng/Năm \n[5] Theo Khoảng số tiền");
                        System.out.print("Lựa chọn: ");
                        searchChoice = getInput();
                        if (searchChoice.matches("[12345]")) step++;
                        else System.out.println("Lỗi: Lựa chọn không hợp lệ!");
                        break;

                    case 2:
                        switch (searchChoice) {
                            case "1":
                                System.out.print("2. Nhập ID cần tìm: ");
                                manager.searchById(getInput());
                                break;
                            case "2":
                                System.out.print("2. Nhập từ khóa Tên danh mục: ");
                                manager.searchByCategory(getInput());
                                break;
                            case "3":
                                System.out.print("2. Nhập ngày cần tìm (dd/MM/yyyy): ");
                                manager.searchByDate(LocalDate.parse(getInput(), formatter));
                                break;
                            case "4":
                                System.out.print("2a. Nhập tháng (Gõ 0 để tìm cả năm): ");
                                int sm = Integer.parseInt(getInput());
                                System.out.print("2b. Nhập năm: ");
                                int sy = Integer.parseInt(getInput());
                                manager.searchByMonthYear(sm, sy);
                                break;
                            case "5":
                                System.out.print("2a. Số tiền TỐI THIỂU: ");
                                double min = Double.parseDouble(getInput());
                                System.out.print("2b. Số tiền TỐI ĐA: ");
                                double max = Double.parseDouble(getInput());
                                manager.searchByAmountRange(min, max);
                                break;
                        }
                        step = 3; // Hoàn thành
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Bạn phải nhập số!");
            } catch (DateTimeParseException e) {
                System.out.println("Lỗi: Định dạng ngày tháng không đúng (Cần dd/MM/yyyy)!");
            }
        }
        if (step == 0) System.out.println("Đã hủy thao tác tìm kiếm.");
    }

    // ======================== 11. CASE 11: QUẢN LÝ GIAO DỊCH ĐỊNH KỲ ========================

    /**
     * Menu quản lý giao dịch định kỳ (Recurring Transactions).
     */
    public void recurringTransactionUI() {
        System.out.println("\n--- QUẢN LÝ GIAO DỊCH ĐỊNH KỲ ---");
        int step = 1;

        while (step > 0 && step <= 2) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.println("[1] Xem tất cả giao dịch định kỳ");
                        System.out.println("[2] Xem các giao dịch SẮP ĐẾN HẠN");
                        System.out.println("[3] Xử lý giao dịch đến hạn (tạo giao dịch mới)");
                        System.out.print("Lựa chọn: ");
                        String rChoice = getInput();

                        switch (rChoice) {
                            case "1":
                                displayAllRecurring();
                                break;
                            case "2":
                                displayDueRecurring();
                                break;
                            case "3":
                                processRecurringUI();
                                break;
                            default:
                                System.out.println("Lỗi: Lựa chọn không hợp lệ!");
                                continue;
                        }
                        step = 3; // Hoàn thành
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            }
        }
        if (step == 0) System.out.println("Đã quay lại Menu chính.");
    }

    /**
     * Hiển thị tất cả giao dịch định kỳ.
     */
    private void displayAllRecurring() {
        List<RecurringExpense> recurring = manager.getRecurringExpenses();
        if (recurring.isEmpty()) {
            System.out.println("\nChưa có giao dịch định kỳ nào.");
            return;
        }

        System.out.println("\n DANH SÁCH GIAO DỊCH ĐỊNH KỲ (" + recurring.size() + " giao dịch)");
        for (RecurringExpense re : recurring) {
            re.printInfo();
            System.out.printf("   Trạng thái: %s%n", re.isDue() ? "ĐÃ ĐẾN HẠN" : "Chưa đến hạn");
            System.out.println("---");
        }

        System.out.println("Bấm Enter để tiếp tục...");
        scanner.nextLine();
    }

    /**
     * Hiển thị các giao dịch định kỳ đã đến hạn.
     */
    private void displayDueRecurring() {
        List<RecurringExpense> dueList = manager.getDueRecurringExpenses();
        if (dueList.isEmpty()) {
            System.out.println("\n Không có giao dịch định kỳ nào đến hạn.");
            return;
        }

        System.out.println("\n CÁC GIAO DỊCH ĐỊNH KỲ ĐẾN HẠN (" + dueList.size() + " giao dịch)");
        for (RecurringExpense re : dueList) {
            re.printInfo();
            System.out.println("---");
        }

        System.out.println("Bấm Enter để tiếp tục...");
        scanner.nextLine();
    }

    /**
     * Xử lý giao dịch đến hạn — Tùy chọn B: hỏi người dùng xác nhận từng giao dịch.
     */
    private void processRecurringUI() {
        List<RecurringExpense> dueList = manager.getDueRecurringExpenses();
        if (dueList.isEmpty()) {
            System.out.println("\n✓ Không có giao dịch định kỳ nào cần xử lý.");
            return;
        }

        System.out.println("\n CÁC GIAO DỊCH ĐẾN HẠN CẦN XỬ LÝ:");

        int processed = 0;
        for (RecurringExpense re : dueList) {
            re.printInfo();
            System.out.print("Bạn có muốn tạo giao dịch cho kỳ này không? (y/n): ");
            String confirm = scanner.nextLine().trim();

            if (confirm.equalsIgnoreCase("y")) {
                Expense newTx = manager.generateRecurringTransaction(re);
                if (newTx != null) {
                    System.out.println("Đã tạo giao dịch: " + newTx.getId());
                    processed++;
                }
            } else {
                System.out.println("Bỏ qua giao dịch " + re.getId());
            }
            System.out.println("---");
        }

        System.out.printf("%nĐã xử lý %d/%d giao dịch định kỳ.%n", processed, dueList.size());
    }

    /**
     * Kiểm tra và nhắc nhở giao dịch định kỳ khi khởi động (Tùy chọn B).
     * Gọi từ Main khi chương trình bắt đầu.
     */
    public void checkRecurringOnStartup() {
        List<RecurringExpense> dueList = manager.getDueRecurringExpenses();
        if (dueList.isEmpty()) return;

        System.out.println(" NHẮC NHỞ: Có " + dueList.size() + " giao dịch định kỳ ĐÃ ĐẾN HẠN!");
        for (RecurringExpense re : dueList) {
            System.out.printf("  • %s | %s | %,.0f VND | Hạn: %s%n",
                    re.getId(), re.getCategory().getName(), re.getAmount(), re.nextDueDate());
        }
        System.out.println(" Vào mục [11] Quản lý giao dịch định kỳ để xử lý.");
    }
}
