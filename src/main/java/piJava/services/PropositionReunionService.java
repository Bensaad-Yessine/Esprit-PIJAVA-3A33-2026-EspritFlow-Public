package piJava.services;

import piJava.entities.PropositionReunion;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropositionReunionService implements ICrud<PropositionReunion> {

    private final Connection con = MyDataBase.getInstance().getConnection();
    private static final int MAX_TITRE_LENGTH = 60;
    private static final int MAX_LIEU_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 240;

    // ─── SHOW (SELECT ALL) ───────────────────────────────────────
    @Override
    public List<PropositionReunion> show() throws SQLException {
        return getAll();
    }

    public List<PropositionReunion> getAll() throws SQLException {
        List<PropositionReunion> list = new ArrayList<>();
        String sql = "SELECT * FROM proposition_reunion";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PropositionReunionService] Error getting all: " + e.getMessage());
            return list;
        }
        return list;
    }

    // ─── GET BY GROUPE ID ───────────────────────────────────────
    public List<PropositionReunion> getByGroupeId(int groupeId) throws SQLException {
        List<PropositionReunion> list = new ArrayList<>();
        String sql = "SELECT * FROM proposition_reunion WHERE id_groupe_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PropositionReunionService] Error getting by groupe: " + e.getMessage());
            return list;
        }
        return list;
    }

    // ─── ADD (INSERT) ────────────────────────────────────────────
    @Override
    public void add(PropositionReunion proposition) throws SQLException {
        String sql = "INSERT INTO proposition_reunion (proposition_id, titre, date_reunion, heure_debut, heure_fin, " +
                     "lieu, description, status, date_creation, date_fin_vote, nbr_vote_accept, id_groupe_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, proposition.getPropositionId());
            ps.setString(2, truncate(proposition.getTitre(), MAX_TITRE_LENGTH));
            ps.setDate(3, java.sql.Date.valueOf(proposition.getDateReunion()));
            ps.setTime(4, java.sql.Time.valueOf(proposition.getHeureDebut()));
            ps.setTime(5, java.sql.Time.valueOf(proposition.getHeureFin()));
            ps.setString(6, truncate(proposition.getLieu(), MAX_LIEU_LENGTH));
            ps.setString(7, truncate(proposition.getDescription(), MAX_DESCRIPTION_LENGTH));
            ps.setString(8, proposition.getStatut() != null ? proposition.getStatut() : "En attente");
            ps.setDate(9, proposition.getDateCreation() != null ? java.sql.Date.valueOf(proposition.getDateCreation()) : java.sql.Date.valueOf(java.time.LocalDate.now()));
            ps.setDate(10, proposition.getDateFinVote() != null ? java.sql.Date.valueOf(proposition.getDateFinVote()) : null);
            ps.setInt(11, proposition.getNbrVoteAccept());
            ps.setInt(12, proposition.getIdGroupeId());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    proposition.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────
    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM proposition_reunion WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ─── EDIT (UPDATE) ───────────────────────────────────────────
    @Override
    public void edit(PropositionReunion proposition) throws SQLException {
        String sql = "UPDATE proposition_reunion SET titre = ?, date_reunion = ?, heure_debut = ?, heure_fin = ?, " +
                     "lieu = ?, description = ?, date_fin_vote = ?, nbr_vote_accept = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, proposition.getTitre());
            ps.setDate(2, java.sql.Date.valueOf(proposition.getDateReunion()));
            ps.setTime(3, java.sql.Time.valueOf(proposition.getHeureDebut()));
            ps.setTime(4, java.sql.Time.valueOf(proposition.getHeureFin()));
            ps.setString(5, proposition.getLieu());
            ps.setString(6, proposition.getDescription());
            ps.setDate(7, proposition.getDateFinVote() != null ? java.sql.Date.valueOf(proposition.getDateFinVote()) : null);
            ps.setInt(8, proposition.getNbrVoteAccept());
            ps.setInt(9, proposition.getId());
            ps.executeUpdate();
        }
    }

    // ─── GET BY ID ───────────────────────────────────────────────
    public PropositionReunion getById(int id) throws SQLException {
        String sql = "SELECT * FROM proposition_reunion WHERE id = ?";

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
    private PropositionReunion mapResultSet(ResultSet rs) throws SQLException {
        try {
            String statut = rs.getString("statut");
            return new PropositionReunion(
                    rs.getInt("id"),
                    rs.getInt("proposition_id"),
                    rs.getString("titre"),
                    rs.getDate("date_reunion") != null ? rs.getDate("date_reunion").toLocalDate() : null,
                    rs.getTime("heure_debut") != null ? rs.getTime("heure_debut").toLocalTime() : null,
                    rs.getTime("heure_fin") != null ? rs.getTime("heure_fin").toLocalTime() : null,
                    rs.getString("lieu"),
                    rs.getString("description"),
                    statut,
                    rs.getDate("date_creation") != null ? rs.getDate("date_creation").toLocalDate() : null,
                    rs.getDate("date_fin_vote") != null ? rs.getDate("date_fin_vote").toLocalDate() : null,
                    rs.getInt("nbr_vote_accept"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                    rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
                    rs.getInt("id_groupe_id")
            );
        } catch (SQLException e) {
            // If statut column doesn't exist, set default value
            return new PropositionReunion(
                    rs.getInt("id"),
                    rs.getInt("proposition_id"),
                    rs.getString("titre"),
                    rs.getDate("date_reunion") != null ? rs.getDate("date_reunion").toLocalDate() : null,
                    rs.getTime("heure_debut") != null ? rs.getTime("heure_debut").toLocalTime() : null,
                    rs.getTime("heure_fin") != null ? rs.getTime("heure_fin").toLocalTime() : null,
                    rs.getString("lieu"),
                    rs.getString("description"),
                    "En attente",
                    rs.getDate("date_creation") != null ? rs.getDate("date_creation").toLocalDate() : null,
                    rs.getDate("date_fin_vote") != null ? rs.getDate("date_fin_vote").toLocalDate() : null,
                    rs.getInt("nbr_vote_accept"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                    rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
                    rs.getInt("id_groupe_id")
            );
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        if (cleaned.length() <= maxLength) {
            return cleaned;
        }
        return cleaned.substring(0, Math.max(0, maxLength - 3)).trim() + "...";
    }
}
