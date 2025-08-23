package frc.demacia.utils.Sensors;

public  interface Sensor{
    String name();

    double get();

    void reset();

    boolean isConnected();
}