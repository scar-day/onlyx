package net.polix.system.dialog.entity

import java.awt.image.RenderedImage
import java.io.File

data class Photo(
    val message: String? = null,
    val file: File? = null,
    val photo: RenderedImage? = null
)