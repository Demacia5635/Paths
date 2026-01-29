// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import frc.robot.chassis.kinematics.DemaciaKinematics;

import java.util.ArrayList;

import org.opencv.core.Point;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.chassis.Chassis;
import frc.robot.chassis.Paths.DemaciaTrajectory;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class PathCommand extends Command {
  /** Creates a new tstingPathCommand. */

   DemaciaKinematics kinematics;
   Chassis chassis;
   DemaciaTrajectory demaciaTrajectory;
  ArrayList<Pose2d> pointList = new ArrayList<>();  
  pointList.add(new Pose2d(chassis.getPose()))
  pointList.add(new Pose2d(3, 2, new Rotation2d(68)))
  pointList.add(new Pose2d(4.4, 3.55, new Rotation2d(50)))
  pointList.add(new Pose2d(1.45, 1, new Rotation2d(49)))

  public PathCommand() {
    demaciaTrajectory = new DemaciaTrajectory(pointList);
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {} 

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    ChassisSpeeds needSpeed = demaciaTrajectory.calculateSpeeds(chassis.getChassisSpeedsRobotRel(), chassis.getPose());
    chassis.setVelocities(needSpeed);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    chassis.stop();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    if(demaciaTrajectory.isFinishedTrajectory()){
      return true;
    }else{
      return false;
    }
  }
}
