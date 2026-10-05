package com.example.ForgeHubs.ServiceImpl;

import com.example.ForgeHubs.Service.TwoFactorService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class TwoFactorServiceImpl  implements TwoFactorService {

    private final SecretGenerator secretGenerator;

    private final CodeVerifier codeVerifier;

    public TwoFactorServiceImpl(){
        this.secretGenerator = new DefaultSecretGenerator();
        this.codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(),  new SystemTimeProvider());
    }
    @Override
    public String generateSecret() {
        return secretGenerator.generate();
    }
    @Override
    public String generateQrCodeUri(String email , String secret){
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("ForgeHubs")
                .build();
        return data.getUri();
    }

    @Override
    public boolean verifyCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }

    @Override
   public String generateQrCode(String email , String secret){
     String uri = generateQrCodeUri(email, secret);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(uri, BarcodeFormat.QR_CODE,300,300);
            BufferedImage image = new  BufferedImage(300,300,BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    image.setRGB(
                            x,
                            y,
                            bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF
                    );
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image,"PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());



        } catch (WriterException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
