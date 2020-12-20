import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A TurnRequestBody provides the body for a TakeTurn request
 */
public final class TurnRequestBody {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("cardPlayed")
    private final Card cardPlayed;
    @JsonProperty("how")
    private final WayToPlay how;
    @JsonProperty("passedTo")
    private final String passedTo;
    @JsonProperty("playedBy")
    private final String playedBy;

    /**
     * Constructs a new TurnRequestBody
     *
     * @param c the card that was played
     * @param playedBy the player who took the turn - cannot be null
     * @param how what they did
     * @param passedTo if they passed, the player they passed to
     */
    public TurnRequestBody(Card c, Player playedBy, WayToPlay how, Player passedTo) {
        if (playedBy == null) {
            throw new IllegalArgumentException("playedBy cannot be null");
        }
        this.cardPlayed = c;
        this.playedBy = playedBy.getName();
        this.how = how;
        if (passedTo != null) {
            this.passedTo = passedTo.getName();
        } else {
            this.passedTo = null;
        }
    }

    public String toJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this);
    }
}
