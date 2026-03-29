package farmmarket.model;

import java.util.ArrayList;
import java.util.List;

public class DashboardSnapshot {
    private final AccountUser user;
    private final int cropPostsCount;
    private final int purchaseRequestsCount;
    private final int marketplaceCropCount;
    private final int marketplaceDemandCount;
    private final int availableMatchesCount;
    private final List<ActivityEntry> recentActivity;
    private final List<MatchRecord> topMatches;

    public DashboardSnapshot(AccountUser user, int cropPostsCount, int purchaseRequestsCount, int marketplaceCropCount,
            int marketplaceDemandCount, int availableMatchesCount, List<ActivityEntry> recentActivity,
            List<MatchRecord> topMatches) {
        this.user = user;
        this.cropPostsCount = cropPostsCount;
        this.purchaseRequestsCount = purchaseRequestsCount;
        this.marketplaceCropCount = marketplaceCropCount;
        this.marketplaceDemandCount = marketplaceDemandCount;
        this.availableMatchesCount = availableMatchesCount;
        this.recentActivity = new ArrayList<>(recentActivity);
        this.topMatches = new ArrayList<>(topMatches);
    }

    public AccountUser getUser() {
        return user;
    }

    public int getCropPostsCount() {
        return cropPostsCount;
    }

    public int getPurchaseRequestsCount() {
        return purchaseRequestsCount;
    }

    public int getMarketplaceCropCount() {
        return marketplaceCropCount;
    }

    public int getMarketplaceDemandCount() {
        return marketplaceDemandCount;
    }

    public int getAvailableMatchesCount() {
        return availableMatchesCount;
    }

    public List<ActivityEntry> getRecentActivity() {
        return new ArrayList<>(recentActivity);
    }

    public List<MatchRecord> getTopMatches() {
        return new ArrayList<>(topMatches);
    }
}
