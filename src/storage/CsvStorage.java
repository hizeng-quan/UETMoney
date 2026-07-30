// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package storage;

import enums.Period;
import enums.TransactionType;
import interfaces.Storage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import models.CashWallet;
import models.Category;
import models.Expense;
import models.Income;
import models.RecurringExpense;
import models.Transaction;

public class CsvStorage implements Storage {
    private static final String HEADER = "type,id,amount,date,categoryName,categoryType,walletName,note,extraField,period";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String SEPARATOR = ",";

    public CsvStorage() {
    }

    public void save(List<Transaction> var1, String var2) throws Exception {
        File var3 = new File(var2);
        File var4 = var3.getParentFile();
        if (var4 != null && !var4.exists()) {
            var4.mkdirs();
        }

        BufferedWriter var5 = new BufferedWriter(new FileWriter(var2));

        try {
            var5.write("type,id,amount,date,categoryName,categoryType,walletName,note,extraField,period");
            var5.newLine();

            for(Transaction var7 : var1) {
                String var8 = this.transactionToCSV(var7);
                var5.write(var8);
                var5.newLine();
            }
        } catch (Throwable var10) {
            try {
                var5.close();
            } catch (Throwable var9) {
                var10.addSuppressed(var9);
            }

            throw var10;
        }

        var5.close();
    }

    public List<Transaction> load(String var1) throws Exception {
        ArrayList var2 = new ArrayList();
        File var3 = new File(var1);
        if (!var3.exists()) {
            return var2;
        } else {
            BufferedReader var4 = new BufferedReader(new FileReader(var1));

            try {
                String var5 = var4.readLine();

                while((var5 = var4.readLine()) != null) {
                    var5 = var5.trim();
                    if (!var5.isEmpty()) {
                        try {
                            Transaction var6 = this.csvToTransaction(var5);
                            if (var6 != null) {
                                var2.add(var6);
                            }
                        } catch (Exception var8) {
                            System.out.println("Canh bao: Khong the doc dong CSV: " + var5);
                            System.out.println("  Ly do: " + var8.getMessage());
                        }
                    }
                }
            } catch (Throwable var9) {
                try {
                    var4.close();
                } catch (Throwable var7) {
                    var9.addSuppressed(var7);
                }

                throw var9;
            }

            var4.close();
            return var2;
        }
    }

    private String transactionToCSV(Transaction var1) {
        StringBuilder var2 = new StringBuilder();
        String var5 = "";
        String var3;
        String var4;
        if (var1 instanceof RecurringExpense var6) {
            var3 = "RECURRING";
            var4 = var6.getPaymentMethod();
            var5 = var6.getPeriod().name();
        } else if (var1 instanceof Income) {
            var3 = "INCOME";
            var4 = ((Income)var1).getSource();
        } else if (var1 instanceof Expense) {
            var3 = "EXPENSE";
            var4 = ((Expense)var1).getPaymentMethod();
        } else {
            var3 = "UNKNOWN";
            var4 = "";
        }

        var2.append(var3).append(",");
        var2.append(this.escapeCSV(var1.getId())).append(",");
        var2.append(var1.getAmount()).append(",");
        var2.append(var1.getDate().format(DATE_FORMAT)).append(",");
        var2.append(this.escapeCSV(var1.getCategory().getName())).append(",");
        var2.append(var1.getCategory().getType().name()).append(",");
        var2.append(this.escapeCSV(var1.getWallet().getName())).append(",");
        var2.append(this.escapeCSV(var1.getNote())).append(",");
        var2.append(this.escapeCSV(var4)).append(",");
        var2.append(var5);
        return var2.toString();
    }

    private Transaction csvToTransaction(String var1) {
        String[] var2 = this.parseCSVLine(var1);
        if (var2.length < 9) {
            throw new IllegalArgumentException("Dong CSV khong du truong (can it nhat 9, co " + var2.length + ")");
        } else {
            String var3 = var2[0].trim();
            String var4 = this.unescapeCSV(var2[1].trim());
            double var5 = Double.parseDouble(var2[2].trim());
            LocalDate var7 = LocalDate.parse(var2[3].trim(), DATE_FORMAT);
            String var8 = this.unescapeCSV(var2[4].trim());
            TransactionType var9 = TransactionType.valueOf(var2[5].trim());
            String var10 = this.unescapeCSV(var2[6].trim());
            String var11 = this.unescapeCSV(var2[7].trim());
            String var12 = this.unescapeCSV(var2[8].trim());
            Category var13 = new Category(var8, var9);
            CashWallet var14 = new CashWallet(var10, (double)0.0F);
            switch (var3) {
                case "INCOME":
                    return new Income(var4, var5, var7, var13, var11, var14, var12);
                case "EXPENSE":
                    return new Expense(var4, var5, var11, var7, var13, var14, var12);
                case "RECURRING":
                    Period var17 = Period.MONTHLY;
                    if (var2.length > 9 && !var2[9].trim().isEmpty()) {
                        var17 = Period.valueOf(var2[9].trim());
                    }

                    return new RecurringExpense(var4, var5, var11, var7, var13, var14, var12, var17);
                default:
                    throw new IllegalArgumentException("Loai giao dich khong hop le: " + var3);
            }
        }
    }

    private String escapeCSV(String var1) {
        if (var1 == null) {
            return "";
        } else {
            return !var1.contains(",") && !var1.contains("\"") && !var1.contains("\n") ? var1 : "\"" + var1.replace("\"", "\"\"") + "\"";
        }
    }

    private String unescapeCSV(String var1) {
        if (var1 != null && !var1.isEmpty()) {
            if (var1.startsWith("\"") && var1.endsWith("\"")) {
                var1 = var1.substring(1, var1.length() - 1);
                var1 = var1.replace("\"\"", "\"");
            }

            return var1;
        } else {
            return "";
        }
    }

    private String[] parseCSVLine(String var1) {
        ArrayList var2 = new ArrayList();
        StringBuilder var3 = new StringBuilder();
        boolean var4 = false;

        for(int var5 = 0; var5 < var1.length(); ++var5) {
            char var6 = var1.charAt(var5);
            if (var4) {
                if (var6 == '"') {
                    if (var5 + 1 < var1.length() && var1.charAt(var5 + 1) == '"') {
                        var3.append('"');
                        ++var5;
                    } else {
                        var4 = false;
                    }
                } else {
                    var3.append(var6);
                }
            } else if (var6 == '"') {
                var4 = true;
            } else if (var6 == ',') {
                var2.add(var3.toString());
                var3 = new StringBuilder();
            } else {
                var3.append(var6);
            }
        }

        var2.add(var3.toString());
        return (String[])var2.toArray(new String[0]);
    }
}
