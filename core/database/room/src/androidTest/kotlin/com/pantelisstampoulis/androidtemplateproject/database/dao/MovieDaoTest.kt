package com.pantelisstampoulis.androidtemplateproject.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.pantelisstampoulis.androidtemplateproject.database.AppDatabase
import com.pantelisstampoulis.androidtemplateproject.database.DatabaseTestDoubleFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: MovieDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.movieDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertMovies() = runTest {
        // Given
        val movie = DatabaseTestDoubleFactory.provideMovieEntity()

        // When
        dao.insertMovies(listOf(movie))

        // Then
        val movies = dao.getMovieEntities().firstOrNull()
        assertThat(movies).contains(movie)
    }

    @Test
    fun getMovieByID() = runTest {
        // Given
        val movie1 = DatabaseTestDoubleFactory.provideMovieEntity()
        val movie2 = DatabaseTestDoubleFactory.provideMovieEntity()
        dao.insertMovies(listOf(movie1, movie2))

        // When
        val movie = dao.getMovieEntity(movieId = movie2.id)

        // Then
        assertThat(movie).isNotNull()
        assertThat(movie?.id).isEqualTo(movie2.id)
    }



}