package se.fork.bgrreading.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TimePicker
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import kotlinx.android.synthetic.main.listitem_session.view.*
import se.fork.bgrreading.MapsActivity
import se.fork.bgrreading.R
import se.fork.bgrreading.data.remote.Session
import se.fork.bgrreading.extensions.launchActivity
import timber.log.Timber

class SessionAdapter(options: FirebaseRecyclerOptions<Session>) : FirebaseRecyclerAdapter<Session, SessionViewHolder>(
    options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        return SessionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listitem_session, parent, false))
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int, session: Session) {
        holder.bind(session)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        Timber.d("onDataChanged")
    }

}

class SessionViewHolder(val customView: View, var session: Session? = null) : RecyclerView.ViewHolder(customView) {

    fun bind(session: Session) {
        with(session) {
            customView.session_name?.text = session.name
            customView.locations_text?.text = session.locations.size.toString()
            customView.acceleration_text?.text = session.accelerations.size.toString()
            customView.rotations_text?.text = session.rotations.size.toString()
            customView.setOnClickListener {
                customView.context.launchActivity<MapsActivity> {
                    putExtra("session", session)
                }
            }
        }
    }
}