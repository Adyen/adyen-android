[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Fixed` `Improved` `Changed` `Deprecated` `Removed`)
[//]: # (Example:)
[//]: # (## New)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- Launch Google Pay with `submit()` to get rid of the deprecated activity result handling.
- For drop-in, show a toolbar on every intermediary screen, so shoppers can always easily navigate back.

## Fixed

## Improved

## Changed
- For 3DS2 native flow, cancellations by shopper trigger `onAdditionalDetails()` event. You can make a `/payments/details` call to gain more insight into the transaction.
  - If you already handle other 3DS2 errors by making a `/payments/details` call, then no changes are required and the specific handling for `Cancelled3DS2Exception` can be removed.
  - If not yet implemented, you should update your systems to handle shopper cancellations using the new flow.
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  |                           |                     |

## Deprecated
- `Cancelled3DS2Exception` is now deprecated. Shopper cancellations trigger the `onAdditionalDetails()` event, enabling a `/payments/details` call for transaction insights.
- For 3DS2 native flow, `Cancelled3DS2Exception` is deprecated. Now the shopper cancellations triggers `onAdditionalDetails()` and you can make a `/payments/details` call.
- The styles and strings for the Cash App Pay loading indicator. Use the new styles and strings instead.
  | Previous                                                  | Now                                                              |
  |-----------------------------------------------------------|------------------------------------------------------------------|
  | `AdyenCheckout.CashAppPay.ProgressBar`                    | `AdyenCheckout.ProcessingPaymentView.ProgressBar`                |
  | `AdyenCheckout.CashAppPay.WaitingDescriptionTextView`     | `AdyenCheckout.ProcessingPaymentView.WaitingDescriptionTextView` |
  | `cash_app_pay_waiting_text`                               | `checkout_processing_payment` |

## Repository Maintenance
- We are changing the default branch of our SDK repository from `develop` to `main`. If you are using our SDK repository and working with the `develop` branch, we recommend switching to the `main` branch. The `develop` branch will be removed within the next few weeks.
