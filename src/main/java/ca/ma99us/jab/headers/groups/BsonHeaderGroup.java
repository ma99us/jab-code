package ca.ma99us.jab.headers.groups;

import ca.ma99us.jab.headers.BsonArrayHeader;
import ca.ma99us.jab.headers.ChecksumHeader;
import ca.ma99us.jab.headers.JabHeader;
import ca.ma99us.jab.headers.ToStringHeader;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BsonHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    @JsonIgnore
    private final BsonArrayHeader<P> bsonHeader = new BsonArrayHeader<P>();
//    private final BsonMongoHeader<P> bsonHeader = new BsonMongoHeader<P>();
    @JsonIgnore
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, bsonHeader, toStringHeader};
    }
}
