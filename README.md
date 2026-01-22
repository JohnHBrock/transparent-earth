# Earth X-Ray AR App

An augmented reality Android app that lets you look through the Earth to see what cities and landmarks are on the opposite side of the planet.

## Features

### 🌍 See Through Earth
Point your phone at the ground and the app will show you what's on the other side of the Earth (the antipodal point). Cities, landmarks, and other points of interest are displayed with their names and distances.

### 🔍 Search for Any Location
Search for any city or landmark by name. The app will show you directional arrows guiding you where to point your phone to see that location through the Earth.

### 📍 Real-time AR Overlay
Uses your phone's camera, GPS, compass, and orientation sensors to create a real-time AR experience showing:
- Cities and landmarks visible through the Earth
- Distance information for each location
- Directional guidance arrows when searching
- Interactive crosshair for precise aiming

### 🗺️ Extensive Database
Includes over 100 major cities and famous landmarks worldwide:
- Major cities across all continents
- World capitals
- Famous landmarks (Eiffel Tower, Taj Mahal, Great Wall, etc.)
- Population data for cities

## How It Works

### Antipodal Points
The app calculates the antipodal point of your current location - the point on the opposite side of Earth. When you point your phone at the ground:
1. Your GPS location is determined
2. The antipodal coordinates are calculated (opposite latitude, ±180° longitude)
3. Cities and landmarks near that antipodal point are displayed on your screen

### Orientation Tracking
The app uses your phone's sensors to track:
- **Compass**: Determines which direction you're pointing
- **Accelerometer & Gyroscope**: Detects when you're pointing at the ground
- **Camera**: Provides the live AR view

### Search Mode
When you search for a location:
- Green arrows appear showing you which way to rotate your phone
- The arrows indicate the horizontal direction (left/right)
- Distance to the location is displayed
- When you point in the right direction, the arrow moves to show the exact spot

## Requirements

- Android device with:
  - Android 7.0 (API 24) or higher
  - ARCore support
  - Camera
  - GPS
  - Compass/magnetometer
  - Accelerometer and gyroscope

## Building the App

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 34
- Gradle 8.2+

### Build Instructions

1. Clone the repository:
```bash
git clone <repository-url>
cd transparent-earth
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Connect an Android device or start an emulator

5. Run the app (Shift + F10 or click the Run button)

### Building APK
```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## Usage

1. **Grant Permissions**: When you first launch the app, grant camera and location permissions

2. **Point at Ground**: Point your phone toward the ground (pitch > 45°). You'll see cities and landmarks that are on the opposite side of Earth from your location

3. **Search for Places**:
   - Tap the search bar at the top
   - Type the name of any city or landmark
   - Press the search button or Enter
   - Follow the green arrows to point your phone in the right direction

4. **Clear Search**: Tap the X button to clear your search and return to free exploration mode

## Technology Stack

- **Language**: Kotlin
- **UI**: Android Views with custom Canvas drawing
- **Sensors**: SensorManager (accelerometer, magnetometer)
- **Location**: Google Play Services Location API
- **AR**: Custom AR implementation using camera and sensor fusion
- **Architecture**: MVVM-inspired with data models and utilities

## Key Components

- **MainActivity**: Coordinates camera, sensors, and UI
- **AROverlayView**: Custom view that draws AR markers and arrows
- **OrientationManager**: Manages device orientation sensors
- **LocationTracker**: Handles GPS location updates
- **GeoUtils**: Geographic calculations (antipodal points, distances, bearings)
- **PlacesDatabase**: Database of 100+ cities and landmarks

## Permissions

The app requires:
- `CAMERA`: For the AR view
- `ACCESS_FINE_LOCATION`: To determine your position on Earth
- `INTERNET`: For potential future features

## Future Enhancements

Potential improvements:
- Integration with Google Places API for more locations
- Save favorite locations
- Share screenshots of discovered places
- Night mode
- 3D terrain visualization
- Historical facts about locations
- Distance through Earth's core calculation
- Support for custom location marking

## License

This project is provided as-is for educational and entertainment purposes.

## Credits

Built with Android Studio and Kotlin. Uses Google Play Services for location tracking.
