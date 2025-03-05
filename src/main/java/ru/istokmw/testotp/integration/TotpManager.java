package ru.istokmw.testotp.integration;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Component;

@Component
public class TotpManager {
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier verifier = new DefaultCodeVerifier(
            new DefaultCodeGenerator(HashingAlgorithm.SHA256, 8),
            new SystemTimeProvider());
    private final RecoveryCodeGenerator recoveryCodes = new RecoveryCodeGenerator();

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String[] generateRecovery() {
        return recoveryCodes.generateCodes(16);
    }

    public boolean verifyCode(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}
