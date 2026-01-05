// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.testMechanism.Arm;

import frc.demacia.utils.log.LogEntryBuilder.LogLevel;
import frc.demacia.utils.log.LogManager;
import frc.demacia.utils.mechanisms.StateBaseMechanism;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.TalonFXMotor;
import frc.demacia.utils.sensors.DigitalEncoder;
import frc.demacia.utils.sensors.LimitSwitch;
import frc.demacia.utils.sensors.SensorInterface;
import frc.robot.testMechanism.Arm.ArmConstants.AngleChangeConstants;
import frc.robot.testMechanism.Arm.ArmConstants.STATE_TELESCOPE;
import frc.robot.testMechanism.Arm.ArmConstants.TelescopConstants;

/** Add your docs here. */
public class Arm extends StateBaseMechanism {
    final double offset = -1.303466796875;

    /** Creates a new telescop. */
    public Arm() {
        super("telescope", new MotorInterface[] {
            new TalonFXMotor(AngleChangeConstants.CHANGE_ANGLE_CONFIG), 
            new TalonFXMotor(TelescopConstants.MOTOR_CONFIG)
        }, new SensorInterface[] {
            new LimitSwitch(TelescopConstants.SENSOR_CONFIG),
            new LimitSwitch(AngleChangeConstants.SENSOR_CONFIG_ANGLE),
            new DigitalEncoder(AngleChangeConstants.CHANGE_ANGLE_ANALOG_CONFIG)
        }, STATE_TELESCOPE.class);
        setStartingOption(STATE_TELESCOPE.HOME);
        setPositionMechanism();
        getMotor("Change Angle Motor").setEncoderPosition(getAngleSensor() - offset);
    }

    @SuppressWarnings("unchecked")
    public void putData() {
        LogManager.addEntry("telescope: getAngleSensor, Length", 
            () -> getAngleSensor(), 
            () -> getMotorLength())
            .withLogLevel(LogLevel.LOG_AND_NT_NOT_IN_COMP).build();

        LogManager.addEntry("telescope: isAtBottom, LimitSensorTelescop, Is at Sensor Change Angle,", 
            () -> isAtBottom(),
            () -> getLimitSensorTelescop(),
            () -> getLimitSensorAngle())
            .withLogLevel(LogLevel.LOG_AND_NT_NOT_IN_COMP).build();
    }

    public double getMotorLength() {
        return getMotor("telescopMotor").getCurrentPosition();
    }

    public void setMotorLength(double l) {
        getMotor("telescopMotor").setEncoderPosition(l);
    }

    public double getAngleSensor() {
        return ((DigitalEncoder) getSensor("Change Angle Encoder")).get();
    }

    public boolean getLimitSensorAngle() {
        return ((LimitSwitch) getSensor("Angle change limit switch")).get();
    }

    public boolean isAtBottom() {
        return getLimitSensorTelescop();
    }

    public void setState(STATE_TELESCOPE state) {
        this.state = state;
    }

    public boolean getLimitSensorTelescop() {
        return ((LimitSwitch) getSensor("Telescop Sensor")).get();
    }
}