package farmmarket.model;

import farmmarket.exceptions.InvalidDataException;
import farmmarket.interfaces.Matchable;
import farmmarket.util.ValidationUtil;

public class Farmer extends User implements Matchable {
    private static final long serialVersionUID = 1L;

    private String cropType;
    private int quantityAvailable;
    private double pricePerUnit;

    public Farmer() {
    }

    public Farmer(String id, String name, String city, String phone, String cropType)
            throws InvalidDataException {
        this(id, name, city, phone, cropType, 0, 0.0);
    }

    public Farmer(String id, String name, String city, String phone, String cropType,
            int quantityAvailable, double pricePerUnit) throws InvalidDataException {
        super(id, name, city, phone);
        setCropType(cropType);
        setQuantityAvailable(quantityAvailable);
        setPricePerUnit(pricePerUnit);
    }

    public String getCropType() {
        return cropType;
    }

    public final void setCropType(String cropType) throws InvalidDataException {
        this.cropType = ValidationUtil.requireNonEmpty(cropType, "Crop type");
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public final void setQuantityAvailable(int quantityAvailable) throws InvalidDataException {
        this.quantityAvailable = ValidationUtil.requireNonNegativeInt(quantityAvailable, "Quantity");
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public final void setPricePerUnit(double pricePerUnit) throws InvalidDataException {
        this.pricePerUnit = ValidationUtil.requireNonNegativeDouble(pricePerUnit, "Price per unit");
    }

    @Override
    public String getRole() {
        return "Farmer";
    }

    @Override
    public boolean isCompatibleWith(Buyer buyer) {
        return buyer != null
                && ValidationUtil.matchesIgnoreCase(cropType, buyer.getRequiredCrop())
                && ValidationUtil.matchesIgnoreCase(getCity(), buyer.getCity())
                && pricePerUnit <= buyer.getMaxBudget()
                && quantityAvailable > 0;
    }

    @Override
    public double calculateMatchScore(Buyer buyer) {
        if (!isCompatibleWith(buyer)) {
            return 0;
        }

        double score = 80;
        int requestedQuantity = buyer.getRequiredQuantity();
        int availableQuantity = getQuantityAvailable();

        if (requestedQuantity > 0 && availableQuantity > 0) {
            double gap = Math.abs(requestedQuantity - availableQuantity);
            double max = Math.max(requestedQuantity, availableQuantity);
            double quantityScore = 20 * (1 - (gap / max));
            score += Math.max(0, quantityScore);
        }

        return Math.round(score * 100.0) / 100.0;
    }

    @Override
    public String displayDetails() {
        return "Farmer ID: " + getId()
                + ", Name: " + getName()
                + ", City: " + getCity()
                + ", Crop: " + cropType
                + ", Quantity: " + quantityAvailable
                + ", Price/Unit: " + pricePerUnit;
    }

    @Override
    public String toString() {
        return getName() + " [" + getId() + "] - " + cropType + " @ " + getCity();
    }

    @Override
    public String toCsv() {
        return String.join(",",
                getId(),
                getName(),
                getCity(),
                getPhone(),
                cropType,
                String.valueOf(quantityAvailable),
                String.valueOf(pricePerUnit));
    }

    @Override
    public String[] toTableRow() {
        return new String[] {
            getId(),
            getName(),
            getCity(),
            getPhone(),
            cropType,
            String.valueOf(quantityAvailable),
            String.format("%.2f", pricePerUnit)
        };
    }
}
