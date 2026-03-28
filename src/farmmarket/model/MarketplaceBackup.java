package farmmarket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MarketplaceBackup implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Farmer> farmers;
    private final List<Buyer> buyers;
    private final List<MatchRecord> matches;

    public MarketplaceBackup(List<Farmer> farmers, List<Buyer> buyers, List<MatchRecord> matches) {
        this.farmers = new ArrayList<>(farmers);
        this.buyers = new ArrayList<>(buyers);
        this.matches = new ArrayList<>(matches);
    }

    public List<Farmer> getFarmers() {
        return new ArrayList<>(farmers);
    }

    public List<Buyer> getBuyers() {
        return new ArrayList<>(buyers);
    }

    public List<MatchRecord> getMatches() {
        return new ArrayList<>(matches);
    }
}
