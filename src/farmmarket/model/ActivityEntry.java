package farmmarket.model;

public class ActivityEntry {
    private final String type;
    private final String recordId;
    private final String title;
    private final String subtitle;
    private final String status;
    private final String actionLabel;
    private final String createdAt;
    private final String route;

    public ActivityEntry(String type, String recordId, String title, String subtitle, String status,
            String actionLabel, String createdAt, String route) {
        this.type = type;
        this.recordId = recordId;
        this.title = title;
        this.subtitle = subtitle;
        this.status = status;
        this.actionLabel = actionLabel;
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

    public String getStatus() {
        return status;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getRoute() {
        return route;
    }
}
