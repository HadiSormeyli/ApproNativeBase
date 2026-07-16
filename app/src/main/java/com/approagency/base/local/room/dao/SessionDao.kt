package com.approagency.base.local.room.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.approagency.base.local.room.entity.SessionEntity
import com.approagency.base.model.session.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query(
        """
        SELECT *
        FROM session
        WHERE id = :id
        LIMIT 1
        """
    )
    fun observe(
        id: String = Session.ID
    ): Flow<SessionEntity?>


    @Query(
        """
        SELECT *
        FROM session
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun get(
        id: String = Session.ID
    ): SessionEntity?


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insert(
        entity: SessionEntity
    )

    @Delete
    suspend fun delete(
        entity: SessionEntity
    )

    @Query(
        """
        DELETE FROM session
        WHERE id=:id
        """
    )
    suspend fun clear(
        id: String = "key"
    )
}