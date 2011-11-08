package com.robbix.mp5.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.robbix.mp5.ui.overlay.InputOverlay;

public class DisplayGroup extends HashSet<DisplayPanel>
{
	private static final long serialVersionUID = 1L;

	public boolean add(DisplayPanel panel)
	{
		List<InputOverlay> overlays = getOverlays();
		
		if (super.add(panel))
		{
			panel.completeOverlays();
			
			for (InputOverlay overlay : overlays)
				panel.pushOverlay(overlay);
			
			return true;
		}
		
		return false;
	}
	
	public void pushOverlay(InputOverlay overlay)
	{
		for (DisplayPanel panel : this)
			panel.pushOverlay(overlay);
	}
	
	public void completeOverlay(InputOverlay overlay)
	{
		for (DisplayPanel panel : this)
			panel.completeOverlay(overlay);
	}
	
	public void completeOverlays()
	{
		for (DisplayPanel panel : this)
			panel.completeOverlays();
	}
	
	public InputOverlay getCurrentOverlay()
	{
		return isEmpty() ? null : getFirst().getCurrentOverlay();
	}
	
	public List<InputOverlay> getOverlays()
	{
		return isEmpty() ? new ArrayList<InputOverlay>() : getFirst().getOverlays();
	}
	
	private DisplayPanel getFirst()
	{
		return iterator().next();
	}
}
