// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.paths;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import frc.robot.chassis.paths.DemaciaTrajectory.CenterCircleWithDirection;

/** Add your docs here. */
public class PathsUtils {

    public static boolean isVelocityHeadingInRange(Rotation2d currentVelocityHeading,
            Rotation2d wantedVelocityHeading) {
        return Math.abs(currentVelocityHeading.minus(wantedVelocityHeading)
                .getRadians()) < PathsConstants.MAX_VELOCITY_HEADING_TO_FINISH_SEGMENT;
    }

    public static boolean isLineSegment(SegmentBase segment) {
        return segment instanceof LineSegment;
    }

    public static class ArcUtils {
        public static CenterCircleWithDirection calculateCenterCircle(Pose2d p1, Pose2d p2, Pose2d p3) {
            return calculateCenterWithDirection(p1.getTranslation(), p2.getTranslation(), p3.getTranslation());
        }

        public static CenterCircleWithDirection calculateCenterWithDirection(Translation2d trajP1, Translation2d trajP2,Translation2d trajP3){
            Translation2d p1p2 = trajP2.minus(trajP1);
            Translation2d p2p3 = trajP3.minus(trajP2);
            Rotation2d middleAngle = (p1p2.getAngle().plus(Rotation2d.k180deg).plus(p2p3.getAngle())).div(2);
            Rotation2d turningAngle = p2p3.getAngle().minus(p1p2.getAngle());
            boolean isTurningRight = MathUtil.angleModulus(turningAngle.getRadians()) > 0;
            return new CenterCircleWithDirection(new Translation2d(PathsConstants.MAX_ALLOWED_RADIUS, middleAngle), isTurningRight);
        }

        public static class Same {
            public static Translation2d calculateEntryPointOfArc(Translation2d exitPointLastArc, Translation2d centerCircle1, Translation2d centerCircle2) {
                Translation2d center1toCenter2 = centerCircle2.minus(centerCircle1);
                return exitPointLastArc.plus(center1toCenter2);
            }
            public static Translation2d calculateExitPointOfArc(Translation2d p, Translation2d centerCircle1, Translation2d centerCircle2) {
                double radius = PathsConstants.MAX_ALLOWED_RADIUS;
                Translation2d center1toCenter2 = centerCircle2.minus(centerCircle1);
                Rotation2d angle = Rotation2d.kCW_90deg.plus(center1toCenter2.getAngle());
                return new Translation2d(radius, angle);
            }
        }

        public static class Different{
            public static Translation2d calculateExitPointOfArc(Translation2d centerCircle1, Translation2d centerCircle2){
                Translation2d center1toCenter2 = centerCircle2.minus(centerCircle1);
                Rotation2d angle = new Rotation2d(Math.acos((PathsConstants.MAX_ALLOWED_RADIUS * 2) / center1toCenter2.getNorm())); 
                return centerCircle1.plus(new Translation2d(PathsConstants.MAX_ALLOWED_RADIUS, angle));


            }
            public static Translation2d calculateEntryPointOfArc(Translation2d centerCircle1, Translation2d centerCircle2, Translation2d prevPoint){
                Translation2d center1ToPrevPoint = prevPoint.minus(centerCircle1);
                Translation2d center1toCenter2 = centerCircle2.minus(centerCircle1);
                Rotation2d centerToPrevPointAngle = new Rotation2d(Math.acos((PathsConstants.MAX_ALLOWED_RADIUS * 2) / center1toCenter2.getNorm())); 
                double p2ToP3Distance = 2 * PathsConstants.MAX_ALLOWED_RADIUS * centerToPrevPointAngle.getCos();
               Translation2d p2ToP3 = new Translation2d(p2ToP3Distance, center1ToPrevPoint.getAngle().plus(Rotation2d.kCW_90deg)); 
                return prevPoint.plus(p2ToP3);

            }
        }

    }

}
