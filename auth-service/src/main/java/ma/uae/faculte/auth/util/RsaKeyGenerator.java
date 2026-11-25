
package ma.uae.faculte.auth.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class RsaKeyGenerator {

    public static void main(String[] args) throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keys = generator.generateKeyPair();

        Files.createDirectories(Path.of("keys"));

        save("keys/private.pem", "PRIVATE KEY", keys.getPrivate().getEncoded());
        save("keys/public.pem", "PUBLIC KEY", keys.getPublic().getEncoded());

        System.out.println("Cles creees dans auth-service/keys/");
    }

    private static void save(String file, String type, byte[] bytes)
            throws Exception {

        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(bytes);

        String pem = "-----BEGIN " + type + "-----\n" + base64
                + "\n-----END " + type + "-----\n";

        Files.writeString(Path.of(file), pem);
    }
}
