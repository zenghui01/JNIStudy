package com.testndk.jnistudy.ui.activity

import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseActivity : AppCompatActivity(){
    abstract fun initLayout(): Int

    open val mDisposable = CompositeDisposable()

    open fun initView() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(initLayout())
        initView()
    }
}