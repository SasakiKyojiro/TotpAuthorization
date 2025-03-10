package ru.istokmw.testotp.integration;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.istokmw.testotp.jpa.Member;
import ru.istokmw.testotp.jpa.TOTP;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Component
@Slf4j
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

    public String getQrCode(Member member, TOTP totp) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(member.getName())
                .secret(totp.getSecret())
                .issuer("TestTOTP")
                .algorithm(HashingAlgorithm.SHA256)
                .digits(8)
                .period(30)
                .build();
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        String mimeType = generator.getImageMimeType();
        log.info("Generate QR code for mime type {}", mimeType);
        return getDataUriForImage(imageData, mimeType);
    }
}
