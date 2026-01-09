package frc.robot.testMechanism.intake.commands;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.testMechanism.intake.subsystems.IntakeSubsystem;
import frc.robot.testMechanism.intake.IntakeConstants.Config;
import frc.robot.testMechanism.intake.IntakeConstants.Hardware;

public class IntakeToggle extends InstantCommand {
  private static boolean isActive = false;
  private final IntakeSubsystem intake;

  public IntakeToggle(IntakeSubsystem intake) {
    this.intake = intake;
    addRequirements(intake);
  }

  @Override
  public void initialize() {
    if (isActive) {
      intake.stopAll();
      isActive = false;
    } else {
      intake.setPower(Hardware.MOTOR_NAME, Config.INTAKE_POWER);
      isActive = true;
    }
  }
}