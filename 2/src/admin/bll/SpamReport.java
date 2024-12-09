package bll;

import java.sql.Date;

public class SpamReport {
    private String reportId;
    private String reportedBy;
    private String reportedUser;
    private String reason;
    private String status;
    private Date createdAt;

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }
    public void setReportId(String reportId) {
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
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
