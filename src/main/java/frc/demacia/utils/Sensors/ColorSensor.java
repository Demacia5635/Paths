package frc.demacia.utils.Sensors;
import frc.demacia.utils.Sensors.SensorInterface;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.util.Color;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import frc.demacia.utils.Log.LogManager;


public class ColorSensor implements SensorInterface {

    ColorSensorConfig config;
    String name;

    private final ColorSensorV3 sensor;
    private final ColorMatch matcher;

    private Color lastColor;
    private ColorMatchResult lastMatch;
    private int lastProximity;

    public ColorSensor(ColorSensorConfig config) {
        this.config = config;
        name = config.name;
        sensor = new ColorSensorV3(I2C.Port.kOnboard);
        matcher = new ColorMatch();
        addDefaultColors();
        updateReadings();
        addLog();

        LogManager.log(name + " color sensor initialized");
    }

    private void addDefaultColors() { //ערכי RGB
        matcher.addColorMatch(Color.kBlue);
        matcher.addColorMatch(Color.kRed);
        matcher.addColorMatch(Color.kGreen);
        matcher.addColorMatch(Color.kYellow);
    }

    private void updateReadings() {
        lastColor = sensor.getColor();
        lastMatch = matcher.matchClosestColor(lastColor);
        lastProximity = sensor.getProximity();
    }


    private void addLog() {
        LogManager.addEntry(name + " Color and Proximity", () -> new double[] {
            getRed(), getGreen(), getBlue(), getProximity()
        }, 3);
    }

    public String getName() {
        return config.name;
    }

    public double getRed() {
        updateReadings();
        return lastColor.red;
    }

    public double getGreen() {
        updateReadings();
        return lastColor.green;
    }

    public double getBlue() {
        updateReadings();
        return lastColor.blue;
    }

    public int getProximity() {
        updateReadings();
        return lastProximity;
    }

    // public String getMatchedColorName() {
    //     updateReadings();
    //     if (lastMatch.color == Color.kBlue) {
    //         return "Blue";
    //     } else if (lastMatch.color == Color.kRed) {
    //         return "Red";
    //     } else if (lastMatch.color == Color.kGreen) {
    //         return "Green";
    //     } else if (lastMatch.color == Color.kYellow) {
    //         return "Yellow";
    //     }
    //     return "Unknown";
    // }

    public Color getLastColor() {
        updateReadings();
        return lastColor;
    }
}


