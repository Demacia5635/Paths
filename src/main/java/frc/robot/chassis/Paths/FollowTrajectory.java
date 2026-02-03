// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class FollowTrajectory extends Command {

  Consumer<ChassisSpeeds> setSpeeds;
  Supplier<Pose2d> currentPose;
  Supplier<ChassisSpeeds> currentSpeeds;
  DemaciaTrajectory trajectory;

  public FollowTrajectory(ArrayList<Pose2d> trajectoryPoints, Consumer<ChassisSpeeds> setSpeeds,
      Supplier<Pose2d> chassisPose, Supplier<ChassisSpeeds> currentSpeeds) {
    this(setSpeeds, chassisPose, currentSpeeds, new DemaciaTrajectory(trajectoryPoints));
  }

  public FollowTrajectory(Consumer<ChassisSpeeds> setSpeeds, Supplier<Pose2d> chassisPose,
      Supplier<ChassisSpeeds> currentSpeeds, DemaciaTrajectory trajectory) {
    this.currentPose = chassisPose;
    this.setSpeeds = setSpeeds;
    this.currentPose = chassisPose;
    this.trajectory = trajectory;
  }

  @Override
  public void execute() {

    setSpeeds.accept(trajectory.calculateSpeeds(currentSpeeds.get(), currentPose.get()));
    
  }

  @Override
  public void end(boolean interrupted) {
    setSpeeds.accept(new ChassisSpeeds());
  }

  @Override
  public boolean isFinished() {
    return trajectory.isFinishedTrajectory();
  }
}
