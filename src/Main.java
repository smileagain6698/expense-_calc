import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List; // Explicitly import java.util.List to avoid ambiguity

class PersonalExpenseTrackerGUI extends JFrame {

    private static final String FILE_NAME = "expenses.txt";
    private Map<String, List<Expense>> expensesByCategory = new HashMap<>();  // Use java.util.List explicitly
    private DefaultTableModel tableModel;
    private JTable expenseTable;

    public PersonalExpenseTrackerGUI() {
        setTitle("Personal Expense Tracker");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Apply Dark Mode
        applyDarkMode();

        loadExpenses();

        // GUI Layout
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        // Table to display expenses
        String[] columns = {"Category", "Amount", "Description"};
        tableModel = new DefaultTableModel(columns, 0);
        expenseTable = new JTable(tableModel);
        expenseTable.setBackground(Color.DARK_GRAY);
        expenseTable.setForeground(Color.WHITE);
        expenseTable.setGridColor(Color.LIGHT_GRAY);
        expenseTable.setSelectionBackground(Color.GRAY);
        JScrollPane tablePane = new JScrollPane(expenseTable);
        panel.add(tablePane, BorderLayout.CENTER);

        // Add expense panel
        JPanel addExpensePanel = new JPanel(new GridLayout(4, 2, 5, 5));
        addExpensePanel.setBackground(Color.DARK_GRAY);

        JLabel labelAmount = new JLabel("Amount:");
        labelAmount.setForeground(Color.WHITE);
        JTextField fieldAmount = new JTextField();
        fieldAmount.setBackground(Color.GRAY);
        fieldAmount.setForeground(Color.WHITE);
        addExpensePanel.add(labelAmount);
        addExpensePanel.add(fieldAmount);

        JLabel labelCategory = new JLabel("Category:");
        labelCategory.setForeground(Color.WHITE);
        JTextField fieldCategory = new JTextField();
        fieldCategory.setBackground(Color.GRAY);
        fieldCategory.setForeground(Color.WHITE);
        addExpensePanel.add(labelCategory);
        addExpensePanel.add(fieldCategory);

        JLabel labelDescription = new JLabel("Description:");
        labelDescription.setForeground(Color.WHITE);
        JTextField fieldDescription = new JTextField();
        fieldDescription.setBackground(Color.GRAY);
        fieldDescription.setForeground(Color.WHITE);
        addExpensePanel.add(labelDescription);
        addExpensePanel.add(fieldDescription);

        JButton addButton = new JButton("Add Expense");
        addButton.setBackground(Color.GRAY);
        addButton.setForeground(Color.WHITE);
        addExpensePanel.add(addButton);

        JButton deleteButton = new JButton("Delete Selected Expense");
        deleteButton.setBackground(Color.GRAY);
        deleteButton.setForeground(Color.WHITE);
        addExpensePanel.add(deleteButton);

        panel.add(addExpensePanel, BorderLayout.SOUTH);

        // Add summary button
        JButton summaryButton = new JButton("View Summary");
        summaryButton.setBackground(Color.GRAY);
        summaryButton.setForeground(Color.WHITE);
        panel.add(summaryButton, BorderLayout.NORTH);

        // Add functionality to the buttons
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(fieldAmount.getText());
                    String category = fieldCategory.getText();
                    String description = fieldDescription.getText();

                    Expense expense = new Expense(amount, category, description);
                    addExpense(expense);

                    // Add to table
                    tableModel.addRow(new Object[]{category, amount, description});

                    // Clear input fields
                    fieldAmount.setText("");
                    fieldCategory.setText("");
                    fieldDescription.setText("");

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input for amount!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = expenseTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String category = tableModel.getValueAt(selectedRow, 0).toString();
                    String description = tableModel.getValueAt(selectedRow, 2).toString();
                    deleteExpense(category, description);
                    tableModel.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(null, "No expense selected!", "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        summaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewSummary();
            }
        });

        add(panel);
        setVisible(true);
    }

    // Apply dark mode to the entire UI
    private void applyDarkMode() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background", Color.DARK_GRAY);
            UIManager.put("Table.background", Color.DARK_GRAY);
            UIManager.put("Table.foreground", Color.WHITE);
            UIManager.put("Table.gridColor", Color.LIGHT_GRAY);
            UIManager.put("Button.background", Color.GRAY);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("TextField.background", Color.GRAY);
            UIManager.put("TextField.foreground", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add an expense
    private void addExpense(Expense expense) {
        expensesByCategory.putIfAbsent(expense.getCategory(), new ArrayList<>());
        expensesByCategory.get(expense.getCategory()).add(expense);
        saveExpenses();
    }

    // Delete an expense
    private void deleteExpense(String category, String description) {
        if (expensesByCategory.containsKey(category)) {
            List<Expense> expenses = expensesByCategory.get(category);
            expenses.removeIf(expense -> expense.getDescription().equals(description));
            if (expenses.isEmpty()) {
                expensesByCategory.remove(category);
            }
        }
        saveExpenses();
    }

    // View expense summary
    private void viewSummary() {
        StringBuilder summary = new StringBuilder();
        double total = 0;

        for (String category : expensesByCategory.keySet()) {
            double categoryTotal = expensesByCategory.get(category).stream().mapToDouble(Expense::getAmount).sum();
            summary.append("Category: ").append(category).append(" - Total: ").append(categoryTotal).append("\n");
            total += categoryTotal;
        }

        summary.append("Total Spending: ").append(total);
        JOptionPane.showMessageDialog(this, summary.toString(), "Expense Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    // Save expenses to a file
    private void saveExpenses() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(expensesByCategory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load expenses from a file
    private void loadExpenses() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            expensesByCategory = (Map<String, List<Expense>>) ois.readObject();
            for (String category : expensesByCategory.keySet()) {
                for (Expense expense : expensesByCategory.get(category)) {
                    tableModel.addRow(new Object[]{category, expense.getAmount(), expense.getDescription()});
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            expensesByCategory = new HashMap<>();
        }
    }

    // Expense class
    private static class Expense implements Serializable {
        private double amount;
        private String category;
        private String description;

        public Expense(double amount, String category, String description) {
            this.amount = amount;
            this.category = category;
            this.description = description;
        }

        public double getAmount() {
            return amount;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "Amount: " + amount + ", Category: " + category + ", Description: " + description;
        }
    }

    public static void main(String[] args) {
        new PersonalExpenseTrackerGUI();
    }
}
