---
name: android-ui-resources
description: Add or change XML layouts, styles, and string resources.
---

# android-ui-resources

Add or change XML resources in the Adyen Android SDK without breaking merchant themes or shopper localization.

## Usage

Invoke this skill when touching a module's `res/` directory: layouts, styles, or strings. These rules apply to the XML-based views (v5 and the modules that still use them); Compose UI follows the theme system in `docs/v6/theme.md` instead.

Search the codebase for a similar existing view before writing a new one — the correct style, attribute, and string pattern almost always already exists somewhere.

## Layouts and styles

### Give every new view a style

Merchants customize our UI through styles, so a view without one cannot be customized. Follow the naming of the surrounding styles.

### Use theme-safe attributes

Only reference attributes that are guaranteed to exist in a merchant's theme. Default Material attributes are safe; framework attributes often are not.

- **Avoid** `?android:attr/textColor` — it may be undefined in a merchant theme and crashes at inflation time.
- **Prefer** `?attr/colorOnSurface` and the other Material defaults.

### Keep the style hierarchy complete

A style like `AdyenCheckout.Image.Logo.Large` requires every ancestor to exist as a declared style, even if empty. Without them, merchants who override a parent style hit errors.

```xml
<!-- All three must exist, even though the parents are empty -->
<style name="AdyenCheckout.Image" />
<style name="AdyenCheckout.Image.Logo" parent="AdyenCheckout.Image" />
<style name="AdyenCheckout.Image.Logo.Large" parent="AdyenCheckout.Image.Logo">
    <item name="android:layout_width">48dp</item>
    <item name="android:layout_height">48dp</item>
</style>
```

## Strings

### Always use the shopper locale

Components must render in the shopper locale, not the device locale. This covers every displayed string: labels, hints, errors, and content descriptions. Resolve them through `localizedContext`, never through the plain view or activity context.

```kotlin
val label = localizedContext.getString(R.string.checkout_card_holder_name_label)
```

Search for `localizedContext` and `localizedContext.getString` for examples.

### Set strings through styles, not layouts

Text belongs in the style so merchants can override it. In a layout, use `tools:text` for preview only:

```xml
<TextView
    style="@style/AdyenCheckout.TextView.Title"
    tools:text="@string/checkout_card_holder_name_label" />
```

Do not use `android:text` in a component layout.

## Important

- Adding or changing a merchant-visible string or style is merchant-observable: use a `feature/` or `fix/` branch prefix, not `chore/`.
- When strings change, confirm translations are present for all supported locales.
