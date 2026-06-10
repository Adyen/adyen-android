# CheckoutTheme

`CheckoutTheme` lets you apply Compose-first styling to the Android v6 checkout UI.

Theme customization in Android v6 is intentionally scoped to the public Compose theme model so the SDK can keep the overall checkout UI consistent while still adapting to your app.

## Imports

```kotlin
import androidx.compose.foundation.isSystemInDarkTheme
import com.adyen.checkout.ui.theme.CheckoutColor
import com.adyen.checkout.ui.theme.CheckoutColors
import com.adyen.checkout.ui.theme.CheckoutTheme
```

## Apply a theme

Create the theme in your UI layer and pass it to `CheckoutPaymentFlow(...)`:

```kotlin
val theme = CheckoutTheme(
    colors = CheckoutColors.light(
        background = CheckoutColor(0xFFF8FAFCL),
        container = CheckoutColor(0xFFFFFFFFL),
        primary = CheckoutColor(0xFF111827L),
        textOnPrimary = CheckoutColor(0xFFFFFFFFL),
        highlight = CheckoutColor(0xFF2563EBL),
        destructive = CheckoutColor(0xFFDC2626L),
        text = CheckoutColor(0xFF111827L),
        textSecondary = CheckoutColor(0xFF6B7280L),
    ),
)

CheckoutPaymentFlow(
    controller = controller,
    theme = theme,
)
```

The same theme is used for the payment component, action handling, and any secondary screens rendered by `CheckoutPaymentFlow(...)`.

## Light and dark mode

Choose `CheckoutColors.light()` or `CheckoutColors.dark()` in the same place where your app decides which palette to render:

```kotlin
val useDarkTheme = isSystemInDarkTheme()

val theme = CheckoutTheme(
    colors = if (useDarkTheme) {
        CheckoutColors.dark()
    } else {
        CheckoutColors.light()
    },
)
```

If your app has its own theme toggle, use that state instead of `isSystemInDarkTheme()`. This applies the default Adyen light or dark palette automatically. If you need custom tokens on top of that, see [Color overrides](#color-overrides).

## Color overrides

Start from `CheckoutColors.light()` or `CheckoutColors.dark()` and override only the tokens you need.

| API | Purpose |
| --- | --- |
| `background` | Background color for checkout screens and surfaces. |
| `container` | Background color for secondary containers such as form fields. |
| `containerOutline` | Outline color for bordered containers. |
| `primary` | Primary action background color. |
| `textOnPrimary` | Text color used on primary actions. |
| `highlight` | Accent color for interactive highlights. |
| `destructive` | Destructive action background or emphasis color. |
| `textOnDestructive` | Text color used on destructive actions. |
| `disabled` | Background color for disabled controls. |
| `textOnDisabled` | Text color used on disabled controls. |
| `separator` | Divider and border separator color. |
| `text` | Primary text color across checkout UI. |
| `textSecondary` | Secondary text color across checkout UI. |

## Scope

- Configure the theme in the Compose UI layer, not on `CheckoutConfiguration`.
- Theme changes apply across the `CheckoutPaymentFlow(...)` tree.
- Localization is configured separately with `CheckoutLocalizationProvider`. See [README.md](README.md#localization).

## Related docs

- [v6 foundations](README.md)
- [Card component overview](card.md)
- [Card component: session flow](card-session-flow.md)
- [Card component: advanced flow](card-advanced-flow.md)
- [Migration notes](../../MIGRATION.md)
