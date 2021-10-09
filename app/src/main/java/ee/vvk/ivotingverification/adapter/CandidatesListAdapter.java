package ee.vvk.ivotingverification.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import ee.vvk.ivotingverification.R;
import ee.vvk.ivotingverification.model.Candidate;
import ee.vvk.ivotingverification.util.C;
import ee.vvk.ivotingverification.util.RegexMatcher;
import ee.vvk.ivotingverification.util.TriangleView;
import ee.vvk.ivotingverification.util.Util;

/**
 * Custom adapter.
 * 
 * @version 21.05.2013
 */
public class CandidatesListAdapter extends BaseAdapter {

	private final List<Candidate> entries;
	private final List<String> questionlist;
	private final LayoutInflater mInflater;
	private final Context context;

	private static final String TAG = CandidatesListAdapter.class
			.getSimpleName();

	public CandidatesListAdapter(Context context, List<Candidate> entries,
			List<String> questionList) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.entries = entries;
		this.questionlist = questionList;
		this.context = context;
	}

	@Override
	public int getCount() {
		return entries.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.list_item_candidate, parent, false);
			holder = new ViewHolder();
			convertView.setTag(holder);

			holder.candidateListItemContainer = convertView
					.findViewById(R.id.candidate_list_item_container);
			holder.electionTitleShadow = convertView
					.findViewById(R.id.election_title_label_shadow);
			holder.candidateListItemDetails = convertView
					.findViewById(R.id.candidate_list_item_details);
			holder.triangleLabel = convertView
					.findViewById(R.id.triangle_lbl);
			holder.electionTitle = convertView
					.findViewById(R.id.election_title_label);
			holder.candidateName = convertView
					.findViewById(R.id.candidate_name_text);
			holder.candidateParty = convertView
					.findViewById(R.id.candidate_party_text);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		GradientDrawable candidateListItemContainerD = (GradientDrawable) holder.candidateListItemContainer
				.getBackground();
		candidateListItemContainerD.setColor(Util
				.generateHexColorValue(C.lblOuterContainerBackground));

		holder.electionTitle.setTypeface(C.typeFace);
		holder.electionTitle.setTextColor(Util
				.generateHexColorValue(C.lblOuterContainerForeground));

		GradientDrawable candidateListItemDetailsD = (GradientDrawable) holder.candidateListItemDetails
				.getBackground();
		candidateListItemDetailsD.setColor(Util
				.generateHexColorValue(C.lblInnerContainerBackground));

		holder.electionTitleShadow.setBackgroundColor(Util
				.generateHexColorValue(C.lblOuterInnerContainerDivider));
		holder.candidateName.setTextColor(Util
				.generateHexColorValue(C.lblInnerContainerForeground));
		holder.candidateName.setTypeface(C.typeFace);
		holder.candidateParty.setTextColor(Util
				.generateHexColorValue(C.lblInnerContainerForeground));
		holder.candidateParty.setTypeface(C.typeFace);

		String tempNumber = "";

		if (!RegexMatcher.IsCandidateNumber(entries.get(position).number)) {
			Util.logDebug(TAG, "Wrong candidate number");
			Util.startErrorIntent((Activity) context, C.badServerResponseMessage);
		} else {
			tempNumber = entries.get(position).number.substring(
					entries.get(position).number.indexOf(".") + 1);
		}

		TriangleView cv = new TriangleView(context,
				Util.generateHexColorValue(C.lblInnerContainerBackground),
				tempNumber);
		cv.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		holder.triangleLabel.addView(cv);

		if (C.elections != null) {
			if (C.elections.get(questionlist.get(position)) != null) {
				holder.electionTitle.setText(C.elections
						.get(questionlist.get(position)));
			} else {
				holder.electionTitle.setText(questionlist.get(position));
			}
		} else {
			holder.electionTitle.setText(questionlist.get(position));
		}

		if (!RegexMatcher.IsLessThan101UtfChars(entries.get(position).name)) {
			Util.logDebug(TAG, "Wrong candidate name");
			Util.startErrorIntent((Activity) context, C.badServerResponseMessage);
		} else {
			holder.candidateName.setText(entries.get(position).name);
		}
		holder.candidateParty.setText(entries.get(position).party);

		return convertView;
	}

	public static class ViewHolder {
		private LinearLayout candidateListItemContainer;
		private View electionTitleShadow;
		private RelativeLayout candidateListItemDetails;
		private LinearLayout triangleLabel;
		private TextView electionTitle;
		private TextView candidateName;
		private TextView candidateParty;
	}
}
