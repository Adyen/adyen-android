## Enabling Address Lookup Functionality
- You can enable this feature by setting your address configuration to lookup while building your card configuration as follows:
  ```kotlin
  CheckoutConfiguration(environment = environment, clientKey = clientKey) {
      card {
          setAddressConfiguration(AddressConfiguration.Lookup())
      }
  }
  ```
## Integrating with Address Lookup Functionality
- If you're integrating with Drop-in:
    - Implement the mandatory `onAddressLookupQueryChanged(query: String)` callback and optional `onAddressLookupCompletion(lookupAddress: LookupAddress)` callback.
    - Pass the result of these actions by using `AddressLookupDropInServiceResult` class.
- If you're integrating with standalone `CardComponent`:
    - Set `AddressLookupCallback` via `CardComponent.setAddressLookupCallback(AddressLookupCallback)` function to receive the related events.
    - Pass the result of these actions by calling `CardComponent.setAddressLookupResult(addressLookupResult: AddressLookupResult)`.
    - Delegate back pressed event to `CardComponent` by calling `CardComponent.handleBackPress()` which returns true if the back press is handled by Adyen SDK and false otherwise.
