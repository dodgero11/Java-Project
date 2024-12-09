package bll;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dao.SpamReportDAO;

public class SpamService {
    private SpamReportDAO SpamReportDAO;

    public SpamService() {
        this.SpamReportDAO = new SpamReportDAO();
    }
    
    public List<SpamReport> getSpamReports() throws SQLException {
        return SpamReportDAO.getSpamReports();
    }
}