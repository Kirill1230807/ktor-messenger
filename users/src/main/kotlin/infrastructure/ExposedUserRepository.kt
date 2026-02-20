package com.example.infrastructure

import com.example.domain.User
import com.example.domain.UserRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 255)

    override val primaryKey = PrimaryKey(id)
}

class ExposedUserRepository : UserRepository {
    override suspend fun createUser(user: User): User = newSuspendedTransaction {
        val insertStatement = UserTable.insert {
            it[username] = user.username
            it[email] = user.email
            it[password] = user.passwordHash
        }
        user.copy(id = insertStatement[UserTable.id] ?: 0)
    }

    override suspend fun findByUsername(username: String): User? = newSuspendedTransaction {
        UserTable.selectAll().where { UserTable.username eq username }
            .map {
                User(it[UserTable.id], it[UserTable.username], it[UserTable.email], it[UserTable.password])
            }
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        UserTable.selectAll().where { UserTable.email eq email }
            .map {
                User(it[UserTable.id], it[UserTable.username], it[UserTable.email], it[UserTable.password])
            }
            .singleOrNull()
    }

    override suspend fun deleteUser(id: Int): Boolean = newSuspendedTransaction {
        val deletedRowCount = UserTable.deleteWhere { UserTable.id eq id }

        deletedRowCount > 0
    }
}