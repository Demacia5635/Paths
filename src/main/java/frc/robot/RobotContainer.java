// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.chassis.Chassis;
import frc.demacia.utils.chassis.DriveCommand;
import frc.demacia.utils.Controller.CommandController;
import frc.demacia.utils.Controller.CommandController.ControllerType;
import frc.robot.ChassisConstants.MK5nChassisConstants;
import frc.robot.chassis.kinematics.DemaciaKinematics;
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
  public static DemaciaKinematics kinematics;
  Pose2d[] pathPoint;

  public RobotContainer() {
    kinematics = new DemaciaKinematics(MK5nChassisConstants.MODOLES_POSE);
    CommandController = new CommandController(0, ControllerType.kPS5);
    chassis = new Chassis(MK5nChassisConstants.CHASSIS_CONFIG);
    driveCommand = new DriveCommand(chassis, CommandController);
    pathPoint = new Pose2d[]{
    new Pose2d(chassis.getPose().getX(), chassis.getPose().getY(), chassis.getPose().getRotation()),
    new Pose2d(3, 2, new Rotation2d(68)),
    new Pose2d(4.4, 3.55, new Rotation2d(50)),
    new Pose2d(1.45, 1, new Rotation2d(49))
  };
    configureBindings();
  }


  private void configureBindings() {
    CommandController.leftButton().onTrue(new PathCommand(kinematics, chassis, pathPoint));
    chassis.setDefaultCommand(driveCommand);
  }

  public Command getAutonomousCommand() {
    return null;
  }
}