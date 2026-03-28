package farmmarket.model;

import java.io.Serializable;

import farmmarket.interfaces.Persistable;

public class MatchRecord implements Persistable, Comparable<MatchRecord>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String buyerId;
    private final String buyerName;
    private final String farmerId;
    private final String farmerName;
    private final String cropType;
    private final String city;
    private final int availableQuantity;
    private final double pricePerUnit;
    private final double score;
    private final String status;

    public MatchRecord(Buyer buyer, Farmer farmer, double score, String status) {
        this.buyerId = buyer.getId();
        this.buyerName = buyer.getName();
        this.farmerId = farmer.getId();
        this.farmerName = farmer.getName();
        this.cropType = farmer.getCropType();
        this.city = farmer.getCity();
        this.availableQuantity = farmer.getQuantityAvailable();
        this.pricePerUnit = farmer.getPricePerUnit();
        this.score = score;
        this.status = status;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public double getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }

    public String[] toTableRow() {
        return new String[] {
            buyerId,
            buyerName,
            farmerId,
            farmerName,
            cropType,
            city,
            String.valueOf(availableQuantity),
            String.format("%.2f", pricePerUnit),
            String.format("%.2f", score),
            status
        };
    }

    @Override
    public String toCsv() {
        return String.join(",",
                buyerId,
                buyerName,
                farmerId,
                farmerName,
                cropType,
                city,
                String.valueOf(availableQuantity),
                String.valueOf(pricePerUnit),
                String.valueOf(score),
                status);
    }

    @Override
    public int compareTo(MatchRecord other) {
        return Double.compare(other.score, score);
    }
}
