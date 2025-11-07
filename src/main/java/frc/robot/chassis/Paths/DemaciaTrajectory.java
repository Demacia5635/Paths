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
    private int arcCount;

    public DemaciaTrajectory(ArrayList<Pose2d> trajectoryPoints) {
        this.trajectoryPoints = trajectoryPoints;
        this.pathPoints = new ArrayList<Pose2d>();
        this.circleCenters = new ArrayList<CenterCircleWithDirection>();
        this.segments = new ArrayList<SegmentBase>();
        this.arcCount = trajectoryPoints.size() - 2;        
        createCenterCircles();



    }

    private Translation2d calculateP1OnIntialArc(Translation2d startingPoint, Translation2d centerCircle) {
        Translation2d p1ToCenter = centerCircle.minus(startingPoint);
        double radius = PathsConstants.MAX_ALLOWED_RADIUS;
        Rotation2d toP1Angle = p1ToCenter.getAngle().plus(new Rotation2d(Math.sin(radius / p1ToCenter.getNorm())));
        double toP1Norm = Math.sqrt((radius * radius) + (p1ToCenter.getNorm() * p1ToCenter.getNorm()));
        return new Translation2d(toP1Norm, toP1Angle);
    }

    private Translation2d calculateP2OnLastArc(Translation2d lastCenterCircle, Translation2d lastTrajPoint){
        double distance = lastTrajPoint.minus(lastCenterCircle).getNorm();
        Rotation2d angle = new Rotation2d(Math.acos(PathsConstants.MAX_ALLOWED_RADIUS/distance));

        return lastCenterCircle.plus(new Translation2d(distance, angle));
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
        pathPoints.add(new Pose2d(calculateP1OnIntialArc(trajectoryPoints.get(0).getTranslation(), circleCenters.get(0).centerCircle()), trajectoryPoints.get(1).getRotation()));
        for(int i = 0; i < arcCount - 1; i++){
            if(circleCenters.get(i).isTurningRight() == circleCenters.get(i+1).isTurningRight()){
                pathPoints.add(new Pose2d(PathsUtils.ArcUtils.Same.calculateExitPointOfArc(trajectoryPoints.get(i).getTranslation(), circleCenters.get(i).centerCircle(), circleCenters.get(i+1).centerCircle()), trajectoryPoints.get(i).getRotation()));
                pathPoints.add(new Pose2d(PathsUtils.ArcUtils.Same.calculateEntryPointOfArc(trajectoryPoints.get(i+1).getTranslation(), circleCenters.get(i+1).centerCircle(), circleCenters.get(i+2).centerCircle()), trajectoryPoints.get(i+1).getRotation()));
            }
            else{

                pathPoints.add(new Pose2d(PathsUtils.ArcUtils.Different.calculateExitPointOfArc(circleCenters.get(i).centerCircle(), circleCenters.get(i+1).centerCircle()), trajectoryPoints.get(i).getRotation()));
                pathPoints.add(new Pose2d(PathsUtils.ArcUtils.Different.calculateEntryPointOfArc(circleCenters.get(i+1).centerCircle(), circleCenters.get(i+2).centerCircle(), pathPoints.get(pathPoints.size()-1).getTranslation()), trajectoryPoints.get(i).getRotation()));
            }
        }
        
        Translation2d lastTrajPoint = trajectoryPoints.get(trajectoryPoints.size()-1).getTranslation();
        pathPoints.add(new Pose2d(calculateP2OnLastArc(circleCenters.get(circleCenters.size()-1).centerCircle(), lastTrajPoint), trajectoryPoints.get(trajectoryPoints.size() - 1).getRotation()));
        pathPoints.add(trajectoryPoints.get(trajectoryPoints.size() - 1));
    }
    private void createSegments(){
        for(int i = 0; i < pathPoints.size() / 2; i++){
            segments.add(new LineSegment(pathPoints.get(i), pathPoints.get(i+1)));
            segments.add(new ArcSegment(pathPoints.get(i+1), pathPoints.get(i+2), circleCenters.get(i).centerCircle()));
        }
        segments.add(new LineSegment(pathPoints.get(pathPoints.size() - 2), pathPoints.get(pathPoints.size() - 1)));
    }




    private record CenterCircleWithDirection(Translation2d centerCircle, boolean isTurningRight) {}
}
