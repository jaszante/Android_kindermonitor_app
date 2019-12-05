package nl.jastrix_en_coeninblix.kindermonitor_app.observableToken

import java.util.*

class AuthTokenClass(val token: String)

class ObservableToken : Observable() {
    var authToken: String = ""
        fun changeToken(token : String){
            var newToken = AuthTokenClass(token)
            authToken = token
            //Call setChanges() prior to calling notifyObservers()
            setChanged() //Inherited from Observable()
            notifyObservers(newToken) //Inherited from Observable()
        }
}