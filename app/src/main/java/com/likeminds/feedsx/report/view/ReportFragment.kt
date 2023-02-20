package com.likeminds.feedsx.report.view

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.likeminds.feedsx.R
import com.likeminds.feedsx.databinding.FragmentReportBinding
import com.likeminds.feedsx.report.model.REPORT_TYPE_COMMENT
import com.likeminds.feedsx.report.model.REPORT_TYPE_POST
import com.likeminds.feedsx.report.model.ReportExtras
import com.likeminds.feedsx.report.model.ReportTagViewData
import com.likeminds.feedsx.report.view.adapter.ReportAdapter
import com.likeminds.feedsx.report.view.adapter.ReportAdapter.ReportAdapterListener
import com.likeminds.feedsx.report.viewmodel.ReportViewModel
import com.likeminds.feedsx.utils.ViewUtils
import com.likeminds.feedsx.utils.customview.BaseFragment
import com.likeminds.feedsx.utils.emptyExtrasException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportFragment : BaseFragment<FragmentReportBinding>(),
    ReportAdapterListener {

    private val viewModel: ReportViewModel by viewModels()

    override fun getViewBinding(): FragmentReportBinding {
        return FragmentReportBinding.inflate(layoutInflater)
    }

    companion object {
        const val TAG = "ReportFragment"
    }

    private lateinit var extras: ReportExtras
    private lateinit var mAdapter: ReportAdapter
    private var tagSelected: ReportTagViewData? = null

    override fun receiveExtras() {
        super.receiveExtras()
        extras = requireActivity().intent?.getBundleExtra("bundle")
            ?.getParcelable(ReportActivity.ARG_REPORTS)
            ?: throw emptyExtrasException(TAG)
    }

    override fun reportTagSelected(reportTagViewData: ReportTagViewData) {
        super.reportTagSelected(reportTagViewData)
        //check if [Others] is selected, edit text for reason should be visible
        binding.etOthers.isVisible = reportTagViewData.name.contains("Others", true)

        //replace list in adapter and only highlight selected tag
        mAdapter.replace(
            mAdapter.items()
                .map {
                    (it as ReportTagViewData).toBuilder()
                        .isSelected(it.id == reportTagViewData.id)
                        .build()
                })
    }

    override fun setUpViews() {
        super.setUpViews()
        initRecyclerView()
        initViewAsType()
        initListeners()
    }

    //setup recycler view
    private fun initRecyclerView() {
        mAdapter = ReportAdapter(this)
        val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
        binding.rvReport.layoutManager = flexboxLayoutManager
        binding.rvReport.adapter = mAdapter

        //TODO: testing data
        mAdapter.addAll(
            listOf(
                ReportTagViewData.Builder()
                    .id(1)
                    .name("Nudity")
                    .build(),

                ReportTagViewData.Builder()
                    .id(2)
                    .name("Hate Speech")
                    .build(),

                ReportTagViewData.Builder()
                    .id(3)
                    .name("Spam")
                    .build(),

                ReportTagViewData.Builder()
                    .id(4)
                    .name("Bad Language")
                    .build(),

                ReportTagViewData.Builder()
                    .id(5)
                    .name("Terrorism")
                    .build(),

                ReportTagViewData.Builder()
                    .id(6)
                    .name("Others")
                    .build()
            )
        )
    }

    //set headers and sub header as per report type
    private fun initViewAsType() {
        when (extras.type) {
            REPORT_TYPE_POST -> {
                binding.tvReportSubHeader.text = getString(R.string.report_sub_header, "post")
            }
            REPORT_TYPE_COMMENT -> {
                binding.tvReportSubHeader.text = getString(R.string.report_sub_header, "comment")
            }
        }
    }

    private fun initListeners() {
        binding.ivCross.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnPostReport.setOnClickListener {
            //get selected tag
            tagSelected = mAdapter.items()
                .map { it as ReportTagViewData }
                .find { it.isSelected }

            //get reason for [edittext]
            val reason = binding.etOthers.text?.trim().toString()
            val isOthersSelected = tagSelected?.name?.contains("Others", true)

            //if no tag is selected
            if (tagSelected == null) {
                ViewUtils.showShortSnack(
                    binding.root,
                    "Please select at least on report tag."
                )
                return@setOnClickListener
            }

            //if [Others] is selected but reason is empty
            if (isOthersSelected == true && reason.isEmpty()) {
                ViewUtils.showShortSnack(
                    binding.root,
                    "Please enter a reason."
                )
                return@setOnClickListener
            }
        }
    }
}