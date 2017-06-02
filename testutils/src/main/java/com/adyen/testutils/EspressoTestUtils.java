package com.adyen.testutils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;

import android.support.test.espresso.core.deps.guava.base.Throwables;
import android.support.test.espresso.core.deps.guava.collect.Sets;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;

/**
 *
 * Test utility methods.
 */
public final class EspressoTestUtils {

    private static final int NUMBER_OF_RETRIES = 100;
    private static final long DEFAULT_TIMEOUT = 15000;
    private static final long DEFAULT_INTERVAL = 250;

    /**
     * Toggles the screen orientation.
     * This method finds the current foreground activity and rotates the screen on this activity.
     *
     * This method waits max 2 seconds for device to rotate the screen. Then it will throw an exception.
     *
     * @throws TimeoutException if the screen couldn't be rotated within 2 seconds.
     * @throws InterruptedException when thread is interrupted unexpectedly.
     */
    public static void rotateScreen() throws TimeoutException, InterruptedException {
        Context context = InstrumentationRegistry.getTargetContext();
        final int initialOrientation = context.getResources().getConfiguration().orientation;
        Activity activity = getActivityInstance();
        activity.setRequestedOrientation(
                (initialOrientation == Configuration.ORIENTATION_PORTRAIT)
                        ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        int waitCounter = 0;
        while (context.getResources().getConfiguration().orientation == initialOrientation) {
            Thread.sleep(200);
            waitCounter++;
            if (waitCounter > 10) {
                throw new TimeoutException("Screen could not be rotated within a certain amount of time");
            }
        }
    }

    /**
     * Blocks until the view with indicated ID is found or timeout.
     *
     * Default timeout is {@link #DEFAULT_TIMEOUT}. It checks every {@link #DEFAULT_INTERVAL}
     *
     * @param viewId ID of the view that is requested
     * @throws InterruptedException when thread is interrupted unexpectedly.
     * @throws TimeoutException if the view could not be found within timeout.
     */
    public static void waitForView(final int viewId) throws InterruptedException, TimeoutException {
        waitForView(viewId, DEFAULT_INTERVAL, DEFAULT_TIMEOUT);
    }

    public static void waitForText(final String text) throws InterruptedException, TimeoutException {
        waitForText(text, DEFAULT_INTERVAL, DEFAULT_TIMEOUT);
    }

    public static void closeAllActivities(Instrumentation instrumentation) throws Exception {
        int i = 0;
        while (closeActivity(instrumentation)) {
            if (i++ > NUMBER_OF_RETRIES) {
                throw new AssertionError("Limit of retries excesses");
            }
            Thread.sleep(200);
        }
    }

    private static void waitForView(final int viewId, final long sleepInterval, final long timeout)
            throws InterruptedException, TimeoutException {
        final long currentTime = System.currentTimeMillis();
        final long timeoutTime = currentTime + timeout;
        while (System.currentTimeMillis() < timeoutTime) {
            final Activity activity = EspressoTestUtils.getActivityInstance();
            if ((activity != null) && (activity.findViewById(viewId) != null)) {
                return;
            }
            Thread.sleep(sleepInterval);
        }
        throw new TimeoutException("View with ID " + viewId + " could not be found.");
    }

    private static void waitForText(final String text, final long sleepInterval, final long timeout)
            throws InterruptedException, TimeoutException {
        final long currentTime = System.currentTimeMillis();
        final long timeoutTime = currentTime + timeout;
        while (System.currentTimeMillis() < timeoutTime) {
            try {
                onView(withText(text)).check(matches(isDisplayed()));
                return;
            } catch (final NoMatchingViewException exception) {
                // Don't do anything. Keep waiting till timeout.
            }
            Thread.sleep(sleepInterval);
        }
        throw new TimeoutException("View with text " + text + " could not be found.");
    }

    private static Activity getActivityInstance() {
        final Activity[] currentActivity = {null};
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    currentActivity[0] = (Activity) resumedActivities.iterator().next();
                }
            }
        });

        return currentActivity[0];
    }

    private static <X> X callOnMainSync(Instrumentation instrumentation, final Callable<X> callable)
            throws Exception {
        final AtomicReference<X> retAtomic = new AtomicReference<>();
        final AtomicReference<Throwable> exceptionAtomic = new AtomicReference<>();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                try {
                    retAtomic.set(callable.call());
                } catch (Throwable e) {
                    exceptionAtomic.set(e);
                }
            }
        });
        final Throwable exception = exceptionAtomic.get();
        if (exception != null) {
            Throwables.propagateIfInstanceOf(exception, Exception.class);
            Throwables.propagate(exception);
        }
        return retAtomic.get();
    }

    private static Set<Activity> getActivitiesInStages(Stage... stages) {
        final Set<Activity> activities = Sets.newHashSet();
        final ActivityLifecycleMonitor instance = ActivityLifecycleMonitorRegistry.getInstance();
        for (Stage stage : stages) {
            final Collection<Activity> activitiesInStage = instance.getActivitiesInStage(stage);
            if (activitiesInStage != null) {
                activities.addAll(activitiesInStage);
            }
        }
        return activities;
    }

    private static boolean closeActivity(Instrumentation instrumentation) throws Exception {
        final Boolean activityClosed = callOnMainSync(instrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final Set<Activity> activities = getActivitiesInStages(Stage.RESUMED,
                        Stage.STARTED, Stage.PAUSED, Stage.STOPPED, Stage.CREATED);
                activities.removeAll(getActivitiesInStages(Stage.DESTROYED));
                if (activities.size() > 0) {
                    final Activity activity = activities.iterator().next();
                    activity.finish();
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (activityClosed) {
            instrumentation.waitForIdleSync();
        }
        return activityClosed;
    }

    private EspressoTestUtils() {
        // Private Constructor
    }
}
