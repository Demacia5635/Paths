package frc.robot.testSensors.UltraSonicSensor;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;

public class UltraSonicSensor extends Ultrasonic {
    // === שדות (משתנים פנימיים) ===
    private DigitalInput echoChannel;   // קולט את ההחזרה (Echo)
    private DigitalOutput pingChannel;  // שולח את הפולס (Ping)
    private Counter counter;            // מודד כמה זמן לקח לפולס לחזור

    // קבועים לחישוב
    private static final double kPingTime = 10e-6; // 10 מיקרו־שניות פולס
    private static final double kSpeedOfSoundMetersPerSec = 343.0; // מהירות קול במטרים/שניה

    // automatic mode
    private boolean automaticEnabled = false;
    private Thread backgroundThread;

    // === Constructor ===
    public UltraSonicSensor(int pingChannelPort, int echoChannelPort) {
        this.pingChannel = new DigitalOutput(pingChannelPort);
        this.echoChannel = new DigitalInput(echoChannelPort);

        this.counter = new Counter(this.echoChannel);
        this.counter.setMaxPeriod(1.0);
        this.counter.setSemiPeriodMode(true);
        this.counter.reset();
    }

    // === שליחת Ping יחיד ===
    public void ping() {
        counter.reset();
        pingChannel.pulse(kPingTime);
    }

    // === בדיקה אם התקבלה מדידה תקינה ===
    private boolean isRangeValid() {
        return counter.get() > 1;
    }

    // === החזרת טווח במטרים ===
    public double getRangeMeters() {
        if (isRangeValid()) {
            // זמן מדידה * מהירות הקול / 2 (הלוך וחזור)
            return counter.getPeriod() * kSpeedOfSoundMetersPerSec / 2.0;
        }
        return 0;
    }

    // === מצב אוטומטי – Thread שרץ ברקע ===
    public void setAutomaticMode(boolean enable) {
        automaticEnabled = enable;

        if (enable) {
            backgroundThread = new Thread(() -> {
                while (automaticEnabled) {
                    ping();
                    Timer.delay(0.1);
                }
            });
            backgroundThread.start();
        } else {
            if (backgroundThread != null) {
                try {
                    backgroundThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
