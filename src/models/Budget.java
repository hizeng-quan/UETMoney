package models;

import enums.Period;
import exception.InsufficientBalanceException;

public class Budget {
    private Category category;
    private double limit;
    private Period period;

    public Budget(Category category, double limit, Period period) {
            this.limit = limit;
            this.category = category;
            this.period = period;
    }

    public boolean isExceeded(double spent) {
        return  spent > limit;
    }

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
