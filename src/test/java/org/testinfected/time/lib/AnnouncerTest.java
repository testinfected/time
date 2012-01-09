package org.testinfected.time.lib;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testinfected.time.lib.Announcer;

@RunWith(JMock.class)
public class AnnouncerTest {

    Mockery context = new JUnit4Mockery();
    Announcer<Listener> announcer = Announcer.to(Listener.class);
    Object anEvent = new Object();

    int listenerCount = 3;
    Listener undecided = context.mock(Listener.class, "undecided");

    @Test public void
    announcesToAllSubscribedListeners() {
        listenersHaveSubscribed(listenerCount);
        announcer.announce().eventOccurred(anEvent);
    }

    @Test public void
    stopsAnnouncingToUnsubscribedListeners() {
        announcer.subscribe(undecided);
        listenersHaveSubscribed(listenerCount);
        announcer.unsubscribe(undecided);

        expectsNoNotification(undecided);
        announcer.announce().eventOccurred(anEvent);
    }

    private void listenersHaveSubscribed(int listenerCount) {
        for (int i = 0; i < listenerCount; i++) {
            final Listener listener = context.mock(Listener.class, "listener" + i);
            announcer.subscribe(listener);
            expectsToBeNotified(listener);
        }
    }

    private void expectsToBeNotified(final Listener listener) {
        context.checking(new Expectations() {{
            oneOf(listener).eventOccurred(anEvent);
        }});
    }

    private void expectsNoNotification(final Listener listener) {
        context.checking(new Expectations() {{
            never(listener).eventOccurred(anEvent);
        }});
    }

    public interface Listener {
        public void eventOccurred(Object event);
    }
}
