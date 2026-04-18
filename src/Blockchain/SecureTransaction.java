package Blockchain;

import java.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SecureTransaction {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Bikin hash SHA-256 dari input (buat hash transaksi)
     * 
     * @param input - Data yang mau di-hash
     * @return Hash dalam bentuk hex string
     */
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Bikin tanda tangan digital pake private key
     * 
     * @param privateKey Kunci pribadi si pengirim
     * @param input      Data yang mau ditandatangani
     * @return Tanda tangan dalam bentuk byte array
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        try {
            Signature dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            dsa.update(input.getBytes());
            return dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mengecek apakah tanda tangan valid pakai public key
     * 
     * @param publicKey Kunci publik si pengirim
     * @param data      Data asli yang ditandatangani
     * @param signature Tanda tangan yang mau dicek
     * @return true kalo tanda tangan valid
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mengubah key (public/private) jadi string biar gampang disimpen
     * menggunakan Base64
     * 
     * @param key Kunci Publik atau Kunci Private
     * @return
     */
    public static String getStringFromKey(Key key) {
        return java.util.Base64.getEncoder().encodeToString(key.getEncoded());
    }

    //simulasi commutative encryption
    public static String encrypt(PublicKey key, String data) {
        return applySha256(getStringFromKey(key) + data);
    }
}
