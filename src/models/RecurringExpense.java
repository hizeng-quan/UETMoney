package models;

import enums.Period;

import java.time.LocalDate;

public class RecurringExpense extends Expense {
    private Period period;

    public RecurringExpense(String id, double amount, String note, LocalDate date, Category category, Wallet wallet, String paymentMethod, Period period) {
        super(id, amount, note, date, category, wallet, paymentMethod);
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }

    @Override
    public void printInfo() {
        String periodVN = "";
        if (period == Period.DAILY) periodVN = "Hàng ngày";
        else if (period == Period.WEEKLY) periodVN = "Hàng tuần";
        else if (period == Period.MONTHLY) periodVN = "Hàng tháng";
        else if (period == Period.YEARLY) periodVN = "Hàng năm";
        System.out.printf("[%s] %s | %s | %s | Số tiền: %,.2f VND | Ví: %s | Lặp lại: %s\n",
                id, date.toString(), getType(), category.getName(), getSignedAmount(), wallet.getName(), periodVN);
        if (note != null && !note.isEmpty()) {
            System.out.println("   Ghi chú: " + note);
        }
    }
}
