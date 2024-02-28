package drawingbot.javafx.util;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;
import javafx.util.converter.*;
import org.fxmisc.easybind.Subscription;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

public class JFXUtils {

    public static void runNowOrLater(Runnable runnable){
        if(Platform.isFxApplicationThread()){
            runnable.run();
        }else{
            Platform.runLater(runnable);
        }
    }

    public static void runNow(Runnable runnable) {
        if(Platform.isFxApplicationThread()){
            runnable.run();
        }else{
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                runnable.run();
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    public static <V> V runTaskNow(FutureTask<V> task){
        runNowOrLater(task);
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Subscription subscribeListener(ObservableValue<T> observable, ChangeListener<? super T> subscriber) {
        subscriber.changed(observable, null, observable.getValue());
        observable.addListener(subscriber);
        return () -> observable.removeListener(subscriber);
    }

    public static <VALUE, ROOT> void createBiDirectionalSelectBinding(Property<VALUE> property, Property<ROOT> selectRoot, Function<ROOT, Property<VALUE>> select){

        if(selectRoot.getValue() != null){
            Property<VALUE> target = select.apply(selectRoot.getValue());
            if(target != null){
                property.bindBidirectional(target);
            }
        }

        selectRoot.addListener((observable, oldValue, newValue) -> {
            if(oldValue != null){
                Property<VALUE> oldTarget = select.apply(oldValue);
                if(oldTarget != null){
                    property.unbindBidirectional(oldTarget);
                }
            }
            if(newValue != null){
                Property<VALUE> newTarget = select.apply(newValue);
                if(newTarget != null){
                    property.bindBidirectional(newTarget);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> StringConverter<T> getStringConverter(Class<T> type){
        if(type == Boolean.class){
            return (StringConverter<T>) new BooleanStringConverter();
        }
        if(Number.class.isAssignableFrom(type)){
            Class<Number> numberClass = (Class<Number>) type;
            return (StringConverter<T>) getNumberStringConverter(numberClass);
        }
        if(type == Character.class){
            return (StringConverter<T>) new CharacterStringConverter();
        }
        if(Date.class.isAssignableFrom(type)){
            return (StringConverter<T>) new DateTimeStringConverter();
        }
        if(LocalTime.class.isAssignableFrom(type)){
            return (StringConverter<T>) new LocalTimeStringConverter();
        }
        if(LocalDateTime.class.isAssignableFrom(type)){
            return (StringConverter<T>) new LocalTimeStringConverter();
        }
        return (StringConverter<T>) new DefaultStringConverter();
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number> StringConverter<N> getNumberStringConverter(Class<N> type){
        if (type == Byte.class) {
            return (StringConverter<N>) new ByteStringConverter();
        } else if (type == Short.class) {
            return (StringConverter<N>) new ShortStringConverter();
        } else if (type == Integer.class) {
            return (StringConverter<N>) new IntegerStringConverter();
        } else if (type == Long.class) {
            return (StringConverter<N>) new LongStringConverter();
        } else if (type == Float.class) {
            return (StringConverter<N>) new FloatStringConverter();
        } else if (type == Double.class) {
            return (StringConverter<N>) new DoubleStringConverter();
        } else if (type == BigInteger.class) {
            return (StringConverter<N>) new BigIntegerStringConverter();
        } else if (type == BigDecimal.class) {
            return (StringConverter<N>) new BigDecimalStringConverter();
        }
        return (StringConverter<N>) new NumberStringConverter();
    }

}
