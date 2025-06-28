 # Remaining Android App Tasks

This document outlines the remaining features and improvements for the StepBlocks phone application, based on the `description.txt` and `technical_steps.txt` documents.

## 1. Time Block Auto-Prefill for New Blocks
- **Description**: When adding a new time block, automatically pre-fill `startTime`, `endTime`, `name`, and `targetSteps` based on the last existing block or default values.
    - If blocks exist: `startTime` = `endTime` of last block, `endTime` = `startTime` + 3 hours (or 23:59).
    - If no blocks exist: `startTime` defaults to 06:00.
    - `name`: Empty (focus on this field).
    - `targetSteps`: Average of existing blocks or 2000 if first block.
- **Reference**: `description.txt` (Section 3.2, "Add Block Behavior"), `technical_steps.txt` (Segment 2.2).

## 2. Template Validation: At Least One Time Block
- **Description**: Implement validation to ensure that a template must have at least one time block before it can be saved.
- **Reference**: `description.txt` (Section 3.2, "Validation Rules").

## 3. Template Detail Screen Enhancements (Visual Timeline)
- **Description**: Implement the "Visual timeline of blocks (horizontal bar chart)" on the `TimeBlocksScreen`.
- **Reference**: `description.txt` (Section 3.4, "Layout").

## 4. History Screen
- **Description**: Develop the complete "History Screen" functionality.
    - Calendar view showing completion percentage for days with data.
    - Display selected day's details: date, template used, overall progress, and list of blocks with progress bars (name, actual/target, percentage).
    - Interactions: tap calendar day, swipe calendar, pull to refresh.
- **Reference**: `description.txt` (Section 3.5), `technical_steps.txt` (Phase 6.1).

## 5. Settings Screen
- **Description**: Develop the complete "Settings Screen" functionality.
    - Sections for "Watch Connection", "Notifications", "Data", and "About".
    - Includes connection status, sync button, notification toggles, vibration pattern selector, data export/clear buttons, app version, and help link.
- **Reference**: `description.txt` (Section 3.6).

---

Once these phone app features are complete, the next major phases will involve developing the **Watch App** and the **Notification System** as detailed in `technical_steps.txt` (Phase 3 onwards).