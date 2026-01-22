# Earth X-Ray AR App

An augmented reality Android app that lets you look through the Earth to see what cities and landmarks are in any direction - as if Earth were transparent.

## Features

### 🌍 See Through Earth in ANY Direction
Point your phone at the ground in any direction and the app will show you what's on the other side of the Earth along that ray. Whether you're looking at nearby cities through the crust or distant continents through the core, the app calculates and displays what you'd see if Earth were transparent.

**Examples:**
- In Chicago pointing slightly east → See Gary, IN through the Earth
- In Chicago pointing toward the Atlantic → See NYC through the Earth
- In Chicago pointing straight down → See your antipodal point in the Indian Ocean
- In NYC pointing toward Europe → See London, Paris, or other European cities

### 🔍 Search for Any Location
Search for any city or landmark by name. The app calculates the exact direction to point your phone (both horizontal and vertical) and shows directional arrows guiding you to that location through the Earth.

### 📍 Real-time AR Overlay
Uses your phone's camera, GPS, compass, and orientation sensors to create a real-time AR experience showing:
- Cities and landmarks visible along your viewing ray through the Earth
- Distance information for each location
- 3D directional guidance arrows when searching (horizontal AND vertical)
- Interactive crosshair for precise aiming

### 🗺️ Extensive Database
Includes over 100 major cities and famous landmarks worldwide:
- Major cities across all continents
- World capitals
- Famous landmarks (Eiffel Tower, Taj Mahal, Great Wall, etc.)
- Population data for cities

## How It Works

### Ray-Through-Earth Calculation
The app calculates a ray from your position through the Earth based on where you're pointing:
1. Your GPS location is determined (your position on Earth's surface)
2. Your phone's orientation is tracked (azimuth/compass direction + pitch/tilt angle)
3. A ray is calculated from your position through Earth's interior in that direction
4. The exit point where the ray emerges on the other side is calculated
5. Cities and landmarks near that exit point are displayed on your screen with AR markers

### Real-Time Updates
As you move your phone:
- The viewing ray updates in real-time
- Different cities appear as you sweep across the ground
- Markers stay locked to their geographic positions in 3D space

### Orientation Tracking
The app uses your phone's sensors to track:
- **Compass (Magnetometer)**: Determines which horizontal direction you're pointing (North, East, etc.)
- **Accelerometer & Gyroscope**: Measures the tilt angle (how far down you're pointing)
- **Sensor Fusion**: Combines both to create a precise 3D pointing direction
- **Camera**: Provides the live AR view

### Search Mode
When you search for a location:
- The app calculates the exact 3D direction to point to see that location through Earth
- Green arrows appear showing you which way to rotate and tilt your phone
- Arrows indicate both horizontal direction (left/right) and vertical angle (up/down)
- Distance to the location is displayed
- When you point in the right direction, the arrow appears on screen showing the exact spot

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

2. **Free Exploration Mode**:
   - Point your phone toward the ground in any direction
   - Sweep left and right to scan different directions through Earth
   - Tilt your phone to different angles to look through Earth at various trajectories
   - Cities and landmarks will appear with AR markers showing their names and distances
   - Watch as different locations appear based on where you're pointing

3. **Search for Specific Places**:
   - Tap the search bar at the top
   - Type the name of any city or landmark (e.g., "Tokyo", "Eiffel Tower", "Sydney")
   - Press the search button or Enter
   - Follow the green directional arrows:
     - Arrows at screen edges guide you to rotate your phone horizontally (left/right)
     - Arrows also indicate if you need to tilt up or down
     - The angle indicator shows how far off you are
   - When you point correctly, the arrow will appear on screen showing the exact location

4. **Clear Search**: Tap the X button to clear your search and return to free exploration mode

5. **Tips**:
   - Works best when pointing at least 10° below horizontal
   - Nearby places (like neighboring cities) require pointing at shallow angles
   - Distant places (like other continents) require pointing at steeper angles toward the ground
   - The straight-down direction shows your antipodal point (exact opposite side of Earth)

## Technology Stack

- **Language**: Kotlin
- **UI**: Android Views with custom Canvas drawing
- **Sensors**: SensorManager (accelerometer, magnetometer)
- **Location**: Google Play Services Location API
- **AR**: Custom AR implementation using camera and sensor fusion
- **Architecture**: MVVM-inspired with data models and utilities

## Key Components

- **MainActivity**: Coordinates camera, sensors, and UI
- **AROverlayView**: Custom view that draws AR markers, pins, and directional arrows with 3D screen projection
- **OrientationManager**: Manages device orientation sensors (compass, accelerometer, gyroscope) with sensor fusion
- **LocationTracker**: Handles real-time GPS location updates
- **GeoUtils**: Advanced geographic calculations including:
  - Ray-through-Earth calculations based on 3D orientation
  - Exit point computation where ray emerges on other side
  - Direction calculations (azimuth + pitch) to point at any location through Earth
  - Distance and bearing calculations
  - 3D vector mathematics for sphere geometry
- **PlacesDatabase**: Searchable database of 100+ cities and landmarks worldwide

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
