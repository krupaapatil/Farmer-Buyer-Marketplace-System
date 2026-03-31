package farmmarket.model;

public class PurchaseRequest {
    private final String purchaseRequestId;
    private final String ownerUserId;
    private final String buyerName;
    private final String city;
    private final String phone;
    private final String cropType;
    private final int quantityKg;
    private final String unit;
    private final double maxBudget;
    private final String notes;
    private final String status;
    private final String createdAt;

    public PurchaseRequest(String purchaseRequestId, String ownerUserId, String buyerName, String city, String phone,
            String cropType, int quantityKg, String unit, double maxBudget, String notes, String status,
            String createdAt) {
        this.purchaseRequestId = purchaseRequestId;
        this.ownerUserId = ownerUserId;
        this.buyerName = buyerName;
        this.city = city;
        this.phone = phone;
        this.cropType = cropType;
        this.quantityKg = quantityKg;
        this.unit = unit;
        this.maxBudget = maxBudget;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getPurchaseRequestId() {
        return purchaseRequestId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getCity() {
        return city;
    }

    public String getLocation() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public String getCropType() {
        return cropType;
    }

    public int getQuantityKg() {
        return quantityKg;
    }

    public String getUnit() {
        return unit;
    }

    public double getMaxBudget() {
        return maxBudget;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
