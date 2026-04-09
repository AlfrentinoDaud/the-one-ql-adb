package report;

import java.util.Collection;

import core.DTNHost;
import core.SimClock;
import core.SimScenario;
import core.Settings;
import core.World;

/**
 * Logs the position of every node at every simulation step.
 */
public class MovementLogger extends Report {

    @Override
    public void done() {
        super.done();
    }

    @Override
    public void init() {
        super.init();
        write("# Movement Logger Report");
        write("# Scenario: " + getScenarioName());
        write("# Time (s), Node ID, X, Y");
    }

    public void update() {
        double currentTime = SimClock.getTime();
        Collection<DTNHost> hosts = World.getInstance().getHosts();

        for (DTNHost host : hosts) {
            double x = host.getLocation().getX();
            double y = host.getLocation().getY();
            String line = String.format("%.1f,%s,%.4f,%.4f", currentTime,
                    host.getAddress(), x, y);
            write(line);
        }

        newEvent(); // handle file rotation if interval is set
    }
}
