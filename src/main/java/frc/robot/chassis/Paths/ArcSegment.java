// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public class ArcSegment extends SegmentBase{

    private Translation2d centerCircle;

    public ArcSegment(Pose2d startingPoint, Pose2d finishPoint, Translation2d centerCircle ){
        super(startingPoint, finishPoint);
        this.centerCircle = centerCircle;
    }

    public Translation2d getCenterCircle(){return this.centerCircle;}






}
