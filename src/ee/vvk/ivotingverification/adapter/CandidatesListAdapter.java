/**
 * Copyright (C) 2013 Eesti Vabariigi Valimiskomisjon 
 * (Estonian National Electoral Committee), www.vvk.ee
 *
 * Written in 2013 by AS Finestmedia, www.finestmedia.ee
 * 
 * Vote-verification application for Estonian Internet voting system
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
 
package ee.vvk.ivotingverification.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ee.vvk.ivotingverification.R;
import ee.vvk.ivotingverification.model.Vote;
import ee.vvk.ivotingverification.model.Vote.Candidate;
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

	private List<Candidate> entries;
	private Vote vote;
	private LayoutInflater mInflater;
	private Context context;

	private static final String TAG = CandidatesListAdapter.class
			.getSimpleName();

	public CandidatesListAdapter(Context context, List<Candidate> entries,
			Vote vote) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.entries = entries;
		this.vote = vote;
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

			convertView = mInflater.inflate(R.layout.list_item_candidate, null);
			holder = new ViewHolder();
			convertView.setTag(holder);

			holder.candidateListItemContainer = (LinearLayout) convertView
					.findViewById(R.id.candidate_list_item_container);
			holder.electionTitleShadow = (View) convertView
					.findViewById(R.id.election_title_label_shadow);
			holder.candidateListItemDetails = (RelativeLayout) convertView
					.findViewById(R.id.candidate_list_item_details);
			holder.triangleLabel = (LinearLayout) convertView
					.findViewById(R.id.triangle_lbl);
			holder.electionTitle = (TextView) convertView
					.findViewById(R.id.election_title_label);
			holder.candidateName = (TextView) convertView
					.findViewById(R.id.candidate_name_text);
			holder.candidateParty = (TextView) convertView
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
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Wrong candidate number");
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
		} else {
			tempNumber = entries.get(position).number.substring(
					entries.get(position).number.indexOf(".") + 1,
					entries.get(position).number.length());
		}

		TriangleView cv = new TriangleView(context,
				Util.generateHexColorValue(C.lblInnerContainerBackground),
				tempNumber);
		cv.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		holder.triangleLabel.addView(cv);

		if (C.elections != null) {
			if (C.elections.get(vote.electionNames[position]) != null) {
				holder.electionTitle.setText(C.elections
						.get(vote.electionNames[position]));
			} else {
				holder.electionTitle.setText(vote.electionNames[position]);
			}
		} else {
			holder.electionTitle.setText(vote.electionNames[position]);
		}

		if (!RegexMatcher.IsLessThan101UtfChars(entries.get(position).name)) {
			if (Util.DEBUGGABLE) {
				Log.d(TAG, "Wrong candidate name");
			}
			Util.startErrorIntent((Activity) context,
					C.badServerResponseMessage, true);
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