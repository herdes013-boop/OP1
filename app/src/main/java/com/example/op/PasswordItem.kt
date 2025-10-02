package com.example.op

data class PasswordItem(
    val id: Int,
    val title: String, // Názov služby (napr. Google, Facebook)
    val username: String?, // Používateľské meno alebo e-mail
    val passwordEncrypted: String, // Heslo (šifrované)
    val notes: String? // Voliteľné poznámky
)
