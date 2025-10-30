# Event / Reminder Feature Implementation

## Overview

This document describes the implementation of the Event/Reminder feature for the Ethiopian Calendar app. The feature allows users to create, view, and manage events and reminders with support for recurring patterns compatible with Google Calendar API.

## Architecture

The implementation follows the existing MVVM architecture pattern used in the app:

```
UI Layer (Compose)
    ↓
ViewModel (EventViewModel)
    ↓
Repository (EventRepository)
    ↓
Data Access Object (EventDao)
    ↓
Room Database (CalendarDatabase)
```

## Database Layer

### 1. Entity: `EventEntity`

**Location**: `data/local/entity/EventEntity.kt`

**Google Calendar Compatibility**:
- `id` → Google Calendar event ID
- `summary` → Event title (Google Calendar's summary field)
- `description` → Event details
- `startTime` / `endTime` → ZonedDateTime with timezone (maps to Google Calendar's start.dateTime/end.dateTime)
- `isAllDay` → Distinguishes between date-only and date-time events
- `timeZone` → IANA timezone identifier (Africa/Addis_Ababa by default)
- `recurrenceRule` → RRULE string format (RFC 5545)
- `googleCalendarEventId` → For bidirectional sync
- `googleCalendarId` → Google Calendar identifier (e.g., "primary")

**Ethiopian Calendar Support**:
- `ethiopianYear`, `ethiopianMonth`, `ethiopianDay` fields for local display

### 2. Recurrence Rules

**Location**: `data/local/entity/RecurrenceRuleEntity.kt`

**RRULE Format** (iCalendar RFC 5545):
```
RRULE:FREQ=WEEKLY;BYDAY=TU,TH                    // Weekly on Tuesday & Thursday, no end
RRULE:FREQ=WEEKLY;BYDAY=MO;UNTIL=20251231T235959Z // Weekly on Monday until date
RRULE:FREQ=DAILY;COUNT=10                        // Daily for 10 occurrences
```

**Features**:
- `RecurrenceFrequency`: NONE, DAILY, WEEKLY, MONTHLY, YEARLY
- `weekDays`: Set of DayOfWeek for weekly recurrence
- `RecurrenceEndOption`: NEVER, UNTIL (date), COUNT (occurrences)
- `toRRuleString()`: Converts RecurrenceRule to RRULE format
- `parseRRule()`: Parses RRULE string back to RecurrenceRule object

### 3. Data Access Object: `EventDao`

**Location**: `data/local/dao/EventDao.kt`

**Key Features**:
- All read operations return `Flow<T>` for reactive UI updates
- Comprehensive query methods:
  - By Ethiopian date/month/year
  - By Gregorian date range
  - By category
  - Search by title/description
  - Upcoming events with limit
- Support for recurring events
- Sync status tracking

**Example Queries**:
```kotlin
fun getAllEventsFlow(): Flow<List<EventEntity>>
fun getEventsForDate(year: Int, month: Int, day: Int): Flow<List<EventEntity>>
fun getEventsInRange(startTimeMillis: Long, endTimeMillis: Long): Flow<List<EventEntity>>
fun searchEvents(query: String): Flow<List<EventEntity>>
```

### 4. Database: `CalendarDatabase`

**Location**: `data/local/CalendarDatabase.kt`

- Room version: 1
- TypeConverter: `DateConverter` for ZonedDateTime ↔ ISO-8601 string
- Migration strategy: `fallbackToDestructiveMigration()` (development)

## Repository Layer

### EventRepository

**Location**: `data/repository/EventRepository.kt`

**Responsibilities**:
- Provides clean API for ViewModel
- Handles recurring event instance generation
- Converts EventEntity to EventInstance for UI consumption
- Future: Google Calendar sync logic

**Key Methods**:
```kotlin
fun getAllEvents(): Flow<List<EventEntity>>
fun getEventsForMonth(year: Int, month: Int): Flow<List<EventInstance>>
fun getEventsInRange(start: ZonedDateTime, end: ZonedDateTime): Flow<List<EventInstance>>
suspend fun createEvent(event: EventEntity): Long
suspend fun updateEvent(event: EventEntity): Int
suspend fun deleteEventById(eventId: String): Int
```

**Recurring Event Instance Generation**:
- Parses RRULE string to extract pattern
- Generates instances within date range
- Respects recurrence end date or "never ends"
- Currently implements WEEKLY frequency with BYDAY

## ViewModel Layer

### EventViewModel

**Location**: `ui/event/EventViewModel.kt`

**Features**:
- Uses `@HiltViewModel` for dependency injection
- Exposes `StateFlow<EventUiState>` for reactive UI
- Handles dialog state management
- Error handling with Timber logging

**UI State**:
```kotlin
sealed class EventUiState {
    data object Loading
    data class Success(events: List<EventInstance>, isDialogOpen: Boolean)
    data class Error(message: String)
}
```

**Key Methods**:
```kotlin
fun showAddEventDialog()
fun hideAddEventDialog()
fun createEvent(...)
fun deleteEvent(eventId: String)
```

## UI Layer

### 1. EventScreen

**Location**: `ui/event/EventScreen.kt`

**Components**:
- `FloatingActionButton` for adding events
- `LazyColumn` for event list
- `EventCard` for each event with:
  - Color indicator
  - Title, date/time, description
  - Recurring event badge
  - Reminder indicator
  - Delete button with confirmation dialog
- `EmptyEventsPlaceholder` when no events exist
- `ErrorMessage` for error states

### 2. AddEventDialog

**Location**: `ui/event/AddEventDialog.kt`

**Input Fields**:
1. **Title** (required)
2. **Description** (optional)
3. **Date Picker** (Material DatePickerDialog)
4. **All-Day Toggle**
5. **Start Time Picker** (Material TimePickerDialog, 24-hour format)
6. **End Time Picker** (optional)
7. **Recurrence Pattern**:
   - No repeat / Weekly
   - Weekday selection (M T W T F S S)
   - End option: Never / Until date
8. **Reminder**:
   - Toggle on/off
   - Options: At time, 5/15/30 min before, 1 hour before
9. **Category**: PERSONAL, WORK, RELIGIOUS, BIRTHDAY, ANNIVERSARY

**UI Features**:
- Scrollable dialog for small screens
- Form validation (title required)
- Conditional fields (time pickers only shown if not all-day)
- Visual weekday selector with circular chips

## Dependency Injection

### Hilt Module Updates

**Location**: `di/AppModule.kt`

**Provided Dependencies**:
```kotlin
@Provides @Singleton
fun provideCalendarDatabase(@ApplicationContext context: Context): CalendarDatabase

@Provides @Singleton
fun provideEventDao(database: CalendarDatabase): EventDao

@Provides @Singleton
fun provideEventRepository(eventDao: EventDao): EventRepository
```

## Time Zone Handling

### Best Practices Implemented:

1. **Storage**: All times stored as `ZonedDateTime` with timezone info
   - Preserved as ISO-8601 strings in database
   - Example: `2025-10-30T14:30:00+03:00[Africa/Addis_Ababa]`

2. **Default Timezone**: `Africa/Addis_Ababa` (Ethiopian timezone)

3. **Why ZonedDateTime**:
   - Handles daylight saving time changes
   - Preserves timezone for Google Calendar sync
   - Allows conversion between timezones
   - More accurate than epoch timestamps

4. **Future Considerations**:
   - Support for events in different timezones
   - Automatic timezone conversion when traveling
   - Respect user's current timezone preference

## Recurring Events

### Current Implementation:

**WEEKLY Recurrence**:
```kotlin
// Example: Every Tuesday and Thursday
val rule = RecurrenceRule(
    frequency = RecurrenceFrequency.WEEKLY,
    weekDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
    endOption = RecurrenceEndOption.NEVER
)

// Generates RRULE: "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
```

**Instance Generation Algorithm**:
1. Parse RRULE to extract frequency and weekdays
2. Iterate through date range
3. For each date matching a selected weekday:
   - Create EventInstance with same time as original
   - Include reference to original EventEntity
4. Stop at recurrence end date or range end

### Future Enhancements:

- **MONTHLY** recurrence (e.g., "2nd Tuesday of month")
- **YEARLY** recurrence (e.g., birthdays, anniversaries)
- **Custom intervals** (e.g., "every 2 weeks")
- **BYMONTHDAY** support
- **Exception dates** (EXDATE) for skipping specific instances

## Updating/Deleting Recurring Events

### Best Practices for Implementation:

#### Option 1: Single Instance Update
- Create new EventEntity with EXDATE for original
- Add new single event for the modified instance
- Complexity: Medium

#### Option 2: "This and Future" Update
- Update original event's UNTIL date to before modification
- Create new recurring event starting from modification date
- Complexity: Medium

#### Option 3: Delete All Instances
- Simply delete the EventEntity with the recurrence rule
- All instances disappear (current implementation)
- Complexity: Low

### Recommended UI Flow:
```
When deleting recurring event, show dialog:
┌────────────────────────────────────┐
│ This is a recurring event          │
│                                    │
│ [ ] This event only               │
│ [ ] This and future events        │
│ [ ] All events in the series      │
│                                    │
│      [Cancel]  [Delete]           │
└────────────────────────────────────┘
```

## Notifications / Alarms (Future Implementation)

### Recommended Approach: WorkManager

**Why WorkManager**:
- Guaranteed execution even after device reboot
- Battery-optimized
- Handles Doze mode automatically
- Works with JobScheduler, AlarmManager internally

**Implementation Steps**:

1. **Create NotificationWorker**:
```kotlin
class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getString("event_id") ?: return Result.failure()
        // Fetch event from database
        // Show notification
        return Result.success()
    }
}
```

2. **Schedule Work When Event Created**:
```kotlin
fun scheduleReminder(event: EventEntity) {
    val delay = calculateDelayUntilReminder(event)

    val workRequest = OneTimeWorkRequestBuilder<EventReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("event_id" to event.id))
        .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork(
            "reminder_${event.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
}
```

3. **Handle Recurring Events**:
- Schedule next N instances (e.g., next 10 occurrences)
- When notification fires, schedule next instance
- Cancel all when event deleted

4. **Notification Channels**:
```kotlin
val channel = NotificationChannel(
    "event_reminders",
    "Event Reminders",
    NotificationManager.IMPORTANCE_HIGH
)
```

5. **Boot Receiver** (already exists: `BootCompleteReceiver.kt`):
```kotlin
class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all upcoming event reminders
        }
    }
}
```

## Google Calendar Integration (Future)

### Sync Strategy:

1. **Initial Sync**:
   - Fetch events from Google Calendar API
   - Store in local database with `googleCalendarEventId`
   - Mark as `isSynced = true`

2. **Two-Way Sync**:
   - Local changes: Push to Google Calendar, update `syncedAt`
   - Remote changes: Poll Google Calendar, update local database
   - Conflict resolution: Last-write-wins or user prompt

3. **RRULE Compatibility**:
   - Our RRULE format matches Google Calendar's
   - Direct mapping: `EventEntity.recurrenceRule` → Google Calendar's `recurrence` array
   - Example API payload:
   ```json
   {
     "summary": "Team Meeting",
     "start": {
       "dateTime": "2025-10-30T14:00:00+03:00",
       "timeZone": "Africa/Addis_Ababa"
     },
     "end": {
       "dateTime": "2025-10-30T15:00:00+03:00",
       "timeZone": "Africa/Addis_Ababa"
     },
     "recurrence": [
       "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
     ]
   }
   ```

4. **Sync Worker**:
   - Use WorkManager with PeriodicWorkRequest
   - Sync every 1-4 hours
   - Handle network failures gracefully

## Data Model Extensibility

The current schema supports easy addition of:

### 1. Attachments
```kotlin
@Entity(tableName = "event_attachments")
data class EventAttachment(
    @PrimaryKey val id: String,
    val eventId: String,
    val fileUri: String,
    val mimeType: String
)
```

### 2. Attendees
```kotlin
@Entity(tableName = "event_attendees")
data class EventAttendee(
    @PrimaryKey val id: String,
    val eventId: String,
    val email: String,
    val responseStatus: String // ACCEPTED, DECLINED, TENTATIVE
)
```

### 3. Location
- Add `location: String?` to EventEntity
- Maps to Google Calendar's `location` field

### 4. Conference Data
- For Google Meet / Zoom integration
- Add `conferenceData: String?` (JSON)

## Testing Recommendations

### Unit Tests:

1. **RecurrenceRule Tests**:
   - Test RRULE string generation
   - Test RRULE parsing
   - Test edge cases (invalid input)

2. **Repository Tests**:
   - Test instance generation for weekly recurrence
   - Test instance generation with end date
   - Mock EventDao

3. **ViewModel Tests**:
   - Test state transitions
   - Test event creation with validation
   - Test error handling

### UI Tests (Compose):

1. **EventScreen Tests**:
   - Test empty state display
   - Test event list rendering
   - Test FAB click opens dialog

2. **AddEventDialog Tests**:
   - Test form validation (title required)
   - Test weekday selector interaction
   - Test date/time picker integration

## String Resources

All user-facing strings are externalized in `res/values/strings.xml`:
- Event feature strings: lines 161-199
- Supports internationalization (future: `strings-am.xml` for Amharic)

## Performance Considerations

1. **Database Queries**:
   - Indexed on `startTime`, `ethiopianYear`, `ethiopianMonth`, `ethiopianDay`
   - Use Flow to avoid unnecessary database hits
   - Paging for large event lists (future)

2. **Recurring Instance Generation**:
   - Only generate instances within visible range
   - Cache generated instances in memory
   - Limit to reasonable range (e.g., 1 year forward)

3. **UI Optimization**:
   - LazyColumn for efficient list rendering
   - Keys for stable list items
   - remember {} for expensive calculations

## Summary

This implementation provides:

✅ **Complete CRUD** for events and reminders
✅ **Google Calendar-compatible** data model
✅ **Recurring events** with RRULE support
✅ **Timezone-aware** date/time handling
✅ **Clean architecture** following app patterns
✅ **Reactive UI** with Flow and StateFlow
✅ **Extensible schema** for future features
✅ **Material Design 3** UI components
✅ **Internationalization** support

The foundation is solid for:
- 🔔 Notification scheduling with WorkManager
- ☁️ Google Calendar sync
- 📎 Attachments and attendees
- 🌍 Multi-timezone support
- 🔍 Advanced search and filtering

---

**Implementation Date**: October 30, 2025
**Author**: Claude (Anthropic)
**Framework**: Jetpack Compose, Room, Hilt, Material 3
