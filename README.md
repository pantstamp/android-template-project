# Android Template Project

This repository contains a template project that demonstrates how to implement a modern Android app. The project is written entirely in Kotlin, follows a multi-module structure, and uses Jetpack Compose for the UI while adhering to Clean Architecture principles.

The focus of this project is not to showcase the full capabilities of Jetpack Compose but rather to provide a suggestion for building an extensible and maintainable app using the latest Android libraries and tools.

This template is suitable for both small and large applications. 

**Please note that this is a work in progress. More functionality and detailed documentation will be added in future updates.**

## Architecture
The **Android Template** app follows the Clean Architecture principles
and is described in detail in the
[architecture page](docs/ARCHITECTURE.md).

## Modularization
The **Android Template** app has been fully modularized. The modularization strategy is described in detail in the
[modularization page](docs/MODULARIZATION.md).

## Building the Project

To build this project, you'll need to have the following setup:

- **TMDB API Integration**:
This project uses the TMDB API for backend calls to fetch movie-related data. To use the API, you must register for an API key on the [TMDB Developer Portal](https://developer.themoviedb.org/docs/getting-started).

- **Adding the API Key**:
Once you have your TMDB API key, add the **API Read Access Token** by adding this line to your local.properties file (found in the root of your project): `TMDB_API_KEY=your_tmdb_read_access_token_here`

- **Build & Run**:
After adding the API Read Access Token, you can build and run the project as usual through Android Studio.
