package farmmarket.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import farmmarket.exceptions.DuplicateIdException;
import farmmarket.exceptions.InvalidDataException;
import farmmarket.model.Buyer;
import farmmarket.service.MarketplaceManager;
import farmmarket.util.AppConstants;

public class BuyerPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final MarketplaceManager manager;
    private final Runnable refreshCallback;

    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField cityField;
    private final JTextField phoneField;
    private final JComboBox<String> cropComboBox;
    private final JTextField quantityField;
    private final JTextField budgetField;

    private final JComboBox<String> searchCropComboBox;
    private final JTextField searchCityField;
    private final JTable buyerTable;

    public BuyerPanel(MarketplaceManager manager, Runnable refreshCallback) {
        this.manager = manager;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Buyer"));

        idField = new JTextField();
        nameField = new JTextField();
        cityField = new JTextField();
        phoneField = new JTextField();
        cropComboBox = new JComboBox<>(AppConstants.CROP_TYPES);
        quantityField = new JTextField();
        budgetField = new JTextField();

        formPanel.add(new JLabel("Buyer ID"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("City"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("Phone"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Required Crop"));
        formPanel.add(cropComboBox);
        formPanel.add(new JLabel("Required Quantity"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("Max Budget"));
        formPanel.add(budgetField);

        JButton addButton = new JButton("Add Buyer");
        JButton resetButton = new JButton("Reset");

        addButton.addActionListener(event -> addBuyer());
        resetButton.addActionListener(event -> clearForm());

        formPanel.add(addButton);
        formPanel.add(resetButton);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Buyers"));
        searchCropComboBox = new JComboBox<>(createSearchCropOptions());
        searchCityField = new JTextField(15);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchButton.addActionListener(event -> searchBuyers());
        showAllButton.addActionListener(event -> refreshData());

        searchPanel.add(new JLabel("Crop"));
        searchPanel.add(searchCropComboBox);
        searchPanel.add(new JLabel("City"));
        searchPanel.add(searchCityField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        buyerTable = new JTable();
        buyerTable.setFillsViewportHeight(true);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(buyerTable), BorderLayout.CENTER);
    }

    public void refreshData() {
        updateTable(manager.getBuyers());
    }

    private void addBuyer() {
        try {
            Buyer buyer = new Buyer(
                    idField.getText(),
                    nameField.getText(),
                    cityField.getText(),
                    phoneField.getText(),
                    (String) cropComboBox.getSelectedItem(),
                    Integer.parseInt(quantityField.getText().trim()),
                    Double.parseDouble(budgetField.getText().trim()));

            manager.addBuyer(buyer);
            clearForm();
            refreshCallback.run();
            JOptionPane.showMessageDialog(this, "Buyer added successfully.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity and budget must be numeric values.");
        } catch (InvalidDataException | DuplicateIdException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchBuyers() {
        String selectedCrop = (String) searchCropComboBox.getSelectedItem();
        String cropFilter = "All".equalsIgnoreCase(selectedCrop) ? "" : selectedCrop;
        List<Buyer> buyers = manager.searchBuyers(cropFilter, searchCityField.getText());
        updateTable(buyers);
    }

    private void updateTable(List<Buyer> buyers) {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Name");
        columnNames.add("City");
        columnNames.add("Phone");
        columnNames.add("Crop Needed");
        columnNames.add("Quantity Needed");
        columnNames.add("Max Budget");

        Vector<Vector<Object>> rows = new Vector<>();
        for (Buyer buyer : buyers) {
            Vector<Object> row = new Vector<>();
            for (String value : buyer.toTableRow()) {
                row.add(value);
            }
            rows.add(row);
        }

        buyerTable.setModel(new DefaultTableModel(rows, columnNames) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private String[] createSearchCropOptions() {
        String[] searchOptions = new String[AppConstants.CROP_TYPES.length + 1];
        searchOptions[0] = "All";
        System.arraycopy(AppConstants.CROP_TYPES, 0, searchOptions, 1, AppConstants.CROP_TYPES.length);
        return searchOptions;
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        cityField.setText("");
        phoneField.setText("");
        quantityField.setText("");
        budgetField.setText("");
        cropComboBox.setSelectedIndex(0);
    }
}
