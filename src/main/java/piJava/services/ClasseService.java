package piJava.services;

import piJava.entities.Classe;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasseService {

    private final Connection con = MyDataBase.getInstance().getConnection();

    public List<Classe> getAllClasses() {
        List<Classe> list = new ArrayList<>();
        String sql = "SELECT * FROM classe";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Classe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("niveau"),
                        rs.getString("anneeuniversitaire"),
                        rs.getString("description"),
                        rs.getString("filiere"),
                        rs.getObject("user_id", Integer.class)
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAllClasses: " + e.getMessage());
        }
        return list;
    }
}