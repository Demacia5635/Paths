package frc.demacia.utils.Motors;

public class Utilities {

    public static double clamp(double value, double min, double max) {
        return value < min ? min : value > max ? max : value;
    }
    public static double deadband(double value, double deadband) {
        return Math.abs(value) < deadband ? 0 : value;
    }
    public static double signumWithDeadband(double value, double deadband) {
        return value > deadband ? 1 : value < -deadband ? -1 : 0;
    }
    
}
