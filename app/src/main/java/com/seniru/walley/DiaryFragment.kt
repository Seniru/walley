package com.seniru.walley

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class DiaryFragment : Fragment(R.layout.layout_diary) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<TextView>(R.id.textView12) // Replace with actual button ID
        button.setOnClickListener {
            Toast.makeText(view.context, "Diary button clicked", Toast.LENGTH_SHORT).show()
        }
    }

}
