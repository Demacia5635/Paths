// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.demacia.utils.chassis.Chassis;
import frc.demacia.utils.controller.CommandController;
import frc.demacia.utils.controller.CommandController.ControllerType;
import frc.demacia.utils.mechanisms.DriveCommand;
import frc.robot.chassisConstants.Robot1ChassisConstants;
import frc.robot.testMechanism.arm.commands.ArmCalibration;
import frc.robot.testMechanism.arm.commands.ArmCommand;
import frc.robot.testMechanism.arm.subsystems.Arm;
import frc.robot.testMechanism.arm.ArmConstants.TelescopeConstants;
import frc.robot.testMechanism.arm.ArmConstants.AngleChangeConstants;
import frc.robot.testMechanism.shooter.subsystems.Shooter;
import frc.robot.testMechanism.shooter.commands.ShooterAutoFire;
import frc.robot.testMechanism.shooter.commands.ShooterStateFire;
import frc.robot.testMechanism.shooter.commands.AngleCalibration;
import frc.robot.testMechanism.intake.subsystems.IntakeSubsystem;
import frc.robot.testMechanism.intake.commands.IntakeToggle;
import frc.robot.testMechanism.intake.commands.OuttakeToggle;
import edu.wpi.first.wpilibj2.command.Command;

public class RobotContainer {

  private final Chassis chassis;
  private final frc.demacia.utils.chassis.DriveCommand driveCommand;

  public static CommandController driverController;

  private final Arm arm;
  private final Shooter shooter;
  private final IntakeSubsystem intake;

  public RobotContainer() {
    driverController = new CommandController(0, ControllerType.kXbox);

    chassis = new Chassis(Robot1ChassisConstants.CHASSIS_CONFIG);
    driveCommand = new frc.demacia.utils.chassis.DriveCommand(chassis, driverController);

    arm = new Arm();
    shooter = new Shooter();
    intake = new IntakeSubsystem();

    configureBindings();
  }

  private void configureBindings() {
    chassis.setDefaultCommand(driveCommand);
    arm.setDefaultCommand(new ArmCommand(arm));
    shooter.setDefaultCommand(new ShooterStateFire(shooter));
    
    driverController.upButton().onTrue(new ArmCalibration(arm));
    driverController.downButton().onTrue(new AngleCalibration(shooter));

    driverController.getLeftStickMove().whileTrue(
      new DriveCommand(arm, TelescopeConstants.MOTOR_NAME, () -> driverController.getLeftY() * -0.3)
    );
    
    driverController.getRightStickMove().whileTrue(
      new DriveCommand(arm, AngleChangeConstants.MOTOR_NAME, () -> driverController.getRightY() * 0.3)
    );

    driverController.povDown().whileTrue(new ShooterAutoFire(shooter));

    driverController.rightButton().whileTrue(new IntakeToggle(intake));
    
    driverController.leftBumper().whileTrue(new OuttakeToggle(intake));
  }

  public Command getAutonomousCommand() {
    return null;
  }
}