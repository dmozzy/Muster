package com.github.dmozzy.muster.apilambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;

@Data
@DynamoDBTable(tableName = "MusterCall")
public class DynamoMusterCall {

	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_PENDING = "PENDING";
	public static final String STATUS_FAILED = "FAILED";
	
	@DynamoDBHashKey(attributeName = "requestReference")
	private String requestReference;
	@DynamoDBIndexHashKey(attributeName = "parentReference", globalSecondaryIndexName="ParentReference")
	private String parentReference;
	@DynamoDBAttribute(attributeName = "name")
	public String name;
	@DynamoDBAttribute(attributeName = "service")
	public String service;
	@DynamoDBAttribute(attributeName = "requestMessage")
	private String requestMessage;
	@DynamoDBAttribute(attributeName = "responseMessage")
	private String responseMessage;
	@DynamoDBAttribute(attributeName = "errorMessage")
	private String errorMessage;
	@DynamoDBAttribute(attributeName = "status")
	private String status;
	@DynamoDBAttribute(attributeName = "requestDate")
	private long requestDate;
	@DynamoDBAttribute(attributeName = "timing")
	private long timing;

}
