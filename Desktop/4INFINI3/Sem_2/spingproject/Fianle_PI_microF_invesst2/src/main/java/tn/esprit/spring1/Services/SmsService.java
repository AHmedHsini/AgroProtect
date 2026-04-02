package tn.esprit.spring1.Services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private final String ACCOUNT_SID = "AC686c2f234af7117fb144b3f0beeb0944";
    private final String AUTH_TOKEN = "ae55c2ae9bb23c16ec7258895dbaad6a";
    private final String FROM_NUMBER = "+15186289450"; // numéro Twilio

    public void sendSms(String to, String body) {

        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(FROM_NUMBER),
                    body
            ).create();

            System.out.println("✅ SMS sent: " + message.getSid());

        } catch (Exception e) {
            System.out.println("❌ SMS failed: " + e.getMessage());
        }
    }
}