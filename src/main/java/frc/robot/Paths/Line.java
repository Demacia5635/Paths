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
        Translation2d relativePos = robotPose.getTranslation().minus(helperPoint.getTranslation());

        Rotation2d diffAngle = pointOnTrajectory.getTranslation().minus(helperPoint.getTranslation()).getAngle().minus(relativePos.getAngle());

        return new Translation2d(velocity, relativePos.times(-1).getAngle().minus(diffAngle));    
    }
}
