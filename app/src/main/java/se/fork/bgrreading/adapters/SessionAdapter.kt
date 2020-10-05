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
import java.text.SimpleDateFormat
import java.util.*

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
        val duration = Date(header.endDate.time - header.startDate.time)
        with(header) {
            customView.session_name?.text = header.name
            customView.locations_text?.text = header.nLocations.toString()
            customView.acceleration_text?.text = header.nAccelerations.toString()
            customView.rotations_text?.text = header.nRotations.toString()
            customView.user_name_text.text = header.userName
            customView.device_name_text.text = header.deviceName
            customView.duration_text.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(duration)
            customView.setOnClickListener {
                customView.context.launchActivity<MapsActivity> {
                    putExtra("session", header)
                }
            }
            customView.dropdown_button.setOnClickListener {
                if (customView.divider.visibility.equals(View.GONE)) {
                    customView.divider.visibility = View.VISIBLE
                    customView.readings_values_pane.visibility = View.VISIBLE
                    customView.readings_labels_pane.visibility = View.VISIBLE
                    customView.dropdown_button.setImageResource(R.drawable.drop_up)
                } else {
                    customView.divider.visibility = View.GONE
                    customView.readings_values_pane.visibility = View.GONE
                    customView.readings_labels_pane.visibility = View.GONE
                    customView.dropdown_button.setImageResource(R.drawable.drop_down)
                }
                customView.requestLayout()
            }
        }
    }
}