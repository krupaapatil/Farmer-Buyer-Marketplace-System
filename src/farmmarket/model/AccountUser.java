package farmmarket.model;

public class AccountUser {
    private final String userId;
    private final String fullName;
    private final String email;
    private final String city;
    private final String phone;
    private final String role;
    private final String createdAt;
    private final String updatedAt;

    public AccountUser(String userId, String fullName, String email, String city, String phone, String role,
            String createdAt, String updatedAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.city = city;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
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

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
