package frc.robot.ChassisConstants;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import frc.demacia.utils.chassis.ChassisConfig;
import frc.demacia.utils.chassis.SwerveModuleConfig;
import frc.demacia.utils.motors.TalonFXConfig;
import frc.demacia.utils.motors.BaseMotorConfig.Canbus;
import frc.demacia.utils.sensors.CancoderConfig;
import frc.demacia.utils.sensors.PigeonConfig;

/** 
 * Robot A Chasis
 * the chassis that currenty contains the intake and the shinua
 */
public final class RobotAChassisConstants {

    /** The name of the Chasssi */
    public static final String NAME = "Robot A Chassis";
    /** The canbus that is used in the chassis, the chassis only contains the rio canbus */
    public static final Canbus CANBUS = Canbus.CANIvore;

    /** Drive Motor Constant */
    private static final class DriveMotor {
        /** The name of the motor */
        private static final String NAME = "Drive Motor";

        /** The gear ratio of the drive motor, currenty R2 */
        private static final double GEAR_RATIO = 6.03;
        /** The wheel diamter of the moduel in meter */
        private static final double WHEEL_DIAMETER = 0.1016;
        /** Set if the motor is brake or coast */
        private static final boolean IS_BRAKE = true;
        /** Set if the motor is clockwise or inverted */
        private static final boolean IS_INVERT = false;

        /* Drive Motor PID and FF */
        private static final double KP = 1d;
        private static final double KI = 0d;
        private static final double KD = 0d;
        private static final double KS = 0.22880;
        private static final double KV = 2.24755;
        private static final double KA = 0d;
        private static final double KG = 0d;

        /**
         * Function to get the drive motor config of a module
         * @param id the id of the motor, from 1 to 62
         * @param moduleName the name of the module (eg. "Front Left Module", "Front Right Module")
         * @return the config of the drive motor with all the constants
         */
        private static TalonFXConfig getDriveMotor(int id, String moduleName) {
            return new TalonFXConfig(id, CANBUS, RobotAChassisConstants.NAME + "/" + moduleName + "/" + NAME)
                    .withBrake(IS_BRAKE)
                    .withInvert(IS_INVERT)
                    .withPID(KP, KI, KD, KS, KV, KA, KG)
                    .withMeterMotor(GEAR_RATIO, WHEEL_DIAMETER);
        }
    }

    /** Steer Motor Constants */
    private static final class SteerMotor {
        /** The name of the motor */
        private static final String NAME = "Steer Motor";

        /** The amount of time it will take the motor to go from 0 to 12 volt, in seconds */
        private static final double RAMP_TIME = 0.25;
        /** The gear ratio of the steer in Mk5n */
        private static final double GEAR_RATIO = 287.0 / 11.0;
        /**  Set if the motor is at brake or coast*/
        private static final boolean IS_BRAKE = true;
        /** Set if the motor is clockwise or inverted */
        private static final boolean IS_INVERT = true;

        /* Steer Motor PID + FF*/
        private static final double KP = 6.5d;
        private static final double KI = 0d;
        private static final double KD = 0d;
        private static final double KS = 0.4254d;
        private static final double KV = 0.3218d;
        private static final double KA = 0d;
        private static final double KG = 0d;

        /**
         * Function to get a steer motor of a module
         * @param id the id of the motor, from 1 to 62
         * @param moduleName the name of the module (eg. "Front Left Module", "Front Right Module")
         * @return the config of the steer motor with all the constants
         */
        private static TalonFXConfig getSteerMotor(int id, String moduleName) {
            return new TalonFXConfig(id, CANBUS, RobotAChassisConstants.NAME + "/" + moduleName + "/" + NAME)
                    .withBrake(IS_BRAKE)
                    .withInvert(IS_INVERT)
                    .withRampTime(RAMP_TIME)
                    .withVolts(8)
                    .withPID(KP, KI, KD, KS, KV, KA, KG)
                    .withRadiansMotor(GEAR_RATIO);
        }
    }

    /** The Pigeon id */
    private static final int PIGEON_ID = 14;
    /** The  config of the pigeon*/
    private static final PigeonConfig PIGEON_CONFIG = new PigeonConfig(PIGEON_ID, CANBUS, NAME + "/" + "Pigeon");

    /**
     * Function to get the all the swerve module configs
     * @param offsets the offset of all the modules based on the module order
     * @see Module_Order is Front Left, Front Right, Back Left, Back Right
     * @return all the swerve module configs in the module order
     */
    private static SwerveModuleConfig[] getSwerveModuleConfigs(double[] offsets) {
        SwerveModuleConfig[] ans = new SwerveModuleConfig[4];
        for (int i = 0; i < 4; i++) {
            /* set the name of the module */
            String moduleName = "Error";
            switch (i) {
                case 0:
                    moduleName = "Front Left";
                    break;

                case 1:
                    moduleName = "Front Right";
                    break;

                case 2:
                    moduleName = "Back Left";
                    break;

                case 3:
                    moduleName = "Back Right";
                    break;

                default:
                    break;
            }

            /* set a new config based on the steer motor and drvie motor constatns classes */
            ans[i] = new SwerveModuleConfig(
                    moduleName,
                    SteerMotor.getSteerMotor(i * 3 + 2, moduleName),
                    DriveMotor.getDriveMotor(i * 3 + 1, moduleName),
                    new CancoderConfig(i * 3 + 3, CANBUS, NAME + "/" + moduleName + "/" + "Cancoder"))
                    .withPosition(new Translation2d(
                            i == 0 || i == 1 ? 0.295 : -0.295,
                            i == 0 || i == 2 ? 0.395 : -0.395))
                    .withSteerOffset(offsets[i]);
        }

        return ans;
    }

    /** The swerve modules config using the function */
    private static final SwerveModuleConfig[] MODULES = getSwerveModuleConfigs(
            new double[] {
                    /* Front Left Offset: */ -0.2024882128944765333918712166259,
                    /* Front Right Offset: */ 0.18254538272948852591411035642884,
                    /* Back Left Offset: */ -0.11197892854455459019176246075361,
                    /* Back Right Offset: */ 0.01840973295003618837739109022602
            });


    // public static final TagPose FUEL = new TagPose(new Camera("fuel", new Translation3d(0.329, -0.24325, 0.3195), -24, 0, false, true));

    // public static final TagPose LIMELIGHT4 = new TagPose(new Camera("hub", new Translation3d(-0.133, 0.19, 0.35345), 30.0, 0.0, false,  Rotation2d.kZero, new Translation3d(0.11307,0.14305,0.18386) ));
    /** The Chassis config with the chassis name, modules config, pigeon config and tags */
    public static final ChassisConfig CHASSIS_CONFIG = new ChassisConfig(
        NAME, 
        MODULES, 
        PIGEON_CONFIG);
}
