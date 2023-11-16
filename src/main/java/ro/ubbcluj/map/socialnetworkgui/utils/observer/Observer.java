package ro.ubbcluj.map.socialnetworkgui.utils.observer;

import ro.ubbcluj.map.socialnetworkgui.utils.events.Event;

public interface Observer<E extends Event> {
    void update(E e);
}
