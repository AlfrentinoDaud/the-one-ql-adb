package Blockchain;

import java.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Class SecureTransaction berisi semua fungsi kriptografi
 * yang digunakan dalam sistem blockchain reward DTN.
 * 
 * Fungsinya meliputi:
 * - Hash SHA256
 * - Digital signature (ECDSA)
 * - Verifikasi signature
 * - Commutative Encryption (sesuai paper)
 */
public class SecureTransaction {

    /**
     * Static block untuk memastikan provider kriptografi
     * Bouncy Castle sudah terdaftar sebelum digunakan.
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Fungsi untuk membuat hash SHA-256
     * Biasanya digunakan untuk:
     * - ID transaksi
     * - Hash block
     * - Hash secret (R1, R2)
     * 
     * @param input data yang ingin di-hash
     * @return hash dalam bentuk string hexadecimal
     */
    public static String sha256(String input) {

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

    public static String applySha256(String input) {
        return sha256(input);
    }

    /**
     * Fungsi untuk menandatangani data menggunakan
     * private key (digital signature).
     * 
     * Digunakan agar transaksi tidak bisa dipalsukan.
     *
     * @param privateKey kunci privat pemilik wallet
     * @param data       data yang ingin ditandatangani
     * @return signature dalam bentuk byte[]
     */
    public static byte[] sign(PrivateKey privateKey,
            String data) {

        try {

            Signature signature = Signature.getInstance("ECDSA", "BC");

            signature.initSign(privateKey);

            signature.update(data.getBytes());

            return signature.sign();

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Fungsi untuk memverifikasi signature transaksi.
     * 
     * Digunakan oleh miner untuk memastikan bahwa
     * transaksi benar-benar dibuat oleh pengirimnya.
     *
     * @param publicKey public key pengirim
     * @param data      data asli
     * @param signature signature yang diberikan
     * @return true jika signature valid
     */
    public static boolean verify(PublicKey publicKey,
            String data,
            byte[] signature) {

        try {

            Signature verifier = Signature.getInstance("ECDSA", "BC");

            verifier.initVerify(publicKey);

            verifier.update(data.getBytes());

            return verifier.verify(signature);

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Mengubah key menjadi string agar mudah disimpan
     * atau dijadikan bagian dari hash.
     *
     * @param key public/private key
     * @return string Base64
     */
    public static String keyToString(Key key) {

        return java.util.Base64.getEncoder()
                .encodeToString(key.getEncoded());
    }

    /*
     * =====================================================
     * COMMUTATIVE ENCRYPTION (SIMULASI)
     * =====================================================
     */

    /**
     * Fungsi ini mensimulasikan commutative encryption
     * seperti yang digunakan pada paper.
     *
     * Sifat commutative:
     * Encrypt(A, Encrypt(B, M)) =
     * Encrypt(B, Encrypt(A, M))
     *
     * Artinya urutan enkripsi tidak mempengaruhi hasil.
     *
     * @param key  public key node
     * @param data data yang dienkripsi
     * @return encrypted string
     */
    public static String commutativeEncrypt(PublicKey key,
            String data) {

        String keyString = keyToString(key);

        String combined;

        /*
         * Sorting sederhana agar hasil hash selalu sama
         * meskipun urutan kunci berbeda
         */
        if (keyString.compareTo(data) < 0)
            combined = keyString + data;
        else
            combined = data + keyString;

        return sha256(combined);
    }

    /**
     * Enkripsi dua lapis (double encryption)
     * Digunakan ketika message melewati dua node.
     *
     * @param k1   public key node pertama
     * @param k2   public key node kedua
     * @param data data asli
     */
    public static String doubleEncrypt(PublicKey k1,
            PublicKey k2,
            String data) {

        String first = commutativeEncrypt(k1, data);

        return commutativeEncrypt(k2, first);
    }
}