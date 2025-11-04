// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

/** Add your docs here. */
public class SegmentFollow {
    private TrapezoidExpo trapezoid;
    private static SegmentFollow instace;
    private SegmentFollow(double maxVel, double maxAccel, double maxJerk){
        this.trapezoid = new TrapezoidExpo(maxVel, maxAccel, maxJerk);
    }
    public static SegmentFollow getInstance(){
        if(instace == null) instace = new SegmentFollow(PathsConstants.MAX_LINEAR_VELOCITY, PathsConstants.MAX_LINEAR_ACCEL, PathsConstants.MAX_LINEAR_ACCEL * 2 );
        return instace;
    }

    public ChassisSpeeds calculateSpeeds(SegmentBase currentSegment, Translation2d currentVelocity, Pose2d chassisPose){
        return calculateSpeeds(currentSegment, currentVelocity, chassisPose, PathsConstants.MAX_LINEAR_VELOCITY);
    }

    public ChassisSpeeds calculateSpeeds(SegmentBase currentSegment, Translation2d currentVelocity, Pose2d chassisPose, double finishVelocity){
        
        if(PathsUtils.isLineSegment(currentSegment)){
            LineSegment segment = (LineSegment) currentSegment;
            Translation2d chassisPos = chassisPose.getTranslation();
            Translation2d posToFinish = segment.getFinishPoint().getTranslation().minus(chassisPos);

            double velocity = trapezoid.calculate(posToFinish.getNorm(), currentVelocity.getNorm(), finishVelocity);
            Rotation2d velocityHeading = 



        }
    }




}