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
- Added support for 6 more locales: Catalan (ca-ES), Icelandic (is-IS), Bulgarian (bg-BG), Estonian (et-EE), Latvian (lv-LV) and Lithuanian (lt-lT).

## Improved
- For UPI Intent an error message will be shown when "Continue" button is pressed without selecting any UPI option.
- For drop-in, improved accessibility of back/close button in the navigation bar.
