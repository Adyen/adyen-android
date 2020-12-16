
[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (### Added)
[//]: # ( - New payment method)
[//]: # ( ### Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( ### Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

### Added:
- Blik Component
- Translations for: cs-rCZ, el-rGR, hr-rHR, hu-rHU, ro-rRO, sk-rSK, sl-rSI
- `hideCvc` and `hideCvcStoredCard` flags on `CardConfiguration`
- Frictionless flow for preselected stored payment.

### Changed:
- Change model objects to new API version
- Removed AfterPay Component.
- Removed all deprecated methods.
- Refactored separation between regular and stored payment methods.
- Refactored OneClick flow to Preselected Stored Payment Method.

### Fixed:
- Client key validation on Drop-in default generated Configurations
- Payment methods list will only show up after all availability checks are done.
- GooglePay SDK dependency is now visible to merchants.
