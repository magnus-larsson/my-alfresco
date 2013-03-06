package se.vgregion.alfresco.repo.mail;

public interface SendMailService {

	/**
	 * Send a plain text mail
	 * @param subject
	 * @param from
	 * @param to
	 * @param body
	 */
	void sendTextMail(String subject, String from, String to, String body);

}
