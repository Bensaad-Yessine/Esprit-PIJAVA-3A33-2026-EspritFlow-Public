package piJava.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import piJava.entities.Seance;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "Smart Room AI Calendar Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static Calendar service;

    public static Calendar getCalendarService() {
        if (service != null) return service;
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                System.err.println("WARNING: Google Calendar credentials.json introuvable dans src/main/resources/");
                return null;
            }
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

            service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Google Calendar API initialization failed: " + e.getMessage());
        }
        return service;
    }

    public static String createEvent(Seance seance, String matiereName, String classeName, String salleName) {
        try {
            Calendar calendar = getCalendarService();
            if (calendar == null) return null;

            Event event = new Event()
                .setSummary("Séance " + matiereName + " - " + classeName)
                .setLocation(salleName)
                .setDescription("Matière : " + matiereName + "\nClasse : " + classeName + "\nSalle : " + salleName);

            DateTime startDateTime = new DateTime(seance.getHeureDebut().getTime());
            EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Africa/Tunis");
            event.setStart(start);

            DateTime endDateTime = new DateTime(seance.getHeureFin().getTime());
            EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Africa/Tunis");
            event.setEnd(end);

            event = calendar.events().insert("primary", event).execute();
            System.out.println("Google Calendar Event created: " + event.getHtmlLink());
            return event.getId();

        } catch (Exception e) {
            System.err.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void updateEvent(String eventId, Seance seance, String matiereName, String classeName, String salleName) {
        if (eventId == null || eventId.isEmpty()) return;
        try {
            Calendar calendar = getCalendarService();
            if (calendar == null) return;

            Event event = calendar.events().get("primary", eventId).execute();
            
            event.setSummary("Séance " + matiereName + " - " + classeName)
                 .setLocation(salleName)
                 .setDescription("Matière : " + matiereName + "\nClasse : " + classeName + "\nSalle : " + salleName);

            DateTime startDateTime = new DateTime(seance.getHeureDebut().getTime());
            EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("Africa/Tunis");
            event.setStart(start);

            DateTime endDateTime = new DateTime(seance.getHeureFin().getTime());
            EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("Africa/Tunis");
            event.setEnd(end);

            calendar.events().update("primary", event.getId(), event).execute();
            System.out.println("Google Calendar Event updated.");

        } catch (Exception e) {
            System.err.println("Error updating event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteEvent(String eventId) {
        if (eventId == null || eventId.isEmpty()) return;
        try {
            Calendar calendar = getCalendarService();
            if (calendar == null) return;
            calendar.events().delete("primary", eventId).execute();
            System.out.println("Google Calendar Event deleted.");
        } catch (Exception e) {
            System.err.println("Error deleting event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
