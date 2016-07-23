package com.example.ahmadz.firebase.main.model;

import java.util.List;

/**
 * Created by ahmadz on 7/22/16.
 */
public class OrderMetaInfo {
	String person;
	List<OrderItemMetaInfo> itemList;

	public OrderMetaInfo(){

	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public List<OrderItemMetaInfo> getItemList() {
		return itemList;
	}

	public void setItemList(List<OrderItemMetaInfo> itemList) {
		this.itemList = itemList;
	}
}
