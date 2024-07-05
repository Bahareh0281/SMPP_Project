val host = hostEditText.text.toString()val port = portEditText.text.toString()
val username = usernameEditText.text.toString()val password = passwordEditText.text.toString()
val key = keyEditText.text.toString()val fixedKey = "1Q79Babrt983JHGS09312nsKJ!"
// Check if the entered key matches the fixed key
if (key != fixedKey) {    // Show toast message for incorrect key
    Toast.makeText(this, "The entered key is incorrect.", Toast.LENGTH_SHORT).show()    keyEditText.text.clear()
} else {    // Save configuration to shared preferences
    saveConfig(host, port, username, password, key)
    // Navigate to MainActivity upon successful save    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)}