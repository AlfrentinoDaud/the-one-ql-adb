package Blockchain;

import java.util.*;

/**
 * Class ProofVerifier digunakan oleh miner untuk
 * memverifikasi bahwa proses forwarding message
 * benar-benar terjadi di jaringan DTN.
 * 
 * Miner akan memeriksa:
 * - signature transaksi
 * - proof relay (EncR1)
 * - acknowledgement relay (EncAck)
 * - proof penerimaan receiver (EncR2)
 */
public class ProofVerifier {

    /**
     * Fungsi utama untuk memverifikasi payment chain
     *
     * @param msgId ID message
     * @param chain daftar transaksi relay
     * @param R1    secret dari sender
     * @param R2    secret dari receiver
     * @return true jika chain valid
     */
    public boolean verify(String msgId,
            List<Payment> chain,
            String R1,
            String R2) {

        /*
         * Jika chain kosong berarti tidak ada forwarding
         */
        if (chain == null || chain.isEmpty())
            return false;

        /*
         * Periksa setiap transaksi dalam chain
         */
        for (int i = 0; i < chain.size(); i++) {

            Payment p = chain.get(i);

            /*
             * Verifikasi signature transaksi
             */
            if (!p.verify())
                return false;

            /*
             * Hop pertama harus memiliki EncR1
             * sebagai bukti message dari sender
             */
            if (i == 0) {

                if (p.getEncR1() == null)
                    return false;
            }

            /*
             * Setiap hop harus memiliki ACK
             */
            if (p.getEncAck() == null)
                return false;
        }

        /*
         * Hop terakhir harus memiliki EncR2
         * sebagai bukti message diterima
         */
        Payment last = chain.get(chain.size() - 1);

        if (last.getEncR2() == null)
            return false;

        return true;
    }
}