package com.example.m1tmdbapp2023

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.example.m1tmdbapp2023.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var notificationManager: NotificationManager

class MainActivity : AppCompatActivity() {

    val LOGTAG = MainActivity::class.simpleName

    private lateinit var binding: ActivityMainBinding
    private lateinit var personPopularAdapter: PersonPopularAdapter
    private var persons = arrayListOf<Person>()
    private var totalResults = 0
    private var totalPages = Int.MAX_VALUE
    private var curPage = 1

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                persons[0].name?.let { personName ->
                    persons[0].popularity?.let { popularity ->
                        createPopularPersonNotification(personName, popularity)
                    }
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    persons[0].name?.let { personName ->
                        persons[0].popularity?.let { popularity ->
                            createPopularPersonNotification(personName, popularity)
                        }
                    }
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
                    // Show an explanation to the user why the permission is needed, and ask for the permission again.
                    Toast.makeText(this, "Permission is needed to send SMS", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
                }
            }
        }
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init recycler view
        binding.popularPersonRv.setHasFixedSize(true)
        binding.popularPersonRv.layoutManager = LinearLayoutManager(this)
        personPopularAdapter = PersonPopularAdapter(persons, this)
        binding.popularPersonRv.adapter = personPopularAdapter
        binding.popularPersonRv.addOnScrollListener(object :
            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {
                    if (curPage < totalPages) {
                        curPage++
                        loadPage(curPage)
                    }
                }
            }
        })

        loadPage(curPage)

    }

    private fun loadPage(page: Int) {
        val tmdbapi = ApiClient.instance.create(ITmdbApi::class.java)

        val call = tmdbapi.getPopularPerson(TMDB_API_KEY, page)
        binding.progressWheel.visibility = VISIBLE
        call.enqueue(object : Callback<PersonPopularResponse> {
            override fun onResponse(
                call: Call<PersonPopularResponse>,
                response: Response<PersonPopularResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(applicationContext, "Page $page loaded", Toast.LENGTH_SHORT)
                        .show()
                    persons.addAll(response.body()?.results!!)
                    totalResults = response.body()!!.totalResults!!
                    totalPages = response.body()!!.totalPages!!
                    personPopularAdapter.notifyDataSetChanged()
                    personPopularAdapter.setMaxPopularity()
                    binding.totalResultsTv.text =
                        getString(R.string.total_results_text, persons.size, totalResults)
                } else {
                    Log.e(LOGTAG, "Call to getPopularPerson failed with error ${response.code()}")
                }
                binding.progressWheel.visibility = GONE
            }

            override fun onFailure(call: Call<PersonPopularResponse>, t: Throwable) {
                Log.e(LOGTAG, "Call to getPopularPerson failed")
                binding.progressWheel.visibility = GONE
            }
        })
    }

    private fun createNotificationChannel() {
        val CHANNEL_ID = "popular_people_channel"
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }


    private fun createPopularPersonNotification(
        personName: String,
        score: Double
    ) {
        val NOTIFICATION_ID = 1
        val CHANNEL_ID = "popular_people_channel"
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Most Popular Person")
            .setContentText("$personName has a score of $score")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}