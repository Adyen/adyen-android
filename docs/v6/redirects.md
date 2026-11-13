# Redirects

Some payment methods and 3D Secure flows send the shopper away from your app, for example to their bank's website or app. When the shopper finishes, they are sent back to the `returnUrl` you passed to Checkout API, and your app must hand that return to the SDK so the payment can continue.

This guide covers how to receive the return for standalone components and for Drop-in. For shared v6 concepts, see [README.md](README.md).

## Choose a return URL

You set `returnUrl` yourself: in the `/sessions` request for the session flow, or in the `/payments` request for the advanced flow. The SDK has no default return URL in v6. Whatever URL you choose must match an intent filter in your app.

- **Custom scheme**, for example `myapp://checkout/dropin`. Works on every device without extra setup. Any other app can declare the same scheme, though, and might receive the return instead of yours.
- **`https` App Link**, for example `https://www.example.com/checkout/dropin`, with `android:autoVerify="true"` and a Digital Asset Links file on your domain. Only your app can open a verified link, so we recommend this for production.

If several screens in your app handle redirects, give each its own URL, for example a different path. Android otherwise shows the shopper a chooser between them.

## Standalone components

Declare the intent filter on the activity that hosts your `CheckoutController`, and make that activity `singleTask`. When the return arrives, Android then brings back the existing activity and closes the browser tab that was opened above it.

```xml
<activity
    android:name=".CheckoutActivity"
    android:exported="true"
    android:launchMode="singleTask">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:host="checkout"
            android:path="/card"
            android:scheme="myapp" />
    </intent-filter>
</activity>
```

Pass the return to the controller from `onNewIntent`:

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (intent.data?.toString()?.startsWith(returnUrl) == true) {
        controller.handleReturn(intent)
    }
}
```

With other launch modes, the return can open a new instance of your activity, which doesn't have your controller, or the browser tab can stay on top of your activity until the shopper closes it.

Unlike Drop-in, standalone components need no [task affinity](#task-affinity) changes. If your app sets `<application android:taskAffinity="">`, the `singleTask` activity starts in a task of its own, and the return still finds it there.

## Drop-in

Drop-in receives returns through `com.adyen.checkout.dropin.DropInRedirectHandlingActivity`. Drop-in declares it without an intent filter and does not export it, so nothing can open it until you declare it yourself.

### Declare the redirect handling activity

Add the activity to your manifest with `android:exported="true"` and an intent filter that matches the `returnUrl` you send for Drop-in payments:

```xml
<activity
    android:name="com.adyen.checkout.dropin.DropInRedirectHandlingActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:host="checkout"
            android:path="/dropin"
            android:scheme="myapp" />
    </intent-filter>
</activity>
```

You can declare several intent filters on it, for example a custom scheme and an `https` App Link. The activity only accepts `ACTION_VIEW` intents that carry a URL. It passes on the action and URL alone, never extras or flags from the incoming intent, and closes immediately. It does not appear in Recents or the back stack.

### Turn redirect handling on and off

Use `DropIn.setRedirectHandlingEnabled(context, enabled)` to turn the redirect handling activity on or off at runtime. This is for apps where Drop-in and a standalone components screen share one return URL, for example in cross-platform plugins. Turn redirect handling on before starting Drop-in and off before starting the components screen, so each return reaches the right screen without the shopper seeing a chooser:

```kotlin
DropIn.setRedirectHandlingEnabled(context, enabled = false)
```

- It turns every intent filter declared on the activity on or off at once.
- The system keeps the setting across app restarts and updates, until you change it again. It takes precedence over `android:enabled` in your manifest.
- To keep redirect handling off until you turn it on, declare the activity with `android:enabled="false"` and call `setRedirectHandlingEnabled(context, true)` when you need it.
- While it is off, a redirect that Drop-in has already started can't return to Drop-in. Only turn it off when no payment is in progress.

### Task affinity

Drop-in runs in the task of the activity that launched it. When the return arrives, Android finds that task by its task affinity, which is the affinity of the task's root activity, and compares it with Drop-in's affinity, `${applicationId}`. If they don't match, the return can't reach Drop-in.

Most apps don't set a task affinity and need to do nothing. Check your manifest for `android:taskAffinity`:

| Your manifest | What to do |
| --- | --- |
| No `android:taskAffinity` anywhere | Nothing. |
| `<application android:taskAffinity="">` | Set `android:taskAffinity="${applicationId}"` on the root activity of the task that launches Drop-in. |
| `<application android:taskAffinity="com.example.tasks">` | Same as above. |
| Your own affinity on individual activities | Same as above. |

```xml
<application android:taskAffinity="">

    <!-- The root activity of the task that launches Drop-in, usually your launcher activity -->
    <activity
        android:name=".MainActivity"
        android:exported="true"
        android:taskAffinity="${applicationId}">
        ...
    </activity>

</application>
```

- Set the affinity on the **root activity of the task**, not only on the activity that calls `DropIn.start`. An activity started normally from a root activity with an empty affinity stays in that root's task.
- The value must match Drop-in's exactly. Use the `${applicationId}` placeholder, which the build replaces with your application ID.
- Only the task that launches Drop-in needs this. Other activities and tasks can keep their own affinity.

An empty task affinity is a common hardening against task hijacking (StrandHogg). Giving the root activity a real affinity gives up that hardening for that task. If that is not acceptable for your launcher activity, launch Drop-in from a task whose root activity can have the affinity.

## Troubleshooting

Drop-in logs why a return can't arrive, but only when logging is enabled. Enable it while you integrate:

```kotlin
AdyenLogger.setLogLevel(AdyenLogLevel.VERBOSE)
```

When Drop-in starts a redirect, it checks your configuration and logs these errors:

| Message starts with | Fix |
| --- | --- |
| `The root activity of the task that launched Drop-in has task affinity ...` | See [Task affinity](#task-affinity). The message names both affinities. |
| `com.adyen.checkout.dropin.DropInRedirectHandlingActivity is not exported ...` | [Declare the redirect handling activity](#declare-the-redirect-handling-activity). |
| `com.adyen.checkout.dropin.DropInRedirectHandlingActivity is disabled ...` | Call `DropIn.setRedirectHandlingEnabled(context, true)`, or remove `android:enabled="false"` from the declaration. |

When a return arrives that Drop-in can't use, it logs one of these and ignores the return:

| Message | Meaning |
| --- | --- |
| `Received a redirect return, but Drop-in is not running. Ignoring it.` | Drop-in was closed, or the app's task was removed while the shopper was away, for example by swiping it away from Recents. |
| `Received a redirect return, but no action is shown. Ignoring it.` | The return arrived while Drop-in was not handling an action, for example an old browser tab returning a second time. |
| `Ignoring intent that is not a redirect return: ...` | Something started the redirect handling activity without `ACTION_VIEW` or without a URL. |

If the return still doesn't arrive:

1. **Check the URL.** The `returnUrl` must match your intent filter exactly: scheme, host, and path. Open it from the command line, and check that it opens your app without a chooser:

   ```shell
   adb shell am start -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d "myapp://checkout/dropin"
   ```

2. **Check the declaration.** The intent filter needs the `VIEW` action and the `DEFAULT` and `BROWSABLE` categories, and the activity needs `android:exported="true"`.
3. **Check the task.** While Drop-in is open, run `adb shell dumpsys activity activities` and find the task that contains `DropInActivity`. Its line must show `A=<uid>:<your application ID>`. If it shows another affinity, or `I=` instead of `A=` because the task has no affinity, see [Task affinity](#task-affinity). A task keeps the affinity it was created with, so after changing your manifest, test in a new task, for example after removing the app from Recents.
4. **Check for a second handler.** If Android shows a chooser, another app or another activity in your app claims the same URL. Use a different path, or an `https` App Link.

## Complete working example

- [Manifest declarations](../../example-app/src/main/AndroidManifest.xml): `DropInRedirectHandlingActivity` with the `/dropin` path, and the `singleTask` standalone screens
- [Standalone return handling](../../example-app/src/main/java/com/adyen/checkout/example/ui/v6/V6ViewModel.kt)
- [Drop-in return URL](../../example-app/src/main/java/com/adyen/checkout/example/service/ExampleV6DropInService.kt)
- [Turning redirect handling on and off](../../example-app/src/main/java/com/adyen/checkout/example/ui/settings/viewmodel/SettingsEditor.kt)

## Related docs

- [Android v6 overview](README.md)
- [Card component: session flow](card-session-flow.md)
- [Card component: advanced flow](card-advanced-flow.md)
- [Migration notes](../../MIGRATION.md)
