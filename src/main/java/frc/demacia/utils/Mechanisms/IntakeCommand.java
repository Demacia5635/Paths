package frc.demacia.utils.mechanisms;

import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.motors.MotorInterface;

/**
 * A general-purpose command for intake mechanisms.
 * <p>
 * This command runs a selection of motors at a constant power until a 
 * specified stop condition is met (e.g., a sensor detecting an object).
 * </p>
 */
public class IntakeCommand extends Command {
  BaseMechanism mechanism;
  MotorInterface[] motors;
  int[] motorIndexes;
  double power;
  int length;
  BooleanSupplier stopCondition;

  /**
   * Creates an IntakeCommand for specific motors in the mechanism.
   * @param mechanism The subsystem.
   * @param motorIndexes The indexes of motors to be controlled.
   * @param power The power to apply to the motors.
   * @param stopCondition Condition to finish the intake process (e.g., proximity sensor).
   */
  public IntakeCommand(BaseMechanism mechanism, int[] motorIndexes, double power, BooleanSupplier stopCondition) {
    this.mechanism = mechanism;
    this.motorIndexes = motorIndexes;
    this.power = power;
    this.stopCondition = stopCondition;
    motors = mechanism.getMotors();
    length = Math.min(motors.length, motorIndexes.length);

    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(mechanism);
  }

  /**
   * Creates an IntakeCommand for all motors in the mechanism.
   * @param mechanism The subsystem.
   * @param power The power to apply to the motors.
   * @param stopCondition Condition to finish the intake process.
   */
  public IntakeCommand(BaseMechanism mechanism, double power, BooleanSupplier stopCondition) {
    this(mechanism, generateSequentialIndexes(mechanism.motorArray.length), power, stopCondition);
  }

  /**
   * Helper method used by the constructor to generate 0, 1, 2... indexes.
   * @param size Number of motors in the mechanism.
   * @return An array containing sequential integers.
   */
  private static int[] generateSequentialIndexes(int size) {
    int[] idxs = new int[size];
    for(int i=0; i<size; i++) idxs[i] = i;
    return idxs;
  }

  @Override
  public void initialize() {}

  /**
   * Applies the constant power to all specified intake motors.
   */
  @Override
  public void execute() {
    for (int i = 0; i < length; i++) {
      mechanism.setPower(motorIndexes[i], power);
    }
  }

  /**
   * Stops the specific motors that were running when the command ends or is interrupted.
   */
  @Override
  public void end(boolean interrupted) {
    for (int i = 0; i < length; i++) {
      mechanism.stop(motorIndexes[i]);
    }
  }

  /**
   * @return true when the stop condition is met.
   */
  @Override
  public boolean isFinished() {
    return stopCondition.getAsBoolean();
  }
}

