# VaultPark - VIP Parking Management System Architecture

## Project Structure Overview

```
com/kushan/vaultpark/
├── ui/                          # User Interface Layer
│   ├── screens/                 # Screen composables
│   │   ├── HomeScreen.kt       # QR Code display screen
│   │   ├── HistoryScreen.kt    # Parking history/logs
│   │   ├── BillingScreen.kt    # Monthly invoices
│   │   └── ProfileScreen.kt    # User settings & info
│   ├── navigation/              # Navigation setup
│   │   ├── NavScreen.kt        # Route definitions
│   │   ├── NavHost.kt          # Navigation graph
│   │   └── BottomNavigation.kt # Bottom nav bar
│   ├── components/              # Reusable composables
│   ├── theme/                   # Material3 theme
│   │   ├── Color.kt            # Color palette
│   │   ├── Type.kt             # Typography (Poppins)
│   │   └── Theme.kt            # Theme composition
│   └── ...
├── viewmodel/                   # ViewModel Layer
│   └── UserViewModel.kt        # User state management
├── model/                       # Data Models
│   └── Models.kt               # Data classes
├── data/                        # Data Layer (Repository Pattern)
│   └── ParkingRepository.kt    # Data access abstraction
├── utils/                       # Utility Functions
│   └── Utils.kt                # Date, Currency, Validation
└── MainActivity.kt             # App entry point
```

## Architecture Layers

### 1. **UI Layer** (`ui/`)

Composable functions organized by screen and component:

- **screens/**: Full-screen composables with TopAppBar and content
- **navigation/**: Navigation routes and handlers
- **components/**: Reusable UI components
- **theme/**: Material3 design configuration

### 2. **ViewModel Layer** (`viewmodel/`)

Manages UI state and business logic:

- `UserViewModel`: Handles user data and preferences
- Uses `StateFlow` for reactive state management
- Survives configuration changes

### 3. **Model Layer** (`model/`)

Data class definitions:

- `ParkingSession`: Entry/exit records
- `Invoice`: Monthly billing information
- `User`: User profile data

### 4. **Data Layer** (`data/`)

Repository pattern for data access:

- `ParkingRepository`: Interface for data operations
- `DefaultParkingRepository`: Implementation
- Single source of truth for data

### 5. **Utils Layer** (`utils/`)

Utility functions:

- `DateUtils`: Date/time formatting
- `CurrencyUtils`: Currency formatting
- `ValidationUtils`: Input validation

## Design System

### Color Palette

- **Primary**: Deep Blue (#1A237E)
- **Secondary**: Purple Accent (#7C4DFF)
- **Background**: Dark Theme (#121212)
- **Surface**: #1E1E1E
- **Error**: #CF6679
- **Success**: #4CAF50

### Typography

- **Font Family**: Poppins (Regular, Medium, SemiBold, Bold)
- **Scale**: Material3 standard typography
- Material3 text styles: Display, Headline, Title, Body, Label

### Components

- Material3 design system
- Card elevations for depth
- TopAppBar with navigation support
- Bottom navigation with 4 screens
- Icon support (Material Icons Extended)

## Key Features

### Navigation Flow

1. **Home Screen**: Displays parking QR code for entry/exit
2. **History Screen**: Shows parking session logs with timestamps
3. **Billing Screen**: Displays monthly invoices and payment status
4. **Profile Screen**: User information and app settings

### Bottom Navigation

- 4-tab navigation bar for seamless switching
- Persistent state while navigating
- Material3 styled icons and labels
- Active tab highlighting

## State Management

### UI State (ViewModel)

```kotlin
data class UserUiState(
    val userName: String,
    val email: String,
    val isLoading: Boolean,
    val errorMessage: String?
)
```

### Data Flow

```
MainActivity (Composable App)
    ↓
VaultParkApp (Navigation setup)
    ↓
VaultParkNavHost (Route handling)
    ↓
Screen Composables (UI Rendering)
    ↓
ViewModels (State management)
    ↓
Repository (Data access)
    ↓
Database/API (Data source)
```

## How to Extend

### Adding a New Screen

1. Create composable in `ui/screens/`
2. Add `NavScreen` object in `NavScreen.kt`
3. Add route to `NavHost` in `NavHost.kt`
4. Add bottom nav item in `BottomNavigation.kt`

### Adding State Management

1. Create new ViewModel in `viewmodel/`
2. Define UI state data class in same file
3. Use `StateFlow` for reactive updates
4. Inject into composables via parameters

### Adding Data Models

1. Create data classes in `model/Models.kt`
2. Add repository methods in `data/ParkingRepository.kt`
3. Update repository implementation with actual logic

## Dependencies

### Core Compose

- `androidx.compose:compose-bom` - Version coordination
- `androidx.compose.ui` - Core UI components
- `androidx.compose.material3` - Material3 design
- `androidx.compose.animation` - Animations

### Navigation

- `androidx.navigation:navigation-compose` - Compose navigation

### Lifecycle

- `androidx.lifecycle:lifecycle-viewmodel-compose` - ViewModel integration
- `androidx.activity:activity-compose` - Activity Compose integration

### Icons

- `androidx.compose.material:material-icons-extended` - Extended icon set

## Future Enhancements

- [ ] Database integration (Room)
- [ ] Network API integration (Retrofit)
- [ ] QR code generation library
- [ ] Push notifications
- [ ] Payment gateway integration
- [ ] Real-time location tracking
- [ ] Multi-language support
- [ ] Dark/Light theme toggle
