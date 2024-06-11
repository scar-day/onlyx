package net.polix.system.user.status

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StatusRepository : JpaRepository<Status, String> {

    fun findByPriority(priority: Int): Optional<Status>

    fun deleteByPriority(priority: Int)

    fun existsByPriority(priority: Int): Boolean


}