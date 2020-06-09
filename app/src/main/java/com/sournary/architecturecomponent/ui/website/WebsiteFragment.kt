package com.sournary.architecturecomponent.ui.website

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.sournary.architecturecomponent.R
import com.sournary.architecturecomponent.databinding.FragmentWebsiteBinding
import com.sournary.architecturecomponent.ui.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_website.*

/**
 * The class represents website screen that contain a Toolbar and a WebView.
 */
class WebsiteFragment : BaseFragment<FragmentWebsiteBinding, WebsiteViewModel>() {

    private var title: String = ""
    private var url: String = ""

    override val viewModel: WebsiteViewModel by viewModels()

    override val layoutId: Int = R.layout.fragment_website

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val safeArgs: WebsiteFragmentArgs by navArgs()
        title = safeArgs.title
        url = safeArgs.url
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnApplyWindowInsetsListener { _, insets ->
            view.updatePadding(top = insets.systemWindowInsetTop)
            insets
        }
        NavigationUI.setupWithNavController(toolbar, navController)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupWebsite()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebsite() {
        // website.loadUrl(url)
        website.settings.javaScriptEnabled = true
    }

}
