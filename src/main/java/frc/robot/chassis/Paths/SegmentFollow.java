// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import frc.demacia.utils.Log.LogManager;

import static frc.robot.chassis.Paths.PathsConstants.*;

/** Add your docs here. */
public class SegmentFollow {
    // private Trapezoid driveTrapezoid;
    private TrapezoidProfile driveTrapezoid;
    private Trapezoid rotationTrapezoid;
    private static SegmentFollow instace;
    
   
    private SegmentFollow(){
        this.driveTrapezoid = new TrapezoidProfile(new Constraints(MAX_LINEAR_VELOCITY, MAX_LINEAR_ACCEL));
        this.rotationTrapezoid = new Trapezoid(MAX_OMEGA_ACCEL, MAX_OMEGA_VELOCITY);

    }
    public static SegmentFollow getInstance(){
        if(instace == null) instace = new SegmentFollow();
        return instace;
    }

    public ChassisSpeeds calculateSpeeds(SegmentBase currentSegment, ChassisSpeeds currentVelocity, Pose2d chassisPose, double finishVelocity){
        Translation2d currentVelocityVector = new Translation2d(currentVelocity.vxMetersPerSecond, currentVelocity.vyMetersPerSecond);
        Translation2d chassisPos = chassisPose.getTranslation();

        Translation2d calculatedVelocity = Translation2d.kZero;
        if(PathsUtils.isLineSegment(currentSegment)){
            LineSegment segment = (LineSegment) currentSegment;
            
            Translation2d posToFinish = segment.getFinishPoint().getTranslation().minus(chassisPos);

            // double velocity = driveTrapezoid.calculate(posToFinish.getNorm(), currentVelocityVector.getNorm(), finishVelocity);
            double velocity = driveTrapezoid.calculate(posToFinish.getNorm() / MAX_LINEAR_VELOCITY, 
                new State(posToFinish.getNorm(), PathsUtils.getVelocityNorm(currentVelocity)), new State(0, finishVelocity)).velocity;
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
            else velocity = Math.min(chassisPos.minus(segment.getFinishPoint().getTranslation()).getNorm() * 2, MAX_LINEAR_VELOCITY);
            calculatedVelocity = new Translation2d(velocity, fixedVelocityHeadingWithRatio);
        }
        double angleError = currentSegment.getFinishPoint().getRotation().minus(chassisPose.getRotation()).getRadians();
        double omega = rotationTrapezoid.calculate(angleError, currentVelocity.omegaRadiansPerSecond, 0);
        return new ChassisSpeeds(calculatedVelocity.getX(), calculatedVelocity.getY(), omega);


    }






}