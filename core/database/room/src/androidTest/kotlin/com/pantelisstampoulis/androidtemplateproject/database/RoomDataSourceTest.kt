package com.pantelisstampoulis.androidtemplateproject.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.database.mapper.Mappers
import com.pantelisstampoulis.androidtemplateproject.database.mapper.MovieDbMapper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDataSourceTest {

    private lateinit var database: AppDatabase
    private lateinit var mappers: Mappers
    private lateinit var roomDataSource: RoomDataSource

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        mappers = Mappers(MovieDbMapper())

        roomDataSource = RoomDataSource(database, mappers)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertMovies() = runTest {
        // Given
        val movie = DatabaseTestDoubleFactory.provideMovieDbModel()

        // When
        roomDataSource.insertMovies(listOf(movie))

        // Then
        val movies = roomDataSource.getMovies().firstOrNull()
        assertThat(movies).contains(movie)
    }

    @Test
    fun getMovieByID() = runTest {
        // Given
        val movie1 = DatabaseTestDoubleFactory.provideMovieDbModel()
        val movie2 = DatabaseTestDoubleFactory.provideMovieDbModel()
        roomDataSource.insertMovies(listOf(movie1, movie2))

        // When
        val movie = roomDataSource.getMovie(movieId = movie2.id)

        // Then
        assertThat(movie).isNotNull()
        assertThat(movie?.id).isEqualTo(movie2.id)
    }
}
