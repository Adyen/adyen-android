![Checkout Android Logo][header.image]

[![Maven Central][shield.mavenCentral.image]][shield.mavenCentral.link]
[![Build status][shield.buildStatus.image]][shield.buildStatus.link]
[![License][shield.license.image]][shield.license.link]

# Adyen Android

Adyen Android allows you to accept in-app payments by providing you with the building blocks you need to create a checkout experience.

For an overview of how you can integrate with Adyen on Android check out the [documentation][docs.android].

<br/>

![DropIn Preview][header.preview]

<br/>

## Drop-in/Components lifecycle

| Major version | State       | Deprecated   | End-of-life  |
|---------------|-------------|--------------|--------------|
| 5.x.x         | Active      | ---          | ---          |
| 4.x.x         | Inactive    | TBA          | TBA          |
| 3.x.x         | End-of-life | October 2023 | October 2024 |

More information about our versioning and the Drop-in/Components lifecycle can be found [here](https://docs.adyen.com/online-payments/upgrade-your-integration/#lifecycle).

## Prerequisites

* [Adyen test account][adyen.testAccount]
* [API key][docs.apiKey]
* [Client key][docs.clientKey]

## Installation

There are two main integration types: [Drop-in][docs.dropIn] and [Components][docs.components].
Import the corresponding module in your `build.gradle` file.

### With Jetpack Compose

For Drop-in:
```groovy
implementation "com.adyen.checkout:drop-in-compose:LATEST_VERSION"
```
For the Credit Card component:
```groovy
implementation "com.adyen.checkout:card:LATEST_VERSION"
implementation "com.adyen.checkout:components-compose:LATEST_VERSION"
```

### Without Jetpack Compose

For Drop-in:
```groovy
implementation "com.adyen.checkout:drop-in:LATEST_VERSION"
```
For the Credit Card component:
```groovy
implementation "com.adyen.checkout:card:LATEST_VERSION"
```

Find out what the latest version is [here](https://github.com/Adyen/adyen-android/releases/latest). The library is available on [Maven Central][mavenRepo].

## Additional documentation

* [UI Customization guide][docs.github.uiCustomization]

* [Additional documentation for payment methods][docs.github.paymentMethods]

## Migrate from v4

If you are upgrading from 4.x.x to a current release, check out our [migration guide][migration.guide].

## ProGuard

If you use ProGuard or R8, you do not need to manually add any rules, as they are automatically embedded in the artifacts.
Please let us know if you find any issues.

## Development and testing

For development and testing purposes the project is accompanied by a test app. See [here](example-app/README.md) how to set it up and run it.

To test your integration you could use [Adyen Test Cards Android][adyenTestCardsAndroid]. This will allow you to easily autofill test payment method information.

## Support

If you have a feature request, or spotted a bug or a technical problem, [create an issue here][github.newIssue]. For other questions, contact our Support Team via [Customer Area][adyen.support] or via email: support@adyen.com

## Analytics and data tracking

Starting [5.0.0][analytics.firstVersion] the Drop-in and Components integrations contain analytics and tracking features that are turned on by default. Find out more about [what we track and how you can control it][docs.analytics].

## Contributing

We merge every pull request into the `main` branch. We aim to keep `main` in good shape, which allows us to release a new version whenever we need to.

Have a look at our [contributing guidelines][contributing.guidelines] to find out how to raise a pull request.

## See also

* [Android API documentation][dokka]

* [Adyen Checkout Documentation][docs.checkout]

* [Adyen API Explorer][docs.apiExplorer]

## License

This repository is available under the [MIT license](LICENSE).

[header.image]: https://user-images.githubusercontent.com/9079915/198013698-139bf6f1-a15a-447d-8eed-97ce1354b43f.png
[shield.mavenCentral.image]: https://img.shields.io/maven-central/v/com.adyen.checkout/drop-in
[shield.mavenCentral.link]: https://mvnrepository.com/artifact/com.adyen.checkout
[shield.buildStatus.image]: https://github.com/Adyen/adyen-android/actions/workflows/check_v5.yml/badge.svg
[shield.buildStatus.link]: https://github.com/Adyen/adyen-android/actions
[shield.license.image]: https://img.shields.io/github/license/Adyen/adyen-android
[shield.license.link]: LICENSE
[docs.android]: https://docs.adyen.com/online-payments/build-your-integration/?platform=Android
[header.preview]: https://github.com/user-attachments/assets/0393e58d-172c-45fb-9e49-3a720fe53c89
[adyen.testAccount]: https://www.adyen.com/signup
[docs.apiKey]: https://docs.adyen.com/development-resources/how-to-get-the-api-key
[docs.clientKey]: https://docs.adyen.com/development-resources/client-side-authentication#get-your-client-key
[docs.dropIn]: https://docs.adyen.com/online-payments/build-your-integration/?platform=Android&integration=Drop-in
[docs.components]: https://docs.adyen.com/online-payments/build-your-integration/?platform=Android&integration=Components
[docs.github.uiCustomization]: docs/UI_CUSTOMIZATION.md
[docs.github.paymentMethods]: docs/payment-methods
[mavenRepo]: https://repo1.maven.org/maven2/com/adyen/checkout/
[migration.guide]: https://docs.adyen.com/online-payments/build-your-integration/migrate-to-android-5-0-0
[github.newIssue]: https://github.com/Adyen/adyen-android/issues/new/choose
[adyen.support]: https://ca-live.adyen.com/ca/ca/contactUs/support.shtml
[analytics.firstVersion]: https://github.com/Adyen/adyen-android/releases/tag/5.0.0
[docs.analytics]: https://docs.adyen.com/online-payments/analytics-and-data-tracking
[contributing.guidelines]: https://github.com/Adyen/.github/blob/main/CONTRIBUTING.md
[dokka]: https://adyen.github.io/adyen-android/
[docs.checkout]: https://docs.adyen.com/online-payments/
[docs.apiExplorer]: https://docs.adyen.com/api-explorer/
[adyenTestCardsAndroid]: https://github.com/Adyen/adyen-testcards-android
