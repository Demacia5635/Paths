// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.testMechanism.arm.subsystems;

import frc.demacia.utils.log.LogEntryBuilder.LogLevel;
import frc.demacia.utils.log.LogManager;
import frc.demacia.utils.mechanisms.StateBaseMechanism;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.TalonFXMotor;
import frc.demacia.utils.sensors.DigitalEncoder;
import frc.demacia.utils.sensors.LimitSwitch;
import frc.demacia.utils.sensors.SensorInterface;
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
        super(TelescopeConstants.MOTOR_NAME, new MotorInterface[] {
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
        getMotor(AngleChangeConstants.MOTOR_NAME)
            .setEncoderPosition(getAngleSensor() - AngleChangeConstants.ENCODER_OFFSET_VALUE);

        putData();
    }

    /**
     * Registers subsystem data to the LogManager for dashboard monitoring.
     */
    @SuppressWarnings("unchecked")
    public void putData() {
        LogManager.addEntry(getName() + ": getAngleSensor, Length", 
            () -> getAngleSensor(), 
            () -> getMotorLength())
            .withLogLevel(LogLevel.LOG_AND_NT_NOT_IN_COMP).build();

        LogManager.addEntry(getName() + ": isAtBottom, LimitSensorTelescope, Is at Sensor Angle", 
            () -> isAtBottom(),
            () -> getLimitSensorTelescope(),
            () -> getLimitSensorAngle())
            .withLogLevel(LogLevel.LOG_AND_NT_NOT_IN_COMP).build();
    }

    /** @return The current position of the telescope in meters */
    public double getMotorLength() {
        return getMotor(TelescopeConstants.MOTOR_NAME).getCurrentPosition();
    }

    /** @param l The position to set the telescope encoder to */
    public void setMotorLength(double l) {
        getMotor(TelescopeConstants.MOTOR_NAME).setEncoderPosition(l);
    }

    /** @return The absolute angle sensor value in radians */
    public double getAngleSensor() {
        return ((DigitalEncoder) getSensor(AngleChangeConstants.ENCODER_NAME)).get();
    }

    /** @return true if the angle limit switch is triggered */
    public boolean getLimitSensorAngle() {
        return ((LimitSwitch) getSensor(AngleChangeConstants.LIMIT_SWITCH_NAME)).get();
    }

    /** @return true if the telescope is at its bottom (home) position */
    public boolean isAtBottom() {
        return getLimitSensorTelescope();
    }

    /** @param state The target state to set the mechanism to */
    public void setState(STATE_TELESCOPE state) {
        this.state = state;
    }

    /** @return true if the telescope limit switch is triggered */
    public boolean getLimitSensorTelescope() {
        return ((LimitSwitch) getSensor(TelescopeConstants.LIMIT_SWITCH_NAME)).get();
    }
}