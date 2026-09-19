# Fixing Warnings and Errors in HomeScreen.kt

The goal is to resolve the warnings and functional errors identified in `HomeScreen.kt`, while also applying best practices for Jetpack Compose components.

## Identified Issues

1.  **Unused Import**: `androidx.compose.foundation.interaction.MutableInteractionSource` is imported but never used.
2.  **Incomplete Filtering**: `FeaturedListingsSection` only filters by name and property types, ignoring price range, BHK configuration, furnishing, availability, amenities, and minimum rating.
3.  **Search Scope**: The search query currently only matches the property name. It should also include area name and city for a better user experience.
4.  **Inconsistent Reset Logic**: The `onReset` callback in `FilterSearchBottomSheet` uses hardcoded values that differ from the `FilterParams` defaults.
5.  **Missing Modifier Parameters**: Several Composable functions do not accept a `modifier` parameter, which is a standard best practice for reusability and layout control.
6.  **Redundant Filters in HomeScreen**: The `appliedFilters` state is used to derive temp states for the bottom sheet, but the reset logic and application logic can be simplified.

## Proposed Changes

### [MODIFY] [HomeScreen.kt](file:///C:/Android_Kotlin_Work2/MyRent/app/src/main/java/com/techmania/myrent/screens/HomeScreen.kt)

#### Imports
- Remove `import androidx.compose.foundation.interaction.MutableInteractionSource`.
- Add `import androidx.compose.foundation.layout.ExperimentalLayoutApi` if not already present (it is used for `FlowRow`).

#### Composable Refactoring
- Add `modifier: Modifier = Modifier` to all internal Composables:
    - `HeaderGreetingSection`
    - `StickySearchBarSection`
    - `FilterSearchBottomSheet`
    - `FilterSectionHeader`
    - `FilterChipItem`
    - `CategorySection`
    - `FeaturedListingsSection`
    - `PropertyCard`
    - `PropertyFeatureItem`
- Ensure the passed `modifier` is used on the root element of each Composable.

#### Logic Updates
- **Filter Initialization**: Update `onReset` in `FilterSearchBottomSheet` to use default `FilterParams()` values.
- **FeaturedListingsSection**: Implement full filtering logic using all fields in `FilterParams`.
- **Search Logic**: Expand search to include `areaName` and `city`.

## Verification Plan

### Automated Tests
- Since this is a UI-heavy file, manual verification is primary, but I will ensure the code compiles by running:
  - `gradlew :app:compileDebugKotlin`

### Manual Verification
- Verify that the "Reset all" button in the filter sheet correctly resets all filters to their default states.
- Verify that applying filters (e.g., price range, BHK) correctly updates the property list.
- Verify that searching for a city or area name shows the expected properties.
- Verify that the unused import warning is gone.
