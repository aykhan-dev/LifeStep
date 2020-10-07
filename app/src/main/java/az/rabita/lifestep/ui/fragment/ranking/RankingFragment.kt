package az.rabita.lifestep.ui.fragment.ranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.NavGraphMainDirections
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentRankingBinding
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.viewModel.fragment.ranking.RankingViewModel
import jp.wasabeef.recyclerview.animators.ScaleInAnimator
import kotlinx.coroutines.launch

class RankingFragment : Fragment() {

    private lateinit var binding: FragmentRankingBinding

    private val viewModel by viewModels<RankingViewModel>()

    private val args by navArgs<RankingFragmentArgs>()

    private val navController by lazy { findNavController() }

    private val rankingPagedAdapter = RankingPagedRecyclerAdapter { ranker ->
        viewModel.cachedOwnProfileInfo.value?.let { info ->
            if (info.id == ranker.id) navController.navigate(NavGraphMainDirections.actionToOwnProfileFragment())
            else navController.navigate(NavGraphMainDirections.actionToOtherProfileFragment(ranker.id))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRankingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
        configureRecyclerView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchAllDonors(args.postId)
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@RankingFragment

        textViewTitle.text = getString(R.string.step_donors)
        imageButtonBack.setOnClickListener { activity?.onBackPressed() }
    }

    private fun configureRecyclerView(): Unit = with(binding.recyclerViewChampions) {
        adapter = rankingPagedAdapter
        itemAnimator = ScaleInAnimator().apply { addDuration = 100 }
    }

    private fun observeData(): Unit = with(viewModel) {

        //DON'T REMOVE THIS LINE ELSE IT WILL BE NULL
        cachedOwnProfileInfo.observe(viewLifecycleOwner, { })

        lifecycleScope.launch {
            args.postId.let {
                fetchAllDonors(it).observe(viewLifecycleOwner, { data ->
                    rankingPagedAdapter.submitData(lifecycle, data)
                })
            }
        }

        errorMessage.observe(viewLifecycleOwner, {
            it?.let { errorMsg ->
                MessageDialog.getInstance(errorMsg).show(
                    requireActivity().supportFragmentManager,
                    ERROR_TAG
                )
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpiredToken.observe(viewLifecycleOwner, {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

}