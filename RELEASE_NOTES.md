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
- Support for the UPI Intent flow, where the shopper can choose a UPI app to pay through. They are redirected to and complete the payment on the selected app.
- The new iDEAL payment flow where the shopper is redirected to the iDEAL payment page to select their bank and authorize the payment.

## Improved
- Drop-in navigation:
    - Added the top navigation bar.
    - Disabled the dragging gesture that caused Drop-in to be dismissed.
    - When the shopper navigates back from an additional action screen (for example Await), Drop-in is dismissed.

- Autofill support for the following:
    - For gift cards, the gift card number and PIN fields.
    - For UPI Virtual Payments, the address field.
    - For payment methods that use them, the address input fields.

## Changed
- Dependency versions:
  | Name                                                                                                         | Version                       |
  |--------------------------------------------------------------------------------------------------------------|-------------------------------|
  | [AndroidX Compose BoM](https://developer.android.com/develop/ui/compose/bom/bom-mapping)                     | **2024.05.00**                |
  | [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.2)                | **2.8.2**                     |
  | [Material Design](https://github.com/material-components/material-components-android/releases/tag/1.12.0/)   | **1.12.0**                    |

## Deprecated
We recommend that you remove the following from your integration.
- For `IdealComponent`:
    - `isConfirmationRequired()`
    - `submit()`
- For iDEAL configuration:
    - `setViewType()`
    - `setHideIssuerLogos()`
    - `setSubmitButtonVisible()` 
