package frc.demacia.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;

public class Data<T> {
    private static BaseStatusSignal[] signals = new BaseStatusSignal[0];
    private static final ArrayList<WeakReference<Data<?>>> signalInstances = new ArrayList<>();
    private static final ArrayList<WeakReference<Data<?>>> supplierInstances = new ArrayList<>();

    private StatusSignal<T>[] signal;
    private Supplier<T>[] supplier;
    private int length;

    private boolean isDouble = false;
    private boolean isBoolean = false;
    private boolean isArray = false;

    private boolean changed = true;

    private T[] values;
    private double[] doubleArrayValues;
    private float[] floatArrayValues;
    private boolean[] booleanArrayValues;
    private String[] stringArrayValues;

    @SuppressWarnings("unchecked")
    public Data(StatusSignal<T>... signal){
        this.signal = signal;
        length = signal.length;

        detectTypeFromSignal();
        allocateCachedArrays();

        registerSignal();
        refresh();
    }
    
    @SuppressWarnings("unchecked")
    public Data(Supplier<T>... supplier){
        this.supplier = supplier;
        length = supplier.length;

        detectTypeFromSupplier();
        allocateCachedArrays();

        registerSupplier();
        refresh();
    }

    private void registerSignal() {
        registerSignal(signal);
        synchronized (signalInstances) {
            signalInstances.add(new WeakReference<>(this));
        }
    }

    private void registerSignal(BaseStatusSignal[] newSignals) {
        int oldLength = signals.length;
        int newLength = oldLength + newSignals.length;
        BaseStatusSignal[] combined = new BaseStatusSignal[newLength];
        System.arraycopy(signals, 0, combined, 0, oldLength);
        System.arraycopy(newSignals, 0, combined, oldLength, newSignals.length);
        signals = combined;
    }

    private void registerSupplier() {
        synchronized (supplierInstances) {
            supplierInstances.add(new WeakReference<>(this));
        }
    }

    private void detectTypeFromSignal() {
        if (length == 0) return;
        
        if (length > 1) isArray = true;

        T value = signal[0].getValue();
        
        if (value instanceof Number) {
            isDouble = true;
        }
        else if (value instanceof Boolean) {
            isBoolean = true;
        }
        else {
            try {
                signal[0].getValueAsDouble();
                isDouble = true;
            } catch (Exception e) {
                isDouble = false;
            }
        }
    }

    private void detectTypeFromSupplier() {
        if (length == 0) return;

        if (length > 1) isArray = true;

        T value = supplier[0].get();

        if (value instanceof Number) {
            isDouble = true;
        }
        else if (value instanceof Boolean) {
            isBoolean = true;
        }
    }

    @SuppressWarnings("unchecked")
    private void allocateCachedArrays() {
        if (length > 0) {
            values = (T[]) new Object[length];
            if (isDouble) {
                doubleArrayValues = new double[length];
                floatArrayValues = new float[length];
            } else if (isBoolean) {
                booleanArrayValues = new boolean[length];
            } else {
                stringArrayValues = new String[length];
            }
        }
    }
    
    public void refresh() {
        if (signal != null) {
            StatusSignal.refreshAll(signal);
            updateSignalValue();
        } else {
            refreshSupplier();
        }
    }

    private void updateSignalValue() {
        changed = false;
        if (values == null) return; 
        for (int i = 0; i < length; i++) {
            T val = signal[i].getValue();
            values[i] = val;
            if (isDouble) {
                double newVal = signal[i].getValueAsDouble();
                if (doubleArrayValues[i] != newVal) {
                    changed = true;
                    doubleArrayValues[i] = newVal;
                }
                float newFloatVal = (float) newVal;
                if (floatArrayValues[i] != newFloatVal) {
                    changed = true;
                    floatArrayValues[i] = newFloatVal;
                }
            } else if (isBoolean) {
                boolean newVal = (Boolean) val;
                if (booleanArrayValues[i] != newVal) {
                    changed = true;
                    booleanArrayValues[i] = newVal;
                }
            } else{
                String newVal = (val == null) ? "null" : val.toString();
                if (!Objects.equals(stringArrayValues[i], newVal)) {
                    changed = true;
                    stringArrayValues[i] = newVal;
                }
            }
        }
    }

    private void refreshSupplier() {
        changed = false;
        if (values == null) return; 
        for (int i = 0; i < length; i++) {
            T val = supplier[i].get();
            values[i] = val;
            if (isDouble) {
                double newVal = ((Number) val).doubleValue();
                if (doubleArrayValues[i] != newVal) {
                    changed = true;
                    doubleArrayValues[i] = newVal;
                }
                float newFloatVal = ((Number) val).floatValue();
                if (floatArrayValues[i] != newFloatVal) {
                    changed = true;
                    floatArrayValues[i] = newFloatVal;
                }
            } else if (isBoolean) {
                boolean newVal = (Boolean) val;
                if (booleanArrayValues[i] != newVal) {
                    changed = true;
                    booleanArrayValues[i] = newVal;
                }
            } else{
                String newVal = (val == null) ? "null" : val.toString();
                if (!Objects.equals(stringArrayValues[i], newVal)) {
                    changed = true;
                    stringArrayValues[i] = newVal;
                }
            }
    }
    }

    public static void refreshAll() {
        if (signals.length > 0) {
            BaseStatusSignal.refreshAll(signals);
        }

        synchronized (signalInstances) {
            Iterator<WeakReference<Data<?>>> iterator = signalInstances.iterator();
            while (iterator.hasNext()) {
                Data<?> data = iterator.next().get();
                if (data == null) iterator.remove();
                else data.updateSignalValue();
            }
        }

        synchronized (supplierInstances) {
            Iterator<WeakReference<Data<?>>> iterator = supplierInstances.iterator();
            while (iterator.hasNext()) {
                Data<?> data = iterator.next().get();
                if (data == null) iterator.remove();
                else data.refreshSupplier();
            }
        }
    }

    public boolean hasChanged() { return changed; }

    public T getValue() {
        return values[0];
    }

    public T[] getValueArray() {
        return values;
    }

    public double getDouble() {
        return length == 0 ? 0
        : isDouble? doubleArrayValues[0]
        : isBoolean? booleanArrayValues[0] ? 1.0 : 0.0
        : 0;
    }

    public double[] getDoubleArray() {
        if (length == 0) { return new double[0]; }
        else if (isDouble) { return doubleArrayValues; }
        else if (isBoolean) {
            double[] doubles = new double[length];
            for (int i = 0; i < length; i++) {
                doubles[i] = booleanArrayValues[i] ? 1.0 : 0.0;
            }
            return doubles;
        }
        else { return new double[0]; }
    }

    public float getFloat() {
        return length == 0 ? 0f
        : isDouble? floatArrayValues[0]
        : isBoolean? booleanArrayValues[0] ? 1f : 0f
        : 0f;
    }

    public float[] getFloatArray() {
        if (length == 0) { return new float[0]; }
        else if (isDouble) { return floatArrayValues; }
        else if (isBoolean) {
            float[] floats = new float[length];
            for (int i = 0; i < length; i++) {
                floats[i] = booleanArrayValues[i] ? 1f : 0f;
            }
            return floats;
        }
        else { return new float[0]; }
    }

    public boolean getBoolean() {
        return length == 0 ? false
        : isBoolean? booleanArrayValues[0]
        : isDouble ? doubleArrayValues[0] == 1
        : false;
    }

    public boolean[] getBooleanArray() {
        if (length == 0) { return new boolean[0]; }
        else if (isBoolean) { return booleanArrayValues; }
        else if (isDouble) {
            boolean[] bools = new boolean[length];
            for (int i = 0; i < length; i++) {
                bools[i] = (doubleArrayValues[i] == 1);
            }
            return bools;
        }
        else { return new boolean[0]; }
    }

    public String getString() {
        return length == 0 ? ""
        : isDouble ? ((Double) doubleArrayValues[0]).toString()
        : isBoolean? ((Boolean) booleanArrayValues[0]).toString()
        : stringArrayValues[0];
    }

    public String[] getStringArray() {
        if (length == 0) { return new String[0]; }
        else if (isDouble) {
            String[] strs = new String[length];
            for (int i = 0; i < length; i++) {
                strs[i] = ((Double) doubleArrayValues[i]).toString();
            }
            return strs;
        }
        else if (isBoolean) {
            String[] strs = new String[length];
            for (int i = 0; i < length; i++) {
                strs[i] = ((Boolean) booleanArrayValues[i]).toString();
            }
            return strs;
        }
        else { return stringArrayValues; }
    }
    
    public StatusSignal<T> getSignal() {
        return (signal != null && signal.length > 0) ? signal[0]
        : null;
    }

    public StatusSignal<T>[] getSignalArray() {
        return (signal != null) ? signal
        : null;
    }

    public Supplier<T> getSupplier() {
        return (supplier != null && supplier.length > 0) ? supplier[0]
        : null;
    }

    public Supplier<T>[] getSupplierArray() {
        return (supplier != null) ? supplier
        : null;
    }

    public long getTime() {
        if (signal != null) return (long) (signal[0].getTimestamp().getTime() * 1000);
        return 0;
    }

    public boolean isDouble() { return isDouble; }
    public boolean isBoolean() { return isBoolean; }
    public boolean isArray() { return isArray; }

    public void cleanup() {
        if (signal != null) {
            int count = 0;
            for (BaseStatusSignal s : signals) {
                boolean isMine = false;
                for (StatusSignal<T> mySignal : signal) {
                    if (s == mySignal) {
                        isMine = true;
                        break;
                    }
                }
                if (!isMine) count++;
            }

            BaseStatusSignal[] newSignalsArray = new BaseStatusSignal[count];
            int index = 0;
            
            for (BaseStatusSignal s : signals) {
                boolean isMine = false;
                for (StatusSignal<T> mySignal : signal) {
                    if (s == mySignal) {
                        isMine = true;
                        break;
                    }
                }
                if (!isMine) {
                    newSignalsArray[index++] = s;
                }
            }
            
            signals = newSignalsArray;
        }
        
        signal = null; 
        supplier = null; 
        values = null;
        doubleArrayValues = null; 
        floatArrayValues = null; 
        booleanArrayValues = null; 
        stringArrayValues = null;
    }

    public static void clearAllSignals() {
        signals = new BaseStatusSignal[0];
        signalInstances.clear();
        supplierInstances.clear();
    }

    @SuppressWarnings("unchecked")
    public void expandWithSignals(StatusSignal<T>[] newSignals) {
        if (newSignals == null || newSignals.length == 0) return;

        int newLength = length + newSignals.length;
        StatusSignal<T>[] expandedSignals = new StatusSignal[newLength];
        if (signal != null) {
            System.arraycopy(signal, 0, expandedSignals, 0, length);
        }
        System.arraycopy(newSignals, 0, expandedSignals, length, newSignals.length);
        signal = expandedSignals;

        length = signal.length;
        
        if (length > 1) isArray = true;

        detectTypeFromSignal();
        allocateCachedArrays();

        registerSignal(newSignals);
        refresh();
    }

    @SuppressWarnings("unchecked")
    public void expandWithSuppliers(Supplier<T>[] newSuppliers) {

        if (newSuppliers == null || newSuppliers.length == 0) return;
        int newLength = length + newSuppliers.length;
        Supplier<T>[] expandedSuppliers = new Supplier[newLength];
        if (supplier != null) {
            System.arraycopy(supplier, 0, expandedSuppliers, 0, length);
        }
        System.arraycopy(newSuppliers, 0, expandedSuppliers, length, newSuppliers.length);
        supplier = expandedSuppliers;

        length = expandedSuppliers.length;

        if (length > 1) isArray = true;

        detectTypeFromSupplier();
        allocateCachedArrays();

        refresh();
    }
}