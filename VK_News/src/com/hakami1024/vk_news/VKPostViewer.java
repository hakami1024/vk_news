package com.hakami1024.vk_news;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hakami1024.utils.VKAudioPlayer;
import com.hakami1024.utils.VKPost;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiDocument;
import com.vk.sdk.api.model.VKApiLink;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiPhotoAlbum;
import com.vk.sdk.api.model.VKApiPoll;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKApiVideo;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKAttachments.VKApiAttachment;

public abstract class VKPostViewer {

	VKPost post;
	VKPostHolder holder;
	//private static final String TAG = "VKPostViewer";
	SparseArray<VKApiUser> users;
	SparseArray<VKApiCommunity> groups;
	boolean isFullVersion;

	VKPostViewer(VKPost post, SparseArray<VKApiUser> users,
			SparseArray<VKApiCommunity> groups, VKPostHolder holder,
			boolean isFullV) {
		this.post = post;
		this.holder = holder;
		this.users = users;
		this.groups = groups;
		isFullVersion = isFullV;
	}

	public void setOnView() {
		// Log.d(TAG, "setOnView()");
		viewMainInfo(holder, post);
		if (post.attachments != null)
			viewAttachments(holder, post);
	}

	private void viewMainInfo(final VKPostHolder holder, final VKPost post) {
		// Log.d(TAG, "viewMainInfo - beginning");
		viewSourceInfo(holder, post);

		if (isFullVersion) {
			OnClickListener listener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("http://www.vk.com/wall"
							+ post.source_id + "_" + post.post_id));
					holder.context.startActivity(i);
				}
			};
			holder.attachPhotosView.setOnClickListener(listener);
			holder.mainText.setOnClickListener(listener);
			holder.likesButton.setOnClickListener(listener);
			holder.repostsButton.setOnClickListener(listener);
			holder.repostsMainView.setOnClickListener(listener);
		}

		holder.postTime.setText(DateFormat.format("yyyy-MM-dd kk:mm",
				(post.date + 4 * 60 * 60) * 1000L)); // Moscow time
		
		if (post.text.length() > 280 && !isFullVersion) {
			String color = "" + holder.context.getResources().getColor( R.color.news_ref_color);
			holder.mainText.setText(Html.fromHtml(post.text.substring(0, 280)
					+ "...\n<font color=\"" + color + "\">Читать далее"));
			holder.mainText.setVisibility(View.VISIBLE);
		} else {
			if (post.text.equals(""))
				holder.mainText.setVisibility(View.GONE);
			else {
				holder.mainText.setText(post.text);
				holder.mainText.setVisibility(View.VISIBLE);
			}
		}

		if (isFullVersion)
			holder.mainText.setFocusable(true);
		else
			holder.mainText.setFocusable(false);

		if (!holder.isRepost) {
			if (post.likes_exists) {
				holder.likesButton.setVisibility(View.VISIBLE);
				holder.likesButton.setText("Нравится: " + post.likes_count);
			} else
				holder.likesButton.setVisibility(View.GONE);
			Button repostButton = holder.repostsButton;
			if (post.reposts_count > 0) {
				repostButton.setVisibility(View.VISIBLE);
				repostButton.setText("Репостов: " + post.reposts_count);
			} else
				repostButton.setVisibility(View.GONE);
		}

		if (post.copy_history != null && !post.copy_history.isEmpty()) {
			VKPost repost;
			VKPostHolder repostHolder;

			for (int i = 0, len = post.copy_history.getCount(); i < len; i++) {

				repost = post.copy_history.get(i);
				repostHolder = holder.repostHolder[i];
				viewMainInfo(repostHolder, repost);
				if (repost.attachments != null)
					viewAttachments(repostHolder, repost);
			}
		}
		if (post.geo != null) {
			if (holder.attachmentsView.findViewById(R.id.geoLayout) == null) {
				LinearLayout geoLayout = new LinearLayout(holder.context);
				geoLayout.setId(R.id.geoLayout);
				geoLayout.setOrientation(LinearLayout.HORIZONTAL);
				TextView geoTitle = new TextView(holder.context);
				LayoutParams layoutParam = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				ImageView geoIcon = new ImageView(holder.context);
				geoIcon.setImageResource(R.drawable.place);
				geoIcon.setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				geoTitle.setText(post.geo.title);
				geoTitle.setLayoutParams(layoutParam);
				geoLayout.addView(geoIcon);
				geoLayout.addView(geoTitle);
				((LinearLayout) holder.attachmentsView).addView(geoLayout,
						layoutParam);
			}

		}

	}

	private void viewSourceInfo(VKPostHolder holder, VKPost post) {
		// Log.d(TAG, "viewSourceInfo - beginning");
		if (post.source_id > 0) {
			VKApiUser user = (VKApiUser) users.get(post.source_id);
			holder.publisherName
					.setText(user.first_name + " " + user.last_name);
			ImageLoader.getInstance().displayImage(user.photo_50,
					holder.publisherPhoto);
		} else {
			// Log.d(TAG, "post.source_id = "+post.source_id);
			VKApiCommunity group = groups.get(-1 * post.source_id);
			// if( group == null )
			// Log.d(TAG, "group not found");
			holder.publisherName.setText(group.name);
			ImageLoader.getInstance().displayImage(group.photo_50,
					holder.publisherPhoto);
		}
		changeDataSet();
	}

	private void viewAttachments(VKPostHolder holder, VKPost post) {
		int imagesCount = countImages(post.attachments);
		if (imagesCount > 0) {
			holder.attachPhotosView.setVisibility(View.VISIBLE);
			if (imagesCount > 1 && !isFullVersion)
				holder.moreAttachPhotos.setVisibility(View.VISIBLE);
		}

		holder.attachmentsView.setVisibility(View.VISIBLE);
		boolean isImagesSetted = false;

		for (VKApiAttachment attachment : post.attachments) {
			if (attachment.getType().equals(VKAttachments.TYPE_ALBUM)) {
				if (!isImagesSetted || isFullVersion) {
					setAlbum((VKApiPhotoAlbum) attachment, holder);
					isImagesSetted = true;
					// Log.d(TAG, "album setted for "+post.text );
					// Log.i(TAG, attachment.fields.toString() );
				}

			} else if (attachment.getType().equals(VKAttachments.TYPE_APP)) {
			} else if (attachment.getType().equals(VKAttachments.TYPE_AUDIO)) {
				setAudio((VKApiAudio) attachment, holder);
			} else if (attachment.getType().equals(VKAttachments.TYPE_DOC)) {
				setDocument((VKApiDocument) attachment, holder);
			} else if (attachment.getType().equals(VKAttachments.TYPE_LINK)) {
				setLink((VKApiLink) attachment, holder);
			} else if (attachment.getType().equals(VKAttachments.TYPE_NOTE)) {
			} else if (attachment.getType().equals(VKAttachments.TYPE_PHOTO)) {
				if (!isImagesSetted || isFullVersion) {
					setPhoto((VKApiPhoto) attachment, holder);
					isImagesSetted = true;
					// Log.d(TAG, "image setted for "+post.text );
				}
			} else if (attachment.getType().equals(VKAttachments.TYPE_POLL)) {
				setPoll((VKApiPoll) attachment, holder);
			} else if (attachment.getType().equals(VKAttachments.TYPE_POST)) {
			} else if (attachment.getType().equals(
					VKAttachments.TYPE_POSTED_PHOTO)) {
			} else if (attachment.getType().equals(VKAttachments.TYPE_VIDEO)) {
				if (!isImagesSetted || isFullVersion) {
					setVideo((VKApiVideo) attachment, holder);
					isImagesSetted = true;
					// Log.d(TAG, "video setted for "+post.text );
				}
			} else if (attachment.getType()
					.equals(VKAttachments.TYPE_WIKI_PAGE)) {
			}
		}
	}

	abstract void changeDataSet();

	public int countImages(VKAttachments attach) {
		int ans = 0;
		for (VKApiAttachment a : attach)
			if (a.getType() == VKAttachments.TYPE_PHOTO
					|| a.getType() == VKAttachments.TYPE_VIDEO
					|| a.getType() == VKAttachments.TYPE_ALBUM)
				ans++;
		return ans;
	}

	public static int countReposts(VKPost post) {
		return post.copy_history == null || post.copy_history.isEmpty() ? 0
				: post.copy_history.getCount();
	}

	private LinearLayout.LayoutParams getParams(int w, int h) {
		LinearLayout.LayoutParams params = new LayoutParams(w, h);
		// params.setMargins(2, 5, 2, 5);
		return params;
	}

	protected void setAudio(final VKApiAudio audio, VKPostHolder holder) {
		LayoutInflater inflater = (LayoutInflater) holder.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout audioLayout = (RelativeLayout) inflater.inflate(
				R.layout.news_audio_item, null);

		final ImageButton musicButton = (ImageButton) audioLayout
				.findViewById(R.id.audio_icon);
		final SeekBar seekBar = (SeekBar) audioLayout
				.findViewById(R.id.audio_seekbar);
		TextView artistNameView = (TextView) audioLayout
				.findViewById(R.id.audio_artist_name_tv);
		TextView songNameView = (TextView) audioLayout
				.findViewById(R.id.audio_song_name_tv);
		artistNameView.setText(audio.artist);
		songNameView.setText(audio.title);
		final VKAudioPlayer player = VKAudioPlayer.getInstance();

		seekBar.setMax(audio.duration * 1000);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				player.seekTo(progress);
			}
		});

		musicButton.setImageResource(R.drawable.news_audio_btn_switcher);
		musicButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// musicButton.setSelected( !musicButton.isSelected() );
				if (musicButton.isSelected()) {
					player.pause();
				} else
					player.play(audio.url, seekBar, musicButton);

				changeDataSet();
			}
		});

		holder.attachmentsView.addView(audioLayout);

	}

	private void setAlbum(VKApiPhotoAlbum album, VKPostHolder holder) {
		LinearLayout albumLayout = new LinearLayout(holder.context);
		albumLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		albumLayout.setOrientation(LinearLayout.VERTICAL);
		albumLayout.setPadding(0, 0, 0, 10);

		ImageView imageView = new ImageView(holder.context);
		try {
			album.thumb_src = album.fields.getJSONObject("album")
					.getJSONObject("thumb").getString("photo_604");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// Log.d(TAG, album.description+", "+album.title+", "+album.thumb_src);
		ImageLoader.getInstance().displayImage(album.thumb_src, imageView);
		imageView.setAdjustViewBounds(true);
		imageView.setMaxWidth(320);
		imageView.setMaxHeight(240);
		LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		// imageParams.setMargins(2, 5, 2, 5);
		albumLayout.addView(imageView, imageParams);

		TextView infoView = new TextView(holder.context);
		infoView.setText(album.title + "\n" + album.size + " фото");
		albumLayout.addView(infoView);

		holder.attachPhotosView
				.addView(
						albumLayout,
						getParams(LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
	}

	protected void setDocument(final VKApiDocument document,
			final VKPostHolder holder) {
		LinearLayout layout = new LinearLayout(holder.context);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.HORIZONTAL);

		ImageView icon = new ImageView(holder.context);
		icon.setImageResource(R.drawable.ic_doc_up);
		TextView label = new TextView(holder.context);
		label.setText(document.title);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		lp.leftMargin = 10;
		layout.addView(icon);
		layout.addView(label, lp);

		if (isFullVersion)
			layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(document.url));
					holder.context.startActivity(i);
				}
			});
		holder.attachmentsView.addView(layout);
	}

	protected void setLink(final VKApiLink link, final VKPostHolder holder) {
		RelativeLayout linkLayout = new RelativeLayout(holder.context);
		RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		linkLayout.setLayoutParams(lparams);

		RelativeLayout.LayoutParams iparams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		ImageView iconView = new ImageView(holder.context);
		iconView.setId(R.id.link_image);
		iconView.setImageResource(R.drawable.link);
		linkLayout.addView(iconView, iparams);

		RelativeLayout.LayoutParams tparams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		tparams.leftMargin = 10;
		TextView textView = new TextView(holder.context);
		textView.setId(R.id.link_text);
		link.title = link.title.replaceAll("(\\t|\\r?\\n)", " ");
		textView.setText(link.title);
		textView.setMaxLines(1);
		tparams.addRule(RelativeLayout.RIGHT_OF, iconView.getId());
		linkLayout.addView(textView, tparams);

		TextView urlView = new TextView(holder.context);
		urlView.setId(R.id.link_url);
		urlView.setText(link.url);
		urlView.setMaxLines(1);

		// Log.d(TAG, link.title+" "+link.url);

		RelativeLayout.LayoutParams uparams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		uparams.leftMargin = 10;
		uparams.addRule(RelativeLayout.BELOW, textView.getId());
		uparams.addRule(RelativeLayout.RIGHT_OF, iconView.getId());
		linkLayout.addView(urlView, uparams);

		if (isFullVersion)
			linkLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(link.url));
					holder.context.startActivity(i);
				}
			});

		holder.attachmentsView.addView(linkLayout);
	}

	@SuppressWarnings("deprecation")
	protected void setPhoto(VKApiPhoto photo, VKPostHolder holder) {
		ImageView imageView = new ImageView(holder.context);
		Display display = ((WindowManager) holder.context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point screenSize = new Point();
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			display.getSize(screenSize);
		} else {
			screenSize.y = display.getHeight();
			screenSize.x = display.getWidth();
		}

		LinearLayout.LayoutParams imageParams;

		imageParams = (photo.width >= screenSize.x) ? new LayoutParams(
				screenSize.x, (int) (photo.height * screenSize.x
						/ (double) photo.width + 0.5)) : new LayoutParams(
				photo.width, photo.height);
		imageParams.setMargins(2, 5, 2, 5);
		imageView.setLayoutParams(imageParams);

		imageView.setScaleType(ScaleType.FIT_CENTER);
		ImageLoader.getInstance().displayImage(photo.photo_604, imageView);
		holder.attachPhotosView.addView(imageView);
	}

	protected void setPoll(final VKApiPoll poll, final VKPostHolder holder) {
		LinearLayout layout = new LinearLayout(holder.context);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.HORIZONTAL);

		ImageView icon = new ImageView(holder.context);
		icon.setImageResource(R.drawable.ic_poll_up);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		lp.leftMargin = 10;

		TextView label = new TextView(holder.context);
		label.setText(poll.question);

		layout.addView(icon);
		layout.addView(label, lp);

		holder.attachmentsView.addView(layout);

		if (isFullVersion) {
			layout = new LinearLayout(holder.context);
			layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));
			layout.setOrientation(LinearLayout.VERTICAL);

			// Log.d(TAG, "viewing full poll ");
			// Log.d(TAG, "Answers count:"+(poll.answers == null ? 0 :
			// poll.answers.getCount()) + " because of null: "+(poll.answers ==
			// null));

			if (poll.answers.getCount() == 0)
				try {
					JSONArray ans = poll.fields.getJSONObject("poll")
							.getJSONArray("answers");
					for (int i = 0; i < ans.length(); i++) {
						TextView answerText = new TextView(holder.context);
						int rate = (int) (ans.optJSONObject(i)
								.getDouble("rate"));
						answerText.setText(ans.optJSONObject(i).getString(
								"text")
								+ " - " + rate + "%");
						layout.addView(answerText);
						ProgressBar progressBar = new ProgressBar(
								holder.context, null,
								android.R.attr.progressBarStyleHorizontal);
						progressBar.setProgressDrawable(holder.context
								.getResources().getDrawable(
										R.drawable.progress_bar));
						progressBar.setMax(100);

						progressBar.setProgress(rate);
						progressBar.setEnabled(false);
						layout.addView(progressBar);
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			for (VKApiPoll.Answer ans : poll.answers) {
				TextView answerText = new TextView(holder.context);
				answerText.setText(ans.text);
				layout.addView(answerText);
				ProgressBar progressBar = new ProgressBar(holder.context, null,
						android.R.attr.progressBarStyleHorizontal);
				progressBar.setMax(100);
				progressBar.setProgress((int) (ans.rate * 100));
				progressBar.setEnabled(false);
				layout.addView(progressBar);
			}

			holder.attachmentsView.addView(layout);
		}

	}

	protected void setVideo(final VKApiVideo video, final VKPostHolder holder) {
		LinearLayout videoLayout = new LinearLayout(holder.context);
		videoLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		videoLayout.setOrientation(LinearLayout.VERTICAL);
		videoLayout.setPadding(0, 0, 0, 10);

		ImageView imageView = new ImageView(holder.context);
		ImageLoader.getInstance().displayImage(video.photo_320, imageView);
		imageView.setAdjustViewBounds(true);
		imageView.setMaxWidth(320);
		imageView.setMaxHeight(240);
		videoLayout.addView(imageView, new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		LinearLayout infoLayout = new LinearLayout(holder.context);
		infoLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		infoLayout.setOrientation(LinearLayout.HORIZONTAL);
		infoLayout.setBackgroundColor(Color.BLACK);

		ImageView icon = new ImageView(holder.context);
		icon.setImageResource(R.drawable.ic_left_video_down);

		TextView infoView = new TextView(holder.context);
		infoView.setText(" " + video.duration / 60 + ":" + video.duration % 60
				+ "   " + video.title);
		infoView.setTextColor(Color.WHITE);
		infoView.setGravity(Gravity.CENTER_HORIZONTAL);

		infoLayout.addView(icon);
		infoLayout.addView(infoView);
		videoLayout.addView(infoLayout);

		holder.attachPhotosView
				.addView(
						videoLayout,
						getParams(LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
	}

}
