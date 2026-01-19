// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import frc.robot.chassis.kinematics.DemaciaKinematics;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.spline.PoseWithCurvature;
import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.chassis.Chassis;
import frc.robot.chassis.Paths.DemaciaTrajectory;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class tstingPathCommand extends Command {
  /** Creates a new tstingPathCommand. */

   DemaciaKinematics kinematics;
   Chassis chassis;
   DemaciaTrajectory demaciaTrajectory;
  ArrayList<Pose2d> pointList = new ArrayList<>();
  public tstingPathCommand(DemaciaKinematics kinematics,Chassis chassis) {
    this.kinematics = kinematics;
    this.chassis = chassis;
    demaciaTrajectory = new DemaciaTrajectory(pointList);
    // Use addRequirements() here to declare subsystem dependencies.
  }
  
  public void addPoint(){
    pointList.add(new Pose2d());

  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {} 

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    ChassisSpeeds needSpeed = demaciaTrajectory.calculateSpeeds(null, null);
    chassis.setVelocities(needSpeed);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
