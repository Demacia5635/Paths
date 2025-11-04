// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

/** Add your docs here. */
public class LineSegment extends SegmentBase{
    private Translation2d startToFinishVector;
    
    private double finishVelocity;
    
    public LineSegment(Pose2d startingPoint, Pose2d finishPoint, double finishVelocity){
        super(startingPoint, finishPoint);
        this.finishVelocity = finishVelocity;
        this.startToFinishVector = finishPoint.getTranslation().minus(startToFinishVector);
    }
    public LineSegment(Pose2d startingPoint, Pose2d finishPoint){
        this(startingPoint, finishPoint, PathsConstants.MAX_LINEAR_VELOCITY);
    }


    public Translation2d getStartToFinishVector(){return this.startToFinishVector;}
    public double getFinishVelocity(){return this.finishVelocity;}
    
    



}
