package frc.demacia.utils.mechanisms;

import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.MotorInterface.ControlMode;

public class DefaultCommand extends Command {
  StateBaseMechanism mechanism;
  MotorInterface[] motors;
  int length;
  ControlMode[] controlModes;

  /** Creates a new DriveCommand. */
  public DefaultCommand(StateBaseMechanism mechanism, ControlMode[] controlModes) {
    this.mechanism = mechanism;
    this.controlModes = controlModes;
    motors = mechanism.getMotors();
    length = motors.length;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(mechanism);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    for (int i = 0; i < length; i++) {
        switch (controlModes[i]) {
            case DUTYCYCLE:
                motors[i].setDuty(mechanism.getValue(i));
                break;
            case VOLTAGE:
                motors[i].setVoltage(mechanism.getValue(i));
                break;
            case VELOCITY:
                motors[i].setPositionVoltage(mechanism.getValue(i));
                break;
            case POSITION_VOLTAGE:
                motors[i].setPositionVoltage(mechanism.getValue(i));
                break;
            case MOTION:
                motors[i].setMotion(mechanism.getValue(i));
                break;
            case ANGLE:
                motors[i].setAngle(mechanism.getValue(i));
                break;
            default:
                break;
        }
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
