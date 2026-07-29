package ConsoleUI;

import controllers.ExpenseManager;
import enums.TransactionType;
import models.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class UIManager {
    private ExpenseManager manager;
    private Scanner scanner;

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
        // ... Code giống hệt phần tôi vừa sửa cho bạn ...
        // Lưu ý: Đổi getInput(scanner) thành getInput()
        System.out.println("\n--- THÊM GIAO DỊCH MỚI ---");

        double amount = 0;
        LocalDate date = null;
        String note = "", source = "", paymentMethod = "";
        Wallet wallet = null;
        Category category = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int step = 1;

        while (step > 0 && step <= 8) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.print("1. Nhập số tiền: ");
                        amount = Double.parseDouble(getInput()); // Vừa lấy input, vừa parse, vừa check -1/0
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

                            String autoId = generateTransactionId("THU", date, category);
                            manager.addTransaction(new Income(autoId, amount, date, category, note, wallet, source));
                            System.out.println("=> ĐÃ THÊM THU NHẬP THÀNH CÔNG! (Mã GD: " + autoId + ")");
                            step = 9; // Hoàn thành, thoát vòng lặp
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

                    case 8: // Chọn chu kỳ (Chi tiêu định kỳ)
                        System.out.print("8. Chọn chu kỳ lặp lại (1: Hàng ngày, 2: Hàng tuần, 3: Hàng tháng, 4: Hàng năm): ");
                        String pChoice = getInput();
                        enums.Period periodEnum = switch (pChoice) {
                            case "1" -> enums.Period.DAILY;
                            case "2" -> enums.Period.WEEKLY;
                            case "3" -> enums.Period.MONTHLY;
                            case "4" -> enums.Period.YEARLY;
                            default -> null;
                        };

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
                // XỬ LÝ QUAY LẠI / HỦY GỘP CHUNG TẠI ĐÂY
                if (e.action.equals("CANCEL")) return; // Hủy hoàn toàn
                if (e.action.equals("BACK")) step--;   // Lùi lại 1 bước

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
    // CASE 6: THÊM VÍ MỚI (Làm theo cấu trúc Step)
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
                            step = 5; // Thoát
                        } else if (wChoice.equals("2")) {
                            System.out.print("4a. Nhập tên ngân hàng: ");
                            bankName = getInput();
                            System.out.print("4b. Nhập số tài khoản: ");
                            accNum = getInput(); // Nếu gõ -1 ở đây, nó ném Exception và tự trừ step về 3

                            manager.addWallet(new BankAccount(wName, wBalance, bankName, accNum));
                            System.out.println("=> ĐĐÃ THÊM TÀI KHOẢN NGÂN HÀNG THÀNH CÔNG!");
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

    // CASE 5: THỐNG KÊ (Làm theo cấu trúc Step)
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
                            // Nếu thống kê theo năm thì nhảy qua bước nhập tháng, đến thẳng bước nhập năm
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
                        step = 4; // Kết thúc
                        System.out.println("Bấm phím Enter để quay lại Menu chính...");
                        scanner.nextLine();
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) {
                    step--;
                    // Xử lý nhỏ: Nếu chọn Cả Năm (bước 2 bị bỏ qua), thì khi lùi bước 3 phải lùi thẳng về bước 1
                    if (step == 2 && statChoice.equals("2")) step--;
                }
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Tháng/Năm phải là số!");
            }
        }
    }

    // CASE 2: XÓA GIAO DỊCH (Hàm đơn giản không cần step, nhưng vẫn dùng getInput để hỗ trợ Hủy)
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

    // CASE 3, 4: (Các case chỉ in ra dữ liệu, không cần nhập liệu phức tạp)
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
    // CASE 7: THÊM DANH MỤC MỚI
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

    // CASE 8: ĐẶT/KIỂM TRA HẠN MỨC NGÂN SÁCH
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

    // CASE 9: THỐNG KÊ NÂNG CAO
    public void advancedStatisticsUI() {
        System.out.println("\n--- THỐNG KÊ NÂNG CAO ---");
        int step = 1;
        int m = 0, y = 0;

        while (step > 0 && step <= 2) {
            System.out.println("\n-1: Quay lại | 0: Hủy");
            try {
                switch (step) {
                    case 1:
                        System.out.print("1. Nhập tháng cần xem chi tiết (1-12): ");
                        m = Integer.parseInt(getInput());
                        if (m >= 1 && m <= 12) step++;
                        else System.out.println("Lỗi: Tháng không hợp lệ (Phải từ 1 đến 12).");
                        break;

                    case 2:
                        System.out.print("2. Nhập năm (YYYY): ");
                        y = Integer.parseInt(getInput());
                        step++;
                        break;
                }
            } catch (NavException e) {
                if (e.action.equals("CANCEL")) return;
                if (e.action.equals("BACK")) step--;
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Vui lòng chỉ nhập chữ số nguyên cho tháng và năm.");
            }
        }

        if (step == 0) {
            System.out.println("Đã hủy thao tác thống kê nâng cao.");
        } else {
            System.out.println("\nĐang xử lý dữ liệu...");
            manager.advancedStatistics(m, y);
            System.out.println("\nBấm phím Enter để quay lại Menu chính...");
            scanner.nextLine();
        }
    }
    // Các hàm addCategoryUI, setBudgetUI... bạn tự viết tương tự cấu trúc trên.
}