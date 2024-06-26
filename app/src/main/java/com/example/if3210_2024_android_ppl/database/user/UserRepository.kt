package com.example.if3210_2024_android_ppl.database.user

import androidx.lifecycle.LiveData

class UserRepository(private  val userDao: UserDao) {

    val readAllData: LiveData<List<User>> = userDao.readAllData()

    suspend fun addUser(user: User) {
        if (user.isActive == true) {
            userDao.setActiveUser(user.id ?: 0)
        }
        userDao.addUser(user)
    }

    suspend fun setActiveUser(userId: Int) {
        userDao.setActiveUser(userId)
    }

    suspend fun getActiveUserEmail(): String? {
        return userDao.getActiveUserEmail()
    }

    suspend fun getActiveUserId(): Int {
        return userDao.getActiveUserId()
    }

    suspend fun logout() {
        userDao.logout()
    }

}