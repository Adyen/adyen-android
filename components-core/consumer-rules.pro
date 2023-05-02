# Keep the Component constructors for reflection initialization in the Factory.
-keepclassmembers public class * implements com.adyen.checkout.components.core.internal.PaymentComponent {
   public <init>(...);
}
-keepclassmembers public class * implements com.adyen.checkout.components.core.internal.ActionComponent {
   public <init>(...);
}
