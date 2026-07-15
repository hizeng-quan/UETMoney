package models;

import enums.TransactionType;

public class Category {
    private String name;
    private TransactionType type;

    public Category(String name, TransactionType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getType() {
        return type;
    }
    public void setType(TransactionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
