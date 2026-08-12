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
     * Tính ngày đáo hạn tiếp theo dựa trên ngày giao dịch gốc và chu kỳ.
     * @return ngày đáo hạn kế tiếp kể từ hôm nay
     */
    public LocalDate nextDueDate() {
        LocalDate next = this.getDate();
        LocalDate today = LocalDate.now();

        while (!next.isAfter(today)) {
            switch (period) {
                case DAILY:
                    next = next.plusDays(1);
                    break;
                case WEEKLY:
                    next = next.plusWeeks(1);
                    break;
                case MONTHLY:
                    next = next.plusMonths(1);
                    break;
                case YEARLY:
                    next = next.plusYears(1);
                    break;
            }
        }
        return next;
    }

    /**
     * Kiểm tra giao dịch định kỳ đã đến hạn hay chưa.
     * @return true nếu ngày đáo hạn tiếp theo <= hôm nay
     */
    public boolean isDue() {
        LocalDate next = this.getDate();
        LocalDate today = LocalDate.now();

        // Nếu ngày gốc đã qua, kiểm tra xem có kỳ mới đến hạn không
        while (next.isBefore(today)) {
            switch (period) {
                case DAILY:
                    next = next.plusDays(1);
                    break;
                case WEEKLY:
                    next = next.plusWeeks(1);
                    break;
                case MONTHLY:
                    next = next.plusMonths(1);
                    break;
                case YEARLY:
                    next = next.plusYears(1);
                    break;
            }
        }
        return next.isEqual(today);
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