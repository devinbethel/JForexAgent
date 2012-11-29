package singlejartest;

import org.codehaus.jackson.annotate.JsonProperty;

public interface ITickMixin {
	@JsonProperty("ask") double getAsk();
	@JsonProperty("bid") double getBid();
	@JsonProperty("askVolume") double getAskVolume();
	@JsonProperty("bidVolume") double getBidVolume();
}
