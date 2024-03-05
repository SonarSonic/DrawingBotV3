package drawingbot.javafx.util;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.transform.Transform;
import javafx.stage.Window;

import java.awt.*;
import java.util.*;

/**
 * Handlers the registering / unregistering of multiple events to nodes
 * Useful for managing multiple events which need to be unregistered / registered together
 */
public class MultipleEventHandler {

    public Map<EventTarget, Map<EventType, Set<EventHandler>>> eventHandlerMap;
    public Map<EventTarget, Map<EventType, Set<EventHandler>>> eventFilterMap;

    public MultipleEventHandler() {
        super();
        this.eventHandlerMap = new HashMap<>();
        this.eventFilterMap = new HashMap<>();
    }

    public final <T extends Event> void addEventHandler(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventHandler) {
        addEventHandlerInternal(target, eventType, eventHandler, false);
    }

    public final <T extends Event> void removeEventHandler(EventTarget target, final EventType<T> eventType,final EventHandler<? super T> eventHandler) {
        removeEventHandlerInternal(target, eventType, eventHandler, false);
    }

    public final void removeEventHandlers(EventTarget target){
        removeEventHandlersInternal(target, false, true);
    }

    public final <T extends Event> void addEventFilter(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventHandler) {
        addEventHandlerInternal(target, eventType, eventHandler, true);
    }

    public final <T extends Event> void removeEventFilter(EventTarget target, final EventType<T> eventType,final EventHandler<? super T> eventHandler) {
        removeEventHandlerInternal(target, eventType, eventHandler, true);
    }

    public final void removeEventFilters(EventTarget target){
        removeEventHandlersInternal(target, true, true);
    }

    public final void removeAll(){
        Iterator<EventTarget> eventHandlerTargetIterator = eventHandlerMap.keySet().iterator();
        while (eventHandlerTargetIterator.hasNext()){
            EventTarget target = eventHandlerTargetIterator.next();
            removeEventHandlersInternal(target, false, false);
        }
        eventHandlerMap.clear();

        Iterator<EventTarget> eventFilterTargetIterator = eventFilterMap.keySet().iterator();
        while (eventFilterTargetIterator.hasNext()){
            EventTarget target = eventFilterTargetIterator.next();
            removeEventHandlersInternal(target, true, false);
        }
        eventFilterMap.clear();
    }

    private <T extends Event> void addEventHandlerInternal(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventHandler, boolean isFilter) {
        Map<EventTarget, Map<EventType, Set<EventHandler>>> handlerMap = isFilter ? eventFilterMap : eventHandlerMap;
        if(!handlerMap.containsKey(target)){
            handlerMap.put(target, new HashMap<>());
        }
        Map<EventType, Set<EventHandler>> map = handlerMap.get(target);
        if(!map.containsKey(eventType)){
            map.put(eventType, new HashSet<>());
        }
        if(map.get(eventType).add(eventHandler)){
            if(isFilter){
                addEventFilterToTarget(target, eventType, eventHandler);
            }else{
                addEventHandlerToTarget(target, eventType, eventHandler);
            }
        }
    }

    public final <T extends Event> void removeEventHandlerInternal(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventHandler, boolean isFilter) {
        Map<EventTarget, Map<EventType, Set<EventHandler>>> handlerMap = isFilter ? eventFilterMap : eventHandlerMap;
        if(!handlerMap.containsKey(target)){
            return;
        }
        Map<EventType, Set<EventHandler>> map = handlerMap.get(target);
        if(!map.containsKey(eventType)){
            return;
        }
        if(map.get(eventType).remove(eventHandler)){
            if(isFilter){
                removeEventFilterFromTarget(target, eventType, eventHandler);
            }else{
                removeEventHandlerFromTarget(target, eventType, eventHandler);
            }
            if(map.get(eventType).isEmpty()){
                map.remove(eventType);
            }
            if(map.isEmpty()){
                handlerMap.remove(target);
            }
        }
    }

    private void removeEventHandlersInternal(EventTarget target, boolean isFilter, boolean emptyBaseMap){
        Map<EventTarget, Map<EventType, Set<EventHandler>>> handlerMap = isFilter ? eventFilterMap : eventHandlerMap;

        Map<EventType, Set<EventHandler>> map = handlerMap.get(target);
        if(map == null || map.isEmpty()){
            return;
        }
        Iterator<EventType> typeIterator = map.keySet().iterator();
        while (typeIterator.hasNext()){
            EventType type = typeIterator.next();
            Set<EventHandler> set = map.get(type);
            for(EventHandler handler : set){
                if(isFilter){
                    removeEventFilterFromTarget(target, type, handler);
                }else{
                    removeEventHandlerFromTarget(target, type, handler);
                }
            }
            set.clear();
            typeIterator.remove();
        }

        if(emptyBaseMap){
            handlerMap.remove(target);
        }

    }

    private <T extends Event> void addEventFilterToTarget(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventFilter){
        if(target instanceof Node node){
            node.addEventFilter(eventType, eventFilter);
        }else if(target instanceof Transform transform){
            transform.addEventFilter(eventType, eventFilter);
        }else if(target instanceof Window window){
            window.addEventFilter(eventType, eventFilter);
        }else if(target instanceof Scene scene){
            scene.addEventFilter(eventType, eventFilter);
        }else if(target instanceof Task<?> task){
            task.addEventFilter(eventType, eventFilter);
        }else if(target instanceof Service<?> service){
            service.addEventFilter(eventType, eventFilter);
        }else{
            throw new UnsupportedOperationException("Unsupported Event Target: %s, Event Type: %s".formatted(target, eventType));
        }
    }

    private <T extends Event> void removeEventFilterFromTarget(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventFilter){
        if(target instanceof Node node){
            node.removeEventFilter(eventType, eventFilter);
        }else if(target instanceof Transform transform){
            transform.removeEventFilter(eventType, eventFilter);
        }else if(target instanceof Window window){
            window.removeEventFilter(eventType, eventFilter);
        }else if(target instanceof Scene scene){
            scene.removeEventFilter(eventType, eventFilter);
        }else if(target instanceof Task<?> task){
            task.removeEventFilter(eventType, eventFilter);
        }else if(target instanceof Service<?> service){
            service.removeEventFilter(eventType, eventFilter);
        }else{
            throw new UnsupportedOperationException("Unsupported Event Target: %s, Event Type: %s".formatted(target, eventType));
        }
    }

    private <T extends Event> void addEventHandlerToTarget(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventHandler){
        if(target instanceof Node node){
            node.addEventHandler(eventType, eventHandler);
        }else if(target instanceof Transform transform){
            transform.addEventHandler(eventType, eventHandler);
        }else if(target instanceof Window window){
            window.addEventHandler(eventType, eventHandler);
        }else if(target instanceof Scene scene){
            scene.addEventHandler(eventType, eventHandler);
        }else if(target instanceof Task<?> task){
            task.addEventHandler(eventType, eventHandler);
        }else if(target instanceof Service<?> service){
            service.addEventHandler(eventType, eventHandler);
        }else{
            throw new UnsupportedOperationException("Unsupported Event Target: %s, Event Type: %s".formatted(target, eventType));
        }
    }

    private <T extends Event> void removeEventHandlerFromTarget(EventTarget target, final EventType<T> eventType, final EventHandler<? super T> eventHandler){
        if(target instanceof Node node){
            node.removeEventHandler(eventType, eventHandler);
        }else if(target instanceof Transform transform){
            transform.removeEventHandler(eventType, eventHandler);
        }else if(target instanceof Window window){
            window.removeEventHandler(eventType, eventHandler);
        }else if(target instanceof Scene scene){
            scene.removeEventHandler(eventType, eventHandler);
        }else if(target instanceof Task<?> task){
            task.removeEventHandler(eventType, eventHandler);
        }else if(target instanceof Service<?> service){
            service.removeEventHandler(eventType, eventHandler);
        }else{
            throw new UnsupportedOperationException("Unsupported Event Target: %s, Event Type: %s".formatted(target, eventType));
        }
    }

}
