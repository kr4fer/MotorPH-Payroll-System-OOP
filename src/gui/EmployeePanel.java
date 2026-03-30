package gui;

import model.FileHandler;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class EmployeePanel extends JPanel {

    private final Color gradientStart; 
    private final Color gradientEnd = new Color(20, 50, 110); 
    private final FileHandler fileHandler;
    private final EmployeeTable dashboardTable;
    private Object table;

    public EmployeePanel() {
        this.gradientStart = new Color(29, 69, 143);
        
        setLayout(new BorderLayout());
        setOpaque(false); // Keeps panel transparent to show dashboard background
        
        fileHandler = new FileHandler();
        dashboardTable = new EmployeeTable(fileHandler);

        // --- TOP NAVIGATION SECTION ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(20, 50, 0, 50));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton viewButton = new JButton("View Employee");
        styleMinimalButton(viewButton, 120, 36);
        viewButton.addActionListener(e -> showSelectedEmployeeDetails());
        rightPanel.add(viewButton);

        JButton addButton = new JButton("Add Employee");
        styleMinimalButton(addButton, 120, 36);
        addButton.addActionListener(e -> showAddEmployeeDialog());
        rightPanel.add(addButton);

        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- MAIN TABLE SECTION ---
        dashboardTable.setBorder(new EmptyBorder(20, 50, 10, 50));
        add(dashboardTable, BorderLayout.CENTER);

        // --- BOTTOM ACTION SECTION ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomButtonPanel.setOpaque(false);
        bottomButtonPanel.setBorder(new EmptyBorder(10, 50, 20, 50));

        JButton updateButton = new JButton("Update");
        styleColoredButton(updateButton, Color.BLACK, 120, 36);
        updateButton.setEnabled(false); 
        bottomButtonPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete");
        styleColoredButton(deleteButton, new Color(220, 20, 60), 120, 36);
        deleteButton.setEnabled(false); 
        bottomButtonPanel.add(deleteButton);

        add(bottomButtonPanel, BorderLayout.SOUTH);

        // Selection listener to toggle action buttons
        dashboardTable.getTable().getSelectionModel().addListSelectionListener(e -> {
            boolean isSelected = dashboardTable.getTable().getSelectedRow() != -1;
            updateButton.setEnabled(isSelected);
            deleteButton.setEnabled(isSelected);
        });

        // --- UPDATE LOGIC ---
        updateButton.addActionListener(e -> {
            int selectedRow = dashboardTable.getTable().getSelectedRow();
            if (selectedRow == -1) return;

            String employeeId = dashboardTable.getTable().getValueAt(selectedRow, 0).toString();
            String[] fullRow = fileHandler.getEmployeeById(employeeId);

            if (fullRow == null) {
                JOptionPane.showMessageDialog(null, "Employee record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Define which fields the HR manager is allowed to modify
            String[] editableFields = {
                "Last Name", "First Name", "Birthday", "Address", "Phone Number",
                "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
                "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
                "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
            };

            String[] headers = fileHandler.getEmployeeHeaders();

            // Setup update modal with theme gradient
            JPanel panel = new JPanel(new SpringLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };

            // ID is a primary key and should never be edited
            JLabel lblId = new JLabel("Employee ID *:");
            lblId.setForeground(Color.RED); 
            JTextField txtId = new JTextField(employeeId, 20);
            txtId.setEditable(false);
            txtId.setBackground(new Color(240, 240, 240));
            panel.add(lblId);
            panel.add(txtId);

            // Business Rule: Lock sensitive salary data if employee is already Regular
            int statusIndex = java.util.Arrays.asList(headers).indexOf("Status");
            boolean isRegular = statusIndex != -1 && fullRow[statusIndex].equalsIgnoreCase("Regular");

            JTextField[] textFields = new JTextField[editableFields.length];
            String[] originalValues = new String[editableFields.length];

            for (int i = 0; i < editableFields.length; i++) {
                String fieldName = editableFields[i];
                int fieldIndex = java.util.Arrays.asList(headers).indexOf(fieldName);

                boolean isLockedField = isRegular && (
                    fieldName.equals("Basic Salary") || fieldName.contains("Allowance") || 
                    fieldName.contains("Rate") || fieldName.equals("Rice Subsidy")
                );

                JLabel label = new JLabel(isLockedField ? fieldName + " *" : fieldName + ":");
                if (isLockedField) label.setForeground(Color.RED);

                JTextField textField = new JTextField(20);
                textField.setEditable(!isLockedField);

                // Input validation: Only allow digits and dashes for IDs and Phones
                if (fieldName.equals("Phone Number") || fieldName.contains("#")) {
                    ((AbstractDocument) textField.getDocument()).setDocumentFilter(new NumericAndDashFilter());
                }

                if (fieldIndex != -1 && fieldIndex < fullRow.length) {
                    textField.setText(fullRow[fieldIndex]);
                    originalValues[i] = fullRow[fieldIndex];
                }

                if (isLockedField) {
                    textField.setBackground(new Color(240, 240, 240));
                    textField.setToolTipText("Locked for Regular employees.");
                }

                panel.add(label);
                panel.add(textField);
                textFields[i] = textField;
            }

            JLabel note = new JLabel("* Locked field — cannot be edited.");
            note.setForeground(Color.RED);
            panel.add(note);
            panel.add(new JLabel()); 

            SpringUtilities.makeCompactGrid(panel, editableFields.length + 2, 2, 10, 10, 10, 10);

            JDialog dialog = new JDialog((Frame) null, "Update Employee", true);
            dialog.setSize(650, 750);
            dialog.setLocationRelativeTo(null);
            dialog.setLayout(new BorderLayout());
            dialog.add(new JScrollPane(panel), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false); 
            
            JButton saveBtn = new JButton("Save");
            styleMinimalButton(saveBtn, 120, 36);
            saveBtn.addActionListener(evt -> {
                boolean anyChanged = false;
                for (int i = 0; i < editableFields.length; i++) {
                    String newVal = textFields[i].getText().trim();
                    // Only push updates for fields that actually changed
                    if (!newVal.equals(originalValues[i]) && textFields[i].isEditable()) {
                        boolean updated = fileHandler.updateEmployeeField(employeeId, editableFields[i], newVal);
                        anyChanged |= updated;
                    }
                }
                if (anyChanged) { 
                    showCustomMessage("Employee record updated successfully.", "Updated");
                    dashboardTable.refreshTable(fileHandler.getEmployeeData());
                }
                dialog.dispose();
            });

            JButton cancelBtn = new JButton("Cancel");
            styleColoredButton(cancelBtn, new Color(128, 128, 128), 100, 36);
            cancelBtn.addActionListener(evt -> dialog.dispose());

            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });

        // --- DELETE LOGIC ---
        deleteButton.addActionListener(e -> {
            int selectedRow = dashboardTable.getTable().getSelectedRow();
            if (selectedRow == -1) return;

            String employeeId = (String) dashboardTable.getTable().getValueAt(selectedRow, 0);

            // Custom confirmation dialog
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Warning", true);
            dialog.setSize(400, 180);
            dialog.setLocationRelativeTo(this);
            
            JPanel content = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            content.setBorder(new EmptyBorder(20, 20, 20, 20));
            content.setLayout(new BorderLayout(10, 10));

            JLabel message = new JLabel("<html><center>Are you sure you want to delete<br>employee ID <b>" + employeeId + "</b>?</center></html>", JLabel.CENTER);
            message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            content.add(message, BorderLayout.CENTER);

            JPanel confirmButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            confirmButtonPanel.setOpaque(false);

            JButton yesButton = new JButton("Yes");
            styleColoredButton(yesButton, new Color(220, 20, 60), 80, 36);
            yesButton.addActionListener(ev -> {
                try {
                    fileHandler.deleteEmployeeById(employeeId);
                    dashboardTable.refreshTable(fileHandler.getEmployeeData());
                    showCustomMessage("Record Deleted Successfully", "Deleted");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage());
                }
                dialog.dispose();
            });

            JButton noButton = new JButton("No");
            styleColoredButton(noButton, Color.GRAY, 80, 36);
            noButton.addActionListener(ev -> dialog.dispose());

            confirmButtonPanel.add(yesButton);
            confirmButtonPanel.add(noButton);
            content.add(confirmButtonPanel, BorderLayout.SOUTH);
            
            dialog.setContentPane(content);
            dialog.setVisible(true);
        });
    }

    // Handles row-by-row data rendering for the table
    private void refreshEmployeeTable() {
        fileHandler.readEmployeeFile();
        java.util.List<String[]> updatedData = fileHandler.getEmployeeData();
        DefaultTableModel model = (DefaultTableModel) dashboardTable.getTable().getModel();
        model.setRowCount(0); 

        for (String[] row : updatedData) {
            // Select specific columns for the overview table
            model.addRow(new Object[]{row[0], row[1], row[2], row[6], row[7], row[8], row[9]});
        }
    }

    private void showSelectedEmployeeDetails() {
        Vector<Object> selectedEmployee = dashboardTable.getSelectedEmployeeFullDetails();
        if (selectedEmployee != null) {
            JFrame detailFrame = new JFrame("Employee Information - " + selectedEmployee.get(0));
            detailFrame.setSize(1000, 700);
            detailFrame.setLocationRelativeTo(null);
            
            ViewEmployeePanel detailPanel = new ViewEmployeePanel(selectedEmployee);
            detailFrame.add(detailPanel);
            detailFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee first.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Custom Themed Dialog for success/info messages
    private void showCustomMessage(String message, String title) {
        JDialog customDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        customDialog.setSize(400, 260);
        customDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        contentPanel.setLayout(new BorderLayout(10, 10));

        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        contentPanel.add(messageLabel, BorderLayout.CENTER);

        JPanel dialogButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dialogButtonPanel.setOpaque(false);

        JButton okButton = new JButton("OK");
        okButton.setForeground(Color.WHITE);
        okButton.setPreferredSize(new Dimension(80, 30));
        okButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });

        okButton.addActionListener(e -> customDialog.dispose());
        dialogButtonPanel.add(okButton);
        contentPanel.add(dialogButtonPanel, BorderLayout.SOUTH);
        
        customDialog.setContentPane(contentPanel);
        customDialog.setVisible(true);
    }

    private void showAddEmployeeDialog() {
        FileHandler handler = new FileHandler();
        handler.readEmployeeFile();

        JFrame frame = new JFrame("New Employee Entry");
        frame.setSize(550, 600);
        frame.setLocationRelativeTo(null);
        frame.add(new AddEmployeePanel(handler, () -> {
            dashboardTable.refreshTable(handler.getEmployeeData());
            frame.dispose();
        }));
        frame.setVisible(true);
    }

    // --- REUSABLE UI STYLING METHODS ---

    private void styleMinimalButton(JButton button, int width, int height) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 100, 180));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });
    }

    private void styleColoredButton(JButton button, Color bgColor, int width, int height) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Keeps background transparent to inherit from parent
    }

    public EmployeeTable getDashboardTable() {
        return this.dashboardTable; 
    }

    // Helper class for text field validation
    private static class NumericAndDashFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null && string.matches("[0-9\\-]*")) super.insertString(fb, offset, string, attr);
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null && text.matches("[0-9\\-]*")) super.replace(fb, offset, length, text, attrs);
        }
    }
}
 //   Object getDashboardTable() {
 //       return this.dashboardTable; // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
 //   }
// }