package Blockchain;

import java.util.*;
import core.DTNHost;

/**
 * Class DestinationRegistry digunakan untuk menyimpan
 * informasi tujuan akhir dari setiap message dalam jaringan DTN.
 * 
 * Hal ini diperlukan karena:
 * - node relay tidak selalu tahu tujuan akhir message
 * - sistem reward harus tahu kapan message sampai ke receiver
 */
public class DestinationRegistry {

    /**
     * Map yang menyimpan pasangan:
     * messageID -> DTNHost tujuan
     */
    private Map<String, DTNHost> registry = new HashMap<>();

    /**
     * Mendaftarkan tujuan message.
     * Biasanya dipanggil ketika sender pertama kali membuat message.
     *
     * @param msgId       ID message
     * @param destination node tujuan
     */
    public void register(String msgId,
            DTNHost destination) {

        registry.put(msgId, destination);
    }

    /**
     * Mengambil node tujuan dari sebuah message.
     *
     * @param msgId ID message
     * @return DTNHost tujuan
     */
    public DTNHost get(String msgId) {

        return registry.get(msgId);
    }
}