[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Fixed
- For cards that require the shopper to input their address:
    - When internet connection is lost while loading, it no longer crashes. Instead an error is returned.
    - The **Country** dropdown menu to select no longer displays no options. Previously, an error sometimes caused the menu to have no options, so the transaction couldn't be submitted.
