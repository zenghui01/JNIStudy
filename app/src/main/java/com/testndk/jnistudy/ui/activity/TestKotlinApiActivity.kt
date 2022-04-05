package com.testndk.jnistudy.ui.activity

import com.testndk.jnistudy.R
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TestKotlinApiActivity : BaseActivity() {
    override fun initLayout() = R.layout.activity_test_kotlin


    override fun initView() {
        super.initView()

        val data = 1643730593000

        println("yyy:  ${data.minutes},kkk: ${data.seconds} ")
    }
}