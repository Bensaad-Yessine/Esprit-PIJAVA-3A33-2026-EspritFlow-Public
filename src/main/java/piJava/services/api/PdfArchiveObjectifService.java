package piJava.services.api;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import piJava.entities.ObjectifSante;
import piJava.entities.SuiviBienEtre;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class PdfArchiveObjectifService {

    private static final Font FONT_TITLE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(220, 53, 69));

    private static final Font FONT_SUBTITLE =
            FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(108, 117, 125));

    private static final Font FONT_SECTION =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE);

    private static final Font FONT_LABEL =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(52, 58, 64));

    private static final Font FONT_VALUE =
            FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(33, 37, 41));

    private static final Font FONT_TABLE_HEADER =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

    private static final Font FONT_TABLE_VALUE =
            FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(33, 37, 41));

    private static final Font FONT_SMALL =
            FontFactory.getFont(FontFactory.HELVETICA, 9, new BaseColor(108, 117, 125));

    public File genererPdf(ObjectifSante objectif, List<SuiviBienEtre> suivis) throws Exception {

        String dossierPath = System.getProperty("user.dir") + File.separator + "archives_pdf";
        File dossier = new File(dossierPath);
        if (!dossier.exists()) {
            dossier.mkdirs();
        }

        File pdfFile = new File(dossier, "objectif_archive_" + objectif.getId() + ".pdf");

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        ajouterEntete(document);
        ajouterBlocTitre(document, objectif);
        ajouterBlocStatut(document, objectif, suivis);
        ajouterBlocInformations(document, objectif, suivis);
        ajouterBlocSuivis(document, suivis);

        document.close();

        return pdfFile;
    }
    private void ajouterBlocStatut(Document document, ObjectifSante objectif, List<SuiviBienEtre> suivis) throws Exception {
        double scoreMoyen = calculerScoreMoyen(suivis);

        boolean atteint = "ATTEINT".equalsIgnoreCase(safe(objectif.getStatut()));
        BaseColor fond = atteint ? new BaseColor(230, 255, 237) : new BaseColor(255, 235, 238);
        BaseColor texte = atteint ? new BaseColor(25, 135, 84) : new BaseColor(220, 53, 69);

        PdfPTable bloc = new PdfPTable(1);
        bloc.setWidthPercentage(100);
        bloc.setSpacingAfter(18f);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(fond);
        cell.setPadding(18f);

        Font statutTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new BaseColor(52, 58, 64));
        Font statutValeur = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, texte);
        Font scoreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);

        Paragraph p1 = new Paragraph("Statut final", statutTitre);
        p1.setSpacingAfter(8f);

        Paragraph p2 = new Paragraph(safe(objectif.getStatut()), statutValeur);
        p2.setSpacingAfter(10f);

        Paragraph p3 = new Paragraph("Score moyen final : " + new DecimalFormat("0.00").format(scoreMoyen) + " / 100", scoreFont);

        cell.addElement(p1);
        cell.addElement(p2);
        cell.addElement(p3);

        bloc.addCell(cell);
        document.add(bloc);
    }
    private void ajouterEntete(Document document) throws Exception {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{70f, 30f});
        header.setSpacingAfter(18f);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(0);

        Paragraph app = new Paragraph("ESPRIT FLOW", FONT_TITLE);
        Paragraph sub = new Paragraph("Rapport d’archivage objectif santé", FONT_SUBTITLE);
        left.addElement(app);
        left.addElement(sub);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph date = new Paragraph(
                "Date d’archivage : " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()),
                FONT_LABEL
        );
        date.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(date);

        header.addCell(left);
        header.addCell(right);

        document.add(header);
    }

    private void ajouterBlocTitre(Document document, ObjectifSante objectif) throws Exception {
        PdfPTable bloc = new PdfPTable(1);
        bloc.setWidthPercentage(100);
        bloc.setSpacingAfter(16f);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(new BaseColor(252, 238, 240));
        cell.setPadding(16f);

        Paragraph titre = new Paragraph("Rapport de l’objectif archivé", FONT_TITLE);
        titre.setSpacingAfter(8f);

        Paragraph nomObjectif = new Paragraph("Objectif : " + safe(objectif.getTitre()), FONT_LABEL);
        nomObjectif.setSpacingAfter(4f);

        Paragraph description = new Paragraph(
                "Ce document résume les informations de l’objectif santé, son statut final et la liste détaillée des suivis associés.",
                FONT_VALUE
        );

        cell.addElement(titre);
        cell.addElement(nomObjectif);
        cell.addElement(description);

        bloc.addCell(cell);
        document.add(bloc);
    }

    private void ajouterBlocInformations(Document document, ObjectifSante objectif, List<SuiviBienEtre> suivis) throws Exception {
        ajouterTitreSection(document, "Informations générales");

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{30f, 70f});
        infoTable.setSpacingAfter(18f);

        double scoreMoyen = calculerScoreMoyen(suivis);
        DecimalFormat df = new DecimalFormat("0.00");

        addInfoRow(infoTable, "Titre", safe(objectif.getTitre()));
        addInfoRow(infoTable, "Type", safe(objectif.getType()));
        addInfoRow(infoTable, "Valeur cible", String.valueOf(objectif.getValeurCible()));
        addInfoRow(infoTable, "Date début", String.valueOf(objectif.getDateDebut()));
        addInfoRow(infoTable, "Date fin", String.valueOf(objectif.getDateFin()));
        addInfoRow(infoTable, "Priorité", safe(objectif.getPriorite()));
        addInfoRow(infoTable, "Statut final", safe(objectif.getStatut()));
        addInfoRow(infoTable, "Utilisateur", construireNomUtilisateur(objectif));
        addInfoRow(infoTable, "Nombre de suivis", String.valueOf(suivis != null ? suivis.size() : 0));
        addInfoRow(infoTable, "Score moyen final", df.format(scoreMoyen) + " / 100");

        document.add(infoTable);
    }

    private void ajouterBlocSuivis(Document document, List<SuiviBienEtre> suivis) throws Exception {
        ajouterTitreSection(document, "Liste des suivis");

        if (suivis == null || suivis.isEmpty()) {
            Paragraph vide = new Paragraph("Aucun suivi trouvé pour cet objectif.", FONT_VALUE);
            vide.setSpacingAfter(12f);
            document.add(vide);
            return;
        }

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{12f, 12f, 10f, 10f, 10f, 12f, 10f, 24f});
        table.setSpacingAfter(10f);

        addHeaderCell(table, "Date");
        addHeaderCell(table, "Humeur");
        addHeaderCell(table, "Sommeil");
        addHeaderCell(table, "Énergie");
        addHeaderCell(table, "Stress");
        addHeaderCell(table, "Alimentation");
        addHeaderCell(table, "Score");
        addHeaderCell(table, "Notes");

        boolean alternate = false;

        for (SuiviBienEtre s : suivis) {
            BaseColor bg = alternate ? new BaseColor(252, 248, 248) : BaseColor.WHITE;

            addValueCell(table, safe(String.valueOf(s.getDateSaisie())), bg);
            addValueCell(table, safe(s.getHumeur()), bg);
            addValueCell(table, String.valueOf(s.getQualiteSommeil()), bg);
            addValueCell(table, String.valueOf(s.getNiveauEnergie()), bg);
            addValueCell(table, String.valueOf(s.getNiveauStress()), bg);
            addValueCell(table, String.valueOf(s.getQualiteAlimentation()), bg);
            addValueCell(table, formatScore(s.getScore()), bg);
            addValueCell(table, safe(s.getNotesLibres()), bg);

            alternate = !alternate;
        }

        document.add(table);
    }

    private void ajouterTitreSection(Document document, String titre) throws Exception {
        PdfPTable section = new PdfPTable(1);
        section.setWidthPercentage(100);
        section.setSpacingBefore(6f);
        section.setSpacingAfter(10f);

        PdfPCell cell = new PdfPCell(new Phrase(titre, FONT_SECTION));
        cell.setBackgroundColor(new BaseColor(220, 53, 69));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10f);

        section.addCell(cell);
        document.add(section);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_LABEL));
        c1.setBackgroundColor(new BaseColor(248, 249, 250));
        c1.setPadding(8f);
        c1.setBorderColor(new BaseColor(230, 230, 230));

        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_VALUE));
        c2.setPadding(8f);
        c2.setBorderColor(new BaseColor(230, 230, 230));

        table.addCell(c1);
        table.addCell(c2);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_HEADER));
        cell.setBackgroundColor(new BaseColor(52, 58, 64));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBorderColor(new BaseColor(52, 58, 64));
        table.addCell(cell);
    }

    private void addValueCell(PdfPTable table, String text, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_VALUE));
        cell.setBackgroundColor(bg);
        cell.setPadding(7f);
        cell.setBorderColor(new BaseColor(230, 230, 230));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private double calculerScoreMoyen(List<SuiviBienEtre> suivis) {
        if (suivis == null || suivis.isEmpty()) {
            return 0.0;
        }

        double somme = 0;
        for (SuiviBienEtre s : suivis) {
            somme += s.getScore();
        }
        return somme / suivis.size();
    }

    private String construireNomUtilisateur(ObjectifSante objectif) {
        String nom = safe(objectif.getUserNom());
        String prenom = safe(objectif.getUserPrenom());

        String full = (prenom + " " + nom).trim();
        return full.isBlank() ? "Utilisateur inconnu" : full;
    }

    private String formatScore(double score) {
        return new DecimalFormat("0.00").format(score);
    }

    private String safe(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }
}