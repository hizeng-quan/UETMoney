import controllers.ExpenseManager;
import enums.Period;
import enums.TransactionType;
import models.*;
import storage.CsvStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "data/transactions.csv";

    public static void main(String[] args) {
        ExpenseManager manager = new ExpenseManager();
        CsvStorage storage = new CsvStorage();
        Scanner scanner = new Scanner(System.in);

        // === KHI KHỞI ĐỘNG: Nạp dữ liệu từ file CSV ===
        loadDataFromFile(manager, storage);

        // === VÒNG LẶP MENU CHÍNH ===
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addTransactionUI(manager, scanner);
                    break;
                case "2":
                    System.out.print("Nhap ma giao dich muon xoa: ");
                    String id = scanner.nextLine();
                    manager.removeTransaction(id);
                    break;
                case "3":
                    manager.displayAllTransactions();
                    break;
                case "4":
                    System.out.printf("TONG SO DU HIEN TAI: %,.2f VND\n", manager.calculateTotalBalance());
                    break;
                case "5":
                    statisticsMenuUI(manager, scanner);
                    break;
                case "6":
                    addWalletUI(manager, scanner);
                    break;
                case "7":
                    addCategoryUI(manager, scanner);
                    break;
                case "8":
                    budgetMenuUI(manager, scanner);
                    break;
                case "9":
                    advancedStatsMenuUI(manager, scanner);
                    break;
                case "10":
                    addRecurringExpenseUI(manager, scanner);
                    break;
                case "11":
                    checkRecurringDueUI(manager, scanner);
                    break;
                case "0":
                    // === KHI THOÁT: Tự động lưu dữ liệu ===
                    saveDataToFile(manager, storage);
                    System.out.println("Cam on ban da su dung UETMoney. Hen gap lai!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Lua chon khong hop le. Xin vui long nhap lai!");
            }
            System.out.println(); // Dòng trống giữa các lần chọn menu
        }
    }

    // ==================== MENU ====================

    private static void printMenu() {
        System.out.println("========== UETMoney ==========");
        System.out.println("[0]  Thoat chuong trinh");
        System.out.println("[1]  Them giao dich moi");
        System.out.println("[2]  Xoa giao dich theo ma");
        System.out.println("[3]  Xem tat ca lich su giao dich");
        System.out.println("[4]  Xem tong so du cac vi");
        System.out.println("[5]  Thong ke theo thang/nam");
        System.out.println("[6]  Them vi tien moi");
        System.out.println("[7]  Them danh muc moi");
        System.out.println("[8]  Dat / Xem han muc ngan sach");
        System.out.println("[9]  Thong ke nang cao");
        System.out.println("[10] Them giao dich dinh ky");
        System.out.println("[11] Kiem tra giao dich dinh ky den han");
        System.out.println("==============================");
        System.out.print("Moi ban chon chuc nang (0-11): ");
    }

    // ==================== LOAD / SAVE ====================

    /**
     * Nạp dữ liệu từ file CSV khi khởi động.
     */
    private static void loadDataFromFile(ExpenseManager manager, CsvStorage storage) {
        try {
            List<Transaction> loaded = storage.load(DATA_FILE);
            if (!loaded.isEmpty()) {
                manager.importTransactions(loaded);
                System.out.println("Da nap " + loaded.size() + " giao dich tu file " + DATA_FILE);
            } else {
                System.out.println("Chua co du lieu cu, bat dau moi.");
            }
        } catch (Exception e) {
            System.out.println("Loi khi doc file du lieu: " + e.getMessage());
            System.out.println("Bat dau voi du lieu rong.");
        }
    }

    /**
     * Lưu dữ liệu vào file CSV khi thoát.
     */
    private static void saveDataToFile(ExpenseManager manager, CsvStorage storage) {
        try {
            storage.save(manager.getTransactions(), DATA_FILE);
            System.out.println("Da luu " + manager.getTransactions().size()
                    + " giao dich vao file " + DATA_FILE);
        } catch (Exception e) {
            System.out.println("Loi khi luu du lieu: " + e.getMessage());
        }
    }

    // ==================== [1] THÊM GIAO DỊCH ====================

    private static void addTransactionUI(ExpenseManager manager, Scanner scanner) {
        try {
            System.out.println("\nTHEM GIAO DICH MOI");
            System.out.print("Nhap ma giao dich: ");
            String id = scanner.nextLine();

            System.out.print("Nhap so tien: ");
            double amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) {
                System.out.println("Loi: So tien phai lon hon 0!");
                return;
            }

            System.out.print("Ngay giao dich (dd/MM/yyyy): ");
            String dateStr = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(dateStr, formatter);

            System.out.print("Ghi chu: ");
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

            System.out.println("Phan loai: Thu nhap (1) hay Chi tieu (2)?");
            String type = scanner.nextLine();

            Transaction t;
            if (type.equals("1")) {
                System.out.print("Nguon thu: ");
                String source = scanner.nextLine();
                t = new Income(id, amount, date, category, note, wallet, source);
            } else if (type.equals("2")) {
                System.out.print("Phuong thuc thanh toan: ");
                String paymentMethod = scanner.nextLine();
                t = new Expense(id, amount, note, date, category, wallet, paymentMethod);
            } else {
                System.out.println("Loi: Loai giao dich khong hop le!");
                return;
            }

            manager.addTransaction(t);
        } catch (NumberFormatException e) {
            System.out.println("Loi: So tien nhap vao khong hop le (Phai la chu so)!");
        } catch (DateTimeParseException e) {
            System.out.println("Loi: Dinh dang ngay thang khong dung (Can nhap dd/MM/yyyy)!");
        }
    }

    // ==================== [5] THỐNG KÊ THÁNG/NĂM ====================

    private static void statisticsMenuUI(ExpenseManager manager, Scanner scanner) {
        try {
            System.out.println("Ban muon thong ke theo:");
            System.out.println("[1] Tung thang");
            System.out.println("[2] Ca nam");

            String statChoice = scanner.nextLine();
            if (statChoice.equals("1")) {
                System.out.print("Nhap thang: ");
                int month = Integer.parseInt(scanner.nextLine());
                System.out.print("Nhap nam: ");
                int year = Integer.parseInt(scanner.nextLine());
                manager.monthlySummary(month, year);
            } else if (statChoice.equals("2")) {
                System.out.print("Nhap nam: ");
                int year = Integer.parseInt(scanner.nextLine());
                manager.yearlySummary(year);
            } else {
                System.out.println("Lua chon khong hop le, vui long nhap lai!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Loi: Vui long chi nhap so cho thang va nam!");
        }
    }

    // ==================== [6] THÊM VÍ ====================

    private static void addWalletUI(ExpenseManager manager, Scanner scanner) {
        try {
            System.out.print("Nhap ten vi tien: ");
            String wName = scanner.nextLine();
            System.out.print("Nhap so du ban dau: ");
            double wBalance = Double.parseDouble(scanner.nextLine());
            manager.addWallet(new CashWallet(wName, wBalance));
        } catch (NumberFormatException e) {
            System.out.println("Loi: So du phai la so!");
        }
    }

    // ==================== [7] THÊM DANH MỤC ====================

    private static void addCategoryUI(ExpenseManager manager, Scanner scanner) {
        System.out.print("Nhap ten danh muc: ");
        String cName = scanner.nextLine();
        System.out.print("Loai danh muc - Thu nhap (1) / Chi tieu (2)?: ");
        String cType = scanner.nextLine();
        TransactionType type = cType.equals("1") ? TransactionType.INCOME : TransactionType.EXPENSE;
        manager.addCategory(new Category(cName, type));
        System.out.println("Da them danh muc: " + cName);
    }

    // ==================== [8] HẠN MỨC NGÂN SÁCH ====================

    private static void budgetMenuUI(ExpenseManager manager, Scanner scanner) {
        System.out.println("\n===== HAN MUC NGAN SACH =====");
        System.out.println("[1] Dat han muc moi");
        System.out.println("[2] Xem tat ca han muc");
        System.out.println("[3] Xoa han muc");
        System.out.print("Chon: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                try {
                    System.out.print("Nhap ten danh muc: ");
                    String catName = scanner.nextLine();
                    Category category = manager.getCategoryByName(catName);
                    if (category == null) {
                        System.out.println("Loi: Khong tim thay danh muc '" + catName + "'!");
                        return;
                    }

                    System.out.print("Nhap han muc (VND): ");
                    double limit = Double.parseDouble(scanner.nextLine());
                    if (limit <= 0) {
                        System.out.println("Loi: Han muc phai lon hon 0!");
                        return;
                    }

                    System.out.println("Chu ky: [1] Hang ngay  [2] Hang tuan  [3] Hang thang  [4] Hang nam");
                    System.out.print("Chon: ");
                    String periodChoice = scanner.nextLine();
                    Period period;
                    switch (periodChoice) {
                        case "1": period = Period.DAILY; break;
                        case "2": period = Period.WEEKLY; break;
                        case "3": period = Period.MONTHLY; break;
                        case "4": period = Period.YEARLY; break;
                        default:
                            System.out.println("Lua chon khong hop le, mac dinh la MONTHLY.");
                            period = Period.MONTHLY;
                    }

                    Budget budget = new Budget(category, limit, period);
                    manager.addBudget(budget);
                } catch (NumberFormatException e) {
                    System.out.println("Loi: Han muc phai la so!");
                }
                break;
            case "2":
                manager.displayAllBudgets();
                break;
            case "3":
                System.out.print("Nhap ten danh muc can xoa han muc: ");
                String removeCat = scanner.nextLine();
                manager.removeBudget(removeCat);
                break;
            default:
                System.out.println("Lua chon khong hop le!");
        }
    }

    // ==================== [9] THỐNG KÊ NÂNG CAO ====================

    private static void advancedStatsMenuUI(ExpenseManager manager, Scanner scanner) {
        System.out.println("\n===== THONG KE NANG CAO =====");
        System.out.println("[1] Chi tieu theo tung danh muc");
        System.out.println("[2] Chi tieu theo tung thang trong nam");
        System.out.println("[3] Khoan chi lon nhat / nho nhat");
        System.out.println("[4] Top danh muc ton kem nhat");
        System.out.print("Chon: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                manager.displayStatisticsByCategory();
                break;
            case "2":
                try {
                    System.out.print("Nhap nam: ");
                    int year = Integer.parseInt(scanner.nextLine());
                    manager.displayMonthlyExpenseBreakdown(year);
                } catch (NumberFormatException e) {
                    System.out.println("Loi: Vui long nhap nam hop le!");
                }
                break;
            case "3":
                manager.displayMinMaxExpense();
                break;
            case "4":
                try {
                    System.out.print("Nhap so luong top N: ");
                    int n = Integer.parseInt(scanner.nextLine());
                    if (n <= 0) {
                        System.out.println("Loi: So luong phai lon hon 0!");
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
                case "1": period = Period.DAILY; break;
                case "2": period = Period.WEEKLY; break;
                case "3": period = Period.MONTHLY; break;
                case "4": period = Period.YEARLY; break;
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

    // ==================== [11] KIỂM TRA GIAO DỊCH ĐỊNH KỲ ĐẾN HẠN ====================

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