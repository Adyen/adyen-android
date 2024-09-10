[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Improved` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## New)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- Added support for 6 more locales: Catalan (ca-ES), Icelandic (is-IS), Bulgarian (bg-BG),
  Estonian (et-EE), Latvian (lv-LV) and Lithuanian (lt-lT).
- French meal vouchers are now available with the following payment method types: 
  - Up. Payment method type: **mealVoucher_FR_groupeup**.
  - Natixis. Payment method type: **mealVoucher_FR_natixis**.
  - Sodexo. Payment method type: **mealVoucher_FR_sodexo**.

## Improved
- For UPI Intent an error message will be shown when "Continue" button is pressed without selecting
  any UPI option.
- For drop-in, improved accessibility of back/close button in the navigation bar.

## Changed
- For drop-in, headers of preselected stored payment screen and payment methods list screen are
  updated.
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Adyen 3DS2](https://github.com/Adyen/adyen-3ds2-android/releases/tag/2.2.20)                          | **2.2.20**                    |

## Deprecated
- The style for payment method list headers has been changed.
  | Previous                                 | Now                                      |
  |------------------------------------------|------------------------------------------|
  | AdyenCheckout.TextAppearance.HeaderTitle | AdyenCheckout.TextAppearance.HeaderLabel |
