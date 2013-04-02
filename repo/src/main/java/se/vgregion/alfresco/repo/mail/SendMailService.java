package se.vgregion.alfresco.repo.mail;

import java.util.List;

public interface SendMailService {

  /**
   * Send a plain text mail
   * 
   * @param subject
   * @param from
   * @param to
   * @param body
   */
  void sendTextMail(String subject, String from, String to, String body);

  /**
   * Send a plain text mail
   * 
   * @param subject
   * @param from
   * @param to list of authorities to send email to
   * @param body
   */
  void sendTextMail(String subject, String from, List<String> to, String body);

}
