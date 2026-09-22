# Implementation Plan - Dynamic Profile Badges and Verification

Refine the profile header to dynamically reflect the user's role, verification status, rating, and registration year.

## Proposed Changes

### UI Updates (Profile Screen)

#### [MODIFY] [ProfileScreenNew.kt](file:///C:/Android_Kotlin_Work2/MyRent/app/src/main/java/com/techmania/myrent/screens/ProfileScreenNew.kt)
- **Data Loading**: Update `LaunchedEffect` to fetch `isVerified`, `rating`, and `memberSince` from Firebase.
- **`ProfileHero`**:
    - Update the verification badge:
        - If `isVerified` is true: Show with checkmark icon and label "Verified [role]".
        - If `isVerified` is false: Show with different icon (e.g., `Person` or `Info`) and label just as "[Role]" (no "Verified" text).
    - Ensure `rating` and `memberSince` badges use the loaded `profileState`.

## Verification Plan

### Automated/Build Verification
- Run `:app:compileDebugKotlin` to ensure no syntax errors.

### Manual Verification
1.  **Sign Up**: Register a new tenant. Verify the profile shows "Tenant", "0.0 rating", and "Since [Current Year]".
2.  **Verification Flow**: Manually update the Firebase entry for the user to `isVerified = true`. Verify the profile header automatically updates to "Verified tenant" with a checkmark.
3.  **Rating Test**: Manually update the rating in Firebase and verify it reflects in the profile.
