package JavaAssignment;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentManagementSystem extends JFrame {
    private static final String URL = "jdbc:mariadb://localhost:3306/learn_java";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private DefaultTableModel studentTableModel;
    private Connection connection;
    private PreparedStatement insertStatement;

    private JTextField nameField, regNoField, mathMarksField, javaMarksField, phpMarksField;

    public StudentManagementSystem() {
        setTitle("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField(20);
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Reg Number:"));
        regNoField = new JTextField(20);
        inputPanel.add(regNoField);

        inputPanel.add(new JLabel("Math Marks:"));
        mathMarksField = new JTextField(20);
        inputPanel.add(mathMarksField);

        inputPanel.add(new JLabel("Java Marks:"));
        javaMarksField = new JTextField(20);
        inputPanel.add(javaMarksField);

        inputPanel.add(new JLabel("PHP Marks:"));
        phpMarksField = new JTextField(20);
        inputPanel.add(phpMarksField);

        JButton addButton = new JButton("Add Student");
        addButton.setBackground(Color.GREEN);
        addButton.addActionListener(e -> addStudent());
        addButton.setPreferredSize(new Dimension(200, 40)); // Increase width and height of Add button

        JButton exitButton = new JButton("Exit");
        exitButton.setBackground(Color.RED);
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setPreferredSize(new Dimension(200, 40)); // Increase width and height of Exit button

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 30));
        buttonPanel.setBorder(new EmptyBorder(0, 30, 20, 30));
        buttonPanel.add(addButton);
        buttonPanel.add(exitButton);

        String[] columnNames = {"Name", "Reg Number", "Average Marks"};
        studentTableModel = new DefaultTableModel(columnNames, 0);
        JTable studentTable = new JTable(studentTableModel);
        studentTable.setDefaultRenderer(Object.class, new HighlightRenderer());
        JScrollPane scrollPane = new JScrollPane(studentTable);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);

        initializeDatabase();
        loadExistingStudents();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            String insertSql = "INSERT INTO students (name, reg_number, math_marks, java_marks, php_marks) VALUES (?, ?, ?, ?, ?)";
            insertStatement = connection.prepareStatement(insertSql);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void loadExistingStudents() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name, reg_number, math_marks, java_marks, php_marks FROM students");
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String regNumber = resultSet.getString("reg_number");
                int mathMarks = resultSet.getInt("math_marks");
                int javaMarks = resultSet.getInt("java_marks");
                int phpMarks = resultSet.getInt("php_marks");
                double averageMarks = (mathMarks + javaMarks + phpMarks) / 3.0;

                Object[] rowData = {name, regNumber, averageMarks};
                studentTableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading existing students.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addStudent() {
        String name = nameField.getText().trim();
        String regNo = regNoField.getText().trim();
        String mathMarksStr = mathMarksField.getText().trim();
        String javaMarksStr = javaMarksField.getText().trim();
        String phpMarksStr = phpMarksField.getText().trim();

        if (name.isEmpty() || regNo.isEmpty() || mathMarksStr.isEmpty() || javaMarksStr.isEmpty() || phpMarksStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int mathMarks = Integer.parseInt(mathMarksStr);
            int javaMarks = Integer.parseInt(javaMarksStr);
            int phpMarks = Integer.parseInt(phpMarksStr);

            if (mathMarks < 0 || mathMarks > 100 || javaMarks < 0 || javaMarks > 100 || phpMarks < 0 || phpMarks > 100) {
                JOptionPane.showMessageDialog(this, "Marks should be between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            insertStatement.setString(1, name);
            insertStatement.setString(2, regNo);
            insertStatement.setInt(3, mathMarks);
            insertStatement.setInt(4, javaMarks);
            insertStatement.setInt(5, phpMarks);
            insertStatement.executeUpdate();

            double averageMarks = (mathMarks + javaMarks + phpMarks) / 3.0;
            Object[] rowData = {name, regNo, averageMarks};
            studentTableModel.addRow(rowData);
            clearFields();

        } catch (NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding student. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.setText("");
        regNoField.setText("");
        mathMarksField.setText("");
        javaMarksField.setText("");
        phpMarksField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentManagementSystem().setVisible(true);
        });
    }

    private static class HighlightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 2) { // Column with average marks
                double maxAverageMarks = getMaxAverageMarks(table);
                double cellValue = (double) value;
                if (cellValue == maxAverageMarks) {
                    c.setBackground(Color.YELLOW);
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }

        private double getMaxAverageMarks(JTable table) {
            double max = Double.MIN_VALUE;
            for (int i = 0; i < table.getRowCount(); i++) {
                double value = (double) table.getValueAt(i, 2);
                if (value > max) {
                    max = value;
                }
            }
            return max;
        }
    }
}
