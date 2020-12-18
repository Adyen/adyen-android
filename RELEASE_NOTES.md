
[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (### Added)
[//]: # ( - New payment method)
[//]: # ( ### Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( ### Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

**WARNING** - This version has changes in dependency names and package names.
Make sure you are using the correct dependency (for components) and re-import classes that were in the `base` package.

### Changed:
- Refactored module structure and artifact IDs. Now each payment method has only 1 module with a simplified name.
- Renamed package `com.adyen.checkout.base` to `com.adyen.checkout.components`
- Renamed package `com.adyen.checkout.base.component` to `com.adyen.checkout.components.base`


