# Implementation Plan - Fix Warnings and Errors in HomeScreen.kt

The goal is to resolve the warnings and functional errors identified in `HomeScreen.kt`, while also applying best practices for Jetpack Compose components.

## User Review Required

> [!IMPORTANT]
> The current filtering logic in `FeaturedListingsSection` is highly incomplete. I will be implementing a full filter that respects all parameters (Price, BHK, Furnishing, etc.). This might significantly reduce the number of visible items if the mock data doesn't match the default filter criteria perfectly.

## Proposed Changes

### [Component] UI Screens

#### [MODIFY] [HomeScreen.kt](file:///C:/Android_Kotlin_Work2/MyRent/app/src/main/java/com/techmania/myrent/screens/HomeScreen.kt)

- **Cleanup**: Remove unused `MutableInteractionSource` import.
- **Best Practices**: Add `modifier: Modifier = Modifier` to all internal Composable functions and apply it to their root elements.
- **Functional Fixes**:
    - Update `FeaturedListingsSection` to implement comprehensive filtering (Query, Type, Price, BHK, Furnishing, Availability, Amenities, Rating).
    - Expand Search Query to look into `name`, `areaName`, and `city`.
    - Fix `FilterSearchBottomSheet.onReset` to align with `FilterParams` defaults.
- **Performance**: Use `remember(searchQuery, filters, properties)` for the filtered list calculation in `FeaturedListingsSection` to avoid re-filtering on every recomposition unless dependencies change.

## Verification Plan

### Automated Tests
- Run `gradlew :app:compileDebugKotlin` to ensure no syntax errors.

### Manual Verification
1.  Open the Home Screen.
2.  Test the Search Bar with city and area names.
3.  Open the Filter Bottom Sheet, change multiple filters, and click "Apply".
4.  Test the "Reset all" button in the Filter Bottom Sheet.
5.  Check that the UI remains responsive and follows the adaptive width constraints.
