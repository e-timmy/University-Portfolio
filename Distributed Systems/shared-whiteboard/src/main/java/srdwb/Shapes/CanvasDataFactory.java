package srdwb.Shapes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import srdwb.message.JsonSerializationException;

public class CanvasDataFactory {
	private static CanvasDataFactory _instance = new CanvasDataFactory();
	private static ObjectMapper om;

	public static CanvasDataFactory getInstance() {
		return _instance;
	}

	private CanvasDataFactory() {
		om = new ObjectMapper();
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
				.allowIfBaseType(srdwb.Shapes.CanvasData.class)
				.allowIfBaseType(srdwb.Shapes.Shape.class)
				.allowIfBaseType(srdwb.Shapes.Point.class)
				.allowIfSubType(java.util.ArrayList.class)
				.allowIfSubType(java.util.HashMap.class)
				.build();
		om.activateDefaultTyping(ptv, DefaultTyping.NON_FINAL);
		om.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	}
	
	public ObjectMapper getObjectMapper() {
		return om;
	}

	/**
	 *
	 * @param state
	 * canvas state data to be converted to Json string
	 * @return
	 * Json string of given state
	 */
	public static String toJsonString(CanvasData canvas){
		String ret = "";
		try {
			ret = om.writeValueAsString(canvas);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public boolean writeToFile(CanvasData canvas, File outfile) {
		try {
			om.writeValue(outfile,canvas);
			return true;
		} catch (IOException e) {
			System.out.println("data write to file fail");
		}
		return false;
	}
	
	public CanvasData readFromFile(File inFile) {
		CanvasData data = null;
		try {
			data = om.readValue(inFile, CanvasData.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 *
	 * @param str
	 * Json string of a canvas state data
	 * @return
	 * reconstruction of canvas state data if json string is correctly formatted
	 */
	public static CanvasData fromJsonString(String str) throws JsonSerializationException {
		CanvasData canvas = null;
		try {
			canvas = om.readValue(str, CanvasData.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return canvas;
	}
	
	
}
