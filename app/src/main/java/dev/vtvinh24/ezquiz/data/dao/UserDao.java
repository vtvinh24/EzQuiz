package dev.vtvinh24.ezquiz.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import dev.vtvinh24.ezquiz.data.entity.UserEntity;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("SELECT * FROM user WHERE isLoggedIn = 1 LIMIT 1")
    LiveData<UserEntity> getCurrentUser();

    @Query("SELECT * FROM user WHERE isLoggedIn = 1 LIMIT 1")
    UserEntity getCurrentUserSync();

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM user WHERE id = :id LIMIT 1")
    UserEntity getUserById(String id);

    @Query("UPDATE user SET isLoggedIn = 0")
    void logoutAllUsers();

    @Query("DELETE FROM user")
    void deleteAllUsers();

    @Query("SELECT COUNT(*) FROM user WHERE isLoggedIn = 1")
    int getLoggedInUsersCount();
}
