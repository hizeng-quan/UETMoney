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

    public boolean isExceeded(double spent) {
        return  spent > limit;
    }

    public Category getCategory() {
        return category;
    }

    public double getLimit() {
        return limit;
    }

    public Period getPeriod() {
        return period;
    }
}
