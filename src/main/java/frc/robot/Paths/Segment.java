// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Paths;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public abstract class Segment {
    public Translation2d pointA;
    public Translation2d pointB;
    
    public Segment(Translation2d pointA, Translation2d pointB){
        this.pointA = pointA;
        this.pointB = pointB;
    }

    public double getLength(){return -1;}
    public double getDistanceOnSegment(){return -1;}
    public double getDistanceFromLastPoint(){return -1;}
    public double getDistanceLeftOnSegment(){return -1;}

    public Translation2d calculate(){return new Translation2d();}

    public Translation2d getPointA(){return this.pointA;}
    public Translation2d getPointB(){return this.pointB;}

    public boolean hasFinishedSegment(){return false;}

    public void updateCurrentPosition(Translation2d chassisPose){}
    



}
