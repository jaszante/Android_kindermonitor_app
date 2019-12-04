package nl.jastrix_en_coeninblix.kindermonitor_app.dataClasses

data class UserData (   val userID: Int,
                        val username: String,
                        val password: String,
                        val firstName: String,
                        val lastName: String,
                        val phoneNumber: String,
                        val email: String)