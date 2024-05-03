package srdwb.message;

import com.fasterxml.jackson.databind.*;
import srdwb.Shapes.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.jsontype.*;



/**
 * Singleton class used for Message serialization to json and json to Message
 *
 */
public class MessageFactory {
	private static MessageFactory _instance = new MessageFactory();
	private static ObjectMapper om;

	public static MessageFactory getInstance() {
		return _instance;
	}

	private MessageFactory() {
		om = new ObjectMapper();
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
				.allowIfSubType(Message.class)
				.allowIfBaseType(srdwb.Shapes.Shape.class)
				.allowIfBaseType(srdwb.Shapes.Point.class)
				.allowIfSubType(java.util.ArrayList.class)
				.allowIfBaseType(srdwb.message.GroupEntry.class)
				.allowIfSubType(srdwb.message.GroupEntry.class)
				.allowIfSubType(srdwb.message.User.class)
				.allowIfBaseType(srdwb.message.User.class)
				.allowIfSubType(java.util.HashMap.class)
				.allowIfBaseType(srdwb.group.GroupMember.class)
				.build();
		om.activateDefaultTyping(ptv, DefaultTyping.NON_FINAL);
		om.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	}
	
	public ObjectMapper getObjectMapper() {
		return om;
	}

	/**
	 *
	 * @param msg
	 * Message object to be converted to Json string
	 * @return
	 * Json string of given Message object
	 */
	public static String toJsonString(Message msg){
		String ret = "";
		try {
			ret = om.writeValueAsString(msg);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 *
	 * @param str
	 * Json string of a Message object
	 * @return
	 * reconstruction of the Message object if json string is correctly formatted
	 */
	public static Message fromJsonString(String str) throws JsonSerializationException {
		Message msg = null;
		try {
			msg = om.readValue(str, Message.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return msg;
	}
}
