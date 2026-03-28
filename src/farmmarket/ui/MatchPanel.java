package farmmarket.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import farmmarket.exceptions.NoMatchFoundException;
import farmmarket.model.Buyer;
import farmmarket.model.MatchRecord;
import farmmarket.service.FileManager;
import farmmarket.service.MarketplaceManager;

public class MatchPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final MarketplaceManager manager;
    private final FileManager fileManager;
    private final Runnable refreshCallback;
    private final JComboBox<Buyer> buyerComboBox;
    private final JTable matchTable;
    private final JTextArea reportArea;

    public MatchPanel(MarketplaceManager manager, FileManager fileManager, Runnable refreshCallback) {
        this.manager = manager;
        this.fileManager = fileManager;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Generate Matches"));

        buyerComboBox = new JComboBox<>();
        buyerComboBox.setPrototypeDisplayValue(new BuyerDisplayPlaceholder());

        JButton generateSelectedButton = new JButton("Match Selected Buyer");
        JButton generateAllButton = new JButton("Generate All Matches");
        JButton exportButton = new JButton("Export Matches");

        generateSelectedButton.addActionListener(event -> generateSelectedMatch());
        generateAllButton.addActionListener(event -> generateAllMatches());
        exportButton.addActionListener(event -> exportMatches());

        controlsPanel.add(buyerComboBox);
        controlsPanel.add(generateSelectedButton);
        controlsPanel.add(generateAllButton);
        controlsPanel.add(exportButton);

        matchTable = new JTable();
        matchTable.setFillsViewportHeight(true);

        reportArea = new JTextArea(8, 20);
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);

        add(controlsPanel, BorderLayout.NORTH);
        add(new JScrollPane(matchTable), BorderLayout.CENTER);
        add(new JScrollPane(reportArea), BorderLayout.SOUTH);
    }

    public void refreshData() {
        refreshBuyerChoices();
        updateTable(manager.getLastGeneratedMatches());
        reportArea.setText(manager.buildMatchReport());
    }

    private void refreshBuyerChoices() {
        Buyer selected = (Buyer) buyerComboBox.getSelectedItem();
        buyerComboBox.removeAllItems();
        for (Buyer buyer : manager.getBuyers()) {
            buyerComboBox.addItem(buyer);
        }
        if (selected != null) {
            buyerComboBox.setSelectedItem(selected);
        }
    }

    private void generateSelectedMatch() {
        Buyer buyer = (Buyer) buyerComboBox.getSelectedItem();
        if (buyer == null) {
            JOptionPane.showMessageDialog(this, "Please add or load a buyer first.");
            return;
        }

        try {
            manager.generateMatchesForBuyer(buyer);
            refreshCallback.run();
            JOptionPane.showMessageDialog(this, "Matches generated for buyer " + buyer.getName() + ".");
        } catch (NoMatchFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No Match", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void generateAllMatches() {
        try {
            manager.generateAllMatches();
            refreshCallback.run();
            JOptionPane.showMessageDialog(this, "All compatible matches generated successfully.");
        } catch (NoMatchFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No Match", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportMatches() {
        try {
            List<MatchRecord> matches = manager.getLastGeneratedMatches();
            if (matches.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Generate matches before exporting.");
                return;
            }
            fileManager.saveMatches(matches);
            reportArea.setText(manager.buildMatchReport());
            JOptionPane.showMessageDialog(this, "Match report exported to " + fileManager.getMatchesFile());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<MatchRecord> matches) {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Buyer ID");
        columnNames.add("Buyer Name");
        columnNames.add("Farmer ID");
        columnNames.add("Farmer Name");
        columnNames.add("Crop");
        columnNames.add("City");
        columnNames.add("Available Qty");
        columnNames.add("Price/Unit");
        columnNames.add("Score");
        columnNames.add("Status");

        Vector<Vector<Object>> rows = new Vector<>();
        for (MatchRecord match : matches) {
            Vector<Object> row = new Vector<>();
            for (String value : match.toTableRow()) {
                row.add(value);
            }
            rows.add(row);
        }

        matchTable.setModel(new DefaultTableModel(rows, columnNames) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    private static class BuyerDisplayPlaceholder extends Buyer {
        private static final long serialVersionUID = 1L;

        @Override
        public String toString() {
            return "Select Buyer";
        }
    }
}
