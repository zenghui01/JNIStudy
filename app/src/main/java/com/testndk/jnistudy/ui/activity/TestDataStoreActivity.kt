package com.testndk.jnistudy.ui.activity

import android.view.View
import com.testndk.jnistudy.R
import com.testndk.jnistudy.bean.BookModel
import com.testndk.jnistudy.utils.getBookBf
import com.testndk.jnistudy.utils.saveBookPf
import kotlinx.android.synthetic.main.activity_test_store.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TestDataStoreActivity : BaseActivity() {

    override fun initLayout() = R.layout.activity_test_store


    fun onClickSave(v: View) {
        runBlocking {
            saveBookPf(BookModel("你好", 1f))
        }
    }

    fun onClickRead(v: View) {
        runBlocking {
            val bookModel = getBookBf().first()
            tv_content.text = bookModel.name
        }
    }
}