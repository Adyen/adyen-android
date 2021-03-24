[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (### Added)
[//]: # ( - New payment method)
[//]: # ( ### Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( ### Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Added
- Validation of PublicKey in CSE module if it's used standalone.
- 'isReady' flag to ComponentState because some components might require some initialization time even if all the inputs are valid.
- 'isValid' now checks both 'isInputValid' and 'isReady' to be true.

## Fixed
- Handle Intent results if DropInActivity got destroyed.
- Queue API request if DropInService is not yet bound to DropInActivity




