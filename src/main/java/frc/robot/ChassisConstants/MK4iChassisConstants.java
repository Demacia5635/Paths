package frc.robot.ChassisConstants;
import edu.wpi.first.math.geometry.Translation2d;
import frc.demacia.utils.chassis.ChassisConfig;
import frc.demacia.utils.chassis.SwerveModuleConfig;
import frc.demacia.utils.motors.BaseMotorConfig.Canbus;
import frc.demacia.utils.motors.TalonFXConfig;
import frc.demacia.utils.sensors.CancoderConfig;
import frc.demacia.utils.sensors.PigeonConfig;

public class MK4iChassisConstants {

    public static final String NAME = "MK4i Chassis";

    public static final int PIGEON_ID = 14;
    public static final Canbus CAN_BUS = Canbus.Rio;
    public static final Canbus PIGEON_CAN_BUS = Canbus.Rio;
    public static final double STEER_GEAR_RATIO = 150 / 7d;
    public static final double DRIVE_GEAR_RATIO = 8.14;
    public static final double WHEEL_DIAMETER = 4 * 0.0254;

    public static final double STEER_KP = 6.43;
    public static final double STEER_KI = 0;
    public static final double STEER_KD = 0;
    public static final double STEER_KS = 0;
    public static final double STEER_KV = 0;
    public static final double STEER_KA = 0;

    public static final double DRIVE_KP = 19;
    public static final double DRIVE_KI = 0;
    public static final double DRIVE_KD = 0;
    public static final double DRIVE_KS = 0.15839;
    public static final double DRIVE_KV = 2.25994;
    public static final double DRIVE_KA = 0.08058;

    public static final double MOTION_MAGIC_VEL = 15 * 2 * Math.PI;
    public static final double MOTION_MAGIC_ACCEL = 8 * 2 * Math.PI;
    public static final double MOTION_MAGIC_JERK = 160 * 2 * Math.PI;

    public static final double RAMP_TIME_STEER = 0.25;

    public static final SwerveModuleConfig[] swerveModules(double[] offsets) {
        SwerveModuleConfig[] ans = new SwerveModuleConfig[4];
        for (int i = 0; i < 4; i++) {
            String name = "Error";
            switch (i) {
                case 0:
                    name = "Front Left";
                    break;
                case 1:
                    name = "Front Right";
                    break;
                case 2:
                    name = "Back Left";
                    break;
                case 3:
                    name = "Back Right";
                    break;

                default:
                    name = "";
                    break;
            }

            ans[i] = new SwerveModuleConfig(
                    name,
                    new TalonFXConfig(i * 3 + 2, CAN_BUS, name + "/Steer")
                            .withPID(STEER_KP, STEER_KI, STEER_KD, STEER_KS, STEER_KV, STEER_KA, 0)
                            .withMotionParam(MOTION_MAGIC_VEL, MOTION_MAGIC_ACCEL, MOTION_MAGIC_JERK)
                            .withBrake(true)
                            .withInvert(false)
                            .withRadiansMotor(STEER_GEAR_RATIO)
                            .withRampTime(RAMP_TIME_STEER),
                    new TalonFXConfig(i * 3 + 1, CAN_BUS, name + "/Drive")
                            .withPID(DRIVE_KP, DRIVE_KI, DRIVE_KD, DRIVE_KS, DRIVE_KV, DRIVE_KA, 0)
                            .withBrake(true)
                            .withMeterMotor(DRIVE_GEAR_RATIO, WHEEL_DIAMETER),
                    // i != 2
                    new CancoderConfig(i * 3 + 3, CAN_BUS, name + "/Cancoder")
                    // : new CancoderConfig(6, CAN_BUS, name + "Cancoder")
                    ).withPosition(
                            new Translation2d(
                                    i == 0 || i == 1 ? 0.34 : -0.34,
                                    i == 0 || i == 2 ? 0.29 : -0.29))
                    .withSteerOffset(offsets[i]);
        }
        return ans;
    }

    public static final SwerveModuleConfig[] modules = swerveModules(
            new double[] {
                    /* Front Left Offset: */ 0.300781 * 2 * Math.PI,
                    /* Front Right Offset: */ 0.437744 * 2 * Math.PI,
                    /* Back Left Offset: */ -0.336426 * 2 * Math.PI,
                    /* Back Right Offset: */ 0.406006 * 2 * Math.PI
            });

    public static final PigeonConfig PIGEON_CONFIG = new PigeonConfig(PIGEON_ID, PIGEON_CAN_BUS, NAME + "/pigeon");

    public static final ChassisConfig CHASSIS_CONFIG = new ChassisConfig(
            NAME,
            modules,
            PIGEON_CONFIG);
}