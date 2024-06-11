package net.polix.system.controllers.model


class StatusInfoResponse(
    val code: Int,
    val message: String,
)

class UserInfoResponse(
    val status: StatusInfoResponse,
    val permissions: List<String>,
    val lang: String,
    val group_info: GroupInfo
)

class GroupInfo(
    val id: String,

    val displayName: String,

    val priority: Int,

    val permissions: List<String>
)


class UserServersResponse(
    val status: StatusInfoResponse,
    val countServers: Int,
    val user_id: Long,
    val listServers: List<String?>
)