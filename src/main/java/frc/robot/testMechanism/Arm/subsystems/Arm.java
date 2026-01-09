// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.testMechanism.arm.subsystems;

import frc.demacia.utils.mechanisms.StateBaseMechanism;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.TalonFXMotor;
import frc.demacia.utils.sensors.DigitalEncoder;
import frc.demacia.utils.sensors.LimitSwitch;
import frc.demacia.utils.sensors.SensorInterface;
import frc.robot.testMechanism.ArmConstants;
import frc.robot.testMechanism.arm.ArmConstants.AngleChangeConstants;
import frc.robot.testMechanism.arm.ArmConstants.STATE_TELESCOPE;
import frc.robot.testMechanism.arm.ArmConstants.TelescopeConstants;

/**
 * The Arm subsystem consisting of an angle pivot and a telescoping extension.
 * Utilizes StateBaseMechanism for state-based control.
 */
public class Arm extends StateBaseMechanism {

    /** * Creates a new Arm subsystem.
     * Initializes motors, sensors, and starting configurations.
     */
    public Arm() {
        super(ArmConstants.NAME, new MotorInterface[] {
            new TalonFXMotor(AngleChangeConstants.MOTOR_CONFIG), 
            new TalonFXMotor(TelescopeConstants.MOTOR_CONFIG)
        }, new SensorInterface[] {
            new LimitSwitch(TelescopeConstants.SENSOR_CONFIG),
            new LimitSwitch(AngleChangeConstants.SENSOR_CONFIG),
            new DigitalEncoder(AngleChangeConstants.ENCODER_CONFIG)
        }, STATE_TELESCOPE.class);

        // Initial setup
        setStartingOption(STATE_TELESCOPE.HOME);
        setPositionMechanism();

        // Initialize Encoder position based on the absolute sensor and constants offset
        getMotor(AngleChangeConstants.MOTOR_NAME).setEncoderPosition(getAngleSensor() - AngleChangeConstants.ENCODER_OFFSET_VALUE);
    }

    /** @param l The position to set the telescope encoder to */
    public void setMotorLength(double l) {
        getMotor(TelescopeConstants.MOTOR_NAME).setEncoderPosition(l);
    }

    /** @return The absolute angle sensor value in radians */
    public double getAngleSensor() {
        return ((DigitalEncoder) getSensor(AngleChangeConstants.ENCODER_NAME)).get();
    }

    /** @return true if the telescope is at its bottom (home) position */
    public boolean isAtBottom() {
        return ((LimitSwitch) getSensor(TelescopeConstants.LIMIT_SWITCH_NAME)).get();
    }
}