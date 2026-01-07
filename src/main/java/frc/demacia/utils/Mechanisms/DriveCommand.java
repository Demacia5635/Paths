package frc.demacia.utils.mechanisms;

import java.util.function.Supplier;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * A simple command to drive a specific motor within a mechanism using a dynamic power source.
 * <p>
 * This is typically used for teleoperated control where the power comes from a joystick axis.
 * </p>
 */
public class DriveCommand extends Command {
  BaseMechanism mechanism;
  String motorName;
  Supplier<Double> power;

  /** * Creates a new DriveCommand.
   * * @param mechanism The mechanism containing the motor
   * @param motorName The name of the motor to drive
   * @param power A supplier that provides the duty cycle power [-1.0, 1.0] (e.g., joystick input)
   */
  public DriveCommand(BaseMechanism mechanism, String motorName, Supplier<Double> power) {
    this.mechanism = mechanism;
    this.motorName = motorName;
    this.power = power;
    addRequirements(mechanism);
  }

  @Override
  public void initialize() {}

  /**
   * Continuously updates the motor power from the supplier.
   */
  @Override
  public void execute() {
    mechanism.setPower(motorName, power.get());
  }

  @Override
  public void end(boolean interrupted) {}

  @Override
  public boolean isFinished() {
    return false;
  }
}