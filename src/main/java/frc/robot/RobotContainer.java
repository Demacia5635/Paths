// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.chassis.Chassis;
import frc.demacia.utils.chassis.DriveCommand;
import frc.demacia.utils.Controller.CommandController;
import frc.robot.ChassisConstants.MK5nChassisConstants;

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

  public RobotContainer() {
    CommandController = new CommandController(0, null);
    chassis = new Chassis(MK5nChassisConstants.CHASSIS_CONFIG);
    chassis.setDefaultCommand(new DriveCommand(chassis, CommandController));
    driveCommand = new DriveCommand(chassis, CommandController);
    
    configureBindings();
  }

  private void configureBindings() {
    chassis.setDefaultCommand(driveCommand);
  }

  public Command getAutonomousCommand() {
    return null;
  }
}