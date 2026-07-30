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
        System.out.println("Giao dịch " + id + " | " + getCategory() + " | " + getSignedAmount());
        System.out.println("Ví: " + getWallet().getName() + " | Lặp lại " + periodVN);

        if (note != null && !note.isEmpty()) {
            System.out.println("   Ghi chú: " + note);
        }
    }
}