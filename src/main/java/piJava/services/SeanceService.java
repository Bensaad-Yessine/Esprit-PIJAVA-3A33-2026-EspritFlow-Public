package piJava.services;

// ============================================================================
// IMPORTATIONS DES CLASSES DU PROJET
// ============================================================================

import piJava.entities.Seance;        // Entité représentant une séance
import piJava.utils.MyDataBase;       // Classe de connexion à la base de données

// ============================================================================
// IMPORTATIONS JDBC (Java Database Connectivity)
// ============================================================================

import java.sql.*;                   // API standard pour la gestion de la BD
import java.util.ArrayList;          // Liste dynamique pour stocker les résultats
import java.util.List;              // Interface Liste

/**
 * Service pour gérer les opérations CRUD sur les séances
 * Cette classe implémente l'interface ICrud<Seance> pour standardiser les opérations
 */
public class SeanceService implements ICrud<Seance> {

    // ============================================================================
    // VARIABLES D'INSTANCE
    // ============================================================================

    // Connexion à la base de données (obtenue via MyDataBase singleton)
    private Connection con;

    /**
     * Constructeur par défaut
     * Récupère la connexion à la base de données lors de l'instanciation
     */
    public SeanceService() {
        con = MyDataBase.getInstance().getConnection();
    }

    /**
     * Méthode privée pour obtenir/réinitialiser la connexion
     * @return L'objet Connection prêt à être utilisé
     * @throws SQLException Si la connexion échoue
     */
    private Connection requireConnection() throws SQLException {
        con = MyDataBase.getInstance().getConnection();
        if (con == null) {
            throw new SQLException("Database connection unavailable.");
        }
        return con;
    }

    // ============================================================================
    // MÉTHODES DE L'INTERFACE ICrud
    // ============================================================================

    /**
     * Méthode interface - Retourne toutes les séances
     * Alias pour getAllSeances() pour respects l'interface ICrud
     * @return Liste de toutes les séances
     * @throws SQLException En cas d'erreur BD
     */
    @Override
    public List<Seance> show() throws SQLException {
        return getAllSeances();
    }

    // ============================================================================
    // MÉTHODES SPÉCIFIQUES AU SERVICE
    // ============================================================================

    /**
     * Récupère toutes les séances de la base de données
     * @return Liste complète des séances
     * @throws SQLException En cas d'erreur de base de données
     */
    public List<Seance> getAllSeances() throws SQLException {
        // Créer une liste vide pour stocker les résultats
        List<Seance> list = new ArrayList<>();
        
        // Requête SQL: SELECT * FROM seance - Récupère toutes les colonnes
        String sql = "SELECT * FROM seance";
        
        // Utilisation de try-with-resources pour fermer automatiquement
        try (Statement st = requireConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            // Parcourir chaque ligne du résultat
            while (rs.next()) {
                // Convertir chaque ligne en objet Seance et ajouter à la liste
                list.add(mapResultSet(rs));
            }
        }
        
        // Retourner la liste des séances
        return list;
    }

    /**
     * Ajoute une nouvelle séance dans la base de données
     * @param seance L'objet Seance à ajouter
     * @throws SQLException En cas d'erreur de base de données
     */
    @Override
    public void add(Seance seance) throws SQLException {
        // Requête SQL paramétrée pour l'insertion
        // Les ? représentent les valeurs à injecter
        String sql = "INSERT INTO seance (jour, type_seance, mode, heure_debut, heure_fin, created_at, " +
                   "salle_id, matiere_id, classe_id, qr_token, qr_expires_at, qr_url, google_event_id) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = requireConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // ----------------------------------------------------------------
            // Étape 1: Définir les valeurs des paramètres
            // ----------------------------------------------------------------
            ps.setString(1, seance.getJour());                    // Jour de la semaine
            ps.setString(2, seance.getTypeSeance());           // Type (CM, TD, TP...)
            ps.setString(3, seance.getMode());                // Mode (Présentiel/Distanciel)
            ps.setTimestamp(4, seance.getHeureDebut());       // Horaire début
            ps.setTimestamp(5, seance.getHeureFin());          // Horaire fin
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis())); // Date création actuelle
            ps.setInt(7, seance.getSalleId());               // ID de la salle
            ps.setInt(8, seance.getMatiereId());             // ID de la matière
            ps.setInt(9, seance.getClasseId());              // ID de la classe
            ps.setString(10, seance.getQrToken());             // Token QR Code
            ps.setTimestamp(11, seance.getQrExpiresAt());    // Expiration QR
            ps.setString(12, seance.getQrUrl());            // URL du QR
            ps.setString(13, seance.getGoogleEventId());    // ID événement Google
            
            // Exécuter l'insertion
            ps.executeUpdate();
            
            // ----------------------------------------------------------------
            // Étape 2: Récupérer l'ID auto-généré
            // ----------------------------------------------------------------
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Mettre à jour l'objet Seance avec le nouvel ID
                    seance.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Met à jour une séance existante dans la base de données
     * @param seance L'objet Seance avec les nouvelles valeurs
     * @throws SQLException En cas d'erreur de base de données
     */
    @Override
    public void edit(Seance seance) throws SQLException {
        // Requête SQL UPDATE pour modifier une séance existante
        String sql = "UPDATE seance SET jour = ?, type_seance = ?, mode = ?, heure_debut = ?, " +
                     "heure_fin = ?, salle_id = ?, matiere_id = ?, classe_id = ?, " +
                     "qr_token = ?, qr_expires_at = ?, qr_url = ?, google_event_id = ? " +
                     "WHERE id = ?";
        
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            // Définir tous les paramètres dans l'ordre
            ps.setString(1, seance.getJour());
            ps.setString(2, seance.getTypeSeance());
            ps.setString(3, seance.getMode());
            ps.setTimestamp(4, seance.getHeureDebut());
            ps.setTimestamp(5, seance.getHeureFin());
            ps.setInt(6, seance.getSalleId());
            ps.setInt(7, seance.getMatiereId());
            ps.setInt(8, seance.getClasseId());
            ps.setString(9, seance.getQrToken());
            ps.setTimestamp(10, seance.getQrExpiresAt());
            ps.setString(11, seance.getQrUrl());
            ps.setString(12, seance.getGoogleEventId());
            
            // L'ID dans la clause WHERE
            ps.setInt(13, seance.getId());
            
            // Exécuter la mise à jour
            ps.executeUpdate();
        }
    }

    /**
     * Supprime une séance de la base de données
     * @param id L'ID de la séance à supprimer
     * @throws SQLException En cas d'erreur de base de données
     */
    @Override
    public void delete(int id) throws SQLException {
        // Requête SQL DELETE avec condition WHERE
        String sql = "DELETE FROM seance WHERE id = ?";
        
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id); // Définir l'ID à supprimer
            ps.executeUpdate();
        }
    }

    // ============================================================================
    // MÉTHODES UTILITAIRES
    // ============================================================================

    /**
     * Récupère une séance spécifique par son ID
     * @param id L'ID de la séance recherchée
     * @return L'objet Seance ou null si non trouvé
     * @throws SQLException En cas d'erreur de base de données
     */
    public Seance getById(int id) throws SQLException {
        // Requête SQL SELECT avec filtre WHERE id = ?
        String sql = "SELECT * FROM seance WHERE id = ?";
        
        try (PreparedStatement ps = requireConnection().prepareStatement(sql)) {
            ps.setInt(1, id); // Définir l'ID à rechercher
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Si trouvé, convertir en objet Seance
                    return mapResultSet(rs);
                }
            }
        }
        
        // Retourner null si aucune séance trouvée avec cet ID
        return null;
    }

    /**
     * Convertit une ligne ResultSet en objet Seance
     * @param rs Le ResultSet positionné sur une ligne
     * @return Un nouvel objet Seance avec les données
     * @throws SQLException En cas d'erreur lors de la lecture
     */
    private Seance mapResultSet(ResultSet rs) throws SQLException {
        // Créer un nouvel objet Seance avec toutes les colonnes
        return new Seance(
            rs.getInt("id"),                           // ID unique
            rs.getString("jour"),                     // Jour de la semaine
            rs.getString("type_seance"),              // Type (CM/TD/TP/Révision)
            rs.getString("mode"),                      // Mode (Présentiel/Distanciel)
            rs.getTimestamp("heure_debut"),           // Date/heure de début
            rs.getTimestamp("heure_fin"),             // Date/heure de fin
            rs.getTimestamp("created_at"),           // Date de création
            rs.getInt("salle_id"),                  // ID de la salle
            rs.getInt("matiere_id"),                 // ID de la matière
            rs.getInt("classe_id"),                 // ID de la classe
            rs.getString("qr_token"),               // Token QR Code
            rs.getTimestamp("qr_expires_at"),      // Expiration du QR
            rs.getString("qr_url"),               // URL du QR
            rs.getString("google_event_id")         // ID Google Calendar
        );
    }
}
