package frc.robot.testSensors.UltraSonicSensor;

import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.Timer;

public class UltraSonicSensor extends Ultrasonic {

    private Thread backgroundThread;
    private boolean automaticEnabled = false;

    public UltraSonicSensor(int pingChannelPort, int echoChannelPort) {
        super(pingChannelPort, echoChannelPort);
    }

    @Override
    public void ping() {
        super.ping();
    }

    public double getRangeMeters() {
        return getRangeMM() / 100.0;
    }
}
