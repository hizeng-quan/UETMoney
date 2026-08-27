package controllers;

import enums.Period;
import enums.TransactionType;
import exception.InsufficientBalanceException;
import models.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class UIManager {
    private ExpenseManager manager;
    private Scanner scanner;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public UIManager(ExpenseManager manager, Scanner scanner) {
        this.manager = manager;
        this.scanner = scanner;
    }

    // 1. CÁC HÀM TIỆN ÍCH DÙNG CHUNG CHO TOÀN BỘ GIAO DIỆN

    // Exception tùy chỉnh dùng để điều hướng
    static class NavException extends Exception {
        String action;
        public NavException(String action) { this.action = action; }
    }

    // Hàm lấy input chung, tự động bắt sự kiện -1 (Quay lại) và 0 (Hủy)
    private String getInput() throws NavException {
        String input = scanner.nextLine().trim();
        if (input.equals("0")) throw new NavException("CANCEL");
        if (input.equals("-1")) throw new NavException("BACK");
        return input;
    }

    // 2. CÁC HÀM GIAO DIỆN CHI TIẾT

    // CASE 1: THÊM GIAO DỊCH
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

                    case 6: // Dựa vào Loại danh mục để chia nhánh
                        if (category.getType() == TransactionType.INCOME) {
                            System.out.print("6. Nguồn thu: ");
                            source = getInput();

                            String autoId = Transaction.generateId(category, date);
                            Transaction income = new Income(autoId, amount, date, category, note, wallet, source);
                            manager.addTransaction(income);

                            System.out.println("=> ĐÃ THÊM THU NHẬP THÀNH CÔNG! (Mã GD: " + autoId + ")");
                            step = 9; // Hoàn thành
                        } else {
                            System.out.print("6. Phương thức thanh toán: ");
                            paymentMethod = getInput();
                            step++;
                        }
                        break;

                    case 7: // Chỉ dành cho Chi tiêu
                        System.out.print("7. Đây có phải chi tiêu định kỳ không? (y/n): ");
                        String isRecurring = getInput();

                        if (isRecurring.equalsIgnoreCase("n")) {
                            String autoId = Transaction.generateId(category, date);
                            Transaction expense = new Expense(autoId, amount, note, date, category, wallet, paymentMethod);

                            manager.addTransaction(expense); // Có thể quăng InsufficientBalanceException
                            System.out.println("=> ĐÃ THÊM CHI TIÊU THÀNH CÔNG! (Mã GD: " + autoId + ")");
                            step = 9;
                        } else if (isRecurring.equalsIgnoreCase("y")) {
                            step++;
                        } else {
                            System.out.println("Lỗi: Vui lòng nhập 'y' hoặc 'n'.");
                        }
                        break;

                    case 8: // Chọn chu kỳ (Chi tiêu định kỳ)
                        System.out.print("8. Chọn chu kỳ lặp lại (1: Hàng ngày, 2: Hàng tuần, 3: Hàng tháng, 4: Hàng năm): ");
                        String pChoice = getInput();
                        Period periodEnum = switch (pChoice) {
                            case "1" -> Period.DAILY;
                            case "2" -> Period.WEEKLY;
                            case "3" -> Period.MONTHLY;
                            case "4" -> Period.YEARLY;
                            default -> null;
                        };

                        if (periodEnum != null) {
                            String autoId = Transaction.generateId(category, date);
                            Transaction recExpense = new RecurringExpense(autoId, amount, note, date, category, wallet, paymentMethod, periodEnum);

                            manager.addTransaction(recExpense); // Có thể quăng InsufficientBalanceException
                            System.out.println("=> ĐÃ THÊM CHI TIÊU ĐỊNH KỲ THÀNH CÔNG! (Mã GD: " + autoId + ")");
                            step = 9;
                        } else {
                            System.out.println("Lỗi: Lựa chọn chu kỳ không hợp lệ!");
                        }
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (InsufficientBalanceException e) {
                // Bắt lỗi không đủ số dư khi trừ tiền ví
                System.out.println("LỖI GIAO DỊCH: " + e.getMessage());
                System.out.println("Giao dịch bị hủy do số dư không đủ. Vui lòng kiểm tra lại!");
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



    // CASE 6: THÊM VÍ
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
                        if (wName.isEmpty()) {
                            System.out.println("Lỗi: Tên ví không được để trống!");
                            break;
                        }
                        step++;
                        break;

                    case 3:
                        System.out.print("3. Nhập số dư ban đầu: ");
                        wBalance = Double.parseDouble(getInput());
                        if (wBalance < 0) {
                            System.out.println("Lỗi: Số dư ban đầu không được âm!");
                            break;
                        }
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

    // CASE 5: THỐNG KÊ
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
                            System.out.print("2. Nhập tháng cần thống kê (1-12): ");
                            month = Integer.parseInt(getInput());
                            if (month >= 1 && month <= 12) step++;
                            else System.out.println("Lỗi: Tháng phải từ 1 đến 12.");
                        } else {
                            step++;
                        }
                        break;

                    case 3:
                        System.out.print("3. Nhập năm (YYYY): ");
                        year = Integer.parseInt(getInput());
                        if (year < 1900 || year > 2100) {
                            System.out.println("Lỗi: Năm không hợp lệ!");
                            break;
                        }

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
                System.out.println("Lỗi: Tháng/Năm phải là số nguyên hợp lệ!");
            }
        }
    }

    // CASE 2: XÓA GIAO DỊCH
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

    public void showAllTransactionsUI() {
        manager.displayAllTransactions();
        System.out.println("\nBấm phím Enter để tiếp tục...");
        scanner.nextLine();
    }

    public void showBalancesUI() {
        manager.displayWalletBalances();
        System.out.printf("TỔNG SỐ DƯ HIỆN TẠI: %,.2f VND\n", manager.calculateTotalBalance());
        System.out.println("\nBấm phím Enter để tiếp tục...");
        scanner.nextLine();
    }

    // CASE 7: THÊM DANH MỤC
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
                        if (cName.isEmpty()) {
                            System.out.println("Lỗi: Tên danh mục không được trống.");
                            break;
                        }
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
            try {
                manager.addCategory(new Category(cName, type));
                System.out.println("=> ĐÃ THÊM DANH MỤC THÀNH CÔNG: " + cName);
            } catch (IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }
    }

    // CẬP NHẬT CASE 8: ĐẶT / XEM / XÓA NGÂN SÁCH
    public void setBudgetUI() {
        System.out.println("\n--- QUẢN LÝ HẠN MỨC NGÂN SÁCH ---");
        int step = 1;
        String bChoice = "";

        while (step > 0 && step <= 2) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.println("1. Chọn chức năng ngân sách:");
                        System.out.println("[1] Đặt hạn mức mới\n[2] Xem danh sách toàn bộ hạn mức\n[3] Xóa hạn mức của danh mục");
                        System.out.print("Lựa chọn: ");
                        bChoice = getInput();
                        if (bChoice.matches("[123]")) step++;
                        else System.out.println("Lỗi: Chỉ chọn từ 1 đến 3.");
                        break;

                    case 2:
                        if (bChoice.equals("1")) {
                            System.out.print("Nhập tên danh mục để đặt ngân sách: ");
                            String bCatName = scanner.nextLine().trim();
                            Category bCat = manager.getCategoryByName(bCatName);
                            if (bCat == null) {
                                System.out.println("Lỗi: Không tìm thấy danh mục này.");
                                break;
                            }
                            System.out.print("Nhập số tiền giới hạn: ");
                            double limit = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Chu kỳ (1: Hàng ngày, 2: Hàng tuần, 3: Hàng tháng, 4: Hàng năm): ");
                            String pChoice = scanner.nextLine().trim();
                            enums.Period period = switch (pChoice) {
                                case "1" -> enums.Period.DAILY;
                                case "2" -> enums.Period.WEEKLY;
                                case "3" -> enums.Period.MONTHLY;
                                case "4" -> enums.Period.YEARLY;
                                default -> enums.Period.MONTHLY;
                            };
                            manager.setBudget(bCat, limit, period);
                            System.out.println("=> THIẾT LẬP NGÂN SÁCH THÀNH CÔNG!");
                        } else if (bChoice.equals("2")) {
                            manager.displayAllBudgets();
                        } else if (bChoice.equals("3")) {
                            System.out.print("Nhập tên danh mục cần xóa hạn mức: ");
                            String removeCat = scanner.nextLine().trim();
                            manager.removeBudget(removeCat);
                        }
                        step = 3;
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Số tiền không hợp lệ.");
            }
        }
        System.out.println("\nBấm phím Enter để tiếp tục...");
        scanner.nextLine();
    }

    // CASE 9: THỐNG KÊ NÂNG CAO
    public void advancedStatisticsUI() {
        System.out.println("\n--- THỐNG KÊ NÂNG CAO ---");
        int step = 1;
        String statChoice = "";

        while (step > 0 && step <= 2) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.println("1. Chọn tiêu chí thống kê nâng cao:");
                        System.out.println("[1] Chi tiêu chi tiết theo từng danh mục");
                        System.out.println("[2] Biến động chi tiêu theo các tháng trong năm");
                        System.out.println("[3] Xem khoản chi lớn nhất / nhỏ nhất");
                        System.out.println("[4] Báo cáo Top danh mục tiêu tốn tiền nhất");
                        System.out.print("Lựa chọn của bạn: ");
                        statChoice = getInput();
                        if (statChoice.matches("[1234]")) step++;
                        else System.out.println("Lỗi: Lựa chọn không hợp lệ!");
                        break;

                    case 2:
                        switch (statChoice) {
                            case "1":
                                manager.displayStatisticsByCategory();
                                step = 3;
                                break;
                            case "2":
                                System.out.print("Nhập năm cần xem biến động (YYYY): ");
                                int year = Integer.parseInt(getInput());
                                manager.displayMonthlyExpenseBreakdown(year);
                                step = 3;
                                break;
                            case "3":
                                manager.displayMinMaxExpense();
                                step = 3;
                                break;
                            case "4":
                                System.out.print("Nhập số lượng danh mục muốn xem (Top N): ");
                                int n = Integer.parseInt(getInput());
                                if (n <= 0) {
                                    System.out.println("Lỗi: Số lượng N phải lớn hơn 0!");
                                    break;
                                }
                                manager.displayTopExpensiveCategories(n);
                                step = 3;
                                break;
                        }
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Dữ liệu năm hoặc số lượng nhập vào phải là số nguyên!");
            }
        }

        if (step == 3) {
            System.out.println("\nBấm phím Enter để quay lại Menu chính...");
            scanner.nextLine();
        } else {
            System.out.println("Đã hủy thao tác thống kê nâng cao.");
        }
    }

    // CASE 10: TÌM KIẾM
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
                                if (sm < 0 || sm > 12) {
                                    System.out.println("Lỗi: Tháng không hợp lệ!");
                                    break;
                                }
                                System.out.print("2b. Nhập năm: ");
                                int sy = Integer.parseInt(getInput());
                                manager.searchByMonthYear(sm, sy);
                                break;
                            case "5":
                                System.out.print("2a. Số tiền TỐI THIỂU: ");
                                double min = Double.parseDouble(getInput());
                                System.out.print("2b. Số tiền TỐI ĐA: ");
                                double max = Double.parseDouble(getInput());
                                if (min > max) {
                                    System.out.println("Lỗi: Số tiền tối thiểu không được lớn hơn tối đa!");
                                    break;
                                }
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
                System.out.println("Lỗi: Dữ liệu nhập vào phải là chữ số!");
            } catch (DateTimeParseException e) {
                System.out.println("Lỗi: Định dạng ngày tháng không đúng (Cần dd/MM/yyyy)!");
            }
        }
        if (step == 0) System.out.println("Đã hủy thao tác tìm kiếm.");
    }

    // CASE 11: QUẢN LÝ GIAO DỊCH ĐỊNH KỲ VÀ KIỂM TRA ĐẾN HẠN
    public void checkAndManageRecurringUI() {
        System.out.println("\n--- QUẢN LÝ GIAO DỊCH ĐỊNH KỲ ---");
        java.util.List<RecurringExpense> allRecurring = manager.getRecurringExpenses();

        if (allRecurring.isEmpty()) {
            System.out.println("Chưa có giao dịch định kỳ nào trong hệ thống.");
            System.out.println("Gợi ý: Bạn có thể tạo giao dịch định kỳ bằng cách chọn Chức năng [1] -> Chọn Chi tiêu -> Chọn Có lặp lại.");
            System.out.println("Bấm phím Enter để quay lại Menu chính...");
            scanner.nextLine();
            return;
        }

        System.out.println("\n===== DANH SÁCH GIAO DỊCH ĐỊNH KỲ HIỆN TẠI =====");
        for (RecurringExpense re : allRecurring) {
            re.printInfo(); // Gọi hàm hiển thị thông tin của đối tượng định kỳ
        }

        int step = 1;
        while (step == 1) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            System.out.print("Bạn có muốn kiểm tra và xử lý các giao dịch ĐẾN HẠN hôm nay không? (y/n): ");
            try {
                String confirm = getInput();
                if (confirm.equalsIgnoreCase("n")) {
                    step = 0;
                    break;
                } else if (confirm.equalsIgnoreCase("y")) {
                    // Kiểm tra các giao dịch đến hạn từ manager
                    java.util.List<RecurringExpense> dueList = manager.checkDueRecurring();
                    if (dueList.isEmpty()) {
                        System.out.println("\n>>> Tuyệt vời! Không có giao dịch định kỳ nào đến hạn hôm nay.");
                    } else {
                        System.out.printf("\nCó %d giao dịch định kỳ ĐẾN HẠN hôm nay:\n", dueList.size());
                        for (RecurringExpense re : dueList) {
                            System.out.printf("  - %s: %,.0f VND (Chu kỳ: %s)\n",
                                    re.getNote(), re.getAmount(), re.getPeriod());

                            System.out.print("  Bạn có muốn hệ thống TỰ ĐỘNG TẠO giao dịch chi tiêu này không? (y/n): ");
                            String answer = scanner.nextLine().trim().toLowerCase();
                            if (answer.equals("y")) {
                                try {
                                    Expense newExpense = manager.createFromRecurring(re);
                                    manager.addTransaction(newExpense);
                                    System.out.println("  => Đã tự động tạo và trừ tiền! Mã GD mới: " + newExpense.getId());
                                } catch (exception.InsufficientBalanceException e) {
                                    System.out.println("  => LỖI: Ví không đủ số dư để thực hiện giao dịch tự động này!");
                                }
                            } else {
                                System.out.println("  => Đã bỏ qua giao dịch định kỳ này.");
                            }
                        }
                    }
                    step = 2; // Hoàn thành quy trình
                } else {
                    System.out.println("Lỗi: Vui lòng nhập 'y' hoặc 'n'.");
                }
            } catch (NavException e) {
                return; // Thoát ra menu chính
            }
        }

        System.out.println("\nBấm phím Enter để tiếp tục...");
        scanner.nextLine();
    }

    public void importExportUI() {
        System.out.println("\n--- NHẬP/XUẤT DỮ LIỆU GIAO DỊCH ---");
        System.out.println("1. Xuất dữ liệu sang CSV");
        System.out.println("2. Nhập dữ liệu từ CSV");
        System.out.println("3. Xuất dữ liệu sang JSON");
        System.out.println("4. Nhập dữ liệu từ JSON");
        System.out.println("0. Quay lại");
        System.out.print("Chọn chức năng (0-4): ");
        
        try {
            String choice = getInput();
            if (choice.equals("0")) return;
            
            System.out.print("Nhập đường dẫn file (VD: C:\\Users\\Quan\\Desktop\\data.json): ");
            String path = scanner.nextLine().trim();
            if (path.isEmpty()) return;
            
            if (choice.equals("1")) {
                storage.CsvStorage csvStorage = new storage.CsvStorage();
                csvStorage.saveTransactions(manager.getTransactions(), path);
                System.out.println("=> Đã xuất dữ liệu CSV thành công!");
            } else if (choice.equals("2")) {
                storage.CsvStorage csvStorage = new storage.CsvStorage();
                java.util.List<Transaction> imported = csvStorage.loadTransactions(path, manager.getCategories(), manager.getWallets());
                for (Transaction t : imported) manager.addTransaction(t);
                System.out.println("=> Đã nạp thành công " + imported.size() + " giao dịch từ CSV!");
            } else if (choice.equals("3")) {
                storage.JsonStorage jsonStorage = new storage.JsonStorage();
                jsonStorage.saveTransactions(manager.getTransactions(), path);
                System.out.println("=> Đã xuất dữ liệu JSON thành công!");
            } else if (choice.equals("4")) {
                storage.JsonStorage jsonStorage = new storage.JsonStorage();
                java.util.List<Transaction> imported = jsonStorage.loadTransactions(path, manager.getCategories(), manager.getWallets());
                for (Transaction t : imported) manager.addTransaction(t);
                System.out.println("=> Đã nạp thành công " + imported.size() + " giao dịch từ JSON!");
            } else {
                System.out.println("=> Lựa chọn không hợp lệ!");
            }
        } catch (NavException e) {
            return;
        } catch (Exception e) {
            System.out.println("=> Lỗi hệ thống: " + e.getMessage());
        }
    }
}