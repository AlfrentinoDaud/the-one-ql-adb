package Blockchain;

import java.security.PublicKey;
import java.util.List;

/**
 * Validator smart-contract escrow + payment chain
 *
 * Validasi:
 * 1) Deposit belum expired
 * 2) R2 cocok dengan commitment h2
 * 3) First block harus GENESIS
 * 4) PrevHash chain valid
 * 5) EncR1 valid
 * 6) Signature tiap payment valid
 * 7) (optional) ACK chain validation
 */
public class Validator {

    public static int successCount = 0;
    public static int failCount = 0;

    public static boolean validateMultiHop(
            Deposit deposit,
            List<Payment> chain,
            String R1,
            String R2,
            long now) {

        // ==========================
        // sanity check
        // ==========================
        if (deposit == null) {
            System.out.println("VALIDATOR FAIL: deposit null");
            failCount++;
            return false;
        }

        if (chain == null || chain.isEmpty()) {
            System.out.println("VALIDATOR FAIL: chain kosong");
            failCount++;
            return false;
        }

        // ==========================
        // (0) expiry check
        // ==========================
        if (deposit.isExpired(now)) {
            System.out.println("VALIDATOR FAIL: deposit expired");
            failCount++;
            return false;
        }

        // ==========================
        // (1) R2 commitment check
        // ==========================
        String h2 = SecureTransaction.sha256(R2);

        if (!h2.equals(deposit.getH2())) {
            System.out.println("VALIDATOR FAIL: R2 mismatch");
            failCount++;
            return false;
        }

        // ==========================
        // (2) GENESIS check (first tx)
        // ==========================
        Payment first = chain.get(0);

        if (chain.size() == 1) {
            if (first.getPrevTxHash() != null) {
                System.out.println("VALIDATOR FAIL: first tx should be null prevHash");
                failCount++;
                return false;
            }
        }

        // ==========================
        // (3) route chain validation
        // tx[i].prev == tx[i-1].hash
        // ==========================
        for (int i = 1; i < chain.size(); i++) {

            String prevHash = chain.get(i).getPrevTxHash();
            String expectedHash = chain.get(i - 1).getTxHash();

            if (prevHash == null || !prevHash.equals(expectedHash)) {
                System.out.println("VALIDATOR FAIL: route chain mismatch");
                failCount++;
                return false;
            }
        }

        // ==========================
        // (4) EncR1 proof validation
        // receiver pertama harus punya proof R1
        // ==========================
        PublicKey firstReceiver = first.getReceiver();

        String expectedEncR1 = SecureTransaction.commutativeEncrypt(
                firstReceiver,
                R1);

        if (first.getEncR1() == null ||
                !expectedEncR1.equals(first.getEncR1())) {

            System.out.println("VALIDATOR FAIL: EncR1 mismatch");
            return false;
        }

        // ==========================
        // (5) signature validation
        // ==========================
        for (Payment p : chain) {

            if (!p.verify()) {
                System.out.println("VALIDATOR FAIL: signature invalid");
                failCount++;
                return false;
            }
        }

        /*
         * // ==========================
         * // (6) OPTIONAL ACK CHAIN VALIDATION
         * // aktifkan kalau Payment sudah support:
         * // - verifyACK()
         * // - EncAck antar hop
         * // ==========================
         * for (int i = 0; i < chain.size(); i++) {
         * 
         * Payment p = chain.get(i);
         * 
         * if (!p.verifyACK(p.getReceiver())) {
         * System.out.println("VALIDATOR FAIL: ACK invalid");
         * return false;
         * }
         * 
         * if (i < chain.size() - 1) {
         * 
         * Payment next = chain.get(i + 1);
         * 
         * String ack = p.getEncAck();
         * 
         * String left =
         * SecureTransaction.commutativeEncrypt(
         * p.getReceiver(),
         * SecureTransaction.commutativeEncrypt(
         * next.getReceiver(),
         * ack));
         * 
         * String right =
         * SecureTransaction.commutativeEncrypt(
         * next.getReceiver(),
         * SecureTransaction.commutativeEncrypt(
         * p.getReceiver(),
         * ack));
         * 
         * if (!left.equals(right)) {
         * System.out.println("VALIDATOR FAIL: ACK chain mismatch");
         * return false;
         * }
         * }
         * }
         */
        successCount++;
        System.out.println("VALIDATOR SUCCESS");
        return true;
    }
}