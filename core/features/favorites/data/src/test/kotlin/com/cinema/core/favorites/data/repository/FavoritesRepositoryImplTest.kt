package com.cinema.core.favorites.data.repository

import app.cash.turbine.test
import com.cinema.core.data.datasource.SecureLocalDataSource
import com.cinema.core.favorites.data.model.FavoriteMovieEntity
import com.cinema.core.favorites.data.model.FavoritePersonEntity
import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.model.FavoritePerson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KType

class FavoritesRepositoryImplTest {

    private lateinit var secureLocalDataSource: SecureLocalDataSource
    private lateinit var repository: FavoritesRepositoryImpl

    @Before
    fun setup() {
        secureLocalDataSource = mockk(relaxed = true)
        coEvery { secureLocalDataSource.get<List<FavoriteMovieEntity>>(any(), any<KType>()) } returns null
        coEvery { secureLocalDataSource.get<List<FavoritePersonEntity>>(any(), any<KType>()) } returns null
        repository = FavoritesRepositoryImpl(secureLocalDataSource)
    }

    @Test
    fun `favoriteMovies initially returns empty list`() = runTest {
        repository.favoriteMovies.test {
            assertEquals(emptyList<FavoriteMovie>(), awaitItem())
        }
    }

    @Test
    fun `addFavoriteMovie adds movie and saves to secure storage`() = runTest {
        val movie = FavoriteMovie(
            id = 1,
            title = "Test Movie",
            posterUrl = "https://example.com/poster.jpg",
            releaseDate = "2024-01-01"
        )

        repository.favoriteMovies.test {
            assertEquals(emptyList<FavoriteMovie>(), awaitItem())

            repository.addFavoriteMovie(movie)

            val favorites = awaitItem()
            assertEquals(1, favorites.size)
            assertEquals(movie, favorites.first())
        }

        coVerify { secureLocalDataSource.save(eq("favorite_movies"), any<List<FavoriteMovieEntity>>(), any<KType>()) }
    }

    @Test
    fun `removeFavoriteMovie removes movie and saves to secure storage`() = runTest {
        val movie = FavoriteMovie(
            id = 1,
            title = "Test Movie",
            posterUrl = "https://example.com/poster.jpg",
            releaseDate = "2024-01-01"
        )

        repository.favoriteMovies.test {
            skipItems(1) // Skip initial empty list

            repository.addFavoriteMovie(movie)
            awaitItem() // Skip added state

            repository.removeFavoriteMovie(movie.id)

            val favorites = awaitItem()
            assertTrue(favorites.isEmpty())
        }

        coVerify(exactly = 2) { secureLocalDataSource.save(eq("favorite_movies"), any<List<FavoriteMovieEntity>>(), any<KType>()) }
    }

    @Test
    fun `isMovieFavorite returns true when movie is favorite`() = runTest {
        val movie = FavoriteMovie(
            id = 1,
            title = "Test Movie",
            posterUrl = null,
            releaseDate = null
        )

        repository.isMovieFavorite(1).test {
            assertFalse(awaitItem())

            repository.addFavoriteMovie(movie)

            assertTrue(awaitItem())
        }
    }

    @Test
    fun `favoritePeople initially returns empty list`() = runTest {
        repository.favoritePeople.test {
            assertEquals(emptyList<FavoritePerson>(), awaitItem())
        }
    }

    @Test
    fun `addFavoritePerson and removeFavoritePerson work correctly`() = runTest {
        val person = FavoritePerson(
            id = 1,
            name = "Test Person",
            profileUrl = null
        )

        repository.favoritePeople.test {
            assertEquals(emptyList<FavoritePerson>(), awaitItem())

            repository.addFavoritePerson(person)
            assertEquals(1, awaitItem().size)

            repository.removeFavoritePerson(person.id)
            assertTrue(awaitItem().isEmpty())
        }

        coVerify(exactly = 2) { secureLocalDataSource.save(eq("favorite_people"), any<List<FavoritePersonEntity>>(), any<KType>()) }
    }

    @Test
    fun `init loads favorites from secure storage`() = runTest {
        // Just verify that init calls SecureLocalDataSource.get
        // We can't easily test the async result in unit tests with Dispatchers.IO
        coVerify(timeout = 1000) { secureLocalDataSource.get<List<FavoriteMovieEntity>>(eq("favorite_movies"), any<KType>()) }
        coVerify(timeout = 1000) { secureLocalDataSource.get<List<FavoritePersonEntity>>(eq("favorite_people"), any<KType>()) }
    }

    @Test
    fun `isPersonFavorite returns true when person is favorite`() = runTest {
        val person = FavoritePerson(
            id = 1,
            name = "Test Person",
            profileUrl = "https://example.com/profile.jpg"
        )

        repository.isPersonFavorite(1).test {
            assertFalse(awaitItem())

            repository.addFavoritePerson(person)

            assertTrue(awaitItem())
        }
    }

    @Test
    fun `isPersonFavorite returns false when person is removed`() = runTest {
        val person = FavoritePerson(
            id = 1,
            name = "Test Person",
            profileUrl = null
        )

        repository.isPersonFavorite(1).test {
            assertFalse(awaitItem())

            repository.addFavoritePerson(person)
            assertTrue(awaitItem())

            repository.removeFavoritePerson(person.id)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `isMovieFavorite returns false when movie is removed`() = runTest {
        val movie = FavoriteMovie(
            id = 1,
            title = "Test Movie",
            posterUrl = null,
            releaseDate = null
        )

        repository.isMovieFavorite(1).test {
            assertFalse(awaitItem())

            repository.addFavoriteMovie(movie)
            assertTrue(awaitItem())

            repository.removeFavoriteMovie(movie.id)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `init loads existing movies from secure storage`() = runTest {
        val storedEntities = listOf(
            FavoriteMovieEntity(
                id = 1,
                title = "Stored Movie",
                posterUrl = "https://example.com/poster.jpg",
                releaseDate = "2024-01-01"
            )
        )

        coEvery { secureLocalDataSource.get<List<FavoriteMovieEntity>>(eq("favorite_movies"), any<KType>()) } returns storedEntities

        val newRepository = FavoritesRepositoryImpl(secureLocalDataSource)

        newRepository.favoriteMovies.test {
            val movies = awaitItem()
            // May emit empty list first, then loaded data
            if (movies.isEmpty()) {
                val loadedMovies = awaitItem()
                assertEquals(1, loadedMovies.size)
                assertEquals("Stored Movie", loadedMovies.first().title)
            } else {
                assertEquals(1, movies.size)
                assertEquals("Stored Movie", movies.first().title)
            }
        }
    }

    @Test
    fun `init loads existing people from secure storage`() = runTest {
        val storedEntities = listOf(
            FavoritePersonEntity(
                id = 1,
                name = "Stored Person",
                profileUrl = "https://example.com/profile.jpg"
            )
        )

        coEvery { secureLocalDataSource.get<List<FavoritePersonEntity>>(eq("favorite_people"), any<KType>()) } returns storedEntities

        val newRepository = FavoritesRepositoryImpl(secureLocalDataSource)

        newRepository.favoritePeople.test {
            val people = awaitItem()
            // May emit empty list first, then loaded data
            if (people.isEmpty()) {
                val loadedPeople = awaitItem()
                assertEquals(1, loadedPeople.size)
                assertEquals("Stored Person", loadedPeople.first().name)
            } else {
                assertEquals(1, people.size)
                assertEquals("Stored Person", people.first().name)
            }
        }
    }

    @Test
    fun `favoriteMovies maps entity fields correctly to domain`() = runTest {
        val movie = FavoriteMovie(
            id = 42,
            title = "Test Title",
            posterUrl = "https://example.com/poster.jpg",
            releaseDate = "2024-06-15"
        )

        repository.favoriteMovies.test {
            assertEquals(emptyList<FavoriteMovie>(), awaitItem())

            repository.addFavoriteMovie(movie)

            val favorites = awaitItem()
            val result = favorites.first()

            assertEquals(42, result.id)
            assertEquals("Test Title", result.title)
            assertEquals("https://example.com/poster.jpg", result.posterUrl)
            assertEquals("2024-06-15", result.releaseDate)
        }
    }

    @Test
    fun `favoritePeople maps entity fields correctly to domain`() = runTest {
        val person = FavoritePerson(
            id = 42,
            name = "Test Name",
            profileUrl = "https://example.com/profile.jpg"
        )

        repository.favoritePeople.test {
            assertEquals(emptyList<FavoritePerson>(), awaitItem())

            repository.addFavoritePerson(person)

            val favorites = awaitItem()
            val result = favorites.first()

            assertEquals(42, result.id)
            assertEquals("Test Name", result.name)
            assertEquals("https://example.com/profile.jpg", result.profileUrl)
        }
    }

    @Test
    fun `removeFavoriteMovie only removes matching id`() = runTest {
        val movie1 = FavoriteMovie(id = 1, title = "Movie 1", posterUrl = null, releaseDate = null)
        val movie2 = FavoriteMovie(id = 2, title = "Movie 2", posterUrl = null, releaseDate = null)

        repository.favoriteMovies.test {
            skipItems(1) // Skip empty

            repository.addFavoriteMovie(movie1)
            awaitItem()

            repository.addFavoriteMovie(movie2)
            assertEquals(2, awaitItem().size)

            repository.removeFavoriteMovie(1)
            val remaining = awaitItem()
            assertEquals(1, remaining.size)
            assertEquals(2, remaining.first().id)
        }
    }

    @Test
    fun `removeFavoritePerson only removes matching id`() = runTest {
        val person1 = FavoritePerson(id = 1, name = "Person 1", profileUrl = null)
        val person2 = FavoritePerson(id = 2, name = "Person 2", profileUrl = null)

        repository.favoritePeople.test {
            skipItems(1) // Skip empty

            repository.addFavoritePerson(person1)
            awaitItem()

            repository.addFavoritePerson(person2)
            assertEquals(2, awaitItem().size)

            repository.removeFavoritePerson(1)
            val remaining = awaitItem()
            assertEquals(1, remaining.size)
            assertEquals(2, remaining.first().id)
        }
    }
}
