package com.seniru.walley

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class CategoryFragment : Fragment(R.layout.layout_category) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.create_category_button).setOnClickListener {
            val createCategoryIntent =
                Intent(requireContext(), CreateCategoryActivity::class.java)
            startActivity(createCategoryIntent)
        }
    }

}
