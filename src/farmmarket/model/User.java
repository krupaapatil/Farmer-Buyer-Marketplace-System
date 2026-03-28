package farmmarket.model;

import java.io.Serializable;

import farmmarket.exceptions.InvalidDataException;
import farmmarket.interfaces.Persistable;
import farmmarket.util.ValidationUtil;

public abstract class User implements Persistable, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String city;
    private String phone;

    protected User() {
    }

    protected User(String id, String name, String city, String phone) throws InvalidDataException {
        setId(id);
        setName(name);
        setCity(city);
        setPhone(phone);
    }

    public String getId() {
        return id;
    }

    public final void setId(String id) throws InvalidDataException {
        this.id = ValidationUtil.requireNonEmpty(id, "ID");
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) throws InvalidDataException {
        this.name = ValidationUtil.requireNonEmpty(name, "Name");
    }

    public String getCity() {
        return city;
    }

    public final void setCity(String city) throws InvalidDataException {
        this.city = ValidationUtil.requireNonEmpty(city, "City");
    }

    public String getPhone() {
        return phone;
    }

    public final void setPhone(String phone) throws InvalidDataException {
        this.phone = ValidationUtil.requireNonEmpty(phone, "Phone");
    }

    public abstract String getRole();

    public abstract String displayDetails();

    public abstract String[] toTableRow();
}
