package piJava.services;

import piJava.entities.user;

public class BanNotificationService {

    private final MailService mailService = new MailService();

    public void notifyUserBanned(user user, String banReason) throws java.io.IOException {
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            System.err.println("Cannot send ban notification: invalid user email");
            return;
        }

        String subject = "[ALERTE] Notification - Compte suspendu";
        String htmlBody = buildBanNotificationHtml(user, banReason);
        mailService.sendPasswordResetEmail(user.getEmail(), subject, htmlBody);
    }

    private String buildBanNotificationHtml(user user, String banReason) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 0; }\n" +
                "        .email-container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden; }\n" +
                "        .header { background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); color: white; padding: 40px 20px; text-align: center; }\n" +
                "        .header h1 { margin: 0; font-size: 28px; font-weight: bold; }\n" +
                "        .header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }\n" +
                "        .alert-banner { background-color: #fee2e2; border-left: 4px solid #dc2626; padding: 20px; margin: 0; color: #991b1b; }\n" +
                "        .alert-banner strong { color: #dc2626; font-size: 16px; }\n" +
                "        .content { padding: 40px 30px; }\n" +
                "        .content h2 { color: #dc2626; font-size: 20px; margin: 0 0 20px 0; }\n" +
                "        .content p { color: #333333; line-height: 1.6; margin: 15px 0; }\n" +
                "        .reason-box { background-color: #fef3c7; border-left: 4px solid #dc2626; padding: 20px; margin: 25px 0; border-radius: 4px; }\n" +
                "        .reason-box .label { color: #666666; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; }\n" +
                "        .reason-box .text { color: #333333; margin-top: 10px; line-height: 1.6; }\n" +
                "        .notice { background-color: #f0f0f0; border: 1px solid #cccccc; border-radius: 4px; padding: 15px; margin: 20px 0; color: #555555; }\n" +
                "        .notice strong { color: #dc2626; }\n" +
                "        .actions { margin: 30px 0; padding: 20px; background-color: #f8f8f8; border-radius: 4px; text-align: center; }\n" +
                "        .actions p { margin: 10px 0; color: #333333; }\n" +
                "        .actions a { color: #dc2626; text-decoration: none; font-weight: bold; }\n" +
                "        .footer { background-color: #f8f8f8; padding: 20px 30px; border-top: 1px solid #eeeeee; text-align: center; font-size: 12px; color: #888888; }\n" +
                "        .footer p { margin: 5px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"email-container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>[ALERTE] Notification importante</h1>\n" +
                "            <p>EspritFlow - Plateforme d'apprentissage</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"alert-banner\">\n" +
                "            <strong>Votre compte a ete temporairement suspendu</strong>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"content\">\n" +
                "            <h2>Bonjour " + safe(user.getPrenom()) + ",</h2>\n" +
                "\n" +
                "            <p>Nous vous ecrivons pour vous informer que votre compte EspritFlow a ete suspendu en raison d'une violation de nos conditions d'utilisation.</p>\n" +
                "\n" +
                "            <div class=\"reason-box\">\n" +
                "                <div class=\"label\">Motif de la suspension</div>\n" +
                "                <div class=\"text\">" + (banReason != null && !banReason.isEmpty() ? safe(banReason) : "Non specifie") + "</div>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"notice\">\n" +
                "                <strong>[IMPORTANT] Consequences :</strong> Vous ne pouvez plus acceder a votre compte, consulter vos cours, ou soumettre des travaux. Votre acces a ete temporairement desactive.\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"actions\">\n" +
                "                <p><strong>Vous pensez que c'est une erreur ?</strong></p>\n" +
                "                <p>Vous pouvez contacter notre equipe de support pour contester cette suspension.</p>\n" +
                "                <p><a href=\"mailto:support@espritflow.tn\">[EMAIL] Contactez le support</a></p>\n" +
                "            </div>\n" +
                "\n" +
                "            <p style=\"color: #666666; font-size: 13px; margin-top: 30px;\">\n" +
                "                <strong>Email de support :</strong> support@espritflow.tn<br>\n" +
                "                <strong>Compte e-mail :</strong> " + safe(user.getEmail()) + "\n" +
                "            </p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2026 EspritFlow. Tous droits reserves.</p>\n" +
                "            <p>Cet email a ete envoye a <strong>" + safe(user.getEmail()) + "</strong></p>\n" +
                "            <p>Cette suspension a ete appliqu e le <strong>" + getCurrentDate() + "</strong></p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String getCurrentDate() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return now.getDayOfMonth() + "/" + now.getMonthValue() + "/" + now.getYear() + " à " + now.getHour() + ":" + String.format("%02d", now.getMinute());
    }
}

