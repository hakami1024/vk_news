package com.hakami1024.vk_news;

import java.util.ArrayList;

import com.hakami1024.utils.FlowLayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

public class VKPostHolder {
	//private static String TAG = "VKPostHolder";
	public TextView publisherName = null;
	public TextView postTime = null;
	public ImageView publisherPhoto = null;
	public TextView mainText = null;
	public LinearLayout attachmentsView = null;
	public FlowLayout attachPhotosView = null;
	public TextView moreAttachPhotos = null;
	public LinearLayout repostsMainView = null;
	public ArrayList<ImageView> images = new ArrayList<ImageView>();
	public long time = -1;

	public Button likesButton = null;
	public Button repostsButton = null;
	public Context context = null;
	public boolean isRepost = false;
	public LinearLayout[] repostLayout = null;
	public VKPostHolder[] repostHolder = null;

	public VKPostHolder(View view, int repostsCount, boolean isRepost, long t) {
		context = view.getContext();
		time = t;
		publisherName = (TextView) view.findViewById(R.id.news_source_text);
		publisherPhoto = (ImageView) view.findViewById(R.id.news_source_image);
		postTime = (TextView) view.findViewById(R.id.news_time);
		mainText = (TextView) view.findViewById(R.id.news_body_text);
		likesButton = (Button) view.findViewById(R.id.news_like_button);
		repostsButton = (Button) view.findViewById(R.id.news_reposted_button);
		repostsMainView = (LinearLayout) view.findViewById(R.id.news_reposts);
		attachPhotosView = (FlowLayout) view.findViewById(R.id.news_images);
		moreAttachPhotos = (TextView) view.findViewById(R.id.news_more_images);
		attachmentsView = (LinearLayout) view
				.findViewById(R.id.news_attachments);

		this.isRepost = isRepost;

		resetPost(t, repostsCount, view);
	}

	public void resetPost(long time, int repostsCount, View view) {
		this.time = time;

		repostsMainView.removeAllViews();
		attachPhotosView.removeAllViews();
		attachmentsView.removeAllViews();

		repostsMainView.setVisibility(View.GONE);
		attachPhotosView.setVisibility(View.GONE);
		moreAttachPhotos.setVisibility(View.GONE);
		attachmentsView.setVisibility(View.GONE);

		repostsMainView.invalidate();
		attachPhotosView.invalidate();
		attachmentsView.invalidate();

		if (repostsCount != 0) {
			repostsMainView.setVisibility(View.VISIBLE);
			repostLayout = new LinearLayout[repostsCount];
			repostHolder = new VKPostHolder[repostsCount];

			for (int i = 0; i < repostsCount; i++) {
				LayoutInflater layoutInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				repostLayout[i] = (LinearLayout) layoutInflater.inflate(
						R.layout.news_repost, null);
				repostLayout[i].setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				repostLayout[i].setOrientation(LinearLayout.VERTICAL);
				repostsMainView.addView(repostLayout[i], i);
				repostHolder[i] = new VKPostHolder(repostLayout[i], 0, true,
						time);
			}
		} else {
			repostLayout = null;
			repostHolder = null;
		}
	}

}
