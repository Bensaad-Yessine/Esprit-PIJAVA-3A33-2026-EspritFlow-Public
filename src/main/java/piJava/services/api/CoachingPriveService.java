package piJava.services.api;

import org.json.JSONArray;
import org.json.JSONObject;
import piJava.entities.ObjectifSante;
import piJava.entities.SuiviBienEtre;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CoachingPriveService {

    private final GroqClient groqClient;

    public CoachingPriveService() {
        this.groqClient = new GroqClient();
    }

    public CoachingResponse genererCoaching(ObjectifSante objectif, List<SuiviBienEtre> suivis) throws org.json.JSONException {
        double scoreMoyen = calculerScoreMoyen(suivis);
        String tendance = calculerTendance(suivis);
        SuiviBienEtre dernierSuivi = getDernierSuivi(suivis);
        String pointFort = detecterPointFort(dernierSuivi);
        String pointFaible = detecterPointFaible(dernierSuivi);
        List<String> resumeSuivis = construireResumeSuivis(suivis);

        JSONArray messages = new JSONArray();

        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", """
Tu es un coach santé premium dans une application JavaFX professionnelle.
Tu réponds uniquement en JSON valide.

Format JSON attendu :
{
  "titre": "...",
  "messageMotivation": "...",
  "resumeAnalyse": "...",
  "niveau": "LOW|MEDIUM|HIGH",
  "scoreMoyen": 0,
  "tendance": "UP|STABLE|DOWN",
  "pointFort": "...",
  "pointFaible": "...",
  "conseilsTypeObjectif": ["...", "...", "..."],
  "conseilsNiveauxFaibles": ["...", "..."],
  "messageConfiance": "..."
}

Règles strictes :
- Réponds uniquement en JSON valide.
- Pas de markdown.
- Pas de texte hors JSON.
- Langue : français.
- Ton : professionnel, motivant, humain, clair.
- Ne jamais inventer des données.
- Utiliser uniquement les données fournies dans le prompt utilisateur.
- Ne pas répéter plusieurs fois la même idée.
- Ne donne jamais de conseil générique vide.
- Tous les paragraphes doivent être courts.
- Les champs "messageMotivation", "resumeAnalyse" et "messageConfiance" doivent contenir maximum 2 phrases chacun.
- Ne jamais écrire de titre dans "messageMotivation", "resumeAnalyse" ou "messageConfiance".
- Ne jamais commencer par "Message de motivation :", "Analyse :", "Résumé :", "Résumé d'analyse :" ou "Message de confiance :".
- Le seul vrai titre du coaching est le champ "titre".
- Le ton doit être encourageant, jamais négatif ou culpabilisant.
- Ne jamais dire "mauvais endroit", "échec", "vous êtes mal parti", "situation mauvaise" ou une phrase dure.
- Si les résultats sont faibles, utiliser une formulation douce comme : "certains points doivent être améliorés pour progresser plus régulièrement".

Règle importante sur les données :
- Le score moyen est une note de progression sur 100.
- La valeur cible n’est pas un score.
- Ne jamais comparer le score moyen avec la valeur cible.
- Ne jamais dire que le score moyen dépasse la valeur cible.
- Ne jamais dire que l'utilisateur doit atteindre la valeur cible comme si c'était un score.
- La valeur cible représente la cible réelle de l’objectif selon le type.
- Pour SPORT, la valeur cible représente des minutes d’activité physique.
- Pour SOMMEIL, la valeur cible représente des heures de sommeil.
- Pour ALIMENTATION, la valeur cible représente un objectif alimentaire.
- Utiliser la valeur cible seulement pour expliquer l’objectif, pas pour juger le score.

Pour "niveau" :
- LOW si le score moyen est inférieur à 50.
- MEDIUM si le score moyen est entre 50 et 74.99.
- HIGH si le score moyen est supérieur ou égal à 75.

Pour "messageMotivation" :
- écrire maximum 2 phrases.
- écrire seulement un paragraphe normal.
- ne pas écrire de titre au début.
- mentionner le score moyen.
- mentionner la tendance.
- mentionner l’objectif avec sa vraie unité.
- dire clairement si l'utilisateur est sur une bonne voie ou s'il doit améliorer sa régularité.
- si le score moyen est faible ou la tendance est DOWN, dire doucement qu'il faut corriger certaines habitudes pour mieux avancer vers l'objectif.
- terminer par une petite phrase motivante.

Pour "resumeAnalyse" :
- écrire maximum 2 phrases.
- écrire seulement un paragraphe normal.
- ne pas écrire de titre au début.
- expliquer rapidement le point fort.
- expliquer rapidement le point faible principal.
- relier l’analyse au type d’objectif.

Pour "conseilsTypeObjectif" :
- donner 3 à 5 conseils précis, utiles, pratiques et concrets.
- les conseils doivent être adaptés exactement au type de l'objectif.
- éviter les répétitions.

Pour "conseilsNiveauxFaibles" :
- donner 2 à 4 conseils spécifiques liés aux indicateurs faibles.
- expliquer quoi faire concrètement.
- éviter les phrases générales.

Pour "messageConfiance" :
- écrire maximum 2 phrases.
- écrire seulement un paragraphe normal.
- ne pas écrire de titre au début.
- produire un message positif, court et crédible.
- valoriser les efforts déjà faits.
- encourager l’utilisateur sans exagération.

Chaque texte doit être détaillé, utile et exploitable, mais court.
"""));

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", construirePrompt(
                        objectif,
                        suivis,
                        scoreMoyen,
                        tendance,
                        dernierSuivi,
                        pointFort,
                        pointFaible
                ))
        );

        try {
            JSONObject result = groqClient.chatJson(messages, 0.4, 900);

            String titre = requireString(result, "titre");
            String messageMotivation = requireString(result, "messageMotivation");
            String resumeAnalyse = requireString(result, "resumeAnalyse");
            String niveau = requireEnum(result, "niveau", List.of("LOW", "MEDIUM", "HIGH"));
            String messageConfiance = requireString(result, "messageConfiance");

            List<String> conseilsType = requireStringList(result, "conseilsTypeObjectif");
            List<String> conseilsFaibles = requireStringList(result, "conseilsNiveauxFaibles");

            if (conseilsType.isEmpty()) {
                throw new RuntimeException("Groq a retourné une liste vide pour conseilsTypeObjectif.");
            }

            if (conseilsFaibles.isEmpty()) {
                throw new RuntimeException("Groq a retourné une liste vide pour conseilsNiveauxFaibles.");
            }

            return new CoachingResponse(
                    titre,
                    messageMotivation,
                    resumeAnalyse,
                    niveau,
                    scoreMoyen,
                    tendance,
                    pointFort,
                    pointFaible,
                    conseilsType,
                    conseilsFaibles,
                    resumeSuivis,
                    messageConfiance
            );

        } catch (Exception e) {
            throw new RuntimeException("Échec du coaching Groq : " + e.getMessage(), e);
        }
    }

    private String construirePrompt(ObjectifSante objectif,
                                    List<SuiviBienEtre> suivis,
                                    double scoreMoyen,
                                    String tendance,
                                    SuiviBienEtre dernierSuivi,
                                    String pointFort,
                                    String pointFaible) {
        StringBuilder sb = new StringBuilder();

        sb.append("Objectif santé\n");
        sb.append("Titre : ").append(objectif.getTitre()).append("\n");
        sb.append("Type : ").append(objectif.getType()).append("\n");

        sb.append("Valeur cible : ")
                .append(objectif.getValeurCible())
                .append(" ")
                .append(getUniteValeurCible(objectif.getType()))
                .append("\n");

        sb.append("Important : la valeur cible n'est pas un score sur 100. ")
                .append("Elle représente la cible réelle de l'objectif selon son type.\n");

        sb.append("Priorité : ").append(objectif.getPriorite()).append("\n");
        sb.append("Statut : ").append(objectif.getStatut()).append("\n\n");

        sb.append("Analyse globale\n");
        sb.append("Nombre de suivis : ").append(suivis == null ? 0 : suivis.size()).append("\n");

        sb.append("Score moyen calculé localement : ")
                .append(String.format(Locale.US, "%.2f", scoreMoyen))
                .append("/100\n");

        sb.append("Niveau calculé selon le score : ")
                .append(calculerNiveauLocal(scoreMoyen))
                .append("\n");

        sb.append("Tendance calculée localement : ").append(tendance).append("\n");
        sb.append("Point fort détecté localement : ").append(pointFort).append("\n");
        sb.append("Point faible détecté localement : ").append(pointFaible).append("\n\n");

        if (dernierSuivi != null) {
            sb.append("Dernier suivi\n");
            sb.append("Date : ").append(dernierSuivi.getDateSaisie()).append("\n");
            sb.append("Humeur : ").append(dernierSuivi.getHumeur()).append("\n");
            sb.append("Sommeil : ").append(dernierSuivi.getQualiteSommeil()).append("/10\n");
            sb.append("Énergie : ").append(dernierSuivi.getNiveauEnergie()).append("/10\n");
            sb.append("Stress : ").append(dernierSuivi.getNiveauStress()).append("/10\n");
            sb.append("Alimentation : ").append(dernierSuivi.getQualiteAlimentation()).append("/10\n");
            sb.append("Score : ").append(dernierSuivi.getScore()).append("/100\n");

            if (dernierSuivi.getNotesLibres() != null && !dernierSuivi.getNotesLibres().isBlank()) {
                sb.append("Notes : ").append(dernierSuivi.getNotesLibres()).append("\n\n");
            } else {
                sb.append("Notes : aucune note renseignée\n\n");
            }
        }

        sb.append("""
                Consignes finales :
                - Génère seulement un JSON valide.
                - Utilise le score moyen comme une note de progression sur 100.
                - Utilise la valeur cible seulement comme information sur l'objectif.
                - Ne compare jamais le score moyen avec la valeur cible.
                - Ne dis jamais que le score moyen dépasse la valeur cible.
                - Ne mets pas de titre dans messageMotivation, resumeAnalyse ou messageConfiance.
                - messageMotivation doit contenir maximum 2 phrases.
                - resumeAnalyse doit contenir maximum 2 phrases.
                - messageConfiance doit contenir maximum 2 phrases.
                - Le ton doit être doux, motivant et professionnel.
                - Si le score est faible ou la tendance est DOWN, explique avec douceur que certaines habitudes doivent être améliorées.
                - Ne jamais utiliser des phrases dures comme "vous êtes dans un mauvais endroit", "vous êtes mal parti" ou "échec".
                - Donne des conseils précis pour le type de l'objectif.
                - Donne des conseils précis pour les niveaux faibles.
                - Aucun conseil générique.
                - Aucune phrase vide.
                - Aucune réponse de secours.
                """);

        return sb.toString();
    }

    private String getUniteValeurCible(String type) {
        if (type == null) {
            return "unité liée à l'objectif";
        }

        return switch (type.toUpperCase(Locale.ROOT)) {
            case "SPORT" -> "minutes d'activité physique";
            case "SOMMEIL" -> "heures de sommeil";
            case "ALIMENTATION" -> "objectif alimentaire";
            default -> "unité liée à l'objectif";
        };
    }

    private String calculerNiveauLocal(double scoreMoyen) {
        if (scoreMoyen < 50) {
            return "LOW";
        }

        if (scoreMoyen < 75) {
            return "MEDIUM";
        }

        return "HIGH";
    }

    private double calculerScoreMoyen(List<SuiviBienEtre> suivis) {
        if (suivis == null || suivis.isEmpty()) {
            return 0;
        }

        double somme = 0;
        for (SuiviBienEtre suivi : suivis) {
            somme += suivi.getScore();
        }

        return somme / suivis.size();
    }

    private String calculerTendance(List<SuiviBienEtre> suivis) {
        if (suivis == null || suivis.size() < 2) {
            return "STABLE";
        }

        List<SuiviBienEtre> tries = suivis.stream()
                .sorted(Comparator.comparing(SuiviBienEtre::getDateSaisie))
                .toList();

        double avant = tries.get(tries.size() - 2).getScore();
        double dernier = tries.get(tries.size() - 1).getScore();
        double diff = dernier - avant;

        if (diff > 5) return "UP";
        if (diff < -5) return "DOWN";
        return "STABLE";
    }

    private SuiviBienEtre getDernierSuivi(List<SuiviBienEtre> suivis) {
        if (suivis == null || suivis.isEmpty()) {
            return null;
        }

        return suivis.stream()
                .max(Comparator.comparing(SuiviBienEtre::getDateSaisie))
                .orElse(null);
    }

    private String detecterPointFort(SuiviBienEtre s) {
        if (s == null) return "Aucun point fort détecté pour le moment";

        int sommeil = s.getQualiteSommeil();
        int energie = s.getNiveauEnergie();
        int stressInverse = 10 - s.getNiveauStress();
        int alimentation = s.getQualiteAlimentation();

        int max = Math.max(Math.max(sommeil, energie), Math.max(stressInverse, alimentation));

        if (max == sommeil) return "Qualité du sommeil";
        if (max == energie) return "Niveau d'énergie";
        if (max == stressInverse) return "Gestion du stress";
        return "Qualité de l'alimentation";
    }

    private String detecterPointFaible(SuiviBienEtre s) {
        if (s == null) return "Aucun point faible détecté pour le moment";

        int sommeil = s.getQualiteSommeil();
        int energie = s.getNiveauEnergie();
        int stressProbleme = s.getNiveauStress();
        int alimentation = s.getQualiteAlimentation();

        int stressInverse = 10 - stressProbleme;

        int min = Math.min(Math.min(sommeil, energie), Math.min(stressInverse, alimentation));

        if (min == sommeil) return "Qualité du sommeil insuffisante";
        if (min == energie) return "Niveau d'énergie faible";
        if (min == stressInverse) return "Stress trop élevé";
        return "Qualité de l'alimentation à améliorer";
    }

    private List<String> construireResumeSuivis(List<SuiviBienEtre> suivis) {
        List<String> resume = new ArrayList<>();

        if (suivis == null || suivis.isEmpty()) {
            return resume;
        }

        List<SuiviBienEtre> tries = suivis.stream()
                .sorted(Comparator.comparing(SuiviBienEtre::getDateSaisie).reversed())
                .limit(3)
                .toList();

        for (SuiviBienEtre s : tries) {
            resume.add(
                    s.getDateSaisie() + " | Humeur: " + s.getHumeur()
                            + " | Score: " + String.format(Locale.US, "%.0f", s.getScore()) + "/100"
                            + " | Sommeil: " + s.getQualiteSommeil() + "/10"
                            + " | Énergie: " + s.getNiveauEnergie() + "/10"
                            + " | Stress: " + s.getNiveauStress() + "/10"
                            + " | Alimentation: " + s.getQualiteAlimentation() + "/10"
            );
        }

        return resume;
    }

    private String requireString(JSONObject object, String key) {
        String value = object.optString(key, null);

        if (value == null || value.isBlank()) {
            throw new RuntimeException("Champ JSON manquant ou vide : " + key);
        }

        return value.trim();
    }

    private String requireEnum(JSONObject object, String key, List<String> allowed) {
        String value = requireString(object, key).toUpperCase(Locale.ROOT);

        if (!allowed.contains(value)) {
            throw new RuntimeException("Valeur invalide pour " + key + " : " + value);
        }

        return value;
    }

    private List<String> requireStringList(JSONObject object, String key) {
        JSONArray array = object.optJSONArray(key);

        if (array == null) {
            throw new RuntimeException("Liste JSON manquante : " + key);
        }

        List<String> list = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, "").trim();

            if (!value.isBlank()) {
                list.add(value);
            }
        }

        return list;
    }
}