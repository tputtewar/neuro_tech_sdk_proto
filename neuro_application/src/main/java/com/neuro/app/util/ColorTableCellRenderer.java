package com.neuro.app.util;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ColorTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 5152961981995932787L;
	private static final int SUBJECT_NAME_COL = 0;
	private static final int SUBJECT_APPEARED_TIMESTAMP_COL = 2;
	private static final int SUBJECT_STATE_COL = 2;

	private final Color DETECTED_COLOR = Color.GREEN;
	private final Color UNDETECTED_COLOR = new Color(230, 0, 0);
	private final Color DEFAULT_COLOR = Color.BLACK;
	private final Color ALT_ROW_COLOR = new Color(235, 245, 251);

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// renderAlternateBackground(row, table.getBackground(), table.getRowCount(),
		// table.getColumnCount());
		renderForeground(row, column, table.getForeground(), table.getRowCount(), value, table);

		// Centers the text
		setHorizontalAlignment(SwingConstants.LEFT);

		return this;
	}

	/**
	 * Renders the foreground. Render only if column is one of the predefined "PL"
	 * columns, using the helper method <code>getColor</code> to determine the color
	 * to render based on the <code>value</code>.
	 * 
	 * @param row
	 * @param col
	 * @param tableForeground
	 * @param rowCount
	 * @param value
	 */
	private void renderForeground(int row, int col, Color tableForeground, int rowCount, Object value, JTable table) {
		int state = (Integer) table.getModel().getValueAt(row, 3);
		if (col == SUBJECT_NAME_COL || col == SUBJECT_APPEARED_TIMESTAMP_COL) {
			setForeground(getColor(tableForeground, state));
		} else {
			setForeground(DEFAULT_COLOR);
		}
	}

	/**
	 * Helper method for the <code>renderForeground</code> method. Returns a
	 * <code>Color</code> base on the value.
	 * 
	 * @param value
	 * @param tableForeground
	 * @return
	 */
	private Color getColor(Color tableForeground, int state) {
		Color color = null;
		if (state == 2) {
			color = DETECTED_COLOR;
		} else if (state == 1) {
			color = UNDETECTED_COLOR;
		} else {
			color = DEFAULT_COLOR;
		}
		return color;
	}

	/**
	 * Rendered alternate background color. Check for odd rows numbers. The last row
	 * is rendered as the select <code>LAST_ROW_COLOR</code> color.
	 * 
	 * @param row
	 * @param tableBackground
	 * @param rowCount
	 */
	private void renderAlternateBackground(int row, Color tableBackground, int rowCount, int colCount) {

		for (int i = 0; i < colCount; i++) {
			if (row % 2 != 0) {
				setBackground(ALT_ROW_COLOR);
			} else {
				setBackground(tableBackground);
			}
		}
	}

}
