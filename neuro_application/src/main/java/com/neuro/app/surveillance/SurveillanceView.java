package com.neuro.app.surveillance;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.neurotec.biometrics.swing.NViewImage;
import com.neurotec.images.NImage;
import com.neurotec.samples.Subject;
import com.neurotec.surveillance.NSurveillanceZone;
import com.neurotec.surveillance.NSurveillanceZoneType;
import com.neurotec.swing.NView;

public class SurveillanceView extends NView {

	// ===========================================================
	// Static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	private static final Color RECTANGLE_COLOR = new Color(0x008000);
	private static final Color ZONE_COLOR = new Color(0x800000);
	private static final int RECTANGLE_DISPLAY_TIME = 1000;

	public static final String PROPERTY_CHANGE_DRAWING_MODE = SurveillanceView.class.getName() + "drawingMode";

	// ===========================================================
	// Private fields
	// ===========================================================

	private final NViewImage image;
	private Date frameTimestamp;
	private final Map<Integer, Subject> subjects;
	private NSurveillanceZone zone;
	private boolean drawingMode;

	// ===========================================================
	// Public constructor
	// ===========================================================

	public SurveillanceView() {
		image = new NViewImage(this);
		image.setRotateFlipImage(false);
		frameTimestamp = new Date(0);
		subjects = new ConcurrentHashMap<Integer, Subject>();
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	public void setFrame(NImage frameImage, Date timestamp) {
		if (frameImage == null) {
			this.image.setImage((Image) null);
			dataChanged(1, 1);
		} else {
			this.image.setImage(frameImage);
			dataChanged(this.image.getWidth(), this.image.getHeight(), this.image.isOddQuadrantRotate());
		}
		this.frameTimestamp = timestamp;
	}

	public void addSubject(Subject subject) {
		subjects.put(subject.getId(), subject);
		dataChanged();
	}

	public void removeSubject(int id) {
		subjects.remove(id);
		dataChanged();
	}

	public void setSubjectRectangle(int id, Rectangle rect, Date timestamp) {
		Subject s = subjects.get(id);
		s.setBoundingRectangle(rect, timestamp);
		dataChanged();
	}

	public NSurveillanceZone getZone() {
		return zone;
	}

	public void resetZone(NSurveillanceZoneType zoneType) {
		if (zoneType == null) {
			this.zone = null;
		} else {
			this.zone = new NSurveillanceZone();
			this.zone.setType(zoneType);
		}
		dataChanged();
	}

	public boolean isDrawingMode() {
		return drawingMode;
	}

	public void setDrawingMode(boolean drawingMode) {
		boolean oldValue = this.drawingMode;
		this.drawingMode = drawingMode;
		firePropertyChange(PROPERTY_CHANGE_DRAWING_MODE, oldValue, this.drawingMode);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		prepareGraphics(g2d, image.getTransform());
		super.paint(g2d);
		g2d.dispose();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (!image.isEmpty()) {
			g2d.drawImage(image.getImage(), 0, 0, this);
		}

		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(RECTANGLE_COLOR);
		for (Map.Entry<Integer, Subject> entry : subjects.entrySet()) {
			Subject subject = entry.getValue();
			if (frameTimestamp.getTime() - subject.getLastUpdate().getTime() < RECTANGLE_DISPLAY_TIME) {
				Rectangle rect = subject.getBoundingRectangle();
				List<Point> points = new ArrayList<Point>();
				points.add(new Point(rect.x, rect.y));
				points.add(new Point(rect.x + rect.width, rect.y));
				points.add(new Point(rect.x + rect.width, rect.y + rect.height));
				points.add(new Point(rect.x, rect.y + rect.height));

				Polygon polygon = new Polygon();
				for (Point point : points) {
					polygon.addPoint(point.x, point.y);
				}
				g2d.drawPolygon(polygon);
			}
		}

		if (zone != null) {
			g2d.setColor(ZONE_COLOR);
			List<Point> zonePoints = zone.getPoints();
			Point[] zonePointsArr = zonePoints.toArray(new Point[zonePoints.size()]);
			int[] xPoints = new int[zonePointsArr.length];
			int[] yPoints = new int[zonePointsArr.length];
			int pointSize = (int) Math.rint(5 / getScale());
			int halfPointSize = pointSize / 2;
			for (int i = 0; i < zonePointsArr.length; i++) {
				xPoints[i] = zonePointsArr[i].x;
				yPoints[i] = zonePointsArr[i].y;
				g2d.fillOval(zonePointsArr[i].x - halfPointSize, zonePointsArr[i].y - halfPointSize, pointSize, pointSize);
			}
			g2d.drawPolyline(xPoints, yPoints, zonePointsArr.length);
		}
	}

	@Override
	public void mouseClicked(MouseEvent ev) {
		super.mouseClicked(ev);
		if (drawingMode) {
			int vertPadding = (int) Math.rint((getHeight() / getScale() - viewHeight) / 2);
			int horzPadding = (int) Math.rint((getWidth() / getScale() - viewWidth) / 2);
			int x = (int) Math.rint(ev.getX() / getScale()) - horzPadding;
			int y = (int) Math.rint(ev.getY() / getScale()) - vertPadding;
			zone.getPoints().add(new Point(x, y));

			if (ev.getClickCount() > 1) {
				if (zone.getType() == NSurveillanceZoneType.PATCH) {
					Point firstPoint = zone.getPoints().get(0);
					zone.getPoints().add(new Point(firstPoint));
				}
				setDrawingMode(false);
			}

			dataChanged();
		}
	}

}
