package net.polix.system.user

import jakarta.persistence.*
import net.polix.system.localization.LanguageType
import net.polix.system.integration.IntegrationType
import net.polix.system.user.status.Status

@Entity
data class User(
        @Id
        val id: Long,

        @Enumerated(EnumType.STRING)
        val type: IntegrationType,

        @ElementCollection(fetch = FetchType.EAGER)
        @MapKeyColumn(name = "peer_id")
        @CollectionTable(
                name = "dialogs_mapping", joinColumns = [
                        JoinColumn(name = "user_id", referencedColumnName = "id")
                ]
        )

        @Column(columnDefinition = "text")
        var dialogs: MutableMap<Int, String> = mutableMapOf(),

        @ManyToOne(cascade = [CascadeType.ALL])
        var status: Status = Status(id = "user"),

        @ElementCollection(fetch = FetchType.EAGER)
        val permissions: MutableList<String> = mutableListOf(),

        @Column(name = "lang")
        @Enumerated(EnumType.STRING)
        var lang: LanguageType = LanguageType.RU
) {

        fun has(permission: String): Boolean {
                val parts = permission.split('.')
                return (status.permissions.contains(permission) ||
                        permissions.contains(permission) ||
                        status.permissions.contains("*") ||
                        permissions.contains("*") ||
                        (parts.size >= 2 &&
                                permissions.contains("${parts[0]}.${parts[1]}.*") &&
                                permission.startsWith("${parts[0]}.${parts[1]}."))
                        )
        }





}
