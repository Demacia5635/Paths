// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Paths;

import edu.wpi.first.math.geometry.Translation2d;

/** Add your docs here. */
public class PathsUtils {
    public static double distanceOfPointFromLine(Translation2d pointA, Translation2d pointB, Translation2d point){
        double y2 = pointB.getY();
        double y1 = pointA.getY();
        double x2 = pointB.getX();
        double x1 = pointA.getX();
        double y0 = point.getY();
        double x0 = point.getX();
        
        return (Math.abs(((y2-y1)*x0) - ((x2-x1)*y0) + (x2 * y1) - (y2 * x1))) / Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    }
    public static Translation2d rotateVector180deg(Translation2d vector){
        return vector.times(-1);
    }
    public static Translation2d normalVector(Translation2d vector, double scalar){
        return (vector.div(vector.getNorm())).times(scalar);
    }

}
