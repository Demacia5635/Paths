// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

/** Add your docs here. */
public class DemaciaTrajectory {

    private final ArrayList<Pose2d> trajectoryPoints;
    private ArrayList<Pose2d> pathPoints;

    private ArrayList<SegmentBase> segments;
    private ArrayList<CenterCircleWithDirection> circleCenters;
    private SegmentBase currentSegment;
    private int arcCount;
    private int currentSegmentIndex;
    private boolean isFinishedTrajectory;
    

    public DemaciaTrajectory(ArrayList<Pose2d> trajectoryPoints) {
        this.trajectoryPoints = trajectoryPoints;
        this.pathPoints = new ArrayList<Pose2d>();
        this.circleCenters = new ArrayList<CenterCircleWithDirection>();
        this.segments = new ArrayList<SegmentBase>();
        this.arcCount = trajectoryPoints.size() - 2;
        this.isFinishedTrajectory = false;
        if(PathsUtils.isLineSegment(currentSegment)){
            createSimplePath(currentSegment.getStartingPoint(), currentSegment.getFinishPoint());
        }else{
            createCenterCircles();
            createPathPoints();
            createSegments();
        }
        currentSegmentIndex = 0;
        currentSegment = segments.get(currentSegmentIndex);

        
        
    }

    private boolean isFinishedSegment(ChassisSpeeds currentSpeeds, Pose2d currentPose, SegmentBase currentSegment){
        double distanceFromFinishPoint = currentSegment.getFinishPoint().getTranslation().getDistance(currentPose.getTranslation());
        Rotation2d currentVelocityHeading = new Translation2d(currentSpeeds.vxMetersPerSecond, currentSpeeds.vyMetersPerSecond).getAngle();

        if(currentSegment instanceof LineSegment){

            
            boolean isVelocityHeadingTowardesFinishPoint = PathsUtils.isVelocityHeadingInRange(currentVelocityHeading, ((LineSegment)currentSegment).getStartToFinishVector().getAngle());
            if(currentSegmentIndex == segments.size() -1){
                return (distanceFromFinishPoint < PathsConstants.MAX_POSITION_THRESHOLD_FINAL_POINT);
            }
            
            return (distanceFromFinishPoint < PathsConstants.MAX_POSITION_THRESHOLD_DURING_PATH) || ((distanceFromFinishPoint < (PathsConstants.MAX_POSITION_THRESHOLD_DURING_PATH * 3)) && isVelocityHeadingTowardesFinishPoint);
            
            
        }

        else{
            ArcSegment segment = (ArcSegment) currentSegment;
            Translation2d centerToFinish = segment.getCenterCircle().minus(segment.getFinishPoint().getTranslation());
            Rotation2d wantedVelocityHeading = centerToFinish.getAngle().minus(Rotation2d.kCW_90deg);
            boolean isHeadingTowardesNextSegment = PathsUtils.isVelocityHeadingInRange(currentVelocityHeading, wantedVelocityHeading);


            

            return (distanceFromFinishPoint < PathsConstants.MAX_POSITION_THRESHOLD_DURING_PATH) || ((distanceFromFinishPoint < (PathsConstants.MAX_POSITION_THRESHOLD_DURING_PATH * 3)) && isHeadingTowardesNextSegment);
        }
    }




    public void createSimplePath(Pose2d firstPoint, Pose2d lestPoint){
        segments.add(new LineSegment(trajectoryPoints.get(0), trajectoryPoints.get(1)));
    }

    public ChassisSpeeds calculateSpeeds(ChassisSpeeds currentSpeeds, Pose2d currentPose) {
        
        double finishVelocity = currentSegmentIndex == segments.size() - 1 ? 0 : PathsConstants.MAX_LINEAR_VELOCITY;
        ChassisSpeeds speeds = SegmentFollow.getInstance().calculateSpeeds(segments.get(currentSegmentIndex), currentSpeeds, currentPose, finishVelocity);

        if(isFinishedSegment(currentSpeeds, currentPose, currentSegment)){
            if(currentSegmentIndex == segments.size() - 1) isFinishedTrajectory = true;
            currentSegmentIndex++;
            currentSegment = segments.get(currentSegmentIndex);
        }

        return speeds;
    }

    private Translation2d calculateP1OnIntialArc(Translation2d startingPoint, CenterCircleWithDirection centerCircle) {
        Translation2d p1ToCenter = centerCircle.centerCircle().minus(startingPoint);
        double radius = PathsConstants.MAX_ALLOWED_RADIUS;
        
        Rotation2d toP1Angle = Rotation2d.kZero;
        if(centerCircle.isTurningRight()){
            toP1Angle = p1ToCenter.getAngle().plus(new Rotation2d(Math.asin(radius / p1ToCenter.getNorm())));
        }
        else{
            toP1Angle = p1ToCenter.getAngle().minus(new Rotation2d(Math.asin(radius / p1ToCenter.getNorm())));
        }
        double toP1Norm = Math.sqrt((radius * radius) + (p1ToCenter.getNorm() * p1ToCenter.getNorm()));
        return new Translation2d(toP1Norm, toP1Angle);
    }

    private Translation2d calculateP2OnLastArc(CenterCircleWithDirection lastCenterCircle, Translation2d lastTrajPoint){
        double distance = lastTrajPoint.minus(lastCenterCircle.centerCircle()).getNorm();
        Rotation2d angle = new Rotation2d(Math.acos(PathsConstants.MAX_ALLOWED_RADIUS/distance));
        Translation2d vectorToReturn = Translation2d.kZero;
        if(lastCenterCircle.isTurningRight()){
            vectorToReturn = lastCenterCircle.centerCircle().plus(new Translation2d(distance, angle));
        }
        else{
            
            vectorToReturn = lastCenterCircle.centerCircle().minus(new Translation2d(distance, angle));
        }
        return vectorToReturn;
    }

    private void createCenterCircles(){
        for(int i = 0; i < trajectoryPoints.size() - 2; i++){
           CenterCircleWithDirection center = PathsUtils.ArcUtils.calculateCenterCircle(trajectoryPoints.get(i), trajectoryPoints.get(i+1), trajectoryPoints.get(i+2));
            circleCenters.add(center);
        }
    }

    private void createPathPoints(){
        pathPoints.add(trajectoryPoints.get(0));
        Translation2d firstPointOnArc = calculateP1OnIntialArc(trajectoryPoints.get(0).getTranslation(), circleCenters.get(0));
        pathPoints.add(new Pose2d(firstPointOnArc, trajectoryPoints.get(1).getRotation()));
        for(int i = 0; i < circleCenters.size() -1; i++){ 
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
        pathPoints.add(new Pose2d(calculateP2OnLastArc(circleCenters.get(circleCenters.size()-1), lastTrajPoint), trajectoryPoints.get(trajectoryPoints.size() - 1).getRotation()));
        pathPoints.add(trajectoryPoints.get(trajectoryPoints.size() - 1));
    }
    private void createSegments(){
        for(int i = 0; i < pathPoints.size() / 2; i++){
            segments.add(new LineSegment(pathPoints.get(i), pathPoints.get(i+1)));
            segments.add(new ArcSegment(pathPoints.get(i+1), pathPoints.get(i+2), circleCenters.get(i).centerCircle()));
        }
        segments.add(new LineSegment(pathPoints.get(pathPoints.size() - 2), pathPoints.get(pathPoints.size() - 1)));
    }

    public boolean isFinishedTrajectory(){
        return this.isFinishedTrajectory;
    }


    public record CenterCircleWithDirection(Translation2d centerCircle, boolean isTurningRight) {}
}
