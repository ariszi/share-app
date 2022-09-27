package zi.aris.data_provider.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.*
import zi.aris.data_provider.domain.*
import javax.inject.Inject


class UserRepository @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserRepositoryContract {

    companion object {

        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_PASSWORD = stringPreferencesKey("user_password")
        private val USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        private val USER_LAST_NAME = stringPreferencesKey("user_last_name")
        private val USER_TELEPHONE = stringPreferencesKey("user_telephone")
        private val USER_PIN = stringPreferencesKey("user_pin")
        private val USER_PIN_CONFIRMED = booleanPreferencesKey("user_pin_confirmed")

        private val USER_NOT_CONFIRMED = "The pin you have entered doesn't match with the previous"

    }

    override suspend fun saveUsersEmailAndPassword(email: String, password: String) {
        dataStore.edit {
            it[USER_EMAIL] = email
            it[USER_PASSWORD] = password
        }
    }

    override suspend fun saveUsersInfo(firstName: String, lastName: String, telephone: String) {
        dataStore.edit {
            it[USER_FIRST_NAME] = firstName
            it[USER_LAST_NAME] = lastName
            it[USER_TELEPHONE] = telephone
        }
    }

    override suspend fun saveUsersPin(pin: String) {
        dataStore.edit {
            it[USER_PIN] = pin
        }
    }

    override suspend fun confirmUsersPin(pin: String): Result<Unit> {
        val usersInsertedPin = dataStore.data.first()[USER_PIN] ?: ""
        var isConfirmed: Boolean = false
        dataStore.edit {
            isConfirmed = usersInsertedPin == pin
            it[USER_PIN_CONFIRMED] = isConfirmed
        }
        return if (isConfirmed) {
            Result.Success(Unit)
        } else {
            ErrorWithMessage(USER_NOT_CONFIRMED)
        }
    }


    override suspend fun validateUsersPin(pin: String): Flow<Result<Unit>> {
        return flow<Result<Unit>> {
            val storedPin = dataStore.data.first()[USER_PIN] ?: ""
            if (storedPin == pin) {
                Result.Success(Unit)
            } else
                throw UnauthorisedException()
        }.catch { cause: Throwable ->
            cause.message?.let { ErrorWithMessage(it) } ?: GenericError
        }
    }

    override suspend fun cleanUserData() {
        dataStore.edit { it.clear() }
    }

    override suspend fun cleanUsersPin() {
        dataStore.edit { it.remove(USER_PIN) }
    }

    override suspend fun usersCredentials(): Flow<Result<User>> {
        return dataStore.data.map { preferences ->
            Result.Success(User(email = preferences[USER_EMAIL] ?: "", password = preferences[USER_EMAIL] ?: ""))
        }.catch { cause: Throwable -> cause.message?.let { ErrorWithMessage(it) } ?: GenericError }
    }

    override suspend fun isUserRegistered(): Boolean {
        return dataStore.data.first()[USER_PIN_CONFIRMED] ?: false
    }

    override suspend fun usersInfo(): Flow<Result<User>> {
        return dataStore.data.map { preferences ->
            Result.Success(
                User(
                    name = preferences[USER_FIRST_NAME] ?: "",
                    lastName = preferences[USER_LAST_NAME] ?: "",
                    telephone = preferences[USER_TELEPHONE] ?: ""
                )
            )
        }.catch { cause: Throwable -> cause.message?.let { ErrorWithMessage(it) } ?: GenericError }
    }
}
