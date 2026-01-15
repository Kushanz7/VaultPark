package com.kushan.vaultpark.config

/**
 * Firebase Setup Instructions for VaultPark
 *
 * STEP 1: Firebase Project Creation
 * ================================
 * 1. Go to https://console.firebase.google.com/
 * 2. Click "Create Project" or select existing project
 * 3. Enter project name: "VaultPark"
 * 4. Accept default settings and create project
 * 5. Wait for project to be created
 *
 * STEP 2: Add Android App to Firebase
 * ==================================
 * 1. In Firebase Console, click "Add app" > Android
 * 2. Enter Package name: com.kushan.vaultpark
 * 3. Enter App nickname: VaultPark (optional)
 * 4. Click "Register app"
 * 5. Download google-services.json file
 * 6. Place it in your app/ folder (android/app/)
 * 7. Follow the gradle setup instructions (already added to build.gradle.kts)
 * 8. Click "Next" and then "Continue to console"
 *
 * STEP 3: Enable Firebase Authentication
 * =======================================
 * 1. In Firebase Console, go to "Authentication" (under Build)
 * 2. Click "Get started"
 * 3. Select "Email/Password" sign-in method
 * 4. Enable it and click "Save"
 *
 * STEP 4: Create Firestore Database
 * =================================
 * 1. In Firebase Console, go to "Firestore Database"
 * 2. Click "Create database"
 * 3. Select "Start in production mode"
 * 4. Choose location (e.g., us-central1) and click "Create"
 * 5. Update Firestore Rules for testing (see STEP 6)
 *
 * STEP 5: Firestore Security Rules (For Development/Testing)
 * ==========================================================
 * Go to Firestore > Rules and replace with:
 *
 * rules_version = '2';
 * service cloud.firestore {
 *   match /databases/{database}/documents {
 *     match /{document=**} {
 *       allow read, write: if request.auth != null;
 *     }
 *   }
 * }
 *
 * IMPORTANT: Update these rules for production security!
 * See Firestore documentation for proper security rules.
 *
 * STEP 6: Create Test Users in Firebase Console
 * =============================================
 * Go to Authentication > Users and create:
 *
 * Drivers:
 * --------
 * Email: john.driver@vaultpark.com
 * Password: Driver123!
 *
 * Email: sarah.vip@vaultpark.com
 * Password: Driver123!
 *
 * Security Guards:
 * ----------------
 * Email: guard1@vaultpark.com
 * Password: Guard123!
 *
 * Email: guard2@vaultpark.com
 * Password: Guard123!
 *
 * STEP 7: Create Firestore User Documents
 * ========================================
 * Go to Firestore > Data and create these documents manually or run initialization code:
 *
 * Collection: "users"
 *
 * Document: john.driver@vaultpark.com (use email as doc ID or use UID)
 * {
 *   id: "user_id_from_auth",
 *   email: "john.driver@vaultpark.com",
 *   name: "John Smith",
 *   role: "DRIVER",
 *   vehicleNumber: "CAR-001",
 *   membershipType: "Gold",
 *   createdAt: timestamp,
 *   updatedAt: timestamp
 * }
 *
 * Document: sarah.vip@vaultpark.com
 * {
 *   id: "user_id_from_auth",
 *   email: "sarah.vip@vaultpark.com",
 *   name: "Sarah Johnson",
 *   role: "DRIVER",
 *   vehicleNumber: "SUV-202",
 *   membershipType: "Platinum",
 *   createdAt: timestamp,
 *   updatedAt: timestamp
 * }
 *
 * Document: guard1@vaultpark.com
 * {
 *   id: "user_id_from_auth",
 *   email: "guard1@vaultpark.com",
 *   name: "Mike Wilson",
 *   role: "SECURITY",
 *   gateLocation: "Main Entrance",
 *   createdAt: timestamp,
 *   updatedAt: timestamp
 * }
 *
 * Document: guard2@vaultpark.com
 * {
 *   id: "user_id_from_auth",
 *   email: "guard2@vaultpark.com",
 *   name: "Lisa Chen",
 *   role: "SECURITY",
 *   gateLocation: "Exit Gate A",
 *   createdAt: timestamp,
 *   updatedAt: timestamp
 * }
 *
 * STEP 8: Firestore Collection Structure
 * ======================================
 *
 * Collection: users
 * -----------------
 * Documents contain user profiles with authentication data
 *
 * Collection: parkingSessions
 * ---------------------------
 * Documents contain parking session records
 *
 * STEP 9: Test the Setup
 * =====================
 * 1. Run the app on an emulator or physical device
 * 2. Login with: john.driver@vaultpark.com / Driver123!
 * 3. Check Firebase Console > Authentication to see active session
 * 4. Check Firestore > Database to see user document reads/writes
 *
 * TROUBLESHOOTING
 * ==============
 * 1. "google-services.json not found" error:
 *    - Make sure google-services.json is in app/ folder
 *    - Check build.gradle.kts has plugin id("com.google.gms.google-services")
 *
 * 2. "Permission denied" errors:
 *    - Update Firestore security rules (STEP 5)
 *    - Ensure user is authenticated
 *
 * 3. "User not found" during login:
 *    - Create user in Firebase Console > Authentication first
 *    - Then create Firestore user document
 *    - Make sure email matches exactly
 *
 * 4. "Firestore database not found":
 *    - Go to Firestore Database and click "Create database"
 *    - Select production mode and appropriate region
 *
 * 5. Dependencies not resolving:
 *    - Invalidate cache and restart Android Studio
 *    - Check build.gradle.kts has correct Firebase BOM version
 *    - Sync Gradle files again
 *
 * PRODUCTION CHECKLIST
 * ===================
 * Before deploying to production:
 * ☐ Update Firestore security rules (secure mode)
 * ☐ Enable Firebase Analytics
 * ☐ Set up Cloud Messaging (for notifications)
 * ☐ Configure App Check for security
 * ☐ Set up proper error logging
 * ☐ Test all authentication flows
 * ☐ Implement proper input validation
 * ☐ Add rate limiting for API calls
 * ☐ Enable backup and recovery procedures
 * ☐ Review and monitor Firebase usage/costs
 */
