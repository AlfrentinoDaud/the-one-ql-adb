package movement;

import core.World;
import interfaces.Reporter;
import core.DTNHost;

import java.io.*;
import java.util.List;

/**
 * MovementLogger mencatat posisi semua node setiap interval simulasi.
 */
public class MovementLogger1 implements Reporter {
    private BufferedWriter writer;
    private String filePath;
    private boolean isLoggingEnabled;

    public MovementLogger1(String[] args) {
        this.filePath = "movement_log.txt"; // default
        this.isLoggingEnabled = true;

        // Jika diberikan argumen, gunakan sebagai path log file
        if (args.length > 0) {
            this.filePath = args[0];
        }

        try {
            this.writer = new BufferedWriter(new FileWriter(filePath));
        } catch (IOException e) {
            System.err.println("Gagal membuka file log: " + filePath);
            isLoggingEnabled = false;
        }
    }

    @Override
    public void done() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Gagal menutup file log.");
        }
    }

    @Override
    public void report() {
        if (!isLoggingEnabled)
            return;

        try {
            List<DTNHost> hosts = World.getInstance().getHosts();
            double currentTime = core.SimClock.getTime();

            writer.write("Time: " + currentTime + "\n");
            for (DTNHost host : hosts) {
                core.Coord loc = host.getLocation();
                writer.write(String.format("Host %d: (%.2f, %.2f)\n", host.getAddress(), loc.getX(), loc.getY()));
            }
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Gagal menulis log pergerakan.");
        }
    }
}
