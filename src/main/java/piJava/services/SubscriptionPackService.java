package piJava.services;

import piJava.entities.SubscriptionPack;
import piJava.utils.MyDataBase;
import piJava.utils.SubscriptionCurrency;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionPackService implements ICrud<SubscriptionPack> {

    private final Connection con = MyDataBase.getInstance().getConnection();

    @Override
    public List<SubscriptionPack> show() throws SQLException {
        return getActivePacks();
    }

    public List<SubscriptionPack> getAll() throws SQLException {
        List<SubscriptionPack> list = new ArrayList<>();
        String sql = "SELECT * FROM subscription_pack ORDER BY is_active DESC, is_popular DESC, price ASC";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[SubscriptionPackService] Error getting packs: " + e.getMessage());
            return list;
        }
        return list;
    }

    public List<SubscriptionPack> getActivePacks() throws SQLException {
        List<SubscriptionPack> list = new ArrayList<>();
        String sql = "SELECT * FROM subscription_pack WHERE is_active = 1 ORDER BY is_popular DESC, price ASC";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[SubscriptionPackService] Error getting active packs: " + e.getMessage());
            return list;
        }
        return list;
    }

    public SubscriptionPack getById(int id) throws SQLException {
        String sql = "SELECT * FROM subscription_pack WHERE id = ?";
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

    public List<SubscriptionPack> searchByName(String keyword) throws SQLException {
        List<SubscriptionPack> list = new ArrayList<>();
        String sql = "SELECT * FROM subscription_pack WHERE name LIKE ? ORDER BY is_active DESC, is_popular DESC, price ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    @Override
    public void add(SubscriptionPack pack) throws SQLException {
        String sql = "INSERT INTO subscription_pack (name, description, price, currency, duration_days, features, is_active, icon, color, is_popular) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindPack(ps, pack, false);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    pack.setId(keys.getInt(1));
                }
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM subscription_pack WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void edit(SubscriptionPack pack) throws SQLException {
        String sql = "UPDATE subscription_pack SET name = ?, description = ?, price = ?, currency = ?, duration_days = ?, " +
                "features = ?, is_active = ?, icon = ?, color = ?, is_popular = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bindPack(ps, pack, true);
            ps.executeUpdate();
        }
    }

    private void bindPack(PreparedStatement ps, SubscriptionPack pack, boolean includeId) throws SQLException {
        ps.setString(1, pack.getName());
        ps.setString(2, pack.getDescription());
        ps.setBigDecimal(3, pack.getPrice() != null ? pack.getPrice() : BigDecimal.ZERO);
        ps.setString(4, SubscriptionCurrency.normalize(pack.getCurrency()));
        ps.setInt(5, pack.getDurationDays());
        ps.setString(6, pack.getFeatures());
        ps.setInt(7, pack.isActive() ? 1 : 0);
        ps.setString(8, pack.getIcon());
        ps.setString(9, pack.getColor());
        ps.setInt(10, pack.isPopular() ? 1 : 0);
        if (includeId) {
            ps.setInt(11, pack.getId());
        }
    }

    private SubscriptionPack mapResultSet(ResultSet rs) throws SQLException {
        return new SubscriptionPack(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                SubscriptionCurrency.normalize(rs.getString("currency")),
                rs.getInt("duration_days"),
                rs.getString("features"),
                rs.getInt("is_active") != 0,
                rs.getString("icon"),
                rs.getString("color"),
                rs.getInt("is_popular") != 0
        );
    }
}

