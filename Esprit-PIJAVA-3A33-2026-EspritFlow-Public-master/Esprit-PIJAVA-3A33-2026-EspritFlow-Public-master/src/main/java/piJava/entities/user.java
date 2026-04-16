package piJava.entities;

public class user {

    private int id;
    private String email;
    private String roles;
    private String password;
    private int is_verified;
    private String nom;
    private String prenom;
    private String num_tel;
    private String date_de_naissance;
    private String sexe;
    private String profile_pic;
    private String created_at;
    private int is_banned;
    private String ban_reason;
    private String banned_at;
    private String verification_token;
    private Integer classe_id; // nullable → Integer not int

    // ─── Full Constructor ───────────────────────────────────────
    public user(int id, String email, String roles, String password,
                int is_verified, String nom, String prenom,
                String num_tel, String date_de_naissance, String sexe,
                String profile_pic, String created_at, int is_banned,
                String ban_reason, String banned_at,
                String verification_token, Integer classe_id) {

        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.is_verified = is_verified;
        this.nom = nom;
        this.prenom = prenom;
        this.num_tel = num_tel;
        this.date_de_naissance = date_de_naissance;
        this.sexe = sexe;
        this.profile_pic = profile_pic;
        this.created_at = created_at;
        this.is_banned = is_banned;
        this.ban_reason = ban_reason;
        this.banned_at = banned_at;
        this.verification_token = verification_token;
        this.classe_id = classe_id;
    }

    // ─── Minimal Constructor (for login/register) ────────────────
    public user(String email, String password, String nom, String prenom) {
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
    }
    public user(String nom , String prenom,String email ,String num_tel , String date_de_naissance,String sexe,int classe_id , String password ) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.num_tel = num_tel;
        this.date_de_naissance = date_de_naissance;
        this.sexe = sexe;
        this.classe_id = classe_id;
        this.password = password;
    }

    // ─── Empty Constructor ───────────────────────────────────────
    public user() {}

    // ─── Getters & Setters ───────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getIs_verified() { return is_verified; }
    public void setIs_verified(int is_verified) { this.is_verified = is_verified; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNum_tel() { return num_tel; }
    public void setNum_tel(String num_tel) { this.num_tel = num_tel; }

    public String getDate_de_naissance() { return date_de_naissance; }
    public void setDate_de_naissance(String date_de_naissance) { this.date_de_naissance = date_de_naissance; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public String getProfile_pic() { return profile_pic; }
    public void setProfile_pic(String profile_pic) { this.profile_pic = profile_pic; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public int getIs_banned() { return is_banned; }
    public void setIs_banned(int is_banned) { this.is_banned = is_banned; }

    public String getBan_reason() { return ban_reason; }
    public void setBan_reason(String ban_reason) { this.ban_reason = ban_reason; }

    public String getBanned_at() { return banned_at; }
    public void setBanned_at(String banned_at) { this.banned_at = banned_at; }

    public String getVerification_token() { return verification_token; }
    public void setVerification_token(String verification_token) { this.verification_token = verification_token; }

    public Integer getClasse_id() { return classe_id; }
    public void setClasse_id(Integer classe_id) { this.classe_id = classe_id; }

    // ─── toString (useful for debugging) ────────────────────────
    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', nom='" + nom +
                "', prenom='" + prenom + "', is_verified=" + is_verified +
                ", is_banned=" + is_banned + "}";
    }
}