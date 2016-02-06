/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.plaidapp.R;
import io.plaidapp.data.DataLoadingSubject;
import io.plaidapp.data.PaginatedDataManager;
import io.plaidapp.data.api.dribbble.FollowersDataManager;
import io.plaidapp.data.api.dribbble.ShotLikesDataManager;
import io.plaidapp.data.api.dribbble.model.Follow;
import io.plaidapp.data.api.dribbble.model.Like;
import io.plaidapp.data.api.dribbble.model.PlayerListable;
import io.plaidapp.data.api.dribbble.model.Shot;
import io.plaidapp.data.api.dribbble.model.User;
import io.plaidapp.ui.recyclerview.InfiniteScrollListener;
import io.plaidapp.ui.widget.BottomSheet;
import io.plaidapp.util.DribbbleUtils;
import io.plaidapp.util.glide.CircleTransform;

import static io.plaidapp.util.AnimUtils.getFastOutLinearInInterpolator;
import static io.plaidapp.util.AnimUtils.getLinearOutSlowInInterpolator;

public class PlayerSheet extends Activity {

    private static final int MODE_SHOT_LIKES = 1;
    private static final int MODE_FOLLOWERS = 2;
    private static final String EXTRA_MODE = "EXTRA_MODE";
    private static final String EXTRA_SHOT = "EXTRA_SHOT";
    private static final String EXTRA_USER = "EXTRA_USER";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            MODE_SHOT_LIKES,
            MODE_FOLLOWERS
    })
    @interface PlayerSheetMode { }

    @Bind(R.id.bottom_sheet) BottomSheet bottomSheet;
    @Bind(R.id.bottom_sheet_content) ViewGroup content;
    @Bind(R.id.title_bar) ViewGroup titleBar;
    @Bind(R.id.close) ImageView close;
    @Bind(R.id.title) TextView title;
    @Bind(R.id.player_list) RecyclerView playerList;
    @BindDimen(R.dimen.large_avatar_size) int largeAvatarSize;
    private @Nullable Shot shot;
    private @Nullable User player;
    private PaginatedDataManager dataManager;
    private LinearLayoutManager layoutManager;
    private PlayerAdapter adapter;
    private boolean showingClose = false;

    public static void start(Activity launching, Shot shot) {
        Intent starter = new Intent(launching, PlayerSheet.class);
        starter.putExtra(EXTRA_MODE, MODE_SHOT_LIKES);
        starter.putExtra(EXTRA_SHOT, shot);
        ActivityCompat.startActivity(launching, starter,
                ActivityOptionsCompat.makeSceneTransitionAnimation(launching).toBundle());
    }

    public static void start(Activity launching, User player) {
        Intent starter = new Intent(launching, PlayerSheet.class);
        starter.putExtra(EXTRA_MODE, MODE_FOLLOWERS);
        starter.putExtra(EXTRA_USER, player);
        ActivityCompat.startActivity(launching, starter,
                ActivityOptionsCompat.makeSceneTransitionAnimation(launching).toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_sheet);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        final @PlayerSheetMode int mode = intent.getIntExtra(EXTRA_MODE, -1);
        switch (mode) {
            case MODE_SHOT_LIKES:
                shot = intent.getParcelableExtra(EXTRA_SHOT);
                title.setText(getResources().getQuantityString(
                        R.plurals.fans,
                        (int) shot.likes_count,
                        NumberFormat.getInstance().format(shot.likes_count)));
                dataManager = new ShotLikesDataManager(this, shot.id) {
                    @Override
                    public void onLikesLoaded(List<Like> likes) {
                        adapter.addItems(likes);
                    }
                };
                break;
            case MODE_FOLLOWERS:
                player = intent.getParcelableExtra(EXTRA_USER);
                title.setText(getResources().getQuantityString(
                        R.plurals.follower_count,
                        player.followers_count,
                        NumberFormat.getInstance().format(player.followers_count)));
                dataManager = new FollowersDataManager(this, player.id) {
                    @Override
                    public void onFollowersLoaded(List<Follow> followers) {
                        adapter.addItems(followers);
                    }
                };
                break;
            default:
                throw new IllegalArgumentException("Unknown launch mode.");
        }

        bottomSheet.registerCallback(new BottomSheet.Callbacks() {
            @Override
            public void onSheetDismissed() {
                ActivityCompat.finishAfterTransition(PlayerSheet.this);
            }

            @Override
            public void onSheetPositionChanged(int sheetTop) {
                if (sheetTop == 0) {
                    showClose();
                } else {
                    hideClose();
                }
            }
        });

        layoutManager = new LinearLayoutManager(this);
        playerList.setLayoutManager(layoutManager);
        adapter = new PlayerAdapter(this);
        dataManager.registerCallback(adapter);
        playerList.setAdapter(adapter);
        playerList.addOnScrollListener(new InfiniteScrollListener(layoutManager, dataManager) {
            @Override
            public void onLoadMore() {
                dataManager.loadData();
            }
        });
        playerList.addOnScrollListener(titleElevation);
        playerList.setHasFixedSize(true);
        dataManager.loadData(); // kick off initial load
    }

    private RecyclerView.OnScrollListener titleElevation = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final boolean raiseTitleBar = dy > 0 || playerList.computeVerticalScrollOffset() != 0;
            titleBar.setActivated(raiseTitleBar); // animated via a StateListAnimator
        }
    };

    private void showClose() {
        if (showingClose) return;
        close.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200L)
                .setInterpolator(getLinearOutSlowInInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        close.setVisibility(View.VISIBLE);
                    }
                });
        showingClose = true;
    }

    private void hideClose() {
        if (!showingClose) return;
        close.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(200L)
                .setInterpolator(getFastOutLinearInInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        close.setVisibility(View.INVISIBLE);
                    }
                });
        showingClose = false;
    }

    @OnClick({ R.id.bottom_sheet, R.id.close })
    public void dismiss(View view) {
        if (view.getVisibility() != View.VISIBLE) return;
        bottomSheet.dismiss();
    }

    private class PlayerAdapter<T extends PlayerListable>
            extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements DataLoadingSubject.DataLoadingCallbacks{

        private static final int TYPE_PLAYER = 7;
        private static final int TYPE_LOADING = -1;
        private List<T> items;
        private boolean loading = true;
        private LayoutInflater layoutInflater;
        private CircleTransform circleTransform;

        PlayerAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
            items = new ArrayList<>();
            circleTransform = new CircleTransform(context);
            setHasStableIds(true);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_PLAYER:
                    return createPlayerViewHolder(parent);
                default: // TYPE_LOADING
                    return new LoadingViewHolder(
                            layoutInflater.inflate(R.layout.list_loading, parent, false));
            }
        }

        @NonNull
        private PlayerViewHolder createPlayerViewHolder(ViewGroup parent) {
            final PlayerViewHolder holder = new PlayerViewHolder(
                    layoutInflater.inflate(R.layout.player_item, parent, false));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final User user = items.get(holder.getAdapterPosition()).getPlayer();
                    final Intent player = new Intent(PlayerSheet.this, PlayerActivity.class);
                    player.putExtra(PlayerActivity.EXTRA_PLAYER, user);
                    final ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(PlayerSheet.this,
                                    Pair.create((View) holder.playerAvatar,
                                            getString(R.string.transition_player_avatar)),
                                    Pair.create((View) holder.playerName,
                                            getString(R.string.transition_player_name)));
                    ActivityCompat.startActivity(PlayerSheet.this, player, options.toBundle());
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == getLoadingMoreItemPosition()) return;
            bindPlayer(((PlayerViewHolder) holder), items.get(position));
        }

        private void bindPlayer(PlayerViewHolder holder, T player) {
            Glide.with(holder.itemView.getContext())
                    .load(player.getPlayer().getHighQualityAvatarUrl())
                    .transform(circleTransform)
                    .placeholder(R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .into(holder.playerAvatar);
            holder.playerName.setText(player.getPlayer().name);
            if (!TextUtils.isEmpty(player.getPlayer().bio)) {
                DribbbleUtils.parseAndSetText(holder.playerBio, player.getPlayer().bio);
            } else if (!TextUtils.isEmpty(player.getPlayer().location)) {
                holder.playerBio.setText(player.getPlayer().location);
            }
            holder.timeAgo.setText(
                    DateUtils.getRelativeTimeSpanString(player.getDateCreated().getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS));
        }

        @Override
        public int getItemViewType(int position) {
            if (position < getDataItemCount()
                    && getDataItemCount() > 0) {
                return TYPE_PLAYER;
            }
            return TYPE_LOADING;
        }

        @Override
        public long getItemId(int position) {
            if (getItemViewType(position) == TYPE_LOADING) {
                return -1L;
            }
            return items.get(position).getId();
        }

        public int getDataItemCount() {
            return items.size();
        }

        private int getLoadingMoreItemPosition() {
            return loading ? getItemCount() - 1 : RecyclerView.NO_POSITION;
        }

        @Override
        public int getItemCount() {
            return getDataItemCount() + (loading ? 1 : 0);
        }

        @Override
        public void dataStartedLoading() {
            if (loading) return;
            loading = true;
            notifyItemInserted(getLoadingMoreItemPosition());
        }

        @Override
        public void dataFinishedLoading() {
            if (!loading) return;
            final int loadingPos = getLoadingMoreItemPosition();
            loading = false;
            notifyItemRemoved(loadingPos);
        }

        public void addItems(List<T> newItems) {
            final int insertRangeStart = getDataItemCount();
            items.addAll(newItems);
            notifyItemRangeInserted(insertRangeStart, newItems.size());
        }
    }

    /* package */ static class PlayerViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.player_avatar) ImageView playerAvatar;
        @Bind(R.id.player_name) TextView playerName;
        @Bind(R.id.player_bio) TextView playerBio;
        @Bind(R.id.time_ago) TextView timeAgo;

        public PlayerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progress;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progress = (ProgressBar) itemView;
        }

    }
}
