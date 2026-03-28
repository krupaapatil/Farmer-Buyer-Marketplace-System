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
import farmmarket.model.Farmer;
import farmmarket.service.MarketplaceManager;
import farmmarket.util.AppConstants;

public class FarmerPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final MarketplaceManager manager;
    private final Runnable refreshCallback;

    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField cityField;
    private final JTextField phoneField;
    private final JComboBox<String> cropComboBox;
    private final JTextField quantityField;
    private final JTextField priceField;

    private final JComboBox<String> searchCropComboBox;
    private final JTextField searchCityField;
    private final JTable farmerTable;

    public FarmerPanel(MarketplaceManager manager, Runnable refreshCallback) {
        this.manager = manager;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Farmer"));

        idField = new JTextField();
        nameField = new JTextField();
        cityField = new JTextField();
        phoneField = new JTextField();
        cropComboBox = new JComboBox<>(AppConstants.CROP_TYPES);
        quantityField = new JTextField();
        priceField = new JTextField();

        formPanel.add(new JLabel("Farmer ID"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("City"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("Phone"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Crop Type"));
        formPanel.add(cropComboBox);
        formPanel.add(new JLabel("Quantity Available"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("Price Per Unit"));
        formPanel.add(priceField);

        JButton addButton = new JButton("Add Farmer");
        JButton resetButton = new JButton("Reset");

        addButton.addActionListener(event -> addFarmer());
        resetButton.addActionListener(event -> clearForm());

        formPanel.add(addButton);
        formPanel.add(resetButton);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Farmers"));
        searchCropComboBox = new JComboBox<>(createSearchCropOptions());
        searchCityField = new JTextField(15);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        searchButton.addActionListener(event -> searchFarmers());
        showAllButton.addActionListener(event -> refreshData());

        searchPanel.add(new JLabel("Crop"));
        searchPanel.add(searchCropComboBox);
        searchPanel.add(new JLabel("City"));
        searchPanel.add(searchCityField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        farmerTable = new JTable();
        farmerTable.setFillsViewportHeight(true);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(farmerTable), BorderLayout.CENTER);
    }

    public void refreshData() {
        updateTable(manager.getFarmers());
    }

    private void addFarmer() {
        try {
            Farmer farmer = new Farmer(
                    idField.getText(),
                    nameField.getText(),
                    cityField.getText(),
                    phoneField.getText(),
                    (String) cropComboBox.getSelectedItem(),
                    Integer.parseInt(quantityField.getText().trim()),
                    Double.parseDouble(priceField.getText().trim()));

            manager.addFarmer(farmer);
            clearForm();
            refreshCallback.run();
            JOptionPane.showMessageDialog(this, "Farmer added successfully.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity and price must be numeric values.");
        } catch (InvalidDataException | DuplicateIdException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchFarmers() {
        String selectedCrop = (String) searchCropComboBox.getSelectedItem();
        String cropFilter = "All".equalsIgnoreCase(selectedCrop) ? "" : selectedCrop;
        List<Farmer> farmers = manager.searchFarmers(cropFilter, searchCityField.getText());
        updateTable(farmers);
    }

    private void updateTable(List<Farmer> farmers) {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Name");
        columnNames.add("City");
        columnNames.add("Phone");
        columnNames.add("Crop");
        columnNames.add("Quantity");
        columnNames.add("Price/Unit");

        Vector<Vector<Object>> rows = new Vector<>();
        for (Farmer farmer : farmers) {
            Vector<Object> row = new Vector<>();
            for (String value : farmer.toTableRow()) {
                row.add(value);
            }
            rows.add(row);
        }

        farmerTable.setModel(new DefaultTableModel(rows, columnNames) {
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
        priceField.setText("");
        cropComboBox.setSelectedIndex(0);
    }
}
