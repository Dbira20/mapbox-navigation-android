package com.mapbox.navigation.ui.internal.summary;

import android.content.res.Configuration;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.navigation.ui.R;
import com.mapbox.navigation.ui.instruction.maneuver.ManeuverView;

class InstructionViewHolder extends RecyclerView.ViewHolder implements InstructionListView {

  private ManeuverView maneuverView;
  private TextView distanceText;
  private TextView primaryText;
  private TextView secondaryText;
  private View instructionLayoutText;

  InstructionViewHolder(@NonNull View itemView) {
    super(itemView);
    maneuverView = itemView.findViewById(R.id.maneuverView);
    distanceText = itemView.findViewById(R.id.stepDistanceText);
    primaryText = itemView.findViewById(R.id.stepPrimaryText);
    secondaryText = itemView.findViewById(R.id.stepSecondaryText);
    instructionLayoutText = itemView.findViewById(R.id.instructionLayoutText);
  }

  @Override
  public void updateManeuverViewTypeAndModifier(@NonNull String maneuverType, String maneuverModifier) {
    maneuverView.setManeuverTypeAndModifier(maneuverType, maneuverModifier);
  }

  @Override
  public void updateManeuverViewRoundaboutDegrees(@Nullable Float roundaboutAngle) {
    maneuverView.setRoundaboutAngle(roundaboutAngle);
  }

  @Override
  public void updateManeuverViewDrivingSide(String drivingSide) {
    maneuverView.setDrivingSide(drivingSide);
  }

  @Override
  public void updateDistanceText(SpannableString distanceText) {
    this.distanceText.setText(distanceText);
  }

  @Override
  public void updatePrimaryText(String primaryText) {
    this.primaryText.setText(primaryText);
  }

  @Override
  public void updatePrimaryMaxLines(int maxLines) {
    primaryText.setMaxLines(maxLines);
  }

  @Override
  public void updateSecondaryText(String secondaryText) {
    this.secondaryText.setText(secondaryText);
  }

  @Override
  public void updateSecondaryVisibility(int visibility) {
    secondaryText.setVisibility(visibility);
  }

  @Override
  public void updateBannerVerticalBias(float percentBias) {
    adjustBannerVerticalBias(percentBias);
  }

  @Override
  public void updateViewColors(int primaryTextColor, int secondaryTextColor,
                               int maneuverPrimaryColor, int maneuverSecondaryColor) {
    primaryText.setTextColor(primaryTextColor);
    secondaryText.setTextColor(secondaryTextColor);
    distanceText.setTextColor(secondaryTextColor);
    maneuverView.setPrimaryColor(maneuverPrimaryColor);
    maneuverView.setSecondaryColor(maneuverSecondaryColor);
  }

  private void adjustBannerVerticalBias(float percentBias) {
    int orientation = itemView.getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) instructionLayoutText.getLayoutParams();
      params.verticalBias = percentBias;
      instructionLayoutText.setLayoutParams(params);
    }
  }
}
