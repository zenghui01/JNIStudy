import android.app.Activity
import android.view.View
import com.testndk.jnistudy.R

class FirstActivity : Activity() {
    override fun setContentView(view: View?) {
        super.setContentView(view)
        setContentView(R.layout.activity_first)
    }
}