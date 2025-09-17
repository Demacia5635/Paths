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
        if (signal != null){
            return (double)signal[0].getValueAsDouble();
        }
        else {
            return toDouble(lastValues[0]);
        }
    }

    public double[] getDoubleArray() {
        if (!isDouble || length == 0) {return null;}
        refresh();
        if (signal != null){
            double[] doubleArray = new double[signal.length];
                for (int i = 0; i < signal.length; i++) {
                    doubleArray[i] = (double)signal[i].getValueAsDouble();
                }
                return doubleArray;
        }
        else {
            return toDoubleArray(lastValues);
        }
    }

    public Float getFloat() {
        if (!isDouble || length == 0) {return null;}
        refresh();
        if (signal != null){
            return (float) signal[0].getValueAsDouble();
        }
        else {
            return toDouble(lastValues[0]).floatValue();
        }
    }

    public float[] getFloatArray() {
        if (!isDouble || length == 0) {return null;}
        refresh();
        if (signal != null){
            float[] floatArray = new float[signal.length];
                for (int i = 0; i < signal.length; i++) {
                    floatArray[i] = (float)signal[i].getValueAsDouble();
                }
                return floatArray;
        }
        else {
            return toFloatArray(lastValues);
        }
    }

    public Boolean getBoolean() {
        if (!isBoolean || length == 0) {return null;}
        refresh();
        if (signal != null){
            return (Boolean) signal[0].getValue();
        }
        else {
            return (Boolean) lastValues[0];
        }
    }

    public boolean[] getBooleanArray() {
        if (!isBoolean || length == 0) {return null;}
        refresh();
        if (signal != null){
            boolean[] booleanArray = new boolean[signal.length];
                for (int i = 0; i < signal.length; i++) {
                    booleanArray[i] = (Boolean)signal[i].getValue();
                }
                return booleanArray;
        }
        else {
            return toBooleanArray(lastValues);
        }
    }

    public String getString() {
        if (isDouble || isBoolean || length == 0) {return "";}
        refresh();
        if (signal != null){
            return signal[0].getValue().toString();
        }
        else {
            return lastValues[0] != null ? lastValues[0].toString() : "";
        }
    }

    public String[] getStringArray() {
        if (isDouble || isBoolean || length == 0) {return null;}
        refresh();
        if (signal != null){
            String[] stringArray = new String[signal.length];
                for (int i = 0; i < signal.length; i++) {
                    stringArray[i] = signal[i].getValue().toString();
                }
                return stringArray;
        }
        else {
            return toStringArray(lastValues);
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

    public boolean hasChanged() {
        return lastValues == getValueArray();
    }

    // public boolean hasChanged() {
    //     if (lastValues == null || lastValues[0] == null) {
    //         return true;
    //     }
    
    //     refresh();
    //     T[] currentValues = lastValues;
    
    //     if (isArray){
    //         if (isDouble){
    //             double[] newArr = toDoubleArray(currentValues);
    //             double[] lastArr = toDoubleArray(lastValues);
    //             if (newArr.length != lastArr.length) {
    //                 return true;
    //             }
    //             for (int i = 0; i < newArr.length; i++) {
    //                 if (Math.abs(newArr[i] - lastArr[i]) >= 0.001) {
    //                     return true;
    //                 }
    //             }
    //             return false;
    //         } else if (isBoolean){
    //             boolean[] newArr = toBooleanArray(currentValues);
    //             boolean[] lastArr = toBooleanArray(lastValues);
    //             if (newArr.length != lastArr.length) {
    //                 return true;
    //             }
    //             for (int i = 0; i < newArr.length; i++) {
    //                 if (newArr[i] != lastArr[i]) {
    //                     return true;
    //                 }
    //             }
    //             return false;
    //         } else{
    //             String[] newArr = toStringArray(currentValues);
    //             String[] lastArr = toStringArray(lastValues);
    //             if (newArr.length != lastArr.length) {
    //                 return true;
    //             }
    //             for (int i = 0; i < newArr.length; i++) {
    //                 if (!(newArr[i].toString()).equals((lastArr[i].toString()))) {
    //                     return true;
    //                 }
    //             }
    //             return false;
    //         }
    //     } else{
    //         if (isDouble){
    //             return Math.abs(toDouble(currentValues[0]) - toDouble(lastValues[0])) >= 0.001;
    //         } else if (isBoolean){
    //             return !currentValues[0].equals(lastValues[0]);
    //         } else{
    //             return !(currentValues[0].toString()).equals((lastValues[0].toString()));
    //         }
    //     }
    // }

    public long getTime() {
        if (signal != null) {
            return (long) (signal[0].getTimestamp().getTime() * 1000);
        }
        return 0;
    }

    public boolean isDouble(){
        return isDouble;
    }

    public boolean isBoolean(){
        return isBoolean;
    }

    public boolean isArray(){
        return isArray;
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

    private float[] toFloatArray(T[] values){
        int length = java.lang.reflect.Array.getLength(values);
        float[] floatArr = new float[length];
            for (int i = 0; i < length; i++) {
                Object elem = java.lang.reflect.Array.get(values, i);
                floatArr[i] = (elem != null) ? ((Number) elem).floatValue() : 0f;
            }
            return floatArr;
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