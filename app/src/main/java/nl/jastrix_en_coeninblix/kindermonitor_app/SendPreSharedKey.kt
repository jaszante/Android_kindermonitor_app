package nl.jastrix_en_coeninblix.kindermonitor_app

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.util.Log;
import java.lang.Exception
import java.util.*

// this whole class is outside the scope of the project but nessesary since the API is not filled with new measurements from actual sensors
class SendPreSharedKey(preSharedKey: String, patient: String) {

    val emailPort = "587"
    val smtpAuth = "true"
    val starttls = "true"
    val emailHost = "smtp.gmail.com"

    var fromEmail: String
    var fromPassword: String
    //    List toEmailList;
    var emailSubject: String
    var emailBody: String
    var toEmail: String

    var emailProperties: Properties
    lateinit var mailSession: Session
    //    MimeMessage emailMessage;
    lateinit var emailMessage: MimeMessage

    init {
        emailProperties = System.getProperties()
        emailProperties.put("mail.smtp.port", emailPort)
        emailProperties.put("mail.smtp.auth", smtpAuth)
        emailProperties.put("mail.smtp.starttls.enable", starttls)

        fromEmail = "projectpresharedkeys@gmail.com"
        fromPassword = "A1324657"
        emailSubject = patient
        emailBody = preSharedKey
        toEmail = "projectpresharedkeys@gmail.com"

//        sendPreSharedKeyToEmail()
    }

//    init {
//        emailProperties = System.getProperties()
//        emailProperties.put("mail.smtp.port", emailPort)
//        emailProperties.put("mail.smtp.auth", smtpAuth)
//        emailProperties.put("mail.smtp.starttls.enable", starttls)
//
//        fromEmail = "projectpresharedkeys@gmail.com"
//        fromPassword = "A1324657"
//        emailSubject = patient
//        emailBody = preSharedKey
//        toEmail = "projectpresharedkeys@gmail.com"
//
//        sendPreSharedKeyToEmail()
//    }

//    class SendPreSharedKey constructor()
//    {
//        emailProperties = System.getProperties()
//    }

//    public GMail(String fromEmail, String fromPassword,
//    List toEmailList, String emailSubject, String emailBody)
//    {
//        this.fromEmail = fromEmail;
//        this.fromPassword = fromPassword;
//        this.toEmailList = toEmailList;
//        this.emailSubject = emailSubject;
//        this.emailBody = emailBody;
//
//        emailProperties = System.getProperties();
//        emailProperties.put("mail.smtp.port", emailPort);
//        emailProperties.put("mail.smtp.auth", smtpAuth);
//        emailProperties.put("mail.smtp.starttls.enable", starttls);
//        Log.i("GMail", "Mail server properties set.");
//    }

    fun sendPreSharedKeyToEmail()//: MimeMessage //throws AddressException,
    // MessagingException, UnsupportedEncodingException
    {
        try {
            mailSession = Session.getDefaultInstance(emailProperties, null);
            emailMessage = MimeMessage(mailSession)

            emailMessage.setFrom(InternetAddress(fromEmail, fromEmail))

            emailMessage.addRecipient(
                Message.RecipientType.TO,
                InternetAddress(toEmail)
            )

            emailMessage.setSubject(emailSubject)
            emailMessage.setContent(emailBody, "text/html")

            sendEmail()
        }
        catch (e: Exception)
        {
            Log.d(e.message, e.message)
        }
    }

    private fun sendEmail() //throws AddressException, MessagingException
    {
        val transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromEmail, fromPassword);
//        Log.i("GMail", "allrecipients: " + emailMessage.getAllRecipients());
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
//        Log.i("GMail", "Email sent successfully.");
    }

}