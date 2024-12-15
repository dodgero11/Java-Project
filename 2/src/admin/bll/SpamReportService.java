package bll;

import dao.SpamReportDAO;
import java.sql.SQLException;
import java.util.List;

public class SpamReportService {
    private SpamReportDAO SpamReportDAO;

    public SpamReportService() {
        this.SpamReportDAO = new SpamReportDAO();
    }
    
    public List<SpamReport> getSpamReports() throws SQLException {
        return SpamReportDAO.getSpamReports();
    }

    public SpamReport getSpamReportById(String reportId) throws SQLException {
        return SpamReportDAO.getSpamReportById(reportId);
    }

    public void dismissReport(String reportId) throws SQLException {
        SpamReportDAO.dismissReport(reportId);
    }

    public boolean reportSpam(String reporterUsername, String reportedUsername, String description) throws Exception {
        return SpamReportDAO.reportSpam(reporterUsername, reportedUsername, description);
    }
}