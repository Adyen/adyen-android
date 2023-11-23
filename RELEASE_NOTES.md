[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Note
If you are using WeChat Pay please update to this version or migrate to 5.x.x to make sure WeChat Pay will work for all Android versions.

## Fixed
- WeChatPay now works correctly on [Android 11](https://www.android.com/android-11/) and later. This fixes a known issue from previous 4.x.x versions.

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [WeChat Pay](https://developers.weixin.qq.com/doc/oplatform/en/Mobile_App/Access_Guide/Android.html)   | **6.8.0**                     |