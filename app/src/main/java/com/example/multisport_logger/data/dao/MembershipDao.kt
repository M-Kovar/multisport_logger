package com.example.multisport_logger.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.multisport_logger.data.entity.MonthlyMembership
import kotlinx.coroutines.flow.Flow

@Dao
interface MembershipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(membership: MonthlyMembership)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memberships: List<MonthlyMembership>)

    @Update
    suspend fun update(membership: MonthlyMembership)

    @Delete
    suspend fun delete(membership: MonthlyMembership)

    @Query("DELETE FROM monthly_memberships WHERE yearMonth = :yearMonth")
    suspend fun deleteByYearMonth(yearMonth: String)

    @Query("DELETE FROM monthly_memberships")
    suspend fun deleteAll()

    @Query("SELECT * FROM monthly_memberships WHERE yearMonth = :yearMonth")
    fun getMembershipForMonth(yearMonth: String): Flow<MonthlyMembership?>

    @Query("SELECT * FROM monthly_memberships WHERE yearMonth = :yearMonth")
    suspend fun getMembershipForMonthDirect(yearMonth: String): MonthlyMembership?

    @Query("SELECT * FROM monthly_memberships ORDER BY yearMonth DESC")
    fun getAllMemberships(): Flow<List<MonthlyMembership>>

    @Query("SELECT * FROM monthly_memberships ORDER BY yearMonth DESC")
    suspend fun getAllMembershipsList(): List<MonthlyMembership>
}
