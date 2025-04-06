// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Paths;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public class Leg extends Segment {
    private double kDefaultVelocity = 1;
    private Translation2d relativePosition;
    private Translation2d currentPosition;
    private final Translation2d legVector;

    public Leg(Translation2d pointA, Translation2d pointB) {
        super(pointA, pointB);
        this.legVector = pointB.minus(pointA);
    }

    @Override
    public double getLength() {
        return pointB.minus(pointA).getNorm();
    }

    @Override
    public double getDistanceFromLastPoint() {
        return relativePosition.getDistance(pointB);
    }

    @Override
    public double getDistanceOnSegment() {
        return pointA.getDistance(relativePosition);
    }

    @Override
    public double getDistanceLeftOnSegment() {
        return pointB.minus(relativePosition).getNorm();
    }

    @Override
    public Translation2d calculate() {
        Translation2d finishToCurrentPosition = pointB.minus(currentPosition);
        double fixAngle = Math.min(PathsConstraints.MAX_ANGLE_FIX, MathUtil.applyDeadband(
            PathsUtils.rotateVector180deg(legVector).getAngle().minus(finishToCurrentPosition.getAngle()).getRadians(), PathsConstraints.MIN_ANGLE_FIX)); //if chassis is on the leg the fix will be zero

        Rotation2d wantedAngle = PathsUtils.rotateVector180deg(finishToCurrentPosition).getAngle().minus(new Rotation2d(fixAngle));
        return new Translation2d(kDefaultVelocity, wantedAngle); //need to normal the vector when finished
    }

    public void updateCurrentPosition(Translation2d chassisPose) {
        this.currentPosition = chassisPose;
        this.relativePosition = currentPosition
                .plus(new Translation2d(PathsUtils.distanceOfPointFromLine(pointA, pointB, currentPosition),
                        legVector.getAngle().plus(Rotation2d.kCW_90deg)));
    }

}
