package net.polix.system.user.status

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id

@Entity
data class Status(
    @Id
    val id: String = "",
    var priority: Int = 0,
    var displayName: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    val permissions: MutableList<String> = mutableListOf()

)