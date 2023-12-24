package ro.ubbcluj.map.socialnetworkgui.utils;

import ro.ubbcluj.map.socialnetworkgui.domain.FriendRequest;

import javax.crypto.*;
import java.security.*;
import java.util.Base64;
import java.util.UUID;

/**
 * Clasa ce contine metode care nu tin neaparat de entitatile proiectului.
 * Contine functii generale self made.
 */


public class Utils {
    /**
     * Genereaza un ID unic de tipul Long.
     *
     * @return un id unic
     */
    public static Long generateUniqueLongID() {
        long val = -1;
        do {
            val = UUID.randomUUID().getMostSignificantBits();
        } while (val < 0);
        return val;
    }

    public static FriendRequest decideStatus(String status){
        return switch (status) {
            case "accepted" -> FriendRequest.ACCEPTED;
            case "rejected" -> FriendRequest.REJECTED;
            case "pending" -> FriendRequest.PENDING;
            default -> null;
        };
    }

    public static String statusToString(FriendRequest status){
        return switch (status){
            case ACCEPTED -> "accepted";
            case PENDING -> "pending";
            case REJECTED -> "rejected";
        };
    }

    static Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(String input, SecretKey secretKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] inputTextByte = input.getBytes();

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedByte = cipher.doFinal(inputTextByte);

        Base64.Encoder encoder = Base64.getEncoder();

        return encoder.encodeToString(encryptedByte);
    }

    public static String decrypt(String input, SecretKey secretKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Base64.Decoder decoder = Base64.getDecoder();

        byte[] encryptedTextByte = decoder.decode(input);

        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);

        return new String(decryptedByte);
    }
}