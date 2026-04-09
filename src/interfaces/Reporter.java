package interfaces;

/**
 * Interface untuk semua kelas pelaporan yang dijalankan selama simulasi.
 */
public interface Reporter {

    /**
     * Dipanggil setiap interval waktu pelaporan (misalnya setiap 5 detik simulasi).
     */
    void report();

    /**
     * Dipanggil setelah simulasi selesai untuk menutup file/log atau melakukan
     * cleanup.
     */
    void done();
}
