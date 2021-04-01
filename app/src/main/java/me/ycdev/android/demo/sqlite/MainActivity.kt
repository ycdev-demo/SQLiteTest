package me.ycdev.android.demo.sqlite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.ycdev.android.demo.sqlite.databinding.ActivityMainBinding
import me.ycdev.android.demo.sqlite.utils.AsyncHelper
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createDbData.setOnClickListener {
            AsyncHelper.addTask {
                Timber.tag(TAG).d("Inserting test data...")
                Timber.tag(TAG).d("Insert data done")
            }
        }

        binding.clearDbData.setOnClickListener {
            AsyncHelper.addTask {
                Timber.tag(TAG).d("Clearing test data...")
                Timber.tag(TAG).d("Clear data done")
            }
        }

        binding.queryDbData.setOnClickListener {
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
