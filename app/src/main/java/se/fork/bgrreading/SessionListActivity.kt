package se.fork.bgrreading

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_session_list.*
import se.fork.bgrreading.adapters.SessionAdapter
import se.fork.bgrreading.data.remote.Session
import timber.log.Timber

class SessionListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_list)
        initializeRecycler()
    }

    override fun onStart() {
        super.onStart()
        fetchData()
    }

    private fun fetchData() {
        Timber.d("fetchData")
        val query = FirebaseDatabase.getInstance()
            .reference
            .child("sessions")
            .limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<Session>()
            .setQuery(query, Session::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SessionAdapter(options)
        recycler.adapter = adapter
        adapter.startListening()
    }

    private fun initializeRecycler() {
        val gridLayoutManager = GridLayoutManager(this, 1)
        gridLayoutManager.orientation = RecyclerView.VERTICAL

        recycler.apply {
            layoutManager = gridLayoutManager
        }
    }

}
