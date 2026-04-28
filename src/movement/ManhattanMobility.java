package movement;

import core.Coord;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.DTNHost;

import movement.Path;

/**
 * Manhattan Movement Model
 * Node bergerak hanya horizontal atau vertikal seperti jalan grid kota.
 * 
 */
public class ManhattanMobility extends MovementModel {

    /* SETTINGS */
    public static final String GRID_SIZE = "gridSize";
    public static final String SAFE_DIST = "safeDistance";
    public static final String TL_DELAY = "trafficLightTime";

    /* PARAMETERS */
    private int grid;
    private double safeDist;
    private double tlDelay;

    /* STATE */
    private Coord last;
    private int dir;
    private double currentSpeed;

    /* DIRECTIONS */
    private static final int UP = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;

    /* CONSTRUCTOR */
    public ManhattanMobility(Settings s) {
        super(s);

        // this.grid = s.getInt(GRID_SIZE, 100);
        // this.safeDist = s.getDouble(SAFE_DIST, 20);
        // this.tlDelay = s.getDouble(TL_DELAY, 15);
        this.grid = s.contains(GRID_SIZE) ? s.getInt(GRID_SIZE) : 100;
        this.safeDist = s.contains(SAFE_DIST) ? s.getDouble(SAFE_DIST) : 20;
        this.tlDelay = s.contains(TL_DELAY) ? s.getDouble(TL_DELAY) : 15;

        this.dir = rng.nextInt(4);
        this.currentSpeed = generateSpeed();
    }

    /* COPY CONSTRUCTOR */
    public ManhattanMobility(ManhattanMobility m) {
        super(m);
        this.grid = m.grid;
        this.safeDist = m.safeDist;
        this.tlDelay = m.tlDelay;
        this.dir = m.dir;
        this.last = m.last;
        this.currentSpeed = m.currentSpeed;
    }

    /* RANDOM START POSITION */
    private Coord randomIntersection() {
        int gx = rng.nextInt(Math.max(1, getMaxX() / grid));
        int gy = rng.nextInt(Math.max(1, getMaxY() / grid));
        return new Coord(gx * grid, gy * grid);
    }

    @Override
    public Coord getInitialLocation() {
        last = randomIntersection();
        return last;
    }

    /**
     * Manhattan probabilistic direction choice
     */
    private void chooseDirection() {

        double r = rng.nextDouble();

        if (r < 0.5) {
            return; // straight
        } else if (r < 0.75) {
            dir = (dir + 3) % 4; // left
        } else {
            dir = (dir + 1) % 4; // right
        }

        // speed changes only when turning
        currentSpeed = generateSpeed();
    }

    /**
     * Compute next intersection safely
     */
    private Coord nextIntersection() {

        for (int attempt = 0; attempt < 4; attempt++) {

            double x = last.getX();
            double y = last.getY();

            switch (dir) {
                case UP:
                    y += grid;
                    break;
                case RIGHT:
                    x += grid;
                    break;
                case DOWN:
                    y -= grid;
                    break;
                case LEFT:
                    x -= grid;
                    break;
            }

            boolean outside = (x < 0 || x > getMaxX() ||
                    y < 0 || y > getMaxY());

            Coord candidate = new Coord(x, y);

            if (!outside && !isOccupied(candidate)) {
                return candidate;
            }

            // rotate direction clockwise and retry
            dir = (dir + 1) % 4;
        }

        // all directions blocked → stay
        return last;
    }

    /**
     * Collision avoidance
     */
    private boolean isOccupied(Coord p) {

        for (DTNHost h : SimScenario.getInstance().getHosts()) {

            if (h.getLocation() != null &&
                    h.getLocation().distance(p) < safeDist)
                return true;

            if (h.getLocation().distance(p) < safeDist)
                return true;
        }

        return false;
    }

    @Override
    public Path getPath() {

        if (last == null)
            last = getInitialLocation();

        chooseDirection();

        Coord next = nextIntersection();
        last = next;

        Path p = new Path(currentSpeed);
        p.addWaypoint(next);

        return p;
    }

    /**
     * Traffic light delay at intersection
     */
    @Override
    public double nextPathAvailable() {
        return SimClock.getTime()
                + generateWaitTime()
                + rng.nextDouble() * tlDelay;
    }

    @Override
    public MovementModel replicate() {
        return new ManhattanMobility(this);
    }
}
