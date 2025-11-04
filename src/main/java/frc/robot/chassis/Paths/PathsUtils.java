// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public class PathsUtils {


    public static boolean isVelocityHeadingInRange(Rotation2d currentVelocityHeading, Rotation2d wantedVelocityHeading){
        return Math.abs(currentVelocityHeading.minus(wantedVelocityHeading).getRadians()) < PathsConstants.MAX_VELOCITY_HEADING_TO_FINISH_SEGMENT;
    }

    public static boolean isLineSegment(SegmentBase segment){
        return segment instanceof LineSegment;
    }
}
