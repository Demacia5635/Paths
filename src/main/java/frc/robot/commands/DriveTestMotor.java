// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.TestMotor;
import frc.utils.CommandController;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class DriveTestMotor extends Command {

/** the controller */
private final CommandController controller;
/** the arm subsystem */
private final TestMotor testMotor;

/** the arm angle motor power later changed by the controller */
private double power;

  /** Creates a new DriveTestMotor. */
  public DriveTestMotor(TestMotor testMotor, CommandController controller) {
    // Use addRequirements() here to declare subsystem dependencies.
    this.testMotor = testMotor;
    this.controller = controller;
    addRequirements(testMotor);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    power = controller.getLeftY() * -0.8;
    testMotor.setPower(power);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    testMotor.stop();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
