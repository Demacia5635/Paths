// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.paths;

import edu.wpi.first.math.geometry.Pose2d;

/** Add your docs here. */
public abstract class SegmentBase {
    private Pose2d finishPoint;
    private Pose2d startingPoint;
    
    public SegmentBase(Pose2d startingPoint, Pose2d finishPoint){ 
        this.startingPoint = startingPoint;
        this.finishPoint = finishPoint;
    }
    public Pose2d getFinishPoint(){return this.finishPoint;}
    public Pose2d getStartingPoint(){return this.startingPoint;}
    
}
