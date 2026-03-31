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
    private final int requestedQuantity;
    private final int matchedQuantity;
    private final double pricePerUnit;
    private final double buyerBudget;
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
        this.requestedQuantity = buyer.getRequiredQuantity();
        this.matchedQuantity = Math.min(farmer.getQuantityAvailable(), buyer.getRequiredQuantity());
        this.pricePerUnit = farmer.getPricePerUnit();
        this.buyerBudget = buyer.getMaxBudget();
        this.score = score;
        this.status = status;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public String getCropType() {
        return cropType;
    }

    public String getCity() {
        return city;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getMatchedQuantity() {
        return matchedQuantity;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public double getBuyerBudget() {
        return buyerBudget;
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
            String.valueOf(requestedQuantity),
            String.valueOf(matchedQuantity),
            String.format("%.2f", pricePerUnit),
            String.format("%.2f", buyerBudget),
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
                String.valueOf(requestedQuantity),
                String.valueOf(matchedQuantity),
                String.valueOf(pricePerUnit),
                String.valueOf(buyerBudget),
                String.valueOf(score),
                status);
    }

    @Override
    public int compareTo(MatchRecord other) {
        return Double.compare(other.score, score);
    }
}
