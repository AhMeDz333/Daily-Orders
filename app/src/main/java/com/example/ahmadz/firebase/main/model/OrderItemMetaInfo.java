package com.example.ahmadz.firebase.main.model;

/**
 * Created by ahmadz on 7/22/16.
 */
public class OrderItemMetaInfo {
	String name;
	String comment;
	int quantity;

	public OrderItemMetaInfo(){}

	public OrderItemMetaInfo(String name) {
		this.name = name;
		quantity = 1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
