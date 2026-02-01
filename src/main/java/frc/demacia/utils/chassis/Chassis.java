// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.demacia.utils.chassis;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import frc.robot.chassis.kinematics.DemaciaKinematics;
import java.util.Optional;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.Utilities;
import frc.demacia.utils.Sensors.PigeonConfig;
import frc.demacia.utils.Sensors.Pigeon;
import frc.demacia.vision.subsystem.Quest;;
import frc.demacia.vision.subsystem.Tag;
import frc.demacia.vision.subsystem.ObjectPose;
import frc.demacia.vision.utils.VisionFuse;
import static frc.demacia.vision.utils.VisionConstants.*;

/**
 * Main swerve drive chassis controller.
 * 
 * <p>Manages four swerve modules, odometry, and provides high-level drive control
 * with acceleration limiting and smooth motion profiling.</p>
 * 
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Field-relative and robot-relative control</li>
 *   <li>Pose estimation with vision integration</li>
 *   <li>Smooth acceleration limiting</li>
 *   <li>Path following capabilities</li>
 *   <li>Auto-rotate to target angle</li>
 * </ul>
 * 
 * <p><b>Example Usage:</b></p>
 * <pre>
 * ChassisConfig config = new ChassisConfig(
 *     "MainChassis", 
 *     frontLeftConfig, frontRightConfig, 
 *     backLeftConfig, backRightConfig,
 *     pigeonConfig,
 *     new Translation2d(0.3, 0.3),   // FL position
 *     new Translation2d(0.3, -0.3),  // FR position
 *     new Translation2d(-0.3, 0.3),  // BL position
 *     new Translation2d(-0.3, -0.3)  // BR position
 * );
 * 
 * Chassis chassis = new Chassis(config);
 * 
 * // Field-relative drive with acceleration limiting
 * chassis.setVelocitiesWithAccel(new ChassisSpeeds(vx, vy, omega));
 * </pre>
 */
public class Chassis extends SubsystemBase {
  
    ChassisConfig chassisConfig;
    private SwerveModule[] modules;
    private PigeonConfig gyro;

    private DemaciaKinematics demaciaKinematics;
    private SwerveDriveKinematics wpilibKinematics;
    private DemaciaPoseEstimator demaciaPoseEstimator;
    private SwerveDrivePoseEstimator poseEstimator;
    private Field2d field;

    public Tag[] tags;
    public Quest quest;
    public VisionFuse visionFuse;
    public ObjectPose objectPose;

    private StatusSignal<Angle> gyroYawStatus;
    private Rotation2d lastGyroYaw;

    public Chassis(ChassisConfig chassisConfig) {
        this.chassisConfig = chassisConfig;
        modules = new SwerveModule[] {
        new SwerveModule(chassisConfig.frontLeftModuleConfig),
        new SwerveModule(chassisConfig.frontRightModuleConfig),
        new SwerveModule(chassisConfig.backLeftModuleConfig),
        new SwerveModule(chassisConfig.backRightModuleConfig),
        };
        quest = new Quest();
        gyro = new Pigeon(chassisConfig.pigeonConfig);
        addStatus();
        Translation2d[] modulePositions = new Translation2d[] {
            chassisConfig.frontLeftPosition,
            chassisConfig.frontRightPosition,
            chassisConfig.backLeftPosition,
            chassisConfig.backRightPosition
        };
        demaciaKinematics = new DemaciaKinematics(modulePositions);
        wpilibKinematics = new SwerveDriveKinematics(modulePositions);
        demaciaPoseEstimator = new DemaciaPoseEstimator(
            modulePositions, 
            getSTD(), 
            getSTD()
        );
        poseEstimator = new SwerveDrivePoseEstimator(wpilibKinematics, getGyroAngle(), getModulePositions(), new Pose2d());

        SimpleMatrix std = new SimpleMatrix(new double[] { 0.02, 0.02, 0 });
        poseEstimator.setVisionMeasurementStdDevs(new Matrix<>(std));
        field = new Field2d();
    }

    /**
     * Checks all module electronics for faults and logs them.
     */
    public void checkElectronics() {
        for (SwerveModule module : modules) {
            module.checkElectronics();
        }
    }

    /**
     * Sets neutral mode (brake/coast) for all modules.
     * 
     * @param isBrake true for brake mode, false for coast
     */
    public void setNeutralMode(boolean isBrake) {
        for (SwerveModule module : modules) {
            module.setNeutralMode(isBrake);
        }
    }

    public void resetPose(Pose2d pose) {
        demaciaPoseEstimator.resetPose(pose);
    }

    private void addStatus() {
        gyroYawStatus = gyro.getYaw();
        lastGyroYaw = new Rotation2d(gyroYawStatus.getValueAsDouble());
    }

    /**
     * Gets the current estimated robot pose on the field.
     * 
     * @return Current pose (position and rotation) using odometry fusion
     */
    public Pose2d getPose() {
        return demaciaPoseEstimator.getEstimatedPose();
    }


    /**
     * Sets chassis velocities without acceleration limiting.
     * 
     * <p>Applies discrete kinematics for accurate odometry.
     * Use this for precise path following where acceleration is pre-profiled.</p>
     * 
     * @param speeds Desired chassis speeds (field-relative)
     */
    public void setVelocities(ChassisSpeeds speeds) {
        SwerveModuleState[] states = demaciaKinematics.toSwerveModuleStatesWithLimit(
            speeds, 
            getChassisSpeedsFieldRel(), 
            getGyroAngle()
        );
        setModuleStates(states);
    }

    /**
     * Sets robot-relative velocities with acceleration limiting.
     * 
     * <p>Useful for manual control where joystick inputs are in robot frame.</p>
     * 
     * @param speeds Desired chassis speeds (robot-relative)
     */
    public void setRobotRelSpeedsWithAccel(ChassisSpeeds speeds){
        ChassisSpeeds fieldSpeeds = ChassisSpeeds.fromRobotRelativeSpeeds(speeds, getGyroAngle());
        setVelocities(fieldSpeeds); 
    }

    public void setSteerPositions(double[] positions) {
        for (int i = 0; i < positions.length; i++) {
            modules[i].setSteerPosition(positions[i]);
        }
    }

    public void setSteerPower(double pow, int id) {
        modules[id].setSteerPower(pow);
    }

    public double getSteerVelocity(int id) {
        return modules[id].getSteerVel();
    }

    public double getSteerAcceleration(int id) {
        return modules[id].getSteerAccel();
    }

    public void setSteerPositions(double position) {
        setSteerPositions(new double[] { position, position, position, position });
    }

    public ChassisSpeeds getRobotRelVelocities() {
        return ChassisSpeeds.fromFieldRelativeSpeeds(getChassisSpeedsRobotRel(), getGyroAngle());
    }

    public void setRobotRelVelocities(ChassisSpeeds speeds) {
        SwerveModuleState[] states = wpilibKinematics.toSwerveModuleStates(speeds);
        setModuleStates(states);
    }

    public void setDriveVelocities(double[] velocities) {
        for (int i = 0; i < velocities.length; i++) {
            modules[i].setDriveVelocity(velocities[i]);
        }
    }

    public void setDriveVelocities(double velocity) {
        setDriveVelocities(new double[] { velocity, velocity, velocity, velocity });
    }

    public Rotation2d getGyroAngle() {
        gyroYawStatus.refresh();
        if (gyroYawStatus.getStatus() == StatusCode.OK) {
            lastGyroYaw = new Rotation2d(gyroYawStatus.getValue());
        }
        return lastGyroYaw;
    }

    private SwerveModulePosition[] getModulePositions() {
        SwerveModulePosition[] arr = new SwerveModulePosition[modules.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = modules[i].getModulePosition();
        }
        return arr;
    }

    public void setModuleStates(SwerveModuleState[] states) {
        for (int i = 0; i < states.length; i++) {
            modules[i].setState(states[i]);
        }
    }

    private void updateVision(Pose2d pose) {
        demaciaPoseEstimator.updateVisionSTD(getSTD());
        
        VisionMeasurment measurement = new VisionMeasurment(
            Timer.getFPGATimestamp() - 0.05,
            pose.getTranslation(),
            Optional.of(pose.getRotation())
        );
        demaciaPoseEstimator.addVisionMeasurement(measurement);
    }

    private void updateQuest(Pose2d pose){
        demaciaPoseEstimator.updateVisionSTD(getSTDQuest());
        
        VisionMeasurment measurement = new VisionMeasurment(
            quest.getTimestamp(),
            pose.getTranslation(),
            Optional.of(pose.getRotation())
        );
        demaciaPoseEstimator.addVisionMeasurement(measurement);
    }

    private Matrix<N3,N1> getSTDQuest(){
        double x =0.005; 
        double y =0.005; 
        double theta =0.035; 

        return new Matrix<N3, N1>(new SimpleMatrix(new double[] { x, y, theta }));
    }

    private Matrix<N3, N1> getSTD() {
        double x = 0.05;
        double y = 0.05;
        double theta = 0.03;

        ChassisSpeeds currentSpeeds = getChassisSpeedsRobotRel();
        double speed = Utilities.hypot(currentSpeeds.vxMetersPerSecond, currentSpeeds.vyMetersPerSecond);

        // Vision confidence adjustment
        if (visionFuse.getVisionConfidence() < 0.3) {
            x += 0.3;
            y += 0.3;
        }

        // Speed-based confidence calculation
        if (speed > WORST_RELIABLE_SPEED) {
            // Maximum uncertainty for high speeds
            x += 0.02;
            y += 0.02;
        } else if (speed <= BEST_RELIABLE_SPEED) {
            // Minimum uncertainty for low speeds
            x -= 0.02;
            y -= 0.02;
        } else {
            // Calculate normalized speed for the falloff range
            double normalizedSpeed = (speed - BEST_RELIABLE_SPEED)
                    / (WORST_RELIABLE_SPEED - BEST_RELIABLE_SPEED);

            // Apply exponential falloff to calculate additional uncertainty
            double speedConfidence = Math.exp(-3 * normalizedSpeed);

            // Scale the uncertainty adjustment based on confidence
            double adjustment = 0.02 * (1 - speedConfidence);
            x += adjustment;
            y += adjustment;
        }

        return new Matrix<N3, N1>(new SimpleMatrix(new double[] { x, y, theta }));
    }


    Pose2d questPoseEstimation;
    Pose2d visionFusePoseEstimation;
    Rotation2d gyroAngle;

    @Override
    public void periodic() {
        visionFusePoseEstimation = visionFuse.getPoseEstemation();
        questPoseEstimation = quest.getRobotPose();
        gyroAngle = getGyroAngle();

        OdometryObservation observation = new OdometryObservation(
            Timer.getFPGATimestamp(), 
            gyroAngle, 
            getModulePositions()
        );
        demaciaPoseEstimator.addOdomteryCalculation(observation, new Translation2d()); 

        if (visionFusePoseEstimation != null) {
            updateVision(new Pose2d(visionFusePoseEstimation.getTranslation(), gyroAngle));
        }
        else if(quest.isCalibrated() && questPoseEstimation != null){
            updateQuest(questPoseEstimation);
        }
        else if(visionFusePoseEstimation != null){
            quest.setQuestPose(new Pose3d(new Pose2d(visionFusePoseEstimation.getTranslation(), gyroAngle)));
        }

        field.setRobotPose(demaciaPoseEstimator.getEstimatedPose());
    }

    /**
     * Gets the current chassis speeds in robot-relative frame.
     * 
     * @return Current velocities in robot frame
     */
    public ChassisSpeeds getChassisSpeedsRobotRel() {
        return demaciaKinematics.toChassisSpeeds(
            getModuleStates(), 
            Math.toRadians(gyroYawStatus.getValueAsDouble())
        );
    }

    /**
     * Gets the current chassis speeds in field-relative frame.
    * 
    * @return Current velocities transformed to field frame
    */
    public ChassisSpeeds getChassisSpeedsFieldRel() {
        return ChassisSpeeds.fromRobotRelativeSpeeds(wpilibKinematics.toChassisSpeeds(getModuleStates()), getGyroAngle());
    }

    /**
     * Returns the state of every module
     * 
     * 
     * @return Velocity in m/s, angle in Rotation2d
     */
    public SwerveModuleState[] getModuleStates() {
        SwerveModuleState[] res = new SwerveModuleState[modules.length];
        for (int i = 0; i < modules.length; i++) {
            res[i] = modules[i].getState();
        }
        return res;
    }

    /**
     * Sets the gyro yaw angle (for field-relative reset).
     * 
     * <p>Call this at the start of autonomous to set known field orientation.</p>
     * 
     * @param angle New yaw angle (null to skip)
     */
    public void setYaw(Rotation2d angle) {
        if (angle != null) {
            gyro.setYaw(angle.getDegrees());
            poseEstimator
                    .resetPose(new Pose2d(poseEstimator.getEstimatedPosition().getTranslation(), gyro.getRotation2d()));
        }
    }

    /**
     * Drives while automatically rotating to face a target angle.
     * 
     * <p>Overrides the omega component of speeds to rotate toward target.
     * Useful for shooting while driving.</p>
     * 
     * @param speeds Base chassis speeds (vx, vy from driver)
     * @param angle Target angle in radians to face
     */
    public void setVelocitiesRotateToAngleOld(ChassisSpeeds speeds, double angle) {
        double angleError = angle - getGyroAngle().getRadians();
        double angleErrorabs = Math.abs(angleError);
        if (angleErrorabs > Math.toRadians(1.5)) {
            speeds.omegaRadiansPerSecond = angleError * 1.5;
        }

        setVelocities(speeds);
    }

    /**
     * Drives while automatically rotating to face a target pose.
     * 
     * <p>Calculates angle to target and rotates to face it.
     * Useful for auto-aiming at game pieces or goals.</p>
     * 
     * @param speeds Base chassis speeds
     * @param target Target pose to face
     */
    public void setVelocitiesRotateToTarget(ChassisSpeeds speeds, Pose2d target) {
      Translation2d robotToTarget = target.getTranslation().minus(getPose().getTranslation());
      double angleError = robotToTarget.getAngle().minus(getGyroAngle()).getRadians();
      double angleErrorabs = Math.abs(angleError);
      if (angleErrorabs > Math.toRadians(1.5)) {
          speeds.omegaRadiansPerSecond = angleError * 2;
      }
      setVelocities(speeds);
    }

    PIDController drivePID = new PIDController(2, 0, 0);

    /**
     * Autonomous navigation to a target pose with PID control.
     * 
     * @param pose Target pose to reach
     * @param threshold Distance threshold to consider "arrived" (meters)
     * @param stopWhenFinished true to stop at target, false to slow down
     */
    public void goTo(Pose2d pose, double threshold, boolean stopWhenFinished) {

        Translation2d diffVector = pose.getTranslation().minus(getPose().getTranslation());

        double distance = diffVector.getNorm();
        if (distance <= threshold) {
            if (stopWhenFinished)
                setVelocitiesRotateToAngleOld(new ChassisSpeeds(0, 0, 0), pose.getRotation().getRadians());
            else
                setVelocitiesRotateToAngleOld(
                        new ChassisSpeeds(0.5 * diffVector.getAngle().getCos(), 0.5 * diffVector.getAngle().getSin(),
                                0),
                        pose.getRotation().getRadians());
        }

        else {
            double vX = MathUtil.clamp(-drivePID.calculate(diffVector.getX(), 0), -3.2, 3.2);
            double vY = MathUtil.clamp(-drivePID.calculate(diffVector.getY(), 0), -3.2, 3.2);

            ChassisSpeeds fieldSpeeds = new ChassisSpeeds(vX, vY, 0);

            ChassisSpeeds robotSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(
                fieldSpeeds, 
                getPose().getRotation()
            );

            setVelocitiesRotateToAngleOld(robotSpeeds, pose.getRotation().getRadians());
        }

    }

    public double getMaxDriveVelocity(){
        return chassisConfig.maxDriveVelocity;
    }

    public double getMaxRotationalVelocity(){
        return chassisConfig.maxRotationalVelocity;
    }

    public Pose2d computeFuturePosition(double sec){
        return CalculatePositionAndAngle.computeFuturePosition(getChassisSpeedsFieldRel(), getPose(), sec);
    }

    /**
     * Stops all swerve modules immediately.
     */
    public void stop() {
        for (SwerveModule i : modules) {
            i.stop();
        }
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        super.initSendable(builder);
    }

    public Trajectory vector(Translation2d start, Translation2d end){
      return TrajectoryGenerator.generateTrajectory(
            List.of(
              new Pose2d(start, end.getAngle().minus(start.getAngle())),
              new Pose2d(end, end.getAngle().minus(start.getAngle()))),
            new TrajectoryConfig(4.0, 4.0));
    }
}