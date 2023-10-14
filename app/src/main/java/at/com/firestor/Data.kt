package at.com.firestor

import com.google.firebase.Timestamp

data class Data(
    var id: String? = null,
    val title: String? = null,
    val descriptor: String? = null,
    val description: Timestamp
)