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
    - Implement the mandatory `onAddressLookupQueryChanged(query: String)` callback and optional `onAddressLookupCompletion(lookupAddress: LookupAddress)` callback in your implementation of DropInService.
    - Pass the result of these actions by using `AddressLookupDropInServiceResult` class.
      ```kotlin
      override fun onAddressLookupQueryChanged(query: String) {
          // Use the query parameter to make a call to your desired maps endpoint
          // Map the address objects LookupAddress object and 
          // pass it back to Drop-in as follows:
          sendAddressLookupResult(AddressLookupDropInServiceResult.LookupResult(options))  
      }

      // Optionally use this callback to get the complete details for a LookupAddress option selected by the shopper
      override fun onAddressLookupCompletion(lookupAddress: LookupAddress): Boolean {
          // Make a call to your desired maps endpoint and pass the complete 
          // LookupAddress object back to Drop-in as follows:
          sendAddressLookupResult(AddressLookupDropInServiceResult.LookupComplete(completeLookupAddress))
          return true // Return false if you do not need to make an api call
         
      } 
      ```
- If you're integrating with standalone `CardComponent`:
    - Set `AddressLookupCallback` via `CardComponent.setAddressLookupCallback(AddressLookupCallback)` function to receive the related events.
    - Pass the result of these actions by calling `CardComponent.setAddressLookupResult(addressLookupResult: AddressLookupResult)`.
    ```kotlin
    cardComponent.setAddressLookupCallback(object : AddressLookupCallback {
        override fun onQueryChanged(query: String) {
            // Use the query parameter to make a call to your desired maps endpoint
            // Map the address objects LookupAddress object and 
            // pass it back to card component as follows:
            cardComponent.updateAddressLookupOptions(options)
        }

  // Optionally use this callback to get the complete details for a LookupAddress option selected by the shopper
        override fun onLookupCompletion(lookupAddress: LookupAddress): Boolean {
            // Make a call to your desired maps endpoint and pass the complete 
            // LookupAddress object back to card component as follows:
            cardComponent.setAddressLookupResult(AddressLookupResult.Completed(completeLookupAddress))
            return true // Return false if you do not need to make an api call
        }
    })
    ```
    - Delegate back pressed event to `CardComponent` by calling `CardComponent.handleBackPress()` which returns true if the back press is handled by Adyen SDK and false otherwise.
    ```kotlin
    override fun onBackPressed() {
        if (cardComponent?.handleBackPress() == true) return
        super.onBackPressed()
    }
    ```
## Screenshots
![address-lookup](https://github.com/Adyen/adyen-android/assets/6615094/ee671606-fbf4-4674-96ec-99a804e53d2e)
