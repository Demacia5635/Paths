// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Paths;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public class Arc extends Segment {
    Rotation2d angle;
    Translation2d centerCircle;
    Translation2d centerToStart;
    public Arc(PathPoint startingPointOnArc, PathPoint centerPoint, Rotation2d angle ){
        super(startingPointOnArc, centerPoint);
        this.angle = angle;
        this.centerCircle = centerPoint.getTranslation();
        this.centerToStart = startingPointOnArc.getTranslation().minus(centerPoint.getTranslation());
    }

    @Override
    public Translation2d calc(Pose2d robotPose, double velocity) {
        Translation2d centerToRobot = robotPose.getTranslation().minus(centerCircle);
        Translation2d robotPosOnArc = centerToRobot.div(centerToRobot.getNorm()).times(centerToStart.getNorm());
        Rotation2d wantedVelocityAngle = robotPosOnArc.getAngle().plus(Rotation2d.fromDegrees(90 * Math.signum(angle.getDegrees())));
        return new Translation2d(velocity, wantedVelocityAngle);
    }
}
