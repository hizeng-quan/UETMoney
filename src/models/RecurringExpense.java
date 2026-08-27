package models;

import enums.Period;

import java.time.LocalDate;

public class RecurringExpense extends Expense {
    private Period period;
    private LocalDate lastProcessedDate;

    public RecurringExpense(String id, double amount, String note, LocalDate date, Category category, Wallet wallet, String paymentMethod, Period period) {
        super(id, amount, note, date, category, wallet, paymentMethod);
        this.period = period;
    }

    public LocalDate getLastProcessedDate() {
        return lastProcessedDate;
    }

    public void setLastProcessedDate(LocalDate lastProcessedDate) {
        this.lastProcessedDate = lastProcessedDate;
    }

    /**
     * Tính ngày đáo hạn tiếp theo dựa trên ngày giao dịch gốc và chu kỳ (hoặc lần xử lý cuối cùng).
     * @return ngày đáo hạn kế tiếp kể từ lần xử lý cuối cùng (hoặc ngày gốc)
     */
    public LocalDate nextDueDate() {
        LocalDate baseDate = (lastProcessedDate != null) ? lastProcessedDate : this.getDate();
        
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
                return baseDate;
        }
    }

    /**
     * Kiểm tra giao dịch định kỳ đã đến hạn hay chưa.
     * @return true nếu ngày đáo hạn tiếp theo <= hôm nay
     */
    public boolean isDue() {
        return !nextDueDate().isAfter(LocalDate.now());
    }

    @Override
    public void printInfo() {
        System.out.printf("Giao dich %s | %s | %,.0f VND | Chu ky: %s | Dao han tiep: %s\n",
                getId(), getType(), getSignedAmount(), period, nextDueDate());
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }
}