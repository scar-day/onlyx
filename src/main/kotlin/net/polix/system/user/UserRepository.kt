package net.polix.system.user

import net.polix.system.integration.IntegrationType
import net.polix.system.user.status.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByIdAndType(id: Long, type: IntegrationType): Optional<User>

    fun existsByIdAndType(id: Long, type: IntegrationType): Boolean

    fun countUserBy(): Int

    fun countUsersByStatus(status: Status): Int

    fun getUsersByStatus(status: Status): List<User>

    fun countAllByType(type: IntegrationType): Int


    fun existsUserById(Id: Long): Boolean

    fun getUserById(id: Long): User
}