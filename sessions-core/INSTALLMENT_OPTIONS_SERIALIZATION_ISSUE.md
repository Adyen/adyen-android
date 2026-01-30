# Installment Options Serialization Issue

## Problem Summary

The `installmentOptions` field in `SessionSetupConfiguration` is not being serialized correctly. Specifically, the card options (and other payment method installment options) lose their nested structure during serialization because the `SessionSetupInstallmentOptions` objects are not being properly serialized.

## Affected Code

**File:** `sessions-core/src/main/java/com/adyen/checkout/sessions/core/SessionSetupConfiguration.kt`

**Line 40-43:**
```kotlin
putOpt(
    INSTALLMENT_OPTIONS,
    modelObject.installmentOptions?.let { JSONObject(it) },
)
```

## Root Cause

The `JSONObject(Map)` constructor does not properly serialize complex model objects. When called with a `Map<String, SessionSetupInstallmentOptions?>`, it attempts to convert the map naively (likely using `toString()` or reflection) without invoking the dedicated `SessionSetupInstallmentOptions.SERIALIZER`.

### What Should Happen

Each `SessionSetupInstallmentOptions` object should be serialized using its own `SERIALIZER.serialize()` method, which properly handles:
- `plans` (List<String>)
- `values` (List<Int>)
- `preselectedValue` (Int?)

### What Actually Happens

The `JSONObject(Map)` constructor treats `SessionSetupInstallmentOptions` as a plain object, resulting in incorrect or incomplete serialization of the nested fields.

## Evidence from Test

**Test:** `SessionSetupConfigurationTest.kt` line 109-169

The test deserializes JSON with installment options:
```json
{
  "installmentOptions": {
    "card": {
      "plans": ["with_interest"],
      "values": [1, 2, 3, 6],
      "preselectedValue": 2
    },
    "visa": {
      "plans": ["regular", "revolving"],
      "values": [1, 2, 3, 4, 5, 12]
    }
  }
}
```

After deserializing and re-serializing, the test expects the card options to be present with their fields intact, but the current serialization logic fails to produce the correct output.

## Why Deserialization Works

The deserialization code **correctly** uses a helper function:

**Line 56-57:**
```kotlin
installmentOptions = jsonObject.optJSONObject(INSTALLMENT_OPTIONS)
    ?.jsonToMap(SessionSetupInstallmentOptions.SERIALIZER),
```

The `jsonToMap()` extension function (defined in `JsonUtils.kt`) properly:
1. Iterates through the JSONObject entries
2. Invokes `SessionSetupInstallmentOptions.SERIALIZER.deserialize()` for each value
3. Returns a properly constructed map

## Proposed Solution

Create a symmetric serialization helper function (inverse of `jsonToMap()`) that:

1. Takes a `Map<String, SessionSetupInstallmentOptions?>` and a serializer
2. Creates a new `JSONObject`
3. Iterates through map entries
4. For each entry, calls `SessionSetupInstallmentOptions.SERIALIZER.serialize()` on the value
5. Puts the serialized `JSONObject` into the result with the corresponding key
6. Returns the final `JSONObject`

### Implementation Options

#### Option 1: Add Extension Function to JsonUtils.kt
Add a new extension function similar to `jsonToMap()`:

```kotlin
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Throws(JSONException::class)
inline fun <reified T : ModelObject> Map<String, T?>.mapToJson(
    modelSerializer: ModelObject.Serializer<T>
): JSONObject {
    val jsonObject = JSONObject()
    this.forEach { (key, value) ->
        if (value != null) {
            jsonObject.put(key, modelSerializer.serialize(value))
        }
    }
    return jsonObject
}
```

Then update the serialization code:
```kotlin
putOpt(
    INSTALLMENT_OPTIONS,
    modelObject.installmentOptions?.mapToJson(SessionSetupInstallmentOptions.SERIALIZER),
)
```

#### Option 2: Inline Implementation
Directly implement the logic in the serializer without creating a reusable extension:

```kotlin
putOpt(
    INSTALLMENT_OPTIONS,
    modelObject.installmentOptions?.let { options ->
        JSONObject().apply {
            options.forEach { (key, value) ->
                if (value != null) {
                    put(key, SessionSetupInstallmentOptions.SERIALIZER.serialize(value))
                }
            }
        }
    },
)
```

## Recommendation

**Option 1** is preferred because:
- It creates a reusable utility function that mirrors `jsonToMap()`
- It maintains consistency with the existing deserialization pattern
- It can be used in other parts of the codebase if similar serialization needs arise
- It follows the existing code organization in `JsonUtils.kt`

## Impact

- **Severity:** High - Data loss during serialization
- **Affected Area:** Sessions module, specifically `SessionSetupConfiguration`
- **User Impact:** Any code relying on round-trip serialization of installment options will fail
- **Test Coverage:** Already has failing test case in `SessionSetupConfigurationTest.kt`

## Verification (Jan 30, 2026)

### Test Execution
Running the first test in `SessionSetupConfigurationTest`:
```bash
./gradlew :sessions-core:testDebugUnitTest --tests "com.adyen.checkout.sessions.core.SessionSetupConfigurationTest.when serializing deserialized configuration, then all fields should be serialized"
```

### Observed Output
The `println` statement in the test reveals the serialized `installmentOptions`:
```
{"card":null,"visa":null}
```

### Expected Output
```json
{
  "card": {"plans": ["with_interest"], "values": [1, 2, 3, 6], "preselectedValue": 2},
  "visa": {"plans": ["regular", "revolving"], "values": [1, 2, 3, 4, 5, 12]}
}
```

### Failure Point
- **Line 54:** `assertTrue("card options should not be null", cardOptions != null)`
- **Error:** `java.lang.AssertionError: card options should not be null`

### Confirmed Cause
The `JSONObject(Map)` constructor at `SessionSetupConfiguration.kt:42` cannot serialize `SessionSetupInstallmentOptions` objects - it treats them as unknown types and outputs `null` for each entry.
