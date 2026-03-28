package farmmarket.model;

import farmmarket.exceptions.InvalidDataException;
import farmmarket.util.ValidationUtil;

public class Buyer extends User {
    private static final long serialVersionUID = 1L;

    private String requiredCrop;
    private int requiredQuantity;
    private double maxBudget;

    public Buyer() {
    }

    public Buyer(String id, String name, String city, String phone, String requiredCrop)
            throws InvalidDataException {
        this(id, name, city, phone, requiredCrop, 0, 0.0);
    }

    public Buyer(String id, String name, String city, String phone, String requiredCrop,
            int requiredQuantity, double maxBudget) throws InvalidDataException {
        super(id, name, city, phone);
        setRequiredCrop(requiredCrop);
        setRequiredQuantity(requiredQuantity);
        setMaxBudget(maxBudget);
    }

    public String getRequiredCrop() {
        return requiredCrop;
    }

    public final void setRequiredCrop(String requiredCrop) throws InvalidDataException {
        this.requiredCrop = ValidationUtil.requireNonEmpty(requiredCrop, "Required crop");
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public final void setRequiredQuantity(int requiredQuantity) throws InvalidDataException {
        this.requiredQuantity = ValidationUtil.requireNonNegativeInt(requiredQuantity, "Required quantity");
    }

    public double getMaxBudget() {
        return maxBudget;
    }

    public final void setMaxBudget(double maxBudget) throws InvalidDataException {
        this.maxBudget = ValidationUtil.requireNonNegativeDouble(maxBudget, "Max budget");
    }

    @Override
    public String getRole() {
        return "Buyer";
    }

    @Override
    public String displayDetails() {
        return "Buyer ID: " + getId()
                + ", Name: " + getName()
                + ", City: " + getCity()
                + ", Crop Needed: " + requiredCrop
                + ", Quantity Needed: " + requiredQuantity
                + ", Max Budget: " + maxBudget;
    }

    @Override
    public String toString() {
        return getName() + " [" + getId() + "] - needs " + requiredCrop + " in " + getCity();
    }

    @Override
    public String toCsv() {
        return String.join(",",
                getId(),
                getName(),
                getCity(),
                getPhone(),
                requiredCrop,
                String.valueOf(requiredQuantity),
                String.valueOf(maxBudget));
    }

    @Override
    public String[] toTableRow() {
        return new String[] {
            getId(),
            getName(),
            getCity(),
            getPhone(),
            requiredCrop,
            String.valueOf(requiredQuantity),
            String.format("%.2f", maxBudget)
        };
    }
}
