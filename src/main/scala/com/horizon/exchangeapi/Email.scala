/** Class for sending email to an exchange user. */
package com.horizon.exchangeapi

import javax.mail._
import javax.mail.internet._
import java.util.Date
import java.util.Properties
import scala.collection.JavaConversions._
import scala.util._

/** Encapsulates sending email to exchange users */
object Email {
	// val ibmRelay = "na.relay.ibm.com"
 //  val gmailRelay = "smtp.gmail.com"
  val fromAddress = "noreply@bluehorizon.network"
  val subject = "Horizon password reset token"

  // var smtpHost = gmailRelay     //TODO: determine if localhost has a relay

  /** Sends email to the specified email
   * @return Success(success-msg) or Failure(exception)
   */
  def send(username: String, toAddress: String, token: String): Try[String] = {
    // Check toAddress
    val toAddressObj = try { new InternetAddress(toAddress) } catch { case e: Exception => return Failure(new Exception("bad email address format")) }

    // Get info from the config file
    val host = ExchConfig.getString("api.smtp.host")
    if (host == "") return Failure(new Exception("api.smtp.host not set in config.json"))
    val user = ExchConfig.getString("api.smtp.user")
    val password = ExchConfig.getString("api.smtp.password")
    val auth = (user != "" && password != "")

    // Create msg object
    val properties = new Properties()
    if (auth) {
      properties.put("mail.smtp.port", "587")
      properties.put("mail.smtp.auth", "true")
      properties.put("mail.smtp.starttls.enable", "true")
    } else {
      properties.put("mail.smtp.host", host)
    }
    val session = Session.getDefaultInstance(properties, null)
    val message = new MimeMessage(session)

    // Set properties of the email
    message.setFrom(new InternetAddress(fromAddress))
    message.setRecipients(Message.RecipientType.TO, Array[Address](toAddressObj))
    message.setSentDate(new Date())
    message.setSubject(subject)

    // Set the email body content
    val content = "You requested that your Horizon Exchange password be changed for username '" + username + "'. To change your password, copy/paste the token below into the Horizon registration UI. The token will expire in 5 minutes. If you did not request to have your password reset, simply ignore this email.\n\n" + token
    message.setText(content)
    // message.setText(content, "text/html")

    // Send the email
    try {
      if (auth) {
        val transport = session.getTransport("smtp")
        transport.connect(host, user, password)
        transport.sendMessage(message, message.getAllRecipients())
        transport.close()
      } else {
        Transport.send(message)   // for non-authenticate svrs
      }
    }
    catch {
      //TODO: determine which exceptions mean we should retry
      case e: com.sun.mail.util.MailConnectException => return Failure(e)     // this usually means that host is not running a smtp relay svr
      case e: Exception => return Failure(e)
    }
    // println("email sent to "+toAddress)

    return Success("password reset token emailed to "+toAddress)
  }
}