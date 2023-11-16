package ro.ubbcluj.map.socialnetworkgui.utils.observer;

import ro.ubbcluj.map.socialnetworkgui.utils.events.Event;

public interface Observable<E extends Event> {
    void addObserver(Observer<E> e);
    void removeObserver(Observer<E> e);
    void notifyObservers(E t);
}
