package cz.tp.usousedu.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["login"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val login: String,
    val passwordHash: String,
    // UserRole.code (1 = customer, 2 = admin, 3 = owner)
    @ColumnInfo(defaultValue = "1") val role: Int = 1,
)
