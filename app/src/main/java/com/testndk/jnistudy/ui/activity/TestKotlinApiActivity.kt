package com.testndk.jnistudy.ui.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.testndk.jnistudy.R
import com.testndk.jnistudy.ui.TestAdapter
import kotlinx.android.synthetic.main.activity_test_kotlin.*

class TestKotlinApiActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_kotlin)
        vpBrowser.run {
            adapter = TestAdapter(supportFragmentManager, arrayListOf("", "", "", ""))
        }
    }
}