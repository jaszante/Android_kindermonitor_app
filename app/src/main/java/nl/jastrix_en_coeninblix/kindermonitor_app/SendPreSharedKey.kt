package nl.jastrix_en_coeninblix.kindermonitor_app

import android.os.AsyncTask
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.util.Log;
import nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses.EmailContent
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

// this whole class is outside the scope of the project but nessesary since the API is not filled with new measurements from actual sensors
class SendPreSharedKey(emailContents: ArrayList<EmailContent>, patient: String) : AsyncTask<Void, Void, Boolean>() {
    override fun doInBackground(vararg params: Void?): Boolean {
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

//            sendEmail()
            val transport = mailSession.getTransport("smtp");
            transport.connect(emailHost, fromEmail, fromPassword);
            transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
            transport.close();

            return true
        }
        catch (e: Exception)
        {
            Log.d(e.message, e.message)
            return false
        }
    }

    val emailPort = "587"
    val smtpAuth = "true"
    val starttls = "true"
    val emailHost = "smtp.gmail.com"

    var fromEmail: String
    var fromPassword: String
    //    List toEmailList;
    var emailSubject: String
    var emailBody: String = ""
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

        for (emailContent in emailContents) {
            emailBody += emailContent.preSharedKey + " id: " + emailContent.sensorID + " type: " + emailContent.sensorType + "["
        }

        toEmail = "projectpresharedkeys@gmail.com"

//        sendPreSharedKeyToEmail()
    }

    fun sendPreSharedKeyToEmail()
    {
        execute()// doInBackground().execute()
    }

    private fun sendEmail() //throws AddressException, MessagingException
    {
        val transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromEmail, fromPassword);
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
    }

}