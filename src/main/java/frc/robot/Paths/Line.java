// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Paths;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public class Line extends Segment{

    public Line(PathPoint pointOnTrajectory, PathPoint helperPoint){
        super(pointOnTrajectory, helperPoint);
    }

    @Override
    public Translation2d calc(Pose2d robotPose, double velocity) {
        Translation2d line = helperPoint.getTranslation().minus(pointOnTrajectory.getTranslation());
        Rotation2d angle = line.getAngle();
        return new Translation2d(velocity, angle);
    }
}
