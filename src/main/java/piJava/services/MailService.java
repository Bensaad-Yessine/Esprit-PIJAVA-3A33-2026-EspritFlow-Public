package piJava.services;

import piJava.utils.EnvConfig;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MailService {

	private final String host = EnvConfig.get("SMTP_HOST", "");
	private final int port = EnvConfig.getInt("SMTP_PORT", 587);
	private final String username = EnvConfig.get("SMTP_USER", "");
	private final String password = EnvConfig.get("SMTP_PASSWORD", "");
	private final String from = EnvConfig.get("SMTP_FROM", username);
	private final boolean startTls = Boolean.parseBoolean(EnvConfig.get("SMTP_STARTTLS", "true"));
	private final boolean ssl = Boolean.parseBoolean(EnvConfig.get("SMTP_SSL", "false"));

	public void sendPasswordResetEmail(String to, String subject, String body) throws IOException {
		if (host.isBlank() || username.isBlank() || password.isBlank()) {
			throw new IOException("Configuration SMTP manquante. Renseignez SMTP_HOST, SMTP_PORT, SMTP_USER et SMTP_PASSWORD dans .env.");
		}

		if (ssl) {
			try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
				 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
				 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII))) {
				socket.startHandshake();
				sendMailConversation(reader, writer, to, subject, body);
			}
			return;
		}

		try (Socket socket = new Socket(host, port);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
			 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII))) {

			expectCode(reader, 220);
			sendCommand(writer, "EHLO localhost");
			readMultilineResponse(reader, 250);

			if (startTls) {
				sendCommand(writer, "STARTTLS");
				expectCode(reader, 220);
				SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(socket, host, port, true);
				sslSocket.startHandshake();
				try (SSLSocket secureSocket = sslSocket;
					 BufferedReader secureReader = new BufferedReader(new InputStreamReader(secureSocket.getInputStream(), StandardCharsets.US_ASCII));
					 BufferedWriter secureWriter = new BufferedWriter(new OutputStreamWriter(secureSocket.getOutputStream(), StandardCharsets.US_ASCII))) {
					sendMailConversation(secureReader, secureWriter, to, subject, body);
				}
			} else {
				sendMailConversation(reader, writer, to, subject, body);
			}
		}
	}

	private void sendMailConversation(BufferedReader reader, BufferedWriter writer, String to, String subject, String body) throws IOException {
		sendCommand(writer, "EHLO localhost");
		readMultilineResponse(reader, 250);

		sendCommand(writer, "AUTH LOGIN");
		expectCode(reader, 334);
		sendCommand(writer, Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)));
		expectCode(reader, 334);
		sendCommand(writer, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
		expectCode(reader, 235);

		sendCommand(writer, "MAIL FROM:<" + from + ">");
		expectCode(reader, 250);
		sendCommand(writer, "RCPT TO:<" + to + ">");
		expectCode(reader, 250);
		sendCommand(writer, "DATA");
		expectCode(reader, 354);

		writer.write("From: " + from + "\r\n");
		writer.write("To: " + to + "\r\n");
		writer.write("Subject: " + subject + "\r\n");
		writer.write("MIME-Version: 1.0\r\n");
		writer.write("Content-Type: text/html; charset=UTF-8\r\n");
		writer.write("\r\n");
		writer.write(body.replace("\n", "\r\n"));
		writer.write("\r\n.\r\n");
		writer.flush();
		expectCode(reader, 250);

		sendCommand(writer, "QUIT");
	}

	private void sendCommand(BufferedWriter writer, String command) throws IOException {
		writer.write(command);
		writer.write("\r\n");
		writer.flush();
	}

	private void expectCode(BufferedReader reader, int expectedCode) throws IOException {
		String line = reader.readLine();
		if (line == null || !line.startsWith(String.valueOf(expectedCode))) {
			throw new IOException("SMTP error: expected " + expectedCode + " but got: " + line);
		}
	}

	private void readMultilineResponse(BufferedReader reader, int expectedCode) throws IOException {
		String line;
		do {
			line = reader.readLine();
			if (line == null) {
				throw new IOException("SMTP error: unexpected end of stream.");
			}
		} while (line.length() >= 4 && line.charAt(3) == '-');

		if (!line.startsWith(String.valueOf(expectedCode))) {
			throw new IOException("SMTP error: expected " + expectedCode + " but got: " + line);
		}
	}
}

