package models;

import enums.Period;

public class Budget {
    private Category category;
    private double limit;
    private Period period;

    public Budget(Category category, double limit, Period period) {
        this.category = category;
        this.limit = limit;
        this.period = period;
    }

    /**
     * Kiểm tra chi tiêu đã vượt hạn mức chưa.
     * @param spent tổng chi tiêu hiện tại
     * @return true nếu đã vượt hạn mức
     */
    public boolean isExceeded(double spent) {
        return spent > limit;
    }

    /**
     * Tính số tiền còn lại trong hạn mức.
     * @param spent tổng chi tiêu hiện tại
     * @return số tiền còn được phép chi (âm nếu đã vượt)
     */
    public double getRemaining(double spent) {
        return limit - spent;
    }

    public Category getCategory() {
        return category;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        if (limit > 0) {
            this.limit = limit;
        }
    }

    public Period getPeriod() {
        return period;
    }
}
