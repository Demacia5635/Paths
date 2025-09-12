package frc.demacia.utils;

import java.util.ArrayList;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;

public class Data<T> {
    
    @SuppressWarnings("rawtypes")
    private static ArrayList<Data> signals = new ArrayList<>();

    private StatusSignal<T>[] signal;
    private T[] lastValues;
    private int length;

    private boolean isDouble = false;
    private boolean isBoolean = false;
    private boolean isArray = false;

    @SuppressWarnings("unchecked")
    public Data(StatusSignal<T> ... signal){
        this.signal = signal;

        signals.add(this);
        
        length = signal.length;

        this.lastValues = (T[]) new Object[length];
        
        refresh();

        if (length > 1){
            isArray = true;
        }

        try {
            signal[0].getValueAsDouble();
            isDouble = true;
        } catch (Exception e) {
            if (signal[0].getValue() instanceof Boolean){
              isBoolean = true;
            }
        }
    }

    public Double getDouble() {
        if (!isDouble || length == 0) {return null;}
        refresh();
        return toDouble(lastValues[0]);
    }

    public double[] getDoubleArray() {
        if (!isDouble || length == 0) {return null;}
        refresh();
        if (isArray){
            return toDoubleArray(lastValues);
        } else{
            return new double[]{toDouble(lastValues[0])};
        }
    }

    public Boolean getBoolean() {
        if (!isBoolean || length == 0) {return null;}
        refresh();
        return (Boolean) lastValues[0];
    }

    public boolean[] getBooleanArray() {
        if (!isBoolean || length == 0) {return null;}
        refresh();
        if (isArray){
            return toBooleanArray(lastValues);
        } else{
            return new boolean[] {(Boolean)lastValues[0]};
        }
    }

    public String getString() {
        if (isDouble || isBoolean || length == 0) {return null;}
        refresh();
        return lastValues[0] != null ? lastValues[0].toString() : null;
    }

    public String[] getStringArray() {
        if (isDouble || isBoolean || length == 0) {return null;}
        refresh();
        if (isArray){
            return toStringArray(lastValues);
        } else{
            return new String[] {lastValues[0].toString()};
        }
    }

    public T getValue() {
        if (length == 0) {return null;}
        refresh();
        return lastValues[0];
    }

    public T[] getValueArray() {
        if (length == 0) {return null;}
        refresh();
        return lastValues;
    }

    public StatusSignal<T> getSignal() {
        return (signal != null && signal.length > 0) ? signal[0] : null;
    }

    public StatusSignal<T>[] getSignals() {
        return signal;
    }

    public void refresh(){
        StatusCode st = StatusSignal.refreshAll(signal);
        if(st == StatusCode.OK) {
            for (int i = 0; i < length; i++){
                lastValues[i] = signal[i].getValue();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static void refreshAll() {
        for(Data s : signals) {
            StatusCode st = StatusSignal.refreshAll(s.signal);
            if (st == StatusCode.OK) {
                for (int i = 0; i < s.signal.length; i++){
                    s.lastValues[i] = s.signal[i].getValue();
                }
            }
        }
    }

    private double[] toDoubleArray(T[] values){
        if (values == null) return null;

        double[] doubleArr = new double[length];
        for (int i = 0; i < length; i++) {
            T value = values[i];
            doubleArr[i] = (value != null) ? toDouble(value) : 0.0;
        }
        return doubleArr;
    }

    private boolean[] toBooleanArray(T[] values){
        if (values == null) return null;

        boolean[] booleanArr = new boolean[length];
        for (int i = 0; i < length; i++) {
            T value = values[i];
            booleanArr[i] = (value != null) ? (Boolean)value : false;
        }
        return booleanArr;
    }

    private String[] toStringArray(T[] values){
        if (values == null) return null;

        String[] stringArr = new String[length];
        for (int i = 0; i < length; i++) {
            T value = values[i];
            stringArr[i] = (value != null) ? value.toString() : null;
        }
        return stringArr;
    }

    private Double toDouble(T value){
        if (value == null) return null;

        return ((Number) value).doubleValue();
    }

    public void cleanup() {
        signals.remove(this);
    }
    
    public static void clearAllSignals() {
        signals.clear();
    }
}