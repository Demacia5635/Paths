// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.testMechanism.arm;

import frc.demacia.utils.motors.BaseMotorConfig.Canbus;
import frc.demacia.utils.mechanisms.StateBaseMechanism.MechanismState;
import frc.demacia.utils.motors.TalonFXConfig;
import frc.demacia.utils.sensors.DigitalEncoderConfig;
import frc.demacia.utils.sensors.LimitSwitchConfig;

/**
 * Constants for the Arm subsystem.
 * Organized into Telescope and Angle Change configurations using variables only.
 */
public class ArmConstants {

    /**
     * Enum defining the possible states for the Arm.
     * [Angle in Radians, Telescope Length in Meters]
     */
    public enum STATE_TELESCOPE implements MechanismState {
        HOME(0, 0.05),
        LOW_CLOSED(0, 0.05),
        LOW_OPEN(0, 0.6),
        MID_CLOSED(Math.toRadians(30), 0.05),
        MID_OPEN(Math.toRadians(30), 0.6),
        HIGH_CLOSED(Math.toRadians(60), 0.05),
        HIGH_OPEN(Math.toRadians(60), 0.6);

        private final double[] values;

        STATE_TELESCOPE(double... values) {
            this.values = values;
        }

        @Override
        public double[] getValues() {
            return values;
        }
    }

    /** Constants for the telescoping extension mechanism. */
    public static class TelescopeConstants {
        // Hardware & ID
        public static final int MOTOR_ID = 40;
        public static final String MOTOR_NAME = "Telescope Motor";
        public static final Canbus MOTOR_CANBUS = Canbus.Rio;
        public static final int LIMIT_SWITCH_CHANNEL = 3;
        public static final String LIMIT_SWITCH_NAME = "Telescope Limit Sensor";
        public static final boolean LIMIT_SWITCH_INVERT = true;

        // Physical Parameters
        public static final double GEAR_RATIO = 58.24 / 0.2268; 
        public static final double DRUM_DIAMETER = 1.0;
        public static final double CURRENT_LIMIT = 40.0;
        public static final boolean MOTOR_BRAKE_MODE = true;

        // Motion Profile
        public static final double MAX_VELOCITY = 1.2;
        public static final double MAX_ACCELERATION = 6.0;
        public static final double MAX_JERK = 11.0;

        // PID & FeedForward Gains
        public static final double KP = 2.0;
        public static final double KI = 0.0;
        public static final double KD = 0.0;
        public static final double KS = 0.14;
        public static final double KV = 9.3;
        public static final double KA = 0.00028;
        public static final double KG = 0.0;

        // Configurations
        public static final LimitSwitchConfig SENSOR_CONFIG = new LimitSwitchConfig(LIMIT_SWITCH_CHANNEL, LIMIT_SWITCH_NAME)
                .withInvert(LIMIT_SWITCH_INVERT);

        public static final TalonFXConfig MOTOR_CONFIG = new TalonFXConfig(MOTOR_ID, MOTOR_CANBUS, MOTOR_NAME)
                .withBrake(MOTOR_BRAKE_MODE)
                .withMeterMotor(GEAR_RATIO, DRUM_DIAMETER)
                .withPID(KP, KI, KD, KS, KV, KA, KG)
                .withMotionParam(MAX_VELOCITY, MAX_ACCELERATION, MAX_JERK)
                .withCurrent(CURRENT_LIMIT);

        public static class CalibrationConstants {
            public static final double START_TIME_SEC = 0.5;
            public static final double START_POWER = 0.05;
            public static final double CALIBRATION_POWER = -0.1;
            public static final double RESET_POSITION = 0.0;
        }
    }

    /** Constants for the pivot/angle adjustment mechanism. */
    public static class AngleChangeConstants {
        // Hardware & ID
        public static final int MOTOR_ID = 11;
        public static final String MOTOR_NAME = "Angle Pivot Motor";
        public static final Canbus MOTOR_CANBUS = Canbus.Rio;
        public static final int ANALOG_ENCODER_CHANNEL = 0;
        public static final String ENCODER_NAME = "Angle Absolute Encoder";
        public static final int LIMIT_SWITCH_CHANNEL = 2;
        public static final String LIMIT_SWITCH_NAME = "Angle Limit Switch";

        // Physical Parameters
        public static final double GEAR_RATIO = (64.0 * 42.0) / 22.0;
        public static final double ENCODER_SCALAR = -1.0;
        public static final double ENCODER_OFFSET = 2 * Math.PI;
        public static final boolean MOTOR_INVERT = true;
        public static final boolean MOTOR_BRAKE_MODE = true;
        public static final double ENCODER_OFFSET_VALUE = -1.303466796875;

        // Motion Profile
        public static final double MAX_VELOCITY = 2.0;
        public static final double MAX_ACCEL = 6.0;
        public static final double MAX_JERK = 18.0;

        // PID & FeedForward Gains
        public static final double KP = 0.0;
        public static final double KI = 0.0;
        public static final double KD = 0.0;
        public static final double KS = 0.0329;
        public static final double KV = 2.47177;
        public static final double KA = 0.00002;
        public static final double KG_STATIC = 0.0;
        public static final double KSIN = -0.00290;

        // Configurations
        public static final LimitSwitchConfig SENSOR_CONFIG = new LimitSwitchConfig(LIMIT_SWITCH_CHANNEL, LIMIT_SWITCH_NAME)
                .withInvert(MOTOR_INVERT);

        public static final DigitalEncoderConfig ENCODER_CONFIG = new DigitalEncoderConfig(ANALOG_ENCODER_CHANNEL, ENCODER_NAME)
                .withScalar(ENCODER_SCALAR)
                .withOffset(ENCODER_OFFSET);

        public static final TalonFXConfig MOTOR_CONFIG = new TalonFXConfig(MOTOR_ID, MOTOR_CANBUS, MOTOR_NAME)
                .withMotionParam(MAX_VELOCITY, MAX_ACCEL, MAX_JERK)
                .withRadiansMotor(GEAR_RATIO)
                .withPID(KP, KI, KD, KS, KV, KA, KG_STATIC)
                .withFeedForward(KG_STATIC, KSIN)
                .withInvert(MOTOR_INVERT)
                .withBrake(MOTOR_BRAKE_MODE);
    }
}