package com.aptitekk.TeamScreen.Frames.Util;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ImageButton {
	JLabel image;
	boolean enabled;

	public ImageButton(ImageIcon image) {
		this.image = new JLabel(image);
		this.enabled = true;
	}
	public ImageButton(ImageIcon image, boolean enabled) {
		this.image = new JLabel(image);
		this.enabled = enabled;
	}
	public ImageButton(ImageIcon image, boolean enabled, String toolTip) {
		this.image = new JLabel(image);
		this.enabled = enabled;
		this.image.setToolTipText(toolTip);
	}
	public ImageButton(ImageIcon image, String toolTip) {
		this.image = new JLabel(image);
		this.enabled = true;
		this.image.setToolTipText(toolTip);
	}

	public boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enable) {
		enabled = enable;
	}
	public JLabel getIconPane() {
		return image;
	}

}
