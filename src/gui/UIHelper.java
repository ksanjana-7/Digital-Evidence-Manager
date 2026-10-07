package gui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIHelper {

    public static JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return comboBox;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        return label;
    }

    public static JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textField.setPreferredSize(new Dimension(200, 30));
        return textField;
    }

    public static JTextField createTextField(String text) {
        JTextField textField = new JTextField(text);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textField.setPreferredSize(new Dimension(200, 30));
        return textField;
    }

    public static void styleTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
    }
}
