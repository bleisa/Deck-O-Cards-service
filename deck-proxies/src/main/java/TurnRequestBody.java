import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A TurnRequestBody provides the body for a TakeTurn request
 */
public class TurnRequestBody {
    @JsonProperty("cardPlayed")
    private Card cardPlayed;
    @JsonProperty("how")
    private WayToPlay how;
    @JsonProperty("passedTo")
    private String passedTo;
    @JsonProperty("playedBy")
    private String playedBy;

    public TurnRequestBody(Card c, Player playedBy, WayToPlay how, Player passedTo) {
        this.cardPlayed = c;
        this.playedBy = playedBy.getName();
        this.how = how;
        this.passedTo = passedTo.getName();
    }

    public TurnRequestBody(Card c, String name, WayToPlay pass, String teammate) {
        this.cardPlayed = c;
        this.passedTo = teammate;
        this.how = pass;
        this.playedBy = name;
    }
}
