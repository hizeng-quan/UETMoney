package models;

import java.time.LocalDate;

public class RecurringExpense extends Expense {
    private String period;

    public RecurringExpense(String id, double amount, String note, LocalDate date, Category category, Wallet wallet, String paymentMethod, String period) {
        super(id, amount, note, date, category, wallet, paymentMethod);
        this.period = period;
    }

    public String getPeriod() {
        return period;
    }

    @Override
    public void printInfo() {
        System.out.printf("[%s] %s | %s | %s | Số tiền: %,.2f VND | Ví: %s | Lặp lại: %s\n",
                id, date.toString(), getType(), category.getName(), getSignedAmount(), wallet.getName(), period);
        if (note != null && !note.isEmpty()) {
            System.out.println("   Ghi chú: " + note);
        }
    }
}
