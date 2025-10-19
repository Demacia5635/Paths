// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Paths;

import java.nio.file.Path;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public abstract class Segment {
    PathPoint pointOnTrajectory;
    PathPoint helperPoint;
    
    public Segment(PathPoint pointOnTrajectory, PathPoint helperPoint){
        this.pointOnTrajectory = pointOnTrajectory;
        this.helperPoint = helperPoint;
    }

    public Translation2d calc(Pose2d robotPose, double velocity){return new Translation2d();}
}
