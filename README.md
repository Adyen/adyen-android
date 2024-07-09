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
implementation "com.adyen.checkout:drop-in-compose:5.6.0"
```
For the Credit Card component:
```groovy
implementation "com.adyen.checkout:card:5.6.0"
implementation "com.adyen.checkout:components-compose:5.6.0"
```

### Without Jetpack Compose

For Drop-in:
```groovy
implementation "com.adyen.checkout:drop-in:5.6.0"
```
For the Credit Card component:
```groovy
implementation "com.adyen.checkout:card:5.6.0"
```

The library is available on [Maven Central][mavenRepo].

## UI Customization

[See the UI Customization Guide for more.](docs/UI_CUSTOMIZATION.md)

## Migrate from v4

If you are upgrading from 4.x.x to a current release, check out our [migration guide][migration.guide].

## ProGuard

If you use ProGuard or R8, you do not need to manually add any rules, as they are automatically embedded in the artifacts.
Please let us know if you find any issues.

## Development

For development and testing purposes the project is accompanied by a test app. See [here](example-app/README.md) how to set it up and run it.

## Support

If you have a feature request, or spotted a bug or a technical problem, [create an issue here][github.newIssue].

For other questions, [contact our support team][adyen.support].

## Analytics and data tracking

Starting [5.0.0][analytics.firstVersion] the Drop-in and Components integrations contain analytics and tracking features that are turned on by default. Find out more about [what we track and how you can control it][docs.analytics].

## Contributing

We merge every pull request into the `develop` branch. We aim to keep `develop` in good shape, which allows us to release a new version whenever we need to.

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
[shield.buildStatus.image]: https://github.com/Adyen/adyen-android/actions/workflows/check_release.yml/badge.svg
[shield.buildStatus.link]: https://github.com/Adyen/adyen-android/actions
[shield.license.image]: https://img.shields.io/github/license/Adyen/adyen-android
[shield.license.link]: LICENSE
[docs.android]: https://docs.adyen.com/online-payments/build-your-integration/?platform=Android
[header.preview]: https://github.com/Adyen/adyen-android/assets/9079915/e6e18a07-b30f-41f0-b7ef-701b20e2e339
[adyen.testAccount]: https://www.adyen.com/signup
[docs.apiKey]: https://docs.adyen.com/development-resources/how-to-get-the-api-key
[docs.clientKey]: https://docs.adyen.com/development-resources/client-side-authentication#get-your-client-key
[docs.dropIn]: https://docs.adyen.com/online-payments/build-your-integration/?platform=Android&integration=Drop-in
[docs.components]: https://docs.adyen.com/online-payments/build-your-integration/?platform=Android&integration=Components
[mavenRepo]: https://repo1.maven.org/maven2/com/adyen/checkout/
[migration.guide]: https://docs.adyen.com/online-payments/build-your-integration/migrate-to-android-5-0-0
[github.newIssue]: https://github.com/Adyen/adyen-android/issues/new/choose
[adyen.support]: https://www.adyen.help/hc/en-us/requests/new
[analytics.firstVersion]: https://github.com/Adyen/adyen-android/releases/tag/5.0.0
[docs.analytics]: https://docs.adyen.com/online-payments/analytics-and-data-tracking
[contributing.guidelines]: https://github.com/Adyen/.github/blob/main/CONTRIBUTING.md
[dokka]: https://adyen.github.io/adyen-android/
[docs.checkout]: https://docs.adyen.com/online-payments/
[docs.apiExplorer]: https://docs.adyen.com/api-explorer/
