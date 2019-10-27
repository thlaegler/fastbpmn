/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator.converter;

/**
 * Simple Coordinate with x and y
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class LayoutQuadrant {

	private int startX;
	private int startY;
	private int endX;
	private int endY;

	/**
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 */
	public LayoutQuadrant(int startX, int startY, int endX, int endY) {
		super();
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}

	/**
	 * @return the startX
	 */
	public int getStartX() {
		return startX;
	}

	/**
	 * @return the startY
	 */
	public int getStartY() {
		return startY;
	}

	/**
	 * @return the endX
	 */
	public int getEndX() {
		return endX;
	}

	/**
	 * @return the endY
	 */
	public int getEndY() {
		return endY;
	}

	/**
	 * @param startX
	 *            the startX to set
	 */
	public void setStartX(int startX) {
		this.startX = startX;
	}

	/**
	 * @param startY
	 *            the startY to set
	 */
	public void setStartY(int startY) {
		this.startY = startY;
	}

	/**
	 * @param endX
	 *            the endX to set
	 */
	public void setEndX(int endX) {
		this.endX = endX;
	}

	/**
	 * @param endY
	 *            the endY to set
	 */
	public void setEndY(int endY) {
		this.endY = endY;
	}

}
