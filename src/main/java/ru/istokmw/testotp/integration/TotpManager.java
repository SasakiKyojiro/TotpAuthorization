package ru.istokmw.testotp.integration;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Component;

@Component
public class TotpManager {
    private final SecretGenerator secretGenerator;
    private final CodeVerifier verifier;
    private final CodeGenerator codeGenerator;

    public TotpManager() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA256, 8);
        this.verifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());
    }

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public boolean verifyCode(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}
