package Blockchain;

import java.util.*;

/**
 * Class Block merepresentasikan satu block
 * dalam blockchain.
 * 
 * Setiap block berisi:
 * - daftar transaksi (Payment)
 * - hash block sebelumnya
 * - timestamp
 * - nonce (untuk mining)
 */
public class Block {

    /**
     * Hash block sebelumnya
     */
    private String previousHash;

    /**
     * Daftar transaksi reward
     */
    private List<Payment> payments;

    /**
     * Waktu pembuatan block
     */
    private long timestamp;

    /**
     * Nonce digunakan untuk proses mining
     */
    private int nonce;

    /**
     * Hash block saat ini
     */
    private String hash;

    /**
     * Constructor block
     */
    public Block(String previousHash,
            List<Payment> payments) {

        this.previousHash = previousHash;
        this.payments = payments;

        this.timestamp = System.currentTimeMillis();

        this.hash = calculateHash();
    }

    /**
     * Menghitung hash block
     */
    public String calculateHash() {

        String data = previousHash +
                timestamp +
                nonce;

        /*
         * Tambahkan semua transaksi ke data hash
         */
        for (Payment p : payments) {

            data += p.getValue();
        }

        return SecureTransaction.sha256(data);
    }

    /**
     * Proses mining sederhana
     *
     * Block dianggap valid jika hash
     * diawali dengan sejumlah nol sesuai difficulty
     */
    public void mine(int difficulty) {

        String target = new String(new char[difficulty])
                .replace('\0', '0');

        while (!hash.substring(0, difficulty)
                .equals(target)) {

            nonce++;

            hash = calculateHash();
        }
    }

    public String getHash() {
        return hash;
    }
}