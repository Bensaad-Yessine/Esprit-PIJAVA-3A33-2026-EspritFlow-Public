package piJava.utils;

import piJava.entities.user;

public class SessionManager {

    private static SessionManager instance;
    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    private user currentUser;

    /** Call this right after a successful login. */
    public void login(user u) {
        // ✅ CRITICAL: clear the plain-text password from memory immediately.
        // This prevents edit() from ever re-hashing it and corrupting the DB.
        u.setPassword("");
        this.currentUser = u;
    }

    public void logout() { this.currentUser = null; }

    public user getCurrentUser() { return currentUser; }

    public boolean isLoggedIn() { return currentUser != null; }

    public String getFullName() {
        if (currentUser == null) return "";
        return currentUser.getPrenom() + " " + currentUser.getNom();
    }

    public String getProfilePic() {
        if (currentUser == null) return null;
        return currentUser.getProfile_pic();
    }

    public boolean isAdmin() {
        if (currentUser == null) return false;
        String roles = currentUser.getRoles();
        return roles != null && roles.contains("ROLE_ADMIN");
    }
}