# Implementation Plan - Compact and Attractive Home Screen

Redesign the `HomeScreen` to optimize screen space (compactness) and elevate the visual aesthetics (attractiveness) by adding a horizontal scroll section for quick discovery, modernizing property cards, and improving typography and layouts.

## User Review Required

> [!TIP]
> **Design Enhancements**:
> 1. **Horizontal Scroll Layer**: Introduce a "Nearby / Trending Properties" row that scrolls horizontally, allowing users to browse listings instantly without infinite scrolling.
> 2. **Compact Cards**: Reduce the property card image height from `210.dp` to `160.dp` and streamline the typography/padding to allow more content to fit comfortably on the screen.
> 3. **Modern Aesthetics**: Add soft card elevations, cleaner Material 3 chips for categories, and sleek transparent-tinted badges instead of heavy solid colors.

## Proposed Changes

### UI Components

#### [MODIFY] [HomeScreen.kt](file:///C:/Android_Kotlin_Work2/MyRent/app/src/main/java/com/techmania/myrent/screens/HomeScreen.kt)
- **Header & Search Bar Integration**: Merge the greeting section and search bar into a single streamlined top banner with improved visual padding.
- **Modern Category Chips**: Style category chips using subtle background color fills with thinner, elegant borders and better typography states.
- **Horizontal Trending Row**: Add a horizontally scrollable container for "Trending Homes" before the main vertical grid/list section.
- **Sleek Property Card**:
  - Decrease header/image height (`160.dp` instead of `210.dp`).
  - Update card corners to `18.dp` for a crisp look.
  - Swap hard solid badges for modern high-legibility transluscent pill overlays.
  - Optimize padding inside features and labels to make row spacing tighter and more professional.

## Verification Plan

### Automated/Build Verification
- Run Gradle task `:app:compileDebugKotlin` to verify no compilation errors.

### Manual Verification
- Deploy to an emulator/device.
- Observe the new header layout, category selection interaction, horizontal trending property list, and the compact vertical property cards.
