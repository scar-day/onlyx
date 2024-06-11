package net.polix.system.user.status

import org.springframework.stereotype.Service

@Service
class StatusService(
    val repository: StatusRepository
) {


    fun getStatus(value: String): Status? = repository.findById(value).orElse(null)

    fun createStatus(
        name: String,
        priority: Int,
        displayName: String,
        permissions: MutableList<String> = mutableListOf()
    ) = Status(name, priority, displayName, permissions).also { repository.save(it)  }


    fun existsStatus(value: String): Boolean = repository.existsById(value)



}