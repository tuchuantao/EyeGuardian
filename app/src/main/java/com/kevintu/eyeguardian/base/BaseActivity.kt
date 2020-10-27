package com.kevintu.eyeguardian.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding


/**
 * Create by Kevin-Tu on 2019/5/30.
 */
open abstract class BaseActivity<T: ViewDataBinding>: AppCompatActivity() {

    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initBinding()

        if (binding != null) {
            setContentView(binding.root)
        }

        initView(savedInstanceState)
    }

    abstract fun initBinding(): T

    abstract fun initView(savedInstanceState: Bundle?)
}