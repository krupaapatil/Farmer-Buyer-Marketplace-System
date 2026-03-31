package farmmarket.model;

public class CropPost {
    private final String cropPostId;
    private final String ownerUserId;
    private final String sellerName;
    private final String city;
    private final String phone;
    private final String cropType;
    private final int quantityKg;
    private final String unit;
    private final double pricePerKg;
    private final String notes;
    private final String status;
    private final String createdAt;

    public CropPost(String cropPostId, String ownerUserId, String sellerName, String city, String phone,
            String cropType, int quantityKg, String unit, double pricePerKg, String notes, String status,
            String createdAt) {
        this.cropPostId = cropPostId;
        this.ownerUserId = ownerUserId;
        this.sellerName = sellerName;
        this.city = city;
        this.phone = phone;
        this.cropType = cropType;
        this.quantityKg = quantityKg;
        this.unit = unit;
        this.pricePerKg = pricePerKg;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getCropPostId() {
        return cropPostId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getSellerName() {
        return sellerName;
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

    public double getPricePerKg() {
        return pricePerKg;
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
