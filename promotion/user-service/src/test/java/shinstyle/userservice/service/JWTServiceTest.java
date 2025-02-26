package shinstyle.userservice.service;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

class JWTServiceTest {

    @Test
    void createSecretKey() throws NoSuchAlgorithmException {

        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();
        String secretKeyBase64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("secretKeyBase64 = " + secretKeyBase64);
    }
}
