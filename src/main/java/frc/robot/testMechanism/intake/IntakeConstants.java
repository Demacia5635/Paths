package frc.robot.testMechanism.intake;

import frc.demacia.utils.motors.BaseMotorConfig.Canbus;
import frc.demacia.utils.motors.TalonFXConfig;
import frc.demacia.utils.sensors.LimitSwitchConfig;

public class IntakeConstants {

    public static class Hardware {
        public static final int MOTOR_ID = 21;
        public static final String MOTOR_NAME = "Intake Motor";
        public static final Canbus MOTOR_CANBUS = Canbus.Rio;
        
        public static final int SENSOR_ID = 2;
        public static final String SENSOR_NAME = "Intake Note Sensor";
    }

    public static class Config {
        public static final double INTAKE_POWER = 0.7;
        public static final double OUTTAKE_POWER = -0.5;
        
        public static final boolean MOTOR_INVERTED = false;
        public static final double CURRENT_LIMIT = 30.0;

        public static final TalonFXConfig MOTOR_CONFIG = new TalonFXConfig(Hardware.MOTOR_ID, Hardware.MOTOR_CANBUS, Hardware.MOTOR_NAME)
            .withInvert(MOTOR_INVERTED)
            .withCurrent(CURRENT_LIMIT)
            .withBrake(true);

        public static final LimitSwitchConfig SENSOR_CONFIG = new LimitSwitchConfig(Hardware.SENSOR_ID, Hardware.SENSOR_NAME);
    }
}