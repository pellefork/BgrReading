package se.fork.bgrreading.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import kotlinx.android.synthetic.main.listitem_session.view.*
import se.fork.bgrreading.MapsActivity
import se.fork.bgrreading.R
import se.fork.bgrreading.data.remote.Session
import se.fork.bgrreading.data.remote.SessionHeader
import se.fork.bgrreading.extensions.launchActivity
import timber.log.Timber

class SessionAdapter(options: FirebaseRecyclerOptions<SessionHeader>) : FirebaseRecyclerAdapter<SessionHeader, SessionViewHolder>(
    options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        return SessionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listitem_session, parent, false))
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int, sessionHeader: SessionHeader) {
        holder.bind(sessionHeader)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        Timber.d("onDataChanged")
    }

}

class SessionViewHolder(val customView: View, var sessionHeader: SessionHeader? = null) : RecyclerView.ViewHolder(customView) {
    val foregroundView = customView.card_foreground
    val backgroundView = customView.card_background

    fun bind(header: SessionHeader) {
        Timber.d("bind: $header")
        with(header) {
            customView.session_name?.text = header.name
            customView.locations_text?.text = header.nLocations.toString()
            customView.acceleration_text?.text = header.nAccelerations.toString()
            customView.rotations_text?.text = header.nRotations.toString()
            customView.setOnClickListener {
                customView.context.launchActivity<MapsActivity> {
                    putExtra("session", header)
                }
            }
        }
    }
}