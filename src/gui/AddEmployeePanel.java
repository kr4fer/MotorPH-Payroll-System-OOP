package gui;

import model.FileHandler;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;

/**
 * UI for the "Add New Employee" screen. 
 * Dynamically builds a form based on CSV headers and handles validation.
 */
public class AddEmployeePanel extends JPanel {

    private final FileHandler fileHandler;
    private final Map<String, JComponent> fieldMap = new LinkedHashMap<>();
    private final JButton submitButton = new JButton("Add Employee");
    private final JButton backButton = new JButton("Back");
    private final Runnable onEmployeeAdded;

    // Fields that we need but might be missing from the base CSV headers
    private final String[] additionalFields = {"Birthday", "Phone Number"};

    private JPanel formPanel;
    private JPanel bottomPanel;

    public AddEmployeePanel(FileHandler fileHandler, Runnable onEmployeeAdded) {
        this.fileHandler = fileHandler;
        this.onEmployeeAdded = onEmployeeAdded;

        // General panel setup
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setOpaque(false);

        // Nested panel to prevent the form from stretching vertically
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        formContainer.add(formPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setPreferredSize(new Dimension(500, 450));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        // Fetch headers to build the form inputs
        fileHandler.readEmployeeFile();
        String[] headersFromFile = fileHandler.getEmployeeHeaders();

        // Merge file headers with our hardcoded extra fields
        LinkedHashMap<String, Boolean> finalHeaders = new LinkedHashMap<>();
        for (String header : headersFromFile) {
            finalHeaders.put(header, isRequired(header));
        }
        for (String extra : additionalFields) {
            finalHeaders.putIfAbsent(extra, true);
        }

        // Generate the UI components for each field
        for (Map.Entry<String, Boolean> entry : finalHeaders.entrySet()) {
            String header = entry.getKey();
            boolean required = entry.getValue();

            JPanel labelPanel = new JPanel(new BorderLayout());
            labelPanel.setOpaque(false);
            JLabel label = new JLabel(header + ":");

            if (required) {
                JLabel asterisk = new JLabel("*");
                asterisk.setForeground(Color.RED);
                asterisk.setFont(asterisk.getFont().deriveFont(Font.BOLD));
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setToolTipText("Required field");
                labelPanel.add(label, BorderLayout.WEST);
                labelPanel.add(asterisk, BorderLayout.EAST);
            } else {
                label.setForeground(Color.GRAY);
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
                labelPanel.add(label, BorderLayout.WEST);
            }

            JComponent inputField;
            switch (header.toLowerCase()) {
                case "employee #" -> {
                    JTextField field = new JTextField();
                    ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericDocumentFilter(5));
                    addTooltipOnFocus(field, "Enter 5-digit ID");
                    inputField = field;
                }
                case "birthday" -> {
                    DatePickerSettings settings = new DatePickerSettings();
                    settings.setFormatForDatesCommonEra("yyyy/MM/dd");
                    DatePicker datePicker = new DatePicker(settings);
                    addTooltipOnFocus(datePicker.getComponentDateTextField(), "Format: yyyy/MM/dd");
                    inputField = datePicker;
                }
                case "phone number" -> {
                    inputField = createFormattedField("####-###-####");
                    addTooltipOnFocus((JTextField) inputField, "Format: 0000-000-0000");
                }
                case "sss #" -> {
                    inputField = createFormattedField("##-#######-#");
                    addTooltipOnFocus((JTextField) inputField, "Format: 00-0000000-0");
                }
                case "philhealth #" -> {
                    inputField = createFormattedField("####-####-####");
                    addTooltipOnFocus((JTextField) inputField, "Format: 0000-0000-0000");
                }
                case "tin #" -> {
                    inputField = createFormattedField("###-###-###-###");
                    addTooltipOnFocus((JTextField) inputField, "Format: 000-000-000-000");
                }
                case "pag-ibig #" -> {
                    inputField = createFormattedField("####-####-####");
                    addTooltipOnFocus((JTextField) inputField, "Format: 0000-0000-0000");
                }
                case "status" -> {
                    inputField = new JComboBox<>(new String[]{"Regular", "Probationary"});
                }
                case "position" -> {
                    inputField = new JComboBox<>(new String[]{"Staff", "HR Manager", "Finance Manager", "IT Support", "CEO"});
                }
                default -> {
                    JTextField textField = new JTextField();
                    addTooltipOnFocus(textField, "Enter " + header);
                    inputField = textField;
                }
            }

            formPanel.add(labelPanel);
            formPanel.add(inputField);
            fieldMap.put(header, inputField);
        }

        // Button Styling
        submitButton.setBackground(Color.BLACK);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);

        backButton.setBackground(Color.BLACK);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);

        submitButton.addActionListener(this::addEmployee);
        backButton.addActionListener(e -> {
            if (onEmployeeAdded != null) onEmployeeAdded.run();
        });

        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.add(new JLabel("* Required fields"));
        bottomPanel.add(backButton);
        bottomPanel.add(submitButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        // Soft pink-to-peach gradient
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0xFFD1DC),
                0, h / 2, new Color(0xFFE4CC)
        );

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();
    }

    private void addEmployee(ActionEvent e) {
        String[] newRow = new String[fieldMap.size()];
        int index = 0;
        boolean hasError = false;
        StringBuilder errorMessages = new StringBuilder();

        // Clear previous error highlights
        for (JComponent field : fieldMap.values()) {
            field.setBorder(UIManager.getBorder("TextField.border"));
        }

        for (Map.Entry<String, JComponent> entry : fieldMap.entrySet()) {
            String header = entry.getKey();
            JComponent component = entry.getValue();
            String value = "";

            // Handle different component types for data extraction
            if (component instanceof JTextField) {
                value = ((JTextField) component).getText().trim();
            } else if (component instanceof DatePicker) {
                value = ((DatePicker) component).getDate() != null
                        ? ((DatePicker) component).getDate().toString()
                        : "";
            } else if (component instanceof JComboBox<?>) {
                value = ((JComboBox<?>) component).getSelectedItem().toString();
            }

            // Validation: Required check
            if (isRequired(header) && value.isEmpty()) {
                component.setBorder(new LineBorder(Color.RED, 2));
                hasError = true;
                errorMessages.append("- ").append(header).append(" is required.\n");
            }

            // Validation: Numeric format check (stripping non-digits might be better but checking regex for now)
            if ((header.equalsIgnoreCase("Employee #") ||
                    header.equalsIgnoreCase("Phone Number") ||
                    header.equalsIgnoreCase("SSS #") ||
                    header.equalsIgnoreCase("Philhealth #") ||
                    header.equalsIgnoreCase("TIN #") ||
                    header.equalsIgnoreCase("Pag-ibig #")) &&
                    !value.matches("\\d+")) {
                // This regex might fail if the user leaves mask characters like '-' in JFormattedTextField
                // Checking anyway to ensure integrity
                component.setBorder(new LineBorder(Color.RED, 2));
                hasError = true;
                errorMessages.append("- ").append(header).append(" must be numeric.\n");
            }

            // Validation: Duplicate Employee ID check
             if (header.equalsIgnoreCase("Employee #") && employeeNumberExists(value)) {                component.setBorder(new LineBorder(Color.RED, 2));
                component.requestFocus();
                JOptionPane.showMessageDialog(this,
                        "Employee Number already exists!",
                        "Duplicate Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            newRow[index++] = value;
        }

        if (hasError) {
            JOptionPane.showMessageDialog(this, "Please fix the following:\n" + errorMessages,
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Commit to file
        if (fileHandler.appendEmployeeToFile(newRow)) {
            fileHandler.readEmployeeFile(); // Refresh local list
            JOptionPane.showMessageDialog(this, "✅ Employee added successfully!");
            clearFields();
            if (onEmployeeAdded != null) onEmployeeAdded.run();
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "❌ Failed to add employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Forces tooltips to show up when navigating via keyboard (tabbing)
    private void addTooltipOnFocus(JTextField textField, String tooltipText) {
        textField.setToolTipText(tooltipText);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ToolTipManager.sharedInstance().mouseMoved(
                    new java.awt.event.MouseEvent(
                        textField,
                        java.awt.event.MouseEvent.MOUSE_MOVED,
                        System.currentTimeMillis(),
                        0, 1, 1, 0, false
                    )
                );
            }
        });
    }

    private boolean employeeNumberExists(String empNum) {
        for (String[] row : fileHandler.getEmployeeData()) {
            if (row.length > 0 && row[0].equalsIgnoreCase(empNum)) {
                return true;
            }
        }
        return false;
    }

    private void clearFields() {
        for (Map.Entry<String, JComponent> entry : fieldMap.entrySet()) {
            JComponent field = entry.getValue();
            if (field instanceof JTextField) {
                ((JTextField) field).setText("");
            } else if (field instanceof DatePicker) {
                ((DatePicker) field).clear();
            } else if (field instanceof JComboBox<?>) {
                ((JComboBox<?>) field).setSelectedIndex(0);
            }
            field.setBorder(UIManager.getBorder("TextField.border"));
        }
    }

    // Configuration for which fields the system refuses to save without data
    private boolean isRequired(String header) {
        return header.equalsIgnoreCase("Employee #")
                || header.equalsIgnoreCase("Last Name")
                || header.equalsIgnoreCase("First Name")
                || header.equalsIgnoreCase("Birthday")
                || header.equalsIgnoreCase("Phone Number")
                || header.equalsIgnoreCase("Basic Salary") 
                || header.equalsIgnoreCase("Gross Semi-monthly Rate")
                || header.equalsIgnoreCase("SSS #")
                || header.equalsIgnoreCase("Philhealth #")
                || header.equalsIgnoreCase("TIN #")
                || header.equalsIgnoreCase("Pag-ibig #");
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        private final int maxLength;
        
        public NumericDocumentFilter(int maxLength) {
            this.maxLength = maxLength;
        }
        
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string != null && string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text != null && text.matches("\\d+")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    private JFormattedTextField createFormattedField(String mask) {
        try {
            MaskFormatter formatter = new MaskFormatter(mask);
            formatter.setPlaceholderCharacter('_'); 
            formatter.setAllowsInvalid(false);      
            formatter.setOverwriteMode(true);
            return new JFormattedTextField(formatter);
        
        } catch (java.text.ParseException e) {
            // Fallback if mask is malformed
            return new JFormattedTextField(); 
        }
    }
}
