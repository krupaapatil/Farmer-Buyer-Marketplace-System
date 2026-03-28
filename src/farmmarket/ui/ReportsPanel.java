package farmmarket.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import farmmarket.exceptions.DuplicateIdException;
import farmmarket.exceptions.InvalidDataException;
import farmmarket.service.FileManager;
import farmmarket.service.MarketplaceManager;

public class ReportsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final MarketplaceManager manager;
    private final FileManager fileManager;
    private final Runnable refreshCallback;
    private final JTextArea reportArea;
    private final JLabel fileInfoLabel;

    public ReportsPanel(MarketplaceManager manager, FileManager fileManager, Runnable refreshCallback) {
        this.manager = manager;
        this.fileManager = fileManager;
        this.refreshCallback = refreshCallback;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("File Operations"));

        JButton saveButton = new JButton("Save All");
        JButton loadButton = new JButton("Load All");
        JButton backupButton = new JButton("Create Backup");
        JButton restoreButton = new JButton("Restore Backup");
        JButton refreshButton = new JButton("Refresh Report");

        saveButton.addActionListener(event -> saveAll());
        loadButton.addActionListener(event -> loadAll());
        backupButton.addActionListener(event -> exportBackup());
        restoreButton.addActionListener(event -> importBackup());
        refreshButton.addActionListener(event -> refreshData());

        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(backupButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(refreshButton);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setLineWrap(true);
        reportArea.setWrapStyleWord(true);

        fileInfoLabel = new JLabel();

        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(reportArea), BorderLayout.CENTER);
        add(fileInfoLabel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        reportArea.setText(manager.buildSummaryReport());
        fileInfoLabel.setText("Files: " + fileManager.getFarmersFile()
                + " | " + fileManager.getBuyersFile()
                + " | " + fileManager.getMatchesFile()
                + " | Backup: " + fileManager.getBackupFile());
    }

    private void saveAll() {
        try {
            manager.saveAll(fileManager);
            refreshData();
            JOptionPane.showMessageDialog(this, "All records saved successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAll() {
        try {
            manager.loadAll(fileManager);
            refreshCallback.run();
            JOptionPane.showMessageDialog(this, "Records loaded successfully.");
        } catch (IOException | InvalidDataException | DuplicateIdException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportBackup() {
        try {
            manager.exportBackup(fileManager);
            refreshData();
            JOptionPane.showMessageDialog(this, "Serialized backup created successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Backup Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importBackup() {
        try {
            manager.importBackup(fileManager);
            refreshCallback.run();
            JOptionPane.showMessageDialog(this, "Backup restored successfully.");
        } catch (IOException | ClassNotFoundException | DuplicateIdException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Restore Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
