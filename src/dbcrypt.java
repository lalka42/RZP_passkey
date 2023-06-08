import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class dbcrypt {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public static void main(String[] args) {
        String inputFile = "/path/to/input/file";
        String encryptedFile = "/path/to/encrypted/file";
        String decryptedFile = "/path/to/decrypted/file";
        String key = "MySecretKey123456"; // Замените на свой ключ шифрования

        try {
            encryptFile(inputFile, encryptedFile, key);
            decryptFile(encryptedFile, decryptedFile, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void encryptFile(String inputFile, String encryptedFile, String key) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(inputFile));

        SecretKey secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(fileBytes);

        Path outputPath = Paths.get(encryptedFile);
        Files.write(outputPath, encryptedBytes);
        System.out.println("Файл успешно зашифрован: " + encryptedFile);
    }

    public static void decryptFile(String encryptedFile, String decryptedFile, String key) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] encryptedBytes = Files.readAllBytes(Paths.get(encryptedFile));

        SecretKey secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        Path outputPath = Paths.get(decryptedFile);
        Files.write(outputPath, decryptedBytes);
        System.out.println("Файл успешно расшифрован: " + decryptedFile);
    }
}


