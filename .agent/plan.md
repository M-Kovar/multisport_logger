# Project Plan

Enhance Multisport Logger for Samsung Galaxy S23 and refine UI/UX:
1. Galaxy S23 UI optimization & decluttering:
   - Fix overlapping FAB covering last items in the list (add proper content padding to LazyColumn).
   - Compact search field.
   - Fix "Výchozí měsíční cena karty" card border/styling in Subscriptions tab.
   - Fix Analytics tab layout: compact stats card, fix vertical alignment of '% hodnoty' text in negative balance.
   - Streamline bottom navigation bar: icon-only tabs (hide labels to maximize vertical screen space).
   - Replace Extended FAB with simple '+' icon FAB.
   - Remove 'Rychlé šablony' top bar and integrate template selection directly into an expandable menu / bottom sheet when clicking the '+' FAB (giving options: 'Nová aktivita' or select a template to log).
2. Recurring Card Payment:
   - Auto-record a payment entry for Multisport card with the default cost at the beginning of each month in the database if not present.
3. App Icon:
   - New adaptive icon matching the MultiSport style: deep royal blue background (#2E3CA6), white ring with 'MSL' lettering breaking through the circle.

## Project Brief

# Multisport Logger - Project Brief

## Features
- **Quick Activity & Template Logging:** Streamlined activity creation initiated via a compact '+' floating action button that opens an expandable sheet to log new activities or pick from saved templates.
- **Decluttered Galaxy S23 Mobile UI:** Space-optimized screen real estate with icon-only bottom navigation tabs, a compact search bar, and FAB-padded scrollable lists to prevent overlap.
- **Automated Recurring Card Payment:** Automatic monthly entry creation for the Multisport card subscription at the beginning of each billing cycle in local storage.
- **Compact Value & Usage Analytics:** Refined stats cards tracking visit frequencies, cost offset, and percentage of card value utilized.

## High-Level Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Navigation & Adaptive Strategy:** Jetpack Navigation 3 (state-driven) and Compose Material Adaptive library
- **Concurrency:** Kotlin Coroutines & Flow
- **Architecture:** MVVM with Unidirectional Data Flow (ViewModel, StateFlow)
- **Data Persistence:** Room Database (to store activities, templates, and recurring card payments)

## UI Design Image
![UI Design](C:/Users/mapup/AndroidStudioProjects/multisport_logger/input_images/multisport_logger_ui_design.jpg)
Image path = C:/Users/mapup/AndroidStudioProjects/multisport_logger/input_images/multisport_logger_ui_design.jpg

## Implementation Steps
**Total Duration:** 13h 24m 8s

### Task_1_DataLayerAndDatabase: Implement Room database entities (ActivityLog, ActivityTemplate, MonthlyMembership), DAOs, Repository, and JSON/CSV export/import utilities.
- **Status:** COMPLETED
- **Updates:** Room entities, DAOs, Repository, and DataExportImportUtil built and verified with unit tests and clean assembleDebug.
- **Acceptance Criteria:**
  - Room database configured with entities for ActivityLog, ActivityTemplate, and MonthlyMembership
  - DAOs and Repository implementation ready with Flow/Coroutines support
  - JSON/CSV export and import helper functions created
  - Data layer builds clean
- **Duration:** 8m 36s

### Task_2_ActivityLoggingAndTemplatesUI: Build Jetpack Compose screens for activity visit logging, template quick-fill, and full CRUD operations connected to ViewModel.
- **Status:** COMPLETED
- **Updates:** ActivityLogViewModel, ActivityLogScreen, AddEditActivityLogDialog, and Templates UI created with full CRUD, template quick-fill, search/filter/sort, and unit test coverage. Build and unit tests passed.
- **Acceptance Criteria:**
  - Activity log list screen displaying visit entries with edit/delete actions
  - Add/Edit Activity Log dialog/screen with date, venue, activity, cost fields
  - Quick-fill templates UI to easily record frequent activities
  - ViewModel correctly connected to Repository
- **Duration:** 5m 35s

### Task_3_SubscriptionsAnalyticsAndExportUI: Build Monthly Subscription Tracking, Analytics & Savings Dashboard UI, and integrate JSON/CSV Export features with navigation.
- **Status:** COMPLETED
- **Updates:** Completed Monthly Subscriptions UI, Analytics & Savings Dashboard UI, Data Export/Import UI, and MainScreen Bottom Navigation bar in MainActivity. Verified build assembleDebug and 16 unit tests passed.
- **Acceptance Criteria:**
  - Monthly subscription management UI to record membership fees
  - Analytics dashboard UI showing monthly visit counts, activity expenditure vs subscription cost, net savings, and all-time stats
  - JSON/CSV export/import triggered from settings/UI
  - Navigation and state management connected across all tabs/screens
- **Duration:** 11h 59m 49s

### Task_4_RunAndVerify: Run and verify application stability, confirm alignment with user requirements, ensure no crashes, and instruct critic_agent to verify.
- **Status:** COMPLETED
- **Updates:** Application tested on phone emulator by critic_agent. App launched cleanly, no crashes, all features (Activity logging, Quick templates, Analytics & Savings Dashboard, Subscriptions management, Export/Import, Navigation) verified working properly with clean M3 UI design. All 16 unit tests passed.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Application verified for stability, functionality, and UI completeness
- **Duration:** 4m 53s

### Task_5_CzechLocalizationAndRecurringSubscriptions: Implement Czech language localization (res/values-cs/strings.xml and Kč currency formatting) and default monthly subscription auto-generation with individual month overrides.
- **Status:** COMPLETED
- **Updates:** Implemented full Czech localization (values-cs and values strings.xml), Kč currency formatting throughout all UI components, and recurring default monthly subscription with individual month overrides and persistence. Build and 22 unit tests passed.
- **Acceptance Criteria:**
  - Czech localization strings added in res/values-cs/strings.xml and integrated into UI
  - Currency values formatted with Kč throughout the application
  - Configurable default monthly card subscription fee with auto-generation for current and past months
  - Support for overriding subscription costs on individual months
  - Unit tests updated for recurring logic and currency formatting
- **Duration:** 41m 56s

### Task_6_RunAndVerify: Run and verify application stability, confirm alignment with Czech localization and recurring subscription features, ensure no crashes, and instruct critic_agent to verify.
- **Status:** COMPLETED
- **Updates:** Application successfully built, verified with 22 unit tests passing, deployed to emulator-5554, launched without any crashes or runtime errors, with process running cleanly. All Czech localization, Kč currency formatting, and recurring subscription features verified.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Application verified for stability, functionality, and UI completeness
- **Duration:** 4m 8s

### Task_7_OptimizeGalaxyS23UIAndRecurringPaymentAndAppIcon: Optimize UI for compact screens (Galaxy S23): fix FAB overlaps with list padding, icon-only bottom navigation, compact search bar, replace extended FAB with '+' button opening bottom sheet/menu for templates and new activity, fix analytics & subscription cards alignment/borders, auto-record monthly card payment in DB, and create MultiSport-style app icon with 'MSL' lettering.
- **Status:** COMPLETED
- **Updates:** Completed Galaxy S23 UI decluttering and optimizations (compact search, icon-only bottom nav, FAB padding, '+' FAB opening bottom sheet with templates and manual entry, cleaner subscription & analytics cards alignment), auto-recording of current month card payment on launch, and MultiSport-style 'MSL' app icon. Build and 24 unit tests passed.
- **Acceptance Criteria:**
  - The implemented UI must match the design provided in C:/Users/mapup/AndroidStudioProjects/multisport_logger/input_images/multisport_logger_ui_design.jpg
  - LazyColumn lists include adequate bottom content padding so items are not obscured by the FAB
  - Bottom navigation bar displays icon-only tabs to optimize vertical screen space
  - Compact '+' FAB opens bottom sheet or expandable menu offering 'Nová aktivita' and template logging options, replacing top templates bar
  - Compact search field and fixed styling/alignment for Analytics and Subscription cards
  - Multisport card monthly payment entry automatically recorded at the start of each month if absent
  - MultiSport-style adaptive app icon created with deep royal blue background (#2E3CA6), white ring, and 'MSL' lettering
- **Duration:** 16m 40s

### Task_8_RunAndVerify: Run and verify application stability, confirm alignment with Galaxy S23 optimizations, auto-recurring payments, and app icon, ensure no crashes, and instruct critic_agent to verify.
- **Status:** COMPLETED
- **Updates:** Verified build, all 24 unit tests passing, successfully installed and launched on both physical Samsung Galaxy S23 (SM-S911B) and emulator-5554 with 0 crashes and active processes. All layout decluttering, FAB sheet, recurring monthly payment auto-recording, and 'MSL' app icon verified.
- **Acceptance Criteria:**
  - The implemented UI must match the design provided in C:/Users/mapup/AndroidStudioProjects/multisport_logger/input_images/multisport_logger_ui_design.jpg
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Application verified on device/emulator for UI fidelity, responsiveness, and zero regressions
- **Duration:** 2m 31s

