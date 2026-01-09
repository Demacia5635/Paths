package frc.robot.testMechanism.arm.commands;

import frc.demacia.utils.mechanisms.DefaultCommand;
import frc.demacia.utils.motors.MotorInterface.ControlMode;
import frc.robot.testMechanism.arm.subsystems.Arm;

/**
 * Default command for the Arm subsystem.
 * Applies Motion Magic / Profiled control to both the Pivot and Telescope.
 */
public class ArmCommand extends DefaultCommand {
    /**
     * Creates a new ArmCommand.
     * @param arm The Arm subsystem to control.
     */
    public ArmCommand(Arm arm) {
        super(arm, new ControlMode[] {ControlMode.MOTION, ControlMode.MOTION});
    }
}