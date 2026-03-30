/*
 * MotorPH HR Management System
 */
package gui;

import dao.EmployeeDAO;
import service.EmployeeService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import model.FileHandler;

public class HRDashboard extends JFrame {

    private final EmployeeService employeeService;

    // Table components for data display
    private JTable table;
    private DefaultTableModel tableModel; // Full data model
    private JButton employeeBtn;
    private JButton logoutBtn;

    // Mapping for employee.csv structure
    private static final String[] HEADERS = {
        "Employee #", "Last Name", "First Name", "Birthday", "Address",
        "Phone Number", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #",
        "Status", "Position", "Immediate Supervisor", "Basic Salary",
        "Rice Subsidy", "Phone Allowance", "Clothing Allowance",
        "Gross Semi-monthly Rate", "Hourly Rate"
    };

    public HRDashboard(String username) {
        employeeService = new EmployeeService(new EmployeeDAO());

        // Window Configuration
        setTitle("MotorPH — HR Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 500));

        // Assemble Layout
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentPanel(), BorderLayout.CENTER);

        // Sidebar Navigation Logic
        employeeBtn.addActionListener(e -> refreshTable());
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        refreshTable();
        setVisible(true);
    }

    /**
     * Builds the white sidebar containing the user profile and primary navigation
     */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // User Profile Header
        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.add(new JLabel(loadIcon("/assets/userprofile.png", 40, 40)), BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("HR Staff");
        JLabel roleLabel = new JLabel("Human Resources");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.GRAY);
        namePanel.add(nameLabel);
        namePanel.add(roleLabel);
        profilePanel.add(namePanel, BorderLayout.CENTER);

        // Navigation Group
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel generalLabel = new JLabel("General");
        generalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        generalLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        generalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        employeeBtn = makeNavBtn("Employees", "employee.png");
        logoutBtn   = makeNavBtn("Log-out",   "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(Box.createVerticalStrut(20));
        navPanel.add(generalLabel);
        navPanel.add(employeeBtn);

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel,      BorderLayout.CENTER);
        sidebar.add(logoutBtn,     BorderLayout.SOUTH);
        return sidebar;
    }

    /**
     * Main content area: includes search bar, the employee table, and action buttons
     */
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(29, 69, 143));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Employee Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        // --- SEARCH BAR SECTION ---
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        
        searchField.setText("Search by name or ID...");
        searchField.setForeground(Color.GRAY);
        
        // Placeholder logic for search field
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search by name or ID...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search by name or ID...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        JButton viewDetailsBtn = makeActionBtn("View Details", new Color(100, 60, 180));
        viewDetailsBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String empId = table.getValueAt(selectedRow, 0).toString();
            String[] empData = employeeService.getEmployeeById(empId);
            
            if (empData != null) {
                java.util.Vector<Object> dataVector = new java.util.Vector<>();
                for (String s : empData) dataVector.add(s);
                
                JDialog dialog = new JDialog(this, "Employee Details", true);
                dialog.setSize(900, 600);
                dialog.setLocationRelativeTo(this);
                dialog.add(new ViewEmployeePanel(dataVector));
                dialog.setVisible(true);
            }
        });

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);
        rightBar.add(viewDetailsBtn);
        rightBar.add(searchField);
        searchField.setPreferredSize(new Dimension(220, 34));

        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);

        // --- TABLE CONFIGURATION ---
        // We only show a subset of columns in the dashboard to avoid clutter
        String[] visibleHeaders = {"Employee #", "Last Name", "First Name", "Status", "Position", "Basic Salary"};
        int[] dataIndices = {0, 1, 2, 10, 11, 13};

        tableModel = new DefaultTableModel(HEADERS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        DefaultTableModel displayModel = new DefaultTableModel(visibleHeaders, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(displayModel);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(100, 149, 237));
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Column width optimization
        int[] colWidths = {100, 140, 140, 90, 240, 130};
        for (int i = 0; i < colWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        // Custom renderer for zebra striping and cell padding
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 255));
                    setForeground(new Color(40, 40, 40));
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        });

        // Header Styling
        table.getTableHeader().setBackground(new Color(20, 50, 110));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Document Listener for real-time search filtering
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String query = searchField.getText().trim().toLowerCase();
                displayModel.setRowCount(0);
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String id = tableModel.getValueAt(i, 0).toString().toLowerCase();
                    String last = tableModel.getValueAt(i, 1).toString().toLowerCase();
                    String first = tableModel.getValueAt(i, 2).toString().toLowerCase();
                    
                    if (id.contains(query) || last.contains(query) || first.contains(query)) {
                        Object[] row = new Object[visibleHeaders.length];
                        for (int j = 0; j < dataIndices.length; j++) {
                            row[j] = tableModel.getValueAt(i, dataIndices[j]);
                        }
                        displayModel.addRow(row);
                    }
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        // --- ACTION BUTTONS (CRUD) ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);

        JButton addBtn    = makeActionBtn("+ Add Employee",  new Color(56, 142, 60));
        JButton updateBtn = makeActionBtn("Update",        new Color(30, 144, 255));
        JButton deleteBtn = makeActionBtn("Delete",        new Color(211, 47, 47));

        addBtn.addActionListener(e    -> openAddDialog());
        updateBtn.addActionListener(e -> openUpdateDialog());
        deleteBtn.addActionListener(e -> deleteSelected());

        actionPanel.add(addBtn);
        actionPanel.add(updateBtn);
        actionPanel.add(deleteBtn);

        panel.add(topBar,      BorderLayout.NORTH);
        panel.add(scrollPane,  BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog(this, "Add New Employee", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        model.FileHandler fh = new model.FileHandler();

        // Use the centralized AddEmployeePanel
        AddEmployeePanel addPanel = new AddEmployeePanel(fh, () -> {
            refreshTable();    
            dialog.dispose();  
        });

        dialog.add(addPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void openUpdateDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Update Employee", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(15, 20, 15, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JTextField[] fields = new JTextField[HEADERS.length];
        for (int i = 0; i < HEADERS.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            form.add(new JLabel(HEADERS[i] + ":"), gc);
            gc.gridx = 1; gc.weightx = 1;
            fields[i] = new JTextField(tableModel.getValueAt(selectedRow, i).toString(), 20);
            if (i == 0) fields[i].setEditable(false); // ID Lock
            form.add(fields[i], gc);
        }

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton saveBtn   = makeActionBtn("Save",   new Color(30, 144, 255));
        JButton cancelBtn = makeActionBtn("Cancel", new Color(150, 150, 150));
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String empId = fields[0].getText().trim();
            boolean allUpdated = true;
            for (int i = 1; i < HEADERS.length; i++) {
                boolean updated = employeeService.updateField(empId, HEADERS[i], fields[i].getText().trim());
                if (!updated) allUpdated = false;
            }
            if (allUpdated) {
                refreshTable();
                JOptionPane.showMessageDialog(dialog, "Employee updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Check data types or connection and try again.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(formScroll, BorderLayout.CENTER);
        dialog.add(btnPanel,   BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String empId = tableModel.getValueAt(selectedRow, 0).toString();
        String empName = tableModel.getValueAt(selectedRow, 2) + " " + tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + empName + "?", "Confirm Delete", 
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (employeeService.deleteEmployee(empId)) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error during deletion.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Syncs both the hidden full model and the visible display model with the service layer
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        DefaultTableModel displayModel = (DefaultTableModel) table.getModel();
        displayModel.setRowCount(0);

        int[] dataIndices = {0, 1, 2, 10, 11, 13};

        for (String[] row : employeeService.getAllEmployees()) {
            tableModel.addRow(row);
            Object[] displayRow = new Object[dataIndices.length];
            for (int j = 0; j < dataIndices.length; j++) {
                displayRow[j] = row[dataIndices[j]];
            }
            displayModel.addRow(displayRow);
        }
    }

    private JButton makeNavBtn(String text, String icon) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon("/assets/" + icon, 20, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private JButton makeActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return btn;
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}













