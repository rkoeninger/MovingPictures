package com.robbix.mp5.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Use this modal dialog to let the user choose one string from a long list.
 */
public class JListDialog extends JDialog
{
	private static final long serialVersionUID = -5552359529097955550L;
	
	private JList list;
	private JButton setButton;
	private JButton cancelButton;
	
	private boolean cancelled = false;
	
	public static Object showDialog(
		Component parent,
		String labelText,
		String titleText,
		Object[] possibleValues,
		Object initialValue,
		String selectButtonText,
		String cancelButtonText)
	{
		if (selectButtonText == null)
			selectButtonText = "Select";
		
		if (cancelButtonText == null)
			cancelButtonText = "Cancel";
		
		if (titleText == null)
			titleText = "List Select";
		
		if (labelText == null)
			labelText = "Select an Item:";
		
		Frame frame = JOptionPane.getFrameForComponent(parent);
		
		JListDialog dialog = new JListDialog(
			frame,
			labelText,
			titleText,
			possibleValues,
			initialValue,
			selectButtonText,
			cancelButtonText
		);
		
		dialog.setVisible(true);
		dialog.dispose();
		
		return dialog.cancelled ? null : dialog.list.getSelectedValue();
	}
	
	public static Object showDialog(Object[] possibleValues)
	{
		return showDialog(
			null,
			null,
			null,
			possibleValues,
			null,
			null,
			null
		);
	}
	
	public static Object showDialog(
		Object[] possibleValues,
		Object initialValue)
	{
		return showDialog(
			null,
			null,
			null,
			possibleValues,
			initialValue,
			null,
			null
		);
	}
	
	public static Object[] showMultiSelectListDialog(
		Component parent,
		String labelText,
		String titleText,
		Object[] possibleValues,
		Object[] initialValues,
		String selectButtonText,
		String cancelButtonText)
	{
		if (selectButtonText == null)
			selectButtonText = "Select";
		
		if (cancelButtonText == null)
			cancelButtonText = "Cancel";
		
		if (titleText == null)
			titleText = "List Select";
		
		if (labelText == null)
			labelText = "Select an Item:";
		
		Frame frame = JOptionPane.getFrameForComponent(parent);
		
		JListDialog dialog = new JListDialog(
			frame,
			labelText,
			titleText,
			possibleValues,
			initialValues,
			selectButtonText,
			cancelButtonText
		);
		
		dialog.setVisible(true);
		dialog.dispose();
		
		return dialog.cancelled ? null : dialog.list.getSelectedValues();
	}
	
	public static Object[] showMultiSelectDialog(Object[] possibleValues)
	{
		return showMultiSelectListDialog(
			null,
			null,
			null,
			possibleValues,
			null,
			null,
			null
		);
	}
	
	public static Object[] showMultiSelectDialog(
		Object[] possibleValues,
		Object[] initialValues)
	{
		return showMultiSelectListDialog(
			null,
			null,
			null,
			possibleValues,
			(Object[])initialValues,
			null,
			null
		);
	}
	
	private JListDialog(
		Frame frame,
		String labelText,
		String titleText,
		Object[] possibleValues,
		Object initialValue,
		String selectButtonText,
		String cancelButtonText)
	{
		super(frame, titleText, true);
		
		init(
			labelText,
			titleText, 
			possibleValues,
			selectButtonText,
			cancelButtonText
		);
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedValue(initialValue, true);
        pack();
        setLocationRelativeTo(frame);
    }
	
	private JListDialog(
		Frame frame,
		String labelText,
		String titleText,
		Object[] possibleValues,
		Object[] initialValues,
		String selectButtonText,
		String cancelButtonText)
	{
		super(frame, titleText, true);
		
		init(
			labelText,
			titleText, 
			possibleValues,
			selectButtonText,
			cancelButtonText
		);
		
		if (initialValues != null)
		{
			Set<Integer> selectedIndexSet = new HashSet<Integer>();
			
			for (Object initialValue : initialValues)
			{
				for (int i = 0; i < possibleValues.length; ++i)
				{
					if (initialValue.equals(possibleValues[i]))
					{
						selectedIndexSet.add(i);
						break;
					}
				}
			}
			
			int[] selectedIndicies = new int[selectedIndexSet.size()];
			int i = 0;
			
			for (Integer index : selectedIndexSet)
			{
				selectedIndicies[i++] = index;
			}
	
			list.setSelectedIndices(selectedIndicies);
		}
		
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pack();
        setLocationRelativeTo(frame);
    }
	
	private void init(
		String labelText,
		String titleText,
		Object[] possibleValues,
		String selectButtonText,
		String cancelButtonText)
	{
		ActionListener buttonListener = new ButtonListener();
		MouseListener listClickListener = new ListMouseListener();
		WindowListener windowCloseListener = new WindowCloseListener();
		
		cancelButton = new JButton(cancelButtonText);
		cancelButton.addActionListener(buttonListener);
		
		setButton = new JButton(selectButtonText);
		setButton.addActionListener(buttonListener);
		
		list = new JList(possibleValues);
		list.addMouseListener(listClickListener);
		list.setLayoutOrientation(JList.VERTICAL);
		
		Object longValue = "";
		
		for (Object value : possibleValues)
		{
			if (value.toString().length() > longValue.toString().length())
				longValue = value;
		}
		
		if (longValue != null)
		{
			list.setPrototypeCellValue(longValue);
		}
		
		int listHeight = possibleValues.length * 20;
		
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, listHeight));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		
		getRootPane().setDefaultButton(setButton);
		
		JLabel label = new JLabel(labelText);
		label.setLabelFor(list);
		
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.add(label);
		listPane.add(Box.createRigidArea(new Dimension(0,5)));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);
        
        add(listPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);
        
        addWindowListener(windowCloseListener);
	}
    
    private class ButtonListener implements ActionListener
    {
	    public void actionPerformed(ActionEvent e)
	    {
	    	cancelled = ! e.getSource().equals(setButton);
	    	setVisible(false);
	    }
    }
    
    private class ListMouseListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            if (e.getClickCount() == 2)
            {
    	        setVisible(false);
            }
        }
    }
    
    private class WindowCloseListener extends WindowAdapter
    {
    	public void windowClosing(WindowEvent e)
    	{
    		cancelled = true;
    	}
    }
}