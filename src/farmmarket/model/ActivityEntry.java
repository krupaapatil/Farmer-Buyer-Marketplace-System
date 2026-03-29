package farmmarket.model;

public class ActivityEntry {
    private final String type;
    private final String recordId;
    private final String title;
    private final String subtitle;
    private final String createdAt;
    private final String route;

    public ActivityEntry(String type, String recordId, String title, String subtitle, String createdAt, String route) {
        this.type = type;
        this.recordId = recordId;
        this.title = title;
        this.subtitle = subtitle;
        this.createdAt = createdAt;
        this.route = route;
    }

    public String getType() {
        return type;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getRoute() {
        return route;
    }
}
