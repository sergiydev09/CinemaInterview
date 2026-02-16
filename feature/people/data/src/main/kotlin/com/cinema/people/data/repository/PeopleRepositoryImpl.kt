package com.cinema.people.data.repository

import com.cinema.core.data.datasource.SecureLocalDataSource
import com.cinema.core.data.datasource.get
import com.cinema.core.data.datasource.observe
import com.cinema.core.data.datasource.save
import com.cinema.people.data.datasource.PeopleRemoteDataSource
import com.cinema.people.data.mapper.PersonMapper.toDomain
import com.cinema.people.data.mapper.PersonMapper.toDomainList
import com.cinema.people.data.model.FavoritePersonDTO
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.model.PersonDetail
import com.cinema.people.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepositoryImpl @Inject constructor(
    private val remoteDataSource: PeopleRemoteDataSource,
    private val secureLocalDataSource: SecureLocalDataSource
) : PeopleRepository {

    override fun getTrendingPeople(timeWindow: String): Flow<List<Person>> =
        flow { emit(remoteDataSource.getTrendingPeople(timeWindow).toDomainList()) }
            .combine(
                secureLocalDataSource.observe<Map<Int, FavoritePersonDTO>>(KEY_FAVORITE_PEOPLE)
            ) { people, favorites ->
                val favoriteIds = favorites?.keys ?: emptySet()
                people.map { it.copy(isFavorite = it.id in favoriteIds) }
            }

    override suspend fun getPersonDetail(personId: Int): PersonDetail =
        remoteDataSource.getPersonDetail(personId).toDomain()

    override fun isPersonFavorite(personId: Int): Flow<Boolean> =
        secureLocalDataSource.observe<Map<Int, FavoritePersonDTO>>(KEY_FAVORITE_PEOPLE)
            .map { it?.containsKey(personId) ?: false }

    override suspend fun toggleFavoritePerson(id: Int, name: String, profileUrl: String?) {
        val current = secureLocalDataSource.get<Map<Int, FavoritePersonDTO>>(KEY_FAVORITE_PEOPLE) ?: emptyMap()
        val updated = if (current.containsKey(id)) current - id
        else current + (id to FavoritePersonDTO(id, name, profileUrl))
        secureLocalDataSource.save(KEY_FAVORITE_PEOPLE, updated)
    }

    companion object {
        private const val KEY_FAVORITE_PEOPLE = "favorite_people"
    }
}
