// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public class DemaciaTrajectory {

    private final ArrayList<Pose2d> trajectoryPoints;
    private ArrayList<Pose2d> pathPoints;

    private ArrayList<SegmentBase> segments;
    private ArrayList<CenterCircleWithDirection> circleCenters;

    public DemaciaTrajectory(ArrayList<Pose2d> trajectoryPoints) {
        this.trajectoryPoints = trajectoryPoints;
        this.pathPoints = new ArrayList<Pose2d>();
        this.circleCenters = new ArrayList<CenterCircleWithDirection>();
        this.segments = new ArrayList<SegmentBase>();
        
        createCenterCircles();



    }

    private Translation2d calculateP1OnIntiailArc(Translation2d startingPoint, Translation2d centerCircle) {
        Translation2d p1ToCenter = centerCircle.minus(startingPoint);
        double radius = PathsConstants.MAX_ALLOWED_RADIUS;
        Rotation2d toP1Angle = p1ToCenter.getAngle().plus(new Rotation2d(Math.sin(radius / p1ToCenter.getNorm())));
        double toP1Norm = Math.sqrt((radius * radius) + (p1ToCenter.getNorm() * p1ToCenter.getNorm()));
        return new Translation2d(toP1Norm, toP1Angle);
    }

    private void createCenterCircles(){
        for(int i = 0; i <= trajectoryPoints.size() - 2; i++){
            Translation2d circleCenter = PathsUtils.ArcUtils.calculateCenterCircle(trajectoryPoints.get(i), trajectoryPoints.get(i+1), trajectoryPoints.get(i+2));
            Boolean isTurningRight = PathsUtils.ArcUtils.isRightTurn(circleCenter, circleCenter, circleCenter);
            circleCenters.add(new CenterCircleWithDirection(circleCenter, isTurningRight));
        }
    }

    private void createPathPoints(){
        pathPoints.add(trajectoryPoints.get(0));

        


        pathPoints.add(trajectoryPoints.get(trajectoryPoints.size() - 1));
    }




    private record CenterCircleWithDirection(Translation2d centerCircle, boolean isTurningRight) {
    }
}
