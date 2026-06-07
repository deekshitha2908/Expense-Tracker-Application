import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// Expense class (Encapsulation + Serializable)
class Expense implements Serializable {
    private String date;       // DD-MM-YYYY
    private String category;
    private double amount;
    private String note;

    public Expense(String date, String category, double amount, String note) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.note = note;
    }

    public String getDate() { return date; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        return "Date: " + date + ", Category: " + category +
               ", Amount: Rs." + amount + ", Note: " + note;
    }
}

// ExpenseManager class
class ExpenseManager {
    private ArrayList<Expense> expenseList = new ArrayList<>();
    private static final String FILE_NAME = "expenses.dat";
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private Scanner sc = new Scanner(System.in);

    // Add new expense
    public void addExpense() {
        System.out.print("Enter Date (DD-MM-YYYY): ");
        String date = sc.nextLine().trim();
        System.out.print("Enter Category: ");
        String category = sc.nextLine().trim();
        System.out.print("Enter Amount (Rs.): ");
        double amount = sc.nextDouble();
        sc.nextLine();
        System.out.print("Enter Note (optional): ");
        String note = sc.nextLine().trim();

        expenseList.add(new Expense(date, category, amount, note));
        saveToFile();
        System.out.println("Expense added successfully!");
    }

    // Show all expenses
    public void showAllExpenses() {
        if (expenseList.isEmpty()) {
            System.out.println("No expenses recorded yet.");
            return;
        }

        System.out.println("\nAll Recorded Expenses:");
        int i = 1;
        for (Expense e : expenseList) {
            System.out.println(i++ + ". " + e);
        }
    }

    // Show summary by category
    public void showCategorySummary() {
        if (expenseList.isEmpty()) {
            System.out.println("No expenses to summarize.");
            return;
        }

        HashMap<String, Double> summary = new HashMap<>();
        for (Expense e : expenseList) {
            String cat = e.getCategory().trim().toLowerCase();
            summary.put(cat, summary.getOrDefault(cat, 0.0) + e.getAmount());
        }

        System.out.println("\nExpense Summary by Category:");
        for (Map.Entry<String, Double> entry : summary.entrySet()) {
            System.out.println(entry.getKey() + " : Rs." + entry.getValue());
        }
    }

    // Show total by month and compare with previous month
    public void showMonthlyComparison() {
        if (expenseList.isEmpty()) {
            System.out.println("No data to compare.");
            return;
        }

        HashMap<String, Double> monthlyTotal = new HashMap<>();

        for (Expense e : expenseList) {
            try {
                Date date = sdf.parse(e.getDate());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int month = cal.get(Calendar.MONTH) + 1;
                int year = cal.get(Calendar.YEAR);
                String key = month + "-" + year;
                monthlyTotal.put(key, monthlyTotal.getOrDefault(key, 0.0) + e.getAmount());
            } catch (ParseException ex) {
                System.out.println("Invalid date format in entry: " + e.getDate());
            }
        }

        if (monthlyTotal.isEmpty()) {
            System.out.println("No valid data to compare.");
            return;
        }

        System.out.println("\nTotal Expenses by Month:");
        List<String> sortedKeys = new ArrayList<>(monthlyTotal.keySet());
        Collections.sort(sortedKeys, (a, b) -> {
            String[] pa = a.split("-");
            String[] pb = b.split("-");
            int yearA = Integer.parseInt(pa[1]);
            int monthA = Integer.parseInt(pa[0]);
            int yearB = Integer.parseInt(pb[1]);
            int monthB = Integer.parseInt(pb[0]);
            if (yearA == yearB) return monthA - monthB;
            return yearA - yearB;
        });

        for (String key : sortedKeys) {
            System.out.println(key + " : Rs." + monthlyTotal.get(key));
        }

        // Compare last month vs current month
        if (sortedKeys.size() >= 2) {
            String lastMonthKey = sortedKeys.get(sortedKeys.size() - 2);
            String currentMonthKey = sortedKeys.get(sortedKeys.size() - 1);
            double lastTotal = monthlyTotal.get(lastMonthKey);
            double currentTotal = monthlyTotal.get(currentMonthKey);
            double diff = currentTotal - lastTotal;

            System.out.println("\nComparison:");
            System.out.println("Last Month (" + lastMonthKey + "): Rs." + lastTotal);
            System.out.println("This Month (" + currentMonthKey + "): Rs." + currentTotal);

            if (diff > 0)
                System.out.println("You spent Rs." + diff + " more this month");
            else if (diff < 0)
                System.out.println("You saved Rs." + (-diff) + " compared to last month!");
            else
                System.out.println("You spent the same amount both months!");
        }
    }

    // Delete expense
    public void deleteExpense() {
        if (expenseList.isEmpty()) {
            System.out.println("No expenses to delete.");
            return;
        }

        showAllExpenses();
        System.out.print("Enter the number of the expense to delete: ");
        int index = sc.nextInt();
        sc.nextLine();

        if (index > 0 && index <= expenseList.size()) {
            Expense removed = expenseList.remove(index - 1);
            System.out.println("Deleted: " + removed);
            saveToFile();
        } else {
            System.out.println("Invalid expense number.");
        }
    }

    // File handling
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(expenseList);
        } catch (IOException e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            expenseList = (ArrayList<Expense>) ois.readObject();
        } catch (Exception e) {
            System.out.println("Error loading: " + e.getMessage());
        }
    }
}

// Main Class
public class ExpenseTracker {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ExpenseManager manager = new ExpenseManager();
        manager.loadFromFile();

        while (true) {
            System.out.println("\n===== Expense Tracker Menu =====");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. View Summary by Category");
            System.out.println("4. View Monthly Comparison");
            System.out.println("5. Delete Expense");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: manager.addExpense(); break;
                case 2: manager.showAllExpenses(); break;
                case 3: manager.showCategorySummary(); break;
                case 4: manager.showMonthlyComparison(); break;
                case 5: manager.deleteExpense(); break;
                case 6:
                    System.out.println("Exiting and saving data... ");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }
}
