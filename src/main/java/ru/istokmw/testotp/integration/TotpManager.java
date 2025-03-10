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
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TotpManager {
    @Value("${2fa_size_code}")
    private Integer codeSize;
    @Value("${2fa_name}")
    private String name;
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private CodeVerifier verifier;
    private final RecoveryCodeGenerator recoveryCodes = new RecoveryCodeGenerator();

    @PostConstruct
    public void initVerifier() {
        this.verifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(HashingAlgorithm.SHA256, codeSize),
                new SystemTimeProvider());
    }

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String[] generateRecovery() {
        return recoveryCodes.generateCodes(16);
    }

    public Mono<Boolean> verifyCode(Mono<String> secret, String code) {
        return secret
                .map(tmp -> verifier.isValidCode(tmp, code));
    }


    public Mono<byte[]> getQrCode(String email, String secret) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer(name)
                .algorithm(HashingAlgorithm.SHA256)
                .digits(codeSize)
                .period(30)
                .build();
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        return Mono.just(imageData);
        //return Mono.just(new ByteArrayInputStream(imageData));
        //String mimeType = generator.getImageMimeType();
        //log.info("Generate QR code for mime type {}", mimeType);
        //return Mono.just(getDataUriForImage(imageData, mimeType));
    }

    public Mono<ResponseEntity<byte[]>> getImage(byte[] imageBytes) {
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview.png\"")
                .body(imageBytes));
    }


//    public Mono<ResponseEntity<Flux<DataBuffer>>> getImage(InputStream stream) {
//        Flux<DataBuffer> flux = DataBufferUtils.readInputStream(() -> stream, new DefaultDataBufferFactory(), 4096);
//        return Mono.just(ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr.png\"")
//                .contentType(MediaType.IMAGE_PNG)
//                .body(flux));
//    }
}
