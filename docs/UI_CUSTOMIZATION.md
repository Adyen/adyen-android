# UI customization Guide

The UI can be styled to match your app and brand. The styling of fonts, colors, layouts and buttons can be customized using XML and string resources can also be customized.

The base theme can be extended and overridden. You can also use a custom base theme, but make sure it extends `Theme.MaterialComponents`.

#### In this guide:
- [Customizing the base theme](#customizing-the-base-theme)
- [Customizing the style for specific view types](#customizing-the-style-for-specific-view-types)
- [Customizing a specific view](#customizing-a-specific-view)
- [Adding dark mode support](#adding-dark-mode-support)
- [Overriding string resources](#overriding-string-resources)
- [Styling Custom Tabs](#styling-custom-tabs)

## Customizing the base theme

To customize the base theme there are three different options:

### Option 1: Inherit from your app theming

To make our SDK inherit from your app theming you can simply make our `AdyenCheckout` style extend from your app theme. Our SDK is built with Material theme, so it’s very important that your theme extends a `Theme.MaterialComponents` theme. If your theme doesn’t or can’t extend a Material theme, then you can try option 2.

```XML
<style name="AdyenCheckout" parent="YOUR_APP_THEME" />
```

### Option 2: SDK to have it’s own custom theming

If you want our SDK to have its own custom theme, separate from your app theme, you can simply create a style that extends a Material theme and change the attributes to your liking.

```XML
<style name="AdyenCheckout" parent="Theme.MaterialComponents(.SOME_EXTENSION)" >
    <!-- Changes colors and styles to your liking here -->
</style>
```

### Option 3: Keep default Adyen theme

You can keep the default Adyen theme by simply adding one line. Optionally, you can change some attributes.

```XML
<style name="AdyenCheckout" parent="Adyen" >
    <!-- Optionally: change the primary color and background color of the default theme -->
    <item name="colorPrimary">#00FF00</item>
    <item name="android:colorBackground">#FF0000</item>
</style>
```

To figure out all the colors and styling you can override have a look at [the Material guides](https://m2.material.io/design/color/the-color-system.html) or check [our Adyen base style](https://github.com/Adyen/adyen-android/blob/main/ui-core/src/main/res/values/styles.xml).

## Customizing the style for specific view types

In case you want to change the styling for a specific view type, you can do this by copying the base styling into your `styles.xml` and change the values as you like. For example, if you want to change the border color of every `TextInputLayout` to red:

```XML
<style name="AdyenCheckout.TextInputLayout" parent="Widget.MaterialComponents.TextInputLayout.OutlinedBox">
    <!-- Changes the border color to red -->
    <item name="boxStrokeColor">#FF0000</item>
    <!-- These are part of our base style. It's better to leave them in so the UI looks good, but you can change the values to your liking -->
    <item name="hintTextColor">?attr/colorPrimary</item>
    <item name="android:minHeight">@dimen/input_layout_height</item>
</style>
```

It can be difficult to find which style is applied to which view. To figure this out we recommend to take a look at [our styles.xml](https://github.com/Adyen/adyen-android/blob/main/ui-core/src/main/res/values/styles.xml) or use the Layout Inspector.

## Customizing a specific view

Every view has its own style that builds on top of the view type style. This allows you to customize a specific view. Take for example the card number input field, copy the base style in your `styles.xml`

```XML
<style name="AdyenCheckout.Card.CardNumberInput" parent="AdyenCheckout.TextInputEditText">
    <!-- Attribute from base style, it's recommended to leave it as is -->
    <item name="android:hint">@string/checkout_card_number_hint</item>
    <!-- Change the background color to magenta and increase the text size -->
    <item name="android:background">#FF00FF</item>
    <item name="android:textSize">16sp</item>
</style>
```

## Adding dark mode support
Out of the box the SDK doesn’t support dark mode, but you can easily add this with some additional setup. Copy the base `AdyenCheckout` style in your `styles.xml` and change the parent to a Material DayNight theme. Now you can use the `values-night` resource folder to define colors and styles to be used in dark mode.

```XML
<style name="AdyenCheckout" parent="Theme.MaterialComponents.DayNight">
    <!-- These color values are both defined in values/colors.xml and values-night/colors.xml to change the color based on selected mode -->
    <item name="colorPrimary">@color/color_primary</item>
    <item name="colorOnPrimary">@color/color_on_primary</item>
    <item name="colorAccent">@color/color_accent</item>
    <item name="android:colorBackground">@color/color_background</item>
    <item name="colorBackgroundFloating">@color/color_background</item>
    <item name="colorOnBackground">@color/color_on_background</item>
    <item name="colorSurface">@color/color_surface</item>
    <item name="colorOnSurface">@color/color_on_surface</item>

    <item name="android:textColor">@color/text_color_primary</item>
    <item name="android:textColorPrimary">@color/text_color_primary</item>
    <item name="android:textColorSecondary">@color/text_color_primary</item>
    <item name="android:textColorTertiary">@color/text_color_primary</item>
    <item name="android:textColorLink">@color/text_color_link</item>
    <item name="bottomSheetDialogTheme">@style/AdyenCheckout.BottomSheetDialogTheme</item>
</style>
```

To learn more about Material dark theming you can read [this guide](https://m2.material.io/develop/android/theming/dark) or take a look at [our example app](https://github.com/Adyen/adyen-android/blob/main/example-app/src/main/res/values/styles.xml).

## Overriding string resources

It is possible to change text in the SDK by overriding string resources. To override a string resource you have to copy the string resource in your own strings.xml. The easiest way to find the string resource is to search for the string on Github. In case your app supports multiple languages you can do the exact same thing, but make sure to use the right `values` directory (for example `values-nl-rNL`).

```XML
<resources>
    <!-- Original text is: "Change Payment Method" -->
    <string name="change_payment_method">More Payment Methods</string>
</resources>
```

Payment method names in the payment method list can be overridden with a configuration object:

```kotlin
CheckoutConfiguration(shopperLocale, environment, clientKey) {
    dropIn {
        overridePaymentMethodName(PaymentMethodTypes.SCHEME, "Credit cards")
        overridePaymentMethodName(PaymentMethodTypes.GIFTCARD, "Specific gift card")
    }
}
```

If you cannot find a certain string in the code base, then check whether it is coming from the Checkout API. Make sure you localize these strings by sending the correct [shopperLocale](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions#request-shopperLocale).

## Styling Custom Tabs

We use Custom Tabs to launch any external redirects that cannot be handled inside the SDK or by another installed native app.
By default we set the toolbar color to match the `colorPrimary` attribute defined in your theme.
To change this color to a different value than your `colorPrimary` attribute, you can override the `AdyenCheckout.CustomTabs` style in your `styles.xml`:

```xml
<style name="AdyenCheckout.CustomTabs">
    <item name="adyenCustomTabsToolbarColor">@color/someColor1</item>
    <!--
    Additional colors that can be overridden as well
    <item name="adyenCustomTabsSecondaryToolbarColor">@color/someColor2</item>
    <item name="adyenCustomTabsNavigationBarColor">@color/someColor3</item>
    <item name="adyenCustomTabsNavigationBarDividerColor">@color/someColor4</item>
    -->
</style>
```
