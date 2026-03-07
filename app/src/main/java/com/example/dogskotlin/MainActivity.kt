package com.example.dogskotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogskotlin.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DogAdapter
    private val dogImages = mutableListOf<String>()

    companion object {
        private const val WEB = "https://dog.ceo/api/breed/"

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(WEB)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.svDogs.setOnQueryTextListener(this)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = DogAdapter(dogImages)
        binding.rvDogs.layoutManager = LinearLayoutManager(this)
        binding.rvDogs.adapter = adapter
    }
    /*
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(WEB)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    */

    private fun getRetrofit(): Retrofit = retrofit

    private fun searchByName(query: String) {
        // CoroutineScope(Dispatchers.IO).launch {
        lifecycleScope.launch(Dispatchers.IO) {
            val call: Response<DogsResponse> =
                getRetrofit().create(APIService::class.java).getDogsByBreeds("$query/images")

            val puppies: DogsResponse? = call.body()
            // runOnUiThread {
            withContext(Dispatchers.Main) {
                if (call.isSuccessful) {
                    val images: List<String> = puppies?.images ?: emptyList()
                    dogImages.clear()
                    dogImages.addAll(images)
                    adapter.notifyDataSetChanged()
                } else {
                    showErrorDialog()
                }
                hideKeyboard()
            }
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (!query.isNullOrEmpty())
            searchByName(query.lowercase())
        return true
    }

    private fun showErrorDialog() {
        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.viewRoot.windowToken, 0)
    }
}