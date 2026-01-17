# 🔴 URGENT: Create Firestore Indexes

## Why Screens Are Empty

Your queries need **composite indexes** that don't exist yet. Firestore returns empty results when indexes are missing.

## Solution: Create Indexes in Firebase Console

### Method 1: Automatic (Easiest) ⭐

1. **Run your app** and open the problematic screens (History/Logs)
2. **Open Logcat** in Android Studio (View → Tool Windows → Logcat)
3. **Filter by**: `FirestoreRepository` or `HistoryViewModel`
4. Look for an error like:
   ```
   The query requires an index. You can create it here: https://console.firebase.google.com/...
   ```
5. **Click the link** in the error message
6. Firebase Console will open with pre-filled index settings
7. Click **"Create Index"**
8. **Wait 2-5 minutes** for it to build
9. **Restart your app**

### Method 2: Manual Creation

Go to: [Firebase Console](https://console.firebase.google.com/) → Your Project → Firestore Database → Indexes

#### Index 1: Driver History

- **Collection ID**: `parkingSessions`
- **Fields to index**:
  1. `driverId` → Ascending
  2. `status` → Ascending
  3. `entryTime` → Descending
- Click **Create Index**

#### Index 2: Security Logs

- **Collection ID**: `parkingSessions`
- **Fields to index**:
  1. `scannedByGuardId` → Ascending
  2. `entryTime` → Descending
- Click **Create Index**

## How to Verify It's Working

### Check Logcat:

```
HistoryViewModel: Fetching sessions for driver: [ID] with filter: ALL
HistoryViewModel: Fetched 1 sessions
```

or

```
FirestoreRepository: Fetched 1 sessions for driver [ID]
```

### If you see 0 sessions but data exists:

- Indexes are still building (wait a few more minutes)
- Or index creation failed - check Firebase Console → Indexes tab

## After Creating Indexes:

1. **Wait for "Index Status: Enabled"** in Firebase Console
2. **Restart your app** completely (force close and reopen)
3. **Navigate to History/Logs screens**
4. Data should now appear! ✅

## Still Not Working?

Check Logcat for:

- `Error fetching sessions` - Shows the actual error
- User ID mismatches
- Network connectivity issues
