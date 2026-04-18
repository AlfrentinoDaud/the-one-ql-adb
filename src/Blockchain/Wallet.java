package Blockchain;

import java.security.*;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

/**
 * Class Wallet merupakan dompet digital yang dimiliki tiap miner
 */
public class Wallet {
    
    /**
     * Private Key digunakan untuk menandatangani transaksi dan hanya
     * bisa diakses oleh miner itu sendiri. Anggap seperti PIN pribadi.
     */
    private PrivateKey privateKey;
    
    /**
     * Public Key digunakan sebagai identitas dompet, bisa diakses oleh pihak
     * lain. Anggap seperti nomor rekening jika ingin mengirim uang.
     */
    private PublicKey publicKey; 
    
    /**
     * Saldo wallet
     */
    private double balance;    
    
    static {
        /* Pastikan Bouncy Castle terdaftar */
        Security.addProvider(new BouncyCastleProvider()); 
    }
    
    /**
     * Saat sebuah wallet dibuat pertama kali, akan membuat pasangan kunci
     * yaitu PrivateKey dan PublicKey
     */
    public Wallet() {
        generateKeyPair();
        this.balance = 0; // set saldo awal 0
    }

    /**
    * Bikin pasangan kunci (public & private) pake ECDSA
    */
    private void generateKeyPair() {
        try {
            // 1. Gunakan algoritma "ECDSA"
            KeyPairGenerator keyGen = KeyPairGenerator.
                                      getInstance("ECDSA", "BC"); // Provider BC

            // 2. Pilih kurva elliptic (contoh: secp256k1 untuk Bitcoin)
            ECParameterSpec ecSpec = ECNamedCurveTable.
                                    getParameterSpec("secp256k1");
            keyGen.initialize(ecSpec);

            // 3. Generate key pair
            KeyPair pair = keyGen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate EC key pair", e);
        }
    }
    
    /**
    * Nambahin saldo (misal pas nerima transaksi)
    * @param amount Jumlah yang mau ditambah
    */
    public void addBalance(double amount) {
        this.balance += amount;
    }
    
    public PublicKey getPublicKey() {return publicKey;}

    public PrivateKey getPrivateKey() {return privateKey;}

    public double getBalance() {return balance;}
}
