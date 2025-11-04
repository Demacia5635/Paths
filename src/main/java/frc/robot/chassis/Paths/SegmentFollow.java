// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import static frc.robot.chassis.utils.ChassisConstants.MAX_DRIVE_VELOCITY;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.ExponentialProfile;
import edu.wpi.first.math.trajectory.ExponentialProfile.Constraints;
import frc.robot.utils.TrapezoidNoam;
import edu.wpi.first.math.trajectory.TrapezoidProfile;

/** Add your docs here. */
public class SegmentFollow {
    private TrapezoidExpo driveTrapezoid;
    
    private TrapezoidNoam rotationTrapezoid;
    private static SegmentFollow instace;
    
   
    private SegmentFollow(double maxVel, double maxAccel, double maxJerk){
        this.driveTrapezoid = new TrapezoidExpo(maxVel, maxAccel, maxJerk);
        this.rotationTrapezoid = new TrapezoidNoam(PathsConstants.MAX_OMEGA_ACCEL, PathsConstants.MAX_OMEGA_VELOCITY);

    }
    public static SegmentFollow getInstance(){
        if(instace == null) instace = new SegmentFollow(PathsConstants.MAX_LINEAR_VELOCITY, PathsConstants.MAX_LINEAR_ACCEL, PathsConstants.MAX_LINEAR_ACCEL * 2 );
        return instace;
    }

    public ChassisSpeeds calculateSpeeds(SegmentBase currentSegment, ChassisSpeeds currentVelocity, Pose2d chassisPose){
        return calculateSpeeds(currentSegment, currentVelocity, chassisPose, PathsConstants.MAX_LINEAR_VELOCITY);
    }

    public ChassisSpeeds calculateSpeeds(SegmentBase currentSegment, ChassisSpeeds currentVelocity, Pose2d chassisPose, double finishVelocity){
        Translation2d currentVelocityVector = new Translation2d(currentVelocity.vxMetersPerSecond, currentVelocity.vyMetersPerSecond);
        Translation2d chassisPos = chassisPose.getTranslation();

        Translation2d calculatedVelocity = Translation2d.kZero;
        if(PathsUtils.isLineSegment(currentSegment)){
            LineSegment segment = (LineSegment) currentSegment;
            
            Translation2d posToFinish = segment.getFinishPoint().getTranslation().minus(chassisPos);

            double velocity = driveTrapezoid.calculate(posToFinish.getNorm(), currentVelocityVector.getNorm(), finishVelocity);
            Rotation2d velocityHeadingError = segment.getStartToFinishVector().getAngle().minus(posToFinish.getAngle());
            Rotation2d fixedVelocityHeading = posToFinish.getAngle().minus(velocityHeadingError);
            
            calculatedVelocity = new Translation2d(velocity, fixedVelocityHeading);
        }

        else{
            ArcSegment segment = (ArcSegment) currentSegment;
            Translation2d centerToChassis = chassisPos.minus(segment.getCenterCircle());
            Rotation2d tanToCircleAngle = centerToChassis.getAngle().plus(Rotation2d.kCW_90deg.times(Math.signum(segment.getAngleBetweenRadius().getRadians())));
            Rotation2d fixedVelocityHeadingWithRatio = tanToCircleAngle.times(centerToChassis.getNorm() / PathsConstants.MAX_ALLOWED_RADIUS);
            double velocity = 0;
            if(Math.abs(currentVelocityVector.getNorm() - PathsConstants.MAX_LINEAR_VELOCITY) < 0.1) velocity = PathsConstants.MAX_LINEAR_VELOCITY;
            else velocity = driveTrapezoid.calculate(chassisPos.minus(segment.getFinishPoint().getTranslation()).getNorm(), currentVelocityVector.getNorm(), finishVelocity);
            calculatedVelocity = new Translation2d(velocity, fixedVelocityHeadingWithRatio);
        }

        double angleError = currentSegment.getFinishPoint().getRotation().minus(chassisPose.getRotation()).getRadians();
        double omega = rotationTrapezoid.calculate(angleError, currentVelocity.omegaRadiansPerSecond, 0);
        return new ChassisSpeeds(calculatedVelocity.getX(), calculatedVelocity.getY(), omega);


    }






}