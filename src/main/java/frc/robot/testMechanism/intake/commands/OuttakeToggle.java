package frc.robot.testMechanism.intake.commands;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.testMechanism.intake.subsystems.IntakeSubsystem;
import frc.robot.testMechanism.intake.IntakeConstants.Config;

public class OuttakeToggle extends InstantCommand {
  private static boolean isActive = false;
  private final IntakeSubsystem intake;

  public OuttakeToggle(IntakeSubsystem intake) {
    this.intake = intake;
    addRequirements(intake);
  }

  @Override
  public void initialize() {
    if (isActive) {
      intake.stopAll();
      isActive = false;
    } else {
      intake.setPower(0, Config.OUTTAKE_POWER);
      isActive = true;
    }
  }
}