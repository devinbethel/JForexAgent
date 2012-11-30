package singlejartest;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility=Visibility.NONE)
public interface ITickMixin {	
	@JsonProperty("ask") double getAsk();
	@JsonProperty("bid") double getBid();
	@JsonProperty("askVolume") double getAskVolume();
	@JsonProperty("bidVolume") double getBidVolume();		
}
