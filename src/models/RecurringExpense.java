package models;

import enums.Period;
import enums.TransactionType;

import java.time.LocalDate;

/**
 * Giao dịch chi tiêu định kỳ - kế thừa Expense.
 * Bổ sung chu kỳ lặp lại (period) và tính ngày đến hạn tiếp theo (nextDueDate).
 */
public class RecurringExpense extends Expense {
    private Period period;
    private LocalDate lastProcessedDate;

    public RecurringExpense(String id, double amount, String note, LocalDate date,
            Category category, Wallet wallet, String paymentMethod, Period period) {
        super(id, amount, note, date, category, wallet, paymentMethod);
        this.period = period;
        this.lastProcessedDate = null; // Chưa xử lý lần nào
    }

    public Period getPeriod() {
        return period;
    }

    public LocalDate getLastProcessedDate() {
        return lastProcessedDate;
    }

    public void setLastProcessedDate(LocalDate lastProcessedDate) {
        this.lastProcessedDate = lastProcessedDate;
    }

    /**
     * Tính ngày đến hạn tiếp theo dựa trên ngày gốc (hoặc lastProcessedDate) và chu
     * kỳ.
     * Nếu đã xử lý trước đó, tính từ lastProcessedDate.
     * Nếu chưa, tính từ ngày tạo giao dịch (date).
     *
     * @return ngày đến hạn kỳ tiếp theo
     */
    public LocalDate nextDueDate() {
        LocalDate baseDate = (lastProcessedDate != null) ? lastProcessedDate : date;

        switch (period) {
            case DAILY:
                return baseDate.plusDays(1);
            case WEEKLY:
                return baseDate.plusWeeks(1);
            case MONTHLY:
                return baseDate.plusMonths(1);
            case YEARLY:
                return baseDate.plusYears(1);
            default:
                return baseDate.plusMonths(1);
        }
    }

    /**
     * Kiểm tra xem giao dịch định kỳ này đã đến hạn chưa (so với ngày hiện tại).
     *
     * @return true nếu nextDueDate <= hôm nay
     */
    public boolean isDue() {
        LocalDate nextDue = nextDueDate();
        return !nextDue.isAfter(LocalDate.now());
    }

    @Override
    public void printInfo() {
        String periodVN;
        switch (period) {
            case DAILY:
                periodVN = "Hàng ngày";
                break;
            case WEEKLY:
                periodVN = "Hàng tuần";
                break;
            case MONTHLY:
                periodVN = "Hàng tháng";
                break;
            case YEARLY:
                periodVN = "Hàng năm";
                break;
            default:
                periodVN = period.name();
        }

        System.out.println("Giao dịch " + id + " | " + getCategory() + " | " + getSignedAmount());
        System.out.println("Ví: " + getWallet().getName() + " | Lặp lại: " + periodVN + " | Kỳ tiếp: " + nextDueDate());

        if (note != null && !note.isEmpty()) {
            System.out.println("   Ghi chú: " + note);
        }
    }
}