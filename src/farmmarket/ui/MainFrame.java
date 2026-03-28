package farmmarket.ui;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import farmmarket.exceptions.DuplicateIdException;
import farmmarket.exceptions.InvalidDataException;
import farmmarket.service.FileManager;
import farmmarket.service.MarketplaceManager;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final MarketplaceManager manager;
    private final FileManager fileManager;
    private final FarmerPanel farmerPanel;
    private final BuyerPanel buyerPanel;
    private final MatchPanel matchPanel;
    private final ReportsPanel reportsPanel;
    private final JLabel statusLabel;

    public MainFrame() {
        this.manager = new MarketplaceManager();
        this.fileManager = new FileManager();

        loadInitialData();

        setTitle("Farmer-Buyer Marketplace System");
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        farmerPanel = new FarmerPanel(manager, this::refreshAllViews);
        buyerPanel = new BuyerPanel(manager, this::refreshAllViews);
        matchPanel = new MatchPanel(manager, fileManager, this::refreshAllViews);
        reportsPanel = new ReportsPanel(manager, fileManager, this::refreshAllViews);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Farmers", farmerPanel);
        tabbedPane.addTab("Buyers", buyerPanel);
        tabbedPane.addTab("Match Center", matchPanel);
        tabbedPane.addTab("Reports / Files", reportsPanel);

        statusLabel = new JLabel(" Ready");
        add(tabbedPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        refreshAllViews();
    }

    public void refreshAllViews() {
        farmerPanel.refreshData();
        buyerPanel.refreshData();
        matchPanel.refreshData();
        reportsPanel.refreshData();
        setStatus("Data refreshed successfully.");
    }

    public void setStatus(String message) {
        statusLabel.setText(" " + message);
    }

    private void loadInitialData() {
        try {
            manager.loadAll(fileManager);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load startup data: " + e.getMessage(),
                    "Startup Warning",
                    JOptionPane.WARNING_MESSAGE);
        } catch (InvalidDataException | DuplicateIdException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Startup data contains an issue: " + e.getMessage(),
                    "Data Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
