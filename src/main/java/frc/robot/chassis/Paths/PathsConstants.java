// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

/** Add your docs here. */
public class PathsConstants {
    public static final double MAX_RADIAL_ACCEL = Math.toRadians(720);
    public static final double MAX_LINEAR_VELOCITY = 1;
    public static final double MAX_LINEAR_ACCEL = 2;
    public static final double MAX_JERK = 16;
    public static final double MAX_OMEGA_VELOCITY = Math.toRadians(360);
    public static final double MAX_OMEGA_ACCEL = Math.toRadians(720); 
public static final double MAX_ALLOWED_RADIUS = (MAX_LINEAR_VELOCITY * MAX_LINEAR_VELOCITY) / MAX_RADIAL_ACCEL; //1.27m
    
    public static final double MAX_POSITION_THRESHOLD_DURING_PATH = 0.2; //meters
    public static final double MAX_POSITION_THRESHOLD_FINAL_POINT = 0.1; //meters
    public static final double MAX_VELOCITY_HEADING_TO_FINISH_SEGMENT = Math.toRadians(7);

}
