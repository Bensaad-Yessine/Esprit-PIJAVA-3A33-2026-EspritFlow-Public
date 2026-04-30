package piJava.services;

import piJava.entities.Vote;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoteService implements ICrud<Vote> {

    private final Connection con = MyDataBase.getInstance().getConnection();

    // ─── SHOW (SELECT ALL) ───────────────────────────────────────
    @Override
    public List<Vote> show() throws SQLException {
        return getAll();
    }

    public List<Vote> getAll() throws SQLException {
        List<Vote> list = new ArrayList<>();
        String sql = "SELECT * FROM vote";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[VoteService] Error getting all: " + e.getMessage());
            return list;
        }
        return list;
    }

    // ─── GET VOTES BY PROPOSITION ID ────────────────────────────
    public List<Vote> getByPropositionId(int propositionId) throws SQLException {
        List<Vote> list = new ArrayList<>();
        String sql = "SELECT * FROM vote WHERE proposition_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, propositionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[VoteService] Error getting by proposition: " + e.getMessage());
            return list;
        }
        return list;
    }

    // ─── GET VOTE BY USER AND PROPOSITION ───────────────────────
    public Vote getByUserAndProposition(int userId, int propositionId) throws SQLException {
        String sql = "SELECT * FROM vote WHERE user_id = ? AND proposition_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, propositionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[VoteService] Error getting by user and proposition: " + e.getMessage());
        }
        return null;
    }

    // ─── ADD (INSERT) ───────────────────────────────────────────
    @Override
    public void add(Vote vote) throws SQLException {
        String sql = "INSERT INTO vote (user_id, proposition_id, vote, voted_at) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, vote.getUserId());
            ps.setInt(2, vote.getPropositionId());
            ps.setString(3, vote.getType());
            ps.setTimestamp(4, Timestamp.valueOf(vote.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vote.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // ─── EDIT (UPDATE) ──────────────────────────────────────────
    @Override
    public void edit(Vote vote) throws SQLException {
        String sql = "UPDATE vote SET vote = ?, voted_at = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vote.getType());
            ps.setTimestamp(2, Timestamp.valueOf(vote.getUpdatedAt()));
            ps.setInt(3, vote.getId());
            ps.executeUpdate();
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vote WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── GET BY ID ───────────────────────────────────────────────
    public Vote getById(int id) throws SQLException {
        String sql = "SELECT * FROM vote WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // ─── Result Set Mapper ────────────────────────────────────────
    private Vote mapResultSet(ResultSet rs) throws SQLException {
        return new Vote(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("proposition_id"),
                rs.getString("vote"),
                rs.getTimestamp("voted_at") != null ? rs.getTimestamp("voted_at").toLocalDateTime() : null,
                rs.getTimestamp("voted_at") != null ? rs.getTimestamp("voted_at").toLocalDateTime() : null
        );
    }

    // ─── CALCULATE VOTE STATISTICS ──────────────────────────────
    public VoteStats calculateVoteStats(int propositionId) throws SQLException {
        int pour = 0;
        int contre = 0;
        int abstention = 0;

        String sql = "SELECT vote, COUNT(*) as count FROM vote WHERE proposition_id = ? GROUP BY vote";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, propositionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("vote");
                    int count = rs.getInt("count");
                    if ("pour".equalsIgnoreCase(type)) {
                        pour = count;
                    } else if ("contre".equalsIgnoreCase(type)) {
                        contre = count;
                    } else if ("abstention".equalsIgnoreCase(type)) {
                        abstention = count;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[VoteService] Error calculating vote stats: " + e.getMessage());
        }

        return new VoteStats(pour, contre, abstention);
    }

    // ─── VOTE STATISTICS CLASS ──────────────────────────────────
    public static class VoteStats {
        public int pour;
        public int contre;
        public int abstention;

        public VoteStats(int pour, int contre, int abstention) {
            this.pour = pour;
            this.contre = contre;
            this.abstention = abstention;
        }

        public int getTotalVotants() {
            return pour + contre; // Abstention excluded
        }

        public String checkProposalStatus() {
            if (getTotalVotants() == 0) {
                return "En attente";
            }

            double pourPercent = (double) pour / getTotalVotants() * 100;
            double contrePercent = (double) contre / getTotalVotants() * 100;

            if (pourPercent > 50) {
                return "Acceptée";
            } else if (contrePercent > 50) {
                return "Rejetée";
            } else {
                return "Reportée";
            }
        }
    }
}
