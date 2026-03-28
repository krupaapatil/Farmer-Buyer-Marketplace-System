package farmmarket.app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import farmmarket.ui.MainFrame;

public final class FarmerBuyerMarketplaceApp {
    private FarmerBuyerMarketplaceApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            applySystemLookAndFeel();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
