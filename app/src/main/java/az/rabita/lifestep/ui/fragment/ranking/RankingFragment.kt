package az.rabita.lifestep.ui.fragment.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentRankingBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.ranking.RankingViewModel
import kotlinx.coroutines.launch

class RankingFragment : Fragment() {

    private lateinit var binding: FragmentRankingBinding

    private val viewModel: RankingViewModel by viewModels()

    private val args by navArgs<RankingFragmentArgs>()

    private val rankingAdapter = RankingRecyclerAdapter(showMedals = true) {}
    private val rankingPagedAdapter = RankingPagedRecyclerAdapter {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRankingBinding.inflate(inflater)

        binding.apply {
            lifecycleOwner = this@RankingFragment
        }

        with(binding) {
            if (args.postId != null) textViewTitle.text = getString(R.string.step_donors)
            else textViewTitle.text = getString(R.string.champions)

            imageButtonBack.setOnClickListener { activity?.onBackPressed() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        if (args.postId != null) viewModel.fetchAllDonors(args.postId!!)
        else viewModel.fetchAllChampions()
    }

    private fun configureRecyclerView() = with(binding.recyclerViewChampions) {
        adapter = if (args.postId != null) rankingPagedAdapter else rankingAdapter
    }

    private fun observeData(): Unit = with(viewModel) {

        if (args.postId != null) {
            lifecycleScope.launch {
                args.postId?.let {
                    fetchAllDonors(it).observe(viewLifecycleOwner, Observer { data ->
                        rankingPagedAdapter.submitData(lifecycle, data)
                    })
                }
            }
        } else {
            listOfChampions.observe(viewLifecycleOwner, Observer {
                it?.let { rankingAdapter.submitList(it) }
            })
        }

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.let { activity ->
                    MessageDialog(MessageType.ERROR, it).show(
                        activity.supportFragmentManager,
                        ERROR_TAG
                    )
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

}