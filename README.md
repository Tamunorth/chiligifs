# ChiliGIF - A Modern Android GIF Search App

ChiliGIF is a modern Android application built with Jetpack Compose that allows users to search and browse GIFs from the Giphy API. The app demonstrates best practices in Android development, including MVVM architecture, dependency injection with Hilt, and comprehensive testing.

## 🎯 Features

- **GIF Search**: Search for GIFs with auto-search (debounced input)
- **Infinite Scrolling**: Seamless pagination with Paging 3 library
- **Detail View**: View full-size GIFs with detailed information
- **Network Monitoring**: Real-time network connectivity status
- **Beautiful UI**: Modern Material 3 design with smooth animations
- **Offline Support**: Graceful error handling and retry mechanisms
- **Orientation Support**: Adaptive grid layout for different screen sizes

## 🏗️ Architecture

The app follows a **Single-Activity Architecture** with **MVVM (Model-View-ViewModel)** pattern:

### Layers

1. **View (UI) Layer**

    - `MainActivity.kt`: Single entry point with Navigation Compose
   - `SearchScreen.kt`: Grid view of GIF search results
   - `DetailScreen.kt`: Detailed view of individual GIFs
   - `NetworkStatusBanner.kt`: Network connectivity indicator

2. **ViewModel Layer**

    - `SearchViewModel.kt`: Manages search state with debounced auto-search
   - `DetailViewModel.kt`: Manages detail screen state

3. **Data Layer**

    - `GiphyRepository.kt`: Single source of truth for data access
   - `GiphyPagingSource.kt`: Paging 3 integration for infinite scrolling
   - `GiphyApiService.kt`: Retrofit interface for Giphy API

4. **Dependency Injection**
   - Hilt for dependency injection throughout the app

## 🛠️ Tech Stack

- **UI**: Jetpack Compose, Material 3
- **Architecture**: MVVM, Single-Activity
- **Navigation**: Navigation Compose
- **Networking**: Retrofit, Moshi, OkHttp
- **Image Loading**: Coil with GIF support
- **Pagination**: Paging 3
- **Dependency Injection**: Hilt
- **Coroutines**: Kotlin Coroutines & Flow
- **Testing**: JUnit, MockK, Turbine, Coroutines Test

## 📋 Requirements

- Android Studio Hedgehog or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36
- Kotlin 2.0.21

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/chiligif.git
cd chiligif
```

### 2. Get a Giphy API Key

1. Go to [Giphy Developers](https://developers.giphy.com/)
2. Create an account and create a new app
3. Copy your API key

### 3. Configure the API Key

Add your API key to `local.properties` (this file is already in `.gitignore` and will NOT be committed):

```properties
GIPHY_API_KEY=your_api_key_here
```

**Important Security Notes:**

- ✅ A template file `local.properties.template` is provided for reference

### 4. Build and Run

Open the project in Android Studio and run it on an emulator or physical device.

## 📱 App Structure

```
com.example.chiligif/
├── data/
│   ├── api/
│   │   └── GiphyApiService.kt
│   ├── model/
│   │   ├── GifDto.kt
│   │   ├── SearchResponseDto.kt
│   │   └── SingleGifResponseDto.kt
│   ├── paging/
│   │   └── GiphyPagingSource.kt
│   └── repository/
│       └── GiphyRepository.kt
├── di/
│   ├── AppModule.kt
│   └── NetworkModule.kt
├── ui/
│   ├── components/
│   │   └── NetworkStatusBanner.kt
│   ├── screen/
│   │   ├── DetailScreen.kt
│   │   └── SearchScreen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/
│       ├── DetailViewModel.kt
│       └── SearchViewModel.kt
├── util/
│   └── NetworkMonitor.kt
├── ChiligifApplication.kt
└── MainActivity.kt
```

## 🧪 Testing

The app includes comprehensive unit tests:

- **SearchViewModelTest**: Tests for search functionality and debouncing
- **DetailViewModelTest**: Tests for detail screen state management
- **GiphyPagingSourceTest**: Tests for pagination logic
- **GiphyRepositoryTest**: Tests for data layer

Run tests with:

```bash
./gradlew test
```

## 🎨 UI Features

### Search Screen

- Clean search interface with Material 3 design
- Real-time search with 500ms debounce
- Adaptive grid layout (GridCells.Adaptive)
- Loading states and error handling
- Smooth animations and transitions

### Detail Screen

- Full-size GIF display
- Detailed information including:
  - Title
  - Creator username
  - Rating
  - Dimensions
  - File size
  - Upload date
  - Source URL

### Network Status

- Real-time network connectivity monitoring
- Banner notification when offline
- Automatic updates when connection restored

## 🔑 Key Implementation Details

### Auto-Search with Debouncing

```kotlin
val gifs: Flow<PagingData<GifDto>> = _searchQuery
    .debounce(500L) // Wait 500ms after user stops typing
    .filter { it.isNotBlank() }
    .flatMapLatest { query ->
        repository.getSearchStream(query)
    }
    .cachedIn(viewModelScope)
```

### Pagination with Paging 3

```kotlin
fun getSearchStream(query: String): Flow<PagingData<GifDto>> {
    return Pager(
        config = PagingConfig(
            pageSize = 20,
            initialLoadSize = 40,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            GiphyPagingSource(apiService, apiKey, query)
        }
    ).flow
}
```

### Network Monitoring

```kotlin
override val isConnected: Flow<Boolean> = callbackFlow {
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)
        }
        override fun onLost(network: Network) {
            trySend(false)
        }
    }
    // ... implementation
}.distinctUntilChanged()
```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👏 Acknowledgments

- [Giphy API](https://developers.giphy.com/) for providing the GIF data
- [Android Jetpack](https://developer.android.com/jetpack) for the excellent libraries
- [Coil](https://coil-kt.github.io/coil/) for image loading

## 📞 Contact

For questions or feedback, please open an issue on GitHub.

---

Built with ❤️ using Jetpack Compose
