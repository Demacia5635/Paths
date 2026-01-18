package frc.demacia.utils.mechanisms;

import java.util.function.BooleanSupplier;

import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.MotorInterface.ControlMode;

/**
 * A specialized command for shooter-like mechanisms that use a LookUpTable to determine
 * target values based on an external input like distance.
 * <p>
 * Features:
 * <ul>
 * <li>Selective motor control via motorIndexes.</li>
 * <li>Automatic sequential mapping for all motors.</li>
 * <li>Pre-compiled control Runnables for high performance.</li>
 * </ul>
 * </p>
 */
public class ShooterCommand extends Command {
  StateBaseMechanism mechanism;
  MotorInterface[] motors;
  int[] motorIndexes;
  int length;
  Runnable[] Controls;
  BooleanSupplier stopCondition;

  /**
   * Creates a ShooterCommand for specific motors in the mechanism.
   * @param mechanism The subsystem.
   * @param motorIndexes The indexes of motors to be controlled.
   * @param controlModes The control mode for each motor.
   * @param stopCondition Finished condition.
   */
  public ShooterCommand(StateBaseMechanism mechanism, int[] motorIndexes, ControlMode[] controlModes, BooleanSupplier stopCondition) {
    this.mechanism = mechanism;
    this.motorIndexes = motorIndexes;
    this.stopCondition = stopCondition;
    motors = mechanism.getMotors();
    length = Math.min(Math.min(motors.length, controlModes.length), motorIndexes.length);
    Controls = new Runnable[length];
    for (int i = 0; i < length; i++) {
      switch (controlModes[i]) {
        case DUTYCYCLE:
          final int powerIndex = motorIndexes[i];
          Controls[i] = () -> mechanism.setPower(powerIndex, mechanism.getLookUpTableValue(powerIndex));
          break;
        case VOLTAGE:
          final int voltageIndex = motorIndexes[i];
          Controls[i] = () -> mechanism.setVoltage(voltageIndex, mechanism.getLookUpTableValue(voltageIndex));
          break;
        case VELOCITY:
          final int velocityIndex = motorIndexes[i];
          Controls[i] = () -> mechanism.setVelocity(velocityIndex, mechanism.getLookUpTableValue(velocityIndex));
          break;
        case POSITION_VOLTAGE:
          final int positionVoltageIndex = motorIndexes[i];
          Controls[i] = () -> mechanism.setPositionVoltage(positionVoltageIndex, mechanism.getLookUpTableValue(positionVoltageIndex));
          break;
        case MOTION:
          final int motionIndex = motorIndexes[i];
          Controls[i] = () -> mechanism.setMotion(motionIndex, mechanism.getLookUpTableValue(motionIndex));
          break;
        case ANGLE:
          final int angleIndex = motorIndexes[i];
          Controls[i] = () -> mechanism.setAngle(angleIndex, mechanism.getLookUpTableValue(angleIndex));
          break;
        default:
          Controls[i] = () -> {};
          break;
      }
  }
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(mechanism);
  }

  /**
   * Creates a ShooterCommand that controls ALL motors in the mechanism in sequence.
   */
  public ShooterCommand(StateBaseMechanism mechanism, ControlMode[] controlModes, BooleanSupplier stopCondition) {
    this(mechanism, generateSequentialIndexes(mechanism.motorArray.length), controlModes, stopCondition);
  }

  /**
     * Helper method used by the constructor to generate 0, 1, 2... indexes.
     * This ensures the arrays are populated before the command starts.
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

  @Override
  public void execute() {
    for (int i = 0; i < length; i++) {
      Controls[i].run();
    }
  }

  /**
     * Stops only the motors that were controlled by this command.
     * Uses the motorIndexes array to target the correct hardware.
     */
  @Override
  public void end(boolean interrupted) {
    for (int i = 0; i < length; i++) {
      mechanism.stop(motorIndexes[i]);
    }
  }

  @Override
  public boolean isFinished() {
    return stopCondition.getAsBoolean();
  }
}

