package piJava.services;

import piJava.entities.user;
import piJava.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * StreakEngagementService
 *
 * Automatically finds users who:
 *  - Have an active streak (current_streak >= 1)
 *  - Have NOT logged in for at least 1 day (last_login < yesterday midnight)
 *
 * Then sends a personalized HTML re-engagement email reminding them that
 * their streak is at risk so they come back and learn today.
 */
public class StreakEngagementService {

    private static final String TIMEZONE = "Africa/Tunis";
    private static final ZoneId  ZONE_ID  = ZoneId.of(TIMEZONE);

    private final MailService mailService = new MailService();
    private final MyDataBase  db          = MyDataBase.getInstance();

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the list of users whose streak is > 0 and last login was
     * at least minDaysInactive days ago (default: 1).
     */
    public List<user> findInactiveStreakUsers(int minDaysInactive) {
        List<user> result = new ArrayList<>();

        // Compute the cutoff timestamp: NOW() - minDaysInactive days
        LocalDateTime cutoff = LocalDateTime.now(ZONE_ID).minusDays(minDaysInactive);

        String sql = "SELECT id, email, nom, prenom, current_streak, longest_streak, " +
                     "last_streak_date, last_login " +
                     "FROM `user` " +
                     "WHERE current_streak >= 1 " +
                     "  AND is_banned = 0 " +
                     "  AND is_verified = 1 " +
                     "  AND (last_login IS NULL OR last_login <= ?) " +
                     "ORDER BY current_streak DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(cutoff));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                user u = new user();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setCurrentStreak(rs.getInt("current_streak"));
                u.setLongestStreak(rs.getInt("longest_streak"));

                Date lastStreak = rs.getDate("last_streak_date");
                if (lastStreak != null) u.setLastStreakDate(lastStreak.toString());

                Timestamp lastLogin = rs.getTimestamp("last_login");
                if (lastLogin != null) u.setLastLogin(lastLogin.toLocalDateTime().toString());

                result.add(u);
            }

        } catch (SQLException e) {
            System.err.println("[StreakEngagement] DB error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Sends a personalized re-engagement email to every inactive streak user.
     *
     * @return a summary: "sent=X, failed=Y"
     */
    public String sendEngagementEmails() {
        return sendEngagementEmails(1);
    }

    /**
     * Overload with configurable inactivity threshold.
     *
     * @param minDaysInactive minimum days since last login (default: 1)
     * @return a summary: "sent=X, failed=Y"
     */
    public String sendEngagementEmails(int minDaysInactive) {
        List<user> targets = findInactiveStreakUsers(minDaysInactive);

        int sent   = 0;
        int failed = 0;

        System.out.println("[StreakEngagement] Found " + targets.size() +
                           " user(s) with inactive streaks (inactivity >= " + minDaysInactive + " day(s)).");

        for (user u : targets) {
            try {
                String subject = buildSubject(u);
                String body    = buildEmailHtml(u);
                mailService.sendPasswordResetEmail(u.getEmail(), subject, body);
                System.out.println("[StreakEngagement] ✅ Email sent to " + u.getEmail() +
                                   " (streak=" + u.getCurrentStreak() + ")");
                sent++;
            } catch (Exception e) {
                System.err.println("[StreakEngagement] ❌ Failed for " + u.getEmail() +
                                   ": " + e.getMessage());
                failed++;
            }
        }

        return "sent=" + sent + ", failed=" + failed + ", total_targets=" + targets.size();
    }

    // ── Email Builders ────────────────────────────────────────────────────────

    private String buildSubject(user u) {
        int streak = u.getCurrentStreak();
        if (streak >= 30) {
            return "🔥 Your " + streak + "-day streak is at risk! Don't break it now";
        } else if (streak >= 7) {
            return "⚡ " + streak + " days strong — keep your learning streak alive!";
        } else {
            return "📚 Your " + streak + "-day streak needs you today!";
        }
    }

    private String buildEmailHtml(user u) {
        String firstName  = safe(u.getPrenom());
        int    streak     = u.getCurrentStreak();
        int    longest    = u.getLongestStreak();
        String lastDate   = formatStreakDate(u.getLastStreakDate());
        String flameColor = streak >= 30 ? "#ff6b00" : (streak >= 7 ? "#f59e0b" : "#ef4444");
        String headerGrad = streak >= 30
                ? "linear-gradient(135deg, #ff6b00 0%, #c2410c 100%)"
                : (streak >= 7
                   ? "linear-gradient(135deg, #f59e0b 0%, #b45309 100%)"
                   : "linear-gradient(135deg, #6366f1 0%, #4338ca 100%)");

        String urgencyMsg;
        if (u.getLastLogin() != null) {
            try {
                LocalDateTime lastLogin = LocalDateTime.parse(u.getLastLogin(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                long hours = ChronoUnit.HOURS.between(lastLogin, LocalDateTime.now(ZONE_ID));
                urgencyMsg = "You haven't visited EspritFlow in <strong>" + hours + " hour(s)</strong>. " +
                             "Log in today to keep your streak alive!";
            } catch (Exception e) {
                urgencyMsg = "You haven't visited EspritFlow in over a day. Log in to keep your streak alive!";
            }
        } else {
            urgencyMsg = "You haven't visited EspritFlow in over a day. Log in to keep your streak alive!";
        }

        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "  <meta charset=\"UTF-8\">\n" +
               "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "  <title>Your streak is waiting!</title>\n" +
               "  <style>\n" +
               "    body { margin:0; padding:0; background:#f3f4f6; font-family:'Segoe UI',Arial,sans-serif; }\n" +
               "    .wrap { max-width:600px; margin:32px auto; background:#ffffff; border-radius:16px;\n" +
               "            box-shadow:0 4px 24px rgba(0,0,0,0.10); overflow:hidden; }\n" +
               "    .header { background:" + headerGrad + "; padding:44px 32px 36px; text-align:center; color:#fff; }\n" +
               "    .header .flame { font-size:56px; line-height:1; margin-bottom:12px; }\n" +
               "    .header h1 { margin:0 0 8px; font-size:28px; font-weight:800; letter-spacing:-0.5px; }\n" +
               "    .header p  { margin:0; font-size:15px; opacity:0.9; }\n" +
               "    .streak-badge { background:#fff3cd; border:2px solid " + flameColor + ";\n" +
               "                   border-radius:50px; display:inline-block; padding:10px 28px;\n" +
               "                   margin:24px auto 0; font-size:22px; font-weight:800;\n" +
               "                   color:" + flameColor + "; }\n" +
               "    .body { padding:36px 40px; }\n" +
               "    .body h2 { color:#1e293b; font-size:22px; margin:0 0 16px; font-weight:700; }\n" +
               "    .body p  { color:#475569; line-height:1.7; margin:0 0 16px; font-size:15px; }\n" +
               "    .stats { display:flex; gap:16px; margin:24px 0; }\n" +
               "    .stat  { flex:1; background:#f8fafc; border-radius:12px; padding:16px;\n" +
               "             text-align:center; border:1px solid #e2e8f0; }\n" +
               "    .stat .num { font-size:28px; font-weight:800; color:" + flameColor + "; line-height:1; }\n" +
               "    .stat .lbl { font-size:12px; color:#94a3b8; margin-top:4px; text-transform:uppercase;\n" +
               "                 letter-spacing:0.5px; }\n" +
               "    .warning { background:#fef3c7; border-left:4px solid " + flameColor + ";\n" +
               "               border-radius:0 8px 8px 0; padding:14px 18px; margin:20px 0;\n" +
               "               color:#92400e; font-size:14px; line-height:1.6; }\n" +
               "    .cta-wrap { text-align:center; margin:32px 0 8px; }\n" +
               "    .cta { display:inline-block; background:" + headerGrad + ";\n" +
               "           color:#fff; text-decoration:none; padding:14px 36px;\n" +
               "           border-radius:50px; font-size:16px; font-weight:700;\n" +
               "           letter-spacing:0.3px;\n" +
               "           box-shadow:0 4px 14px rgba(0,0,0,0.15); }\n" +
               "    .tips { background:#f0fdf4; border-radius:12px; padding:20px 24px; margin:24px 0; }\n" +
               "    .tips h3 { margin:0 0 12px; color:#166534; font-size:15px; font-weight:700; }\n" +
               "    .tips ul { margin:0; padding-left:20px; color:#166534; }\n" +
               "    .tips li { margin-bottom:8px; font-size:14px; line-height:1.5; }\n" +
               "    .footer { background:#f8fafc; border-top:1px solid #e2e8f0;\n" +
               "              padding:20px 32px; text-align:center; font-size:12px; color:#94a3b8; }\n" +
               "    .footer a { color:#6366f1; text-decoration:none; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class=\"wrap\">\n" +
               "    <!-- Header -->\n" +
               "    <div class=\"header\">\n" +
               "      <div class=\"flame\">🔥</div>\n" +
               "      <h1>Your streak is at risk!</h1>\n" +
               "      <p>EspritFlow — Keep Learning Every Day</p>\n" +
               "      <div class=\"streak-badge\">🔥 " + streak + "-Day Streak</div>\n" +
               "    </div>\n" +
               "\n" +
               "    <!-- Body -->\n" +
               "    <div class=\"body\">\n" +
               "      <h2>Hey " + firstName + "! Don't let it slip away 💪</h2>\n" +
               "      <p>" + urgencyMsg + "</p>\n" +
               "\n" +
               "      <!-- Streak Stats -->\n" +
               "      <div class=\"stats\">\n" +
               "        <div class=\"stat\">\n" +
               "          <div class=\"num\">🔥 " + streak + "</div>\n" +
               "          <div class=\"lbl\">Current Streak</div>\n" +
               "        </div>\n" +
               "        <div class=\"stat\">\n" +
               "          <div class=\"num\">🏆 " + longest + "</div>\n" +
               "          <div class=\"lbl\">Longest Streak</div>\n" +
               "        </div>\n" +
               "        <div class=\"stat\">\n" +
               "          <div class=\"num\">📅</div>\n" +
               "          <div class=\"lbl\">Last active: " + lastDate + "</div>\n" +
               "        </div>\n" +
               "      </div>\n" +
               "\n" +
               "      <!-- Urgency warning -->\n" +
               "      <div class=\"warning\">\n" +
               "        ⚠️ <strong>Your streak will reset to 0</strong> if you miss today!\n" +
               "        It only takes <strong>one course</strong> to keep it going.\n" +
               "      </div>\n" +
               "\n" +
               "      <!-- CTA -->\n" +
               "      <div class=\"cta-wrap\">\n" +
               "        <a href=\"#\" class=\"cta\">🚀 Continue Learning Now</a>\n" +
               "      </div>\n" +
               "\n" +
               "      <!-- Tips -->\n" +
               "      <div class=\"tips\">\n" +
               "        <h3>💡 Quick ways to maintain your streak:</h3>\n" +
               "        <ul>\n" +
               "          <li>📖 Complete just <strong>one lesson</strong> today — that's all it takes!</li>\n" +
               "          <li>⏱️ Even <strong>10 minutes</strong> of learning counts.</li>\n" +
               "          <li>🎯 Pick up where you left off — your progress is saved.</li>\n" +
               "          <li>📱 You can learn anytime, anywhere.</li>\n" +
               "        </ul>\n" +
               "      </div>\n" +
               "\n" +
               "      <p style=\"color:#94a3b8; font-size:13px; text-align:center; margin-top:8px;\">\n" +
               "        You've come so far — " + streak + " consecutive days of learning!\n" +
               "        Don't break the chain now.\n" +
               "      </p>\n" +
               "    </div>\n" +
               "\n" +
               "    <!-- Footer -->\n" +
               "    <div class=\"footer\">\n" +
               "      <p>© 2026 EspritFlow. All rights reserved.</p>\n" +
               "      <p>This email was sent to <strong>" + safe(u.getEmail()) + "</strong></p>\n" +
               "      <p><a href=\"mailto:support@espritflow.tn\">Unsubscribe</a> · " +
               "         <a href=\"mailto:support@espritflow.tn\">Contact Support</a></p>\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private String formatStreakDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return "N/A";
        try {
            LocalDate d = LocalDate.parse(isoDate, DateTimeFormatter.ISO_DATE);
            return d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear();
        } catch (Exception e) {
            return isoDate;
        }
    }
}
