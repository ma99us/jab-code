package ca.ma99us.jab.headers.groups;

import ca.ma99us.jab.headers.*;
import lombok.Data;

@Data
public class BsonHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    private final BsonArrayHeader<P> bsonHeader = new BsonArrayHeader<P>();
//    private final BsonMongoHeader<P> bsonHeader = new BsonMongoHeader<P>();
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, bsonHeader, toStringHeader};
    }
}
