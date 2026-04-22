package piJava.services;

import piJava.entities.StudentIntelligenceProfile;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StudentIntelligenceProfileService implements ICrud<StudentIntelligenceProfile>{

    Connection con;
    public StudentIntelligenceProfileService() {
        con = MyDataBase.getInstance().getConnection();
    }


    @Override
    public List<StudentIntelligenceProfile> show() throws SQLException {

        return List.of();
    }

    public StudentIntelligenceProfile showLatestPerUser(int id) throws SQLException {
        String sql = "SELECT * FROM student_intelligence_profile " +
                "WHERE user_id = ? ORDER BY analyzed_at DESC LIMIT 1";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            StudentIntelligenceProfile profile = new StudentIntelligenceProfile();
            profile.setId(rs.getInt("id"));
            profile.setUserId(rs.getInt("user_id"));
            profile.setAnalyzedAt(rs.getTimestamp("analyzed_at"));
            profile.setAbandonmentRate(rs.getDouble("abandonment_rate"));
            profile.setCompletionRate(rs.getDouble("completion_rate"));
            profile.setAverageStartDelayMinutes(rs.getDouble("average_start_delay_minutes"));
            profile.setAverageCompletionDurationMinutes(rs.getDouble("average_completion_duration_minutes"));
            profile.setPauseFrequency(rs.getDouble("pause_frequency"));
            profile.setMostProductiveHour(rs.getInt("most_productive_hour"));
            profile.setMostProductiveDayOfWeek(rs.getInt("most_productive_day_of_week"));
            profile.setWeeklyProductivitySummary(rs.getString("weekly_productivity_summary"));
            profile.setBehavioralAdvice(rs.getString("behavioral_advice"));

            return profile;
        }

        return null;

    }

    @Override
    public void add(StudentIntelligenceProfile p) throws SQLException {
        String sql = "INSERT INTO student_intelligence_profile " +
                "(analyzed_at, abandonment_rate, completion_rate, average_start_delay_minutes, " +
                "average_completion_duration_minutes, pause_frequency, most_productive_hour, " +
                "most_productive_day_of_week, abandonment_rate_by_type, completion_rate_by_type, " +
                "average_start_delay_by_type, global_risk_trend, forgotten_task_probability, " +
                "overdue_probability, weekly_productivity_summary, behavioral_advice, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setTimestamp(1, p.getAnalyzedAt());

        ps.setObject(2, p.getAbandonmentRate());
        ps.setObject(3, p.getCompletionRate());
        ps.setObject(4, p.getAverageStartDelayMinutes());
        ps.setObject(5, p.getAverageCompletionDurationMinutes());
        ps.setObject(6, p.getPauseFrequency());

        ps.setObject(7, p.getMostProductiveHour());
        ps.setObject(8, p.getMostProductiveDayOfWeek());

        ps.setString(9, toJson(p.getAbandonmentRateByType()));
        ps.setString(10, toJson(p.getCompletionRateByType()));
        ps.setString(11, toJson(p.getAverageStartDelayByType()));

        ps.setObject(12, p.getGlobalRiskTrend());
        ps.setObject(13, p.getForgottenTaskProbability());
        ps.setObject(14, p.getOverdueProbability());

        ps.setString(15, p.getWeeklyProductivitySummary());
        ps.setString(16, p.getBehavioralAdvice());

        ps.setInt(17, p.getUserId());

        ps.executeUpdate();
    }

    private String toJson(Map<String, Double> map) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return map == null ? null : mapper.writeValueAsString(map);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void delete(int id) throws SQLException {

    }

    @Override
    public void edit(StudentIntelligenceProfile studentIntelligenceProfile) throws SQLException {

    }
}