package farmmarket.model;

public class CropPost {
    private final String cropPostId;
    private final String ownerUserId;
    private final String sellerName;
    private final String city;
    private final String phone;
    private final String cropType;
    private final int quantityKg;
    private final double pricePerKg;
    private final String notes;
    private final String createdAt;

    public CropPost(String cropPostId, String ownerUserId, String sellerName, String city, String phone, String cropType,
            int quantityKg, double pricePerKg, String notes, String createdAt) {
        this.cropPostId = cropPostId;
        this.ownerUserId = ownerUserId;
        this.sellerName = sellerName;
        this.city = city;
        this.phone = phone;
        this.cropType = cropType;
        this.quantityKg = quantityKg;
        this.pricePerKg = pricePerKg;
        this.notes = notes;
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

    public String getPhone() {
        return phone;
    }

    public String getCropType() {
        return cropType;
    }

    public int getQuantityKg() {
        return quantityKg;
    }

    public double getPricePerKg() {
        return pricePerKg;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
