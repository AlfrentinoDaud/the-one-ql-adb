package movement;

import core.Coord;
import core.DTNHost;
import core.Settings;
import movement.MovementModel;
import movement.Path;
import movement.SwitchableMovement;
import java.util.Random;

public class LevyWalkMovement extends MovementModel implements SwitchableMovement {
    private static final String ALPHA_SETTING = "alpha";
    private static final String AREA_WIDTH_SETTING = "width";
    private static final String AREA_HEIGHT_SETTING = "height";

    private double alpha;
    private double width;
    private double height;
    private Random rng;
    private Coord lastWaypoint;

    public LevyWalkMovement(Settings settings) {
        super(settings);
        this.alpha = settings.getDouble(ALPHA_SETTING);
        this.width = settings.getDouble(AREA_WIDTH_SETTING);
        this.height = settings.getDouble(AREA_HEIGHT_SETTING);
        this.rng = new Random();
    }

    @Override
    public Path getPath() {
        Path p = new Path();
        double stepLength = getLevyStepLength();
        double angle = rng.nextDouble() * 2 * Math.PI;

        double newX = lastWaypoint.getX() + stepLength * Math.cos(angle);
        double newY = lastWaypoint.getY() + stepLength * Math.sin(angle);

        // Handle boundary conditions
        newX = Math.min(width, Math.max(0, newX));
        newY = Math.min(height, Math.max(0, newY));

        Coord nextWaypoint = new Coord(newX, newY);
        p.addWaypoint(nextWaypoint);
        this.lastWaypoint = nextWaypoint;

        return p;
    }

    private double getLevyStepLength() {
        // Generate step length from power-law distribution
        return Math.pow(1 - rng.nextDouble(), -1.0 / alpha);
    }

    @Override
    public Coord getInitialLocation() {
        double x = rng.nextDouble() * width;
        double y = rng.nextDouble() * height;
        this.lastWaypoint = new Coord(x, y);
        return this.lastWaypoint;
    }

    @Override
    public MovementModel replicate() {
        return new LevyWalkMovement(new Settings());
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setLocation(Coord lastWaypoint) {
        // Menyimpan posisi terakhir node
        this.lastWaypoint = lastWaypoint;
    }

    @Override
    public Coord getLastLocation() {
        // Mengembalikan posisi terakhir node
        return this.lastWaypoint;
    }

    private double nextPareto(double alpha) {
        // uniform variable to inverse cumulative distribution function -> [0,1]
        double uniformRandom = rng.nextDouble();
        return Math.pow(1.0 - uniformRandom, -1.0 / alpha);
    }
}