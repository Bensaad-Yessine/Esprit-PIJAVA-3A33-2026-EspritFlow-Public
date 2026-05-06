package piJava.tests;

import org.json.JSONArray;
import org.json.JSONObject;
import piJava.services.api.GroqClient;

public class TestGroq {

    public static void main(String[] args) {
        try {
            GroqClient groqClient = new GroqClient();

            JSONArray messages = new JSONArray();

            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "Tu es un coach santé. Réponds uniquement en JSON.")
            );

            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", """
                            Donne un coaching simple pour un objectif santé.
                            Réponds avec ce JSON:
                            {
                              "titre": "...",
                              "message": "...",
                              "conseil": "...",
                              "niveau": "LOW|MEDIUM|HIGH"
                            }
                            """)
            );

            JSONObject result = groqClient.chatJson(messages, 0.4, 300);

            System.out.println("Titre : " + result.optString("titre"));
            System.out.println("Message : " + result.optString("message"));
            System.out.println("Conseil : " + result.optString("conseil"));
            System.out.println("Niveau : " + result.optString("niveau"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}