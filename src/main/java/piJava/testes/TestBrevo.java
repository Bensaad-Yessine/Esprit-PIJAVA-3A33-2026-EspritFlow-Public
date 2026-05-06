package piJava.testes;

import piJava.services.api.BrevoEmailService;

import java.io.File;

public class TestBrevo {
    public static void main(String[] args) {
        try {
            BrevoEmailService brevo = new BrevoEmailService();

            File pdf = new File("archives_pdf/objectif_archive_63.pdf");

            if (!pdf.exists()) {
                System.out.println("PDF introuvable : " + pdf.getAbsolutePath());
                return;
            }

            String toEmail = "adammbi721@gmail.com";

            String html = """
                    <h2>Test Brevo JavaFX ✅</h2>
                    <p>Ceci est un test d'envoi email avec PDF depuis JavaFX.</p>
                    <p><b>Objectif Santé</b></p>
                    """;

            boolean sent = brevo.sendPdfWithAttachment(
                    toEmail,
                    "Test Brevo JavaFX",
                    html,
                    pdf,
                    "test.pdf"
            );

            System.out.println(sent ? "Email envoyé avec succès." : "Email non envoyé (Brevo non autorisé ou indisponible).");

        } catch (Exception e) {
            System.out.println("Erreur test Brevo : " + e.getMessage());
            e.printStackTrace();
        }
    }
}