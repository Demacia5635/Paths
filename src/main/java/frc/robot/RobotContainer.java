// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.chassis.Chassis;
import frc.demacia.utils.chassis.DriveCommand;
import frc.demacia.utils.controller.CommandController;
import frc.demacia.utils.controller.CommandController.ControllerType;
import frc.robot.chassisConstants.MK4iChassisConstants;
import frc.robot.chassis.Paths.FollowTrajectory;
import frc.robot.chassisConstants.MK5nChassisConstants;
import frc.robot.commands.PathCommand;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
  



public class RobotContainer {
  public static boolean isRed = true;
  public static RobotContainer robotContainer;

  private final Chassis chassis;
  private final frc.demacia.utils.chassis.DriveCommand driveCommand;
  public static CommandController CommandController;
  private static ArrayList<Pose2d> pointList;

  public RobotContainer() {
    CommandController = new CommandController(0, ControllerType.kPS5);
    chassis = new Chassis(MK4iChassisConstants.CHASSIS_CONFIG);
    driveCommand = new DriveCommand(chassis, CommandController);
    pointList = new ArrayList<>();
    pointList.add(new Pose2d(0, 0, Rotation2d.fromDegrees(0)));
    pointList.add(new Pose2d(0.5, 0, Rotation2d.fromDegrees(0)));
    pointList.add(new Pose2d(4.4, 3.55, Rotation2d.fromDegrees(90)));
    // pointList.add(new Pose2d(1.45, 1, new Rotation2d(Math.toRadians(60))));
    configureBindings();
  }


  private void configureBindings() {
    CommandController.leftButton().onTrue(new FollowTrajectory(pointList, (s) -> chassis.setVelocities(s), () -> chassis.getPose(), () -> chassis.getChassisSpeedsFieldRel()));
    chassis.setDefaultCommand(driveCommand);
  }

  public Command getAutonomousCommand() {
    return null;
  }
}