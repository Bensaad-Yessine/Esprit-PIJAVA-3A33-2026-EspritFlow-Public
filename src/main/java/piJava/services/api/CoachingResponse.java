package piJava.services.api;

import java.util.ArrayList;
import java.util.List;

public class CoachingResponse {

    private String titre;
    private String messageMotivation;
    private String resumeAnalyse;
    private String niveau;
    private double scoreMoyen;
    private String tendance;
    private String pointFort;
    private String pointFaible;

    private List<String> conseilsTypeObjectif;
    private List<String> conseilsNiveauxFaibles;
    private List<String> resumeSuivis;

    private String messageConfiance;

    public CoachingResponse() {
        this.conseilsTypeObjectif = new ArrayList<>();
        this.conseilsNiveauxFaibles = new ArrayList<>();
        this.resumeSuivis = new ArrayList<>();
    }

    public CoachingResponse(String titre,
                            String messageMotivation,
                            String resumeAnalyse,
                            String niveau,
                            double scoreMoyen,
                            String tendance,
                            String pointFort,
                            String pointFaible,
                            List<String> conseilsTypeObjectif,
                            List<String> conseilsNiveauxFaibles,
                            List<String> resumeSuivis,
                            String messageConfiance) {
        this.titre = titre;
        this.messageMotivation = messageMotivation;
        this.resumeAnalyse = resumeAnalyse;
        this.niveau = niveau;
        this.scoreMoyen = scoreMoyen;
        this.tendance = tendance;
        this.pointFort = pointFort;
        this.pointFaible = pointFaible;
        this.conseilsTypeObjectif = conseilsTypeObjectif != null ? conseilsTypeObjectif : new ArrayList<>();
        this.conseilsNiveauxFaibles = conseilsNiveauxFaibles != null ? conseilsNiveauxFaibles : new ArrayList<>();
        this.resumeSuivis = resumeSuivis != null ? resumeSuivis : new ArrayList<>();
        this.messageConfiance = messageConfiance;
    }

    public String getTitre() {
        return titre;
    }

    public String getMessageMotivation() {
        return messageMotivation;
    }

    public String getResumeAnalyse() {
        return resumeAnalyse;
    }

    public String getNiveau() {
        return niveau;
    }

    public double getScoreMoyen() {
        return scoreMoyen;
    }

    public String getTendance() {
        return tendance;
    }

    public String getPointFort() {
        return pointFort;
    }

    public String getPointFaible() {
        return pointFaible;
    }

    public List<String> getConseilsTypeObjectif() {
        return conseilsTypeObjectif;
    }

    public List<String> getConseilsNiveauxFaibles() {
        return conseilsNiveauxFaibles;
    }

    public List<String> getResumeSuivis() {
        return resumeSuivis;
    }

    public String getMessageConfiance() {
        return messageConfiance;
    }
}