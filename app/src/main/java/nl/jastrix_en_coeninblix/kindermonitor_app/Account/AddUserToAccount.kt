package nl.jastrix_en_coeninblix.kindermonitor_app.Account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import nl.jastrix_en_coeninblix.kindermonitor_app.BaseActivityClass
import nl.jastrix_en_coeninblix.kindermonitor_app.R

class AddUserToAccount : BaseActivityClass() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user_to_account)
        this.setTitle(R.string.title_add_users)
    }
}
