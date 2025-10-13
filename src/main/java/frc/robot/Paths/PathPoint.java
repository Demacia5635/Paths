package frc.robot.Paths;

import edu.wpi.first.math.geometry.Pose2d;

public class PathPoint extends Pose2d{
    Pose2d point;
    double velocityAtPoint;
    
    public PathPoint(Pose2d point, double velocityAtPoint){
        this.point = point;
        this.velocityAtPoint = velocityAtPoint;
    }

    public double calculateRadius(){
        return velocityAtPoint * velocityAtPoint / PathsConstants.MAX_RADIAL_ACCEL;
    }

    
}
