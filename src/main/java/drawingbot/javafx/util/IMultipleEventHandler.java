package drawingbot.javafx.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * Convenience interface to add clear methods that link back to the implementers {@link MultipleEventHandler}
 */
public interface IMultipleEventHandler {

    MultipleEventHandler getMultipleEventHandler();

    default <T extends Event> void addEventHandler(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventHandler) {
        getMultipleEventHandler().addEventHandler(target, eventType, eventHandler);
    }

    default <T extends Event> void removeEventHandler(EventTarget target, final EventType<T> eventType,final EventHandler<? super T> eventHandler) {
        getMultipleEventHandler().removeEventHandlerInternal(target, eventType, eventHandler, false);
    }

    default void removeEventHandlers(EventTarget target){
        getMultipleEventHandler().removeEventHandlers(target);
    }

    default <T extends Event> void addEventFilter(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventHandler) {
        getMultipleEventHandler().addEventFilter(target, eventType, eventHandler);
    }

    default <T extends Event> void removeEventFilter(EventTarget target, final EventType<T> eventType,final EventHandler<? super T> eventHandler) {
        getMultipleEventHandler().removeEventFilter(target, eventType, eventHandler);
    }

    default void removeEventFilters(EventTarget target){
        getMultipleEventHandler().removeEventFilters(target);
    }
}
