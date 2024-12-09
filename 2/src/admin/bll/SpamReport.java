package bll;

import java.sql.Date;

public class SpamReport {
    private int reportId;
    private String reportedBy;
    private String reportedUser;
    private String reason;
    private Date createdAt;

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }
    public String getReportedBy() {
        return reportedBy;
    }
    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }
    public String getReportedUser() {
        return reportedUser;
    }
    public void setReportedUser(String reportedUser) {
        this.reportedUser = reportedUser;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
