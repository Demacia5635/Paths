package frc.robot.chassisConstants;

import edu.wpi.first.math.geometry.Translation2d;
import frc.demacia.utils.chassis.ChassisConfig;
import frc.demacia.utils.chassis.SwerveModuleConfig;
import frc.demacia.utils.motors.TalonFXConfig;
import frc.demacia.utils.motors.BaseMotorConfig.Canbus;
import frc.demacia.utils.sensors.CancoderConfig;
import frc.demacia.utils.sensors.PigeonConfig;

public class MK5nChassisConstants {
    
  public static final String NAME = "MK5n chassis";
    public static final double CYCLE_DT = 0.02;
    public static final double MAX_DRIVE_VELOCITY = 3.6;
    public static final double MAX_ROTATIONAL_VELOCITY = Math.toRadians(360);
    public static final double MIN_DRIVE_VELOCITY_FOR_ROTATION = 0.2;


    public static class AccelConstants{
        public static final double MAX_LINEAR_ACCEL = 10;
        
        public static final double MAX_OMEGA_VELOCITY = Math.toRadians(540);
        public static final double MAX_RADIAL_ACCEL = 6;
        public static final double MAX_RADIUS = 0.4;
        public static final double MIN_OMEGA_DIFF = Math.toRadians(20);
        public static final double MAX_DELTA_VELOCITY = MAX_LINEAR_ACCEL * CYCLE_DT;
        public static final double MAX_VELOCITY_TO_IGNORE_RADIUS = MAX_RADIUS * MAX_OMEGA_VELOCITY;
        public static final double MIN_VELOCITY = 1.5;
    }
    
    public static final int GYRO_ID = 14;
    public static final Canbus CAN_BUS = Canbus.Rio;
    public static final Canbus GYRO_CAN_BUS = Canbus.Rio;
    public static final double STEER_GEAR_RATIO = 287.0/11.0;
    public static final double DRIVE_GEAR_RATIO = 7.03;
    
    public static final double STEER_KP = 4.1;
    public static final double STEER_KI = 0;
    public static final double STEER_KD = 0;
    public static final double STEER_KS = 0.2;
    public static final double STEER_KV = 0.42;
    public static final double STEER_KA = 0;

    public static final double DRIVE_KP = 24;
    public static final double DRIVE_KI = 0;
    public static final double DRIVE_KD = 0;
    public static final double DRIVE_KS = 0.6;
    public static final double DRIVE_KV = 2.2;
    public static final double DRIVE_KA = 0;

    public static final double MOTION_MAGIC_VEL = 15 * 2 * Math.PI;
    public static final double MOTION_MAGIC_ACCEL = 8 * 2 * Math.PI;
    public static final double MOTION_MAGIC_JERK = 160 * 2 * Math.PI;

    public static final double RAMP_TIME_STEER = 0.25;
    
    public static class SwerveModuleConfigs {
        public final TalonFXConfig STEER_CONFIG;
        public final TalonFXConfig DRIVE_CONFIG;
        public final CancoderConfig CANCODER_CONFIG;
        public final Translation2d POSITION;
        public final double STEER_OFFSET;
        public final String NAME;
        public SwerveModuleConfig SWERVE_MODULE_CONFIG;

        public SwerveModuleConfigs(TalonFXConfig steerConfig, TalonFXConfig driveConfig, CancoderConfig cancoderConfig, Translation2d position, double steerOffset, String name) {
            STEER_CONFIG = steerConfig;
            DRIVE_CONFIG = driveConfig;
            CANCODER_CONFIG = cancoderConfig;
            POSITION = position;
            STEER_OFFSET = steerOffset;
            NAME = name;
        }
        
        public SwerveModuleConfigs(int swerveId, double steerOffset, double wheelDiameter) {
            switch (swerveId) {
                case 0:
                    NAME = "Front Left";
                    break;
                case 1:
                    NAME = "Front Right";
                    break;
                case 2:
                    NAME = "Back Left";
                    break;
                case 3:
                    NAME = "Back Right";
                    break;
            
                default:
                    NAME = "";
                    break;
            }
            STEER_CONFIG = new TalonFXConfig(swerveId * 3 + 2, CAN_BUS, NAME + " Steer")
                .withPID(STEER_KP, STEER_KI, STEER_KD, STEER_KS, STEER_KV, STEER_KA, 0)
                .withMotionParam(MOTION_MAGIC_VEL, MOTION_MAGIC_ACCEL, MOTION_MAGIC_JERK)
                .withBrake(true)
                .withRadiansMotor(STEER_GEAR_RATIO)
                .withRampTime(RAMP_TIME_STEER);
            DRIVE_CONFIG = new TalonFXConfig(swerveId * 3 + 
            1, CAN_BUS, NAME + " Drive")
                .withPID(DRIVE_KP, DRIVE_KI, DRIVE_KD, DRIVE_KS, DRIVE_KV, DRIVE_KA, 0)
                .withBrake(true)
                .withInvert(true)
                .withMeterMotor(DRIVE_GEAR_RATIO, wheelDiameter * Math.PI);
            CANCODER_CONFIG = new CancoderConfig(swerveId * 3 + 3, Canbus.Rio, NAME + " Cancoder");
            POSITION = new Translation2d(
                swerveId == 0 || swerveId == 1 ? 0.34 : -0.34,
                swerveId == 0 || swerveId == 2 ? 0.29 : -0.29
            );
            STEER_OFFSET = steerOffset;
            SWERVE_MODULE_CONFIG = new SwerveModuleConfig(NAME, STEER_CONFIG, DRIVE_CONFIG, CANCODER_CONFIG)
            .withSteerOffset(steerOffset);
        }
    }

    public static final SwerveModuleConfigs FRONT_LEFT = new SwerveModuleConfigs(
        0,
        0.4484*2*Math.PI,
        0.1
    );

    public static final SwerveModuleConfigs FRONT_RIGHT = new SwerveModuleConfigs(
        1,
        0.527 * 2 * Math.PI,
        0.1
    );

    public static final SwerveModuleConfigs BACK_LEFT = new SwerveModuleConfigs(
        2,
        0.235*2*Math.PI,
        0.1
    );

    public static final SwerveModuleConfigs BACK_RIGHT = new SwerveModuleConfigs(
        3,
        -0.246*2*Math.PI,
        0.1
    );

    public static final PigeonConfig PIGEON_CONFIG = new PigeonConfig(GYRO_ID, GYRO_CAN_BUS, NAME + " pigeon");

    public static final ChassisConfig CHASSIS_CONFIG = new ChassisConfig(
    NAME,
    FRONT_LEFT.SWERVE_MODULE_CONFIG,
    FRONT_RIGHT.SWERVE_MODULE_CONFIG,
    BACK_LEFT.SWERVE_MODULE_CONFIG,
    BACK_RIGHT.SWERVE_MODULE_CONFIG,
    PIGEON_CONFIG,
    FRONT_LEFT.POSITION,
    FRONT_RIGHT.POSITION,
    BACK_LEFT.POSITION,
    BACK_RIGHT.POSITION);
}
