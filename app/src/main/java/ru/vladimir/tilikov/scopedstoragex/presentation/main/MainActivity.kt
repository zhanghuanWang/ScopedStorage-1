package ru.vladimir.tilikov.scopedstoragex.presentation.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.vladimir.tilikov.scopedstoragex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = ActivityMainBinding.inflate(layoutInflater).root
        setContentView(view)
        binding = ActivityMainBinding.bind(view)
    }
}