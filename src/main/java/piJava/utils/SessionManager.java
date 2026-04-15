package piJava.utils;

import piJava.entities.user;

/**
 * SessionManager — the JavaFX equivalent of a web session / cookie.
 *
 * Holds the currently logged-in user for the entire lifetime of the app.
 * Access it from ANY controller with: SessionManager.getInstance().getCurrentUser()
 */
public class SessionManager {

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static SessionManager instance;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ─── Session Data ─────────────────────────────────────────────────────────
    private user currentUser;

    // ─── API ──────────────────────────────────────────────────────────────────

    /** Call this right after a successful login. */
    public void login(user u) {
        this.currentUser = u;
    }

    /** Call this on logout to clear the session. */
    public void logout() {
        this.currentUser = null;
    }

    /** Returns the logged-in user, or null if no one is logged in. */
    public user getCurrentUser() {
        return currentUser;
    }

    /** Quick check — use this in controllers to guard protected pages. */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // ─── Convenience getters (null-safe) ──────────────────────────────────────

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
