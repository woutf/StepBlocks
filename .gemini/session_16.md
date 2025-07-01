# Goals for this Session:
- Implement the ViewModel and logic for the Settings Screen.
- Make the master notification toggle interactive.

# What Was Accomplished:
- **Created `G:/My Drive/Programming/StepBlocks/app/src/main/java/com/stepblocks/viewmodel/SettingsViewModel.kt`**:
    - Defined `SettingsUiState` data class to hold the UI state.
    - Implemented `SettingsViewModel` with a `MutableStateFlow` for `_uiState` and exposed `uiState` as a `StateFlow`.
    - Added `onMasterNotificationToggleChange` function to update the `masterNotificationToggleEnabled` state.
    - Made the `SettingsViewModel` class and `onMasterNotificationToggleChange` function `open` to allow preview inheritance.
- **Modified `G:/My Drive/Programming/StepBlocks/app/src/main/java/com/stepblocks/ui/screens/SettingsScreen.kt`**:
    - Integrated `SettingsViewModel` using `viewModel()`.
    - Collected the `uiState` using `collectAsState()`.
    - Bound the `checked` state of the `Switch` to `uiState.masterNotificationToggleEnabled`.
    - Set the `onCheckedChange` callback of the `Switch` to call `viewModel.onMasterNotificationToggleChange(it)`.
- Successfully built the project and confirmed that the master notification toggle works in the emulator.

# Blockers or Issues:
- The initial build failed because `SettingsViewModel` was `final`, which prevented `FakeSettingsViewModel` (used for previews) from inheriting from it. This was resolved by making `SettingsViewModel` and its relevant methods `open`.

# Next Steps:
- Continue implementing the remaining functionality for the Settings Screen, such as handling "Sync Now", "Export History", and "Clear All Data" button clicks, and implementing the "Vibration pattern selector".