// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Paths;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public class Arc extends Segment{
    private double kDefaultVelocity = 1;
    private Translation2d relativePosition;
    private Translation2d currentPosition;

    private Translation2d centerToRelative;
    private Translation2d centerToFinish;

    private Rotation2d arcAngle;
    private Rotation2d angleLeft;
    private double radius;
    private Translation2d pointC;


    public Arc(Translation2d pointA, Translation2d pointB, Rotation2d arcAngle, Translation2d pointC){

        //pointA represents the starting point of the arc, pointB represents the center circle and pointC represents the finishing point of the arc
        super(pointA, pointB);
        this.arcAngle = arcAngle;
        this.radius = pointA.getDistance(pointB);
        this.pointC = pointC;
        this.centerToRelative = relativePosition.minus(pointB);
        this.centerToFinish = pointC.minus(pointB);
        this.angleLeft = arcAngle;
    }

    
    
    @Override
    public double getLength() {
        return PathsUtils.getLengthOfArc(radius, arcAngle);
    }
    @Override
    public double getDistanceOnSegment() {
        return PathsUtils.getLengthOfArc(radius, arcAngle.minus(angleLeft));
    }

    @Override
    public double getDistanceLeftOnSegment() {
        return PathsUtils.getLengthOfArc(radius, angleLeft);
    }
    
    @Override
    public void updateCurrentPosition(Translation2d chassisPose) {
        currentPosition = chassisPose;
        relativePosition = PathsUtils.normalVector(currentPosition.minus(pointB), radius);
        centerToRelative = relativePosition.minus(pointB);
        angleLeft = relativePosition.getAngle().minus(centerToFinish.getAngle());
    }

    @Override
    public Translation2d calculate() {
        double realDistanceFromCenter = currentPosition.getDistance(pointB);
        Rotation2d wantedVelocityAngle = relativePosition.getAngle().plus(Rotation2d.fromDegrees(90 * Math.signum(arcAngle.getDegrees())));

        Rotation2d wantedAngleFixed = wantedVelocityAngle.times((realDistanceFromCenter / radius));
        //fix angle = turn angle, multiplied by a ratio.
        //bigger ratio - will turn more towards the center
        //smaller ratio - will turn less towards the center

        return new Translation2d(kDefaultVelocity, wantedAngleFixed);
        
    }



    @Override
    public boolean hasFinishedSegment() {
        return currentPosition.getDistance(pointC) < PathsConstraints.MAX_TRAJECTORY_DISTANCE_THRESHOLD
            || getDistanceLeftOnSegment() < PathsConstraints.MAX_TRAJECTORY_DISTANCE_THRESHOLD;
    }
}
