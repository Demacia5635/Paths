// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.demacia.utils.chassis.Chassis;
import frc.demacia.utils.controller.CommandController;
import frc.demacia.utils.controller.CommandController.ControllerType;
import frc.demacia.utils.mechanisms.DefaultCommand;
import frc.demacia.utils.mechanisms.DriveCommand;
import frc.demacia.utils.motors.MotorInterface.ControlMode;
import frc.robot.testMechanism.Arm.Arm;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {

  Chassis chassis;
  // DriveCommand driveCommand;

  public static CommandController driverController;

  Arm arm;

  // The robot's subsystems and commands are defined here...

  // Replace with CommandPS4Controller or CommandJoystick if needed

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    driverController = new CommandController(0, ControllerType.kXbox);

    // chassis = new Chassis(Robot1ChassisConstants.CHASSIS_CONFIG);
    // driveCommand = new DriveCommand(chassis, driverController);

    arm = new Arm();

    // Configure the trigger bindings
    configureBindings();
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be
   * created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with
   * an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for
   * {@link
   * CommandXboxController
   * Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or
   * {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    arm.setDefaultCommand(
        new DefaultCommand(arm, new ControlMode[] { ControlMode.MOTION, ControlMode.MOTION }));
    driverController.getLeftStickMove().onTrue(
        new DriveCommand(arm, "telescopMotor", () -> driverController.getLeftY() * -0.3));
    driverController.getRightStickMove().onTrue(
      new DriveCommand(arm, "Change Angle Motor", () -> driverController.getRightY() * 0.3)
    );
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return null;
  }
}
