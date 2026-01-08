package frc.robot.testMechanism.shooter;

import frc.demacia.utils.mechanisms.StateBaseMechanism.MechanismState;
import frc.demacia.utils.motors.BaseMotorConfig.Canbus;
import frc.demacia.utils.motors.TalonFXConfig;
import frc.demacia.utils.sensors.LimitSwitchConfig;

public class ShooterConstants {

    /** States for the shooter mechanism: [Angle, VelUp, VelDown] */
    public enum SHOOTER_STATE implements MechanismState {
        IDLE(0, 0, 0),
        AMP(48.5, 4.5, 19),
        PODIUM(36, 16.5, 16.5),
        SUBWOOFER(54, 14, 14);

        private final double[] values;
        SHOOTER_STATE(double... values) { this.values = values; }
        @Override public double[] getValues() { return values; }
    }

    public static class Hardware {
        public static final int MOTOR_ANGLE_ID = 33;
        public static final int MOTOR_UP_ID = 31;
        public static final int MOTOR_DOWN_ID = 32;
        public static final int MOTOR_FEEDING_ID = 34;
        public static final int LIMIT_SWITCH_ID = 1;
        public static final int ANALOG_INPUT_ID = 1;
        
        public static final String ANGLE_MOTOR_NAME = "Angle Motor";
        public static final String SHOOTER_UP_NAME = "Shooter Up";
        public static final String SHOOTER_DOWN_NAME = "Shooter Down";
        public static final Canbus SHOOTER_CANBUS = Canbus.Rio;
        public static final String LIMIT_SWITCH_NAME = "Angle Limit";
    }

    public static class Config {
        // Shared Shooter Parameters
        public static final double SHOOTER_KP = 0.02;
        public static final double SHOOTER_KI = 0.0;
        public static final double SHOOTER_KD = 0.0;
        public static final double SHOOTER_KS = 0.15;
        public static final double SHOOTER_KV = 0.016;
        public static final double SHOOTER_KA = 0.0;
        public static final double SHOOTER_KG = 0.0;
        public static final double SHOOTER_RAMP_TIME = 0.5;
        public static final boolean SHOOTER_UP_INVERT = true;
        public static final boolean SHOOTER_DOWN_INVERT = false;

        // Angle Motor Parameters
        public static final double ANGLE_KP = 0.3;
        public static final double ANGLE_KI = 0.0;
        public static final double ANGLE_KD = 0.003;
        public static final double ANGLE_KS = 0.07;
        public static final double ANGLE_KV = 0.06;
        public static final double ANGLE_KA = 0.0;
        public static final double ANGLE_KG = 0.0;
        public static final double ANGLE_VEL = 20000.0;
        public static final double ANGLE_ACCEL = 20000.0;
        public static final double ANGLE_JERK = 0.0;

        // Config Objects using only variables
        public static final TalonFXConfig SHOOTER_UP_CONFIG = new TalonFXConfig(Hardware.MOTOR_UP_ID, Hardware.SHOOTER_CANBUS, Hardware.SHOOTER_UP_NAME)
            .withPID(SHOOTER_KP, SHOOTER_KI, SHOOTER_KD, SHOOTER_KS, SHOOTER_KV, SHOOTER_KA, SHOOTER_KG)
            .withRampTime(SHOOTER_RAMP_TIME)
            .withInvert(SHOOTER_UP_INVERT);

        public static final TalonFXConfig SHOOTER_DOWN_CONFIG = new TalonFXConfig(Hardware.MOTOR_DOWN_ID, Hardware.SHOOTER_CANBUS, Hardware.SHOOTER_DOWN_NAME)
            .withPID(SHOOTER_KP, SHOOTER_KI, SHOOTER_KD, SHOOTER_KS, SHOOTER_KV, SHOOTER_KA, SHOOTER_KG)
            .withRampTime(SHOOTER_RAMP_TIME)
            .withInvert(SHOOTER_DOWN_INVERT);

        public static final TalonFXConfig ANGLE_CONFIG = new TalonFXConfig(Hardware.MOTOR_ANGLE_ID, Hardware.SHOOTER_CANBUS, Hardware.ANGLE_MOTOR_NAME)
            .withPID(ANGLE_KP, ANGLE_KI, ANGLE_KD, ANGLE_KS, ANGLE_KV, ANGLE_KA, ANGLE_KG)
            .withMotionParam(ANGLE_VEL, ANGLE_ACCEL, ANGLE_JERK);

        public static final LimitSwitchConfig LIMIT_CONFIG = new LimitSwitchConfig(Hardware.LIMIT_SWITCH_ID, Hardware.LIMIT_SWITCH_NAME);

        public static class CalibrationConstants {
            public static final double START_TIME_SEC = 0.5;
            public static final double START_POWER = 0.1;
            public static final double CALIBRATION_POWER = -0.3;
            public static final double RESET_POSITION = 0.0;
        }
    }
}