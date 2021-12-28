package net.mouazkaadan.inshort.ui.newsPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.mouazkaadan.inshort.R
import net.mouazkaadan.inshort.databinding.NewsFragmentBinding
import net.mouazkaadan.inshort.ui.newsPage.model.Data
import net.mouazkaadan.inshort.ui.newsPage.model.NewsController
import net.mouazkaadan.inshort.ui.newsPage.model.OnNewsClickListener
import net.mouazkaadan.inshort.utils.asUri
import net.mouazkaadan.inshort.utils.showToast

@AndroidEntryPoint
class NewsFragment : Fragment() {

    private lateinit var viewBinding: NewsFragmentBinding
    val viewModel by viewModels<NewsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewBinding = DataBindingUtil.inflate(inflater, R.layout.news_fragment, null, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.viewModel = viewModel

        navArgs<NewsFragmentArgs>().value.apply {
            viewModel.getNews(category)
        }

        viewBinding.newsEpoxyRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val controller = NewsController(object : OnNewsClickListener<Data> {
            override fun onShareClick(item: Data?) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, item!!.readMoreUrl)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            override fun onItemClick(item: Data?) {
                val browserIntent = Intent(Intent.ACTION_VIEW, item!!.readMoreUrl.asUri())
                requireActivity().startActivity(browserIntent, null)
            }
        })

        viewBinding.newsEpoxyRv.setController(controller)

        viewModel.newsResponse.observe(viewLifecycleOwner, {
            viewBinding.progressBar.visibility = View.GONE
            controller.list = it.data as ArrayList<Data>
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            requireContext().showToast(it)
            findNavController().navigateUp()
        })
    }
}
